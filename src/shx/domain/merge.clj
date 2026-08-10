(ns shx.domain.merge
  "Pure fragment-merge semantics. No I/O.

  Merge rules:
  - maps merge keywise (later wins)
  - vectors concatenate (order preserved, dedup happens at render)
  - anything else: later wins

  Precedence, weakest to strongest: earlier fragments < later fragments
  < top-level config < host overrides.

  Note: not in the typed.clojure gate. These fns fold heterogeneous EDN
  (validated upstream by malli in shx.law.config); typed.clojure's strict
  Any handling cannot model that without lies. The t/ann signatures below
  are living documentation, and the malli contracts + tests are the gate."
  (:require [typed.clojure :as t]))

(t/defalias Frag
  "An environment fragment: an open map of mergeable keys."
  (t/Map t/Any t/Any))

(defn merge-frag
  "Merge fragment b into a. Later values win for maps, concat for vectors."
  [a b]
  (merge-with (fn [x y]
                (cond
                  (and (map? x) (map? y)) (merge x y)
                  (and (vector? x) (vector? y)) (into x y)
                  :else y))
              a b))

(t/ann merge-frag [Frag Frag :-> Frag])

(defn fold-fragments
  "Fold a sequence of fragments into one. Later fragments override earlier."
  [frags]
  (reduce merge-frag {} frags))

(t/ann fold-fragments [(t/Seqable Frag) :-> Frag])

(defn fold-fragment-tree
  "Fold a fragment tree of {:content <map> :children [<tree>...]}
  depth-first. Children merge first, then the node's own content on top.
  This is the bubbling fix: includes are a tree-fold of data, not a
  subprocess side effect."
  [tree]
  (merge-frag (fold-fragments (map fold-fragment-tree (:children tree)))
              (or (:content tree) {})))

(t/ann fold-fragment-tree [Frag :-> Frag])

(defn apply-host-overrides
  "Merge the :hosts entry for hostname h on top of cfg."
  [cfg h]
  (if-let [ov (get-in cfg [:hosts h])]
    (merge-frag (dissoc cfg :hosts) ov)
    (dissoc cfg :hosts)))

(t/ann apply-host-overrides [Frag t/Str :-> Frag])
