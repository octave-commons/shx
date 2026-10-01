---
uuid: "supervisor-ir-pm2"
title: "Render, check and apply a stack on pm2"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897668433-0.c66y8xn24uib0e18uo"
created_at: "2026-10-01T23:34:28.433Z"
---

# Render, check and apply a stack on pm2

## Outcome

A stack renders to an ecosystem JSON document; `check` diffs the stack against live pm2 state (pm2 jlist); `apply` converges live state for apps whose name starts with the stack's `:stack/scope` prefix and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters.

## Acceptance criteria

- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the pm2 golden file.
- [ ] GIVEN a stack using a feature pm2 cannot express WHEN rendered THEN the unsupported-feature report equals its pm2 golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # emitter namespace has no surviving mutants
```

## Scope

- `src/shx/shape/supervisor_pm2.cljc`, `src/shx/infra/supervisor_pm2.clj`, golden files under `test/resources/supervisor/pm2/`

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".
