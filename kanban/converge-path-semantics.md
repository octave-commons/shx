---
uuid: "shx-kanban-converge-path-semantics"
title: "Decide whether envm PATH rendering should converge with IR :path/prepend"
status: "icebox"
priority: P2
labels: ["tasks","shx","design","3sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/converge-path-semantics.md"
category: "tasks"
points: 3
---

# Two PATH semantics

`cli/render-env-bash` filters directories at RENDER time via
`infra.fs/existing-dirs` and emits one collapsed PATH line.

The IR's `:path/prepend` emits a per-entry RUNTIME `[ -d ... ] &&` guard.

Documented at `src/shx/cli.clj:31` as a deliberate divergence. Converging them
is a design decision, not a cleanup. Parked until the epic settles.
