(ns up.pedestal-webapp
  (:require
   [lamina.core :refer (receive-all filter*)]
   [io.pedestal.service.impl.interceptor :refer (resume)])
  (:import (up.start Plugin)))

(defrecord PedestalWebapp [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus)
          handler (-> pctx :options :handler)]
      (require (symbol (namespace handler)))
      (let [h (ns-resolve (symbol (namespace handler)) (symbol (name handler)))]
        (receive-all (->> bus
                          (filter* (comp (partial = :up.http/request) :up/topic)))
                     (fn [{:keys [context]}]
                       (resume (assoc context :response (h (:request context))))))))))
