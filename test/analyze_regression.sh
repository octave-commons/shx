#!/usr/bin/env bash
# Fault injection for the executable gate; no real analyzer or source mutation.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
fixture=$(mktemp -d)
trap 'rm -rf "$fixture"' EXIT
mkdir -p "$fixture/bin" "$fixture/tools"
cp "${SHX_ANALYZE_SCRIPT:-$root/bin/analyze}" "$fixture/bin/analyze"

# A closed PATH makes the missing-npx case independent of the host toolchain.
for tool in dirname mktemp grep rm sed tail; do
  ln -s "$(command -v "$tool")" "$fixture/tools/$tool"
done
for tool in clj-kondo clojure clojure-lsp; do
  printf '#!/bin/sh\nexit 0\n' > "$fixture/tools/$tool"
  chmod +x "$fixture/tools/$tool"
done

failures=0
checks=0
for mode in default strict fix; do
  args=()
  case "$mode" in strict) args=(--strict);; fix) args=(--fix);; esac
  for scenario in missing empty-success empty-failure partial-failure unrecognized-success contradictory-success clean; do
    rm -f "$fixture/tools/npx"
    case "$scenario" in
      missing) ;;
      empty-success) printf '#!/bin/sh\nexit 0\n' > "$fixture/tools/npx" ;;
      empty-failure) printf '#!/bin/sh\necho "runner failed" >&2\nexit 47\n' > "$fixture/tools/npx" ;;
      partial-failure) printf '#!/bin/sh\necho "Found 0 clones."\nexit 47\n' > "$fixture/tools/npx" ;;
      unrecognized-success) printf '#!/bin/sh\necho "Starting scan"\nexit 0\n' > "$fixture/tools/npx" ;;
      contradictory-success) printf '#!/bin/sh\necho "Found 1 clones."\nexit 0\n' > "$fixture/tools/npx" ;;
      clean) printf '#!/bin/sh\necho "Found 0 clones."\nexit 0\n' > "$fixture/tools/npx" ;;
    esac
    [ ! -f "$fixture/tools/npx" ] || chmod +x "$fixture/tools/npx"
    status=0
    PATH="$fixture/tools" /bin/bash "$fixture/bin/analyze" "${args[@]}" > "$fixture/output" 2>&1 || status=$?
    checks=$((checks + 1))
    if { [ "$scenario" = clean ] && [ "$status" -eq 0 ]; } ||
       { [ "$scenario" != clean ] && [ "$status" -ne 0 ] && grep -q 'jscpd:' "$fixture/output"; }; then
      printf 'PASS %s/%s\n' "$mode" "$scenario"
    else
      printf 'FAIL %s/%s: exit %s\n' "$mode" "$scenario" "$status"
      cat "$fixture/output"
      failures=$((failures + 1))
    fi
  done
done
printf '%s gate regression cases, %s failures\n' "$checks" "$failures"
[ "$failures" -eq 0 ]
