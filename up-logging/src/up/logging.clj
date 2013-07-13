(ns up.logging
  (:require
   [lamina.core :refer (channel* join close receive-all) :as lamina]
   [taoensso.timbre :as timbre :refer (trace debug info warn error fatal spy)])
  (:import (up.start Plugin)))

(def log (channel* :grounded? true))

(defn console-logger [ev]
  (debug (if-let [mt (meta ev)] (format "[meta: %s]" (str mt)) "") ev))

(defrecord Logging [pctx]
  Plugin
  (start [_]
    (println "logging start")
    (join (:bus pctx) log)
    (receive-all log console-logger))
  (stop [_ started]
    (println "logging stop")))
