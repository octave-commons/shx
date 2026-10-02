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

- [ ] GIVEN a unit with `:exec` (whose existing `:dir` and `:env` fields, `src/shx/law/ir.clj:23-27`, are the **only** working-directory and environment authority), `:ports`, `:needs`, `:health`, `:restart`, `:replicas`, optional `:image`, `:volumes`, `:user` WHEN validated THEN it passes.
- [ ] GIVEN a unit that carries its own `:cwd` or `:env` WHEN validated THEN validation fails, naming the key and pointing at `:exec`'s `:dir`/`:env`, so no adapter has to pick a precedence.
- [ ] GIVEN a stack missing `:stack/scope` WHEN validated THEN `explain-stack` names `:stack/scope`.
- [ ] GIVEN a unit whose `:needs` names an undeclared unit WHEN validated THEN validation fails naming both units.
- [ ] VERIFY: no schema name collides with a Katamorph-owned name.
- [ ] GIVEN `heretic.edn` WHEN `bb mutate` runs THEN it scans `.cljc` sources too (today it scans only `.clj`, `heretic.edn:23`), and its report shows a nonzero **killed** count from executable code in `shx.law.supervisor`, such as the cross-unit `:needs` and `:cwd`/`:env` rejection functions. Top-level Malli schema literals are never mutated (`docs/mutation-testing.md:127-142`, carded as `heretic-schema-literal-attribution`), so for each schema constraint a test must assert that an input violating it is rejected. A report with zero killed mutants fails this criterion.

## Verification

```bash
bb check
bb mutate   # nonzero KILLED mutants in shx.law.supervisor executable code, none surviving
```

## Scope

- `src/shx/law/supervisor.cljc`, `test/shx/law/supervisor_test.clj`
- `heretic.edn` and `bin/mutate` (plus any Heretic dependency change in `deps.edn`): needed for the `.cljc` mutation-scan criterion

## Reference points

- `src/shx/law/ir.clj` — registry style and `explain-program` error shape.

## Anti-patterns

- Do not encode target-specific keys (pm2 `exec_mode`, k8s `spec`) in the IR; targets map from the IR, not into it.
