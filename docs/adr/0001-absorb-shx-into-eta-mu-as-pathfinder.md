# ADR-0001: Absorb shx into eta-mu as `packages/shx`, the pathfinder absorption

Status: Proposed
Date: 2026-08-10

## Context

shx is a standalone JVM-Clojure repo (`/home/err/spaces/shx`) holding two
products, the shell-intent IR and `envm`, on a seven-check static gate
(`bin/analyze`; `docs/static-analysis.md:18-28`). Two boards already card its
move:

- eta-mu, `absorb-shx-into-the-eta-mu-monorepo-sorption` (P3, 8sp): shx is
  "the smallest and least entangled of the absorptions, which makes it the
  one that proves the import procedure before Muse and Katamorph rely on it"
  (`/home/err/spaces/eta-mu/kanban/epics/absorb-shx-into-the-eta-mu-monorepo-sorption.md:23-24`).
  Its acceptance criteria require clean-clone gates, one real in-repo call
  site, and a round-trip fallback test (`:42-46`).
- shx, `port-shx-to-cljc` (P1, 8sp) — listed as the hard blocker of the
  hexis epic (`/home/err/spaces/shx/kanban/hexis-unification-epic.md:35`) —
  is the same work viewed from the shx board: `law/`, `shape/`, `domain/`
  become runtime-neutral `.cljc`, JVM-bound `infra/` moves behind `extern`
  (`/home/err/spaces/shx/kanban/port-shx-to-cljc.md:18-29`).

shx's load-bearing guarantee — translation can never do worse than the
original, via the `:raw` escape hatch — "is unreachable across a repo
boundary" (absorb-shx epic `:27-28`). That guarantee only becomes a
repo-wide property if the IR ships as a library inside the monorepo.

Three concrete obstacles, all recorded in the synthesis
(`/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md:281-287`):

1. **npm name collision.** `shx@0.4.0` (ShellJS) is a real dependency in
   eta-mu's legacy packages. The unscoped name is taken.
2. **typed.clojure.** No eta-mu package uses it; shx's gate does
   (`docs/static-analysis.md:27`). `shx.shape.quote` is the only
   typechecked pure logic in the constellation.
3. **Runtime split.** shx is JVM-only; eta-mu packages run bb + nbb +
   shadow-cljs on the clio template
   (`/home/err/spaces/eta-mu/packages/clio/README.md:143-181`).

## Decision

Absorb shx into eta-mu as `packages/shx`, npm name `@eta-mu/shx`, with git
history preserved, as the pathfinder absorption that proves the import
procedure before muse and katamorph depend on it.

Concretely:

- Port `shx.law.*`, `shx.shape.*`, `shx.domain.*` to `.cljc`; push
  `shx.infra.*` (JVM-bound: `infra/config.clj`, `infra/fs.clj`) behind an
  `extern` boundary, following the clio construction order
  `law -> shape -> extern -> domain -> infra`
  (`packages/clio/README.md:145-149`). This is exactly the
  `port-shx-to-cljc` card; both boards' work is one task.
- Satisfy the monorepo package conventions per the clio template:
  register a suite in `scripts/test.bb:26`, add the package to
  `kondo-packages` in `scripts/lint.bb:26`, wire `scripts/ci-gates.bb`,
  and add `packages/shx/**` path filters to
  `.github/workflows/main-pr-gate.yml` (clio job at `:59-86` is the model).
- Publish only as `@eta-mu/shx`; never claim the unscoped `shx` name.
- **Keep typed.clojure as a shx-local gate.** It is the only checked pure
  logic in the constellation and `shx.shape.quote` proves the pattern
  (synthesis `:284-287`). Do not impose it on other packages; do not drop
  it because no sibling runs it.
- Preserve the seven-check `bin/analyze` gate as the package suite until
  the monorepo gates demonstrably subsume it; the zero-findings contract
  (`docs/static-analysis.md:37-40`) moves with the code.

## Consequences

- The `:raw` fallback guarantee becomes available to every in-repo agent
  tool that shells out, with a test enforcing it on monorepo CI.
- The import procedure (history preservation, clean-clone gates,
  first-consumer requirement) is exercised and debugged on the smallest
  package before muse (13sp) and katamorph attempt it.
- `shx.core` — the unimplemented runtime API that `shx.shape.bb` emits
  calls into (`AGENTS.md`) — stays unimplemented; absorption does not
  silently acquire a runnable bb product. Its resolution is deferred to
  the keryx/hexis phases (synthesis `:185-188`).
- Two boards close with one implementation: `port-shx-to-cljc` here and
  the absorption epic there.
- The scoped npm name is permanent; renaming later costs a deprecation
  cycle.

## Alternatives considered

- **Hexis-first single-package merge** (the shx board's
  `hexis-unification-epic.md:42-43` DoD: one package emitting both shell
  text and agent instructions). Rejected for Phase 0: the import procedure
  is unproven, and merging two products before either has landed couples
  two failure modes. The synthesis sequences instead of choosing
  (synthesis `:215-245`); see ADR-0004.
- **Keep shx standalone.** Rejected: the translation-fidelity guarantee is
  unreachable across a repo boundary (absorb-shx epic `:27-28`), and the
  fold/emitter duplication with muse (synthesis `:183-188`) can never be
  deduplicated.

## Connections

- Synthesis: `/home/err/spaces/shx/docs/design/2026-08-10-keryx-clio-hexis-synthesis.md` §1, §8, §11.
- Epics: `/home/err/spaces/eta-mu/kanban/epics/absorb-shx-into-the-eta-mu-monorepo-sorption.md`;
  `/home/err/spaces/shx/kanban/hexis-unification-epic.md`;
  `/home/err/spaces/shx/kanban/port-shx-to-cljc.md`.
- Template: `/home/err/spaces/eta-mu/packages/clio/README.md:143-181`;
  `scripts/test.bb:26`, `scripts/lint.bb:26`,
  `.github/workflows/main-pr-gate.yml:59-86` in eta-mu.
- Followed by ADR-0002 (keryx extraction), ADR-0003 (clio as medium),
  ADR-0004 (sequencing to the fold).


---

## Qualification addendum — 2026-10-03

This is a proposed, historical import plan, not authority to perform an
absorption or close its cards. Import readiness is one blocking contract:

1. Use committed source and a clean clone of the exact proposed import revision;
   record both source and destination commits and retained history.
2. Run all seven `bin/analyze --strict` checks (or a separately proved equivalent
   destination gate), including missing-tool failure paths. `bb check` stays the
   source gate; a partial run is not readiness.
3. Prove a real destination consumer calls the imported library successfully.
4. Prove the `:raw` fallback round-trip at that boundary; retain input/output
   fixtures and behavior, not just a successful compile.
5. Missing tests/configuration, stale references, unavailable dependencies, and
   uncommitted required inputs are pre-import blockers. Each must be checked on
   the fresh candidate, not deferred until after the import.

None of these import proofs has been supplied by this documentation PR. See
[qualification constraints](../design/shx1-qualification-constraints.md).
