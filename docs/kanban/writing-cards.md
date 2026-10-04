# Writing cards for this board

The board is `./kanban`, one markdown file per card, governed by the **promethean
FSM** (`openhax.kanban.edn` › `:fsm`). Templates live in
[`templates/`](templates/).

This guide is written for the reader who will actually pick a card up: usually an
agent, sometimes you in three weeks with none of today's context. Those two
readers want the same thing, and it is not prose.

## 0. The one idea

**A card is a claim that can be checked without trusting whoever closed it.**

Everything below follows from that. An outcome is checkable; "improve the merge
code" is not. A verification command is checkable; "tested locally" is not. That
is why this board's `in_progress -> review` transition runs `bin/analyze
--strict` (see [`../static-analysis.md`](../static-analysis.md)) — "it's done"
stops being a report and becomes an exit code.

An agent that has been asked to close a card will find the cheapest path to
looking finished. Do not write cards that make looking finished cheap.

## 1. A card is a unit of pull, not a note

| It is | It goes |
|---|---|
| a thought, a link, a session artifact, an origin story | `docs/inbox/` |
| a decision already made and needing no work | a ledger or `AGENTS.md` |
| reference material | `docs/reference/` |
| **work someone will pull and finish** | **the board** |

The test: *can this be finished?* If there is no state of the world in which
someone says "that is done", it is not a card. Park genuinely-deferred work in
`icebox` — that is what the state is for — but a note with a `status:` field is
still a note.

## 2. The FSM is the contract

Fourteen states, and the transitions between them are the *only* legal moves.
`rheos move` enforces them; hand-editing `status:` in frontmatter bypasses the
whole mechanism and is how a board starts lying.

```
icebox ⇄ incoming → accepted → breakdown → ready → todo → in_progress
                                   ↕                          ↓
                                 blocked                    testing
                                                              ↓
                              done ← document ← review ←──────┘
```

| State | Means | Entry criteria — do not move it here until |
|---|---|---|
| `icebox` | deliberately not now | there is a reason it is parked, in the body |
| `incoming` | captured, unjudged | it exists. Every new card starts here |
| `accepted` | we agree this is real work | the outcome is stated and someone wants it |
| `breakdown` | being scoped | — this is where the body gets written |
| `blocked` | scoping cannot finish | the blocker is named, with what would unblock it |
| `ready` | scoped, not yet queued | acceptance criteria + verification are written, no open questions remain |
| `todo` | queued to be pulled | it is next, and it is small enough to finish |
| `in_progress` | being worked | someone/something is actually on it (WIP-gated) |
| `testing` | implementation done, being verified | the change exists and its tests are written |
| `review` | up for judgement | **`bin/analyze --strict` passes** — the FSM runs it |
| `document` | behaviour settled, docs owe an update | the change is accepted |
| `done` | finished | docs match the code |
| `rejected` | decided against | the reason is a comment on the card |
| `archived` | off the board | anything |

Four consequences worth internalising, because they are enforced and surprising:

- **`incoming` cannot go straight to `breakdown`.** It goes through `accepted`.
  "Is this real work?" and "what exactly is the work?" are separate decisions and
  the FSM refuses to let you skip the first.
- **`done` is reachable only from `document`.** Documentation is a state, not an
  afterthought. There is no path that skips it.
- **`in_progress -> review` is the only gated transition** (`:build-gate`). Every
  other move is `:always-allow`. So this is the single mechanical choke point on
  this board, which is exactly why it runs the full gate here rather than a
  subset.
- **`rejected` is not reachable from `incoming`.** An unwanted new card goes to
  `icebox` or `archived`. Rejection is a judgement about *scoped* work.

Reopening is legal and normal: `done -> review`, `review -> in_progress`,
`ready -> breakdown`, `in_progress -> breakdown`. Use them. A card that goes
backwards through a state it failed is honest; a card edited in place is not.

## 3. Anatomy

### Frontmatter — author input; engine-owned lifecycle

```yaml
---
uuid: merge-type-collision-untested
title: Assert merge-frag's type-collision fallback
status: incoming
type: task
priority: P1
points: 2
labels: shx, tests, mutation
parent: null
category: tasks
write-id: <written by rheos>
created_at: 2026-08-10T…Z
---
```

The block illustrates a CLI-created card, not a required serialization order.
Incoming Markdown cards may be hand-authored with stable identity and descriptive
fields; do not invent `write-id`, creation events, or lifecycle transitions.
Operational state, transitions and comments belong to Rheos. For CLI-created cards:

- `uuid` defaults to the title slug, because you address the card by uuid on
  every later call. `merge-type-collision-untested` is citable in a commit
  message; a hex blob is not.
- **Prefer the default over `--uuid`.** When the uuid differs from the title slug,
  the file lands as `<slug>-<last-8-of-uuid>.md`. Verified 2026-08-10: passing
  `--uuid merge-type-collision-untested` with the title "Assert merge-frag's
  type-collision fallback" produced
  `assert-merge-frag-s-type-collision-fallback-untested.md`. Choose the *title* so
  its slug is the name you want to cite, and let the uuid follow.
- The historical category was derived from the task root. Current CLI help
  supports `create --labels shx,tests`; verify upstream help for your version.
  Use `rheos frontmatter <uuid> --set 'labels=shx, tests'` for later changes.
- `status` is owned by `move`. `rheos frontmatter` refuses to write it.
- `uuid`, `created_at`, `write-id`, `source-path` are never writable.
- Mutable set: `title`, `priority`, `labels`, `points`, `category`,
  `description`, `estimate`, `assignee`.

Older cards on this board carry `labels: ["tasks","harness","docs","2sp"]` — a
YAML array, with the estimate encoded *twice* (as `points: 2` and as a `2sp`
label). Do not copy that. `points` is the field; a `2sp` label is a duplicate
that can disagree with it.

### Body — the sections, in order

Outcome, Context, Acceptance criteria, and Verification are mandatory. Empty
optional sections are worse than absent ones.

```markdown
# <Title as an imperative: the change, not the symptom>

## Outcome
One paragraph. What is TRUE when this is done, in the present tense.

## Context
Why this is real, with provenance. Facts get a date and a source; guesses are
labelled as guesses. `file.clj:31` beats "in the config code".

## Acceptance criteria
- [ ] GIVEN <state> WHEN <action> THEN <observable result>
- [ ] …

## Verification
The commands that decide it. Exit code, not vibes.

    bb test
    bin/mutate mutate --files src/shx/domain/merge.clj

## Scope
The files this touches. Name them.

## Out of scope
What a reader might reasonably fold in, and must not.

## Reference points
The existing code to follow, with line numbers.

## Anti-patterns
What NOT to do here, and why. Negative constraints are cheap and save whole
wrong implementations.

## Open questions
Anything undecided. A card with open questions CANNOT leave `breakdown`.
```

## 4. The rules

1. **One outcome per card.** If the outcome needs "and", you have two cards or an
   epic.
2. **The title is the change, imperative.** "Assert merge-frag's type-collision
   fallback", not "merge-frag is untested". A symptom title tells the reader what
   is wrong and leaves them to invent the work.
3. **Every acceptance criterion is independently observable.** GIVEN/WHEN/THEN, or
   a `VERIFY:` line with a threshold. "Should be fast" is not a criterion;
   "`bb test` completes under 5s" is.
4. **Every card has a verification command.** If nothing can be run, say how a
   human decides, in one sentence — and treat that as a smell.
5. **Slice vertically.** A card delivers a working thin thing end to end, not a
   layer. "Add the schema" + "add the emitter" + "add the tests" are three cards
   that are individually worthless; "emit `:foo` nodes to bash, validated and
   tested" is one card that is worth something.
6. **Facts carry provenance; guesses are labelled.** "Verified 2026-08-10: no
   Claude Code hook runs before CLAUDE.md is read" is a fact. "Whether a stable
   `tool_call_id` is exposed has NOT been confirmed" is an honest unknown, and
   saying so is what stops the next reader designing on top of it.
7. **Name the files.** Entry points and reference implementations by path, with
   line numbers where they help. This is the single largest difference between a
   card an agent executes and a card an agent explores for twenty minutes.
8. **No open question leaves `breakdown`.** An either/or in a `todo` card is a
   decision delegated by accident to whoever picks it up, which means the
   decision gets made by whoever is cheapest, not whoever knows.
9. **The card is not the work log.** After `breakdown`, updates are comments.

## 5. Types

| `type` | Template | For |
|---|---|---|
| `task` | [`templates/task.md`](templates/task.md) | one change with one outcome |
| `epic` | [`templates/epic.md`](templates/epic.md) | a named end state with child cards; `--parent` links them |
| `task` | [`templates/bug.md`](templates/bug.md) | broken behaviour — repro first, then expected vs actual |
| `task` | [`templates/decision.md`](templates/decision.md) | a choice to make; the deliverable is the decision, recorded |
| `task` | [`templates/spike.md`](templates/spike.md) | a question to answer; time-boxed, output is knowledge |

rheos only knows `task` and `epic`. Bug/decision/spike are body shapes, not new
types — distinguish them with a label (`bug`, `decision`, `spike`).

The two that get written wrong most often:

- **A decision card's outcome is "X is decided and written down"**, not
  "X is implemented". It closes when the decision exists, with its reasoning and
  its runner-up. `name-the-unified-package` on this board does this well.
- **A spike's acceptance criterion is a written answer**, and it names where the
  answer will be recorded. A spike that ends in someone's head produced nothing.
  Time-box it in the body.

## 6. Bodies settle after breakdown

While a card is in `breakdown` the body is a draft. Once it leaves, **the body is
the agreed contract** and later information goes in the comment log:

```bash
rheos comment merge-type-collision-untested --text "Reviewer wants the :else branch split; scope unchanged"
```

Rewriting the body of a card in flight silently replaces the scope other people
and agents are working from, with no record of what changed. Comments are
append-only and ledger-recorded. If scope genuinely must change, move the card
back to `breakdown` (legal from `ready`, `blocked`, and `in_progress`),
re-author, and move it forward again.

**This includes the retrospective.** Several `done` cards here had their bodies
*replaced* by a "# Done 2026-08-10" write-up. The lesson is now unrecoverable
next to the scope it was supposed to be judged against — you cannot tell what was
asked for. Put the retro in a comment; leave the body as the contract it was.

## 7. Writing for an agent reader

An agent has no tribal knowledge, will not ask, and — left underspecified — will
guess plausibly and confidently. Four sections do most of the work:

- **Entry points.** "Add the branch to `src/shx/shape/bb.clj` next to the
  `:path/prepend` method at line 18", not "update the bb emitter".
- **Reference points.** "Follow `shape/bash.clj:26` — same dispatch, same
  quoting via `shape/quote`." Pointing at one real example beats three paragraphs
  describing the pattern.
- **Anti-patterns.** "Do NOT add a `:default` method to `shape/bash/emit` — an
  unhandled head must throw; silent degradation is what `shape/bb` already gets
  wrong." One line, one whole wrong implementation avoided.
- **Verification.** The literal commands. An agent that can run the check does
  not need to be believed.

And the adversarial half, which is why this board runs a build gate at all:

- Acceptance criteria that only the implementer can evaluate will be evaluated
  favourably. Prefer criteria the gate already enforces.
- "Add tests" is not a criterion — a test that asserts nothing passes. Say what
  must be asserted, or better, say which mutant must die:
  `bin/mutate` reports it, and no amount of confident summary can fake it.
- If the work touches `domain/`, `law/`, or `shape/`, put the mutation check in
  Verification. It is the one check that cannot be satisfied by a test written to
  pass.

## 8. Sizing, priority, labels

**Points** are Fibonacci and mean *uncertainty × size*, not hours: `1` trivial ·
`2` an afternoon · `3` a day · `5` needs breaking down soon · `8` needs breaking
down now · `13` epic. A `13` that is not an epic is an admission that breakdown
did not happen.

**Priority** is a promise about order, not importance: `P0` blocks other work
right now · `P1` this cycle · `P2` wanted, unscheduled · `P3` someday. If
everything is P1, nothing is.

**Labels** are for filtering (`rheos compose --labels shx,tests`). Use the
subsystem (`shx`, `envm`, `hexis`, `clio`), the kind (`tests`, `docs`, `debt`,
`design`, `bug`, `decision`, `spike`), and nothing else. No `2sp` — that is what
`points` is for.

## 9. Anti-patterns seen on this board

Every one of these is from a real card here. They are all *specific and factual*,
which is the hard part — what they are missing is the checkable part.

| Anti-pattern | Example | Fix |
|---|---|---|
| Symptom as title | "shape/bb and cli have no tests" | Name the change: "Cover shape/bb's emit dispatch with tests" |
| No outcome, no criteria | a card whose body is two sentences of observation | Add Outcome + Acceptance criteria, or move it to `docs/inbox/` |
| Unresolved either/or in a non-breakdown card | "Either implement `shx.core` or narrow what the bb emitter claims to support" (status `incoming`) | Split: a decision card, then the work card it unblocks |
| Retro overwrites scope | "# Done 2026-08-10" as the whole body of a `done` card | Body stays; retro is a comment |
| Estimate stored twice | `points: 2` and a `2sp` label | `points` only |
| Open question shipped forward | "OPEN QUESTION — do not implement past this line" in a `todo` card | It belongs in `breakdown` or `blocked` until answered |
| Verification absent | most of the board | Add the command that decides it |

## 10. Definition of ready

Before `breakdown -> ready`, all of:

- [ ] Title is an imperative naming the change
- [ ] Outcome is one paragraph, present tense, checkable
- [ ] Context carries provenance; unverified claims are labelled
- [ ] Acceptance criteria are independently observable
- [ ] Verification names runnable commands
- [ ] Scope names files; Out of scope names the tempting adjacent work
- [ ] Reference points cite real code
- [ ] `points` set, and ≤ 5
- [ ] Open questions section is empty or absent
- [ ] `type` and `labels` set

## 11. Commands

These lifecycle observations were made on 2026-08-10; they are not proof of a
current build-gate transition. Use the upstream-installed `rheos` on PATH, or
set `RHEOS_CLI` to the absolute path of an upstream-built artifact and invoke
`node "$RHEOS_CLI" <verb> ... --config openhax.kanban.edn` directly. Verify help
and the configured engine; an unavailable/unsupported gate is an upstream gap,
not permission to duplicate Rheos semantics locally. The examples below assume
the executable is on PATH:

```bash
rheos help

rheos projects                                    # board config as this repo sees it
rheos read-board --status incoming,breakdown       # scope your reads
rheos search-tasks --query merge

# create — a hand-authored Markdown body or a template is supported
sed -e 's/<TITLE>/Assert merge-frag type-collision fallback/' \
    docs/kanban/templates/task.md > /tmp/card.md
$EDITOR /tmp/card.md
rheos create --title "Assert merge-frag's type-collision fallback" \
             --points 2 --priority P1 --body-file /tmp/card.md

# Copy the UUID returned by create; do not substitute an existing card's ID.
CARD_UUID='<UUID returned by rheos create>'

# advance — one state at a time; the FSM refuses illegal jumps
rheos move "$CARD_UUID" --to accepted
rheos move "$CARD_UUID" --to breakdown
rheos frontmatter "$CARD_UUID" --set points=2 --set labels="shx, tests"
rheos move "$CARD_UUID" --to ready
rheos move "$CARD_UUID" --to todo
rheos move "$CARD_UUID" --to in_progress
rheos move "$CARD_UUID" --to review     # runs bin/analyze --strict

rheos comment "$CARD_UUID" --text "…"
rheos read-task "$CARD_UUID"
rheos events "$CARD_UUID"               # this card's history
rheos drift                                             # cards edited outside rheos
```

Exit codes are the interface: `0` ok · `1` usage · `2` not found · `3` refused by
policy (FSM, WIP, build gate, duplicate uuid) · `4` internal. **Branch on the
exit code, not on stdout.** A `3` on `--to review` means the gate failed — that
is the system working.

`rheos drift` is worth running before you trust the board: it lists cards whose
files changed outside a recorded rheos write, which is precisely what
hand-editing frontmatter produces.

## 12. What the FSM does not enforce yet

Everything in this document that the FSM cannot check is a convention — which
means it holds only as long as someone reads it. The honest list, so it is clear
what is load-bearing and what is etiquette:

| Rule | Status |
|---|---|
| Legal state transitions | **enforced** by `rheos move` |
| `bin/analyze --strict` before `review` | **enforced** (`:build-gate`) |
| New cards enter at `incoming` | **enforced** (`--force-status` is the deliberate exception) |
| `status` only via `move` | **enforced** (`frontmatter` refuses it) |
| Body frozen after `breakdown` | convention — carded upstream as `rheos-card-body-lock-after-breakdown` |
| Definition of ready (§10) | convention |
| Verification section present | convention |
| WIP limits that actually bind | **not expressible** — see below |

The WIP limits in force are promethean's own (`in_progress` 50, `review` 40,
`ready` 100). Those are eta-mu-monorepo numbers; at shx's size they never fire,
so `todo -> in_progress` is effectively ungated. rheos' `:extends` currently only
overrides `:build-gate` commands, so a tighter limit cannot be set from
`openhax.kanban.edn` today.

When rheos gains first-class workflow descriptions, three things here should stop
being prose and become config:

1. **§10, as per-column entry criteria** — a `markdown-score`-style check on
   `breakdown -> ready` (the non-promethean default FSM already has the hook:
   `{:check :markdown-score}`), so a card without acceptance criteria cannot be
   marked ready.
2. **Per-project WIP limits**, so `todo -> in_progress` is a real pull signal
   rather than a formality.
3. **A `document -> done` check** — docs are a required state on this board, and
   nothing verifies that anything was written.

Until then: this file is the policy, and `bin/analyze` is the only part of it
that cannot be talked out of.

---

Also note: `openhax.kanban.json` is a deprecated mirror and already disagrees
with the EDN (no `:meta`, no build gate). EDN wins discovery, so the JSON is
never read while `openhax.kanban.edn` exists. Do not edit it; do not trust it.
