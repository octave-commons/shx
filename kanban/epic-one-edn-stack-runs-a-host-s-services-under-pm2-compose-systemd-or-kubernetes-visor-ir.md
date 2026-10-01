---
uuid: "shx-kanban-supervisor-ir"
title: "EPIC: one EDN stack runs a host's services under pm2, compose, systemd or Kubernetes"
status: "incoming"
type: "epic"
priority: "P1"
points: "13"
labels: "shx, hexis, design"
parent: "shx-kanban-hexis-unification-epic"
category: "kanban"
write-id: "1790897662395-0.hjdqvl8auvkfllf39rt"
created_at: "2026-10-01T23:34:22.395Z"
---

# EPIC: One EDN stack runs a host's services under pm2, compose, systemd or Kubernetes

## Outcome

A host's process supervision is described once as an EDN stack. shx/Hexis renders it to pm2, docker compose, systemd user units or Kubernetes manifests; `check` reports the diff between the stack and live state; `apply` converges live state inside each adapter's owned scope and touches nothing outside it.

## Context

- 2026-10-01 inventory of host `stealth` (observed: `pm2 jlist`, `docker compose ls`): 13 pm2 apps and 11 compose projects, defined in at least eight hand-written files across `~/devel`, `~/spaces` and `~/.local/share/promethean`, several pointing at stale checkouts.
- Predecessor: the clobber / pm2-clj DSL, recovered from `riatzukiza/devel@01d0f7200^` into `open-hax/foresight` `clobber/` (open-hax/foresight#120; see `clobber/PROVENANCE.md`).
- User decisions (2026-10-01): build on the unified Hexis substrate; authority is render, check **and** apply; targets are pm2, compose, systemd and Kubernetes; clobber was copied whole into Foresight, and the port into shx carries its supported semantics only, with the merge-by-name bug fixed first.

## The core identity

A running host is a projection of a stack, the way `.bashrc` is a projection of `env.edn`: same fold, different emitter.

## Children

- `supervisor-ir-law` — the Malli unit/stack IR in `.cljc` — **hard blocker for the rest**
- `supervisor-ir-merge-law` — clobber's merge and prototype semantics as pure `.cljc`, bug fixed — blocks the four target cards
- `supervisor-ir-pm2` — pm2 emitter, `check`, `apply`
- `supervisor-ir-compose` — docker compose emitter, `check`, `apply`
- `supervisor-ir-systemd` — systemd user-unit emitter, `check`, `apply`
- `supervisor-ir-k8s` — Kubernetes emitter, `check`, `apply`
- `supervisor-ir-knoxx-dev-stack` — the Knoxx dev stack in EDN, replacing its hand-written pm2 ecosystem — needs `supervisor-ir-pm2`

The whole epic is blocked by `shx-kanban-port-shx-to-cljc` and `shx-kanban-hexis-assembler`.

## Definition of done

The Knoxx dev stack on `stealth` runs from one EDN stack via `apply`, and `check` reports zero diff against live pm2 state.

## Verification

```bash
bb check
bb mutate   # law/domain/shape survivors reviewed
```

## Out of scope

- Secrets and environment-file management (services repository).
- Remote/multi-host orchestration (the mesh); this epic is one host at a time.
