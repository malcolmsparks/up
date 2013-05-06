;; Copyright © 2013, Malcolm Sparks. All Rights Reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) 
;; which can be found in the file epl-v10.html at the root of this distribution.  
;;
;; By using this software in any fashion, you are agreeing to be bound by the
;; terms of this license.
;; 
;; You must not remove this notice, or any other, from this software.

(ns up.http
  (:require 
   [lamina.core :refer (enqueue)]
   [io.pedestal.service.interceptor :refer (defhandler)]
   [io.pedestal.service.impl.interceptor :refer (with-pause interceptor pause resume)]
   [io.pedestal.service.http.route.definition :refer (expand-routes)]
   [io.pedestal.service.http :as bootstrap])
  (:import (up.start Plugin)))

(defn create-routes [bus]
  (expand-routes
   [[["/" {:get (interceptor :name "root"
                             :enter (fn [ctx] 
                                      (enqueue bus {:type :http-request :context (pause ctx)})))}]]]))

(defrecord HttpService [pctx]
  Plugin
  (start [_]
    {:pre [(map? (-> pctx :options)) 
           (number? (-> pctx :options :port))]}
    (let [server
          (bootstrap/create-server {:end :prod
                                    ::bootstrap/routes (create-routes (:bus pctx))
                                    ::bootstrap/type :jetty
                                    ::bootstrap/port (-> pctx :options :port)})]
      [server (future (bootstrap/start server))])))

