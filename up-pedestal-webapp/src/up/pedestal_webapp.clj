(ns up.pedestal-webapp
  (:require 
   [lamina.core :refer (receive-all)]
   [io.pedestal.service.impl.interceptor :refer (resume)]))

(defn start [{:keys [handler] :as options} bus]
  (require (symbol (namespace handler)))  
  (let [h (ns-resolve (symbol (namespace handler)) (symbol (name handler)))]
    (receive-all bus (fn [{:keys [context]}] (resume (assoc context :response (h (:request context))))))))

