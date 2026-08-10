(ns shx.infra.fs
  "Filesystem guards used at render time. All I/O lives here."
  (:require [clojure.java.io :as io]
            [shx.infra.config :as cfg])
  (:import [java.io File]))

(defn existing-dirs
  "Filter paths to those that exist as directories, expanding ~,
  removing duplicates while preserving order."
  [paths]
  (->> paths
       (map cfg/expand-home)
       (filter (fn [p] (File/.isDirectory (io/file p))))
       (distinct)))
