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

A stack renders to Deployment and Service manifests; `check` and `apply` obtain separate namespace-scoped inventories using `kubectl get deployments -n <namespace> -o json` and `kubectl get services -n <namespace> -o json`. Both lists must succeed, pass the target live-state contract and be complete before planning removals; if the adapter consumes paginated results directly, it must collect every page of each kind. `check` diffs the stack against that validated inventory; `apply` converges live state only for resources in the stack's namespace with proven adapter ownership and both labels `shx.dev/managed-by=<target-validated :stack/scope>` and `app.kubernetes.io/managed-by=shx`. Labels select candidates; they do not prove authority. Mutation requires the active admission control and principal-bound ownership evidence below. `app.kubernetes.io/part-of` is descriptive and never authorises deletion; every resource outside the owned scope remains untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

## Acceptance criteria

- [ ] GIVEN the target cluster/namespace WHEN enabling `apply` THEN require a cluster-admin-controlled ValidatingAdmissionPolicy and binding with `failurePolicy: Fail` and `validationActions: [Deny]`. Match Deployment and Service CREATE/UPDATE requests, including PATCH/apply, whole-metadata updates and restore/recreate paths; compare `object` and `oldObject` against authenticated `request.userInfo`. Only the configured adapter principal may introduce, change, remove or restore either reserved ownership label, or create an object bearing them. Do not infer the principal from object metadata. Untrusted principals must not alter the policy/binding, bypass its matching scope, or impersonate that principal; their mutable labels cannot opt requests out. General permission to update resources is not permission to assert ownership.
- [ ] GIVEN a candidate owned resource WHEN planning or issuing any mutation THEN verify active enforcing policy/binding coverage for the exact cluster, namespace, kind and configured principal, plus trusted admission evidence binding that resource's UID/scope and ownership markers to that principal. Labels on legacy/pre-policy objects or evidence supplied by object metadata alone cannot establish ownership. Missing, unreadable, disabled, warn/audit-only, mismatched or no-longer-active control/evidence fails closed with an explicit error and no mutation; recheck before each write and retain the atomic UID/resourceVersion conditions below. Policy installation and ownership proof are prerequisites, not facts asserted by this planning PR.
- [ ] GIVEN both Deployment and Service fixtures WHEN a non-adapter principal tries CREATE with either/both markers, UPDATE/PATCH adding or changing each marker, removing a protected marker, or restoring/recreating labeled metadata THEN the actual admission-policy test boundary denies the request and records zero accepted ownership changes. Assert forged matching-label resources remain untouched by `apply`; loss of policy/binding or trusted provenance before a planned write also yields no mutation. Retain successful admitted-adapter controls and a pre-policy matching-label refusal. These are future policy and adapter acceptance tests, not implemented or executed checks in this planning change.
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
- [ ] GIVEN a resource with `shx.dev/managed-by=<scope>` but missing or mismatching `app.kubernetes.io/managed-by=shx` WHEN `apply` runs THEN it remains untouched. Immediately before every planned update or deletion, GET that specific name in the expected namespace and validate both ownership labels, namespace, name, UID and nonempty `metadata.resourceVersion`, together with the active control and trusted ownership evidence above; missing ownership never defaults to owned. Bind the authorized read to the write with the API preconditions below, since a fresh read alone does not prevent a concurrent ownership change. A failed or malformed read, or an existing resource outside the owned scope, permits no mutation.
- [ ] GIVEN an authorized fresh read WHEN updating an existing Deployment or Service THEN send one HTTP PATCH with `Content-Type: application/json-patch+json` to that kind's namespaced resource URL. Before the planned field changes, include JSON Patch `test` operations for `/metadata/resourceVersion` and `/metadata/uid` with the exact observed string values, `/metadata/namespace` with the expected namespace, `/metadata/labels/shx.dev~1managed-by` with the validated scope, and `/metadata/labels/app.kubernetes.io~1managed-by` with `"shx"`. Tests and changes must be in the same atomic request; an absent path, failed test, conflict or vanished object refuses the affected change. For deletion, send HTTP DELETE to that same specific resource URL with a DeleteOptions JSON body `{"preconditions": {"uid": "<observed UID>", "resourceVersion": "<observed resourceVersion>"}}`; the API rejects a precondition mismatch with 409 Conflict. Resource versions are passed back unchanged, not parsed as numbers. These are native request-body conditions, not invented `kubectl` flags or a collection-wide delete.
- [ ] GIVEN a planned create WHEN a fresh GET confirms 404 NotFound for that name in the expected namespace THEN POST the named manifest (with both ownership labels and that namespace) to the kind's collection URL: `/apis/apps/v1/namespaces/<namespace>/deployments` or `/api/v1/namespaces/<namespace>/services`. Existing updates/deletes use that collection URL plus `/<name>`. Create is create-only: if the name appears after the GET, 409 AlreadyExists permits no replacement, adoption or apply/upsert fallback. Any conflict or failed precondition stops the affected operation and reports it; a later attempt must obtain new validated ownership/absence evidence and a new plan, never retry the stale write unconditionally.
- [ ] GIVEN a hypothetical resource inventoried as owned WHEN either ownership label becomes missing/mismatched or the fresh read returns a different namespace before its planned mutation THEN `apply` sends no mutation for that resource and reports the ownership change or invalid recheck. Cover both Deployment and Service fixtures and each label/namespace independently, with one read immediately before each planned update, deletion or create. A multi-resource plan must recheck each resource rather than reuse the first resource's result; a failed or malformed recheck also sends no affected mutation. These are future stubbed-I/O acceptance fixtures, not evidence of a live Kubernetes move or executed adapter behavior.
- [ ] GIVEN hypothetical Deployment and Service API stubs WHEN each ownership label is independently removed or changed after the authorized GET but before the update/delete reaches the server (therefore changing `resourceVersion`), or the named resource is deleted and replaced with a new UID, THEN the conditional PATCH/DELETE is rejected and the stub records zero accepted mutations to the changed resource. WHEN a resource outside the owned scope with that same name appears between a create's 404 GET and POST THEN the stub returns 409 AlreadyExists and records no overwrite or fallback request. Cover both kinds and both update/delete operations, retain a successful unchanged-version owned control and an absent-through-POST create control, and assert the exact UID/resourceVersion conditions and error report. These remain future stubbed acceptance fixtures, not executed tests in this planning change.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_k8s.cljc`, `src/shx/infra/supervisor_k8s.clj`, golden files under `test/resources/supervisor/k8s/`
- Admission policy/binding artifacts and their principal-based CREATE/UPDATE/metadata-restore denial tests; adapter fixtures for trusted ownership provenance and missing/inactive-control refusal. Installing those controls on a live cluster is outside this planning PR.
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_k8s.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_k8s_test.clj` (goldens) and `test/shx/infra/supervisor_k8s_test.clj`: `check` and `apply` against stubbed process/API I/O, covering separate namespace-scoped Deployment/Service queries, either kind failed/omitted, both kinds genuinely empty, all pages and later-page failure for either kind, fresh per-mutation ownership/namespace rechecks and failed rechecks, atomic update/delete preconditions, post-read ownership/replacement races and create-only appearance races for both kinds, zero-diff, one-changed-unit, in-scope-removal and out-of-scope-preservation without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- [Kubernetes ValidatingAdmissionPolicy](https://kubernetes.io/docs/reference/access-authn-authz/validating-admission-policy/) — authenticated request identity, old/new object comparison, enforcing binding and fail-closed evaluation; [admission matching](https://kubernetes.io/docs/reference/access-authn-authz/extensible-admission-controllers/#matching-requests-objectselector) cautions against mutable-label opt-out for mandatory controls. Checked 2026-10-04; these mechanisms do not prove an existing cluster is protected.
- [Kubernetes label-value constraints](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/) — checked 2026-10-03; both ownership labels and the namespace remain required.
- [Kubernetes list pagination](https://kubernetes.io/docs/reference/using-api/api-concepts/#retrieving-large-results-sets-in-chunks) — direct list clients follow `metadata.continue` until the complete collection is retrieved.
- [Kubernetes conditional updates](https://kubernetes.io/docs/reference/using-api/api-concepts/#updates-to-existing-resources), [DeleteOptions](https://kubernetes.io/docs/reference/kubernetes-api/definitions/delete-options-v1-meta/) and [Preconditions](https://kubernetes.io/docs/reference/kubernetes-api/definitions/preconditions-v1-meta/) — same-request JSON Patch conditions and deletion UID/resourceVersion preconditions bind mutation to the ownership-authorizing read.
- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: adapter now depends on supervisor-ir-live-state-law and fails closed on invalid external payloads before check/apply.

---
