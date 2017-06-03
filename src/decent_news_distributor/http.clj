(ns decent-news-distributor.http
  (:require [ring.adapter.jetty :as jetty]
            [decent-news-distributor.cache :refer [cache]]
            [mount.core :as mount]
            [taoensso.timbre :as log]
            [bidi.ring :refer (make-handler)]
            [ring.util.response :as res]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))


(defn news-handler
  [request]
  (log/debug "Retrieving news.")
  (let [news (map (fn [[timestamp item]] item)
                  @(-> cache :cache-ttl))]
    (-> (res/response (json/write-str news :key-fn name))
        (res/content-type "application/json"))))

(def handler
  (make-handler ["/api/" {"news" news-handler}]))

(def app (-> handler
             (wrap-cors :access-control-allow-origin [#"\*"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn start
  [handler]
  (log/info "Starting http subsystem...")
  (let [jetty-server (jetty/run-jetty app {:port 80})]
    {:jetty-server jetty-server}))

(defn stop
  [web-api])

(mount/defstate web-api :start (start handler) :stop (stop web-api))
