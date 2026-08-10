(ns shx.infra.config
  "Config file loading: the external-world boundary.

  Everything read here is validated against shx.law.config before it can
  influence the environment. Failure semantics: missing files, bad EDN,
  schema violations, and include cycles all warn and degrade; a broken
  fragment can never wedge a login shell."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [shx.law.config :as law])
  (:import [java.io File]))

(defn warn
  "Print a warning to stderr. Never throws."
  [& msgs]
  (binding [*out* *err*]
    (apply println "shx:" msgs)))

(defn expand-home
  "Expand a leading ~ to the user's home directory."
  [s]
  (str/replace-first s #"^~" (System/getProperty "user.home")))

(defn default-config-path
  "Config path: $ENVM_CONFIG, else ~/.config/envm/env.edn."
  []
  (or (System/getenv "ENVM_CONFIG")
      (expand-home "~/.config/envm/env.edn")))

(defn hostname
  "The local hostname, for :hosts overrides."
  []
  (or (System/getenv "HOSTNAME")
      (-> (Runtime/getRuntime)
          (Runtime/.exec (into-array String ["hostname"]))
          (Process/.getInputStream)
          (slurp)
          (str/trim))))

(defn read-edn-file
  "Read and parse an EDN file. Returns nil (with warning) on any failure."
  [path]
  (let [f (io/file (expand-home path))]
    (if-not (File/.isFile f)
      (do (warn "file not found:" (File/.getPath f)) nil)
      (try
        (edn/read-string (slurp f))
        (catch Exception e
          (warn "bad edn in" (File/.getPath f) "-" (ex-message e))
          nil)))))

(defn read-fragment-tree
  "Read a fragment file into a tree of {:content <map> :children [...]}.
  Validates each fragment against shx.law.config/Fragment.
  Cycles warn and terminate the branch. Missing/invalid files yield
  empty content. seen is a set of canonical paths on the current branch."
  ([path] (read-fragment-tree path #{}))
  ([path seen]
   (let [f (File/.getCanonicalFile (io/file (expand-home path)))]
     (if (contains? seen (File/.getPath f))
       (do (warn "cycle in :merge at" (File/.getPath f))
           {:content {} :children []})
       (let [data (read-edn-file (File/.getPath f))
             valid? (and (map? data)
                         (or (law/valid-config? data)
                             (do (warn "invalid fragment" (File/.getPath f) "-"
                                       (pr-str (law/explain-config data)))
                                 false)))]
         (if-not valid?
           {:content {} :children []}
           {:content (dissoc data :merge)
            :children (mapv #(read-fragment-tree % (conj seen (File/.getPath f)))
                            (:merge data))}))))))

(defn load-config
  "Load the top-level config file. Exits with an informative message if
  missing or invalid (unlike fragments, the top config is mandatory)."
  [path]
  (let [data (read-edn-file path)]
    (when-not (map? data)
      (warn "config not found or unreadable:" path)
      (System/exit 1))
    (when-not (law/valid-config? data)
      (warn "invalid config" path "-" (pr-str (law/explain-config data)))
      (System/exit 1))
    data))
