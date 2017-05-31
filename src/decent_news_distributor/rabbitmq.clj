(ns decent-news-distributor.rabbitmq
  (:require [langohr.core :as lcore]
            [langohr.channel :as lchan]
            [langohr.basic :as lbasic]
            [langohr.consumers :as lconsumers]
            [langohr.queue :as lqueue]
            [mount.core :as mount]
            [clojure.edn :as edn]
            [clojure.core.async :as a]
            [taoensso.timbre :as log]))


(def config {:queue {:opt {:declare {:durable true
                                     :exclusive false
                                     :auto-delete true}
                           :bind {:routing-key "slack.event.*"}
                           :subscribe {:auto-ack true}}
                     :name (str (java.util.UUID/randomUUID))
                     :exchange "amq.topic"}
             :broker {:host "localhost"
                      :port 5672
                      :user "guest"
                      :pass "guest"
                      :vhost "/"}})

(defn start
  [config handler-fn]
  (log/info "Starting rabbitmq subsystem...")
  (let [queue      (:queue config)
        broker     (:broker config)
        connection (lcore/connect broker)
        chan       (lchan/open connection)
        _          (lqueue/declare chan (:name queue) (-> queue :opt :declare))
        _          (lqueue/bind chan (:name queue) (:exchange queue) (-> queue :opt :bind))
        async-chan (a/chan)
        async-pub (a/pub async-chan #("incoming.message.news"))
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
  [rabbitmq]
  (when rabbitmq
   (lbasic/cancel (:chan rabbitmq) (:consumer rabbitmq))
   (a/close! (:async-chan rabbitmq))
   (lcore/close (:chan rabbitmq))
   (lcore/close (:connection rabbitmq)))
  {})

(defn wrap-payload-to-edn [next-fn]
  (fn [message]
    (next-fn (assoc message :bytes-payload (:payload message)
                            :payload (edn/read-string (String. (:payload message)))))))

(defn wrap-into-map [next-fn]
  (fn [channel metadata ^bytes payload]
    (next-fn {:channel channel
              :metadata metadata
              :payload payload})))

(defn message-handler
  [async-chan channel metadata ^bytes payload]
  (let [executor (-> (partial a/>!! async-chan)
                     wrap-payload-to-edn
                     wrap-into-map)]
    (executor channel metadata payload)))

(mount/defstate rabbitmq :start (start config message-handler) :stop  (stop rabbitmq))
