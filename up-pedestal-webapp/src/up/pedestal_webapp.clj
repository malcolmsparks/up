(ns up.pedestal-webapp
  (:require 
   [lamina.core :refer (receive-all)]
   [io.pedestal.service.impl.interceptor :refer (resume)])
  (:import (up.start Plugin)))

(defrecord PedestalWebapp [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus)
          handler (-> pctx :options :handler)]
      (println "context is " pctx)
      (require (symbol (namespace handler)))  
      (let [h (ns-resolve (symbol (namespace handler)) (symbol (name handler)))]
        (receive-all bus 
                     (fn [{:keys [context]}] (resume (assoc context :response (h (:request context))))))))))

