---
category: "kanban"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790903196159-0.waud71mejshmko9816c"
points: "3"
title: "Render, check and apply a stack on pm2"
priority: "P1"
status: "incoming"
uuid: "supervisor-ir-pm2"
created_at: "2026-10-01T23:34:28.433Z"
---

# Render, check and apply a stack on pm2

## Outcome

A stack renders to an ecosystem JSON document; `check` diffs the stack against live pm2 state (pm2 jlist); `apply` converges live state for apps whose name starts with the delimited prefix `<:stack/scope>-` (the same prefix `scope` produces in `supervisor-ir-merge-law`) and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters.

## Acceptance criteria

- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_pm2.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the pm2 golden file.
- [ ] GIVEN a stack using a feature pm2 cannot express WHEN rendered THEN the unsupported-feature report equals its pm2 golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN scope `svc` and an unmanaged resource named `svcadmin` WHEN `apply` runs THEN it is not treated as owned and is untouched (only `svc-…` names are owned).
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_pm2.cljc`, `src/shx/infra/supervisor_pm2.clj`, golden files under `test/resources/supervisor/pm2/`
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_pm2.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_pm2_test.clj` (goldens) and `test/shx/infra/supervisor_pm2_test.clj`: `check` and `apply` against stubbed process I/O, covering the zero-diff, one-changed-unit, in-scope-removal and out-of-scope-preservation criteria without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.
---