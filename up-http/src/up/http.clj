(ns up.http
  (:require 
   [io.pedestal.service.http.route.definition :refer (defroutes)]
   [io.pedestal.service.http :as bootstrap]))

(defn home-page [req]
{:status 200 :body "<h1>Up</h1>"}
)

(defroutes routes
  [[["/" {:get home-page}]]])

(defn start [options bus]
  {:pre [(map? options) 
         (number? (:port options))]}
  (let [server
        (bootstrap/create-server {:end :prod
                                  ::bootstrap/routes routes
                                  ::bootstrap/type :jetty
                                  ::bootstrap/port (:port options)})]
    [server (future (bootstrap/start server))]
    ))

