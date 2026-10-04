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

The four logical units `knoxx-shadow`, `knoxx-backend`, `knoxx-frontend` and `knoxx-ingestion` on host `stealth` are described in one EDN stack pointing at the Foresight `knoxx` checkout. `apply` starts their encoded physical pm2 names under the declared stack ownership prefix and `check` reports zero diff for that owned inventory. Legacy raw process names remain outside this ownership authority and are reported for separate user handling before retiring their old configuration.

## Context

Observed 2026-10-01: the running Knoxx dev processes use an Aug-01 checkout at `~/devel/orgs/open-hax/openplanner/packages/agents/knoxx`; `knoxx-backend` and `knoxx-ingestion` were stopped while `knoxx-shadow` and `knoxx-frontend` ran. Needs `supervisor-ir-pm2`.

## Acceptance criteria

- [ ] GIVEN the EDN stack WHEN rendered for pm2 THEN every app's `cwd` is under the Foresight `knoxx` checkout.
- [ ] GIVEN `apply` on stealth THEN all four rendered physical apps in the declared owned scope are online and `check` reports zero diff.
- [ ] VERIFY: pm2 apps outside the stack's scope (sol, muse, mnemosyne, bitch-tracker, shoedelussy) are unchanged before and after.
- [ ] GIVEN the complete live pm2 inventory recorded before `apply` THEN classify every app by the stack's encoded physical ownership prefix from `supervisor-ir-pm2`, `shx-<lowercase hex of the exact UTF-8 :stack/scope bytes>--` (for scope `"knoxx"`, `shx-6b6e6f7878--`). Each undeclared app within that prefix is either added to the stack or approved for removal by the user before `apply`. Raw `knoxx-` names, including `knoxx-stt-npu` if present, are diagnostic matches only: apps outside the encoded prefix remain untouched and are reported for separate user handling, even if a removal is requested. After `apply`, only the owned inventory must equal the declared rendered physical names.
- [ ] GIVEN a hypothetical inventory with a declared owned app, an undeclared app under `shx-6b6e6f7878--`, raw `knoxx-stt-npu` and an app owned by another encoded scope WHEN the future adapter reconciles THEN only the declared/approved owned set can change; both outside-scope apps remain unchanged and are reported separately. This is a planned fixture requirement, not a live process probe.

## Verification

```bash
pm2 jlist | jq -r '.[] | [.name, .pm2_env.status, .pm2_env.pm_cwd] | @tsv'
```

## Anti-patterns

- Do not restart Knoxx processes without the user's explicit go-ahead (knoxx/AGENTS.md).

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

---
