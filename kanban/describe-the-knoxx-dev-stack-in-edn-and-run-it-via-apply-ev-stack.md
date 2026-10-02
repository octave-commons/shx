---
category: "kanban"
labels: "shx, hexis, knoxx"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790903197039-0.t434b1k6ixlqozwdti"
points: "2"
title: "Describe the Knoxx dev stack in EDN and run it via apply"
priority: "P1"
status: "incoming"
uuid: "supervisor-ir-knoxx-dev-stack"
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

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.
---