(defproject decent-news-distributor "0.1.0-SNAPSHOT"
  :description "Distributes news to clients."
  :url "http://decentstudio.com"
  :license {:name "N/A"
            :url ""}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [mount "0.1.11"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.novemberain/langohr "4.0.0"]
                 [metrics-clojure "2.9.0"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/core.cache "0.6.5"]
                 [clj-time "0.13.0"]
                 [ring/ring-core "1.6.1"]
                 [ring/ring-jetty-adapter "1.6.1"]
                 [bidi "2.1.1"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.1.0"]
                 [ring-cors "0.1.10"]]
                 
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot decent-news-distributor.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]}})
