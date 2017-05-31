(ns decent-news-distributor.core
  (:require [decent-news-distributor.http :as http]
            [mount.core :as mount])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (mount/start)
  (println "Hello, World!"))
