---
uuid: "1fee3b27-c3ff-5e23-b325-0c0411f997da"
title: "Adopt the pinned Antigravity reviewer for shx"
status: incoming
type: task
priority: P1
points: 2
labels: shx, docs
category: chores
---

## Outcome

This repository consumes the shared advisory reviewer with the exact requested
`gemini-3.8-flash-high` model through its existing review and qualification process.

## Context

This is an incoming source proposal for `octave-commons/shx`, following
[Foresight rollout planning](https://github.com/open-hax/foresight/pull/145) and
[eta-mu provider planning](https://github.com/open-hax/eta-mu/pull/364). The root planning record is commit
`1f3f7317012e23d7ec0f35a7e22b2b01cdff65b3`. Eta-mu owns reviewer semantics;
Muse provides only organization-local compatibility bytes for Octave Commons.
The checked-in caller records the exact immutable workflow and runtime pins.
The deployed trusted runtime must match that runtime pin; deployment proof is
recorded separately from this incoming planning card.

## Acceptance criteria

- [ ] VERIFY: the caller contains one full 40-character lowercase hexadecimal
  organization-local workflow pin matching the approved coordinator revision;
  the count of mutable workflow references is zero.
- [ ] VERIFY: `review_runtime_revision` is one full 40-character lowercase
  hexadecimal commit matching the reviewed coordinator plan and installed
  trusted runtime; the count of mutable runtime references is zero.
- [ ] VERIFY: the model is exactly `gemini-3.8-flash-high` and the count of
  fallback models is zero.
- [ ] VERIFY: the caller grants only `contents: read` and `pull-requests: read`;
  forwarded secrets and write/signing/deployment credentials each occur zero times.
- [ ] GIVEN a PR whose current head or base differs from the requested immutable
  tuple, WHEN preflight runs, THEN it fails before scheduling native inference.
- [ ] VERIFY: the preinstalled runtime's HEAD matches the declared revision and
  its nonignored tracked/untracked dirty-path count is zero.
- [ ] VERIFY: the native CLI SHA-256 equals its approved operator pin;
  every unapproved CLI digest fixture is refused.
- [ ] VERIFY: the Bubblewrap SHA-256 equals its approved operator pin;
  every unapproved Bubblewrap digest fixture is refused.
- [ ] GIVEN a candidate attempting read, write, command, or MCP effects, WHEN the
  strict observer profile runs, THEN the permitted effect count is zero.
- [ ] VERIFY: the native invocation emits exactly one finish-only terminal
  result, without streaming terminal updates.
- [ ] VERIFY: the AGY process and every model descendant receive zero GitHub
  credentials of any scope, including read-only tokens and GitHub App tokens;
  environment captures with injected credential variables show zero inherited
  credential-bearing entries.
- [ ] VERIFY: retained input passes all runtime size and complete-scope
  admission checks with zero refused or truncated input sections.
- [ ] VERIFY: organization group readback admits exactly the approved immutable
  workflow and repository-ID set, with zero unrestricted workflow entries.
- [ ] VERIFY: the Octave Commons mirror SHA-256 equals the approved eta-mu
  workflow SHA-256.
- [ ] GIVEN a completed or failed inference attempt, WHEN cleanup finishes, THEN
  its isolated model workspace and owned child process count are both zero.
- [ ] VERIFY: `bin/analyze --strict`, caller syntax validation, and the mapped
  rollout check each exit zero.
- [ ] VERIFY: the package-manager, application, branch-protection and approval
  policy diff contains zero changes in this slice.
- [ ] VERIFY: the current PR head has fresh eligible review and all existing
  required checks satisfied; imported CLI artifacts supply zero native approvals.
- [ ] GIVEN a real hosted invocation, WHEN evidence is inspected, THEN its
  base/head and diff digest match the requested input, every admitted changed
  path is covered, and retained native output records the exact selected model.
- [ ] GIVEN worker unavailability, missing login, or malformed model output,
  WHEN the workflow runs, THEN it records failure or unavailability and emits
  zero successful review results.

## Verification

Run `bin/analyze --strict`, `actionlint .github/workflows/antigravity-review.yml`,
and `git diff --check`,
then the root rollout tool's exact mapped `check` documented in
[Foresight rollout notes](https://github.com/open-hax/foresight/pull/145/files).
Those local source checks do not establish hosted runner or model availability.
Existing deterministic repository checks and required review remain necessary.

## Scope

- `.github/workflows/antigravity-review.yml` is the thin caller proposal.
- `kanban/antigravity-review-rollout.md` is the incoming planning document.
- `receipts.edn` is the append-only local accountability record.
- `.ημ/session-mycology/ledger.md` is the append-only reflection record.
- The expected reusable workflow route is `octave-commons/muse/.github/workflows/antigravity-code-review.yml`.

## Status and non-goals

This document remains **incoming**. It is hand-authored planning input, with no
Rheos admission, transition, write-id or engine event asserted. Shared worker
registration has been observed; this repository's final source adoption,
exact-head review/CI and hosted qualification remain pending. No merge, deployment, credential copying, branch-policy change,
new board engine, or application/runtime implementation is authorized here.
