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
   [ojo
    [watch :refer (defwatch start-watch)]
    [impl :refer (create-watch)]
    [respond :refer (response)]]
   [lamina.core :refer (enqueue) :as lamina])
  (:import (up.start Plugin)))

(defn emacs-tmpfile? [n]
  (re-matches #"(?:.*/)?\.?#.*" n))

(defrecord FileWatcher [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus)
          patterns (-> pctx :options :patterns)
          watch
          (create-watch patterns
                        [:create :modify :delete]
                        :respond (response
                                  (doseq [ev *events*
                                          :when ((comp not emacs-tmpfile? :file) ev)]
                                    (enqueue bus {:up/event :up.watch/file-event
                                                  :event ev
                                                  :state *state*
                                                  :settings *settings*}))))]
      (future (start-watch watch)))))
