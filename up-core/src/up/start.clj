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

(ns up.start
  (:require
   [clojure.pprint :refer (pprint)]
   [clojure.string :as string]
   [defemeral.defemeral :refer (defemeral shutdown)]
   [lamina.core :refer (channel enqueue filter* join map*) :as lamina]))

(defn create-bus []
  (lamina/channel* :permanent true
                   :grounded? true
                   :transactional? false
                   :description "Host event bus"))

(def bus (atom (create-bus)))

(defn init [prj]
  (println "Up"))

