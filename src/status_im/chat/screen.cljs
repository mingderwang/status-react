(ns status-im.chat.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as s]
            [status-im.components.react :refer [view
                                                animated-view
                                                text
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-action
                                                           chat-icon-view-menu-item]]
            [status-im.chat.styles.screen :as st]
            [status-im.utils.listview :refer [to-datasource-inverted]]
            [status-im.utils.utils :refer [truncate-str]]
            [status-im.utils.datetime :as time]
            [status-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.chat.views.message :refer [chat-message]]
            [status-im.chat.views.suggestions :refer [suggestion-container]]
            [status-im.chat.views.response :refer [response-view]]
            [status-im.chat.views.new-message :refer [chat-message-new]]
            [status-im.chat.views.actions :refer [actions-view]]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.components.animation :as anim]
            [reagent.core :as r]
            [clojure.string :as str]
            [cljs-time.core :as t]))

(defn contacts-by-identity [contacts]
  (->> contacts
       (map (fn [{:keys [identity] :as contact}]
              [identity contact]))
       (into {})))

(defn add-message-color [{:keys [from] :as message} contact-by-identity]
  (if (= "system" from)
    (assoc message :text-color :#4A5258
                   :background-color :#D3EEEF)
    (let [{:keys [text-color background-color]} (get contact-by-identity from)]
      (assoc message :text-color text-color
                     :background-color background-color))))

(defview chat-icon []
  [chat-id [:chat :chat-id]
   group-chat [:chat :group-chat]
   name [:chat :name]
   color [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon-view-action chat-id group-chat name color true])

(defn typing [member platform-specific]
  [view st/typing-view
   [view st/typing-background
    [text {:style             st/typing-text
           :platform-specific platform-specific
           :font              :default}
     (str member " " (label :t/is-typing))]]])

(defn typing-all [platform-specific]
  [view st/typing-all
   ;; TODO stub data
   (for [member ["Geoff" "Justas"]]
     ^{:key member} [typing member platform-specific])])

(defn message-row [{:keys [contact-by-identity platform-specific group-chat messages-count]}]
  (fn [row _ idx]
    (let [message (-> row
                      (add-message-color contact-by-identity)
                      (assoc :group-chat group-chat)
                      (assoc :last-message (= (js/parseInt idx) (dec messages-count))))]
      (list-item [chat-message message platform-specific]))))

(defn on-action-selected [position]
  (case position
    0 (dispatch [:navigate-to :add-participants])
    1 (dispatch [:navigate-to :remove-participants])
    2 (dispatch [:leave-group-chat])))

(defn overlay [{:keys [on-click-outside]} items]
  [view st/actions-overlay
   [touchable-highlight {:on-press on-click-outside
                         :style    st/overlay-highlight}
    [view nil]]
   items])

(defn online-text [contact chat-id]
  (if contact
    (let [last-online      (get contact :last-online)
          last-online-date (time/to-date last-online)
          now-date         (t/now)]
      (if (and (> last-online 0)
               (<= last-online-date now-date))
        (time/time-ago last-online-date)
        (label :t/active-unknown)))
    (if (= chat-id "console")
      (label :t/active-online)
      (label :t/active-unknown))))

(defn toolbar-content [platform-specific]
  (let [{:keys [group-chat name contacts chat-id]} (subscribe [:chat-properties [:group-chat :name :contacts :chat-id]])
        show-actions (subscribe [:show-actions])
        contact      (subscribe [:get-in [:contacts @chat-id]])]
    (fn [platform-specific]
      [view (st/chat-name-view @show-actions)
       [text {:style             st/chat-name-text
              :platform-specific platform-specific
              :number-of-lines   1
              :font              :medium}
        (if (str/blank? @name)
          (label :t/user-anonymous)
          (or @name (label :t/chat-name)))]
       (if @group-chat
         [view {:flexDirection :row}
          [icon :group st/group-icon]
          [text {:style             st/members
                 :platform-specific platform-specific
                 :font              :medium}
           (let [cnt (inc (count @contacts))]
             (label-pluralize cnt :t/members))]]
         [text {:style             st/last-activity
                :platform-specific platform-specific
                :font              :default}
          (online-text @contact @chat-id)])])))

(defn toolbar-action []
  (let [show-actions (subscribe [:show-actions])]
    (fn []
      (if @show-actions
        [touchable-highlight
         {:on-press #(dispatch [:set-show-actions false])}
         [view st/action
          [icon :up st/up-icon]]]
        [touchable-highlight
         {:on-press #(dispatch [:set-show-actions true])}
         [view st/action
          [chat-icon]]]))))

(defview chat-toolbar [platform-specific]
  [show-actions [:show-actions]]
  [view
   [status-bar {:platform-specific platform-specific}]
   [toolbar {:hide-nav?      show-actions
             :custom-content [toolbar-content platform-specific]
             :custom-action  [toolbar-action]
             :style          (get-in platform-specific [:styles :components :toolbar])}]])

(defview messages-view [platform-specific group-chat]
  [messages [:chat :messages]
   contacts [:chat :contacts]
   loaded?  [:all-messages-loaded?]]
  (let [contacts' (contacts-by-identity contacts)]
    [list-view {:renderRow                 (message-row {:contact-by-identity contacts'
                                                         :platform-specific   platform-specific
                                                         :group-chat          group-chat
                                                         :messages-count      (count messages)})
                :renderScrollComponent     #(invertible-scroll-view (js->clj %))
                :onEndReached              (when-not loaded? #(dispatch [:load-more-messages]))
                :enableEmptySections       true
                :keyboardShouldPersistTaps true
                :dataSource                (to-datasource-inverted messages)}]))

(defn messages-container-animation-logic
  [{:keys [offset val]}]
  (fn [_]
    (anim/start (anim/spring val {:toValue @offset}))))

(defn messages-container [messages]
  (let [offset (subscribe [:messages-offset])
        messages-offset (anim/create-value 0)
        context {:offset offset
                 :val    messages-offset}
        on-update (messages-container-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [messages]
         @offset
         [animated-view {:style (st/messages-container messages-offset)}
          messages])})))

(defn chat [{platform-specific :platform-specific}]
  (let [group-chat (subscribe [:chat :group-chat])
        show-actions (subscribe [:show-actions])
        command? (subscribe [:command?])
        layout-height (subscribe [:get :layout-height])]
    (r/create-class
      {:component-did-mount #(dispatch [:check-autorun])
       :reagent-render
       (fn [{platform-specific :platform-specific}]
         [view {:style    st/chat-view
                :onLayout (fn [event]
                            (let [height (.. event -nativeEvent -layout -height)]
                              (when (not= height @layout-height)
                                (dispatch [:set-layout-height height]))))}
          [chat-toolbar platform-specific]
          [messages-container
           [messages-view platform-specific @group-chat]]
          ;; todo uncomment this
          #_(when @group-chat [typing-all platform-specific])
          [response-view]
          (when-not @command? [suggestion-container])
          [chat-message-new platform-specific]
          (when @show-actions [actions-view platform-specific])])})))
