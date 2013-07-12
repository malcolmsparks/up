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

(ns up.watch
  (:require
   [lamina.core :refer (enqueue) :as lamina]
   [pro.juxt.dirwatch :refer (watch-dir close-watcher)]
   [clojure.java.io :refer (file)])
  (:import
   (up.start Plugin)))

(defn emacs-tmpfile? [n]
  (re-matches #"(?:.*/)?\.?#.*" n))

(defrecord FileWatcher [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus)
          watches (-> pctx :options :watches)]
      (doall
       (for [{:keys [topic dir] :as args} watches]
         (let [dir (file dir)]
           (if-not (and dir
                        (.exists dir)
                        (.isDirectory dir))
             (throw (ex-info "Directory does not exist" {:dir dir}))
             (watch-dir #(when-not (emacs-tmpfile? (str (:file %)))
                           (enqueue bus {:up/topic topic
                                         :event %}))
                        dir)))))))
  (stop [_ watchers]
    (doseq [w watchers] (close-watcher w))))
