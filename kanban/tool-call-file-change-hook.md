---
category: "tasks"
labels: ["tasks", "hooks", "telemetry", "3sp"]
write-id: "1791021465041-0.x3gdimqjjwpdfppql5r"
points: "3"
source: "shx/kanban/tool-call-file-change-hook.md"
title: "Hook: record events correlating tool calls to file changes"
priority: "P1"
status: "todo"
uuid: "shx-kanban-tool-call-file-change-hook"
created_at: "2026-08-10T05:44:12.012Z"
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

---
Qualification clarification for SHX#1 findings 4907d5e574d852d40834367d and 6d512eabbed5efd2f07078d3 (2026-10-03). The original scope is retained; implementation remains blocked until this input/telemetry contract is reviewed.

Verify the active PostToolUse payload/version, including whether tool_use_id is actually present; do not assume tool_call_id equivalence or synthesize a missing upstream ID. For Edit/Write, a normalized tool-input path is only a declared target; claim a touched file only from a successful operation plus verifiable before/after change evidence bound to that operation. For Bash, command text is not proof of touched paths: require attributable change evidence, otherwise emit an explicit unknown path state/reason. Unknown, failed, partial, or unsupported tools must not produce invented path lists. Carry evidence kind and requested-versus-verified distinction in the reviewed schema; test successful, failed, relative/absolute, outside-workspace, dynamic-shell, and unknown payload cases.

Persist only schema-admitted minimal metadata. Normalize approved in-workspace paths relative to the workspace; refuse/redact outside paths, home/user/customer/secret-bearing segments and unsupported values before persistence. Raw prompts, tool inputs/responses and private source content are excluded by default. Scope opaque correlation IDs to the authorized job/principal. Readers must be access-controlled; retention duration and purpose require operator-approved policy before collection (disabled until that policy exists). Verify redaction/unknown-state tests before any hook is enabled. This comment adds no hook, telemetry collector or local ledger semantics.
---