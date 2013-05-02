(ns up.logging
  (:require
   [lamina.core :refer (channel* join close receive-all) :as lamina]))

(def log (channel* :grounded? true))

(defn console-logger [ev]
  (println "UP>" (if-let [mt (meta ev)] (format "[meta: %s]" (str mt)) "") ev))

(defn start [options bus]
  (println "logging start")
  (join bus log)
  (receive-all log console-logger))


(defn stop []
  (println "logging stop"))
