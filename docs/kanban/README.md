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

Plain Markdown authoring of incoming cards is supported. Never fabricate engine
write IDs/events or hand-edit lifecycle state. Use the canonical Rheos CLI for
create, comments, descriptive frontmatter updates, and transitions; once scope
leaves breakdown, clarify it through comments.

With an upstream-installed `rheos` executable on PATH, invoke it directly:

```bash
rheos help
rheos create --title "…" --points 2 --priority P1 --body-file /tmp/card.md --config openhax.kanban.edn
```

If using an upstream-built artifact instead, set `RHEOS_CLI` to that artifact's
absolute path; this avoids assuming a particular developer checkout. Verify its
help before use:

```bash
node "$RHEOS_CLI" help
node "$RHEOS_CLI" create --title "…" --points 2 --priority P1 --body-file /tmp/card.md --config openhax.kanban.edn
```
