---
category: "kanban"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790907651009-0.axtce8dedffwl508yl0"
points: "3"
title: "Render, check and apply a stack on Kubernetes"
priority: "P2"
status: "incoming"
uuid: "supervisor-ir-k8s"
created_at: "2026-10-01T23:34:29.115Z"
---

# Render, check and apply a stack on Kubernetes

## Outcome

A stack renders to Deployment and Service manifests; `check` diffs the stack against live Kubernetes state (kubectl get -o json); `apply` converges live state for resources in the stack's namespace that carry the adapter's own ownership marker, label `shx.dev/managed-by=<:stack/scope>` (and `app.kubernetes.io/managed-by=shx`), written only by this adapter. `app.kubernetes.io/part-of` is descriptive and never authorises deletion and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

## Acceptance criteria

- [ ] GIVEN truncated, malformed or version-shifted live output WHEN `check` or `apply` runs THEN the target live-state contract rejects it with a path and no mutation occurs; a valid empty response remains distinguishable.
- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_k8s.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the k8s golden file.
- [ ] GIVEN a stack using a feature Kubernetes cannot express WHEN rendered THEN the unsupported-feature report equals its k8s golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN a resource with `app.kubernetes.io/part-of=<scope>` but without `shx.dev/managed-by=<scope>` WHEN `apply` runs THEN it is untouched, even though it matches the application label.
- [ ] GIVEN a resource with `shx.dev/managed-by=<scope>` but missing or mismatching `app.kubernetes.io/managed-by=shx` WHEN `apply` runs THEN it remains untouched. Both labels and the expected namespace must authorize every mutation, not only deletion; missing ownership never defaults to owned.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_k8s.cljc`, `src/shx/infra/supervisor_k8s.clj`, golden files under `test/resources/supervisor/k8s/`
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_k8s.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_k8s_test.clj` (goldens) and `test/shx/infra/supervisor_k8s_test.clj`: `check` and `apply` against stubbed process I/O, covering the zero-diff, one-changed-unit, in-scope-removal and out-of-scope-preservation criteria without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: adapter now depends on supervisor-ir-live-state-law and fails closed on invalid external payloads before check/apply; commit pending.

---
