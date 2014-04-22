(ns examples.tab-view.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:import [goog.ui IdGenerator]))

(enable-console-print!)

(def app-state
  {:app-info {:title "MyApp"}
   :people
   [{:id 0 :first "Ben" :last "Bitdiddle"
     :email "benb@mit.edu"
     :points 0}
    {:id 1 :first "Alyssa" :middle-initial "P" :last "Hacker"
     :email "aphacker@mit.edu"
     :points 0}
    {:id 2 :first "Eva" :middle "Lu" :last "Ator"
     :email "eval@mit.edu"
     :points 0}]})

(defn default-button-view [props owner opts]
  (reify
    om/IRender
    (render [_]
      (let [class-name (cond-> "tab-button"
                         (:selected props) (str " selected"))
            index (:index props)]
        (dom/div #js {:className class-name
                      :onClick (fn [e] ((:on-click opts) index))}
          ((:identifier opts) props))))))

(defmulti button-view identity)

(defmethod button-view :default
  [type] default-button-view)

(defn default-content-view [props owner opts]
  (reify
    om/IRender
    (render [_]
      (let [class-name (cond-> "content-view"
                         (:selected props) (str " selected"))]
       (dom/div #js {:className class-name}
         (dom/div nil (str (:last props) ", " (:first props)))
         (dom/div nil
           (dom/span nil (str "Points: " (:points props)))
           (dom/button
             #js {:onClick
                  (fn [e]
                    (om/transact! props :points inc))}
             "+")))))))

(defmulti content-view identity)

(defmethod content-view :default
  [type] default-content-view)

(defn tab-view [contents owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:selected 0})
    om/IRenderState
    (render-state [_ {:keys [selected]}]
      (dom/div nil
        (apply dom/div #js {:className "tab-view-controls"}
          (om/build-all (button-view (:button-type opts)) contents
            {:fn (fn [content i]
                   (cond-> (assoc content :index i)
                     (== i selected) (assoc :selected true)))
             :opts {:identifier (:identifier opts)
                    :on-click
                    (fn [idx]
                      (when-not (== selected idx)
                        (om/set-state! owner :selected idx)))}}))
        (apply dom/div #js {:className "tab-view-contents"}
          (om/build-all (content-view (:content-type opts)) contents
            {:fn (fn [content i]
                   (cond-> content
                     (== i selected) (assoc :selected true)))}))))))

(defn people-tab-view [parent]
  (om/build tab-view (om/get-app-state parent :people)
    {:opts {:identifier :id}}))

(defn sub-view [app-info owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h1 nil (:title app-info))
        (people-tab-view owner)))))

(defn app-view [app owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (om/build sub-view (:app-info app))))))

(om/root app-view app-state
  {:target (.getElementById js/document "app")})

;; =============================================================================

;; (defn people-view [app owner opts]
;;   (reify
;;     om/IRender
;;     (render [_]
;;       (dom/div nil "People"))))

;; (defn settings-view [app owner opts]
;;   (reify
;;     om/IRender
;;     (render [_]
;;       (dom/div nil "Settings"))))

;; (defn people-view* [id owner opts]
;;   (people-view (om/get-shared owner [:app-state :people]) owner opts))

;; (defn settings-view* [id owner opts]
;;   (settings-view (om/get-app-state owner :settings) owner opts))

;; (defmethod content-view ::people-view [_] people-view*)

;; (defmethod content-view ::settings-view [_] settings-view*)

;; (om/root tab-view app-state
;;   {:target (.getElementById js/document "tab-view1")
;;    :opts {:identifier :name
;;           :button-type ::button-type
;;           :contents [::people-view ::settings-view]}})

