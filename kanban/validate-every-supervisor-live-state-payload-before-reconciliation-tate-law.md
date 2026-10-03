---
uuid: "supervisor-ir-live-state-law"
title: "Validate every supervisor live-state payload before reconciliation"
status: "incoming"
type: "task"
priority: "P1"
points: "5"
labels: "shx, hexis, law"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790907623411-0.69jesu0fx97wzjlg6bi"
created_at: "2026-10-02T02:20:23.411Z"
---

# Validate every supervisor's live-state payload before reconciliation

## Outcome

`shx.law.supervisor-live` defines Malli contracts and pure normalization for the live-state payloads read by pm2, docker compose, systemd user units and Kubernetes. Every `check` or `apply` adapter validates its external payload before comparing it with the desired stack. Malformed, truncated or version-shifted output fails closed and names the target and offending field; it cannot authorize deletion.

## Context

Child of `shx-kanban-supervisor-ir`. `supervisor-ir-law` validates desired EDN. That does not validate the external data returned by `pm2 jlist`, `docker compose config/ps`, `systemctl --user`, or `kubectl get`. This card is a hard prerequisite for the four adapter stories.

## Acceptance criteria

- [ ] GIVEN a captured valid response for each target WHEN normalized THEN a target-specific contract accepts it and produces a common set of live identities and states.
- [ ] GIVEN a valid Kubernetes response with `shx.dev/managed-by=<scope>` and `app.kubernetes.io/managed-by=shx` WHEN normalized THEN the record retains both labels and namespace unchanged. Missing or mismatched ownership remains missing/mismatched, never synthesized. Producer/consumer fixtures prove apply receives that provenance and rejects unauthorized mutations; recheck ownership at mutation time so a concurrent ownership change cannot authorize deletion from a stale read.
- [ ] GIVEN truncated JSON, malformed JSON, a missing identity or an unsupported version/shape for any target WHEN normalized THEN validation fails with the target and path; neither `check` nor `apply` acts on a partial set.
- [ ] GIVEN a valid empty response WHEN normalized THEN it represents zero live resources and remains distinguishable from parse failure. Any in-scope removal is allowed only from a complete, validated live-state payload; malformed or partial payloads never authorize removal.
- [ ] VERIFY: fixture tests cover each target's valid, empty, malformed and version-shifted output. The adapters call these validators before planning actions.

## Verification

```bash
bb check
bb mutate  # executable normalization logic yields killed mutants; none survive
```

## Scope

- `src/shx/law/supervisor_live.cljc`, `test/shx/law/supervisor_live_test.clj`, target fixtures under `test/resources/supervisor/live/`.

## Anti-patterns

- Do not coerce an invalid payload to `[]` or `{}`. An empty live set can cause destructive reconciliation.
- Do not give a provider response semantic authority merely because it parsed as JSON.
