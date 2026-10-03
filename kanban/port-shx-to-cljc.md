---
uuid: "shx-kanban-port-shx-to-cljc"
title: "Port shx law/shape/domain to .cljc for nbb + shadow-cljs"
status: "todo"
priority: P1
labels: ["tasks","hexis","port","8sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/port-shx-to-cljc.md"
category: "tasks"
points: 8
---

# Port shx to .cljc

## Context

shx is JVM Clojure. clio and eta-mu are nbb / shadow-cljs. Merging requires
`law/`, `shape/`, and `domain/` to be runtime-neutral `.cljc`, with JVM-only
code pushed behind an `extern` boundary.

clio already proved this shape and enforces it with a boundary lint that
refuses raw `js/`, `#js`, `js*`, or string host requires outside
`clio.extern.js.*` — reading forms via edamame, not text.

## Blocked-by nothing, blocks the epic

`shx.shape.quote` and `shx.law.ir` are already pure and should port cleanly.
`shx.infra.config` and `shx.infra.fs` are the JVM-bound parts and become
`extern`.

## Note

The same wall stands in front of Truth and Epiphany, which are also clj/jvm.
Solving it here is reusable.
