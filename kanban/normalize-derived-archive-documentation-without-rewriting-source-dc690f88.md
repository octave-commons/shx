---
uuid: "a6b112a4-9d6c-4f7d-9bdf-d05bdc690f88"
title: "Normalize derived archive documentation without rewriting source"
status: "incoming"
type: "task"
priority: "P2"
points: "3"
labels: "docs, archive, review"
category: "kanban"
write-id: "1791021308010-0.xtb2vhnm22f7lgvthyf"
created_at: "2026-10-03T09:55:08.010Z"
---

## Outcome

Derived documentation from the named historical captures is accessible, correctly
structured, and safe to publish without erasing the original source provenance.

## Context

CodeRabbit review4895165651 on octave-commons/shx#1 identified archive-only
findings40/43/45/46 (IDs below). Source captures remain original documents with
safe-use addenda. This card does not grant runtime/deployment authority.

## Scope

- f3ac8df18e1d9579e0122ef2: docs/inbox/2026.08.09.23.26.12.md opening logo.
- caaf9d91a6eb8b9515206495: docs/inbox/2026.08.10.00.07.37.md duplicate Signal.
- e7e80491581d71779c5cf2ef: same capture, Users and Single ledger heading levels.
- dbf808b35d0d537bfb96961a: opening logos in docs/inbox/2026.08.09.22.19.20.md,
  2026.08.09.23.44.17.md, 2026.08.09.23.28.40.md, and 2026.08.09.23.27.45.md.

## Acceptance criteria

- Publish any normalized text as a derived document retaining source repository,
  full commit, path and original-byte SHA256. Do not rewrite the captured input.
- Each decorative vendor logo in the derived document has empty alt text, or is
  omitted with the source still linked; informative images need descriptive alt.
- The derived event-sourcing note has one unambiguous Signal heading and
  contiguous hierarchy, including Users and Single ledger direct subsections.
- Copy no unsafe NFS or persistence example into a runbook: retain the source's
  qualification addenda and canonical upstream prerequisites.
- Links from the index identify source versus derived documents accurately.

## Verification

Review the derived diffs against the exact cited source revision and hashes;
check image alt attributes, distinct headings and contiguous levels. Run
bin/analyze --strict before presenting the repository change for review.
Record the result with a Rheos comment, not a fabricated status or event.

## Non-goals

Runtime, NFS provisioning, Mongo/Clio/Rheos implementation, deletion or rewriting
of historical receipts/events/source captures, and new repository lint gates.
