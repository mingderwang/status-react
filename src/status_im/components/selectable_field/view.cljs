(ns status-im.components.selectable-field.view
  (:require [status-im.components.react :refer [view
                                                text-input
                                                text]]
            [reagent.core :as r]
            [status-im.components.selectable-field.styles :as st]
            [status-im.i18n :refer [label]]
            [taoensso.timbre :as log]))

(defn- on-press
  [event component]
  (log/debug "Pressed " event component)
  (r/set-state component {:focused? true}))

(defn- on-selection-change
  [event component]
  (let [selection (.-selection (.-nativeEvent event))
        start (.-start selection)
        end (.-end selection)]
    (log/debug "Selection changed: " start end)))

(defn- on-layout-text
  [event component]
  (let [height (.-height (.-layout (.-nativeEvent event)))
        {:keys [full-height]} (r/state component)]
    (when (and (pos? height) (not full-height))
      (r/set-state component {:full-height height
                              :measured?    true}))))

(defn- reagent-render
  [{:keys [label value props] :as data}]
  (let [component (r/current-component)
        {:keys [focused? measured? full-height]} (r/state component)]
    (log/debug "reagent-render: " data focused? measured? full-height)
    [view st/selectable-field-container
     [view st/label-container
      [text {:style st/label
             :font :medium} (or label "")]]
     [view st/text-container
      (if focused?
        [text-input {:style               (st/sized-text full-height)
                     :multiline           true
                     :selectTextOnFocus   true
                     :editable            true
                     :auto-focus          true
                     :on-selection-change #(on-selection-change % component)
                     :on-focus            #(log/debug "Focused" %)
                     :on-blur             #(r/set-state component {:focused? false})
                     :value               value}]
        [text (merge {:style st/text
                      :on-press #(on-press % component)
                      :onLayout #(on-layout-text % component)
                      :font :default
                      :ellipsizeMode :middle
                      :number-of-lines (if measured? 1 0)} (or props {})) (or value "")])]]))

(defn selectable-field [{:keys [label value props]}]
  (let [component-data {:display-name "selectable-field"
                        :reagent-render reagent-render}]
    (reagent.core/create-class component-data)))

