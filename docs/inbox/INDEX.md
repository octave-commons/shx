# inbox

Unprocessed captures. Each line names where the note is *going*, not where it is.
Delete a row when its content has been absorbed into a ledger, spec, or skill.

## Lineage of this repo — absorb into AGENTS.md / resource ledgers

| note | contains | destination |
|---|---|---|
| `2026.08.09.20.21.57.md` | eshell/closh/scsh/nushell ancestors; the `source`-inside-`source` bubbling problem; "bash stays the engine, Clojure becomes the brain that renders it" | **This is the origin story of `envm`.** The bubbling paragraph justifies `domain.merge/fold-fragment-tree`. Belongs in shx `AGENTS.md` as design rationale. |
| `2026.08.09.22.15.54.md` | "I want to avoid `/init`"; can CLAUDE.md be a symlink; keep prompts separate and combine as needed; knoxx as glass prototype | **Resolved this session.** → `~/.ημ/resources/structure/skill-repository.edn :structure/resources-are-truth` |
| `2026.08.09.22.17.33.md` | the `/init` + OPENCODE.md/CODEX.md question | **Resolved.** No CODEX.md or OPENCODE.md needed; see `~/.ημ/INDEX.edn :targets` |
| `2026.08.09.22.19.20.md` | closh is on hiatus since 2022; maintainer recommends fish + babashka | Supports the "render bash, don't replace it" decision. Fold one sentence into AGENTS.md, drop the rest. |

## Event sourcing — absorb into clio

| note | contains | destination |
|---|---|---|
| `2026.08.10.00.07.37.md` | event-sourcing axioms; simplest nbb EDN append-only ledger; single vs. several ledger files | Compare against `packages/clio/README.md` — clio already answers the partition question ("physical file order and partitioning have no semantic authority"). Keep the delta only. |

## Naming lattice — the actor model

| note | contains | destination |
|---|---|---|
| `2026.08.09.23.28.40.md` | Keryx (heralds) as message passing over Axxium + Clio | Naming ledger |
| `2026.08.10.00.17.16.md` | Keryx / Axxium / Clio / Nomos / Psephisma / Praxis role assignments | **The canonical lattice.** Should become `~/.ημ/resources/structure/naming.edn`, one event per name. |

## Reference — keep, low urgency

| note | contains | destination |
|---|---|---|
| `2026.08.09.23.27.45.md` | Methodology vs Process | `docs/reference/`. Two byte-identical copies deleted 2026-08-10. |
| `2026.08.09.23.44.17.md` | portable `.cljs` across shadow-cljs and nbb | Directly relevant — clio already does this (`.cljc` + `.cljs`, no `.nbb` logic). Merge into a build note. |
| `2026.08.09.23.26.12.md` | DigitalOcean NFS is VPC-only; mount on droplet, reach via SSHFS | Unrelated to this repo. Move to an ops notes area. |
| `2026.08.09.22.59.57.md` | repo links; eta-mu#282 follows #280, "282 is probably ready to merge" | **Actionable, not a note.** Move to kanban. |

## Session noise — safe to delete once skimmed

| note | why |
|---|---|
| `2026.08.09.22.14.06.md` | bare version/OS/session header, no content |
| `2026.08.09.22.20.37.md` | raw session transcript dump |
