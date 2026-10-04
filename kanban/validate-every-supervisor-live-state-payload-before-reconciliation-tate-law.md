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

## Target-specific pull units

The five-point parent remains an incoming planning contract. Scope the following independent target pull units before implementation; this outline creates no lifecycle transition or completed substory. Each unit delivers its target contract, pure normalization and target fixtures in the shared `shx.law.supervisor-live` boundary, with adapter-consumer assertions in its existing adapter story. The shared normalized identity/state shape stays one contract.

1. **pm2:** validate and normalize `pm2 jlist` identities and states, with valid, genuinely empty, truncated/malformed, missing-identity and version-shifted fixtures under `test/resources/supervisor/live/pm2/`. The `supervisor-ir-pm2` consumer must reject invalid inventory before planning and preserve the encoded physical ownership identity. Completion is the target's rejecting/accepting fixture assertions plus its adapter-consumer tests.
2. **Docker Compose:** validate and normalize the selected project's `config`/`ps` payloads with the same valid/empty/invalid/version cases under `test/resources/supervisor/live/compose/`. The `supervisor-ir-compose` consumer must retain the explicit rendered-file/project context and reject partial or wrong-context evidence before planning. Completion is target fixture assertions plus that consumer binding.
3. **systemd user units:** validate and normalize user-unit identities and states with the same cases under `test/resources/supervisor/live/systemd/`. The `supervisor-ir-systemd` consumer must preserve the encoded physical ownership identity and distinguish failed reads from a genuinely empty unit set. Completion is target fixture assertions plus its adapter-consumer tests.
4. **Kubernetes:** validate and normalize complete separate Deployment/Service lists with the same cases under `test/resources/supervisor/live/k8s/`, including omitted/failed kinds and incomplete pagination. Preserve both ownership labels and namespace verbatim for the `supervisor-ir-k8s` consumer's immediate per-mutation recheck fixtures. Completion is target fixture assertions plus producer/consumer provenance and fail-closed assertions.

Each proposed pull unit scopes its change to `src/shx/law/supervisor_live.cljc`, its own cases in `test/shx/law/supervisor_live_test.clj`, its target fixture directory and the corresponding adapter story's consumer tests; the other three targets are outside that pull. Each is verified with its focused tests, `bb check` and executable-normalization mutation evidence when implemented. These fixtures and target-specific completion conditions are planned work; this Markdown repair has not implemented or executed them. Existing parent points, status and dependencies remain unchanged.

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
