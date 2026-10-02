---
uuid: "supervisor-ir-k8s"
title: "Render, check and apply a stack on Kubernetes"
status: "incoming"
type: "task"
priority: "P2"
points: "3"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897669115-0.u4s1woy0wqdi313pno9"
created_at: "2026-10-01T23:34:29.115Z"
---

# Render, check and apply a stack on Kubernetes

## Outcome

A stack renders to Deployment and Service manifests; `check` diffs the stack against live Kubernetes state (kubectl get -o json); `apply` converges live state for resources in the stack's namespace that carry the adapter's own ownership marker, label `shx.dev/managed-by=<:stack/scope>` (and `app.kubernetes.io/managed-by=shx`), written only by this adapter. `app.kubernetes.io/part-of` is descriptive and never authorises deletion and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters.

## Acceptance criteria

- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_k8s.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the k8s golden file.
- [ ] GIVEN a stack using a feature Kubernetes cannot express WHEN rendered THEN the unsupported-feature report equals its k8s golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN a resource with `app.kubernetes.io/part-of=<scope>` but without `shx.dev/managed-by=<scope>` WHEN `apply` runs THEN it is untouched, even though it matches the application label.
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
