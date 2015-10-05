(ns passports.core
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.java.io :as io]
            [passports.base :as base]
            [ring.util.request :as ring]
            [ring.util.response :refer [response]]
            )
  (:gen-class))

;; This is a handler that returns the
;; contents of `resources/index.html`
(defn home
  [req]
  (render (io/resource "index.html") req))

(defn bulk [req]
  (let [result (base/check-bulk (:bulk-body (:params req)))]
   (assoc (response result) :headers {"Content-Type" "application/json"})))


;; Defines a handler that acts as router
(defroutes app
  (GET "/" [] home)
  (POST "/bulk" [] bulk)
  (route/resources "/static")
  (route/not-found "<h1>Page not found</h1>"))

;; Application entry point
(defn -main
  [& args]
  (let [app (wrap-json-response (wrap-defaults app (assoc site-defaults :session false :security {:anti-forgery false})))
        server (run-jetty {:ring-handler app :port 3000 :join? false})]
    server
    (println "Input stop if you need stop server")
    (loop []
      (if (= (symbol "stop") (read))
        (.stop server)
        (recur)))))

