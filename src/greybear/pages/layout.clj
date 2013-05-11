(ns greybear.pages.layout
  (:use [hiccup page element]
        (sandbar stateful-session)))

(defn base-layout
  [subtitle & content]
  (html5
   [:head
    [:title (str "Grey Bear - " subtitle)]
    (include-css "/bootstrap/css/bootstrap.css")]
   [:body
    [:div#content content]]))
