# <TITLE — the wrong behaviour, stated as the fix>

<!--
  rheos create --title "…" --points N --priority P0 --body-file this-file.md
  then: rheos frontmatter <uuid> --set labels="bug, <subsystem>"
Repro FIRST. A bug card without a reproduction is a rumour, and the first thing
whoever picks it up will do is try to reproduce it — so do that work once, here.
Guide: docs/kanban/writing-cards.md
-->

## Reproduction

<!--
Exact steps or an exact command. Copy-pasteable. Include the input that triggers
it. If it is intermittent, say how often and what you have ruled out.
-->

```bash
```

## Expected

<!-- What should happen, and WHERE that is specified: a test, a docstring, AGENTS.md. -->

## Actual

<!-- What happens. Paste the output, including the error, verbatim. -->

```text
```

## Blast radius

<!--
Who or what is affected, and how bad it is right now. envm-specific: does it
wedge a login shell? That distinction is the repo's core failure-semantics rule —
warn and degrade, never wedge (AGENTS.md › Quality contract).
-->

## Diagnosis

<!--
Where it goes wrong, with file:line, and WHY. Say "not yet diagnosed" rather
than guessing. If the cause is known, the fix belongs in Scope below.
-->

## Acceptance criteria

- [ ] GIVEN the reproduction above WHEN … THEN … (the correct behaviour)
- [ ] A regression test exists that FAILS on the current code and passes after

## Verification

<!--
The regression test must fail before the fix. State how you confirmed that —
`git stash` the fix and re-run — or the test may be asserting nothing.
-->

```bash
bb test
bb check
```

## Scope

-

## Out of scope

<!-- The adjacent bug you found while investigating. Card it separately, link it here. -->

-
