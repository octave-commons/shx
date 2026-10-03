---
uuid: "shx-kanban-shx-core-runtime-missing"
title: "shape/bb emits calls to a shx.core runtime that does not exist"
status: "incoming"
priority: P1
labels: ["tasks","shx","debt","5sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/shx-core-runtime-missing.md"
category: "tasks"
points: 5
---

# shx.core does not exist

`shx.shape.bb` generates calls to `set-env!`, `path-prepend!`, `path-append!`,
`defalias`, `source-bash`, and `test` on a `shx.core` namespace that is not in
the repo. Generated bb code is therefore not runnable for shell-state effects.

Either implement `shx.core` or narrow what the bb emitter claims to support.
