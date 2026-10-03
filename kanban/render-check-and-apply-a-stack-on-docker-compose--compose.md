---
category: "kanban"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790907650564-0.3i18trsdpoemuxkw9kq"
points: "3"
title: "Render, check and apply a stack on docker compose"
priority: "P2"
status: "incoming"
uuid: "supervisor-ir-compose"
created_at: "2026-10-01T23:34:28.661Z"
---

# Render, check and apply a stack on docker compose

## Outcome

A stack renders to compose YAML; `check` diffs the stack against live docker compose state (`docker compose -p <scope> config` / `docker compose -p <scope> ps --all --format json`); `apply` converges live state for the compose project named by `:stack/scope` and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

## Acceptance criteria

- [ ] GIVEN truncated, malformed or version-shifted live output WHEN `check` or `apply` runs THEN the target live-state contract rejects it with a path and no mutation occurs; a valid empty response remains distinguishable.
- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_compose.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the compose golden file.
- [ ] GIVEN a stack using a feature docker compose cannot express WHEN rendered THEN the unsupported-feature report equals its compose golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN an exited in-scope container that the desired stack no longer declares in the live-state fixture WHEN `check` queries `docker compose -p <scope> ps --all --format json` THEN the container is included in the validated inventory and appears in the diff rather than being omitted as if absent.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_compose.cljc`, `src/shx/infra/supervisor_compose.clj`, golden files under `test/resources/supervisor/compose/`
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_compose.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_compose_test.clj` (goldens) and `test/shx/infra/supervisor_compose_test.clj`: `check` and `apply` against stubbed process I/O, covering the zero-diff, one-changed-unit, exited-container inventory/diff, in-scope-removal and out-of-scope-preservation criteria without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: adapter now depends on supervisor-ir-live-state-law and fails closed on invalid external payloads before check/apply; commit pending.

---