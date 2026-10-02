---
uuid: "supervisor-ir-knoxx-dev-stack"
title: "Describe the Knoxx dev stack in EDN and run it via apply"
status: "incoming"
type: "task"
priority: "P1"
points: "2"
labels: "shx, hexis, knoxx"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897669339-0.2mbvq96epumz79p2cfb"
created_at: "2026-10-01T23:34:29.339Z"
---

# Describe the Knoxx dev stack in EDN and run it via apply

## Outcome

`knoxx-shadow`, `knoxx-backend`, `knoxx-frontend` and `knoxx-ingestion` on host `stealth` are described in one EDN stack pointing at the Foresight `knoxx` checkout, started by `apply`, and `check` reports zero diff; `~/devel/services/openplanner/ecosystem.host.config.cjs` is no longer used for them.

## Context

Observed 2026-10-01: the running Knoxx dev processes use an Aug-01 checkout at `~/devel/orgs/open-hax/openplanner/packages/agents/knoxx`; `knoxx-backend` and `knoxx-ingestion` were stopped while `knoxx-shadow` and `knoxx-frontend` ran. Needs `supervisor-ir-pm2`.

## Acceptance criteria

- [ ] GIVEN the EDN stack WHEN rendered for pm2 THEN every app's `cwd` is under the Foresight `knoxx` checkout.
- [ ] GIVEN `apply` on stealth THEN all four apps are online and `check` reports zero diff.
- [ ] VERIFY: pm2 apps outside the stack's scope (sol, muse, mnemosyne, bitch-tracker, shoedelussy) are unchanged before and after.
- [ ] GIVEN the live inventory of every pm2 app whose name starts with `knoxx-` (recorded before `apply`, e.g. also `knoxx-stt-npu` if present) THEN each undeclared match is either added to the stack or approved for removal by the user before `apply`. After `apply`, the `knoxx-` inventory is exactly the declared set.

## Verification

```bash
pm2 jlist | jq -r '.[] | [.name, .pm2_env.status, .pm2_env.pm_cwd] | @tsv'
```

## Anti-patterns

- Do not restart Knoxx processes without the user's explicit go-ahead (knoxx/AGENTS.md).
