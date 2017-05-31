(ns decent-news-distributor.messaging
  (:require [langohr.core :as lcore]
            [langohr.channel :as lchan]
            [langohr.basic :as lbasic]
            [langohr.consumers :as lconsumers]
            [langohr.queue :as lqueue]
            [mount.core :as mount]
            [clojure.edn :as edn]
            [clojure.core.async :as a]
            [taoensso.timbre :as log]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))


(def config {:queue {:opt {:declare {:durable true
                                     :exclusive false
                                     :auto-delete true}
                           :bind {:routing-key "slack.event.*"}
                           :subscribe {:auto-ack true}}
                     :name (str (java.util.UUID/randomUUID))
                     :exchange "amq.topic"}
             :broker {:host (env :rabbitmq-host)
                      :port (Integer/parseInt (env :rabbitmq-port))
                      :user (env :rabbitmq-user)
                      :pass (env :rabbitmq-pass)
                      :vhost (env :rabbitmq-vhost)}})

(defn start
  [config handler-fn]
  (log/info "Starting messaging subsystem...")
  (let [queue      (:queue config)
        broker     (:broker config)
        connection (lcore/connect broker)
        chan       (lchan/open connection)
        _          (lqueue/declare chan (:name queue) (-> queue :opt :declare))
        _          (lqueue/bind chan (:name queue) (:exchange queue) (-> queue :opt :bind))
        async-chan (a/chan)
        async-pub (a/pub async-chan (fn [m] :message.received))
        consumer (lconsumers/subscribe chan
                                       (:name queue)
                                       (partial handler-fn async-chan)
                                       (-> queue :opt :subscribe))]
   {:queue queue
    :broker broker
    :connection connection
    :chan chan
    :async-chan async-chan
    :async-pub async-pub
    :consumer consumer}))

(defn stop
  [messaging]
  (when messaging
    (let [{:keys [chan consumer async-chan connection]} messaging
          _ (lbasic/cancel chan consumer)
          _ (a/close! async-chan)
          _ (lcore/close chan)
          _ (lcore/close connection)])))

(defn publish
  [async-chan message]
  (a/>!! async-chan message) 
  (log/debug "Published message."))

(defn wrap-strip-message
  [next-fn]
  (fn [message]
    (next-fn {:topic (-> message :metadata :routing-key)
              :body  (-> message :payload)})))

(defn wrap-payload-to-edn [next-fn]
  (fn [message]
    (next-fn (assoc message :bytes-payload (:payload message)
                            :payload (json/read-str (String. (:payload message)) :key-fn keyword)))))

(defn message-handler
  [async-chan channel metadata ^bytes payload]
  (log/debug "Message received.")
  (let [publish (partial publish async-chan)
        executor (-> publish
                     wrap-strip-message
                     wrap-payload-to-edn)
        message  {:channel channel 
                  :metadata metadata 
                  :payload payload}
        _ (executor message)]))

(mount/defstate messaging :start (start config message-handler) :stop  (stop messaging))
