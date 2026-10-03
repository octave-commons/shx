---
category: "tasks"
labels: ["tasks", "clio", "ledger", "5sp"]
write-id: "1791026139601-0.z45mqktywbponbwsvwj"
points: "5"
source: "shx/kanban/migrate-ledgers-to-clio.md"
title: "Migrate ~/.eta-mu instruction ledgers to real clio events"
priority: "P1"
status: "todo"
uuid: "shx-kanban-migrate-ledgers-to-clio"
created_at: "2026-08-10T05:44:12.012Z"
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

---

Qualification clarification for SHX#1 finding 67b950d51579584256919d22 (2026-10-03). The original mapping is illustrative, not a complete migration or permission to write events.

Before implementation, a versioned adapter contract must define every canonical Clio field through upstream APIs: event/id (stable source binding/idempotent import), event/type, content-derived event/schema from an admitted catalog, stable scoped event/stream, serialized event/seq allocation, evidenced event/causes (never invented), event/at, event/actor, event/subject and catalog-validated event/data. Preserve raw source records, hashes and source IDs as provenance; legacy :id identifies the resource subject, not automatically a unique occurrence. Missing identity/order/schema or unrepresentable sources are refused until the adapter contract defines them. Record source envelope/version and replay policy; never rewrite historical source bytes or hand-create a schema hash.

Acceptance requires schema validation, exact source-to-destination fixtures for all fields, deterministic repeated import/no duplicate event, equal legacy IDs in different scopes, unknown format/type refusal, missing/gapped revisions, concurrent revision allocation, causal-reference failures and old-ledger replay. All writes must participate in canonical Clio locking/compare-and-append; board state stays Rheos-owned. No local migration parser/event kernel is authorized or implemented here.

Comment rendering was repaired using the actual Rheos CLI built from unmerged open-hax/rheos PR #2 head 66e8b67951971af541503c4a556c5645eaa727c0, with complete CodeRabbit review and 153 tests / 790 assertions passing. Blank separator lines preserve prose rendering, previous section content and the complete historical event prefix. This is a staged consumer repair; the upstream formatter still awaits review convergence. No card transition, historical rewrite, or formatter merge is claimed.

---