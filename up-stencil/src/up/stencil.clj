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

(ns up.stencil
  (:require
   [clojure.java.io :refer (file)]
   [clojure.core.cache :refer (soft-cache-factory)]
   [stencil.loader :refer (set-cache register-template invalidate-cache)]
   [lamina.core :refer (receive-all filter* map*)])
  (:import (up.start Plugin)))

(defn emacs-tmpfile? [n]
  (re-matches #"(?:.*/)?\.?#.*" n))

(defrecord TemplateService [pctx]
  Plugin
  (start [_]
    (let [bus (-> pctx :bus)]
      (set-cache (soft-cache-factory {}))
      (receive-all (->> bus 
                        (filter* (comp (partial = :up.watch/file-event) :up/event))
                        (map* #(update-in % [:events] (fn [evs] (vec (filter (comp not emacs-tmpfile? :file) evs)))))
                        (filter* (comp not empty? :events)))
                   (fn [ev] 
                     (println "invalidating cache due to file event")
                     (invalidate-cache))))))



