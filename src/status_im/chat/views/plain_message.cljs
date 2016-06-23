(ns status-im.chat.views.plain-message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                icon
                                                touchable-highlight]]
            [status-im.components.animation :as anim]
            [status-im.chat.styles.plain-message :as st]
            [status-im.constants :refer [response-input-hiding-duration]]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send []
  (dispatch [:send-chat-msg]))

(defn message-valid? [staged-commands message]
  (or (and (pos? (count message))
           (not= "!" message))
      (pos? (count staged-commands))))

(defn button-animation-logic [{:keys [command? val]}]
  (fn [_]
    (let [to-scale (if @command? 0 1)]
      (anim/start (anim/spring val {:toValue  to-scale})))))

(defn list-container [min]
  (fn [{:keys [command? width]}]
    (let [n-width (if @command? min 56)
          delay (if @command? 100 0)]
      (anim/start (anim/timing width {:toValue  n-width
                                      :duration response-input-hiding-duration
                                      :delay delay})))))

(defn commands-button []
  (let [command?        (subscribe [:command?])
        buttons-scale   (anim/create-value (if @command? 1 0))
        container-width (anim/create-value (if @command? 20 56))
        context         {:command? command?
                         :val      buttons-scale
                         :width    container-width}
        on-update       (fn [_]
                          ((button-animation-logic context))
                          ((list-container 20) context))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn []
         [touchable-highlight {:on-press #(dispatch [:switch-command-suggestions])
                               :disabled @command?}
          [animated-view {:style (st/message-input-button-touchable
                                   container-width)}
           [animated-view {:style (st/message-input-button buttons-scale)}
            [icon :list st/list-icon]]]])})))

(defn smile-animation-logic [{:keys [command? val width]}]
  (fn [_]
    (let [to-scale (if @command? 0 1)]
      (when-not @command? (anim/set-value width 56))
      (anim/start (anim/spring val {:toValue  to-scale})
                  (fn [e]
                    (when (and @command? (.-finished e))
                      (anim/set-value width 0.1)))))))

(defn smile-button []
  (let [command?      (subscribe [:command?])
        buttons-scale (anim/create-value (if @command? 1 0))
        container-width (anim/create-value (if @command? 0.1 56))
        context       {:command? command?
                       :val      buttons-scale
                       :width    container-width}
        on-update (smile-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn []
         [touchable-highlight {:on-press (fn []
                                           ;; TODO emoticons: not implemented
                                           )
                               :disabled @command?}
          [animated-view {:style (st/message-input-button-touchable
                                   container-width)}
           [animated-view {:style (st/message-input-button buttons-scale)}
            [icon :smile st/smile-icon]]]])})))
