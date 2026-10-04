#!/usr/bin/env bash
# Real pinned Heretic status/clean against disposable caches; no mutation run.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
source_root=${SHX_MUTATE_SOURCE_ROOT:-$root}
fixture=$(mktemp -d "${TMPDIR:-/tmp}/shx-mutate-XXXXXX")
trap 'if [ "${SHX_MUTATE_KEEP_FIXTURE:-0}" != 1 ]; then rm -rf "$fixture"; fi' EXIT
mkdir -p "$fixture/bin" "$fixture/src" "$fixture/test"
cp "$source_root/bin/mutate" "$fixture/bin/mutate"
if [ -f "$source_root/bin/mutate-cache.clj" ]; then
  cp "$source_root/bin/mutate-cache.clj" "$fixture/bin/mutate-cache.clj"
fi
cp "$source_root/deps.edn" "$fixture/deps.edn"

checks=0
failures=0
check() {
  checks=$((checks + 1))
  if "$@"; then
    printf 'PASS %s\n' "$label"
  else
    printf 'FAIL %s\n' "$label"
    failures=$((failures + 1))
  fi
}
prepare() {
  rm -rf "$fixture/.heretic" "$fixture/.heretic-sandbox" "$fixture/mutation sandbox"
  sandbox=$1
  mkdir -p "$sandbox/.heretic" "$sandbox/src" "$sandbox/test" "$fixture/.heretic"
  cat > "$fixture/heretic.edn" <<EOF
{:source-paths ["src"] :test-paths ["test"] :test-namespaces []
 :heretic-dir ".heretic" :sandbox-dir "${2:-$sandbox}" :sandbox-aliases ["heretic"]}
EOF
  cp "$fixture/heretic.edn" "$sandbox/heretic.edn"
  cp "$fixture/deps.edn" "$sandbox/deps.edn"
  # Persisted EDN is consumed by the real Heretic load-index implementation.
  printf '{}\n' > "$sandbox/.heretic/index.edn"
  printf '{:survivors []}\n' > "$fixture/.heretic/mutation-results.edn"
}
invoke() {
  status=0
  bash "$fixture/bin/mutate" "$@" > "$fixture/output" 2>&1 || status=$?
  cat "$fixture/output"
}

printf 'Fixture: %s\n' "$fixture"
prepare "$fixture/.heretic-sandbox" '.heretic-sandbox'
invoke status
label='status reads the retained mutation index'
check grep -q 'Coverage index: present' "$fixture/output"
label='status preserves the project results'
check test -f "$fixture/.heretic/mutation-results.edn"

invoke clean
label='clean deletes the index reused by mutation'
check test ! -e "$sandbox/.heretic"
label='clean preserves the copied project results'
check test -f "$fixture/.heretic/mutation-results.edn"

mkdir -p "$fixture/.heretic"
printf '{}\n' > "$fixture/.heretic/index.edn"
invoke status
label='status ignores a project index after sandbox clean'
check grep -q 'Coverage index: missing' "$fixture/output"

prepare "$fixture/mutation sandbox"
invoke status
label='status respects configured sandbox paths with spaces'
check grep -q 'Coverage index: present' "$fixture/output"
invoke clean
label='clean respects the configured sandbox path'
check test ! -e "$sandbox/.heretic"
label='custom sandbox clean preserves project results'
check test -f "$fixture/.heretic/mutation-results.edn"

prepare "$fixture/.heretic-sandbox" '.heretic-sandbox'
printf '{invalid\n' > "$sandbox/.heretic/index.edn"
invoke status
label='sandbox index read failure propagates a nonzero child exit'
check test "$status" -ne 0
label='sandbox index read failure preserves project results'
check test -f "$fixture/.heretic/mutation-results.edn"

prepare "$fixture/.heretic-sandbox"
rm -rf "$sandbox"
invoke clean
label='missing sandbox fails instead of cleaning the project'
check test "$status" -ne 0
label='missing sandbox leaves project results intact'
check test -f "$fixture/.heretic/mutation-results.edn"

# Dispatch-only probes: capture argv/exit without running collect/mutate/watch.
mkdir -p "$fixture/tools"
cat > "$fixture/tools/clojure" <<'EOF'
#!/bin/bash
printf '%s\0' "$@" > "$SHX_MUTATE_ARGV"
exit 37
EOF
chmod +x "$fixture/tools/clojure"
for cmd in default collect mutate survivors no-coverage watch unknown; do
  case "$cmd" in
    default) args=(); expected=(mutate);;
    collect) args=(collect --force); expected=("${args[@]}");;
    mutate) args=(mutate --files 'src/a b.clj'); expected=("${args[@]}");;
    *) args=("$cmd"); expected=("${args[@]}");;
  esac
  printf '%s\0' -M:heretic -m heretic.core "${expected[@]}" > "$fixture/expected"
  status=0
  SHX_MUTATE_ARGV="$fixture/argv" PATH="$fixture/tools:$PATH" \
    bash "$fixture/bin/mutate" "${args[@]}" > "$fixture/output" 2>&1 || status=$?
  label="$cmd preserves the existing dispatch and argument boundaries"
  check cmp -s "$fixture/expected" "$fixture/argv"
  label="$cmd preserves the child exit status"
  check test "$status" -eq 37
done
printf '%s mutation adapter checks, %s failures\n' "$checks" "$failures"
[ "$failures" -eq 0 ]
