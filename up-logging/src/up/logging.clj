(ns up.logging
  (:require
   [lamina.core :refer (channel* join close receive-all) :as lamina])
  (:import (up.start Plugin)))

(def log (channel* :grounded? true))

(defn console-logger [ev]
  (println "UP>" (if-let [mt (meta ev)] (format "[meta: %s]" (str mt)) "") ev))

(defrecord Logging [pctx]
  Plugin
  (start [_]
    (println "logging start")
    (join (:bus pctx) log)
    (receive-all log console-logger))
  (stop [_ started]
    (println "logging stop")))
