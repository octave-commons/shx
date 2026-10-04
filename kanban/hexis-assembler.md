---
category: "tasks"
labels: ["tasks", "hexis", "assembler", "5sp"]
write-id: "1791026138822-0.pwn2rntakvqmont9nt1"
points: "5"
source: "shx/kanban/hexis-assembler.md"
title: "Write the assembler: fold resources/**.edn into rules/ and configs/"
priority: "P0"
status: "todo"
uuid: "shx-kanban-hexis-assembler"
created_at: "2026-08-10T05:44:12.012Z"
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

---

Qualification clarification for SHX#1 findings 7b9f2b2e986a0fa141de721d and 624c4ee0fda03cf883e2ef72 (2026-10-03). The original body remains historical scope. Its instruction-event fold is a separate proposed contract, not the implemented shx.domain.merge fragment-tree fold.

Before implementation, publish/version the admitted event schemas and legacy adapter. Use the canonical upstream event admission/order, never physical file order. Resource identity must be schema-defined. A resource's updates require one validated ordered history; ambiguous cross-stream updates, unknown kinds, missing order/identity and conflicting duplicates are refused until the reviewed contract resolves them. For that history, ignore proposal-only events before selecting the final admitted assert/amend/retract. Apply a final retract after winner selection: it removes the resource and never resurrects an older value. A later explicitly admitted assertion may restore it. Timestamps are metadata, not sufficient ordering; equal timestamps must not silently choose a winner. :ord is the explicit precedence/output order from decompose-contract-to-ledgers, not event recency: sort surviving resources by schema-defined :ord and stable resource identity, refusing absent/ambiguous required precedence. State and test the version's exact kind/order/identity/default rules before implementation. Include input permutations, duplicate identity, equal timestamps, final retract, retract-then-assert, proposal-only input and explicit :ord output fixtures.

Exact emitted CLAUDE.md is @AGENTS.md followed by a newline (architecture notes stay in AGENTS.md, not duplicated). Assert these bytes plus rules/config output in fixtures. Existing no-implementation/runtime choices remain prerequisites; no assembler or actor is built by this qualification.

Comment rendering was repaired using the actual Rheos CLI built from unmerged open-hax/rheos PR #2 head 66e8b67951971af541503c4a556c5645eaa727c0, with complete CodeRabbit review and 153 tests / 790 assertions passing. Blank separator lines preserve prose rendering, previous section content and the complete historical event prefix. This is a staged consumer repair; the upstream formatter still awaits review convergence. No card transition, historical rewrite, or formatter merge is claimed.

---