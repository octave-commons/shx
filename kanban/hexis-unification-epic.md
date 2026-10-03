---
uuid: "shx-kanban-hexis-unification-epic"
title: "EPIC: unify shx and muse onto one EDN substrate in eta-mu"
status: "breakdown"
priority: P1
labels: ["epic","hexis","13sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/hexis-unification-epic.md"
category: "tasks"
points: 13
---

# EPIC: unify shx and muse onto one EDN substrate

## Context

shx renders EDN into shell environments (`envm`). muse renders EDN into agent
harness config. They are the same operation with different emitters, and both
already use the eta-mu namespace law.

Grounding discovered 2026-08-10: `packages/clio` uses the SAME construction
order as shx (`law -> shape -> extern -> domain -> infra`). That shared
convention is the strongest argument the two belong in one package.

## The core identity

The ledger is the repeated act; the projection is the settled state you run.

- `.bashrc` is a projection of habit.
- A system prompt is a projection of habit.
- Same fold, different emitter.

## Children

- `port-shx-to-cljc` (hard blocker)
- `hexis-assembler`
- `migrate-ledgers-to-clio`
- `name-the-unified-package`

## Definition of Done

One package under `~/spaces/eta-mu/packages/` that emits both shell
environment text and agent instruction files from one resource fold.
