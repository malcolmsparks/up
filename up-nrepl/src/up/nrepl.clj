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

(ns up.nrepl
  (:require
   [clojure.tools.nrepl.server :refer (start-server stop-server default-handler)])
  (:import (up.start Plugin)))

(defrecord NReplService [pctx]
  Plugin
  (start [_]
    {:pre [(number? (-> pctx :options :port))]}
    (start-server
     :host "127.0.0.1"
     :port (-> pctx :options :port)
     :handler (default-handler))))
