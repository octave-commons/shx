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

A stack renders to Deployment and Service manifests; `check` diffs the stack against live Kubernetes state (kubectl get -o json); `apply` converges live state for the namespace and `app.kubernetes.io/part-of=<scope>` label set by `:stack/scope` and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters.

## Acceptance criteria

- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the k8s golden file.
- [ ] GIVEN a stack using a feature Kubernetes cannot express WHEN rendered THEN the unsupported-feature report equals its k8s golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # emitter namespace has no surviving mutants
```

## Scope

- `src/shx/shape/supervisor_k8s.cljc`, `src/shx/infra/supervisor_k8s.clj`, golden files under `test/resources/supervisor/k8s/`

## Reference points

- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".
