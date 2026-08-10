# RP-002: One extern-boundary law across three repos

Status: Proposed
Date: 2026-08-10

## Problem

Three repos independently encode the same purity stratification with different
vocabularies and different enforcement:

- **shx** — four quadrants (`domain/` pure, `infra/` all I/O, `shape/` pure
  morphisms, `law/` contracts only; shx/AGENTS.md). Enforcement: review plus
  typed.clojure on one namespace; the layer-boundary check is not yet ported
  (docs/static-analysis.md:148-153).
- **muse** — `boundaries.*` namespaces with docstring-level laws: "the ONLY
  namespace allowed to touch JS values, #js literals, promises-as-transport"
  (muse/src/cljs/eta_mu/boundaries/opencode.cljs:2-9; likewise
  node/fs.cljs:2-3, node/watch.cljs:2-4). Enforcement: docstrings.
- **eta-mu** — the constitution: `law -> shape -> extern -> domain -> infra`,
  kondo-enforced via `@open-hax/kondo-config` `hooks.layer-boundaries`, plus
  clio's edamame lint refusing `js/`, `#js`, `js*`, string host requires
  outside `clio.extern.js.*` (eta-mu/packages/clio/README.md:143-181).

The terminology fork is recorded: muse code says `boundaries`, the constitution
says `extern`, and muse's own AGENTS.md disagrees with its code (synthesis
§10). The synthesis resolves the name ("standardize on `extern`; constitution
wins") but not the rule set.

Research questions:

1. Can one mechanically checkable "extern calculus" subsume all three layouts —
   shx's JVM quadrants, muse's CLJS boundaries, clio's `.cljc`-with-`.cljs`
   -extern split?
2. What is the minimal rule set: which namespace families may reference `js/`,
   `#js`, `node:*`, `java.*`, promises-as-transport?
3. How do reader conditionals interact with the lint? Clio parses both
   branches of every `.cljc` with edamame, keeping `#js` as a tagged literal
   (README.md:169-177). May the `:cljs` branch of a `domain/` namespace touch
   `js/` if the `:clj` branch is pure?
4. Can the lint be proven *complete* (no interop outside extern) or only
   sound-ish? Known holes today: dynamic `require`, `js*` emitted by macros,
   interop smuggled through `(.-prototype js/Object)` chains, Java interop in
   `.cljc` `:clj` branches that kondo's layer hook does not classify.

## Why now

Absorption is sequenced, not optional: shx lands as `packages/shx`, muse as
`packages/muse`, keryx is extracted (synthesis §8). Three boundary disciplines
entering one monorepo with one gate will either be unified mechanically or
diverge silently. Clio's edamame lint is the only one of the three that reads
forms rather than text or convention (README.md:169-171); shx's gating-at-zero
policy (docs/static-analysis.md:37-54) is the only enforcement posture proven
to hold. Both already exist; the research is whether they compose into one
law. `../Truth`'s unported layer-boundary check (static-analysis.md:148-153)
is the fourth data point: the pattern recurs wherever these repos grow.

## Prior art to survey

- Plotkin & Pretnar 2009, "Handlers of Algebraic Effects" — effects as
  operations with handlers; extern namespaces are handler sites.
- Miller 2006, *Robust Composition* (PhD dissertation, Johns Hopkins) — capability discipline;
  `node:fs` reachability is a capability question.
- The IO monad's isolation of effects in Haskell (standard literature) — the
  canonical typed boundary; our lint is its untyped, syntactic analogue.
- Bernhardt 2012, "Boundaries" (talk) — "functional core, imperative shell",
  the pattern's popular statement; all three repos are instances.
- Rust's `unsafe` blocks as audited-effect regions — the closest industrial
  analogue to an enforced, greppable boundary with a completeness argument.
- Free monads / interpreters (standard literature) — shx's IR and clio's
  Psephisma-as-data are both "effects reified as data, interpreted at the
  boundary".

## Open questions

- Is promises-as-transport an extern concern (muse says yes,
  opencode.cljs:2-9) or an infra concern? Clio's split suggests transport
  belongs to `extern`, orchestration to `infra`; muse's `eta-mu.actor`
  normalizes promises in what looks like domain code (actor.cljc:1-7).
- Does the kondo layer hook need reader-conditional branch sensitivity, or is
  edamame the only honest reader and kondo keeps to namespace-level order?
- What is the JVM-side analogue of the `#js`/`js/` rule — `java.*` outside
  `infra/`? shx's quadrants imply it; nothing checks it.
- Completeness: can the rule set be closed under macro expansion (lint the
  expansion, not the source), or is "sound-ish with named holes" the honest
  claim?

## Proposed method

1. Catalog every interop site in all three repos by parsing with edamame
   (clio's approach) and kondo analysis export; classify by namespace family
   and interop kind. This is the empirical rule set, stated after the fact.
2. Write the unified rule spec as a decision table: namespace family × interop
   kind × reader branch → allowed | finding. Diff it against the catalog;
   every disagreement is either a rule fix or a code fix, enumerated.
3. Implement as one edamame lint (runtime-neutral `.cljc`, per clio's
   scripts/clio/lint_extern_boundary.cljc model) plus the existing kondo
   hook; run over all three layouts.
4. Propose enforcement tiering on the shx model: all findings blocking, only
   after a zero-findings baseline is reached and recorded, with per-site
   suppressions carrying reasons (docs/static-analysis.md:37-54, 85-115).
5. Migration plan for muse `boundaries.*` → `extern.*`: mechanical rename,
   docstring laws promoted into lint rules, no behavior change.

## Deliverables

- The unified rule spec (decision table) with the completeness argument stated
  honestly: what is proven, what is heuristic, what is out of scope.
- A proposed kondo hook + edamame lint working over all three repos' layouts.
- The muse `boundaries` → `extern` migration plan (synthesis §10 execution).
- An enforcement-tiering proposal generalizing shx's gating-at-zero policy to
  the monorepo.

## Fit with the lattice

The lattice is a purity claim: law → shape → extern → domain → infra is the
construction order the synthesis, clio, and the hexis epic all cite
(kanban/hexis-unification-epic.md:21-23). RP-001 depends on this boundary to
guarantee that Psephisma application is the only effectful transition.
RP-003's "emitters are pure shape/ morphisms off the folded value"
(synthesis §6) is exactly what this lint must prove mechanically.
