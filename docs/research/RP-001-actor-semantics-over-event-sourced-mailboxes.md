# RP-001: Actor semantics over event-sourced mailboxes

Status: Proposed
Date: 2026-08-10

## Problem

Muse runs an actor model whose mailboxes are append-only ledgers: sending is
an append to the recipient's mailbox (`IActorStore/-send!`,
muse/src/cljs/eta_mu/actor/store.cljc:41-49), receiving is a stream read
(`-recv`), and actors have no processes — they pull. The OpenCode plugin that
hosts them names the pattern directly: "The humble actor model, hiding in
plain sight" (muse/.ημ/plugins/apifany.cljs:5). The synthesis retargets this
onto Clio: "a mailbox is a Clio stream; `recv` = read stream from seq N"
(docs/design/2026-08-10-keryx-clio-hexis-synthesis.md §5). What is missing is
the formal semantics. No delivery guarantee is specified anywhere; `tell!` and
`ask!` are both ledger appends, and `ask!`'s own docstring concedes "the
recipient answers on their own time" (muse/src/cljs/eta_mu/actor.cljc:91-99).

The questions that must be answered before Keryx is extracted:

1. **Delivery semantics.** When send is a ledger append under Clio law 1
   (immutability) and law 2 (id dedupe), what do at-most-once, at-least-once,
   and exactly-once mean? The nbb ledger prototype already shows append retry
   returning `:already-present` (docs/inbox/2026.08.10.00.07.37.md:482-506);
   is dedupe-at-append sufficient for exactly-once *delivery*, or only for
   exactly-once *storage*?
2. **Idempotent handlers under replay.** A Nomos is a pure function
   `(history, keryx) -> {:events :send :spawn :reply}`
   (docs/inbox/2026.08.09.23.28.40.md:174-181; synthesis §7). Purity gives
   the same decision for the same canonical history and Keryx. Replay safety
   additionally needs defined ordering/deduplication and an idempotent effect
   application contract. Where do those guarantees live?
3. **Causal ordering vs stream ordering.** Clio law 4 gives stream slots;
   law 3 gives the causal DAG. The README warns explicitly that the
   `[stream,seq,id]` replay tie-break "is not a causal claim"
   (eta-mu/packages/clio/README.md:34-37). Muse envelopes carry
   `:causal/root` / `:causal/parent` by convention, unvalidated
   (store.cljc:27-28). Which order does a Nomos see, and may it depend on the
   tie-break?
4. **Ask without rendezvous.** What is the type of `ask!` when there is no
   blocking receive — a correlated pair of events, a future resolved by
   projection, or a protocol message the caller's own Nomos must fold?
5. **Supervision without processes.** Who notices a dead Nomos? Today nothing
   does: `influence!` is "guidance lands only if the phase chooses to look"
   (the namespace docstring, muse/src/cljs/eta_mu/actor/muse.cljc:7; the append itself is `influence!`, :69-74). Supervision must be
   re-expressed as staleness projections over streams, not process monitors.

## Why now

The lattice has named the unit and the medium but not their interaction laws:
Clio is built (eta-mu/packages/clio), Keryx is half-built and trapped in muse
(synthesis §2, build table). The synthesis defers Nomos hosting to this
proposal (§11: "sol/turn-processor, OpenCode plugin, or bb/nbb process") and
the extraction epic is about to be carded against §4's cut. Writing semantics
after extraction would ossify whatever muse happens to do today — including
its known ambiguities (`-clear!` as mailbox destruction vs the mongo store's
append-marker behavior, synthesis §5 consequence 1).

## Prior art to survey

- Hewitt, Bishop & Steiger 1973, "A Universal Modular ACTOR Formalism for
  Artificial Intelligence" — the original model.
- Agha 1986, *Actors: A Model of Concurrent Computation in Distributed
  Systems* — the standard operational semantics; our pull-model diverges from
  its asynchronous-buffer assumption and the divergence must be characterized.
- Armstrong 2003, "Making reliable distributed systems in the presence of
  software errors" (PhD thesis) — supervision trees; we keep the intent,
  replace the mechanism.
- Fowler 2005, "Event Sourcing" (martinfowler.com) — the pattern baseline.
- Helland 2007, "Life beyond Distributed Transactions: an Apostate's
  Opinion" — entities-with-disjoint-data as the scalability unit; a mailbox
  stream is such an entity.
- Shapiro et al. 2011, "Conflict-free Replicated Data Types" — concurrent
  mailbox merge when two partitions accept appends to the same stream.
- The transactional outbox pattern (widely documented; no single canonical
  paper) — Psephisma application is an outbox drain.

## Open questions

- Is exactly-once Nomos *execution* even the right goal, or is the honest
  guarantee "idempotent Psephisma application with dedupe keys derived from
  (keryx id, nomos version)"?
- Does `:event/causes` validation (law 3) subsume muse's `:causal/root` chain,
  and does anything still need the root once the DAG is queryable?
- Can supervision-staleness be a Clio projection (law 7), or does detection
  require a wall clock and therefore an infra-side oracle?
- What is the delivery-guarantee vocabulary — do we adopt the
  at-most/at-least/exactly-once trichotomy knowing it misleads, or define
  storage/delivery/effect tiers separately?

## Proposed method

1. Write a small-step operational semantics: states are `(streams, registry,
   nomos-table)`; transitions are `append`, `pull(actor, seq)`, `decide`
   (Nomos fold → Psephisma), and `apply` (Psephisma → infra effects). Prove
   deterministic `decide` for identical canonical ordered/deduplicated input.
   Purity alone does not prove confluence under reordered or duplicated delivery,
   or idempotence of effects. Specify and test those guarantees separately.
2. Define delivery tiers as predicates over traces; classify `tell!`, `ask!`,
   and `influence!` against them; pick the vocabulary.
3. Enumerate Nomos host options (sol/turn-processor; OpenCode plugin; bb/nbb
   process) and score each against the semantics: lock participation
   (clio README:196-221), pull cadence, crash boundary.
4. Derive property tests from the semantics: partition-invariance (the clio
   1,944-layout fixture, README:313-321) extended with actor traces; idempotent
   replay; tie-break determinism without causal leakage.

## Deliverables

- A formal model (transition system with stated invariants), in EDN-adjacent
  notation, checked into docs/research/.
- A decision record on delivery-guarantee vocabulary.
- The Nomos host-execution options analysis answering synthesis §11.
- A property-test suite specification mapped to clio's existing fixture style.

## Fit with the lattice

This is the semantics half of synthesis §7: Nomos is the pure decision
function, Psephisma its consequential output as data, Keryx the unit, Clio
the medium. RP-002 constrains where the effectful `apply` transition may
live (extern/infra only). RP-003 supplies the fold algebra the `decide`
transition's history argument is built from.


---

## Qualification addendum — 2026-10-03

Replay guarantees are conditional on identical canonical ordered/deduplicated
inputs. Specify event/delivery IDs, per-stream ordering and conflict handling,
handler version, and effect dedupe keys separately. Include duplicate delivery,
reordered arrival, restart after decision/before effect, and repeated application
trace cases before claiming confluence or idempotent effects. This is a research
prerequisite, not a runtime or a completed proof.
