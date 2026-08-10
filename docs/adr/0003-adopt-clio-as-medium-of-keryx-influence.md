# ADR-0003: Adopt Clio as the medium of Keryx influence

Status: Proposed
Date: 2026-08-10

## Context

The second half of the 2026-08-10 declaration: **Clio is the medium in
which Keryx influence effects change.** Muse's envelope was written to
mirror `@promethean-os/event-ledger`
(`/home/err/spaces/muse/src/cljs/eta_mu/actor/envelope.cljc:2-4`), and
that authority is dead — the absorb-katamorph epic records that "Clio now
supersedes `event-ledger`"
(`/home/err/spaces/eta-mu/kanban/epics/absorb-katamorph-into-the-eta-mu-monorepo-sorption.md:37`;
synthesis `:123-126`). The extracted keryx core (ADR-0002) therefore needs
its storage semantics re-grounded, and Clio's seven laws
(`/home/err/spaces/eta-mu/packages/clio/README.md:11-24`) already state
them.

The field mapping is given by the synthesis §5
(`/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md:133-141`):

| Muse envelope (today, `store.cljc:20-35`) | Clio event (target) |
|---|---|
| `:event/id`, `:event/time` | `:event/id`, `:event/at` |
| `:event/from` actor descriptor | `:event/actor` |
| `:event/to` actor descriptor | implied by `:event/stream` — the recipient's mailbox *is* the stream; the descriptor survives in `:event/data` only when cross-stream routing needs it. Clio's `:event/subject` is the domain object the event concerns (`README.md:54`), **not** the recipient. |
| `:causal/root`, `:causal/parent` | `:event/causes` (vector; generalizes the pair) |
| `:event/type` string | keyword + content-derived `:event/schema` |
| mailbox per actor | `:event/stream` per actor + `:event/seq` |
| `:delivery/mode` — full enum `"tell" "ask" "stream" "ack-required"` (`store.cljc:31`) | demoted into payload-level `:event/data`. `"stream"`/`"ack-required"` semantics are unexamined; RP-001 owns the delivery-guarantee vocabulary. |
| `:delivery/id` | `:event/data` correlation id (ask/ack pairing) |
| `:session/id`, `:turn/id` | `:event/data` correlation metadata; optionally a stream-naming convention for session-scoped mailboxes |
| `:payload`, `:contracts`, `:expectations` | `:event/data` (closed Malli per type) |

Each Clio law maps onto an actor semantic the system already wants:

- **Law 1** (events immutable, `README.md:11`): the append-only mailbox.
- **Law 3** (`:event/causes` validated, `:14-16`): causal threading stops
  being convention and becomes validated, queryable DAG structure.
- **Law 4** (stream-slot conflicts, `:17-18`): one mailbox slot claimed
  twice is a detected concurrent-write conflict.
- **Law 6** (physical layout has no authority, `:22`): per-actor files,
  per-commit segments (`.ημ/data/rheos/<commit-hash>`,
  `/home/err/spaces/shx/docs/inbox/2026.08.09.22.59.57.md:26`), and Mongo
  partitions canonicalize identically — resolving the PR#181
  ledger-conflict problem *by construction* (synthesis `:155-158`), the
  premise of `/home/err/spaces/eta-mu/kanban/epics/migrate-rheos-to-clio-commit-bound-ledger-segments-igration.md`.

The Mongo side is already carded:
`reconcile-muse-s-mongo-ledger-boundary-against-clio--adapter`
(`/home/err/spaces/eta-mu/kanban/tasks/reconcile-muse-s-mongo-ledger-boundary-against-clio--adapter.md`)
— "Mongo becomes a Clio storage adapter, not a parallel event system."

## Decision

All Keryx delivery is mediated by Clio.

- A mailbox **is** a Clio stream: `:event/stream` per actor, `:event/seq`
  for order. `recv` = read from seq N; `watch-once` = observe stream head
  (synthesis `:145-146`).
- Envelopes retarget onto Clio's envelope per the mapping table above.
  `:delivery/mode` is demoted to payload-level domain data — delivery is
  domain, not storage (synthesis `:140`).
- Causal linkage moves from `:causal/root`+`:causal/parent` convention to
  `:event/causes`, validated under Clio law 3.
- Mongo becomes a **Clio storage adapter**, not a parallel kernel, per the
  reconcile task. The file ledger stays the stable prototyping surface;
  Mongo is the opt-in remote (`absorb-muse-keryx` epic `:30-32`).
- The `@promethean-os/event-ledger` dependency is retired; Clio supersedes
  it (absorb-katamorph epic `:37`).

## Consequences

- `clear!` remains an append marker, never a delete — already the mongo
  store's behavior: `-clear!` appends a `mailbox.cleared` envelope and
  reads resume after it
  (`/home/err/spaces/muse/src/cljs/eta_mu/boundaries/mongo/ledger.cljs:164-169`;
  synthesis `:147-148`). Clio law 1 makes this the only lawful implementation.
- keryx's `IActorStore` backends reduce to Clio adapters; store selection
  (`eta-mu.actor.backend`) becomes adapter configuration.
- Schema revisions of envelope types version themselves via Clio's
  content-derived `:event/schema` (`README.md:66-131`); no hand-maintained
  version integer.
- Write serialization differs per adapter: fcntl inode locks on files
  (`README.md:196-214`); the Mongo guarantee must be stated in writing and
  tested per the reconcile task's acceptance criteria.
- Fork-tax causal graphs (`docs/inbox/2026.08.09.22.15.54.md:59`) become
  possible because `:event/causes` is real structure, not convention.

## Alternatives considered

- **Keep `@promethean-os/event-ledger` as muse's kernel.** Rejected: it is
  superseded (absorb-katamorph epic `:37`); two event models in one
  monorepo is the divergence the reconcile task exists to prevent.
- **Per-actor custom storage semantics (keep mailbox files as-is).**
  Rejected: loses law 3 validation and law 6 partition invariance; the
  PR#181 conflict problem remains unsolved
  (`docs/inbox/2026.08.09.22.59.57.md:26`).
- **Model delivery mode in storage (separate streams for tell/ask).**
  Rejected: delivery is a domain concern; baking it into storage
  duplicates streams and breaks the single canonical order that law 7
  projections fold over.

## Connections

- Synthesis: `/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md` §5.
- Laws: `/home/err/spaces/eta-mu/packages/clio/README.md:11-24`; envelope shape `:43-57`.
- Cards: `/home/err/spaces/eta-mu/kanban/tasks/reconcile-muse-s-mongo-ledger-boundary-against-clio--adapter.md`;
  `/home/err/spaces/eta-mu/kanban/epics/migrate-rheos-to-clio-commit-bound-ledger-segments-igration.md`;
  `/home/err/spaces/eta-mu/kanban/epics/absorb-katamorph-into-the-eta-mu-monorepo-sorption.md:37`.
- Existing behavior: `/home/err/spaces/muse/src/cljs/eta_mu/boundaries/mongo/ledger.cljs:164-169`.
- Depends on ADR-0002; enables the Phase 2 fold in ADR-0004.
