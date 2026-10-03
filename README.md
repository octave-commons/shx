# shx

A common intermediate representation for shell intent, extractable from and
translatable to both bash and Clojure — plus `envm`, an EDN-driven replacement
for shell rc file accretion.

Worst case, translation falls back to running the original verbatim
(`[:raw {:lang "bash" :text "..."}]` -> bash as-is, `(p/shell "bash" "-c" ...)`
from Clojure). Translation can never do worse than the original.

## The IR

Hiccup-style vectors, keyword heads, two effect families:

| family | nodes |
|---|---|
| shell-state effects | `:export` `:path/prepend` `:path/append` `:alias` `:set` `:source` |
| process effects | `:exec` `:pipe` `:capture` |
| composition | `:if` `:test` |
| escape hatch | `:raw` |

Every node is validated by malli (`shx.law.ir`). Emitters: `shx.shape.bash`
(IR -> bash text) and `shx.shape.bb` (IR -> babashka/Clojure source).

## envm

Shell config as data. `~/.config/envm/env.edn` describes paths, vars, sources,
guarded evals, and per-host overrides; fragments `:merge` other fragments as a
tree-fold, so values always bubble (unlike bash `source` inside `source`).
`render` emits bash for `eval`; `check` diffs the rendered env against the
live one so migration is "run until the diff is empty".

## Layout (namespace law)

| layer | contents | rule |
|---|---|---|
| `shx.domain/` | merge semantics | pure, zero I/O |
| `shx.infra/` | file/process/hostname boundary | all I/O lives here |
| `shx.shape/` | quoting + IR emitters | pure morphisms, data in string out |
| `shx.law/` | malli schemas (IR, config) | contracts only, no I/O |

Validation split: **malli** guards external-world input (EDN files, anything
read from disk, future network), **typed.clojure** checks pure internal logic
(currently `shx.shape.quote`; EDN-boundary code is malli's job — typed.clojure's
strict `Any` handling can't model heterogeneous EDN without lies).

## Gate

```bash
bin/analyze        # or: bb check
```

Seven checks, all blocking, all at zero:

| | finds |
|---|---|
| clj-kondo | bugs, anti-patterns |
| splint | non-idiomatic forms |
| cljfmt | formatting drift |
| clojure-lsp | unused public vars, project-wide |
| jscpd | copy-paste duplication (threshold 0 — the tree has no clones) |
| typed.clojure | type errors in the pure, checked namespaces |
| kaocha | failing tests |

CI runs `bin/analyze --strict`, where a check that could not *run* also fails.
Tool versions are pinned; bump deliberately, fix what the new version finds,
commit both together. See [`docs/static-analysis.md`](docs/static-analysis.md).

```bash
bin/mutate   # mutation testing over domain/law/shape (heretic, experimental)
```

Coverage asks whether a line ran. Mutation testing asks whether breaking it makes
a test fail — the difference between a suite that executes the code and one that
holds it. It is not part of the gate (it swaps the Clojure compiler and is
pre-1.0); it is the loop you run when you change pure logic. See
[`docs/mutation-testing.md`](docs/mutation-testing.md).

## License

GPLv3+. See LICENSE.
