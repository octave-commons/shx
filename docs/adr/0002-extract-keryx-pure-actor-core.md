# ADR-0002: Extract muse's pure actor core as `packages/keryx`

Status: Proposed
Date: 2026-08-10

## Context

The user's declaration of 2026-08-10: **Keryx becomes the fundamental unit
of computation; Clio the medium.** The unit is "an envelope, lawfully
delivered": immutable, causally linked, validated at admission
(`/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md:79-83`).

Two definitions of Keryx are in flight (synthesis §3):

- **Keryx-A, the herald/translation role** — muse's design dialogue: "a
  herald whose function is to carry messages between realms without
  distortion" (`/home/err/spaces/muse/docs/inbox/2026.07.11.17.19.10.md:3016`).
  eta-mu's 2026-07-29 resolution assigns this role to Muse: "Keryx names
  that herald/translation responsibility, **not a second competing package
  yet**" (synthesis `:68-71`).
- **Keryx-B, the fundamental unit** — today's declaration: the pure actor
  message-passing core, domain data before the harness boundary.

The synthesis resolves the tension: these are the same thing seen from two
ends. A herald *is* a message carried lawfully; a composition of lawful
deliveries *is* what translation at a host boundary does (synthesis
`:75-88`). The unit already exists as running code inside muse; it is
"half-built — runs inside muse, not extracted" (synthesis `:48`).

The extraction map (synthesis §4) classifies muse's actor namespaces:

**Extract (pure, `.cljc`, zero I/O):**

- `eta-mu.actor.store` — `envelope-schema`, `IActorStore`
  (`muse/src/cljs/eta_mu/actor/store.cljc:20-49`). The unit's data contract.
- `eta-mu.actor.envelope` — `fill-defaults`, `stamp-route`, `->line`,
  `parse-line` (`envelope.cljc:26-66`).
- `eta-mu.actor.memory`'s shared pure fns — `recv-view`, `spawn-meta`,
  `routed-envelope` (`memory.cljc:8-38`).

**Not pure — extract only with the Nomos refactor, not as a move:**

- `eta-mu.actor.muse` — Muse/Phase domain logic (`muse.cljc:17-111`).
  Despite the proto-Nomos role, it is effectful as written: it requires
  `eta-mu.actor` and `promesa` (`muse.cljc:10-11`), and `influence!` calls
  `actor/tell!` — a ledger append through the store (`muse.cljc:69-74`).
  Extraction requires inverting the store dependency (history in, Psephisma
  out); that inversion is the Nomos spec, a prerequisite card in the keryx
  epic.

**Stays harness-side:** `eta-mu.actor.backend`,
`eta-mu.boundaries.node.ledger`, `eta-mu.boundaries.mongo.ledger`,
`eta-mu.boundaries.opencode`, `eta-mu.daemon.core`, `eta-mu.dsl.*`,
`.ημ/plugins/*` (synthesis `:113-120`).

The original design dialogue's anti-runtime stance is binding: "There is
deliberately **no** `keryx.runtime`, `keryx.core`, `keryx.util`, or
`keryx.engine` namespace. If code cannot be placed in one of those named
responsibilities, it has not been understood enough to write"
(`/home/err/spaces/muse/docs/inbox/2026.07.11.17.19.10.md:3466`; synthesis
§12 `:301-303`).

## Decision

Extract muse's pure actor/message-passing core into `packages/keryx` —
`.cljc`, zero I/O, on the clio construction order
(`law -> shape -> extern -> domain -> infra`,
`/home/err/spaces/eta-mu/packages/clio/README.md:145-149`).

- Move the three pure groups above, re-prefixing `eta-mu.` → `keryx.`;
  `IActorStore` and `envelope-schema` move unchanged — the unit's contract.
  `eta-mu.actor.muse` stays in muse until the Nomos inversion (see Context);
  its pure readers (`tail`, `observations`, `conclusions`, `evidence`) may
  move earlier if the store dependency is injected rather than required.
- No `keryx.runtime`, `keryx.core`, `keryx.util`, or `keryx.engine`
  namespaces. Code fitting no named responsibility is not understood
  enough to extract.
- Extraction follows muse's landing as `packages/muse` (ADR-0001
  pathfinder first), as a child of the absorb-muse-keryx epic
  (`/home/err/spaces/eta-mu/kanban/epics/absorb-muse-keryx-into-the-eta-mu-monorepo-sorption.md`),
  retargeted onto Clio's envelope per ADR-0003.

## Consequences

- muse's `eta-mu.actor` public API becomes a thin facade over keryx plus a
  store backend; the facade lives in `packages/muse`, backend namespaces stay
  harness-side. Unchanged behavior is a compatibility goal, conditional on the
  versioned migration/replay contract below; it has not been demonstrated.
- The 2026-07-29 resolution stays true: this package is the unit, not a
  second universal-harness compiler, so it does not compete with Muse's
  herald/translation role (synthesis `:90-93`).
- `eta-mu.actor.muse`'s "she is not an orchestrator" property
  (`muse.cljc:9`) is an actor-semantics property, not a purity one; it is
  preserved regardless of where the namespace lives. Its extraction is
  blocked on the Nomos dependency inversion, not on this ADR.
- The extraction makes physical a purity boundary that today exists only
  as discipline (synthesis `:122`): any future I/O in the unit fails the
  extern-boundary lint.
- The envelope's comment noting it *mirrors* `@promethean-os/event-ledger`
  (`muse/src/cljs/eta_mu/actor/envelope.cljc:2-4`) is answered: that
  authority is dead, superseded by Clio (synthesis `:123-126`); ADR-0003
  completes the retarget.

## Alternatives considered

- **Leave the core inside muse.** Rejected: the unit remains trapped
  across a repo boundary from clio and axxium (synthesis `:55-59`), and
  "fundamental unit of computation" cannot be a subdirectory of one
  harness product.
- **Extract with a runtime (`keryx.engine`) that executes actors.**
  Rejected: violates the anti-runtime stance of the design dialogue
  (`2026.07.11.17.19.10.md:3466`); execution belongs to harnesses (daemon,
  opencode plugin), not to the unit.
- **Build keryx fresh beside muse.** Rejected: muse's `actor/*` is the
  only running implementation; a greenfield twin would fork semantics
  instead of extracting them.

## Connections

- Synthesis: `/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md` §3, §4, §12.
- Design dialogue: `/home/err/spaces/muse/docs/inbox/2026.07.11.17.19.10.md:3016,3466`.
- Extraction sources: `muse/src/cljs/eta_mu/actor/store.cljc:14-49`,
  `envelope.cljc:26-66`, `memory.cljc:8-38`, `muse.cljc:17-111`.
- Parent epic: `/home/err/spaces/eta-mu/kanban/epics/absorb-muse-keryx-into-the-eta-mu-monorepo-sorption.md`.
- Depends on ADR-0001; medium defined by ADR-0003; sequencing in ADR-0004.


---

## Qualification addendum — 2026-10-03

No persisted-envelope retargeting or behavior-preserving extraction is admitted
until a separately reviewed **versioned migration contract** is published and
its compatibility fixtures pass. That contract must define:

- Source/destination envelope versions, field handling, unknown-version refusal,
  and lossless retention of archived original records (no historical rewrite).
- A stable actor-identity-to-stream mapping, collision handling, and behavior
  under actor/node renames; equal IDs in different identity scopes cannot alias.
- Source event identity, causal lineage, stream/revision allocation, and the
  content-derived schema/catalog rules delegated to canonical Clio APIs.
- Read/replay of every admitted old format, with duplicate and reordered input
  cases, delivery/correlation behavior, and explicitly tested public API output.
- A deterministic, reversible migration adapter or explicit rejection of inputs
  it cannot represent. Do not claim unchanged behavior from a field mapping.

ADR-0003 carries the same blocking prerequisite. The Nomos inversion and runtime
work remain future work; no new store, actor, or event kernel is added here.
