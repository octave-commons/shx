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

`shx.law.supervisor` (`.cljc`) defines separate registry-keyed Malli contracts for composition input and materialized unit/stack IR. Composition consumes prototypes, ordered fragments and the selected profile before `explain-stack` validates the closed IR (named units and owned scope), rejecting malformed data with a path to the bad field.

## Context

First child of `shx-kanban-supervisor-ir`. Every emitter and adapter consumes this shape, so it lands first. The process command reuses the existing `:exec` contract, currently defined in CLJ-only `shx.law.ir`, through one shared portable `shx.law.exec` (`.cljc`) schema boundary. Both the existing IR registry and `shx.law.supervisor` consume that boundary; the supervisor never requires the CLJ-only namespace or copies its schema.

## Acceptance criteria

- [ ] GIVEN the shared `ExecArgs` and `:shx/exec` schema definitions extracted to `src/shx/law/exec.cljc` WHEN the existing IR registry and supervisor schema validate the same exec fixtures THEN both consume those definitions without changing the existing contract. Run the portable exec/supervisor fixtures on JVM Clojure, nbb and a compiled shadow-cljs test artifact; each target must report nonempty passing tests for valid argv/dir/env, empty argv, wrong field types and the same offending data paths. A CLJ-only transitive dependency, unavailable runner, failed compilation or empty suite fails portability; an unexecuted target is not evidence of support.
- [ ] GIVEN a unit with `:exec` (whose existing `:dir` and `:env` fields, `src/shx/law/ir.clj:23-27`, are the **only** working-directory and environment authority), `:ports`, `:needs`, `:health`, `:restart`, `:replicas`, optional `:image`, `:volumes`, `:user` WHEN validated THEN it passes.
- [ ] Optional fields may be absent but not nil. Pin these target-neutral shapes with at least one valid and invalid fixture each: `:ports` is a vector of unique endpoint maps with integer `:port` in 1–65535, optional integer `:publish` in 1–65535 and `:protocol` in `#{:tcp :udp}`; `:needs` is a vector of distinct nonblank declared unit names; `:health` is a map with nonempty string-vector `:argv`, positive integer `:interval-ms`, `:timeout-ms` and `:retries`; `:restart` is `:never`, `:on-failure` or `:always`; `:replicas` is a positive integer; `:image` and `:user` are nonblank strings; `:volumes` is a vector of maps with nonblank string `:source`, absolute string `:target` and boolean `:read-only?`. Reject unknown keys and wrong types. Health probes inherit the process `:exec` working directory/environment; these fields cannot introduce another authority. Target adapters report unsupported features explicitly.
- [ ] GIVEN a unit that carries its own `:cwd` or `:env` WHEN validated THEN validation fails, naming the key and pointing at `:exec`'s `:dir`/`:env`, so no adapter has to pick a precedence.
- [ ] GIVEN a materialized stack WHEN `explain-stack` validates it THEN it is a closed map with exactly required `:stack/units` and `:stack/scope`, with these concrete shapes and fixtures:
  - `:stack/units` is an ordered vector (including `[]`) of complete unit maps satisfying the unit criteria above, each with required nonblank string `:name`; names are unique. This is the same `:name` used by merge-by-name and `:needs`, not a separate identity or a map keyed by name.
  - `:stack/scope` is a required nonblank string such as `"svc"`; the existing scope-rewrite/adapter ownership criteria determine how it scopes resource names. Missing, nil, blank or non-string scope is invalid.
  - Composition metadata (`:extends`, `:mixins`, `:stack/profiles` and `:composition/*`) is input only and is rejected if it reaches the materialized stack or units. Partial patches are not complete units and are not passed to `explain-stack` before assembly.

  Valid materialized fixture `s`:

  ```clojure
  {:stack/scope "svc"
   :stack/units [{:name "api"
                  :exec [:exec {:argv ["node" "server.js"]}]
                  :ports [] :needs []
                  :health {:argv ["true"] :interval-ms 1000
                           :timeout-ms 1000 :retries 1}
                  :restart :on-failure :replicas 1}]}
  ```

  Fixture assertions pin `explain-stack`'s offending data paths: `(dissoc s :stack/units)` and `(assoc s :stack/units nil)` or `{:api {}}` instead of the vector fail at `[:stack/units]`; a missing/nil/blank unit name fails at `[:stack/units 0 :name]`, and a duplicate second name at `[:stack/units 1 :name]`. `(dissoc s :stack/scope)` and scope values nil, `""`, `" "` or `:svc` fail at `[:stack/scope]`. Unknown stack/unit keys are rejected at their own paths; no missing or nil required field is silently defaulted.
- [ ] GIVEN prospective composition input WHEN validated at the shared `shx.law.supervisor` boundary THEN it has this closed, EDN-readable shape, separate from materialized IR:
  - Required `:composition/base` is a closed map with required `:stack/scope` and ordered `:stack/units` as above. Base units have complete field shapes and unique names; cross-unit references are checked after composition.
  - Optional `:composition/prototypes` is a map from nonblank string IDs to closed unit-field patches without `:name`. A patch can carry optional `:extends` (one prototype ID) and `:mixins` (an ordered vector of prototype IDs); resolve only this supplied map, reject missing references and cycles, and never consult a global registry or evaluate code.
  - Optional `:composition/fragments` is an ordered vector of closed stack patches. A stack patch is `{}` or has only `:stack/units`, an ordered vector of unit patches with required nonblank unique `:name` within that fragment. Unit patches can carry the same `:extends`/`:mixins` directives and a subset of unit fields, each with its declared value shape or the exact qualified removal sentinel. No patch may remove/nil identity. Names may recur across fragments for merge-by-name.
  - Optional `:composition/profiles` is a map from keyword modes to ordered vectors of those same stack patches, including multiple fragments for one mode. Optional `:composition/selected-profile` is a keyword present in that map; absence selects no profile. Selecting `:dev` never implicitly selects `:base`. Absent optional collections mean `{}`/`[]`; explicit nil, wrong container types and unknown keys fail at their input paths. `{}` and `{:stack/units []}` are no-op fragments.
- [ ] GIVEN valid composition input WHEN materialized by `shx.domain.supervisor-merge` THEN consume it in this explicit order: validate input shapes and prototype references; realize each patch's recursive `:extends`, then its `:mixins` left-to-right, then its own non-directive fields; fold the base and ordinary fragments left-to-right using merge-by-name in first-seen order; fold only the selected profile's fragments left-to-right; consume all composition/directive metadata; rewrite scoped names and `:needs` together; finally call closed `explain-stack`, including complete-unit and cross-unit checks. A newly introduced unit must be complete at that final boundary; removed required fields fail there. The shared input contract owns validation and the existing merge-law card owns composition; no second IR or merge implementation is introduced.

  Future fixture input (plain EDN):

  ```clojure
  {:composition/base
   {:stack/scope "svc"
    :stack/units [{:name "api"
                   :exec [:exec {:argv ["node" "api.js"] :env {:MODE "base"}}]
                   :ports [] :needs []
                   :health {:argv ["true"] :interval-ms 1000
                            :timeout-ms 1000 :retries 1}
                   :restart :never :replicas 1}]}
   :composition/prototypes {"parent" {:restart :always :replicas 2}
                            "m1" {:replicas 3 :image "demo:1"}
                            "m2" {:image "demo:2" :user "1000"}}
   :composition/fragments
   [{:stack/units [{:name "api" :extends "parent" :mixins ["m1" "m2"]}]}
    {:stack/units [{:name "api" :volumes []}]}]
   :composition/profiles
   {:base [{:stack/units [{:name "api" :user "base-only"}]}]
    :prod [{:stack/units [{:name "api" :replicas 4}]}
           {:stack/units [{:name "api" :replicas 5}]}]}
   :composition/selected-profile :prod}
  ```

  Required future materialized result:

  ```clojure
  {:stack/scope "svc"
   :stack/units [{:name "svc-api"
                  :exec [:exec {:argv ["node" "api.js"] :env {:MODE "base"}}]
                  :ports [] :needs []
                  :health {:argv ["true"] :interval-ms 1000
                           :timeout-ms 1000 :retries 1}
                  :restart :always :replicas 5
                  :image "demo:2" :user "1000" :volumes []}]}
  ```

  Store these as future `composition-input.edn` / `composition-materialized.edn` goldens and assert exact equality on all three portable targets. Invalid-input fixtures must cover missing base; nil/non-vector fragments; a profile value supplied as a single patch instead of a vector; missing/duplicate patch names; scalar `:mixins`; unknown/cyclic prototype references; unknown or non-keyword selected profile; unknown keys; identity removal/nil; and removal of required `:exec` rejected at final IR validation. Assert input paths for input failures and final IR paths for assembled-unit failures. Passing an unresolved directive directly to the closed unit schema must fail, while this valid input succeeds through composition first. These fixtures are requirements, not executed tests in this planning PR.
- [ ] GIVEN the EDN profile patch `{:stack/units [{:name "api" :image :shx.domain.supervisor-merge/remove}]}` WHEN read with `clojure.edn/read-string` THEN the removal value is that exact qualified keyword; WHEN composed over an `api` unit with an image THEN the image key is absent, the unit identity is preserved and the assembled stack is validated. No namespace-local auto-resolved reader syntax is required in an EDN file.
- [ ] GIVEN a unit whose `:needs` names an undeclared unit WHEN validated THEN validation fails naming both units.
- [ ] VERIFY: no schema name collides with a Katamorph-owned name.
- [ ] GIVEN `heretic.edn` WHEN `bb mutate` runs THEN it scans `.cljc` sources too (today it scans only `.clj`, `heretic.edn:23`), and its report shows a nonzero **killed** count from executable code in `shx.law.supervisor`, such as the cross-unit `:needs` and `:cwd`/`:env` rejection functions. Top-level Malli schema literals are never mutated (`docs/mutation-testing.md:127-142`, carded as `heretic-schema-literal-attribution`), so for each schema constraint a test must assert that an input violating it is rejected. A report with zero killed mutants fails this criterion.

## Verification

```bash
bb check
bb mutate   # nonzero KILLED mutants in shx.law.supervisor executable code, none surviving
```

The implementation must also declare and run the JVM, nbb and compiled shadow-cljs portable fixture targets above, retaining commands, test counts and failures. The current JVM-only project configuration does not supply the latter two targets; this planning change does not claim they already run.

## Scope

- `src/shx/law/exec.cljc`, `src/shx/law/ir.clj` (delegate the existing exec definitions), `src/shx/law/supervisor.cljc`; portable exec/supervisor tests under `test/shx/law/` and the dependency/test-runner declarations needed for JVM, nbb and shadow-cljs
- `heretic.edn` and `bin/mutate` (plus any Heretic dependency change in `deps.edn`): needed for the `.cljc` mutation-scan criterion

## Reference points

- `src/shx/law/ir.clj` — registry style and `explain-program` error shape.

## Anti-patterns

- Do not encode target-specific keys (pm2 `exec_mode`, k8s `spec`) in the IR; targets map from the IR, not into it.

---
Body revised while incoming, during planning review on octave-commons/shx#2 (commits 3ca71e4, 4c5ae88, b24eb9f; see the settled review threads). The task-created event holds the original body; the Markdown body is the current contract.

---
