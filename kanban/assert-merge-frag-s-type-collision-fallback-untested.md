---
category: "kanban"
labels: "shx, tests, mutation"
type: "task"
write-id: "1786348357692-0.0erhpkdmwr6valsels30"
points: "2"
title: "Assert merge-frag's type-collision fallback"
priority: "P1"
status: "done"
uuid: "merge-type-collision-untested"
created_at: "2026-08-10T07:35:45.725Z"
---

# Assert merge-frag's type-collision fallback

## Outcome

`merge-frag` has assertions for the case its own docstring calls "anything else:
later wins" — a key that is a map in one fragment and a vector (or scalar) in
another. Both `:swap-and-or` mutants at `src/shx/domain/merge.clj:27-28` die, and
`bin/mutate mutate --files src/shx/domain/merge.clj` reports zero survivors.

## Context

Found by mutation testing on 2026-08-10, the first `bin/mutate` run in this repo:
2 survivors out of 30 sites, both in `merge-frag`, both the same operator.

```clojure
;; src/shx/domain/merge.clj:25-30
(merge-with (fn [x y]
              (cond
                (and (map? x) (map? y))       (merge x y)   ; line 27 — `and`→`or` survives
                (and (vector? x) (vector? y)) (into x y)     ; line 28 — `and`→`or` survives
                :else y))
            a b)
```

`test/shx/domain/merge_test.clj:5-13` covers map+map, vector+vector, and
scalar+scalar. It never passes a **mixed** pair, so neither `and` is ever
evaluated with one side true and the other false — which is precisely what the
`and` is there to decide.

Under the `or` mutant, `(merge-frag {:vars {:A "1"}} {:vars ["x"]})` reaches
`(merge {:A "1"} ["x"])` and throws `IllegalArgumentException: Vector arg to map
conj must be a pair` instead of falling through to `:else y`.

The `:vars` map/vector example above is rejected by config validation in the
top-level config and hostname overrides; `read-fragment-tree` discards invalid
fragments. However, `Fragment` is an open map. A schema-valid config can contain
an undeclared key with different value shapes across fragments or a hostname
override. Those values reach `merge-frag` before `shx render` renders the shell.
If either `and` guard becomes `or`, such a collision can raise an exception on
the login-shell path.

## Acceptance criteria

- [ ] GIVEN `{:vars {:A "1"}}` and `{:vars ["x"]}` WHEN merged THEN the result is
      `{:vars ["x"]}` — later wins, no exception
- [ ] GIVEN `{:paths-prepend ["a"]}` and `{:paths-prepend "b"}` WHEN merged THEN
      the result is `{:paths-prepend "b"}`
- [ ] GIVEN either mutant applied by hand WHEN `bb test` runs THEN it FAILS
      (confirm the assertions actually constrain the guards)
- [ ] `bin/mutate mutate --files src/shx/domain/merge.clj` reports 0 survivors

## Verification

```bash
bb test
bin/mutate mutate --files src/shx/domain/merge.clj
```

The mutation run is the criterion that cannot be satisfied by a test written to
pass. `bb test` going green proves nothing on its own here — it was already green
with the gap.

## Scope

- `test/shx/domain/merge_test.clj` — extend `merge-frag-test`

## Out of scope

- Changing `merge-frag`'s behaviour. Later-wins is the documented rule
  (`src/shx/domain/merge.clj` ns docstring); this card asserts it, it does not
  renegotiate it.
- The 11 real no-coverage sites in `shape/bb` and `shape/bash` —
  `shx-kanban-shx-bb-cli-untested`.
- Whether malli should reject a shape change across fragments at the
  `law/config` boundary instead. That is a design question; card it separately if
  the assertions make it look attractive.

## Reference points

- `test/shx/domain/merge_test.clj:5-13` — the existing `merge-frag-test`, and the
  three cases it does cover. Add to it; do not start a new deftest.
- `src/shx/domain/merge.clj:1-14` — the ns docstring holds the three merge rules
  this card is asserting.
- `docs/mutation-testing.md` › "The two survivors are one real gap".

## Anti-patterns

- Do NOT add a `try`/`catch` around the merge to make the symptom go away. The
  `cond` already has the correct branch; the gap is in the tests.
- Do NOT assert on the exception. The expected behaviour is `:else y` returning
  the later value, not a thrown-and-caught error.
- Do NOT satisfy this with a property-based test as the only assertion. Two
  named, readable cases pin the documented rule; generative tests can be added
  after, not instead.

---
Done 2026-08-10. All four acceptance criteria met.

CHANGE: three assertions added to merge-frag-test (test/shx/domain/merge_test.clj:12-23), pinning the ns docstring's 'anything else: later wins' rule:
  {:vars {:A "1"}}     + {:vars ["x"]}        -> {:vars ["x"]}
  {:vars ["x"]}         + {:vars {:A "1"}}     -> {:vars {:A "1"}}   (collection on the other side)
  {:paths-prepend ["a"]} + {:paths-prepend "b"} -> {:paths-prepend "b"}
merge-frag itself is unchanged, as scoped.

CRITERION 3 (the mutants must actually fail the suite) — applied by hand, per the card, not inferred:
  line 27 and->or : 19 tests, 46 assertions, 1 error + 1 failure
  line 28 and->or : 19 tests, 46 assertions, 1 error + 2 failures
The error in both cases is the real defect surfacing: (merge {:A "1"} ["x"]) throws 'Vector arg to map conj must be a pair'. Source restored via git checkout; git diff on merge.clj is empty.

CRITERION 4: bin/mutate mutate --files src/shx/domain/merge.clj -> 3 sites, 3 killed, 0 survived.

BOARD-WIDE EFFECT: full sweep is now 30 sites, 6 killed, 0 survived, 24 no-coverage, score 100.0% (was 4/2/24, 66.7%). The no-coverage list is byte-identical to before — same 24 sites, same 5 files — which is a useful counter-example to the attribution instability recorded in docs/mutation-testing.md: an unrelated test edit MAY perturb attribution and may not. Both observations are now in that doc.

Worth noting for whoever reads this later: coverage had merge-frag at 100% the entire time this gap existed. The mutant is what found it, and re-applying the mutant is what proved the new tests were the thing that killed it.

Docs updated in the document state: docs/mutation-testing.md 'Where it stands' and Operators. Gate green through the FSM build gate on the way to review.
---