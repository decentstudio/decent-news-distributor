(ns decent-news-distributor.cache
  (:require [clojure.core.cache :as cache]
            [mount.core :as mount]
            [clojure.core.async :as a]
            [decent-news-distributor.rabbitmq :refer [rabbitmq]]
            [clj-time.core :as t]
            [taoensso.timbre :as log]))

(defn start
  []
  (log/info "Starting cache subsystem...")
  (let [async-chan (a/chan)
        async-sub (a/sub (:async-pub rabbitmq) "incoming.message.news" async-chan )
        cache-ttl (atom (cache/ttl-cache-factory {} :ttl 60000))]
    (a/go-loop [message (a/<! async-chan)]
      (when message
        (swap! cache-ttl assoc (str (t/now)) (:payload message))
        (recur (a/<! async-chan))))
    {:async-chan async-chan
     :async-sub async-sub
     :cache-ttl cache-ttl}))

(defn stop
  [message-cache])

(mount/defstate message-cache :start (start) :stop (stop message-cache))

