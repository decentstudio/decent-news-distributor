(ns decent-news-distributor.cache
  (:require [clojure.core.cache :as cache]
            [mount.core :as mount]
            [clojure.core.async :as a]
            [decent-news-distributor.messaging :refer [messaging]]
            [clj-time.core :as t]
            [taoensso.timbre :as log]))

(def config {:topic :message.received
             :ttl 60000})

(defn start
  [config async-pub]
  (log/info "Starting cache subsystem...")
  (let [{:keys [topic ttl]} config
        async-chan (a/chan)
        async-sub (a/sub async-pub topic async-chan)
        cache-ttl (atom (cache/ttl-cache-factory {} :ttl ttl))]
    (a/go-loop [message (a/<! async-chan)]
      (when message
        (log/debug "Adding message to cache.")
        (swap! cache-ttl assoc (str (t/now)) (:body message))
        (recur (a/<! async-chan))))
    {:async-chan async-chan
     :async-sub async-sub
     :cache-ttl cache-ttl}))

(defn stop
  [cache])

(mount/defstate cache 
  :start (start config (:async-pub messaging)) 
  :stop (stop cache))

