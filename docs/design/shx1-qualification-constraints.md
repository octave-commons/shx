# SHX #1 qualification constraints

Status: bounded review correction; architectural proposals remain Proposed.
Source snapshot: octave-commons/shx@f6356e91b69d97160151d5b933a0a4f3bf566189.

The historical synthesis/captures are evidence and hypotheses, not current
ownership, completed research, runtime guarantees, or permission to import,
extract, rename, provision or migrate anything. The reviewed qualification
changes the gate and the authority/safe-use contracts, not those future runtimes.

## Gate evidence

`bash test/analyze_regression.sh` fault-injects an isolated exact copy of the
gate with a closed PATH and successful other analyzers. It covers missing npx,
empty success/failure, a completed summary with failed exit, success without a
completed summary, contradictory positive-clone success, and real-looking zero
clones in default/strict/fix modes. The original f6356e9 script fails 12 of 21
cases; the repair passes all 21. This is failure-semantics evidence, not a mock
claim that the real seven tools passed.

The separate actual `bin/analyze --strict` run completed kondo (0 errors/0 warnings),
Splint (14 files/0 style warnings), cljfmt, clojure-lsp (no unused public vars),
jscpd (0 clones), typed.clojure and Kaocha (19 tests/46 assertions/0 failures).
The existing typed.clojure integer-object-ID deprecation message remains visible;
it is not a failure or a newly fixed runtime issue. Missing/failed analyzers block
all modes; the zero-duplication threshold and the other six checks are unchanged.
The CLI `--strict` spelling is retained for CI/Rheos compatibility.

Official actions/setup-node v4 was resolved through the upstream GitHub tag and
commit APIs on 2026-10-03 to
[49933ea5288caeca8642d1e84afbd3f7d6820020](https://github.com/actions/setup-node/commit/49933ea5288caeca8642d1e84afbd3f7d6820020).
This verification establishes this pin, not an invented existing repo-wide SHA
policy. SHX#3's actual clojure-lsp/Babashka installation repair is retained.

## Proposal and card boundaries

- ADR0001 requires committed clean-clone input, the full gate, a real consumer,
  raw-fallback round-trip and repaired tests/configuration before import.
- ADR0002/0003 require one versioned identity/schema/replay compatibility contract
  and delivery-mode semantics before conversion. Behavior preservation is not
  proven by a field map; historical envelopes cannot be rewritten.
- ADR0004/synthesis require user ratification before changing either board's
  one-package destination. RP003 must specify parameters/representation and
  prove cross-implementation equivalence before standardizing a fold.
- RP001 distinguishes deterministic decisions for identical canonical inputs
  from confluence under arrival changes and idempotence of external effects.
  RP002 requires dynamic/macro reference inventory and compile/tests before a
  behavior-preserving rename claim. Pure registries exclude transport/apply.
- Frozen card bodies remain intact: canonical Rheos comments specify telemetry
  privacy/attribution prerequisites, the proposed assembler fold/exact shim,
  complete migration mapping/validation, and source-preserving archive routing.
  Incoming structural/naming cards received bounded Markdown authoring changes.
  No lifecycle transition, board completion or current transition-gate validation
  is claimed. Unsupported board behavior remains an upstream Rheos gap.

## Source and safe-use authority

All original inbox/captured-note bytes remain in place. Addenda supersede their
unsafe or incomplete reuse claims; INDEX and AGENTS explicitly require reading
these constraints before promoting a capture to instructions/code. A captured
command or fenced algorithm is not an approved runbook or production adapter.

- The unfinished actor vocabulary has complete derived Psephisma/Praxis
  definitions, without inventing an admitted actor-runtime contract.
- Mixed EDN/bb/source evaluation is a future wish. Current source is a string;
  raw uses the existing lang/text map (`src/shx/law/ir.clj`). A new normalization,
  evaluation/bubbling/fallback law and fixtures must precede any extension.
- Git hooks are bypassable, not enforcement. Future fork-tax execution requires
  consent, immutable pending operation/commit/completion evidence, restart
  reconciliation, content-bound hunk IDs and explicit binary/rename/generated
  cases. Interaction metadata is allow-listed, redacted and access/retention
  controlled before collection; raw content collection is disabled by default.
- Unique commit file paths are not conflict-free semantic state. Canonical
  Clio/Rheos authority must expose/resolve conflicts; no local second engine.
- NFS examples are withheld from execution until device ownership/newness/
  unmounted-state and version-specific protocol/security checks are approved.
  root_squash does not provide non-root client authentication. The addendum
  links the authoritative filesystem/NFS manual pages; no device or service is
  altered by this work.
- The Mongo sketch needs stable event/cause identity and both event-ID and
  stream-revision uniqueness plus retry/conflict proof before adapter admission.
- The nbb demo still has a shared temporary file, missing sequence-gap checks,
  and an unguarded check/append race. These are explicitly documented defects;
  operational/concurrent extraction is prohibited pending canonical upstream
  contracts and named regressions. No archived algorithm is falsely claimed
  repaired and no new ledger implementation is built here.
- Wildcard versions and both eta-mu query spellings remain historical examples,
  not actual supported runtime pins/commands. Consumers need fresh upstream
  verification rather than an invented canonical command or version.
- The package-registration typo is corrected in an explicit source-bound
  [candidate successor](../notes/2026-10-03-package-registration-successor.edn).
  It is Proposed and not admitted; it fabricates no engine event/registration.

## Derived archive reader correction

Real Rheos card `a6b112a4-9d6c-4f7d-9bdf-d05bdc690f88` names exact IDs/sites for
four archive accessibility/heading findings. Their reader corrections are now
in [event sourcing](../reference/event-sourcing-reader.md),
[the four-topic reader](../reference/shell-actor-portability-methodology.md)
and [the NFS operations qualification](../ops/nfs-capture-qualification.md).
Decorative logos are omitted and the event-sourcing hierarchy is normalized;
the captured source and existing addenda are byte-identical to the prior head.

Existing `shx-kanban-process-inbox` covers the
[redacted published header](../reference/session-header-redacted.md) and NFS
routing. The raw identifier remains in the source archive/Git history under
the preservation constraint; this is reader redaction, not history deletion.
INDEX explicitly retains the NFS source only as review provenance and directs
readers to `docs/ops/`; an approved owning deployment/destination remains a
prerequisite for any operational successor. No source relocation is claimed.

The generic bug template now covers both shell-config users and IR consumers.
Source-bound naming guidance distinguishes current envm from the unratified
Alethexx assistant proposal in a separate capture. Neither change rewrites
captured statements or ratifies a new product/runtime. The parent owns external
settlement and any subsequent Rheos comment. No engine comments, statuses,
cards or events are changed here. Historical completion report 35 remains
recorded by its existing engine-authored comment before done.

## Original capture provenance

These SHA256 values bind the **original bytes at the source snapshot**, not the
current addended file. Each current file either equals those bytes or starts with
them exactly. The original package record is byte-identical; other source records
have additive qualification text. Original receipts/events remain exact prefixes.

| Original source path | Original bytes | SHA256 |
|---|---|---|
| `docs/inbox/2026.08.10.00.17.16.md` | 368 | `953267afaa6d9fac30064cf150ebbd1cea0ad7b4a323f5fa32b51229e479154f` |
| `docs/inbox/2026.08.09.20.21.57.md` | 1668 | `cd16f3c4154ad5ad17b323c578f5b02df33833a2915776d6b7719ef4aeed2667` |
| `docs/inbox/2026.08.09.22.15.54.md` | 4285 | `251c4cd770e925d1162118c6874d485c8f6d25154c8191237cc9c9baecfb8efb` |
| `docs/inbox/2026.08.09.22.59.57.md` | 3302 | `23226d91dcb8d7066ef83aa149548878cd28c6ae2fc4b2de80ebd35471829086` |
| `docs/notes/2026.08.09.22.59.57.md` | 3293 | `8471fd7baeb0dd51c9c918067d89e226b36f91da24749066705d479256e48d38` |
| `docs/inbox/2026.08.09.23.26.12.md` | 88637 | `48b21892725cf6064a48573641168b959ff5e785e2433ee6153e605172fe9439` |
| `docs/inbox/2026.08.10.00.07.37.md` | 29983 | `cc1ee6d911377f0befd8503ffde4bec2717ac92fabaa83b709af170ad35d5592` |
| `docs/inbox/2026.08.09.23.44.17.md` | 30015 | `5490c2cd5c17e4c720f7a109f5e24efa9894e3a3bde694d687561a1139628d50` |
| `docs/notes/2026.08.10.02.32.07.edn` | 862 | `07ae38b98510a7ee3d34dc61cd9ff631e757c475e71836b4d686f4f45e25b426` |

The per-ID [disposition table](shx1-review-dispositions.md) is settlement
preparation only. Parent owns external replies/requests/quota/merge and must use
the final pushed full SHA, fresh checks and a complete exact-head eligible review.
No SHX#2 review-budget exception is transferred or consumed.
