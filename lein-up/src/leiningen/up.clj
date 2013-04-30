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

(ns leiningen.up
  (:require [clojure.pprint :refer (pprint)]
          )
  (:use [leiningen.core.eval :as eval]
        [leiningen.core.classpath :as classpath]))

(defn up [project & args]
  (eval/eval-in-project
   (update-in project [:dependencies]
              concat [['up/up-core "0.0.1"]])
   `(up.start/init (quote ~project))
   '(require 'up.start)))
