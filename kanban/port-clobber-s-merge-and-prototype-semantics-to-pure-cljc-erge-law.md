---
category: "kanban"
labels: "shx, hexis"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790907650060-0.e9or7iup6f5e54gz10q"
points: "3"
title: "Port clobber's merge and prototype semantics to pure .cljc"
priority: "P1"
status: "incoming"
uuid: "supervisor-ir-merge-law"
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
- [ ] GIVEN a profile `:prod` that sets `{:replicas 2}` on unit `api` WHEN applied to fixture stack `{api, worker}` THEN `api` has `:replicas 2`, all of `worker` and every other `api` key are unchanged, and the result equals the golden `test/resources/supervisor/merge/profile-prod.edn`.
- [ ] GIVEN unit `api` that `extends` a base unit with `:restart :always` and overrides `:replicas` THEN the result has both, and the override wins (golden `extends.edn`).
- [ ] GIVEN two mixins that set the same key WHEN applied in order THEN the later mixin's value wins (golden `mixins.edn`).
- [ ] GIVEN separate `:base` and `:dev` tier patches WHEN `:dev` is selected through `apply-profile` THEN only the selected `:dev` patch deep-merges over the assembled stack; keys found only in the `:base` patch are not inherited, and `::remove` in `:dev` deletes an existing stack key (source-parity golden `tiers.edn`).
- [ ] GIVEN a combined assembly in the explicit order base (`:replicas 1`), `extends` patch (2), ordered mixin (3), selected-tier profile fragment (4), then a later fragment for that same selected profile (5), WHEN materialized THEN the result has `:replicas 5`, retains noncolliding keys from each step and one unit per name (golden `combined-precedence.edn`). Pin this to the recovered `pm2_clj.dsl/realize-proto`, `with`, `tiers`/profile-fragment composition and `pm2_clj.runtime/apply-profile` at Foresight `fcf53352345ddb64a0a5dd49eab6390ce7f19e81`, correcting only the separately documented merge-by-name/removal bugs. Composition is explicit left-to-right; do not invent a global precedence detached from assembly order.
- [ ] GIVEN `scope "svc"` on units `api` and `worker`, where `api` has `:needs ["worker"]`, WHEN applied THEN names become `svc-api` and `svc-worker`, `api`'s dependency becomes `:needs ["svc-worker"]`, and the resulting stack passes supervisor-law validation. All unit-name references are rewritten together.

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

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

Review round 5 (Codex) on octave-commons/shx#2: scope acceptance now rewrites :needs references along with unit identities; commit pending.

---
