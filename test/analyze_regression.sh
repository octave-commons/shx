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
printf '#!/bin/sh\nexit 0\n' > "$fixture/tools/clojure"
chmod +x "$fixture/tools/clojure"

write_analyzer() {
  local tool="$1" scenario="$2" banner wrong_banner
  case "$tool" in
    clj-kondo) banner='clj-kondo v2025.07.28'; wrong_banner='clj-kondo v2025.10.23' ;;
    clojure-lsp) banner='clojure-lsp 2025.08.25-14.21.46'; wrong_banner='clojure-lsp 2026.01.01' ;;
  esac
  rm -f "$fixture/tools/$tool"
  [ "$scenario" != missing ] || return 0
  {
    printf '#!/bin/sh\nscenario=%s\nbanner="%s"\nwrong_banner="%s"\ntool=%s\n' "$scenario" "$banner" "$wrong_banner" "$tool"
    cat <<'SHIM'
if [ "${1:-}" = --version ]; then
  case "$scenario" in
    mismatch) echo "$wrong_banner" ;;
    unparseable) echo "unknown version" ;;
    empty) ;;
    failed-probe) echo "version probe failed" >&2; exit 47 ;;
    failed-pinned) echo "$banner"; exit 47 ;;
    misleading-multiline) printf '%s\n%s\n' "$wrong_banner" "$banner" ;;
    clean)
      echo "$banner"
      [ "$tool" != clojure-lsp ] || echo "clj-kondo 2025.07.28"
      ;;
  esac
  exit 0
fi
echo "$tool $*" >> "$SHX_ANALYZER_CALLS"
exit 0
SHIM
  } > "$fixture/tools/$tool"
  chmod +x "$fixture/tools/$tool"
}
export SHX_ANALYZER_CALLS="$fixture/analyzer-calls"
write_analyzer clj-kondo clean
write_analyzer clojure-lsp clean

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

  # Execute the actual gate with one analyzer boundary changed at a time.
  # A refused version must stop before either analyzer performs analysis.
  for tool in clj-kondo clojure-lsp; do
    for scenario in missing mismatch unparseable empty failed-probe failed-pinned misleading-multiline clean; do
      write_analyzer clj-kondo clean
      write_analyzer clojure-lsp clean
      write_analyzer "$tool" "$scenario"
      rm -f "$SHX_ANALYZER_CALLS"
      status=0
      PATH="$fixture/tools" /bin/bash "$fixture/bin/analyze" "${args[@]}" > "$fixture/output" 2>&1 || status=$?
      checks=$((checks + 1))
      if { [ "$scenario" = clean ] && [ "$status" -eq 0 ] &&
           grep -q '^clj-kondo --lint src test$' "$SHX_ANALYZER_CALLS" &&
           grep -q '^clojure-lsp diagnostics$' "$SHX_ANALYZER_CALLS"; } ||
         { [ "$scenario" != clean ] && [ "$status" -eq 2 ] &&
           [ ! -s "$SHX_ANALYZER_CALLS" ] && grep -q "$tool" "$fixture/output"; }; then
        printf 'PASS %s/%s/%s\n' "$mode" "$tool" "$scenario"
      else
        printf 'FAIL %s/%s/%s: exit %s\n' "$mode" "$tool" "$scenario" "$status"
        cat "$fixture/output"
        failures=$((failures + 1))
      fi
    done
  done
done
printf '%s gate regression cases, %s failures\n' "$checks" "$failures"
[ "$failures" -eq 0 ]
