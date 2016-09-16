(ns status-im.chat.views.bottom-info
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                image
                                                text
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-menu-item]]
            [status-im.chat.styles.screen :as st]
            [status-im.i18n :refer [label label-pluralize message-status-label]]
            [status-im.components.animation :as anim]
            [status-im.utils.utils :refer [truncate-str]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.listview :as lw]
            [status-im.utils.logging :as log]
            [clojure.string :as str]))

(defn- container-animation-logic [{:keys [to-value val]}]
  (fn [_]
    (let [to-value @to-value]
      (anim/start
        (anim/spring val {:toValue  to-value
                          :friction 6
                          :tension  40})))))

(defn overlay [{:keys [on-click-outside]} items]
  [view {:style st/bottom-info-overlay}
   [touchable-highlight {:on-press on-click-outside
                         :style    st/overlay-highlight}
    [view nil]]
   items])

(defn container [& _]
  (let [layout-height (r/atom 0)
        anim-value    (anim/create-value 1)
        context       {:to-value layout-height
                       :val      anim-value}
        on-update     (container-animation-logic context)]
    (r/create-class
      {:component-did-update
       on-update
       :reagent-render
       (fn [& children]
         @layout-height
         [animated-view {:style (st/bottom-info-container anim-value)}
          (into [view {:onLayout (fn [event]
                                   (let [height (.. event -nativeEvent -layout -height)]
                                     (reset! layout-height height)))}]
                children)])})))

(defn message-status-row [{:keys [photo-path name]} {:keys [whisper-identity status]}]
  [view st/bottom-info-row
   [image {:source {:uri (or photo-path (identicon whisper-identity))}
           :style  st/bottom-info-row-photo}]
   [view st/bottom-info-row-text-container
    [text {:style           st/bottom-info-row-text1
           :number-of-lines 1}
     (truncate-str (if-not (str/blank? name)
                     name
                     whisper-identity) 30)]
    [text {:style           st/bottom-info-row-text2
           :number-of-lines 1}
     (message-status-label (or status :sending))]]])

(defn render-row [contacts]
  (fn [{:keys [whisper-identity] :as row} _ _]
    (let [contact (get contacts whisper-identity)]
      (list-item [message-status-row contact row]))))

(defn bottom-info-view []
  (let [bottom-info (subscribe [:chat-ui-props :bottom-info])
        contacts    (subscribe [:get-contacts])]
    (r/create-class
      {:reagent-render
       (fn []
         (let [{:keys [user-statuses message-status participants]} @bottom-info
               participants (->> participants
                                 (map (fn [{:keys [identity]}]
                                        [identity {:whisper-identity identity
                                                   :status           message-status}]))
                                 (into {}))]
           [overlay {:on-click-outside #(dispatch [:set-chat-ui-props :show-bottom-info? false])}
            [container
             [list-view {:dataSource            (-> (merge participants user-statuses)
                                                    (vals)
                                                    (lw/to-datasource))
                         :enableEmptySections   true
                         :renderRow             (render-row @contacts)
                         :contentContainerStyle st/bottom-info-list-container}]]]))})))