(ns passports.core
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [schema.core :as s :include-macros true]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go]])
  )

(def bulk-url "/bulk")

(defn input-to-result [owner raw]
  (go (let [response (<! (http/post bulk-url {:form-params {:bulk-body raw}}))]
        (om/set-state! owner :state (:body response)))))
                     
(def Passports {:raw s/Str :state [{:passports s/Str :status s/Bool}]})

(defcomponent passports [data :- Passports owner]
  (will-mount [_]
              (om/set-state! owner :state (:state data)))
    (render-state [_ {:keys [raw state]}]
      (dom/div  
        (dom/p "Введите паспорта")
        (dom/div
          (dom/button {:name "check"
                       :on-click #(input-to-result owner raw)
                       } "Проверить")
          (dom/button {:name "clean"
                       :on-click #(do
                                    (om/set-state! owner :raw "")
                                    (om/set-state! owner :state [])
                                    (let [input-passports (om/get-node owner "input-passports")]
                                      (set! (.-value input-passports) "")))
                       } "Очистить"))
                       
        (dom/ul
          (let [only-bad (filter :check-result state)]
           (map (fn [checked] (dom/li (str (:passport checked)))) only-bad)))
        (dom/textarea {:name "bulk-body" 
                       :cols "40" 
                       :rows "3"
                       :ref "input-passports"
                       :on-change #(do
                                     (let [addition (-> % .-target .-value)]
                                       (om/set-state! owner :raw addition)))}))))

(om/root passports {:raw "" :state []}
  {:target (. js/document (getElementById "passports"))})

