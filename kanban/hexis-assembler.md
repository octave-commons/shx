---
uuid: "shx-kanban-hexis-assembler"
title: "Write the assembler: fold resources/**.edn into rules/ and configs/"
status: "todo"
priority: P0
labels: ["tasks","hexis","assembler","5sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/hexis-assembler.md"
category: "tasks"
points: 5
---

# Write the assembler

## Context

`~/.ημ/resources/**.edn` now holds 67 instruction events across 6 ledgers.
Nothing consumes them yet. `~/.ημ/rules/` and `~/.ημ/configs/` were
deliberately NOT hand-created, because they are emitted artifacts and
hand-creating them contradicts `:structure/resources-are-truth`.

## Shape

Same fold as `shx.domain.merge`: group by `:id`, order by `:ts`, last wins,
drop `:kind :retract`, ignore `:kind :propose`.

## Why build-time, not read-time

Verified 2026-08-10: no Claude Code hook runs before CLAUDE.md is read.
`Setup` only fires under `--init-only`; `InstructionsLoaded` fires after
loading. So assembly is a build step and the emitted artifacts are committed.
A `claude` wrapper is belt-and-braces, never load-bearing.

## Open decision

Build against current JVM shx, or write `.cljc` from the start so it ports
into eta-mu unchanged? Blocks on `port-shx-to-cljc` if the latter.

## Definition of Done

`bb hexis:assemble` regenerates `~/.claude/CLAUDE.md`, `~/.claude/rules/`,
and the opencode `instructions` array from the ledgers alone.
