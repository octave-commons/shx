---
category: "kanban"
type: "task"
write-id: "1786347484327-0.p1jvg6uij5ajfchok0u"
points: "1"
title: "Probe: confirm the FSM build gate runs bin analyze"
priority: "P3"
status: "archived"
uuid: "fsm-build-gate-probe"
created_at: "2026-08-10T07:36:34.382Z"
---

# Probe: confirm the FSM build gate runs bin/analyze --strict

## Outcome

The `in_progress -> review` transition on this board is observed to run
`bin/analyze --strict` and to refuse the move on a non-zero exit. This card is the
probe itself: walking it through the states IS the verification.

## Verification

```bash
rheos move fsm-build-gate-probe --to review   # must stream bin/analyze output
```

## Out of scope

Everything. Archive this card once the gate has been observed; the result belongs
in a comment on it.

---
Verified 2026-08-10, both directions.

GREEN: in_progress -> review ran bin/analyze --strict (all seven checks streamed to the terminal), allowed the move, 32s wall clock.

RED: with a deliberate unused-binding added to src/shx/gate_probe.clj, the same move was refused — 'Build gate failed: bin/analyze --strict exited with code 1', CLI exit 3, and the card stayed in in_progress. clj-kondo and clojure-lsp both fired. Probe file deleted; tree back to zero.

Conclusion: openhax.kanban.edn's :fsm {:extends :promethean :build-gate-commands ["bin/analyze --strict"] :cwd "."} is live, and :cwd resolves relative to the config file as documented. Archiving.
---