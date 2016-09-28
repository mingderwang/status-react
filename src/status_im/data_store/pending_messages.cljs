(ns status-im.data-store.pending-messages
  (:require [status-im.data-store.realm.pending-messages :as data-store]
            [clojure.string :as str]))

(defn- get-id
  [message-id to]
  (let [to' (if (and to (str/starts-with? to "0x"))
              (subs to 2)
              to)
        to'' (when to' (subs to' 0 7))
        id' (if to''
              (str message-id "-" (subs to'' 0 7))
              message-id)]
    id'))

(defn get-all
  []
  (data-store/get-all-as-list))

(defn get-by-chat-id
  [chat-id]
  (data-store/get-by-chat-id chat-id))

(defn get-by-message-id
  [message-id]
  (data-store/get-by-message-id message-id))

(defn save
  [{:keys [id to group-id message] :as pending-message}]
  (let [{:keys [from topics payload]} message
        id' (get-id id to)
        chat-id (or group-id to)
        message' (-> pending-message
                     (assoc :id id'
                            :from from
                            :message-id id
                            :chat-id chat-id
                            :payload payload
                            :topics (prn-str topics))
                     (dissoc :message))]
    (data-store/save message')))

(defn delete
  [pending-message]
  (data-store/delete pending-message))

(defn delete-all-by-chat-id
  [chat-id]
  (data-store/delete-all-by-chat-id chat-id))