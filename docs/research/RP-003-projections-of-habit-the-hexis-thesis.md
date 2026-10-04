# RP-003: Projections of habit — the hexis thesis

Status: Proposed
Date: 2026-08-10

## Problem

The hexis thesis claims ".bashrc is a projection of habit; a system prompt is
a projection of habit. Same fold, different emitter"
(shx/kanban/hexis-unification-epic.md:25-31) and that this is Clio law 7: "a
projection is a pure fold over the canonical event order and can be deleted
and reconstructed" (eta-mu/packages/clio/README.md:22-24). The synthesis
asserts the identity (§6: "these are the same sentence") but assertion is not
verification. Three fold implementations exist today, independently written:

1. **envm's fragment tree-fold** — `fold-fragment-tree` folds children first,
   then merges the node's own `:content` on top: closer-to-root-wins
   precedence (shx/src/shx/domain/merge.clj:41-47).
2. **muse's config fragment pipeline** — `merge-fragments` concatenates
   registry collections by kind (muse/src/cljs/eta_mu/dsl/normalize.cljc:53);
   settings fragments deep-merge "in import order (later wins)"
   (muse/src/cljs/eta_mu/opencode/settings.cljc:19-27).
3. **Clio projection folds** — pure folds over the canonical order
   (README.md:23-24; the `canonicalize` pipeline that feeds every projection is :223-237).

Research questions:

1. Are all three instances of one fold algebra? Candidate: group by id, order
   by timestamp/seq, last-wins on collision, retraction as inverse event.
   The three implementations differ: tree-fold has structural precedence
   (closer-to-root-wins), registry merge concatenates rather than
   merges-by-key, settings deep-merge is last-wins by position. Do the monoid
   laws hold for each, and can the differences be parameters of one algebra
   rather than three algebras?
2. What is the correct translation-fidelity metric across emitters? shx
   defines the raw-node ratio: the `:raw` escape hatch bounds translation loss
   and the ratio "only goes down" (shx/AGENTS.md, quality contract). Does an
   analogous metric exist for prompt-assembly fidelity — how much of a folded
   agent config survives the emitter into host-native form unlossy?
3. Can "habit" be given an operational definition as a queryable projection
   over receipt-river, session-mycology, and rheos event streams? The
   Aristotle mapping is already stated: ethos (repeated acts = the ledger)
   crystallizes into hexis (settled disposition = the projection)
   (shx/kanban/name-the-unified-package.md:29-34). A disposition you cannot
   query is not yet engineering.

## Why now

Phase 2 of the synthesis ("converge the fold", §8) cannot start until the
fold is specified; Phases 0–1 land both folds in one repo, which is when two
implementations either unify or fossilize. The emitter catalogue grows on its
own momentum (bash, bb, opencode plugin, MCP, GitHub Actions; synthesis §6),
and every emitter without a shared fold is a new place for drift. Clio's
law-7 test — "destroy the projection and reconstruct it"
(docs/inbox/2026.08.10.00.07.37.md:695-705) — is the acceptance criterion the
hexis epic's DoD (kanban/hexis-unification-epic.md:41-43) currently lacks.

## Prior art to survey

- Fowler 2005, "Event Sourcing"; Young 2010, "CQRS Documents" — projection/
  read-model separation as pattern literature.
- Meijer, Fokkinga & Paterson 1991, "Functional Programming with Bananas,
  Lenses, Envelopes and Barbed Wire" — catamorphisms are the formal home of
  "same fold, different emitter": the emitter is an algebra, the fold fixed.
- Gupta & Mumick 1995, "Maintenance of Materialized Views: Problems,
  Techniques, and Applications" — incremental re-folds on append.
- The Elm architecture / model-view-update — projection-driven UI as the same
  shape at another altitude; the system prompt is a view of the habit model.

## Open questions

- Is closer-to-root-wins (envm) expressible as last-wins over a
  tree-flattening order, or a genuinely different algebra? If the former, the
  unification is a theorem; if the latter, the algebra needs a precedence
  parameter and the monoid claim weakens.
- Retraction: neither muse's registry merge nor envm's fold has a removal
  operation. Event-sourced habit needs retraction (unlearn a habit). Does the
  algebra become a group (with inverses) rather than a monoid?
- What does the raw-node ratio count for a prompt emitter — unrendered
  fragment keys, passthrough strings, or bytes? The metric must be monotone
  like shx's ("it only goes down") to be a ratchet.
- Is a habit projection a Clio projection in the strict law-7 sense (pure,
  deletable) or does it need wall-clock decay? Decay breaks law-7 purity and
  pushes the fold into RP-001's infra.

## Proposed method

1. Specify the fold algebra as data: an EDN spec of the merge operator, its
   identity element, ordering assumptions, and retraction rule.
2. Verify monoid/associativity laws property-wise over all three
   implementations (shx `domain.merge`, muse `normalize`/`settings`, a clio
   projection) with each repo's own test runner; record which laws each
   satisfies and which it violates.
3. Build the emitter catalogue as a registry of pure `shape/` morphisms from
   folded value to host text (synthesis §6); define the fidelity metric per
   emitter and its aggregation.
4. Sketch the Phase-2 hexis-library API (synthesis §8): one fold function, an
   emitter registry, and the habit-projection query surface over
   receipt-river / session-mycology / rheos streams, as function signatures
   with malli schemas — no implementation.

## Deliverables

- The unified fold algebra spec, with law-verification results per
  implementation.
- The emitter catalogue with per-emitter fidelity metric, generalizing shx's
  raw-node ratio.
- A decision on retraction (group vs monoid) with consequences for all three
  codebases.
- The Phase-2 hexis-library API sketch (signatures + schemas only), feeding
  synthesis §8 Phase 2 and the `name-the-unified-package` decision in review.

## Fit with the lattice

Hexis names the shared projection layer (synthesis §8 Phase 2): ethos is the
ledger, hexis the disposition projected from it
(kanban/name-the-unified-package.md:29-34). This proposal is the proof
obligation behind synthesis §6's identity claim. RP-001 supplies the streams
being folded; RP-002 guarantees the emitters stay pure — a projection fold
that performs I/O is a category error the extern lint must reject.


---

## Qualification addendum — 2026-10-03

RP-003 must first publish a common input representation and explicit parameters:
input identity, tree/sequence precedence, canonical ordering, shallow/deep map
collision behavior, vector concatenation, and retraction. A law proven under one
parameter set cannot silently stand for a different fold. Then run the same
fixture corpus through each versioned implementation/adapter and compare results;
record failures/counterexamples and which laws apply. No equivalence is claimed
until those artifacts exist. The emitter registry remains pure value-to-text/data
only; transports and apply operations stay outside it.
