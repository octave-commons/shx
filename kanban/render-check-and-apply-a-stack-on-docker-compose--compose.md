---
uuid: "supervisor-ir-compose"
title: "Render, check and apply a stack on docker compose"
status: "incoming"
type: "task"
priority: "P2"
points: "3"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897668661-0.ydjmfypynf0x38zbb5"
created_at: "2026-10-01T23:34:28.661Z"
---

# Render, check and apply a stack on docker compose

## Outcome

A stack renders to compose YAML; `check` diffs the stack against live docker compose state (docker compose -p <scope> config / ps --format json); `apply` converges live state for the compose project named by `:stack/scope` and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters.

## Acceptance criteria

- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the compose golden file.
- [ ] GIVEN a stack using a feature docker compose cannot express WHEN rendered THEN the unsupported-feature report equals its compose golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_compose.cljc`, `src/shx/infra/supervisor_compose.clj`, golden files under `test/resources/supervisor/compose/`

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".
