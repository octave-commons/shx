---
category: "tasks"
labels: ["tasks", "docs", "2sp"]
write-id: "1791021467056-0.4mpczzy6a6conmbfumg"
points: "2"
source: "shx/kanban/process-inbox.md"
title: "Route the 13 inbox notes per docs/inbox/INDEX.md"
priority: "P2"
status: "todo"
uuid: "shx-kanban-process-inbox"
created_at: "2026-08-10T05:44:12.012Z"
---

# Process the inbox

`docs/inbox/INDEX.md` routes all 13 notes to destinations. Two byte-identical
duplicates were deleted 2026-08-10 (md5-verified).

Highest value first:

- `2026.08.09.20.21.57.md` is the origin story of envm. The bubbling paragraph
  justifies `domain.merge/fold-fragment-tree`. Fold into AGENTS.md.
- `2026.08.09.22.59.57.md` is actionable, not a note: eta-mu#282 follows #280
  and "282 is probably ready to merge". Move to kanban.
- Two session-noise files can be deleted once skimmed.

---
Qualification clarification for SHX#1 findings a4f813c3316901a93e9a7835 and eea4ae2d34cb2b70397300fc (2026-10-03). Existing routing scope covers the bare session header docs/inbox/2026.08.09.22.14.06.md and NFS capture docs/inbox/2026.08.09.23.26.12.md.

Preserve original captured bytes/source commits and hashes: removal/redaction means omitting the opaque session ID from a derived published header or keeping the raw header only in the provenance archive, not rewriting historical receipts/events or claiming this identifier is an authentication secret. Identify and approve the owning ops-documentation destination before publishing derived NFS material there; retain source links and qualification addenda. INDEX must distinguish the source archive from the derived destination, with no claim a move has already happened. Verify provenance/reference continuity and absence of raw session IDs in the derived surface.

Archive accessibility/heading cleanup is explicitly tracked by real Rheos card a6b112a4-9d6c-4f7d-9bdf-d05bdc690f88; reference that card rather than declaring those fixes complete. This comment does not close/transition cards or deploy NFS.
---