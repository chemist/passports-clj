(ns passports.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go]])
  )

(enable-console-print!)

(defonce state (atom {:phone ""}))

(def check-url "/check/")

(defn check [owner state]
  (cond
    (and (not (= 10 (count (:passport state))))
         (not (nil? (:status state)))) 
      (om/set-state! owner :status nil)
    (and (= 10 (count (:passport state))) 
         (nil? (:status state))) 
      (go (let [response (<! (http/get (str check-url (:passport state))))]
          (om/set-state! owner :status (:status response)))))
  (cond
    (= 401 (:status state)) "red"
    (= 200 (:status state)) "green"
    :else "black"))

(defn passport-input [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:passport "" :status nil})
    om/IRenderState
    (render-state [_ st]
      (dom/form 
        (dom/p {:style {:color (check owner st)}} (:passport st) )
        (dom/input #js {:type "text" 
                        :ref "for-check" 
                        :value (:passport st)
                        :onKeyDown #(when (= (.-key %) "Escape")
                                      (om/set-state! owner :passport nil)
                                      (om/set-state! owner :status nil))
                        :onChange #(do
                                     (let [new-passport (-> % .-target .-value)]
                                       (if (js/isNaN new-passport)
                                         (om/set-state! owner :passport (:passport st))
                                         (om/set-state! owner :passport new-passport)))
                                     )} " Введите номер для проверки" 
        )))))

(om/root passport-input state
  {:target (. js/document (getElementById "passports"))})

