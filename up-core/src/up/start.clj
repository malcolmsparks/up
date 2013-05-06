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
   [clojure.walk :as walk]
   [lamina.core :refer (channel enqueue filter* join map*) :as lamina]
   [leiningen.core.classpath :as classpath]
   ))

(defn create-bus []
  (lamina/channel* :permanent true
                   :grounded? true
                   :transactional? false
                   :description "Host event bus"))

(def bus (atom (create-bus)))

(defn forms [r]
  (when-let [fm (read r false nil)]
    (cons fm (lazy-seq (forms r)))))

(defn get-up-config-from-jar [f]
  (let [jar (java.util.jar.JarFile. f) 
        je (.getEntry jar "project.clj")]
    (let [r (java.io.PushbackReader. (java.io.InputStreamReader. (.getInputStream jar je)))]
      (:up (apply hash-map (drop 3 (last (filter #(= (first %) 'defproject) (forms r) ))))))))

(defprotocol Plugin
  (start [_])
  (stop [_ started]))

(defn init [prj]
  (println "Up")
  (pprint "configuration>")
  (pprint (:up prj))

  (let [plugins (apply hash-set (-> prj :up :plugins keys))
        prj+plugins (update-in prj [:dependencies]
                               concat plugins)]

    ;; Add to classpath
    (classpath/resolve-dependencies :dependencies prj+plugins :add-classpath? true)

    ;; Initialize plugins
    (doseq [pentry (tree-seq coll? seq (classpath/dependency-hierarchy :dependencies prj+plugins))
            :when (and (coll? pentry) (plugins (first pentry)))
            :let [pdef (first pentry)]]
      (let [{:keys [plugin]} (get-up-config-from-jar (-> pdef meta :file))]
        (when plugin
          (require (symbol (namespace plugin)))
          (let [pconf (get-in prj [:up :plugins pdef])
                pctx {:options pconf :bus @bus}
                rec (ns-resolve (symbol (namespace plugin)) (symbol (name plugin)))]
            (when (nil? rec) (throw (Exception. (format "Cannot find plugin: %s" plugin))))
            (let [ctr (.getConstructor rec 
                                       (into-array Class [Object]))]
              (when (nil? ctr) (throw (Exception. (format "Plugin must have a single-arg constructor: %s" plugin))))
              (let [inst (.newInstance ctr (into-array [pctx]))]
                (println "Starting plugin: " plugin "with config" pconf)
                (start inst)))))))
    
    ;; Enqueue test message
    (enqueue @bus "Plugins initialised")))

