(ns ^:typed.clojure shx.shape.quote
  "Pure quoting morphisms: Clojure strings -> shell text. No I/O."
  (:require [clojure.string :as str]
            [typed.clojure :as t]))

(defn sq
  "Single-quote a string for POSIX shell. Safe for arbitrary content."
  [s]
  (str "'" (str/replace (str s) "'" "'\\''") "'"))

(t/ann sq [t/Any :-> t/Str])

(defn dq
  "Double-quote a string for POSIX shell, escaping \\ \" and `.
  A leading ~ is emitted as $HOME so expansion still happens."
  [s]
  (let [s (str/replace-first (str s) #"^~" (fn [_] "$HOME"))]
    (str "\"" (-> s
                  (str/replace "\\" "\\\\")
                  (str/replace "\"" "\\\"")
                  (str/replace "`" "\\`")) "\"")))

(t/ann dq [t/Any :-> t/Str])

(defn argv->bash
  "Render an argv vector as a quoted shell command line."
  [argv]
  (str/join " " (map sq argv)))

(t/ann argv->bash [(t/Seqable t/Any) :-> t/Str])

(defn argv->bb
  "Render an argv vector as Clojure string literals for bb code generation."
  [argv]
  (str/join " " (map pr-str argv)))

(t/ann argv->bb [(t/Seqable t/Any) :-> t/Str])

(defn env-prefix
  "Render a keyword->string env map as POSIX assignment prefixes
  (e.g. GIT_PAGER='cat' ). Only valid immediately before a simple command."
  [env]
  (when (seq env)
    (str (str/join " " (for [[k v] env] (str (name k) "=" (sq v)))) " ")))

(t/ann env-prefix [(t/U nil (t/Map t/Kw t/Any)) :-> (t/U nil t/Str)])
