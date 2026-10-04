# SPIKE: <TITLE — the question to answer>

<!--
  rheos create --title "SPIKE: …" --points N --priority P2 --body-file this-file.md
  then: rheos frontmatter <uuid> --set labels="spike, <subsystem>"

A spike buys KNOWLEDGE, not code. It is time-boxed, and its output is written
down somewhere durable — a spike that ends in someone's head produced nothing and
will be re-run by the next reader.
Guide: docs/kanban/writing-cards.md
-->

## Outcome

<!-- The question has a written answer, or a recorded partial answer when the time box expires. -->

## Context

<!-- The discovery and dated sources behind the question; label what remains unknown. -->

## Question

<!-- One sentence. Answerable. If it has three parts, it is three spikes. -->

## Why we cannot just decide

<!-- What is genuinely unknown. If the answer is already knowable from the repo, this is not a spike — go read it. -->

## Time box

<!--
An explicit budget: "2 hours" / "3 points, stop and report whatever is known".
Without this a spike expands to fill the sprint. Stopping at the box WITH A
PARTIAL ANSWER is a success, not a failure.
-->

## Method

<!--
How the question gets answered, concretely: which experiment, which docs, which
prototype. Name the smallest thing that would settle it.
-->

-

## Definition of done

<!-- The answer is written at <exact destination>. Not "we understand X better". -->

## Acceptance criteria

- [ ] The question is answered, or the time box expired and the partial answer is recorded
- [ ] The answer is written at <docs/inbox/…, a ledger, or the card that consumes it>
- [ ] Claims are marked verified/unverified, with how they were checked
- [ ] The follow-on cards this enables are created, or explicitly not needed

## Verification

<!-- How a reader confirms the answer is real: the command that reproduces it, the file that holds it. -->

```bash
```

## Out of scope

- Shipping the thing this spike is about. Prototype code is throwaway; say so
  here, and delete it when the spike closes.
