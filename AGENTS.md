# shx — Agent Guide

Common IR for shell intent (bash <-> Clojure) plus envm, EDN-driven shell
environment config. JVM Clojure, malli at the boundaries, typed.clojure for
pure logic.

## Commands

```bash
bb check      # full gate: lint + typecheck + test — must stay green
bb lint       # clj-kondo, zero warnings contract
bb typecheck  # typed.clojure over checked namespaces
bb test       # kaocha
```

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

## Quality contract

- Zero warnings: clj-kondo, tests, everything. Warnings are failed contracts.
- Tool versions pinned in CI. Bump deliberately; fix what the new version
  finds; commit both together.
- Failure semantics at the shell boundary: warn and degrade, never wedge a
  login shell.
- The `:raw` escape hatch means translation can never do worse than running
  the original. The raw-node ratio of real corpus files is the translation
  fidelity metric; it only goes down.
