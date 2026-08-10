# shx — Agent Guide

Common IR for shell intent (bash <-> Clojure) plus envm, EDN-driven shell
environment config. JVM Clojure, malli at the boundaries, typed.clojure for
pure logic.

## Commands

```bash
bin/analyze   # THE GATE. Seven blocking checks. Run before reporting anything done.
bin/analyze --fix      # apply what is auto-fixable (cljfmt), then sweep
bb check      # the same thing (delegates to bin/analyze)

bb lint       # clj-kondo, zero warnings contract
bb splint     # splint idiom lint (.splint.edn)
bb fmt        # cljfmt check          (bb fmt:fix rewrites)
bb dead       # clojure-lsp: unused public vars
bb dupes      # jscpd duplication     (.jscpd.json, threshold 0)
bb typecheck  # typed.clojure over checked namespaces
bb test       # kaocha

bb mutate            # heretic mutation testing over the pure layers
bb mutate:survivors  # surviving mutants — real test gaps
bb mutate:gaps       # mutation sites no indexed test reaches

clojure -M:test --focus shx.shape.bash-test                    # one namespace
clojure -M:test --focus shx.shape.bash-test/emit-export-test   # one test

clojure -M:run validate <file>   # CLI: also render | check | emit <bash|bb> <file>
```

`bin/analyze` is the **single definition of the gate** — `bb check` shells out to
it and `.github/workflows/static-analysis.yml` runs `bin/analyze --strict`, so
there is one list to keep true. `test.yml` keeps a standalone fast test signal.
Tool versions are pinned: clj-kondo and clojure-lsp in the workflow, splint and
cljfmt and heretic in `deps.edn`, jscpd in `bin/analyze`.

Details, tiers, and the suppression conventions: `docs/static-analysis.md`.
Mutation testing, and how to read its two very different kinds of "no coverage":
`docs/mutation-testing.md`.

**Mutation testing is the check that cannot be satisfied by a test written to
pass.** If you touched `domain/`, `law/`, or `shape/`, run `bb mutate` and read
the survivor list before claiming a test closed a gap.

## Namespace law (four quadrants, no junk drawers)

| layer | rule |
|---|---|
| `shx.domain/` | pure, zero I/O |
| `shx.infra/` | all I/O lives here |
| `shx.shape/` | pure morphisms: data in, string out |
| `shx.law/` | malli contracts only, no I/O |

No `utils/`, no `helpers/`. Schemas before adapters: a `law/` contract exists
before the `infra/` code that reads it.

## Validation split (load-bearing decision)

- **malli** validates external-world input: EDN files, fragments, IR programs,
  future network calls.
- **typed.clojure** checks pure internal logic. Opt in with `^:typed.clojure`
  ns metadata, then add the ns to the `typecheck` task in `bb.edn` and to
  `.github/workflows/static-analysis.yml`.
- typed.clojure's `Any` is fully strict (not assignable to anything). Do not
  tc-ignore to silence — if the checker can't model it, it's EDN-boundary
  code and belongs to malli. Currently checked: `shx.shape.quote`.

## Two products, one repo

`shx.cli` multiplexes both. They share `shape/quote` and (partly) `shape/bash`
but are otherwise separate pipelines.

**shx** — the IR. Hiccup vectors with keyword heads, defined once in
`shx.law.ir/registry` (a recursive malli registry). `shape/bash` and `shape/bb`
are the two emitters. `:raw` is the escape hatch.

**envm** — EDN shell config. Pipeline, all in `shx.cli/resolved-config`:

```
infra.config/load-config        top-level env.edn — MANDATORY, System/exit 1 on failure
infra.config/read-fragment-tree :merge includes — DEGRADE, warn to stderr, empty content
domain.merge/fold-fragment-tree children first, own :content merged on top
domain.merge/apply-host-overrides  :hosts <hostname> merged on top, :hosts stripped
cli/render-env-bash             bash text for `eval`
```

That asymmetry is deliberate: a broken fragment must never wedge a login shell,
but a missing top-level config is unrecoverable.

## Things you only learn by reading several files

- **Fragment precedence is closer-to-root-wins.** `fold-fragment-tree` folds
  children, then merges the node's own `:content` *on top*. Don't confuse it
  with `fold-fragments`, where later-in-the-seq wins. Both are pinned by tests
  in `test/shx/domain/merge_test.clj`.

- **envm's PATH rendering deliberately does not use the IR.**
  `cli/render-env-bash` filters directories at *render* time via
  `infra.fs/existing-dirs` and emits one collapsed `PATH=` line; the IR's
  `:path/prepend` emits a per-entry runtime `[ -d ... ] &&` guard. Converging
  them is a design decision, not a cleanup — see the docstring at
  `src/shx/cli.clj:31`.

- **`shx.core` does not exist in this repo.** `shape/bb` generates calls to a
  runtime API (`set-env!`, `path-prepend!`, `defalias`, `source-bash`, `test`)
  that is unimplemented. Generated bb code is not runnable for shell-state
  effects yet.

- **Validation happens at `emit-program`, not `emit`.** Both emitters call
  `law.ir/explain-program` and throw `ex-info` with `:type :shx/invalid-program`
  before emitting. Per-node `emit` assumes valid input — which is why tests
  call it directly.

- **`shx.domain.merge` carries `t/ann` annotations but is not typechecked.**
  They are documentation; the gate is malli plus tests. `.clj-kondo/config.edn`
  exists solely to keep kondo quiet about those forms.

## Adding an IR node type

1. `shx.law.ir/registry` — the schema *and* its `:orn` branch in `:shx/node`.
2. `shx.shape.bash/emit` — no `:default` method; an unhandled head throws.
3. `shx.shape.bb/emit` — has a `:default` returning nil, which `emit-program`
   turns into a `# bb emit not implemented for <head>` placeholder. Silent
   degradation, so it will not fail the build.
4. Tests. Current coverage: `shape/bash`, `shape/quote`, `law/ir`,
   `domain/merge`, `infra/config`. **`shape/bb` and `cli` are untested.**
5. `bb mutate`. A test that runs the new branch without asserting on it looks
   identical to a real test in the suite output and is caught here.

## The board

`./kanban`, one markdown file per card, promethean FSM
(`openhax.kanban.edn` › `:fsm`). Read `docs/kanban/writing-cards.md` before
writing a card; templates are in `docs/kanban/templates/`.

Three things that bite:

- **`status` is owned by `rheos move`, not by frontmatter.** Hand-editing it
  bypasses the FSM, and `rheos drift` will report the card.
- **`in_progress -> review` runs `bin/analyze --strict`.** Verified in both
  directions on 2026-08-10 — a red tree is refused with exit 3 and the card does
  not move. You cannot present work for review while the gate fails.
- **A card body is a contract once it leaves `breakdown`.** Updates after that are
  comments (`rheos comment <uuid> --text …`), including the retrospective on a
  card you are closing. Several `done` cards here had their scope overwritten by
  their own write-up; the scope is now unrecoverable.

rheos is not on `PATH` here:
`alias rheos='node ~/spaces/eta-mu/packages/rheos/dist/cli.cjs'`.

## Quality contract

- Zero findings, all seven checks. Not zero *errors* — zero findings. Every check
  in `bin/analyze` was at zero on 2026-08-10, which is the only reason all seven
  are blocking. If one fires it is a blocker, not a backlog item: fix it, add a
  per-site suppression with its reason, or card it — never demote the tier and
  never widen a ratchet to turn red into green.
- Two ratchets: `.jscpd.json` `"threshold": 0` (the tree has no clones) and
  `.splint.edn`'s `naming/lisp-case` exclusion, scoped to `law/` only. A ratchet
  moves in the tightening direction; loosening one is a policy change and needs
  its reason in the same commit.
- Tool versions pinned everywhere. Bump deliberately; fix what the new version
  finds; commit both together.
- Failure semantics at the shell boundary: warn and degrade, never wedge a
  login shell.
- The `:raw` escape hatch means translation can never do worse than running
  the original. The raw-node ratio of real corpus files is the translation
  fidelity metric; it only goes down.
