# Event sourcing — qualified reader

This is a derived reading note, not a ledger implementation or an adopted
Clio contract. It condenses the captured discussion and gives its sections
distinct names and a contiguous heading hierarchy. The complete original,
including its teaching code and later safe-use addendum, remains in the
[source archive](../inbox/2026.08.10.00.07.37.md).

## Signal

The capture distinguishes an authoritative event history, from which state is
reconstructed, from a log describing a separately authoritative mutable store.
Its proposed model is a fold over admitted history. This distinction does not
make every incoming message an event or make every append-only file a complete,
valid history.

## History and projections

The discussion separates commands requesting a change, facts recording an
admitted change, and disposable projections answering a question about history.
Undo in its microblogging example means a later compensating fact, not erasure
or rewriting of the original event. Names and examples in that discussion are
illustrative; admission, identity, ordering and replay rules remain the
canonical Clio implementation's responsibility.

## Non-trivial system: microblogging platform

### Users

The source proposes registration, profile/handle changes, deactivation and
reactivation as facts from which a user view could be reconstructed. These are
example domain operations, not an implemented account model in SHX.

### Posts, relationships, reactions and direct messages

Publishing, editing and retracting posts, following/blocking, reactions and
message histories are separate examples. Their projections do not authorize
access to private messages. A real consumer needs its owning domain's reviewed
authorization and privacy contract before exposing any such view.

## One ledger versus many ledgers

### Single ledger

The capture describes one file as easier to inspect and replay, with a potential
writer bottleneck. A physical file alone supplies neither safe concurrent
admission nor a trustworthy ordering contract.

### Several ledgers

Partitioned storage can represent one logical history only when the canonical
identity, causal dependencies and stream ordering are preserved. Duplicate IDs
alone cannot prove completeness or settle conflicting stream revisions. This
note does not define a second merge or canonicalization algorithm.

## Safe-use prerequisites

The original nbb sketch is withheld from operational adoption: its shared
temporary filename, unchecked missing/gapped stream history and unsynchronized
read/check/append are known defects. No code from that sketch is reproduced
here. Adoption needs the upstream Clio/Rheos contracts and the concurrent-write,
gap/incomplete-history and failure-cleanup regressions named in the source's
qualification addendum and the [SHX constraints](../design/shx1-qualification-constraints.md).

## Provenance

Repository: `octave-commons/shx`.
Original source revision: `f6356e91b69d97160151d5b933a0a4f3bf566189`.
Path: `docs/inbox/2026.08.10.00.07.37.md`.
Original byte count: `29983`.
Original-byte SHA256: `cc1ee6d911377f0befd8503ffde4bec2717ac92fabaa83b709af170ad35d5592`.
Qualified source inspected at `7ef838d0912183b3bafd367419cdfbfeee9670a7`.
The original bytes and its existing qualification addendum are unchanged.

This successor addresses review `4895165651` findings
`caaf9d91a6eb8b9515206495` and `e7e80491581d71779c5cf2ef` under card
`a6b112a4-9d6c-4f7d-9bdf-d05bdc690f88`; it records no board transition.
