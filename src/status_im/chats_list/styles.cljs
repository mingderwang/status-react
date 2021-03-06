(ns status-im.chats-list.styles
  (:require [status-im.components.styles :refer [color-white
                                                 color-light-gray
                                                 color-separator
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 new-messages-count-color]]
            [status-im.components.tabs.styles :refer [tabs-height]]))

(def gradient-top-bottom-shadow
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def chat-separator-wrapper
  {:background-color color-white
   :height           0.5
   :padding-left     74})

(def chat-separator-item
  {:border-bottom-width 0.5
   :border-bottom-color color-separator})

(def chat-container
  {:flex-direction      :row
   :background-color    color-white
   :height              94})

(def chat-icon-container
  {:margin-top  -2
   :margin-left -4
   :padding     16
   :width       48
   :height      48})

(def item-container
  {:flex-direction      :column
   :margin-left         30
   :padding-top         16
   :padding-right       16
   :flex                1})

(def name-view
  {:flex-direction :row})

(def name-text
  {:color      text1-color
   :font-size  14})

(def group-icon
  {:margin-top  5
   :margin-left 8
   :width       14
   :height      9})

(def memebers-text
  {:marginTop  2
   :marginLeft 4
   :fontSize   12
   :color      text2-color})

(def last-message-text
  {:margin-top   5
   :margin-right 40
   :color        text1-color
   :fontSize     14
   :lineHeight   20})

(def last-message-text-no-messages
  (merge last-message-text
         {:color text2-color}))

(def status-container
  {:flex-direction :row
   :top            18
   :right          16})

(def status-image
  {:marginTop 4
   :width     9
   :height    7})

(def datetime-text
  {:fontSize   12
   :color      text2-color
   :marginLeft 5})

(def new-messages-container
  {:position        :absolute
   :top             54
   :right           16
   :width           24
   :height          24
   :backgroundColor new-messages-count-color
   :borderRadius    50})

(def new-messages-text
  {:top       5
   :left      0
   :fontSize  10
   :color     color-blue
   :textAlign :center})

(def hamburger-icon
  {:width  16
   :height 12})

(def toolbar-icon
  {:width  17
   :height 17})

(def chats-container
  {:flex 1})

(def list-container
  {:background-color color-light-gray})

(def create-icon
  {:fontSize 20
   :height   22
   :color    color-white})

(def person-stalker-icon
  {:fontSize 20
   :height   22
   :color    color-white})

(defn action-buttons-container [animation? offset-y]
  ;; todo fix overlaying of parent view
  {:position  :absolute
   :right     0
   :height    230
   :width     220
   :bottom    0
   :transform [{:translateY (if animation? offset-y 1)}]})
