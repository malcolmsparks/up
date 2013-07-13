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

(read "project.clj")
(def version (get-version))

(defproject up/up-firefox-reload version
  :description "Send to Firefox a reload request via the Firefox 'Remote Control' add-on."
  :url "http://github.com/malcolmsparks/up"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[up/up-core ~version]
                 [up/up-watch ~version]]

  :up {:component up.firefox-reload/FirefoxReloadService})
