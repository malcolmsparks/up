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

(require '(clojure [string :refer (join)]
                   [edn :as edn])
         '(clojure.java [shell :refer (sh)]))

;; We don't want to keep having to 'bump' the version when we are
;; sitting on a more capable versioning system: git.
(def get-version 
  (memoize
   (fn []
     (let [{:keys [exit out err]} (sh "git" "describe" "--tags" "--long")] 
       (if (= 128 exit) "0.0.1"
           (let [[[_ tag commits hash]] (re-seq #"(.*)-(.*)-(.*)" out)]
             (if (zero? (edn/read-string commits)) 
               tag 
               (let [[[_ stem lst]] (re-seq #"(.*\.)(.*)" tag)]
                 (join [stem (inc (read-string lst)) "-" "SNAPSHOT"])))))))))

(def versions {:lamina "0.5.0-beta15"
               :pedestal "0.1.2"})

(defproject up/up (get-version)
  :description "Up - A Clojure development and deployment system."
  :url "http://github.com/malcolmsparks/up"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-sub "0.2.3"]]
  :sub ["up-core" 
        "up-logging" 
        "up-http" 
        "up-nrepl" 
        "lein-up"])
