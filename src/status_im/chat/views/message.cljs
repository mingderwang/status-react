(ns status-im.chat.views.message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.i18n :refer [message-status-label]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                animated-view
                                                touchable-highlight]]
            [status-im.components.animation :as anim]
            [status-im.chat.views.request-message :refer [message-content-command-request]]
            [status-im.chat.styles.message :as st]
            [status-im.models.chats :refer [chat-by-id]]
            [status-im.models.commands :refer [parse-command-message-content
                                               parse-command-request]]
            [status-im.resources :as res]
            [status-im.utils.datetime :as time]
            [status-im.constants :refer [text-content-type
                                         content-type-status
                                         content-type-command
                                         content-type-command-request]]
            [status-im.utils.logging :as log]
            [status-im.protocol.api :as api]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.chat.utils :as cu]))

(defn message-date [timestamp]
  [view {}
   [view st/message-date-container
    [text {:style st/message-date-text
           :font  :default}
     (time/to-short-str timestamp)]]])

(defn contact-photo [{:keys [photo-path]}]
  [view st/contact-photo-container
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  st/contact-photo}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view st/online-container
     [view st/online-dot-left]
     [view st/online-dot-right]]))

(defn message-content-status [{:keys [from content]}]
  [view st/status-container
   [view st/status-image-view
    [contact-photo {}]
    [contact-online {:online true}]]
   [text {:style st/status-from
          :font  :default}
    from]
   [text {:style st/status-text
          :font  :default}
    content]])

(defn message-content-audio [_]
  [view st/audio-container
   [view st/play-view
    [image {:source res/play
            :style  st/play-image}]]
   [view st/track-container
    [view st/track]
    [view st/track-mark]
    [text {:style st/track-duration-text
           :font  :default}
     "03:39"]]])

(defview message-content-command [content preview]
  [commands [:get-commands-and-responses]]
  (let [{:keys [command content]} (parse-command-message-content commands content)
        {:keys [name icon type]} command]
    [view st/content-command-view
     [view st/command-container
      [view (st/command-view command)
       [text {:style st/command-name
              :font  :default}
        (str (if (= :command type) "!" "") name)]]]
     (when icon
       [view st/command-image-view
        [image {:source {:uri icon}
                :style  st/command-image}]])
     (if preview
       preview
       [text {:style st/command-text
              :font  :default}
        (str content)])]))

(defn set-chat-command [message-id command]
  (dispatch [:set-response-chat-command message-id (keyword (:name command))]))

(defn message-view
  [message content]
  [view (st/message-view message)
   #_(when incoming-group
       [text {:style message-author-text}
        "Justas"])
   content])

(defmulti message-content (fn [_ message _]
                            (message :content-type)))

(defmethod message-content content-type-command-request
  [wrapper message]
  [wrapper message [message-content-command-request message]])

(defn text-message
  [{:keys [content] :as message}]
  [message-view message
   [text {:style (st/text-message message)
          :font  :default}
    (str content)]])

(defmethod message-content text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-status
  [_ message]
  [message-content-status message])

(defmethod message-content content-type-command
  [wrapper {:keys [content rendered-preview] :as message}]
  [wrapper message
   [message-view message [message-content-command content rendered-preview]]])

(defmethod message-content :default
  [wrapper {:keys [content-type content] :as message}]
  [wrapper message
   [message-view message
    [message-content-audio {:content      content
                            :content-type content-type}]]])

(defview group-message-delivery-status [{:keys [message-id group-id message-status user-statuses] :as msg}]
  [app-db-message-user-statuses [:get-in [:message-user-statuses message-id]]
   app-db-message-status-value [:get-in [:message-statuses message-id :status]]
   chat [:get-chat-by-id group-id]
   contacts [:get-contacts]]
  (let [status            (or message-status app-db-message-status-value :sending)
        user-statuses     (merge user-statuses app-db-message-user-statuses)
        participants      (:contacts chat)
        seen-by-everyone? (and (= (count user-statuses) (count participants))
                               (every? (fn [[_ {:keys [status]}]]
                                         (= (keyword status) :seen)) user-statuses))]
    (if (or (zero? (count user-statuses))
            seen-by-everyone?)
      [view st/delivery-view
       [image {:source (case status
                         :seen {:uri :icon_ok_small}
                         :failed res/delivery-failed-icon
                         nil)
               :style  st/delivery-image}]
       [text {:style st/delivery-text
              :font  :default}
        (message-status-label
          (if seen-by-everyone?
            :seen-by-everyone
            status))]]
      [touchable-highlight
       {:on-press (fn []
                    (dispatch [:show-message-details {:message-status status
                                                      :user-statuses  user-statuses
                                                      :participants   participants}]))}
       [view st/delivery-view
        (for [[_ {:keys [whisper-identity]}] (take 3 user-statuses)]
          ^{:key whisper-identity}
          [image {:source {:uri (or (get-in contacts [whisper-identity :photo-path])
                                    (identicon whisper-identity))}
                  :style  {:width        16
                           :height       16
                           :borderRadius 8}}])
        (if (> (count user-statuses) 3)
          [text {:style st/delivery-text
                 :font  :default}
           (str "+ " (- (count user-statuses) 3))])]])))

(defview message-delivery-status [{:keys [message-id chat-id message-status user-statuses]}]
  [app-db-message-status-value [:get-in [:message-statuses message-id :status]]]
  (let [delivery-status (get-in user-statuses [chat-id :status])
        status          (if (cu/console? chat-id)
                          :seen
                          (or delivery-status message-status app-db-message-status-value :sending))]
    [view st/delivery-view
     [image {:source (case status
                       :seen {:uri :icon_ok_small}
                       :failed res/delivery-failed-icon
                       nil)
             :style  st/delivery-image}]
     [text {:style st/delivery-text
            :font  :default}
      (message-status-label status)]]))

(defview member-photo [from]
  [photo-path [:photo-path from]]
  [view st/photo-view
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  st/photo}]])

(defn incoming-group-message-body
  [{:keys [selected same-author from] :as message} content]
  (let [delivery-status :seen-by-everyone]
    [view st/group-message-wrapper
     (when selected
       [text {:style st/selected-message
              :font  :default}
        "Mar 7th, 15:22"])
     [view (st/incoming-group-message-body-st message)
      [view st/message-author
       (when (not same-author) [member-photo from])]
      [view st/group-message-view
       content
       ;; TODO show for last or selected
       (when (and selected delivery-status)
         [message-delivery-status message])]]]))

(defn message-body
  [{:keys [outgoing message-type] :as message} content]
  [view (st/message-body message)
   content
   (when outgoing
     (if (= (keyword message-type) :group-user-message)
       [group-message-delivery-status message]
       [message-delivery-status message]))])

(defn message-container-animation-logic [{:keys [to-value val callback]}]
  (fn [_]
    (let [to-value @to-value]
      (when (< 0 to-value)
        (anim/start
          (anim/spring val {:toValue  to-value
                            :friction 4
                            :tension  10})
          (fn [arg]
            (when (.-finished arg)
              (callback))))))))

(defn message-container [message & children]
  (if (:new? message)
    (let [layout-height (r/atom 0)
          anim-value (anim/create-value 1)
          anim-callback #(dispatch [:set-message-shown message])
          context {:to-value layout-height
                   :val      anim-value
                   :callback anim-callback}
          on-update (message-container-animation-logic context)]
      (r/create-class
        {:component-did-update
         on-update
         :reagent-render
         (fn [message & children]
           @layout-height
           [animated-view {:style (st/message-container anim-value)}
            (into [view {:onLayout (fn [event]
                                     (let [height (.. event -nativeEvent -layout -height)]
                                       (reset! layout-height height)))}]
                  children)])}))
    (into [view] children)))

(defn chat-message [{:keys [outgoing message-id chat-id user-statuses from]}]
  (let [my-identity (api/my-identity)
        status      (subscribe [:get-in [:message-user-statuses message-id my-identity]])]
    (r/create-class
      {:component-did-mount
       (fn []
         (when (and (not outgoing)
                    (not= :seen (keyword @status))
                    (not= :seen (keyword (get-in user-statuses [my-identity :status]))))
           (dispatch [:send-seen! {:chat-id    chat-id
                                   :from       from
                                   :message-id message-id}])))
       :reagent-render
       (fn [{:keys [outgoing timestamp new-day group-chat] :as message}]
         [message-container message
          ;; TODO there is no new-day info in message
          (when new-day
            [message-date timestamp])
          [view
           (let [incoming-group (and group-chat (not outgoing))]
             [message-content
              (if incoming-group
                incoming-group-message-body
                message-body)
              (merge message {:incoming-group incoming-group})])]])})))
