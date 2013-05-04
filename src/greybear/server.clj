(ns greybear.server
  (:import org.mindrot.jbcrypt.BCrypt)
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:use [ring.middleware.session :only [wrap-session]]
        [hiccup core element page]
        [hiccup.middleware :only [wrap-base-url]]
        [compojure.core :only [defroutes GET POST]]
        [cemerick.friend.credentials :only [hash-bcrypt bcrypt-verify]]
        [greybear.model :only [read-game]]))

(defn stones-to-js
  "Transforms a string of chars into a JSON array
  e.g. \"00120\" becomes: [\"0\", \"0\", \"1\", \"2\", \"0\"]
  "
  [stones]
  (format "[%s]" (apply str (interpose ", " (map str stones)))))

(defn games-page [session game-id]
  (let [game (read-game game-id)
        count (:count session 0)
        session (assoc session :count (inc count))]
    {:session session
     :body
     (html5
      [:head
       [:title "Grey Bear"]
       (include-js "/js/greybear.js")]
      [:body
       [:div#players "Players: " (game :white) " vs. " (game :black)]
       [:div#caca "Username: " session]
       [:canvas#goBoard]
       (javascript-tag (format "goboard.draw(\"goBoard\", %s, 1, function(x, y) {console.log(x, y)}, 18, 17);"
                               (stones-to-js (game :stones))))])}))

(def login-page
  (html5
   [:body
    [:div#login
     (form-to [:post "/login"]
              [:div#username (text-field "username")]
              [:div#password (password-field "password")]
              (submit-button "login"))]]))

(def users
  {"foo" (hash-bcrypt "bar")})

(defn login
  [request]
  (let [creds (get request :params)]
    (if creds
      (let [{:keys [username password]} creds]
        (if (bcrypt-verify password (users username))
          ;; TODO redirect to where the user came from
          (-> (redirect-after-post "/")
              (assoc :session {:user username}))
          {:body (html5 "Failed authentication.")}))
      {:body (html5 "no-params")})))

(defroutes main-routes
  (GET "/games/:id" [id :as {session :session}]
       (games-page session (Integer. id)))
  (GET "/login" [] login-page)
  (POST "/login" request (login request))
  (GET "/" request (html5 request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-session)
      (wrap-base-url)))