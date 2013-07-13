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
   [loom
    [graph :refer (graph)]
    [io :refer (view) :rename {view view-graph}]
    [alg :refer (pre-traverse)]]))

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

(defprotocol Lifecycle
  (start [_])
  (stop [_ started]))

(defn init [prj]
  (println "Up")
  (print "Configuration :-")
  (pprint (:up prj))

  (let [components (set (-> prj :up :components keys))
        prj+components (update-in prj [:dependencies]
                               concat components)]

    ;; Add to classpath
    (classpath/resolve-dependencies :dependencies prj+components :add-classpath? true)

    ;; This code used to use classpath/dependency-hierarchy over the
    ;; whole dependency set. Unfortunately, transitive dependency paths
    ;; are eliminated such that there is only one path per leaf. This is
    ;; fine for Leiningen, because it's trying to build a
    ;; classpath. However, this method is not adequte for finding all
    ;; the transitive paths since some dependency relationships between
    ;; components are dropped and the consequence is that components are
    ;; loaded in the wrong order. Therefore we need to call
    ;; classpath/dependency-hierarchy on each individual component, and use
    ;; loom to give us the dependency order.

    ;; Initialize components
    (println "Initializing components: " components)
    (doseq [pdef
            (->> (for [pg components
                       [k1 v] (classpath/dependency-hierarchy :dependencies {:dependencies [pg]})
                       k2 (keys v)]
                   [k1 k2])
                 (apply graph)
                 pre-traverse
                 (filter (set components)))]
      (let [{:keys [component]} (get-up-config-from-jar (-> pdef meta :file))]
        (when component
          (require (symbol (namespace component)))
          (let [pconf (get-in prj [:up :components pdef])
                pctx {:options pconf :bus @bus}
                rec (ns-resolve (symbol (namespace component)) (symbol (name component)))]
            (when (nil? rec) (throw (Exception. (format "Cannot find component: %s" component))))
            (let [ctr (.getConstructor rec
                                       (into-array Class [Object]))]
              (when (nil? ctr) (throw (Exception. (format "Component must have a single-arg constructor: %s" component))))
              (let [inst (.newInstance ctr (into-array [pctx]))]
                (println "Starting component: " component "with config" pconf)
                (start inst)))))))

    ;; Enqueue test message
    (enqueue @bus "Components initialized"))

  (let [component (-> prj :up :component)]
    (when component
      (require (symbol (namespace component)))
      (let [pctx {:bus @bus}
            rec (ns-resolve (symbol (namespace component)) (symbol (name component)))]
        (when (nil? rec) (throw (Exception. (format "Cannot find component: %s" component))))
        (let [ctr (.getConstructor rec
                                   (into-array Class [Object]))]
          (when (nil? ctr) (throw (Exception. (format "Component must have a single-arg constructor: %s" component))))
          (let [inst (.newInstance ctr (into-array [pctx]))]
            (println "Starting component: " component)
            (start inst))))))

  (println "Application initialized"))
