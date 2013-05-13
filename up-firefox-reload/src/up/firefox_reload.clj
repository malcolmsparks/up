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

(ns up.firefox-reload
  (:require
   up.watch
   [clojure.java.io :refer (file)]
   [lamina.core :refer (receive-all filter* map*)])
  (:import
   (up.start Plugin)
   (java.net Socket)))

(defn reload
  "Send a 'reload' string via the connection to the Firefox Remote
  Control add-on."
  [{:keys [host port sock] :or {host "localhost" port 32000} :as state}]
  (letfn [(write-reload [sock]
            (let [out (.getOutputStream sock)]
              (.write out (.getBytes "reload"))
              (.flush out)))]
    (try
      (let [s (or sock (Socket. host port))] ; the existing socket unless nil
        (write-reload s)
        (assoc state :sock s))
      (catch java.io.IOException e
        ;; One retry...
        (try
          (let [s (Socket. host port)]  ; ...with a new socket
            (write-reload s)
            (assoc state :sock s))
          (catch java.io.IOException e
            ;; Give up, set sock to nil
            (dissoc state :sock)))))))

(defrecord FirefoxReloadService [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus)
          firefox (agent (:options pctx)
                         :error-mode :continue
                         :validate-fn map?)]
      (receive-all (filter* (comp (partial = :up.watch/file-event) :up/topic) bus)
                   (fn [_]
                     (println "Reloading firefox")
                     (send-off firefox reload))))))
