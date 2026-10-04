# DECISION: <TITLE — the question, not the preferred answer>

<!--
  rheos create --title "DECISION: …" --points 1 --priority P1 --body-file this-file.md
  then: rheos frontmatter <uuid> --set labels="decision, <subsystem>"

The deliverable is A DECISION, RECORDED — not an implementation. This card closes
when the choice exists with its reasoning; the work it unblocks is a separate
card. Do not merge the two: a decision card that also implements cannot be
rejected without wasting the implementation.
Guide: docs/kanban/writing-cards.md
-->

## Outcome

<!-- The choice is recorded at an exact destination, with its reasoning and runner-up. -->

## Context

<!-- The situation and dated sources that establish why this question is real. -->

## The question

<!-- One sentence, ending in a question mark. -->

## Why it needs deciding now

<!-- What is blocked, or what will be expensive to change later. Name the cards. -->

## Constraints

<!--
What any answer must satisfy. This is the section that does the work — most
decisions collapse once the constraints are written down honestly.
-->

-

## Options

### Option A — <name>

<!-- What it is. Then: what it costs, what it buys, what it forecloses. -->

### Option B — <name>

### Option C — do nothing

<!-- Always include this one, and say what happens if it wins. -->

## Recommendation

<!-- One option, with the reason. State your confidence. A recommendation is not the decision. -->

## Acceptance criteria

- [ ] The decision is recorded at <exact destination: a ledger path, AGENTS.md section, or ADR>
- [ ] The runner-up and the reason it lost are recorded with it
- [ ] The cards this unblocks are linked, and moved out of `blocked`

## Verification

<!-- How a reader confirms the decision is recorded and reachable. -->

```bash
```

## Out of scope

- Implementing the decision. That is `<uuid>`.
