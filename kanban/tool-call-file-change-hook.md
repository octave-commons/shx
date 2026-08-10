---
uuid: "shx-kanban-tool-call-file-change-hook"
title: "Hook: record events correlating tool calls to file changes"
status: "todo"
priority: P1
labels: ["tasks","hooks","telemetry","3sp"]
created_at: "2026-08-10T05:44:12.012Z"
source: "shx/kanban/tool-call-file-change-hook.md"
category: "tasks"
points: 3
---

# Correlate tool calls to file changes

## Requirement

Record an event per tool call carrying:

- time
- paths touched
- `tool_call_id`

## Approach

Claude Code `PostToolUse` hook. It receives tool name, input, and response.

## Unverified

Whether a stable `tool_call_id` is exposed in the PostToolUse payload has NOT
been confirmed. Verify before designing the event schema, since the id is the
whole point of the correlation.

## Downstream

Feeds `rheos` and gives session-mycology real evidence instead of recall.
