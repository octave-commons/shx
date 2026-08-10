---
uuid: "shx-kanban-claude-md-agents-md-shim"
title: "Make CLAUDE.md a thin @AGENTS.md shim"
status: "done"
priority: P1
labels: ["tasks","harness","docs","2sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/claude-md-agents-md-shim.md"
category: "tasks"
points: 2
---

# Done 2026-08-10

`/import` had appended a verbatim COPY of AGENTS.md into CLAUDE.md, which is
exactly the drift this was meant to prevent. Replaced with `@AGENTS.md` and
moved the architecture content into AGENTS.md.

Verified: Claude Code reads CLAUDE.md, not AGENTS.md, and has no setting to
rename the memory file. opencode does NOT auto-expand `@file` either; it only
hints the agent to Read it. Use its `instructions` glob array, and note that
those arrays are NOT merged across configs, so the published array must be
complete.

No CODEX.md or OPENCODE.md is needed. Codex reads AGENTS.md natively.
