(defproject decent-news-distributor "0.1.0-SNAPSHOT"
  :description "Distributes news to clients."
  :url "http://decentstudio.com"
  :license {:name "N/A"
            :url ""}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aleph "0.4.3"]
                 [mount "0.1.11"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.novemberain/langohr "4.0.0"]
                 [metrics-clojure "2.9.0"]
                 [org.clojure/core.async "0.3.443"]]
  :main ^:skip-aot decent-news-distributor.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]}})
