# ADR-0004: Sequence absorption, then converge the fold

Status: Proposed
Date: 2026-08-10

## Context

Two boards hold a live contradiction (C2 in the synthesis
`/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md:215-245`):

- **shx board** — `hexis-unification-epic` (P1, 13sp): **one** package
  unifying shx + muse, named Hexis. Definition of Done: "One package under
  `~/spaces/eta-mu/packages/` that emits both shell environment text and
  agent instruction files from one resource fold"
  (`/home/err/spaces/shx/kanban/hexis-unification-epic.md:42-43`).
- **eta-mu board** — `absorb-shx` (P3, 8sp) + `absorb-muse-keryx`
  (P2, 13sp): **two** packages, `packages/shx` and `packages/muse`,
  imported with history, shx first as the pathfinder
  (`/home/err/spaces/eta-mu/kanban/epics/absorb-shx-into-the-eta-mu-monorepo-sorption.md:23-24`;
  synthesis `:220-222`).

The contradiction is about *timing*, not destination (synthesis `:224`).

The destination itself is grounded in an identity the synthesis §6 makes
precise (`:164-188`): the hexis thesis — ".bashrc is a projection of
habit. A system prompt is a projection of habit. Same fold, different
emitter" (`hexis-unification-epic.md:27-31`) — **is** Clio law 7: "A
projection is a pure fold over the canonical event order and can be
deleted and reconstructed"
(`/home/err/spaces/eta-mu/packages/clio/README.md:23-24`). Config
rendering is event-sourced projection: envm's `fold-fragment-tree`, muse's
opencode settings deep-merge, and a Clio projection are one operation at
three altitudes (synthesis `:176-181`).

A naming card sits in `review`:
`/home/err/spaces/shx/kanban/name-the-unified-package.md` proposes Hexis
(Aristotle: ethos crystallizes into hexis; "Nothing has been renamed",
`:48`).

## Decision

Resolve C2 by sequencing, not choosing (synthesis `:224-245`):

- **Phase 0 — absorb separately.** shx lands as `packages/shx` per
  ADR-0001, proving the import procedure. Muse lands as `packages/muse`
  with its Mongo boundary reconciled to a Clio adapter
  (`/home/err/spaces/eta-mu/kanban/epics/absorb-muse-keryx-into-the-eta-mu-monorepo-sorption.md:38-40`).
- **Phase 1 — extract keryx on clio.** `packages/keryx` per ADR-0002,
  delivered over the Clio medium per ADR-0003. New relative to both
  boards; the concrete form of the 2026-08-10 declaration.
- **Phase 2 — converge the fold.** Hexis names the **shared projection
  layer**: one `domain.merge`-style resource fold plus an emitter
  registry, consumed by `packages/shx`, `packages/muse`, and rheos alike.
  Hexis is realized as a **library, not a product merger** (synthesis
  `:235-238`).
- `name-the-unified-package` stays in `review` until Phase 2. **Nothing is
  renamed in Phases 0–1** (synthesis `:238-240`, non-goal `:301`).

Rationale: the absorption epics encode the lower-risk path (proven import,
clean-clone gates, first-consumer requirements); the hexis epic encodes
the true destination (one fold). Sequencing preserves both; choosing
either outright loses one (synthesis `:242-245`).

## Consequences

- A fold library consumed by multiple products is a **proposed reinterpretation**,
  not satisfaction of the existing one-package Hexis DoD. Phase 2 requires user
  ratification and corresponding lawful board clarifications before either
  board can claim that destination complete. The original epic remains intact.
- The `shx.shape.bb` placeholder problem (`shx.core` does not exist) gets
  its answer in Phase 1+: the bb runtime is a Keryx actor applying
  Psephismata to shell state (synthesis `:185-188`).
- Both boards close without either being declared wrong; the naming
  decision is deferred to the phase where a rename is cheap and informed.
- Phase 2 cannot start before Phase 1 lands: "same fold, different
  emitter" can only be *implemented* once both folds live in one repo
  (synthesis `:234-235`).

## Alternatives considered

- **Hexis-first** (merge into one package immediately, per the literal
  hexis epic DoD). Rejected: the import procedure is unproven — shx is
  carded precisely as "the one that proves the import procedure"
  (absorb-shx epic `:23-24`) — and merging two products before either
  lands is the higher-risk path (synthesis `:227-230`, `:242-245`).
- **Absorb and never converge** (two packages, two folds, done). Rejected:
  two fold implementations (`shx.domain.merge` and muse's settings merge)
  and two emitter families (bash/bb vs opencode plugin/MCP/GitHub Actions)
  drift; the duplication the synthesis identifies (`:183-185`) becomes
  permanent, and the hexis thesis — which is Clio law 7 — goes unrealized.

## Connections

- Synthesis: `/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md` §6, §8, §12.
- Cards: `/home/err/spaces/shx/kanban/hexis-unification-epic.md`;
  `/home/err/spaces/shx/kanban/name-the-unified-package.md`;
  `/home/err/spaces/eta-mu/kanban/epics/absorb-shx-into-the-eta-mu-monorepo-sorption.md`;
  `/home/err/spaces/eta-mu/kanban/epics/absorb-muse-keryx-into-the-eta-mu-monorepo-sorption.md`.
- Law 7: `/home/err/spaces/eta-mu/packages/clio/README.md:23-24`.
- Executes ADR-0001, ADR-0002, ADR-0003 in phase order.


---

## Qualification addendum — 2026-10-03

This sequencing is proposed. A multi-product shared fold remains a different
acceptance contract from the existing one-package epic. User ratification, lawful
board clarifications, and RP-003 equivalence evidence are required before Phase 2
or any completion claim. Earlier wording cannot substitute for those acts.
