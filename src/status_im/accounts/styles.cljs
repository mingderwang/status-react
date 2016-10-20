(ns status-im.accounts.styles
  (:require [status-im.components.styles :refer [color-white]]
            [status-im.components.react :as r]))


(def screen-container
  {:flex  1
   :color :white})

(def gradient-background
  {:position       :absolute
   :top            0
   :bottom         0
   :left           0
   :right          0
   :padding-bottom 84})

(defn account-list-content [cnt]
  (merge {:justifyContent :center}
         ;; todo this will not work with landscape and looks bad
         (when (< (* 69 (+ 2 cnt)) (:height (r/get-dimensions "window")))
           {:flex 1})))

(def account-list
  {:margin-bottom 20})

(def row-separator
  {:borderBottomWidth 1
   :borderBottomColor "#bababa"})

(def account-container
  {:flex            1
   :flexDirection   :row
   :height          69
   :backgroundColor "rgba(255, 255, 255, 0.1)"
   :alignItems      :center
   :justifyContent  :center})

(def photo-container
  {:flex           0.2
   :flexDirection  :column
   :alignItems     :center
   :justifyContent :center})

(def account-photo-container
  {:flex           1
   :width          36
   :height         36
   :alignItems     :center
   :justifyContent :center})

(def photo-image
  {:width         36
   :height        36
   :border-radius 18})

(def name-container
  {:flex          1
   :flexDirection :column})

(def name-text
  {:color    color-white
   :fontSize 16})

(def address-text
  {:color    color-white
   :fontSize 12})

(def online-container
  {:flex           0.2
   :flexDirection  :column
   :alignItems     :center
   :justifyContent :center})

(def bottom-actions-container
  {:position :absolute
   :left     0
   :right    0
   :bottom   0})

(def add-account-button-container
  {:flex              1
   :paddingVertical   16
   :paddingHorizontal 28
   :justifyContent    :center
   :alignItems        :center})

(def add-account-button
  {:flexDirection :row})

(def icon-plus
  {:flexDirection :column
   :paddingTop    2
   :width         20
   :height        20})

(def add-account-text
  {:flexDirection :column
   :color         :white
   :fontSize      16
   :marginLeft    8})

(def recover-button-container
  {:flex 1})

(def recover-button
  {:flex              1
   :alignItems        :center
   :paddingVertical   16
   :paddingHorizontal 28})

(def recover-button-text
  {:flex     1
   :color    color-white
   :fontSize 16})

;wallet-qr-code.cljs

(def wallet-qr-code
  {:flex 1
   :flex-direction :column})

(def account-toolbar
  {:background-color "#2f3031"
   :padding-bottom 45})

(def wallet-account-container
  {:flex            1
   :flexDirection   :row
   :height          69
   :alignItems      :center
   :justifyContent  :center})

(def qr-code
  {:align-items :center
   :padding-vertical 40})

(def footer
  {:position :absolute
   :left     0
   :right    0
   :bottom   0
   :height 166
   :background-color "#2f3031"})

(def wallet-info
  {:align-items :center
   :padding-top 42
   :padding-bottom 20})

(def wallet-name-text
  {:color color-white
   :padding-bottom 5})

(def wallet-address-text
  {:color "#999999"
   })

(def done-button
  {:flex 1
   :flex-direction :column
   :align-items :center
   :height 60
   :padding-top 20
   :background-color "#7597e4"})

(def done-button-text
  {:color color-white})