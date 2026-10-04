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

A stack renders to compose YAML; `check` compares desired configuration from `docker compose -f <rendered-stack.yml> -p <scope> config` with validated live configuration from inspection of every container in `docker compose -f <rendered-stack.yml> -p <scope> ps --all --orphans=true --no-trunc --format json`. `config` renders supplied desired files; `ps` inventories identity/status/ports, so neither is evidence of actual configured environment or volumes. Every Compose operation in `check` and `apply` uses the same explicitly supplied absolute rendered-file path and target-validated `:stack/scope` project name; `apply` converges only proven owned resources for that project and leaves every resource outside that scope untouched.

## Context

Child of `shx-kanban-supervisor-ir`; consumes `supervisor-ir-law`, `supervisor-ir-live-state-law` and `supervisor-ir-merge-law`. Emitter is pure (`shape/`); live reads and writes are `infra/` adapters. Validate the complete target-specific live-state payload before computing a diff or any destructive action; an invalid or truncated response must fail closed.

The existing `shx.law.supervisor-live` contract owns validation and pure normalization of the combined inventory/inspection evidence, not a second adapter-local validator. Pin supported Compose/Engine output versions and retain source-to-field coverage. `docker container inspect <id>...` is an Engine operation, not a Compose command: bind it to the same Docker endpoint/context and exact full IDs from the selected project's validated inventory; do not invent `-f`/`-p` flags for it.

## Acceptance criteria

- [ ] GIVEN a target-neutral valid stack whose scope contains spaces, a slash, an initial dash or uppercase characters WHEN `render`, `check` or `apply` targets Compose THEN scope validation rejects it with a field path before any process invocation or mutation. Accept exactly `[a-z0-9][a-z0-9_-]*` as the nonempty Compose project name; test invalid examples `Team A`, `team/service`, `-svc` and valid `svc-prod_2`. Do not silently normalize two scopes to one project.
- [ ] GIVEN truncated, malformed or version-shifted live output WHEN `check` or `apply` runs THEN the target live-state contract rejects it with a path and no mutation occurs; a valid empty response remains distinguishable.
- [ ] VERIFY: `heretic.edn` `:exclude-files` lists `src/shx/infra/supervisor_compose.clj`, and `bb mutate` reports no no-coverage sites in that file.
- [ ] GIVEN the shared fixture stack (features every target supports) WHEN rendered THEN output equals the compose golden file.
- [ ] GIVEN a stack using a feature docker compose cannot express WHEN rendered THEN the unsupported-feature report equals its compose golden file, and nothing is silently dropped.
- [ ] GIVEN desired `config`, complete `ps --all --orphans=true --no-trunc --format json` inventory and one successful inspection per full container ID WHEN normalized THEN every inventory ID occurs exactly once in inspection evidence, including replicas and exited orphans. Validate inspection `Id` and actual `Config.Labels` project/service identity against inventory `ID`/`Project`/`Service` and the selected project, independently of whether the desired file still declares that service. Missing, duplicate, unexpected or mismatched IDs/project/service, failed inspection, incomplete inventory, changed engine/context or an unsupported response version refuse both diff and mutation. Empty live state is accepted only with a successful complete empty inventory, never from command failure. Version-pinned fixtures cover `ps`'s JSON Lines output; partial parsing is not a complete inventory.
- [ ] GIVEN the combined live evidence WHEN comparing desired and actual units THEN pin a field-by-field normalization/coverage matrix in the shared live-state law: effective argv/entrypoint, working directory, environment, image identity, user and health configuration from inspection `Config` (and resolved image defaults where required); restart policy and configured port bindings from `HostConfig`; actual mount type/source/destination/read-only mode from `Mounts`; published ports/status from inventory and `NetworkSettings`; replica count from the complete ID set. Preserve argv order and environment key/value case, parse environment entries at their first `=`, reject ambiguous duplicate keys, compare maps independent of output order and convert time/port units explicitly. Desired normalization includes verified image defaults rather than treating extra actual defaults as drift. Do not fill missing actual values from the current desired file or treat running status as configuration equality. Every emitted comparison field must have validated actual evidence: configuration not recoverable from inspection (such as orchestration-only dependency intent) requires trusted applied-configuration evidence bound to these exact IDs/engine/project, or an explicit unsupported/unobservable failure before action; a desired render or label hash alone cannot supply that proof. Unknown defaults, mount identity or field coverage fail closed.
- [ ] GIVEN normalized live state equal to the stack WHEN `check` runs THEN it reports zero diff, including a control with reordered environment entries/JSON map keys; GIVEN one changed unit THEN it reports exactly that unit. Concrete future drift fixture: keep `ps` IDs, project/service, running status, health and ports unchanged for owned `api`; desired `config` has `MODE=new` and bind source `/srv/new`, while live inspection retains `MODE=old` and `/srv/old` at the same mount destination. `check` must report only `api`'s environment/mount drift, never zero diff. A failed/missing inspection, project mismatch or unobservable configured field in the same fixture must refuse reconciliation with zero mutations. These are future stubbed-I/O acceptance tests, not tests executed by this planning change.
- [ ] GIVEN a working directory containing an unrelated `compose.yaml` and a rendered stack file elsewhere WHEN `check` and `apply` run against stubbed process I/O THEN every Compose invocation passes `-f` with that rendered file's absolute path and `-p` with the same target-validated scope; `config`, `ps --all --orphans=true --no-trunc --format json` and mutation operations use the rendered stack, never the unrelated file. Inspection argv contains only validated selected-project IDs and uses the same engine/context. Assert captured argv, identity bindings and configuration-dependent fixture results.
- [ ] GIVEN an exited in-scope container that the desired stack no longer declares in the live-state fixture WHEN `check` queries `docker compose -f <rendered-stack.yml> -p <scope> ps --all --orphans=true --no-trunc --format json` THEN the container is included and inspected in the validated inventory and appears in the diff rather than being omitted as if absent. Project/service labels identify candidate resources; they do not independently authorize adopting or deleting an unowned resource.
- [ ] GIVEN a resource inside the owned scope that the stack no longer declares WHEN `apply` runs THEN it is stopped/removed; GIVEN a resource outside the scope THEN it is untouched (both asserted).

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for the emitter namespace, none surviving
```

## Scope

- `src/shx/shape/supervisor_compose.cljc`, `src/shx/infra/supervisor_compose.clj`, golden files under `test/resources/supervisor/compose/`
- Extend the prerequisite `src/shx/law/supervisor_live.cljc` Compose evidence contract/normalization and its version-pinned inventory/inspection fixtures under `test/resources/supervisor/live/compose/`; reuse that shared boundary, with no adapter-local semantic copy.
- `heretic.edn` `:exclude-files`: add `src/shx/infra/supervisor_compose.clj` (path-suffix match, `heretic.edn:30-36`), or it silently joins the permanent no-coverage list.
- `test/shx/shape/supervisor_compose_test.clj` (goldens) and `test/shx/infra/supervisor_compose_test.clj`: `check` and `apply` against stubbed process I/O, covering the zero-diff, unchanged-status/configuration-drift, complete inspection binding/normalization/failure, explicit rendered-file/project selection from an unrelated Compose directory, exited-orphan inventory/diff, in-scope-removal and out-of-scope-preservation criteria without a live supervisor. Heretic excludes `infra/`, so these tests are the only evidence for the safety criteria.

## Reference points

- [Compose project-name constraints](https://docs.docker.com/compose/how-tos/project-name/) — checked 2026-10-03; target validation is separate from the target-neutral IR.
- [Compose file selection](https://docs.docker.com/reference/cli/docker/compose/) — `-f` selects the configuration path; `-p` selects the project name.
- [Compose config](https://docs.docker.com/reference/cli/docker/compose/config/), [Compose ps](https://docs.docker.com/reference/cli/docker/compose/ps/) and [container inspect](https://docs.docker.com/reference/cli/docker/container/inspect/) — checked 2026-10-04: desired rendering, complete stopped/orphan inventory and actual container details are separate evidence sources. Field names are pinned by the [Engine v1.51 specification](https://docs.docker.com/reference/api/engine/version/v1.51.yaml); accepting another version requires its own validated contract/fixtures.
- `src/shx/shape/bash.clj` — emitter dispatch; an unhandled head throws.

## Anti-patterns

- No `:default` emit method that returns nil; unsupported features go into the report.
- `apply` never acts outside the owned scope, even to "clean up".

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: adapter now depends on supervisor-ir-live-state-law and fails closed on invalid external payloads before check/apply.

---