(ns shx.law.config
  "Malli schemas for envm configuration files (env.edn and fragments).

  These validate external-world input: anything read from disk must pass
  through here before it can influence the rendered environment."
  (:require [malli.core :as m]
            [malli.util :as mu]))

(def EvalEntry
  "A guarded eval: run :cmd only when :guard resolves on PATH."
  [:map
   [:guard :string]
   [:cmd :string]])

(def Fragment
  "An environment fragment. Fragments are data; nesting via :merge is a
  tree-fold, not a subprocess side effect, so values always bubble."
  [:map {:closed false}
   [:paths-prepend {:optional true} [:vector :string]]
   [:paths-append {:optional true} [:vector :string]]
   [:vars {:optional true} [:map-of :keyword :string]]
   [:aliases {:optional true} [:map-of :keyword :string]]
   [:source {:optional true} [:vector :string]]
   [:eval {:optional true} [:vector EvalEntry]]
   [:merge {:optional true} [:vector :string]]])

(def Config
  "Top-level config: a fragment plus per-hostname overrides."
  (mu/assoc Fragment
            [:hosts {:optional true}]
            [:map-of :string Fragment]))

(defn valid-config?
  "True if v is a valid envm config."
  [v]
  (m/validate Config v))

(defn explain-config
  "Human-readable explanation of why v is not a valid config, or nil if valid."
  [v]
  (m/explain Config v))
