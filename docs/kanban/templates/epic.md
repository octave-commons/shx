# EPIC: <TITLE — the end state, not the activity>

<!--
  rheos create --type epic --title "…" --priority P1 --body-file this-file.md
Children are created with --parent <this epic's uuid>. An epic is not a folder:
if it has no end state you can describe in one sentence, it is a label.
Guide: docs/kanban/writing-cards.md
-->

## Outcome

<!--
One paragraph, present tense. The state of the world when every child is done.
If you cannot write this without listing the children, it is not an epic yet.
-->

## Context

<!-- Why now, and what discovery led here. Provenance on every fact. -->

## The core identity

<!--
Optional but high value: the one sentence that makes the children obviously the
same work. Example, from hexis-unification-epic: "The ledger is the repeated act;
the projection is the settled state you run." Delete if you do not have one.
-->

## Children

<!--
One line each: uuid — what it delivers — blocking relationship.
Mark the hard blocker explicitly; an epic with no ordering is a wish list.
-->

- `<uuid>` — <what it delivers> — **hard blocker for the rest**
- `<uuid>` — <what it delivers>

## Acceptance criteria

- [ ] GIVEN <starting state> WHEN <completed capability is used> THEN <observable end state>

## Definition of done

<!--
The observable end state. One or two sentences. This is what closes the epic —
not "all children done", which is a tautology.
-->

## Verification

```bash
```

## Out of scope

<!-- The adjacent epic this is NOT. -->

-

## Open questions

<!-- Undecided things that change the shape of the children. Delete when empty. -->
