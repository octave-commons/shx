#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later
# Caller adapter only: bin/analyze remains the definition of SHX's strict gate.
set -euo pipefail
repo=$(cd "$(dirname "$0")/../.." && pwd)
cd "$repo"
toolchain="${RUNNER_TEMP:?RUNNER_TEMP is required}/shx-review-toolchain"
export PATH="$toolchain/bin:$PATH"
export JAVA_HOME="${JAVA_HOME_21_X64:-${JAVA_HOME:-}}"

verify_tools() {
  local observed
  observed=$(java -version 2>&1); printf '%s\n' "$observed"
  [[ "$observed" =~ version\ \"21\. ]] || { echo 'Java 21 is required' >&2; return 1; }
  observed=$(node --version); printf '%s\n' "$observed"
  [[ "$observed" == v24.14.1 ]] || { echo 'Node 24.14.1 is required' >&2; return 1; }
  observed=$(clojure -Sdescribe); printf '%s\n' "$observed"
  grep -qE '^\{?:version "1\.12\.3\.1577"' <<< "$observed" || { echo 'Clojure CLI 1.12.3.1577 is required' >&2; return 1; }
  observed=$(bb --version); printf '%s\n' "$observed"
  [[ "$observed" == 'babashka v1.12.207' ]] || { echo 'Babashka 1.12.207 is required' >&2; return 1; }
  observed=$(clj-kondo --version); printf '%s\n' "$observed"
  [[ "$observed" == 'clj-kondo v2025.07.28' ]] || { echo 'clj-kondo 2025.07.28 is required' >&2; return 1; }
  observed=$(clojure-lsp --version); printf '%s\n' "$observed"
  [[ "${observed%%$'\n'*}" == 'clojure-lsp 2025.08.25-14.21.46' ]] || { echo 'clojure-lsp 2025.08.25-14.21.46 is required' >&2; return 1; }
}

download() {
  local url="$1" destination="$2" digest="$3"
  curl --disable --fail --silent --show-error --location --retry 2 \
    --connect-timeout 20 --max-time 300 -o "$destination" "$url"
  printf '%s  %s\n' "$digest" "$destination" | sha256sum -c -
}

bootstrap() {
  [[ "$(uname -s)" == Linux && "$(uname -m)" == x86_64 ]] || { echo 'Bootstrap requires Linux x86_64' >&2; return 1; }
  [[ -n "$JAVA_HOME" && -x "$JAVA_HOME/bin/java" ]] || { echo 'Runner Java 21 installation is unavailable' >&2; return 1; }
  local observed downloads="$toolchain/downloads"
  observed=$("$JAVA_HOME/bin/java" -version 2>&1); printf '%s\n' "$observed"
  [[ "$observed" =~ version\ \"21\. ]] || { echo 'Runner Java 21 installation is required' >&2; return 1; }
  mkdir -p "$downloads" "$toolchain/bin" "$toolchain/node" "$toolchain/clojure/libexec"
  ln -sf "$JAVA_HOME/bin/java" "$toolchain/bin/java"

  # Fixed digests bind the exact release bytes, not a mutable checksum response.
  # Node/bb/kondo/LSP match published release checksums. Clojure's digest was
  # computed from its official pinned HTTPS archive; no checksum sidecar claim.
  download 'https://nodejs.org/dist/v24.14.1/node-v24.14.1-linux-x64.tar.xz' "$downloads/node.tar.xz" \
    84d38715d449447117d05c3e71acd78daa49d5b1bfa8aacf610303920c3322be
  tar -xJf "$downloads/node.tar.xz" --strip-components=1 -C "$toolchain/node"
  ln -sf "$toolchain/node/bin/node" "$toolchain/bin/node"
  ln -sf "$toolchain/node/bin/npm" "$toolchain/bin/npm"
  ln -sf "$toolchain/node/bin/npx" "$toolchain/bin/npx"

  download 'https://download.clojure.org/install/clojure-tools-1.12.3.1577.tar.gz' "$downloads/clojure.tar.gz" \
    bbf8513ae88b9873e9781ae86adcb560b81209c66fe94cbe72428af39b1ebacc
  tar -xzf "$downloads/clojure.tar.gz" -C "$downloads"
  cp "$downloads/clojure-tools/"{deps.edn,example-deps.edn,tools.edn} "$toolchain/clojure/"
  cp "$downloads/clojure-tools/"*.jar "$toolchain/clojure/libexec/"
  # Quote the installation path as shell data even when runner temp has spaces.
  node - "$downloads/clojure-tools/clojure" "$toolchain/clojure" "$toolchain/bin/clojure" <<'NODE'
const fs = require('node:fs');
const [source, prefix, target] = process.argv.slice(2);
const quoted = `'${prefix.replaceAll("'", "'\\''")}'`;
const input = fs.readFileSync(source, 'utf8');
if (!input.includes('install_dir=PREFIX')) throw new Error('Unexpected pinned CLI installer');
fs.writeFileSync(target, input.replace('install_dir=PREFIX', `install_dir=${quoted}`), { mode: 0o755 });
NODE

  download 'https://github.com/babashka/babashka/releases/download/v1.12.207/babashka-1.12.207-linux-amd64-static.tar.gz' "$downloads/bb.tar.gz" \
    78bd6f9ba967afd4cfc6eb34fca0d9d6fc521c5b5243f4b1ed13ae2e45e6fe4d
  tar -xzf "$downloads/bb.tar.gz" -C "$toolchain/bin" bb
  download 'https://github.com/clj-kondo/clj-kondo/releases/download/v2025.07.28/clj-kondo-2025.07.28-linux-amd64.zip' "$downloads/kondo.zip" \
    801cbd3ee2c2cce094e9db828f4f6a8ceae4bfe20af653ff8ea49af6bc2bf244
  unzip -o -q "$downloads/kondo.zip" -d "$toolchain/bin"
  download 'https://github.com/clojure-lsp/clojure-lsp/releases/download/2025.08.25-14.21.46/clojure-lsp-native-static-linux-amd64.zip' "$downloads/lsp.zip" \
    c191345074bcbec48f3edc14a86a422ab0bc70a20238d6797a4cf5dc53b83a35
  unzip -o -q "$downloads/lsp.zip" -d "$toolchain/bin"
  verify_tools
}

unit() {
  local output status=0
  output=$(mktemp "$RUNNER_TEMP/shx-unit.XXXXXX")
  clojure -M:test > "$output" 2>&1 || status=$?
  cat "$output"
  if [[ "$status" != 0 ]]; then rm -f "$output"; return "$status"; fi
  # Kaocha omits the errors count when zero. Keep its native output; require one
  # completed, nonempty summary in addition to the process's successful exit.
  node - "$output" <<'NODE' || status=$?
const fs = require('node:fs');
const text = fs.readFileSync(process.argv[2], 'utf8').replace(/\x1b\[[0-9;]*m/g, '');
const summaries = [...text.matchAll(/^(\d+) tests, (\d+) assertions, (\d+) failures(?:, (\d+) errors)?\.$/gm)];
const counts = summaries.length === 1 ? summaries[0].slice(1).map((v) => Number(v || 0)) : [];
if (counts.length !== 4 || !counts.every(Number.isSafeInteger)
    || counts[0] <= 0 || counts[1] <= 0 || counts[2] !== 0 || counts[3] !== 0) {
  console.error('Unit evidence requires one completed nonempty Kaocha summary with zero failures/errors.');
  process.exit(1);
}
NODE
  rm -f "$output"
  return "$status"
}

case "${1:-}" in
  bootstrap) bootstrap ;;
  verify-tools) verify_tools ;;
  analyze) bash test/analyze_regression.sh; bin/analyze --strict ;;
  unit) unit ;;
  *) echo 'Usage: shx-review-evidence.sh bootstrap|verify-tools|analyze|unit' >&2; exit 2 ;;
esac
