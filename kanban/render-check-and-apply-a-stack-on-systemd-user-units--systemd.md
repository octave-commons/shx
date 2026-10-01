---
uuid: "supervisor-ir-systemd"
title: "Render, check and apply a stack on systemd user units"
status: "incoming"
type: "task"
priority: "P2"
points: "3"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897668899-0.rdw91ek2dxi6b6iydh"
created_at: "2026-10-01T23:34:28.899Z"
---

# Render, check and apply a stack on systemd (user units)

## Outcome

A stack renders to one unit file per unit; `check` diffs the stack against live systemd (user units) state (systemctl --user show / list-units); `apply` converges live state for user units whose name starts with the delimited prefix `<:stack/scope>-` (the same prefix `scope` produces in `supervisor-ir-merge-law`) and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters.

## Acceptance criteria

- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the systemd golden file.
- [ ] GIVEN a stack using a feature systemd (user units) cannot express WHEN rendered THEN the unsupported-feature report equals its systemd golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN scope `svc` and an unmanaged resource named `svcadmin` WHEN `apply` runs THEN it is not treated as owned and is untouched (only `svc-…` names are owned).
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_systemd.cljc`, `src/shx/infra/supervisor_systemd.clj`, golden files under `test/resources/supervisor/systemd/`

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".
