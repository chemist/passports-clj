(ns passports.core
  (:require [qbits.jet.server :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.response :refer [render]]
            [clojure.java.io :as io]
            [passports.base :as base])
  (:gen-class))

;; This is a handler that returns the
;; contents of `resources/index.html`
(defn home
  [req]
  (render (io/resource "index.html") req))

;; Defines a handler that acts as router
(defroutes app
  (GET "/" [] home)
  (GET "/check/:passport" [passport]
       (let [check-result (base/check-passport passport)]
         (cond 
           (nil? check-result) {:status 400 :headers {"Content-Type" "text/html; charset=utf-8"} :body "can't parse passport number"}
           check-result {:status 401 :headers {"Content-Type" "text/html; charset=utf-8"} :body "bad passport"}
           :else        {:status 200 :headers {"Content-Type" "text/html; charset=utf-8"} :body "good passport"})))
  (route/resources "/static")
  (route/not-found "<h1>Page not found</h1>"))

(defn -main []
  )
;; Application entry point
(defn main
  [& args]
  (println (base/check-passport "000000"))
  (let [app (wrap-defaults app site-defaults)]
    (run-jetty {:ring-handler app :port 3000})))
