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

A stack renders to one unit file per unit; `check` diffs the stack against live systemd (user units) state (`systemctl --user list-unit-files '<encoded-ownership-prefix>*'` plus a scan of the resolved target-user unit directory for owned files, because `list-units` shows only units currently in memory and misses disabled, unloaded ones; `show` for state); `apply` converges live state for user units whose target name starts with the collision-free prefix `shx-<lowercase hex of the exact UTF-8 :stack/scope bytes>--` and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

Resolve the unit directory once from the explicitly authorized target user's
identity, absolute home and environment, not the coordinator's ambient `HOME` or
`XDG_CONFIG_HOME`. A nonempty valid absolute `XDG_CONFIG_HOME` selects
`$XDG_CONFIG_HOME/systemd/user`; unset, empty or invalid relative values select
`$HOME/.config/systemd/user` using that target user's home. Ignore and diagnose an
invalid relative value; never use it or resolve it against a working directory.
Missing or inconsistent target-user context fails before I/O. Pass the resolved
directory as explicit rendering input and reuse the same binding for inventory,
removal, `check` and `apply`, including commands against that user's manager.
The resolved path alone does not grant permission to another user's units.
Before `systemctl --user` inventories or acts on units, verify that the running
manager's effective unit search path resolves each target from the resolved
directory. Refuse reconciliation if it does not.

Read `org.freedesktop.systemd1.Manager.UnitPath` from the already-running target
user manager over the admitted user-bus connection, after verifying that the
manager peer UID matches the explicitly authorized target UID. Bind rendering's
home/environment and every manager operation to that same target identity;
derive the effective search path from the manager, never the invoker's environment
or `systemd-path`. Check ordered lookup and any loaded unit's `FragmentPath`
against the selected directory, including higher-priority same-named files,
links and masks. Missing, unsupported or inconsistent identity/path evidence
refuses reconciliation; revalidate after reconnect and before mutation. These
are future shared live-state evidence requirements, not an implemented manager
query or a reason to start/reconfigure a manager to make the check pass.

## Acceptance criteria

- [ ] GIVEN the manager started with the default `XDG_CONFIG_HOME` while the
  target environment selects a custom directory, and same-named units exist in
  both directories WHEN `check` or `apply` runs THEN it detects the manager-path
  mismatch and refuses reconciliation without stopping a unit or removing a file.
- [ ] GIVEN a correctly bound existing manager whose `UnitPath` resolves targets from the selected directory WHEN `check` or `apply` runs against stubbed I/O THEN the path check passes; GIVEN a wrong manager UID, an unavailable/unsupported `UnitPath`, a conflicting `FragmentPath` or a changed manager connection/lookup before mutation THEN reconciliation fails with zero mutations. Capture target identity, manager evidence and resolved unit paths in fixtures; changing the invoker's environment or substituting `systemd-path` output cannot turn a mismatch into a pass.
- [ ] GIVEN the authorized target user's absolute home and unset or empty `XDG_CONFIG_HOME` WHEN rendering, inventory, removal, `check` or `apply` selects its unit directory THEN every operation uses that user's `$HOME/.config/systemd/user`; GIVEN a custom absolute `/srv/alice-config` THEN every operation uses `/srv/alice-config/systemd/user` instead.
- [ ] GIVEN `XDG_CONFIG_HOME=relative/config` WHEN resolving the target directory THEN the invalid value is diagnosed and ignored, every operation uses the target-home default, and no relative-path or coordinator-home I/O occurs. Missing or inconsistent target-user identity/home/environment is refused before I/O.
- [ ] GIVEN a stale disabled/unloaded encoded-prefix owned unit exists only in the authorized target user's custom config directory WHEN `check` and `apply` run THEN inventory reports it and removal affects only that owned file; unmanaged files and any same-named file in the default directory remain untouched. Retain the existing daemon-reload ordering assertions.
- [ ] GIVEN coordinator and target-user homes/environments differ WHEN the authorized target is selected THEN rendering and manager/filesystem operations use only that target's resolved context; GIVEN a different user outside the admitted scope THEN reads and mutations are denied before I/O rather than inheriting the coordinator's environment or treating an encoded name as permission.
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
- [Upstream systemd.unit user-mode load path](https://github.com/systemd/systemd/blob/main/man/systemd.unit.xml): user configuration uses `$XDG_CONFIG_HOME/systemd/user` or the target-home default.
- [XDG Base Directory Specification, environment variables](https://specifications.freedesktop.org/basedir/latest/): paths must be absolute; unset/empty `XDG_CONFIG_HOME` defaults to `$HOME/.config` and relative values are invalid and ignored.
- [Upstream systemd manager and unit properties](https://github.com/systemd/systemd/blob/main/man/org.freedesktop.systemd1.xml): `UnitPath` is the manager's active search path; `FragmentPath` identifies the file a loaded unit came from. [systemd-path](https://github.com/systemd/systemd/blob/main/man/systemd-path.xml) uses its invoked environment and need not reflect that manager.

These directory and user-scope acceptance cases are future stubbed adapter
fixtures. This planning amendment implements no adapter and executes no live
systemd or filesystem reconciliation.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Historical note from review round 5 (Codex) on octave-commons/shx#2: the planned adapter requirements were revised to depend on supervisor-ir-live-state-law and fail closed on invalid external payloads before check/apply. Publication was pending when this note was recorded; this planning PR does not implement the adapter.

---
