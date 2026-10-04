---
uuid: "shx-kanban-clio-ledgers-for-eta-mu"
title: "Move receipts, mycology, and rheos ledgers onto clio"
status: "incoming"
priority: P2
labels: ["tasks","clio","ledger","8sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/clio-ledgers-for-eta-mu.md"
category: "tasks"
points: 8
---

# Well-established eta-mu ledgers onto clio

Targets: `receipt-river`, `session-mycology`, `rheos`.

Currently `receipts.edn` is hand-tended newline-delimited EDN and
`~/.ημ/state/*/events.jsonl` is JSONL. Both predate clio.

Depends on `migrate-ledgers-to-clio` proving the catalog + lock protocol on a
smaller surface first.
