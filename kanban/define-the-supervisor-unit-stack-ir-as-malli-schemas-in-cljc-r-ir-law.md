---
category: "kanban"
labels: "shx, hexis, design"
parent: "shx-kanban-supervisor-ir"
type: "task"
write-id: "1790903195733-0.tegvvab9ddjeg8lw8p"
points: "3"
title: "Define the supervisor unit/stack IR as Malli schemas in .cljc"
priority: "P1"
status: "incoming"
uuid: "supervisor-ir-law"
created_at: "2026-10-01T23:34:27.999Z"
---

# Define the supervisor unit/stack IR as Malli schemas in .cljc

## Outcome

`shx.law.supervisor` (`.cljc`) defines a registry-keyed Malli schema for a unit (process intent) and a stack (named units, profiles, owned-scope declaration), and `explain-stack` rejects malformed stacks with a path to the bad field.

## Context

First child of `shx-kanban-supervisor-ir`. Every emitter and adapter consumes this shape, so it lands first. The process command reuses the existing `:exec` node from `shx.law.ir` rather than redefining it.

## Acceptance criteria

- [ ] GIVEN a unit with `:exec` (whose existing `:dir` and `:env` fields, `src/shx/law/ir.clj:23-27`, are the **only** working-directory and environment authority), `:ports`, `:needs`, `:health`, `:restart`, `:replicas`, optional `:image`, `:volumes`, `:user` WHEN validated THEN it passes.
- [ ] Optional fields may be absent but not nil. Pin these target-neutral shapes with at least one valid and invalid fixture each: `:ports` is a vector of unique endpoint maps with integer `:port` in 1–65535, optional integer `:publish` in 1–65535 and `:protocol` in `#{:tcp :udp}`; `:needs` is a vector of distinct nonblank declared unit names; `:health` is a map with nonempty string-vector `:argv`, positive integer `:interval-ms`, `:timeout-ms` and `:retries`; `:restart` is `:never`, `:on-failure` or `:always`; `:replicas` is a positive integer; `:image` and `:user` are nonblank strings; `:volumes` is a vector of maps with nonblank string `:source`, absolute string `:target` and boolean `:read-only?`. Reject unknown keys and wrong types. Health probes inherit the process `:exec` working directory/environment; these fields cannot introduce another authority. Target adapters report unsupported features explicitly.
- [ ] GIVEN a unit that carries its own `:cwd` or `:env` WHEN validated THEN validation fails, naming the key and pointing at `:exec`'s `:dir`/`:env`, so no adapter has to pick a precedence.
- [ ] GIVEN a stack WHEN validated THEN it is a closed map with required `:stack/units` and `:stack/scope`, and optional `:stack/profiles`, with these concrete shapes and fixtures:
  - `:stack/units` is an ordered vector (including `[]`) of complete unit maps satisfying the unit criteria above, each with required nonblank string `:name`; names are unique. This is the same `:name` used by merge-by-name and `:needs`, not a separate identity or a map keyed by name.
  - `:stack/scope` is a required nonblank string such as `"svc"`; the existing scope-rewrite/adapter ownership criteria determine how it scopes resource names. Missing, nil, blank or non-string scope is invalid.
  - `:stack/profiles` is optional; absence and `{}` both mean no profile patches, while nil is invalid. When present it is a map from keyword modes such as `:prod` to closed patch maps with optional `:stack/units`, an ordered vector of unit patches. An empty patch `{}` or `{:stack/units []}` is a no-op; an explicit nil unit vector is invalid. Each unit patch requires `:name` and may supply a subset of the other unit fields, with the same value shapes or the merge-law `::remove` sentinel for removals. No patch can remove/nil the unit identity. Existing units retain unspecified fields; a newly introduced name must yield a complete valid unit after merging. Validate the assembled stack, including cross-unit `:needs`, after selected-profile composition; selecting `:dev` never implicitly applies `:base`.

  Valid fixture `s` (also valid with `:stack/profiles` omitted or `{}`):

  ```clojure
  {:stack/scope "svc"
   :stack/units [{:name "api"
                  :exec [:exec {:argv ["node" "server.js"]}]
                  :ports [] :needs []
                  :health {:argv ["true"] :interval-ms 1000
                           :timeout-ms 1000 :retries 1}
                  :restart :on-failure :replicas 1}]
   :stack/profiles {:prod {:stack/units [{:name "api" :replicas 2}]}}}
  ```

  Fixture assertions pin `explain-stack`'s offending data paths: `(dissoc s :stack/units)` and `(assoc s :stack/units nil)` or `{:api {}}` instead of the vector fail at `[:stack/units]`; a missing/nil/blank unit name fails at `[:stack/units 0 :name]`, and a duplicate second name at `[:stack/units 1 :name]`. `(dissoc s :stack/scope)` and scope values nil, `""`, `" "` or `:svc` fail at `[:stack/scope]`. Omitting `:stack/profiles` passes; profiles nil or `[]` fail at `[:stack/profiles]`, `{:prod []}` fails at `[:stack/profiles :prod]`, `{:prod {}}` passes as a no-op, `{:prod {:stack/units nil}}` fails at `[:stack/profiles :prod :stack/units]`, and a patch missing its name fails at `[:stack/profiles :prod :stack/units 0 :name]`. Unknown stack/profile-patch keys are rejected at their own paths; no missing or nil required field is silently defaulted.
- [ ] GIVEN a unit whose `:needs` names an undeclared unit WHEN validated THEN validation fails naming both units.
- [ ] VERIFY: no schema name collides with a Katamorph-owned name.
- [ ] GIVEN `heretic.edn` WHEN `bb mutate` runs THEN it scans `.cljc` sources too (today it scans only `.clj`, `heretic.edn:23`), and its report shows a nonzero **killed** count from executable code in `shx.law.supervisor`, such as the cross-unit `:needs` and `:cwd`/`:env` rejection functions. Top-level Malli schema literals are never mutated (`docs/mutation-testing.md:127-142`, carded as `heretic-schema-literal-attribution`), so for each schema constraint a test must assert that an input violating it is rejected. A report with zero killed mutants fails this criterion.

## Verification

```bash
bb check
bb mutate   # nonzero KILLED mutants in shx.law.supervisor executable code, none surviving
```

## Scope

- `src/shx/law/supervisor.cljc`, `test/shx/law/supervisor_test.clj`
- `heretic.edn` and `bin/mutate` (plus any Heretic dependency change in `deps.edn`): needed for the `.cljc` mutation-scan criterion

## Reference points

- `src/shx/law/ir.clj` — registry style and `explain-program` error shape.

## Anti-patterns

- Do not encode target-specific keys (pm2 `exec_mode`, k8s `spec`) in the IR; targets map from the IR, not into it.

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.
---
