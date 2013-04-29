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

(read "../project.clj")
(def version (get-version))

(defproject lein-up version
  :description "Leiningen plugin for Up."
  :url "http://github.com/malcolmsparks/up"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :plugins [[lein-localrepo "0.4.1"]]
  :dependencies [[up/up-core ~version]
                 [org.clojure/clojure "1.5.1"]]
  :aliases {"install-local" ["localrepo" "install" ~(str "target/lein-up-" version ".jar") "lein-up" ~version]})
