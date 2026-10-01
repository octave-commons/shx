---
uuid: "supervisor-ir-law"
title: "Define the supervisor unit/stack IR as Malli schemas in .cljc"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "shx, hexis, design"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897667999-0.vjqypig5i74n3ok8h0"
created_at: "2026-10-01T23:34:27.999Z"
---

# Define the supervisor unit/stack IR as Malli schemas in .cljc

## Outcome

`shx.law.supervisor` (`.cljc`) defines a registry-keyed Malli schema for a unit (process intent) and a stack (named units, profiles, owned-scope declaration), and `explain-stack` rejects malformed stacks with a path to the bad field.

## Context

First child of `shx-kanban-supervisor-ir`. Every emitter and adapter consumes this shape, so it lands first. The process command reuses the existing `:exec` node from `shx.law.ir` rather than redefining it.

## Acceptance criteria

- [ ] GIVEN a unit with `:exec`, `:cwd`, `:env`, `:ports`, `:needs`, `:health`, `:restart`, `:replicas`, optional `:image`, `:volumes`, `:user` WHEN validated THEN it passes.
- [ ] GIVEN a stack missing `:stack/scope` WHEN validated THEN `explain-stack` names `:stack/scope`.
- [ ] GIVEN a unit whose `:needs` names an undeclared unit WHEN validated THEN validation fails naming both units.
- [ ] VERIFY: no schema name collides with a Katamorph-owned name.

## Verification

```bash
bb check
bb mutate   # no surviving mutants in shx.law.supervisor
```

## Scope

- `src/shx/law/supervisor.cljc`, `test/shx/law/supervisor_test.clj`

## Reference points

- `src/shx/law/ir.clj` — registry style and `explain-program` error shape.

## Anti-patterns

- Do not encode target-specific keys (pm2 `exec_mode`, k8s `spec`) in the IR; targets map from the IR, not into it.
