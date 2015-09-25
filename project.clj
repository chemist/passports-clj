(defproject passports "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.apache.commons/commons-compress "1.4"]
                 [bytebuffer "0.2.0"]
                 [clj-http "2.0.0"]
                 [clj-mmap "1.1.2"]
                 [org.clojure/clojurescript "0.0-2843"]

                 [compojure "1.4.0"]
                 [instaparse "1.4.0"]
                 [ring/ring-core "1.4.0" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-servlet "1.4.0" :exclusions [javax.servlet/servlet-api]]
                 [ring/ring-defaults "0.1.5" :exclusions [javax.servlet/servlet-api]]
                 [cc.qbits/jet "0.6.6"]
                 [org.omcljs/om "0.9.0"]
                 [prismatic/om-tools "0.4.0"]
                 ]
  :main ^:skip-aot passports.core
  :target-path "target/%s"
  :plugins [[lein-cljsbuild "1.1.0"]
            ]
  :cljsbuild {:builds
            [{:id "passports"
              :source-paths ["src/cljs"]
              :compiler {:output-to "resources/public/js/passports.js"
                         :output-dir "resources/public/js/out"
                         :source-map true
                         :figwheel true
                         :optimizations :none
                         :asset-path "/static/js/out"
                         :main "passports.core"
                         :pretty-print true}}]}
  :profiles {:uberjar {:aot :all}})
