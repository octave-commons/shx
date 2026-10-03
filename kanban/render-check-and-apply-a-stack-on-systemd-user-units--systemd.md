---
category: "kanban"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790907650788-0.h7o2o4ftbb02ymrtmm"
points: "3"
title: "Render, check and apply a stack on systemd user units"
priority: "P2"
status: "incoming"
uuid: "supervisor-ir-systemd"
created_at: "2026-10-01T23:34:28.899Z"
---

# Render, check and apply a stack on systemd (user units)

## Outcome

A stack renders to one unit file per unit; `check` diffs the stack against live systemd (user units) state (`systemctl --user list-unit-files '<encoded-ownership-prefix>*'` plus a scan of the user unit directory `~/.config/systemd/user/` for owned files, because `list-units` shows only units currently in memory and misses disabled, unloaded ones; `show` for state); `apply` converges live state for user units whose target name starts with the collision-free prefix `shx-<lowercase hex of the exact UTF-8 :stack/scope bytes>--` and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

## Acceptance criteria

- [ ] GIVEN valid scopes `svc` and `svc-prod` WHEN target names are rendered THEN their ownership prefixes are respectively `shx-737663--` and `shx-7376632d70726f64--`; neither prefix can match the other. Encode exact UTF-8 bytes without case folding or Unicode normalization; raw scope names and merge-law logical prefixes are never deletion authority. Validate the complete generated target identifier before any I/O. Pin the encoding in the pure emitter and use that same function for inventory selection and every mutation.
- [ ] GIVEN scope `svc` and fixtures containing `svc-prod-api.service`, the encoded resource owned by `svc-prod`, and a stale resource owned by `svc` WHEN `apply` runs THEN only the last resource is stopped/removed; both other resources remain untouched. Test the overlapping scopes, not just `svcadmin`.
- [ ] GIVEN truncated, malformed or version-shifted live output WHEN `check` or `apply` runs THEN the target live-state contract rejects it with a path and no mutation occurs; a valid empty response remains distinguishable.
- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_systemd.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the systemd golden file.
- [ ] GIVEN a stack using a feature systemd (user units) cannot express WHEN rendered THEN the unsupported-feature report equals its systemd golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN scope `svc` and an unmanaged resource named `svcadmin` WHEN `apply` runs THEN it is not treated as owned and is untouched (legacy raw scope prefixes supply no ownership).
- [ ] GIVEN a previously managed unit that is disabled and unloaded but whose unit file remains WHEN `check` runs THEN it reports that unit as stale, and `apply` removes its file.
- [ ] WHEN `apply` writes or removes user unit files THEN it runs `systemctl --user daemon-reload` before relying on the manager's updated unit-file state; tests assert that removed units no longer appear in the manager's inventory.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_systemd.cljc`, `src/shx/infra/supervisor_systemd.clj`, golden files under `test/resources/supervisor/systemd/`
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_systemd.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_systemd_test.clj` (goldens) and `test/shx/infra/supervisor_systemd_test.clj`: `check` and `apply` against stubbed process I/O, covering the zero-diff, one-changed-unit, in-scope-removal, daemon-reload ordering, removed-unit inventory absence and out-of-scope-preservation criteria without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: adapter now depends on supervisor-ir-live-state-law and fails closed on invalid external payloads before check/apply; commit pending.

---