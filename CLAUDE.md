# CLAUDE.md

@AGENTS.md

<!--
AGENTS.md is the single source of truth; Codex reads it natively and opencode
loads it via its `instructions` array. Claude Code cannot read AGENTS.md
directly, so this file imports it. Put shared content in AGENTS.md — anything
below this line must be true only for the Claude Code harness.
-->

## Claude Code

- Architecture notes live in `@AGENTS.md` above. Do not restate them here, and
  do not let `/init` append a second copy — if it does, replace the copy with
  the import.
- Run `bb check` before reporting a change complete. It delegates to
  `bin/analyze`; CI runs the same seven checks with `bin/analyze --strict`.
