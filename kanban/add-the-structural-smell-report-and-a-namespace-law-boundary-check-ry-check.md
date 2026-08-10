---
category: "kanban"
labels: "quality, static-analysis, tooling"
type: "task"
write-id: "1786347485381-0.3c8i48ijrlxao5dpim7"
points: "5"
title: "Add the structural smell report and a namespace-law boundary check"
priority: "P2"
status: "incoming"
uuid: "structural-smells-and-boundary-check"
created_at: "2026-08-10T07:35:46.450Z"
---

# Add the structural smell report and a namespace-law boundary check

## Outcome

`bin/analyze` reports structural decay (god namespaces, mega-functions, parameter
bloat, fan-out) and refuses a violation of the four-quadrant namespace law, so the
two rules `AGENTS.md` states most emphatically stop being enforced by review
alone.

## Context

The gate landed on 2026-08-10 with seven checks, all at zero, all blocking:
clj-kondo, splint, cljfmt, clojure-lsp dead code, jscpd, typed.clojure, kaocha.
Two checks that `../Truth` runs were deliberately left out at the time, recorded
in `docs/static-analysis.md` › "What is not here yet":

- **Structural smells** — `../Truth/dev/smell_report.clj` (301 lines) reads
  clj-kondo's EDN analysis export from stdin and scores god namespaces,
  mega-functions, parameter bloat, and fan-out, with `--strict` promoting HARD
  breaches to a failure.
- **Layer boundaries** — `AGENTS.md` › Namespace law says `domain/` is pure with
  zero I/O, `infra/` holds all I/O, `shape/` is data-in-string-out, `law/` is
  contracts only. Nothing checks it. `../epiphany` has a `:boundary-check` alias;
  `packages/clio` enforces its own construction order by reading forms with
  edamame rather than matching text.

They were left out for a reason that still partly holds: shx has nine source
files and 784 lines, so neither check can fire today. A gate that can only ever
pass trains readers to skip it.

What changes the calculus is `port-shx-to-cljc` and the hexis epic. Once `law/`,
`shape/`, and `domain/` are `.cljc` with JVM code behind an `extern` boundary, the
boundary check stops being decorative — it becomes the thing that keeps the port
honest, and clio has already proved the shape.

## Acceptance criteria

- [ ] GIVEN an `io/file` call added to any `shx.domain.*` namespace WHEN
      `bin/analyze` runs THEN it fails, naming the file and the rule
- [ ] GIVEN a `shx.law.*` namespace that requires `shx.infra.*` WHEN
      `bin/analyze` runs THEN it fails
- [ ] GIVEN the tree as it stands WHEN `bin/analyze` runs THEN both new checks
      report zero findings (they join the gate at zero, or they do not join it)
- [ ] The boundary check reads FORMS, not text — a `;; io/file` in a comment or a
      docstring does not trip it
- [ ] `bin/analyze --strict` promotes HARD structural breaches to failures;
      unstrict runs report them without failing
- [ ] `docs/static-analysis.md`'s tool table and "What is not here yet" section
      are updated to match

## Verification

```bash
bin/analyze                      # zero findings on the current tree
bin/analyze --strict

# and the negative cases, which are the real test:
#   add (clojure.java.io/file "x") to src/shx/domain/merge.clj  -> must fail
#   add [shx.infra.fs] to src/shx/law/ir.clj's :require         -> must fail
# revert both.
```

A check nobody has seen fail is a check nobody knows works. Both negative cases
must be run by hand at least once and the result noted in a comment on this card.

## Scope

- a new smell reporter, reading clj-kondo's analysis export
- a new boundary check over `src/` and `test/`
- `bin/analyze` — two new sections and their tiers
- `bb.edn` — one task each
- `docs/static-analysis.md`

## Out of scope

- Porting `../Truth/.clj-kondo/hooks/`. shx has no macros that need expansion.
- The interop-inventory check from `../epiphany`. There is no JS interop here
  until the `.cljc` port lands.
- Choosing thresholds for a 784-line tree by copying Truth's, which were tuned
  against 49k lines. Pick numbers that are tight for THIS tree, or the check
  passes vacuously.

## Reference points

- `../Truth/dev/smell_report.clj` — the report to port, and the stdin/EDN
  contract it expects.
- `../Truth/bin/analyze:77-97` — how it is invoked, and the `PIPESTATUS` bug that
  made it report structural breaches on any kondo warning. Do not reintroduce it:
  capture the analysis export to a file first, and treat an empty export as
  "the check did not run", not as zero smells.
- `../eta-mu/packages/clio` — construction-order enforcement by reading forms
  with edamame.
- `~/spaces/eta-mu/kanban/tasks/clojure-static-analysis-parity.md` — the sibling
  card for the same work in eta-mu; keep the two from diverging in approach.

## Anti-patterns

- Do NOT write either check in bash with `grep`. A require is a form; text
  matching gets docstrings and comments wrong in both directions.
- Do NOT copy Truth's thresholds unexamined (see Out of scope).
- Do NOT add either check as ADVISORY "for now". The gating contract in
  `docs/static-analysis.md` is that a check joins the gate at zero or does not
  join it; an advisory tier is where checks go to be ignored.
- Do NOT make the boundary check tolerate `utils/` or `helpers/`. `AGENTS.md`
  forbids them by name; the check should say so.

## Open questions

- Build against the current JVM shx, or write it `.cljc` from the start so it
  ports into eta-mu unchanged? Same fork in the road as `hexis-assembler`, and
  the answer should match that card's. **Resolve before this leaves `breakdown`.**