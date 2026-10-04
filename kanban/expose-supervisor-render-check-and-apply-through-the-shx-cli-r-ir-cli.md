---
uuid: "supervisor-ir-cli"
title: "Expose supervisor render, check and apply through the shx CLI"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "shx, hexis, cli"
parent: "shx-kanban-supervisor-ir"
category: "kanban"
write-id: "1790907623651-0.sc137mtlapgykfjn57q"
created_at: "2026-10-02T02:20:23.651Z"
---

# Expose supervisor render, check and apply through the shx CLI

## Outcome

The shx CLI accepts `supervisor render|check|apply <target> <stack.edn>` with exactly four canonical target tokens: `pm2` (pm2), `compose` (docker compose), `systemd` (systemd user units) and `k8s` (Kubernetes). It validates the desired stack and, for `check` and `apply`, the live-state payload, prints a reviewable change plan, and invokes the corresponding adapter. `apply` requires an explicit target and stack path and reports what it changed.

## Context

Child of `shx-kanban-supervisor-ir`. The existing CLI (`src/shx/cli.clj`) only exposes envm `render`/`check` and legacy IR `emit`/`validate`. Internal adapter functions do not make the planned supervisor runtime usable. This card follows the four adapter stories and their live-state law.

## Acceptance criteria

- [ ] GIVEN a shared EDN fixture WHEN `supervisor render <target> <file>` runs for each target THEN it prints the target's golden output and its unsupported-feature report.
- [ ] GIVEN live state equal to the fixture WHEN `supervisor check <target> <file>` runs THEN it reports zero diff for each target. A changed fixture names the changed unit.
- [ ] GIVEN malformed desired data WHEN any command runs THEN it exits nonzero with the target and validation path. GIVEN malformed live data WHEN `check` or `apply` runs THEN it fails with the target/path, and `apply` invokes no mutation. `render` reads only desired data and does not require or query live state.
- [ ] GIVEN an in-scope change and an out-of-scope resource WHEN `supervisor apply <target> <file>` runs THEN the printed plan names only the in-scope change and the out-of-scope resource remains untouched.
- [ ] VERIFY: CLI tests call `-main` with stubbed I/O for each of the exact target strings `"pm2"`, `"compose"`, `"systemd"` and `"k8s"`, across `render`, `check` and `apply`, including the malformed case; help text lists those same four tokens, the commands and the owned-scope boundary. Unknown tokens (including `"docker"` and `"kubernetes"`) exit nonzero before adapter invocation; descriptive names are not additional aliases.

## Verification

```bash
bb check
clojure -M:test --focus shx.cli-test
```

## Scope

- `src/shx/cli.clj`, `test/shx/cli_test.clj`, `README.md` and CLI help text.

## Anti-patterns

- Do not overload envm's existing top-level `render`/`check` commands with supervisor semantics.
- Do not let `apply` run after a validation failure or on an unspecified target.
