# Board docs

The board itself is `../../kanban` — one markdown file per card, governed by the
promethean FSM configured in `../../openhax.kanban.edn`.

| File | What |
|---|---|
| [`writing-cards.md`](writing-cards.md) | How to write a card here: the FSM contract, card anatomy, the rules, anti-patterns, definition of ready, verified commands |
| [`templates/`](templates/) | Body templates: task, epic, bug, decision, spike |

Templates live **here and not under `kanban/`** on purpose: rheos discovers cards
by walking every `*.md` under the task root, including dot-directories, so a
template stored there would appear on the board as a card.

Create cards with the CLI, never by hand — the FSM, the ledger, and the write-id
all depend on it:

```bash
alias rheos='node ~/spaces/eta-mu/packages/rheos/dist/cli.cjs'
rheos create --title "…" --points 2 --priority P1 --body-file /tmp/card.md
```
