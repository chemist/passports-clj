(ns passports.core
  (:require [om.core :as om]
            [om-tools.dom :as dom]))

(enable-console-print!)

(defonce passport-state
  (atom
     {:number nil
      :is-bad true}))

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))

(defn print-value [data owner]
  (let [passport (-> (om/get-node owner "for-check")
                     .-value)]
    (print data)
    (print passport)))

(defn passport-input [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:text ""})
    om/IRenderState
    (render-state [_ state]
      (dom/form 
        (dom/input #js {:type "text" :ref "for-check" :onChange (fn [e] (print-value data owner)) } "Введите номер для проверки" 
        )))))

(om/root passport-input passport-state
  {:target (. js/document (getElementById "passports"))})

