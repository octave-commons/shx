---
uuid: "supervisor-ir-merge-law"
title: "Port clobber's merge and prototype semantics to pure .cljc"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790897668218-0.bcxchxrzc0a43iuoi6a"
created_at: "2026-10-01T23:34:28.218Z"
---

# Port clobber's merge and prototype semantics to pure .cljc

## Outcome

`shx.domain.supervisor-merge` (`.cljc`) implements clobber's deep merge with a `::remove` sentinel, merge-units-by-name, and prototype composition (`extends`, mixins, profiles, tiers, `scope`) as pure functions over data, with the merge-by-name bug fixed and pinned by a regression test.

## Context

Source: `open-hax/foresight` `clobber/src/pm2_clj/merge.cljs` and `clobber/src/pm2_clj/dsl.cljs`. Known defect (observed 2026-10-01): `merge.cljs` defines `(def remove ::remove)` without excluding `clojure.core/remove`, then calls `(remove …)`, invoking the keyword, so merge-by-name is broken.

## Acceptance criteria

- [ ] GIVEN base and override unit vectors sharing a `:name` WHEN merged THEN the result has one unit per name, deep-merged, in first-seen order (regression for the shadowing bug).
- [ ] GIVEN an override value `::remove` WHEN merged THEN the key is absent from the result.
- [ ] GIVEN a profile `:prod` WHEN applied THEN only units the profile names change.
- [ ] GIVEN `scope "svc"` WHEN applied THEN every unit name gains the `svc-` prefix.

## Verification

```bash
bb check
bb mutate   # nonzero mutants generated for shx.domain.supervisor-merge (needs supervisor-ir-law's .cljc scan), none surviving
```

## Scope

- `src/shx/domain/supervisor_merge.cljc`, `test/shx/domain/supervisor_merge_test.clj`

## Reference points

- `src/shx/domain/merge.clj` — the existing fold and precedence conventions.

## Anti-patterns

- No global atom registries and no `eval`; composition is plain functions returning data.
- Do not port the nbb/SCI file evaluation or temp `.cjs` writing.
