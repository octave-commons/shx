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
bb check   # clj-kondo (zero warnings) + typed.clojure + kaocha
```

All three block in CI (`.github/workflows/`). Tool versions are pinned; bump
deliberately, fix what the new version finds, commit both together.

## License

GPLv3+. See LICENSE.
