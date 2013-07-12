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


(defn head-ok []
  (-> (sh "git" "rev-parse" "--verify" "HEAD")
      :exit zero?))

(defn refresh-index []
  (sh "git" "update-index" "-q" "--ignore-submodules" "--refresh")
)

(defn unstaged-changes []
  (-> (sh "git" "diff-files" "--quiet" "--ignore-submodules")
      :exit zero? not))

(defn uncommitted-changes []
  (-> (sh "git" "diff-index" "--cached" "--quiet" "--ignore-submodules" "HEAD" "--")
      :exit zero? not))

;; We don't want to keep having to 'bump' the version when we are
;; sitting on a more capable versioning system: git.
(defn get-version []
  (when (not (head-ok)) (throw (ex-info "HEAD not valid" {})))
  (refresh-index)
  (let [{:keys [exit out err]} (sh "git" "describe" "--tags" "--long")]
    (if (= 128 exit) "0.0.1"
        (let [[[_ tag commits hash]] (re-seq #"(.*)-(.*)-(.*)" out)]
          (if (and
               (zero? (edn/read-string commits))
               (not (unstaged-changes))
               (not (uncommitted-changes)))
            tag
            (let [[[_ stem lst]] (re-seq #"(.*\.)(.*)" tag)]
              (join [stem (inc (read-string lst)) "-" "SNAPSHOT"])))))))

;; TODO: Update these - or use lein-ancient
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
        "up-pedestal-webapp"
        "up-watch"
        "up-stencil"
        "up-firefox-reload"
        "lein-up"
])
