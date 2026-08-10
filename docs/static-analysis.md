# Static analysis

```bash
bin/analyze            # the gate: seven checks, exits non-zero on any BLOCKING finding
bin/analyze --fix      # apply what is safely auto-fixable (cljfmt), then sweep
bin/analyze --strict   # CI mode: a check that could not RUN is a failure
bb check               # same thing (delegates to bin/analyze)
```

`bin/analyze` is the **single definition of the gate**. `bb check` shells out to
it and `.github/workflows/static-analysis.yml` runs `bin/analyze --strict`, so
there is exactly one list of checks to keep true. Adding a check means editing
one file.

Mutation testing is deliberately *not* in the gate — see
[mutation-testing.md](mutation-testing.md).

## The seven checks

| Check | Tool | Finds | Config |
|---|---|---|---|
| bugs / anti-patterns | clj-kondo 2025.07.28 | unused bindings, arity errors, shadowing, unresolved vars | `.clj-kondo/config.edn` |
| idiom | splint 1.24.0 | non-idiomatic forms — the maintained kibit successor | `.splint.edn` |
| formatting | cljfmt 0.16.5 | indentation and whitespace drift | — |
| dead code | clojure-lsp | unused **public** vars, project-wide | `.lsp/config.edn` |
| duplication | jscpd 4.2.5 | copy-pasted blocks (≥8 lines, ≥60 tokens) | `.jscpd.json` |
| pure-logic types | typed.clojure 1.4.0 | type errors in checked namespaces | `deps.edn` `:typecheck` |
| behaviour | kaocha | failing tests | `tests.edn` |

Three of these answer questions no single-namespace linter can. **clojure-lsp**
sees the whole project, so it is the only check that catches the public var an
abandoned refactor left behind. **jscpd** sees text, so it is the only check that
catches the second copy of a function that was easier to paste than to lift.
**splint** knows Clojure idiom, so it catches the shape that works but reads
wrong.

### The gating contract (2026-08-10)

Every one of the seven was at **zero findings** on 2026-08-10. That is the only
reason all seven are BLOCKING.

A gate promoted while findings remain is a gate that gets merged past, and worse:
once one check is habitually red, its output stops being read — and then so does
everything printed next to it. That failure mode is documented in
`../Truth/bin/analyze`, where `static-analysis` failed on 33 consecutive pushes
to `main` and every one landed anyway.

So if a check fires, it is a **blocker**, not a backlog item:

1. Fix it, or
2. add a per-site suppression carrying its reason (below), or
3. put a card on the board — and **do not** demote the tier or widen a ratchet to
   turn red into green.

Getting here took fourteen mechanical fixes on 2026-08-10, recorded so the
numbers above are not mysterious:

- twelve `lint/prefer-method-values` — `(.getPath f)` → `(File/.getPath f)` in
  `infra/config.clj`, `infra/fs.clj`, and one test. Not cosmetic: a qualified
  method resolves at compile time, so this also removed reflection.
- two `style/defmulti-arglists` — `{:arglists '([node])}` on both `emit`
  multimethods.
- one `clojure-lsp/unused-public-var` — `law.ir/valid-node?` had no caller and no
  test. Fixed by **testing it**, not by declaring it exported: it is the
  single-node counterpart to `valid-program?`, and an untested public predicate
  is a gap whichever linter noticed first.

## Ratchets

Two checks are gated by a number rather than by zero/non-zero:

- **`.jscpd.json` `"threshold": 0`** — the tree has no clones (14 files, 798
  lines, 9257 tokens, 0 duplicated, measured 2026-08-10). jscpd fails *above* the
  threshold, so 0 means the first clone introduced into `src/` or `test/` fails
  the build.
- **`.splint.edn` `naming/lisp-case` scoped out of `src/shx/law/*.clj`** — malli
  schema vars are PascalCase by ecosystem convention, and namespace law says
  `law/` holds contracts and nothing else. The rule still fires in every other
  layer.

A ratchet only ever moves in the tightening direction. Loosening one is a change
of policy: it belongs in the same commit as the reason, naming *which* finding is
being exempted and *why* — never as a bare number bump.

## Suppression conventions

### splint — per-form, with the reason at the site

```clojure
;; Intentional: <why this rule is wrong here>
#_{:splint/disable [lint/catch-throwable]}
(defn render-loop [] ...)
```

A form-level marker covers findings nested anywhere inside that form, so one
marker on a `defn` covers several sites in its body. **ns-level
`{:splint/disable [...]}` metadata is not honoured by splint 1.24.0** — do not
reach for it.

A rule is never disabled globally in `.splint.edn`. A blanket
`{:enabled false}` exempts every *future* site too, and then the finding simply
stops appearing and nobody decided anything. Per-rule `:excludes` is allowed when
the exception is a property of a whole layer rather than of one form; there is
exactly one such entry today, and it carries its reasoning in the file.

`:excludes` patterns use `re-find:`, not `glob:`. Splint matches them against the
path *as given on the command line* (`src/shx/law/ir.clj`), and a Java glob
`**/src/...` requires a segment before `src/`, so it silently matches nothing —
which looks exactly like a working exclusion until the rule fires again.

### clj-kondo

`.clj-kondo/config.edn` exists solely to keep kondo quiet about
`typed.clojure/ann`, `defalias`, and `Rec` forms, which it cannot resolve. Do not
grow it into a suppression list.

### clojure-lsp

A var that is genuinely API surface with no in-tree caller can be excluded in
`.lsp/config.edn`. Prefer testing it first: if nothing in the repo — not even a
test — refers to a public var, the linter is usually right.

## Running one check

```bash
bb lint        # clj-kondo
bb splint      # splint
bb fmt         # cljfmt check      (bb fmt:fix rewrites)
bb dead        # clojure-lsp diagnostics
bb dupes       # jscpd
bb typecheck   # typed.clojure
bb test        # kaocha
```

## Version pinning

Every tool version is pinned: clj-kondo and clojure-lsp in
`.github/workflows/static-analysis.yml`, splint and cljfmt in `deps.edn`, jscpd
in `bin/analyze`, heretic in `deps.edn`.

A newer version finds new things. That is good, and it must be a deliberate bump:
raise the pin, fix what the new version finds, and commit both together. A
floating version means a PR can go red for reasons that have nothing to do with
the PR.

## What is not here yet

`../Truth` runs two further checks this repo does not: a **structural smell
report** (god namespaces, mega-functions, parameter bloat, fan-out — 
`dev/smell_report.clj`, reading clj-kondo's analysis export) and a
**layer-boundary check** enforcing construction order. shx has nine source files
and its namespace law is currently enforced by review. Both are worth porting
when the tree is large enough for either to fire; neither is worth a gate that
can only ever pass.
