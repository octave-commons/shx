---
uuid: "shx-kanban-decompose-contract-to-ledgers"
title: "Decompose the global contract into ndEDN resource ledgers"
status: "done"
priority: P1
labels: ["tasks","hexis","ledger","5sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/decompose-contract-to-ledgers.md"
category: "tasks"
points: 5
---

# Done 2026-08-10

`~/.claude/CLAUDE.md` decomposed into 67 events across 6 ledgers under
`~/.ημ/resources/`, plus `INDEX.edn` and `resources/README.md`.

## Lesson, learned the hard way

First attempt used `{:entries [...]}`. That is NOT append-only: appending
means rewriting the closing bracket. Every real ledger here is one flat map
per line, and `receipts.edn` does not use vectors even inside a line.

The rule now written into `resources/README.md`:

THE UNIT OF THE LEDGER IS THE UNIT OF REVOCATION.

Anything independently disableable gets its own line. Precedence that was a
vector is now an `:ord` field per operator. Disabling is an appended
`{:kind :retract}` event.

Validated: 67 events, 67 flat maps, 0 vectors, 0 duplicate ids.
