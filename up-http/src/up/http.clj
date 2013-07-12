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
   [lamina.core :refer (enqueue receive-all filter*)]
   [io.pedestal.service.interceptor :refer (defhandler)]
   [io.pedestal.service.impl.interceptor :refer (with-pause interceptor pause resume)]
   [io.pedestal.service.http.route.definition :refer (expand-routes)]
   [io.pedestal.service.http :as bootstrap])
  (:import (up.start Plugin)))

(def routes (atom []))

(defn add-webapp [{rts :routes }]
  (dosync
   (swap! routes conj rts)))

(defrecord HttpService [pctx]
  Plugin
  (start [_]
    {:pre [(map? (-> pctx :options))
           (number? (-> pctx :options :port))]}
    (let [{:keys [bus]} pctx
          server
          (bootstrap/create-server {:env :dev
                                    ;; This is for dev, in prod just expand routes once
                                    ::bootstrap/routes (fn [] (expand-routes (mapcat #(apply % []) (deref routes))))

;;(expand-routes (mapcat apply @routesfns))
                                    ::bootstrap/type :jetty
                                    ::bootstrap/port (-> pctx :options :port)})]
      (receive-all (->> bus
                        (filter* (comp (partial = :up.http/add-webapp) :up/topic)))
                   add-webapp)

      [server (future (bootstrap/start server))])))
