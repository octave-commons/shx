# Keryx, Clio, and the Fold — shx × muse × eta-mu synthesis

Status: proposed, 2026-08-10. Supersedes nothing; reconciles two live
contradictions (C1, C2 below) that currently sit unresolved on two different
kanban boards.

This document synthesizes the inbox notes in this repo, the code in this repo,
the code in `../muse`, and the code in `../eta-mu` into one design direction:

- **shx merges into eta-mu** (with muse), reducing repo count and complexity.
- **Keryx** — the pure message-passing core of muse's actor system, the part
  that exists *before* the harness touches data or data touches the harness —
  becomes the fundamental unit of computation.
- **Clio** becomes the medium through which Keryx influence effects change in
  the world.

## 1. Authorities consulted

| Repo | Authoritative for | Key artifacts |
|---|---|---|
| shx (here) | the naming lattice; the hexis thesis; envm fold semantics | `docs/inbox/2026.08.10.00.17.16.md`, `docs/inbox/2026.08.09.23.28.40.md`, `kanban/hexis-unification-epic.md`, `src/shx/domain/merge.clj` |
| muse | the only running actor/message-passing code; the Keryx design dialogue | `src/cljs/eta_mu/actor*.cljc`, `.ημ/plugins/apifany.cljs`, `docs/inbox/2026.07.11.17.19.10.md` (lines 2999–4100) |
| eta-mu | the monorepo destination; the clio kernel; the absorption epics; package conventions | `packages/clio/README.md`, `kanban/epics/absorb-shx-…`, `kanban/epics/absorb-muse-keryx-…`, `ROADMAP.md:28-52` |

## 2. The naming lattice, canonical

From `docs/inbox/2026.08.10.00.17.16.md` (all six statements of the note):

- **Keryx** is the message-passing system.
- **Axxium** is the identity and authentication system.
- **Clio** is the event ledger.
- **Nomos** is an actor's internal protocol for handling messages.
- **Psephisma** is the output of an actor's Nomos in response to a message
  that has consequence outside the actor's immediate memory — "stores".
- **Praxis** (the note's terse final line) — the actor dialogue defines it:
  "the consequences enacted: persisted events, outgoing messages, created
  actors"; "the world-in-motion resulting from determinations: action as
  actually carried out" (`docs/inbox/2026.08.09.23.28.40.md:268-270,370-385`).

Extended by `kanban/name-the-unified-package.md` (status `review`): **Hexis** —
ethos (repeated acts = the append-only ledger) crystallizes into hexis (the
settled disposition = the projection you act from). And by eta-mu
`ROADMAP.md:45`: **Muse** performs the Keryx *role* of importing/assembling/
translating declarations into host shapes. **Rheos** is flow (the kanban FSM).

Build status of each name, today:

| Name | Role | Built? | Where |
|---|---|---|---|
| Clio | event ledger / medium | **Yes**, merged 2026-08-09 | `eta-mu/packages/clio` |
| Axxium | identity/auth | **Yes** (Fastify+PG, JWT, actor read surface) | `eta-mu/packages/axxium` |
| Keryx | message passing / unit of computation | **Half** — runs inside muse, not extracted | `muse/src/cljs/eta_mu/actor*.cljc` |
| Muse | assembly/translation (the Keryx *role* at host boundaries) | **Yes** | `muse` repo (dsl/boundaries) |
| Nomos | actor's internal message-handling protocol | **No code anywhere** | — |
| Psephisma | consequential Nomos output ("stores") | **No code anywhere** | — |
| Praxis | the enactment: Psephisma applied to the world | **No code anywhere** (it is the apply transition — clio's locked append is its first home, `packages/clio/README.md:196-221`) | — |
| Hexis | the projection of habit (naming proposal) | Concept only | shx kanban `review` |
| Rheos | flow / kanban FSM | Yes | `eta-mu/packages/rheos` |

The synthesis gap is precise: the substrate (Clio) and identity (Axxium) exist;
the unit (Keryx) exists but is trapped inside a workspace repo; the actor's
inside (Nomos) and outside (Psephisma) are unbuilt. The user's direction —
"Keryx becomes the fundamental unit of computation, clio the medium" — is
exactly the extraction and grounding of the half-built row.

## 3. C1 — the two Keryxs, resolved

Two definitions of Keryx are in flight:

- **Keryx-A (herald/translation).** Muse's design dialogue
  (`muse/docs/inbox/2026.07.11.17.19.10.md:3016`): "a herald whose function is
  to carry messages between realms without distortion… dispatch and
  translation." Eta-mu `ROADMAP.md:45` assigns this role to Muse: "Keryx names
  that herald/translation responsibility, **not a second competing package
  yet**" (`kanban/tasks/universal-agent-platform-dsl.md:111`, resolved
  2026-07-29).
- **Keryx-B (fundamental unit of computation).** Today's declaration: the
  actor message-passing core, pure domain data, before the harness boundary.

**Resolution: these are the same thing seen from two ends.** A herald *is* a
message carried lawfully; a composition of heralds delivering to lawful
recipients *is* what translation at a host boundary does. Formally:

- The **unit** (Keryx-B) is an *envelope, lawfully delivered*: immutable,
  causally linked, validated at admission. Muse already implements this unit
  as `envelope-schema` (`muse/src/cljs/eta_mu/actor/store.cljc:20-35`) and the
  pure send/recv semantics (`eta_mu/actor/envelope.cljc`,
  `eta_mu/actor/memory.cljc:8-38`).
- The **role** (Keryx-A) is what happens when units cross a host boundary:
  assembly, translation, invocation, delivery, receipt
  (`muse/docs/inbox/2026.07.11.17.19.10.md:3368,3770`). Muse's
  `eta-mu.boundaries.opencode` is one such crossing; shx's bash emitter is
  another.

So `packages/keryx` is **not** a second universal harness compiler and does
not compete with Muse's role: it is the pure unit both sides already share.
The 2026-07-29 resolution ("not a second package yet") stays true — the
package being proposed now is the unit, not the compiler.

## 4. What exists today: the extraction map

Muse's actor system, classified by the user's cut — *pure message passing* vs
*harness/effect side*:

**Keryx-core (extract as `packages/keryx`, pure, `.cljc`):**

- `eta-mu.actor.store` — `envelope-schema`, `IActorStore` protocol
  (`store.cljc:20-49`). The unit's data contract.
- `eta-mu.actor.envelope` — `fill-defaults`, `stamp-route`, `->line`,
  `parse-line` (`envelope.cljc:26-66`). Pure envelope mechanics.
- `eta-mu.actor.memory`'s shared pure fns — `recv-view`, `spawn-meta`,
  `routed-envelope` (`memory.cljc:8-38`). The read semantics all backends
  reuse.

**Proto-Nomos (extract only with the Nomos refactor, not as a move):**

- `eta-mu.actor.muse` — Muse/Phase domain logic: `influence!`, `tail`,
  `observations`, `conclusions`, `evidence` (`muse.cljc:17-111`). **Not pure**
  as written: it requires `eta-mu.actor` and `promesa` (`muse.cljc:10-11`)
  and `influence!` calls `actor/tell!` — a ledger append through the store
  (`muse.cljc:69-74`). "She is not an orchestrator" (`muse.cljc:9`) is an
  actor-semantics property (she cannot command phases), not a purity
  property. Extraction requires inverting the store dependency — history in,
  Psephisma out — which *is* the Nomos refactor (§7).

**Stays harness-side (muse/boundaries or future clio adapters):**

- `eta-mu.actor.backend` — env-driven store selection, polling.
- `eta-mu.boundaries.node.ledger` / `mongo.ledger` — effectful stores.
- `eta-mu.boundaries.opencode` — the only namespace allowed to touch JS
  (`boundaries/opencode.cljs:2-9`).
- `eta-mu.daemon.core`, `eta-mu.dsl.*` pipeline, `.ημ/plugins/*` — the harness
  products.

The cut already exists as discipline; the extraction makes it physical. Note
the envelope's own comment: it *mirrors* `@promethean-os/event-ledger`
(`actor/envelope.cljc:2-4`) — and eta-mu's absorb-katamorph epic records that
"Clio now supersedes `event-ledger`" (`:37`). The envelope's authority is
already dead; Clio is its replacement.

## 5. Clio as the medium

"Clio becomes the medium in which Keryx influence effects change" maps
field-by-field:

| Muse envelope (today, `store.cljc:20-35`) | Clio event (target) |
|---|---|
| `:event/id`, `:event/time` | `:event/id`, `:event/at` |
| `:event/from` actor descriptor | `:event/actor` |
| `:event/to` actor descriptor | implied by `:event/stream` (the recipient's mailbox *is* the stream); the descriptor survives in `:event/data` only when cross-stream routing needs it. Note the correction from a first-pass mapping: clio's `:event/subject` is the domain object the event concerns (`"post:p1"`, `README.md:54`), **not** the recipient — the recipient is already the stream. |
| `:causal/root`, `:causal/parent` | `:event/causes` (vector — generalizes the pair) |
| `:event/type` string | `:event/type` keyword + content-derived `:event/schema` |
| mailbox per actor (file/mongo) | `:event/stream` per actor + `:event/seq` |
| `:delivery/mode` — full enum `"tell" "ask" "stream" "ack-required"` (`store.cljc:31`) | payload-level `:event/data` (delivery is domain, not storage). The `"stream"` and `"ack-required"` modes are unexamined semantics — flagged to RP-001. |
| `:delivery/id` | `:event/data` correlation id (ask/ack pairing) |
| `:session/id`, `:turn/id` | `:event/data` correlation metadata; optionally a stream-naming convention for session-scoped mailboxes |
| `:payload`, `:contracts`, `:expectations` | `:event/data` (closed Malli per type) |

Consequences:

1. **A mailbox is a Clio stream.** `recv` = read stream from seq N;
   `watch-once` = poll/observe stream head. Append-only discipline is Clio
   law 1; "clear mailbox" stays an append marker (already the mongo store's
   behavior, `mongo/ledger.cljs:164-169`).
2. **Causal threading becomes real.** `:causal/root` chains today are
   convention; as `:event/causes` they are validated by Clio law 3 and become
   queryable DAG structure — the fork-tax causal-graph vision of
   `docs/inbox/2026.08.09.22.15.54.md:59`.
3. **Mongo becomes a Clio adapter, not a parallel kernel** — already carded:
   `eta-mu/kanban/tasks/reconcile-muse-s-mongo-ledger-boundary-against-clio--adapter.md`.
4. **Physical layout stops mattering** (Clio law 6): per-actor files, per-commit
   segments (`.ημ/data/rheos/<commit-hash>`, `docs/inbox/2026.08.09.22.59.57.md:26`),
   and Mongo partitions all canonicalize identically. This resolves the PR#181
   ledger-conflict problem *by construction*.
5. **Every ledger surface converges.** rheos, receipt-river, session-mycology,
   sol sessions all have migrate-to-clio epics carded 2026-08-10; shx's own
   `kanban/migrate-ledgers-to-clio.md` maps the six `~/.ημ/resources/` ledgers.
   One medium, one canonicalization, one projection law (Clio law 7).

## 6. The Hexis thesis is Clio law 7

`kanban/hexis-unification-epic.md`: ".bashrc is a projection of habit. A
system prompt is a projection of habit. Same fold, different emitter."

Clio law 7: "A projection is a pure fold over the canonical event order and
can be deleted and reconstructed."

These are the same sentence. The hexis-assembler card already says "Same fold
as `shx.domain.merge`" (group by `:id`, order by `:ts`, last wins). The deep
identity the whole synthesis rests on:

> **Config rendering is event-sourced projection.** envm's
> `fold-fragment-tree`, muse's opencode settings deep-merge, and a Clio
> projection are one operation at three altitudes. Hexis names the operation;
> Keryx carries the influence; Clio remembers it; the emitters (bash, bb,
> opencode plugin, MCP, GitHub Actions) are pure `shape/` morphisms off the
> folded value.

This is why "shx merged with muse becomes an eta-mu package" reduces
complexity rather than merely moving code: two fold implementations and two
emitter families become one fold + N emitters, and the `shx.shape.bb`
placeholder problem (`shx.core` does not exist, `src/shx/shape/bb.clj`) gets
its answer: the bb runtime *is* a Keryx actor applying Psephismata to shell
state.

## 7. Nomos and Psephisma: the unbuilt half

The lattice's missing rows have a precise shape waiting for them, already
sketched in `docs/inbox/2026.08.09.23.28.40.md:161,174-181,379`:

```text
Keryx + actor's Clio history → (Nomos) → Psephisma {:events :send :spawn :reply}
```

- **Nomos** = a pure function `(history, keryx) -> decision`. muse's
  `eta-mu.actor.muse` is the proto: a Muse reads phase ledgers (history),
  receives influence (keryx), and decides observations/conclusions — "she is
  not an orchestrator" (`muse.cljc:9`). Nomos generalizes her per-actor.
- **Psephisma** = the decision's consequential part, expressed only as data:
  new Clio events, new Keryx envelopes, new Axxium identities. Never a direct
  effect.
- **Praxis** = the enactment: effects happen when infra applies Psephisma to
  the world — which is exactly Clio's append path with its fcntl lock
  discipline (`packages/clio/README.md:196-221`). The lattice's sixth name is
  the apply transition itself.

This gives the system its computational closure: **the only way anything
changes is a Keryx delivered into a Clio stream, folded by a Nomos, issuing
Psephisma, enacted as Praxis — itself only more Keryx and more Clio.**
"Keryx the fundamental unit, Clio the medium" is not metaphor; it is the
proposed reduction of every effect in the system to two data types.

## 8. C2 — Hexis (one package) vs the absorption epics (two packages), sequenced

- shx board, `hexis-unification-epic` (P1, 13sp): **one** package unifying shx
  + muse, named Hexis. DoD: "emits both shell environment text and agent
  instruction files from one resource fold."
- eta-mu board, `absorb-shx` (P3, 8sp) + `absorb-muse-keryx` (P2, 13sp): **two**
  packages, `packages/shx` and `packages/muse`, imported with history,
  shx first as "the one that proves the import procedure."

**Resolution: sequence, don't choose.** The contradiction is about *timing*,
not destination:

1. **Phase 0 — absorb separately** (eta-mu plan). shx lands as `packages/shx`
   (pathfinder, proves the import procedure; requires the `port-shx-to-cljc`
   work, which is the same work either board demands). Muse lands as
   `packages/muse` with its Mongo boundary reconciled to a Clio adapter.
2. **Phase 1 — extract `packages/keryx`** from muse's pure actor core (§4),
   retargeted onto Clio's envelope (§5). This is new relative to both boards
   and is the concrete form of today's direction.
3. **Phase 2 — converge the fold.** Only after both folds live in one repo can
   "same fold, different emitter" be implemented rather than asserted. Hexis
   then names the *shared projection layer* (one `domain.merge`-style fold +
   emitter registry), consumed by `packages/shx`, `packages/muse`, and rheos
   alike — a library, not a product merger. The naming decision
   (`name-the-unified-package`, in `review`) stays open until Phase 2; nothing
   gets renamed in Phases 0–1.

Rationale: the absorption epics encode the lower-risk path (proven import,
clean-clone gates, first-consumer requirements). The hexis epic encodes the
true destination (one fold). Sequencing preserves both; choosing either
outright loses one.

## 9. Meaningful connections (note → code → card)

| Inbox note (here) | Live code | Carded work |
|---|---|---|
| Lattice `2026.08.10.00.17.16` | `packages/{clio,axxium}`, muse `actor/*` | this doc §7; keryx extraction (new) |
| Actor dialogue `2026.08.09.23.28.40` | `eta_mu/actor/muse.cljc` (proto-Nomos) | Nomos/Psephisma spec (new, RP-001) |
| Event-sourcing axioms `2026.08.10.00.07.37` | `packages/clio` (7 laws) | already satisfied — compare and close |
| Per-commit ledgers `2026.08.09.22.59.57:26` | `kanban/.events/ledger.edn` | `migrate-rheos-to-clio-commit-bound-ledger-segments` (eta-mu) |
| Harness-prompt composition `2026.08.09.22.15.54:11-13` | muse dsl/boundaries; shx `CLAUDE.md` shim | `hexis-assembler` (here) |
| Portable Clojure `2026.08.09.23.44.17` | clio `.cljc`/bb/nbb/shadow template | `port-shx-to-cljc` (here) = absorb-shx prerequisite (eta-mu) |
| Tiered-storage → ECS `2026.08.09.23.26.12:2100` | clio streams; axxium entities | `ascribe-actor-ownership…` (eta-mu) |
| clio ledger design `2026.08.09.22.15.54:59-62` | clio `:event/causes` | fork-tax causal graphs (deferred) |
| envm origin `2026.08.09.20.21.57` | `shx.domain.merge/fold-fragment-tree` | hexis epic §6 above |
| closh/ancestors `2026.08.09.22.19.20:259` | — | eta-mu/Sintel lineage, naming ledger |
| local-GitHub `docs/notes/2026.08.10.02.10.03` | `bin/analyze` + rheos `:build-gate` | `agent-operating-standard` (eta-mu) |

Cross-repo duplicates to dedupe during absorption: `migrate-ledgers-to-clio`
(here) vs the four eta-mu clio-migration epics; `port-shx-to-cljc` (here) vs
`inventory-shx-and-pick-its-first-in-repo-consumer` (eta-mu); the tool-call →
file-change telemetry hook (here, `tool-call-file-change-hook`) vs apifany's
deferred mailbox-policies (muse).

## 10. Terminology fork to fix on import

Muse implemented the purity boundary as `boundaries.*` namespaces; eta-mu's
constitution and clio use `extern.*` (`packages/clio/README.md:143-167`;
muse's own AGENTS.md says `extern` while its code says `boundaries`). On
absorption, standardize on `extern` (constitution wins; clio's edamame
boundary lint already enforces it). One mechanical law across all three repos:
`law → shape → extern → domain → infra`, kondo-enforced
(`@open-hax/kondo-config` `hooks.layer-boundaries`).

## 11. Risks and open questions

- **npm name collision**: `shx@^0.4.0` (ShellJS) is a real dependency in
  eta-mu's **root** `package.json:27` (also referenced in legacy package
  lockfiles). `packages/shx` needs the scoped name `@eta-mu/shx` everywhere;
  the epic's acceptance criteria don't mention it. Open sub-question: audit
  whether anything still imports the npm `shx` — if not, the collision can be
  deleted rather than scoped around.
- **Residual Keryx naming collision**: post-extraction, `packages/keryx` (the
  unit) coexists with eta-mu `ROADMAP.md:45`'s role label "muse (Keryx)". §3
  argues they are one thing seen from two ends, but no in-repo artifact
  records the disambiguation yet — the extraction epic should add one line to
  the ROADMAP ownership table splitting "muse (herald role)" from "keryx
  (unit)".
- **Axxium ↔ Keryx identity reconciliation**: §7 says Psephisma may carry new
  Axxium identities, but how keryx actor descriptors
  (`{:actor-id :actor-kind :actor-node}`, `store.cljc:14-18`) reconcile with
  axxium's entity/actor read surface and katamorph's `ActorContract`
  (`ROADMAP.md:167-171`) is unaddressed. The lattice ties them; this plan
  does not — card it during the extraction epic.
- **Nomos/Psephisma build path**: only RP-001 (research) exists. The path is
  research → spec → epic; the Nomos refactor is also the gate for extracting
  `eta-mu.actor.muse` (§4), so the keryx epic should carry a Nomos-spec
  prerequisite.
- **typed.clojure**: no eta-mu package uses it; shx's gate does. Keep it as a
  shx-local check on absorption, or drop? (Recommendation: keep — it is the
  only checked pure logic in the constellation, and `shx.shape.quote` proves
  the pattern.)
- **Nomos hosting**: does a Nomos run inside sol/turn-processor, inside an
  OpenCode plugin, or as a bb/nbb process? Deferred to RP-001.
- **muse drift**: `test/js/eta_mu_cli_test_stub.cjs` referenced but missing;
  `.opencode/opencode.json` stale. Fix during import, not before.
- **Uncommitted state everywhere**: shx's entire 2026-08-10 layer, eta-mu's
  ~40 new cards, muse's package.json dirt. The absorptions import *history*;
  uncommitted work has none. Commit first (see Next).

## 12. Non-goals

- No IR extension, no new shells (per absorb-shx non-goals).
- No contract-vocabulary changes (Katamorph owns it, per absorb-muse
  non-goals).
- No renaming of products before Phase 2 (Hexis stays a proposal).
- No keryx.runtime/keryx.core/keryx.util — the anti-runtime stance of the
  original design dialogue holds for the extraction.

## Next

Commit the uncommitted planning tranches in all three repos (shx's 2026-08-10
layer; eta-mu's ~40 cards including both absorption epics; muse's package
dirt), then append the keryx-extraction epic to the eta-mu board as a child of
`absorb-muse-keryx`, referencing this document.
