(ns status-im.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.handlers]
            [status-im.subs]
            [status-im.components.react :refer [app-registry
                                                keyboard
                                                orientation]]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.contacts.views.contact-list :refer [contact-list]]
            [status-im.contacts.views.new-contact :refer [new-contact]]
            [status-im.qr-scanner.screen :refer [qr-scanner]]
            [status-im.discovery.tag :refer [discovery-tag]]
            [status-im.discovery.search-results :refer [discovery-search-results]]
            [status-im.chat.screen :refer [chat]]
            [status-im.accounts.login.screen :refer [login]]
            [status-im.accounts.screen :refer [accounts]]
            [status-im.transactions.screen :refer [confirm]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.new-group.screen :refer [new-group]]
            [status-im.participants.views.add :refer [new-participants]]
            [status-im.participants.views.remove :refer [remove-participants]]
            [status-im.group-settings.screen :refer [group-settings]]
            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.profile.photo-capture.screen :refer [profile-photo-capture]]
            status-im.data-store.core
            [taoensso.timbre :as log]))

(defn orientation->keyword [o]
  (keyword (.toLowerCase o)))

(defn validate-current-view
  [current-view signed-up?]
  (if (or (contains? #{:login :chat :recover :accounts} current-view)
          signed-up?)
    current-view
    :chat))

(defn app-root []
  (let [signed-up?      (subscribe [:signed-up?])
        _               (log/debug "signed up: " @signed-up?)
        view-id         (subscribe [:get :view-id])
        account-id      (subscribe [:get :current-account-id])
        keyboard-height (subscribe [:get :keyboard-height])]
    (log/debug "Current account: " @account-id)
    (r/create-class
      {:component-will-mount
       (fn []
         (let [o (orientation->keyword (.getInitialOrientation orientation))]
           (dispatch [:set :orientation o]))
         (.addOrientationListener
           orientation
           #(dispatch [:set :orientation (orientation->keyword %)]))
         (.lockToPortrait orientation)
         (.addListener keyboard
                       "keyboardWillShow"
                       (fn [e]
                         (let [h (.. e -endCoordinates -height)]
                           (when-not (= h @keyboard-height)
                             (dispatch [:set :keyboard-height h])))))
         (.addListener keyboard
                       "keyboardWillHide"
                       (when-not (= 0 @keyboard-height)
                         #(dispatch [:set :keyboard-height 0]))))
       :render
       (fn []
         (when @view-id
           (let [current-view (validate-current-view @view-id @signed-up?)]
             (let [component (case current-view
                               :discovery main-tabs
                               :discovery-tag discovery-tag
                               :discovery-search-results discovery-search-results
                               :add-participants new-participants
                               :remove-participants remove-participants
                               :chat-list main-tabs
                               :new-group new-group
                               :group-settings group-settings
                               :contact-list main-tabs
                               :group-contacts contact-list
                               :new-contact new-contact
                               :qr-scanner qr-scanner
                               :chat chat
                               :profile profile
                               :profile-photo-capture profile-photo-capture
                               :accounts accounts
                               :login login
                               :confirm confirm
                               :my-profile my-profile)]
               [component]))))})))

(defn init []
  (dispatch-sync [:reset-app])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-geth])
  (dispatch [:load-user-phone-number])
  (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root)))
