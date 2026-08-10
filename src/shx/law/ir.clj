(ns shx.law.ir
  "Malli schemas for the shx intermediate representation.

  Every IR node is a hiccup-style vector with a keyword head. Nodes fall into
  three families:

  - shell-state effects: :export :path/prepend :alias :set :source
  - process effects:     :exec :pipe :capture
  - composition:         :if :test
  - escape hatch:        :raw (worst case: run the original text verbatim)"
  (:require [malli.core :as m]))

(def ExportArgs
  [:map
   [:name :string]
   [:value :string]])

(def AliasArgs
  [:map
   [:name :string]
   [:argv [:vector :string]]])

(def ExecArgs
  [:map
   [:argv [:vector {:min 1} :string]]
   [:dir {:optional true} :string]
   [:env {:optional true} [:map-of :keyword :string]]])

(def SetArgs
  [:map
   [:name :string]
   [:value [:or :string [:tuple [:= :capture] [:ref :shx/node]]]]])

(def RawArgs
  [:map
   [:lang :string]
   [:text :string]])

(def PathGuard
  [:map [:if {:optional true} [:enum :dir-exists]]])

(def registry
  {:shx/node [:orn
              [:export :shx/export]
              [:path/prepend :shx/path-prepend]
              [:path/append :shx/path-append]
              [:alias :shx/alias]
              [:source :shx/source]
              [:exec :shx/exec]
              [:pipe :shx/pipe]
              [:capture :shx/capture]
              [:set :shx/set]
              [:if :shx/if]
              [:test :shx/test]
              [:raw :shx/raw]]
   :shx/export [:tuple [:= :export] ExportArgs]
   :shx/path-prepend [:cat [:= :path/prepend] :string [:? PathGuard]]
   :shx/path-append [:cat [:= :path/append] :string [:? PathGuard]]
   :shx/alias [:tuple [:= :alias] AliasArgs]
   :shx/source [:tuple [:= :source] :string]
   :shx/exec [:tuple [:= :exec] ExecArgs]
   :shx/pipe [:cat [:= :pipe] [:+ [:schema [:ref :shx/node]]]]
   :shx/capture [:tuple [:= :capture] [:ref :shx/node]]
   :shx/set [:tuple [:= :set] SetArgs]
   :shx/if [:cat [:= :if] [:schema [:ref :shx/node]] [:schema [:ref :shx/node]]
            [:? [:schema [:ref :shx/node]]]]
   :shx/test [:cat [:= :test] [:* :string]]
   :shx/raw [:tuple [:= :raw] RawArgs]})

(def Node
  "A single IR node."
  [:schema {:registry registry} :shx/node])

(def Program
  "A program is one or more nodes."
  [:+ [:schema {:registry registry} :shx/node]])

(defn valid-node?
  "True if v is a valid IR node."
  [v]
  (m/validate Node v))

(defn valid-program?
  "True if v is a valid IR program."
  [v]
  (m/validate Program v))

(defn explain-program
  "Human-readable explanation of why v is not a valid program, or nil if valid."
  [v]
  (m/explain Program v))
