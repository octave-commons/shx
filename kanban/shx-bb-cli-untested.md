---
category: "tasks"
labels: ["tasks", "shx", "tests", "3sp"]
write-id: "1786347840437-0.i0i2osjpb7ckswdzlyr"
points: "3"
source: "shx/kanban/shx-bb-cli-untested.md"
title: "shape/bb and cli have no tests"
priority: "P1"
status: "incoming"
uuid: "shx-kanban-shx-bb-cli-untested"
created_at: "2026-08-10T05:44:12.012Z"
---

# Untested namespaces

Covered: `shape/bash`, `shape/quote`, `law/ir`, `domain/merge`, `infra/config`.
Uncovered: `shape/bb`, `cli`.

This matters more than usual because `shape/bb/emit` has a `:default` method
returning nil, which `emit-program` turns into a
`# bb emit not implemented for <head>` placeholder. It degrades silently and
will not fail the build.

---
Mutation evidence, 2026-08-10 (first bin/mutate run in this repo).

heretic now measures the pure layers. Of 30 mutation sites, 11 are honest no-coverage gaps and both live in this card's territory:

  src/shx/shape/bb.clj   9 sites — lines 14, 20, 26, 39, 64, 69, 85, 86
  src/shx/shape/bash.clj 2 sites — lines 13, 26

So this is no longer 'bb and cli have no tests' in the abstract: those are the specific forms where a mutation would never be run because no indexed test reaches them. bb.clj's :default-returning-nil path is exactly the silent-degradation risk the body already names, and it is unreached.

cli.clj is NOT in that count — it is excluded from mutation in heretic.edn because it is not instrumented (mutating the process boundary is low-signal), so heretic can say nothing about it either way. Its lack of tests is real and still belongs to this card; it just will not show up in a mutation report.

Verification for whoever picks this up: bin/mutate no-coverage should stop listing shape/bb.clj and shape/bash.clj. Score is killed/(killed+survived) and ignores no-coverage, so watch the site list, not the percentage.

Remaining 12 no-coverage sites are law/ schema literals — a heretic attribution limitation, not a gap. Carded separately as heretic-schema-literal-attribution. See docs/mutation-testing.md.

Correction to the previous comment's arithmetic, after re-measuring on a clean recollect.

The 11 honest gaps are unchanged and still yours: shape/bb.clj 9 sites (14, 20, 26, 39, 64, 69, 85, 86) and shape/bash.clj 2 sites (13, 26).

The remainder is 13, not 12: 12 law/ schema literals plus one lost attribution at shape/quote.clj:18. Totals are now 30 sites, 4 killed, 2 survived, 24 no-coverage, score 66.7% — see docs/mutation-testing.md > 'The score is not stable'. Nothing about this card's scope changes.
---