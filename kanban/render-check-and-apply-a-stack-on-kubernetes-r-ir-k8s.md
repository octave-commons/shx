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

A stack renders to Deployment and Service manifests; `check` and `apply` obtain separate namespace-scoped inventories using `kubectl get deployments -n <namespace> -o json` and `kubectl get services -n <namespace> -o json`. Both lists must succeed, pass the target live-state contract and be complete before planning removals; if the adapter consumes paginated results directly, it must collect every page of each kind. `check` diffs the stack against that validated inventory; `apply` converges live state for resources in the stack's namespace that carry the adapter's own ownership marker, label `shx.dev/managed-by=<target-validated :stack/scope>` and `app.kubernetes.io/managed-by=shx`, written only by this adapter. `app.kubernetes.io/part-of` is descriptive and never authorises deletion; every resource outside the owned scope remains untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

## Acceptance criteria

- [ ] GIVEN a target-neutral valid stack WHEN `render`, `check` or `apply` targets Kubernetes THEN validate `:stack/scope` as a nonempty label value of at most 63 characters, beginning/ending with an ASCII letter or digit and containing only ASCII letters, digits, `-`, `_` and `.` between them. Invalid examples `Team A`, `team/service`, `-svc`, `svc-` and a 64-character scope fail with a field path before process invocation or mutation; valid `Svc.prod_2` passes. Do not silently normalize or truncate scopes.
- [ ] GIVEN truncated, malformed or version-shifted live output WHEN `check` or `apply` runs THEN the target live-state contract rejects it with a path and no mutation occurs; a valid empty response remains distinguishable.
- [ ] GIVEN either the Deployment or Service list is failed or omitted while the other list succeeds with zero items WHEN `check` or `apply` runs THEN inventory is rejected, no removals are planned and no mutation occurs. Cover both kinds as the failed and omitted list; never substitute an empty list for missing or failed evidence.
- [ ] GIVEN successful, complete and schema-valid Deployment and Service lists both containing zero items in the expected namespace WHEN `check` or `apply` reads inventory THEN it establishes zero live resources rather than a read failure.
- [ ] GIVEN directly paginated Deployment and Service list fixtures with an owned stale resource on a later page WHEN `check` or `apply` runs THEN every continuation is followed until an empty `metadata.continue` value for each kind, and the later-page resource appears in the validated inventory and removal plan. A missing or failed later page for either kind rejects the entire inventory and permits no removals or mutation.
- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_k8s.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the k8s golden file.
- [ ] GIVEN a stack using a feature Kubernetes cannot express WHEN rendered THEN the unsupported-feature report equals its k8s golden file, and nothing is silently dropped.
- [ ] GIVEN live state equal to the stack WHEN `check` runs THEN it reports zero diff; GIVEN one changed unit THEN it reports exactly that unit.
- [ ] GIVEN a resource with `app.kubernetes.io/part-of=<scope>` but without `shx.dev/managed-by=<scope>` WHEN `apply` runs THEN it is untouched, even though it matches the application label.
- [ ] GIVEN a resource with `shx.dev/managed-by=<scope>` but missing or mismatching `app.kubernetes.io/managed-by=shx` WHEN `apply` runs THEN it remains untouched. Immediately before every planned mutation, re-read and validate live ownership for that resource: both labels and the expected namespace must still authorize it, not only deletion; missing ownership never defaults to owned. For a planned create, require fresh confirmed absence in the expected namespace and render both ownership labels; a failed read or an existing resource outside the owned scope permits no mutation.
- [ ] GIVEN a hypothetical resource inventoried as owned WHEN either ownership label becomes missing/mismatched or the fresh read returns a different namespace before its planned mutation THEN `apply` sends no mutation for that resource and reports the ownership change or invalid recheck. Cover both Deployment and Service fixtures and each label/namespace independently, with one read immediately before each planned update, deletion or create. A multi-resource plan must recheck each resource rather than reuse the first resource's result; a failed or malformed recheck also sends no affected mutation. These are future stubbed-I/O acceptance fixtures, not evidence of a live Kubernetes move or executed adapter behavior.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_k8s.cljc`, `src/shx/infra/supervisor_k8s.clj`, golden files under `test/resources/supervisor/k8s/`
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_k8s.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_k8s_test.clj` (goldens) and `test/shx/infra/supervisor_k8s_test.clj`: `check` and `apply` against stubbed process I/O, covering separate namespace-scoped Deployment/Service queries, either kind failed/omitted, both kinds genuinely empty, all pages and later-page failure for either kind, fresh per-mutation ownership/namespace rechecks and failed rechecks, zero-diff, one-changed-unit, in-scope-removal and out-of-scope-preservation without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- [Kubernetes label-value constraints](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/) — checked 2026-10-03; both ownership labels and the namespace remain required.
- [Kubernetes list pagination](https://kubernetes.io/docs/reference/using-api/api-concepts/#retrieving-large-results-sets-in-chunks) — direct list clients follow `metadata.continue` until the complete collection is retrieved.
- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: adapter now depends on supervisor-ir-live-state-law and fails closed on invalid external payloads before check/apply.

---
