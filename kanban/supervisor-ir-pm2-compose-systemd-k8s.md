---
uuid: "shx-kanban-supervisor-ir"
title: "Supervisor IR: describe pm2, docker compose, systemd and Kubernetes in EDN, with render, check and apply"
status: "incoming"
priority: P1
labels: ["tasks","hexis","supervisor","clobber","13sp"]
created_at: "2026-10-01T00:00:00.000Z"
source: "shx/kanban/supervisor-ir-pm2-compose-systemd-k8s.md"
category: "tasks"
points: 13
epic: "shx-kanban-hexis-unification-epic"
blocked_by: ["shx-kanban-port-shx-to-cljc", "shx-kanban-hexis-assembler"]
---

# Supervisor IR

## Context

shx describes shell intent as EDN and emits bash or bb. The same move applies
to process supervision. A host's running services are spread across hand-written
`ecosystem.*.cjs` files, compose files and deploy directories. An inventory
taken on 2026-10-01 found 13 pm2 apps and 11 compose projects on one host,
defined in at least eight files across `~/devel`, `~/spaces` and
`~/.local/share/promethean`, with several still pointing at stale checkouts.

The recovered `clobber` (pm2-clj) DSL is the predecessor. It lives at
`foresight/clobber/` as a consolidation input; see `clobber/PROVENANCE.md`.

## User decisions (2026-10-01)

- Home: built on the unified Hexis substrate, so this waits for
  `port-shx-to-cljc` and `hexis-assembler`.
- Authority: render, check **and** apply.
- Targets: pm2, docker compose, systemd, Kubernetes.
- clobber: port it whole, then refactor.

## Shape

- **law**: a Malli-validated unit/stack IR covering process intent (reusing the
  `:exec` node), cwd, env, ports, dependencies, health, restart policy,
  replicas, image and volumes, user, and profiles.
- **domain**: clobber's merge law (deep merge, `::remove` sentinel,
  merge-by-`:name`) and its prototype semantics (`extends`, mixins, profiles,
  tiers, `scope`), all pure `.cljc`.
- **shape**: one emitter per target: pm2 ecosystem, compose YAML, systemd user
  units, k8s Deployment/Service. Each emitter reports what its target cannot
  express rather than silently dropping it.
- **extern/infra**: pm2, `docker compose`, `systemctl --user` and `kubectl`
  adapters for `check` (diff against live state, as envm does) and `apply`.

## Must not carry over from clobber

- Global atom registries and `eval` of read forms.
- Writing temporary `.cjs` files.
- The `remove` shadowing bug in `pm2_clj/merge.cljs`, which breaks
  merge-by-name. Fix it before porting and cover it with a regression test.

## Definition of Done

- One EDN stack renders to all four targets, and the outputs are golden-tested.
- `check` reports zero diff for a stack after `apply`.
- `apply` refuses to restart processes that are not declared in the stack.
- The Knoxx dev stack (`knoxx-shadow`, `knoxx-backend`, `knoxx-frontend`,
  `knoxx-ingestion`) is described in EDN and replaces
  `~/devel/services/openplanner/ecosystem.host.config.cjs` for that host.
