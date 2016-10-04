(ns status-im.chat.styles.command-pill
  (:require  [status-im.utils.platform :as p]
             [status-im.components.styles :refer [color-white]]))

(defn pill [command]
  {:backgroundColor   (:color command)
   :height            24
   :min-width         120
   :borderRadius      50
   :padding-top       (if p/ios? 4 3)
   :paddingHorizontal 12
   :text-align        :left})

(def pill-text
  {:fontSize    12
   :color       color-white})
