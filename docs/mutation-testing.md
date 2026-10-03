# Mutation testing

```bash
bin/mutate                 # collect coverage if stale, then mutate + report
bin/mutate survivors       # surviving mutants from the last run, triaged
bin/mutate no-coverage     # forms no indexed test reaches
bin/mutate status          # which test namespaces need recollection
bin/mutate watch           # continuous mutation testing on file change
bin/mutate clean           # drop the cached coverage index
bin/mutate mutate --files src/shx/domain/merge.clj    # scope to one file

bb mutate · bb mutate:survivors · bb mutate:gaps        # same, via babashka
```

Coverage answers "was this line run?". Mutation testing answers the question that
actually matters: **"if I break this line, does a test notice?"**

A suite can be green, fully covered, and still not hold the behaviour it claims —
because executing a line and asserting on its result are different things. A
*surviving mutant* is exactly that gap made visible: code a test ran through and
never checked.

This is the check that is hardest to fake. Tests can be written to pass; a
surviving mutant says the test does not constrain the code, whoever wrote either.

## The tool

[Heretic](https://github.com/parenstech/heretic), pinned to `main@45ed7c7`.
Configuration in `heretic.edn`; wrapper in `bin/mutate`.

Heretic's trick is **test-to-code mapping**: it instruments with ClojureStorm to
learn which tests exercise which expressions, so each mutation re-runs only the
tests that could possibly kill it. That is what makes mutation testing fast
enough to run in a loop — the full sweep below takes under a second once coverage
is collected.

Two things to know before trusting it:

- **It is EXPERIMENTAL.** Upstream calls it "a demo for the curious" and "not
  ready for production use". Treat its numbers as evidence, not as a gate.
- **ClojureStorm replaces the Clojure compiler.** That is isolated to the
  `:heretic` alias in `deps.edn`. `bb check`, `bin/analyze`, `clojure -M:test`,
  and CI never load it. Mutation runs in a disposable copy of the tree
  (`.heretic-sandbox/`); your working tree is only ever read.

## Why it is not in the gate

`bin/analyze` is pass/fail and must stay fast and boring. Mutation testing is
neither: it swaps the compiler, its output is a *score to act on* rather than a
verdict, and the tool is pre-1.0. Wiring an experimental compiler-replacing tool
into the blocking gate would make the gate's own reliability the weakest link.

Run it when you change `domain/`, `law/`, or `shape/`, and when you add tests
that are supposed to close a gap. Then read the survivor list, not just the score.

## Scope

Instrumented and mutated: `shx.domain`, `shx.law`, `shx.shape` — the pure layers.

Excluded (`:exclude-files` in `heretic.edn`): `cli.clj`, `infra/config.clj`,
`infra/fs.clj`. Mutating file and process I/O is slow and low-signal, and because
those namespaces are not instrumented, heretic could never observe a kill in them
— they contributed 21 permanently-red "no coverage" sites, 41% of the report, that
no test could ever move. **If you add a file under `infra/` or another process
boundary, add it to `:exclude-files`**, or it silently rejoins that list.

That `cli` and `shape/bb` have no tests at all is a real gap. It lives on the
board as `shx-bb-cli-untested`, which is the honest place for it — not as
permanent noise in a report.

## Where it stands (2026-08-10)

```
Total: 30 mutation sites
  Killed:        6 (20%)
  Survived:      0 (0%)
  No Coverage:  24 (80%)
Score: 100.0%
```

**Read the score narrowly.** It is `killed / (killed + survived)` — the kill rate
among sites a test actually reaches. It says nothing about the 24 sites no test
reaches, which is the larger problem and is reported separately. A 100% score
over 6 of 30 sites is not a good suite; it means every site the index *can* see
is held.

**And do not ratchet on it.** See "The score is not stable" below — this number has
moved without any change to the code it measures.

### The first run found a real gap, and it is now closed

The very first `bin/mutate` in this repo reported 2 survivors, both in
`src/shx/domain/merge.clj:27-28`, both the same operator (`:swap-and-or`) applied
to the two guards in `merge-frag`:

```clojure
(cond
  (and (map? x) (map? y))       (merge x y)
  (and (vector? x) (vector? y)) (into x y)
  :else y)
```

Either `and` could become an `or` and every test still passed. So nothing asserted
what happens on a **type collision** — a key that is a map in one fragment and a
vector in another. Under `or`, `(merge-frag {:vars {:A "1"}} {:vars ["x"]})`
reaches `(merge {:A "1"} ["x"])` and throws `Vector arg to map conj must be a
pair` instead of falling through to `:else y`.

Not hypothetical for envm: fragments are hand-edited EDN merged by `:merge`
includes and then by hostname, so a key that changes shape between two fragments
is ordinary editing breakage — and the failure mode was an exception raised while
rendering a login shell, which `AGENTS.md` › Quality contract says must never
happen.

Closed by `merge-type-collision-untested`: three assertions in
`merge_test.clj:12-23` pin the documented "anything else: later wins" rule,
including the reversed case where the collection is on the *other* side. Both
mutants were then applied by hand to confirm the assertions actually constrain the
guards — `and`→`or` on line 27 gives 1 error + 1 failure, on line 28 gives 1 error
+ 2 failures. `bin/mutate mutate --files src/shx/domain/merge.clj` reports 3 of 3
killed.

**That sequence is the method, not a formality.** Coverage had this function at
100% the whole time. Only the mutant said the guards were undefended, and only
re-applying the mutant proved the new tests were what killed it.

### The 24 no-coverage sites are three different things

| Sites | Where | Cause |
|---|---|---|
| 9 | `shape/bb.clj` | **Real gap.** The namespace has no tests at all. |
| 2 | `shape/bash.clj` | **Real gap.** Two branches no test reaches. |
| 9 | `law/config.clj` | **Tool limitation.** Schema literals. |
| 3 | `law/ir.clj` | **Tool limitation.** Schema literals. |
| 1 | `shape/quote.clj:18` | **Tool bug.** A lost attribution — see below. |

The `law/` sites are mutation sites *inside top-level `def` forms* — malli schema
data like `[:vector {:min 1} :string]`. Those forms evaluate when the namespace
loads, not while a test runs, so heretic's test-to-code index attributes them to
no test and never mutates them. They are not untested: `law.ir-test` asserts that
`[:exec {:argv []}]` is rejected, which is precisely the `{:min 1}` constraint. A
mutation there *would* be killed if heretic knew to run that test.

So: **11 of the 24 are honest gaps and 13 are artefacts.** Do not treat the 80% as
a test-quality number. Carded as `heretic-schema-literal-attribution`.

### The score is not stable

Measured on 2026-08-10, in this order:

1. First run, before any test was added: **5 killed, 23 no-coverage, score 71.4%**,
   with `shape/quote.clj:18` killed by `shape.quote-test/dq-test`.
2. One `deftest` was added to `test/shx/law/ir_test.clj` — a different namespace,
   testing `law.ir/valid-node?`. The form registry went from 99 forms to 100.
3. Second run: **4 killed, 24 no-coverage, score 66.7%**. `shape/quote.clj:18`
   had moved to no-coverage and `dq-test` had dropped out of the killer list.

`shape/quote.clj` was not touched. `dq-test` asserts on `dq`'s exact output three
times (`quote_test.clj:13-19`), including the `~` → `$HOME` substitution that the
lost site is part of — so the site is genuinely covered and heretic is now
*under*-reporting it. Reproduced across three consecutive runs and two
`bin/mutate clean` recollects, so it is stable, not flaky: adding an unrelated
test permanently shifted a correct attribution to no-coverage.

It is not universal, either, which is what makes it hard to reason about. Adding
three assertions to `merge_test.clj` shortly afterwards moved the two merge
survivors to killed and left the no-coverage list byte-identical — same 24 sites,
same 5 files. So an unrelated test edit *may* perturb attribution and may not, and
there is no signal at the call site telling you which happened.

Two consequences:

- **Never gate or ratchet on the score.** It can move because a test was added
  elsewhere. `bin/analyze` is the ratcheted gate; this tool is a magnifying glass.
- **Act on the survivor list, and on named sites.** A survivor is a specific
  claim about a specific form and has been reliable here. The killed/no-coverage
  split has not.

If a site you know is tested appears under `no-coverage`, check
`bin/mutate mutate --files <that file>` before believing it, and add the case to
this section.

## Reading a survivor

`bin/mutate survivors` prints each survivor with its file, line, operator,
original form, and replacement. Three outcomes, and naming which one you chose is
the whole value of the exercise:

1. **A real gap** — add the assertion that kills it. This is the common case.
2. **An equivalent mutant** — the mutation produced code that cannot behave
   differently (`(< a b)` → `(<= a b)` where the values can never be equal).
   Heretic triages some of these itself; the rest are yours to recognise. Say so
   in the card or the comment log; do not silently ignore it.
3. **Behaviour nobody has decided yet** — the mutant survives because the
   specification is genuinely open. That is a design question, not a test task,
   and it goes on the board as one.

Never "fix" a survivor by deleting the code it mutated, and never raise the
operator preset over an untriaged survivor list — new survivors bury old ones.

## Operators

`:preset :standard` (36 operators). Available: `:fast` (16), `:minimal` (31),
`:standard` (36), `:comprehensive` (81). Two conditions before raising it, and as
of 2026-08-10 only the first is met: the survivor list is at zero, but the
no-coverage list still holds 11 real gaps in `shape/bb` and `shape/bash`. A wider
preset over untested namespaces just mints more no-coverage sites — it widens the
denominator without measuring anything new. Close
`shx-kanban-shx-bb-cli-untested` first, then raise to `:comprehensive` and expect
new survivors in code that looks finished.

## Caveats

- `rsync` is strongly recommended. Without it every run does a full tree copy and
  a full re-collect.
- `:keep-sandbox true` leaves `.heretic-sandbox/` in place so the next run is
  incremental. Both it and `.heretic/` are gitignored.
- After editing tests, coverage is stale; `bin/mutate` recollects automatically,
  and `bin/mutate status` says what it thinks is stale.
- Heretic prints shx's own stderr warnings ("invalid fragment", "cycle in
  :merge") during collection. Those come from `infra.config-test` exercising the
  degrade paths on purpose. They are not failures.
