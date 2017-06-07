(ns decent-news-distributor.http
  (:require [ring.adapter.jetty :as jetty]
            [decent-news-distributor.cache :refer [cache]]
            [mount.core :as mount]
            [taoensso.timbre :as log]
            [bidi.ring :refer (make-handler)]
            [ring.util.response :as res]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.data.json :as json]
            [environ.core :refer [env]]
            [ring.middleware.params :as params]))

(defn to-json
  [x]
  (json/write-str x :key-fn name))

(defn content-type-json
  [response]
  (res/content-type response "application/json"))

(defn not-found-handler
  [request]
  (let [status 404
        error "Resource Not Found"
        response {:status status :error error}]
    (-> (res/response (to-json response))
        (res/status status)
        (content-type-json))))

(defn news-handler
  [request]
  (log/debug "Retrieving news.")
  (let [news (map (fn [[timestamp item]] item)
                  @(-> cache :cache-ttl))]
    (-> (res/response (to-json news))
        (content-type-json))))

(def handler
  (make-handler ["/" [["api/news" news-handler]
                      [true not-found-handler]]]))

(defn wrap-check-access-token [next-fn]
  (let [access-token "ea2d7d06-7190-41d5-b16e-ad2dce0f86fc"
        status 400
        error "Not Authorized"
        response {:status status :error error}]
    (fn [request]
      (if-not (= access-token (get (:query-params request) "access_token"))
        (-> (res/response (to-json response))
            (res/status status)
            (content-type-json))
        (next-fn request)))))

(def app (-> handler
             (wrap-check-access-token)
             (params/wrap-params)
             (wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn start
  [handler]
  (log/info "Starting http subsystem...")
  (let [jetty-server (jetty/run-jetty app {:port 80})]
    {:jetty-server jetty-server}))

(defn stop
  [web-api])

(mount/defstate web-api :start (start handler) :stop (stop web-api))
