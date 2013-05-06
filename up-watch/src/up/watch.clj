(ns up.watch
  (:require
   [ojo
    [watch :refer (defwatch start-watch)]
    [impl :refer (create-watch)]
    [respond :refer (response)]]
   [lamina.core :refer (enqueue) :as lamina])
  (:import (up.start Plugin)))

(defrecord FileWatcher [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus) 
          patterns (-> pctx :options :patterns) 
          watch
          (create-watch patterns
                        [:create :modify :delete]
                        :respond (response
                                  (enqueue bus {:up/event :up.watch/file-event
                                                :events *events*
                                                :state *state*
                                                :settings *settings*})))]
      (future (start-watch watch)))))
