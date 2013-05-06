(ns up.watch
  (:require
   [ojo
    [watch :refer (defwatch start-watch)]
    [impl :refer (create-watch)]
    [respond :refer (response)]]
   [lamina.core :refer (enqueue) :as lamina]))

(defn start [{:keys [patterns]} bus]
  (let [watch
        (create-watch patterns
                      [:create :modify :delete]
                      :respond (response
                                (enqueue bus {:up/origin *ns*
                                              :events *events*
                                              :state *state*
                                              :settings *settings*})))]
    (future (start-watch watch))))
