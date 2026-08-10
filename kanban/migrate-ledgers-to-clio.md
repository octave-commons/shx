---
uuid: "shx-kanban-migrate-ledgers-to-clio"
title: "Migrate ~/.eta-mu instruction ledgers to real clio events"
status: "todo"
priority: P1
labels: ["tasks","clio","ledger","5sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/migrate-ledgers-to-clio.md"
category: "tasks"
points: 5
---

# Migrate instruction ledgers to clio

## Context

The six ledgers under `~/.ημ/resources/` are the right SHAPE for clio
(newline-delimited EDN) but are NOT clio events. They lack `:event/id`,
`:event/schema`, `:event/stream`, `:event/seq`, `:event/causes`.

## Blockers, recorded in ~/.eta-mu/INDEX.edn

1. Author a Malli catalog for instruction event types; let
   `clio.infra.runtime/open` materialize the schema root.
   NEVER hand-write a schema hash. They are content-derived; inventing one
   is corruption.
2. Create each ledger via `create-ledger!` (CLI `new`). `append-event!`
   deliberately never brings a ledger into being.
3. All appends must go through clio. `append-event!` holds a POSIX
   `fcntl(F_SETLKW)` advisory lock on the ledger inode, and external writers
   must take the same lock. Appending with `>>` breaks the protocol.

## Key mapping

`:id -> :event/subject`, `:kind -> :event/type`, `:ts -> :event/at`,
`:owner -> :event/actor`, remaining keys -> `:event/data`.
