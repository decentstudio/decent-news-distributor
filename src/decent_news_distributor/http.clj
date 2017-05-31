(ns decent-news-distributor.http
  (:require [ring.adapter.jetty :as jetty]
            [decent-news-distributor.cache :as cache]
            [mount.core :as mount]
            [taoensso.timbre :as log]
            [bidi.ring :refer (make-handler)]
            [ring.util.response :as res]
            [clojure.data.json :as json]))

(defn news-handler
  [request]
  (let [news (map (fn [[timestamp item]] 
                    {:timestamp timestamp
                     :news item}) 
                  @(-> cache/message-cache :cache-ttl))]
    (-> (res/response (json/write-str news :key-fn name))
        (res/content-type "application/json"))))

(def handler
  (make-handler ["/api/" {"news" news-handler}]))

(defn start
  [handler]
  (log/info "Starting http subsystem...")
  (let [jetty-server (jetty/run-jetty handler {:port 80})]
    {:jetty-server jetty-server}))

(defn stop
  [web-api])

(mount/defstate web-api :start (start handler) :stop (stop web-api))
