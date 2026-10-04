// SPDX-License-Identifier: GPL-3.0-or-later
// Executes the actual caller adapter in disposable toolchain fixtures.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const repo = path.resolve(__dirname, '..');
const script = process.env.SHX_EVIDENCE_SCRIPT
  || path.join(repo, '.github/scripts/shx-review-evidence.sh');
const node = process.execPath;
const quote = (value) => `'${value.replaceAll("'", "'\\''")}'`;

function fixture(t) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'shx-caller-'));
  t.after(() => fs.rmSync(root, { recursive: true, force: true }));
  const tools = path.join(root, 'runner/shx-review-toolchain/bin');
  fs.mkdirSync(tools, { recursive: true });
  const javaHome = path.join(root, 'jdk');
  fs.mkdirSync(path.join(javaHome, 'bin'), { recursive: true });
  fs.mkdirSync(path.join(root, '.github/scripts'), { recursive: true });
  fs.copyFileSync(script, path.join(root, '.github/scripts/shx-review-evidence.sh'));
  const put = (name, body) => {
    fs.writeFileSync(path.join(tools, name), `#!/bin/bash\n${body}\n`, { mode: 0o755 });
  };
  put('java', 'echo \'openjdk version "21.0.12.1"\' >&2');
  fs.copyFileSync(path.join(tools, 'java'), path.join(javaHome, 'bin/java'));
  put('node', `if [[ "$1" == --version ]]; then echo v24.14.1; else exec ${quote(node)} "$@"; fi`);
  put('clojure', 'if [[ "$1" == -Sdescribe ]]; then echo \'{:version "1.12.3.1577"}\'; else echo "19 tests, 46 assertions, 0 failures."; fi');
  put('bb', 'echo "babashka v1.12.207"');
  put('clj-kondo', 'echo "clj-kondo v2025.07.28"');
  put('clojure-lsp', 'printf "clojure-lsp 2025.08.25-14.21.46\\nclj-kondo 2025.07.28\\n"');
  const run = (mode) => spawnSync('/bin/bash', ['.github/scripts/shx-review-evidence.sh', mode], {
    cwd: root,
    env: { ...process.env, RUNNER_TEMP: path.join(root, 'runner'), JAVA_HOME_21_X64: javaHome,
      PATH: `${tools}:/usr/bin:/bin` },
    encoding: 'utf8', timeout: 30000,
  });
  return { root, tools, javaHome, put, run };
}

for (const [name, output, status, passes] of [
  ['real nonempty summary', '\u001b[32m19 tests, 46 assertions, 0 failures.\u001b[m', 0, true],
  ['empty successful process', '', 0, false],
  ['zero tests', '0 tests, 0 assertions, 0 failures.', 0, false],
  ['zero assertions', '19 tests, 0 assertions, 0 failures.', 0, false],
  ['reported failure', '19 tests, 46 assertions, 1 failures.', 0, false],
  ['reported error', '19 tests, 46 assertions, 0 failures, 1 errors.', 0, false],
  ['runner error despite clean output', '19 tests, 46 assertions, 0 failures.', 47, false],
  ['unrecognized output', 'Starting suite', 0, false],
  ['contradictory summaries', '0 tests, 0 assertions, 0 failures.\n19 tests, 46 assertions, 0 failures.', 0, false],
]) {
  test(`unit evidence: ${name}`, (t) => {
    const f = fixture(t);
    f.put('clojure', `printf '%s\\n' ${quote(output)}; exit ${status}`);
    const result = f.run('unit');
    assert.equal(result.error, undefined);
    assert.equal(result.status === 0, passes, result.stdout + result.stderr);
    assert.ok(result.stdout.includes(output), 'preserve actual runner output');
    if (status) assert.equal(result.status, status, 'preserve runner exit');
  });
}

test('toolchain: all actual pinned versions', (t) => {
  const result = fixture(t).run('verify-tools');
  assert.equal(result.status, 0, result.stdout + result.stderr);
});

for (const [name, command, body] of [
  ['wrong CLI', 'clojure', 'echo \'{:version "1.12.2.1565"}\''],
  ['wrong bb', 'bb', 'echo "babashka v1.13.219"'],
  ['wrong kondo', 'clj-kondo', 'echo "clj-kondo v2025.10.23"'],
  ['wrong LSP', 'clojure-lsp', 'echo "clojure-lsp 2026.01.01"'],
  ['wrong Node', 'node', 'echo v22.0.0'],
  ['wrong Java', 'java', 'echo \'openjdk version "17.0.1"\' >&2'],
  ['bb unavailable', 'bb', 'echo "bb unavailable" >&2; exit 127'],
  ['LSP unavailable', 'clojure-lsp', 'echo "LSP unavailable" >&2; exit 127'],
]) {
  test(`toolchain: ${name} refuses instead of fallback`, (t) => {
    const f = fixture(t);
    f.put(command, body);
    const result = f.run('verify-tools');
    assert.notEqual(result.status, 0, result.stdout + result.stderr);
  });
}

test('bootstrap: download failure remains visible and stops installation', (t) => {
  const f = fixture(t);
  f.put('curl', 'echo "fixture download failure" >&2; exit 22');
  const result = f.run('bootstrap');
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /fixture download failure/);
  assert.equal(fs.existsSync(path.join(f.root, 'runner/shx-review-toolchain/clojure/libexec/clojure-tools-1.12.3.1577.jar')), false);
});

test('bootstrap: checksum mismatch stops before unpacking', (t) => {
  const f = fixture(t);
  f.put('curl', 'while [[ "$1" != -o ]]; do shift; done; printf "forged archive" > "$2"');
  f.put('tar', 'echo "UNSAFE unpack" >&2; exit 0');
  const result = f.run('bootstrap');
  assert.notEqual(result.status, 0);
  assert.match(result.stdout + result.stderr, /FAILED/);
  assert.doesNotMatch(result.stdout + result.stderr, /UNSAFE unpack/);
});

test('analyze delegates both existing regression and strict gate without fixing source', (t) => {
  const f = fixture(t);
  fs.mkdirSync(path.join(f.root, 'test'));
  fs.mkdirSync(path.join(f.root, 'bin'));
  fs.writeFileSync(path.join(f.root, 'test/analyze_regression.sh'), 'echo existing-regression\n');
  fs.writeFileSync(path.join(f.root, 'bin/analyze'), '#!/bin/bash\nprintf "gate:%s\\n" "$*"\nexit 47\n', { mode: 0o755 });
  const result = f.run('analyze');
  assert.equal(result.status, 47);
  assert.match(result.stdout, /existing-regression/);
  assert.match(result.stdout, /gate:--strict/);
  assert.doesNotMatch(result.stdout, /--fix/);
});

test('unknown adapter verb fails', (t) => {
  assert.equal(fixture(t).run('not-a-command').status, 2);
});

test('workflow: enabled guarded review; native scope and identity inputs preserved', () => {
  const yaml = fs.readFileSync(path.join(repo, '.github/workflows/eta-mu-evidence-review.yml'), 'utf8');
  assert.match(yaml, /name: eta-mu evidence review/);
  assert.match(yaml, /name: Evidence-first review \(eta-mu\)/);
  assert.match(yaml, /if: \$\{\{ github\.event\.pull_request\.draft == false && github\.event\.pull_request\.head\.repo\.full_name == github\.repository \}\}/);
  assert.match(yaml, /pr_head_sha: \$\{\{ github\.event\.pull_request\.head\.sha \}\}/);
  assert.match(yaml, /model: opencode\/mimo-v2\.6-flash-free/);
  assert.match(yaml, /muse_revision: 0b9a91492c8355e6933dc2164d35668cb76d9e60/);
  assert.match(yaml, /setup_eta_mu_toolchain: false/);
  assert.match(yaml, /ETA_MU_APP_ID: \$\{\{ secrets\.ETA_MU_APP_ID \}\}/);
  assert.match(yaml, /ETA_MU_APP_PRIVATE_KEY: \$\{\{ secrets\.ETA_MU_APP_PRIVATE_KEY \}\}/);
  assert.doesNotMatch(yaml, /secrets: inherit|pull_request_target|auto.merge|permission.*write|DISCORD_REVIEW_WEBHOOK_URL/);
});

test('actual enabled guarded review invocation accepts only ready same-repository PRs', () => {
  const yaml = fs.readFileSync(path.join(repo, '.github/workflows/eta-mu-evidence-review.yml'), 'utf8');
  const review = yaml.split('  evidence_review:\n')[1];
  assert.ok(review, 'actual reusable review invocation');
  assert.match(review, /^    needs: caller_contract$/m);
  const condition = /^    if: \$\{\{ (.+) \}\}$/m.exec(review);
  assert.ok(condition, 'explicit review invocation guard');
  // Evaluate the actual boolean expression; do not replace it with a fixture predicate.
  const enabled = new Function('github', `return (${condition[1]});`);
  for (const [draft, headRepo, expected] of [
    [false, 'octave-commons/shx', true],
    [true, 'octave-commons/shx', false],
    [false, 'contributor/shx', false],
    [true, 'contributor/shx', false],
  ]) {
    const github = { repository: 'octave-commons/shx',
      event: { pull_request: { draft, head: { repo: { full_name: headRepo } } } } };
    assert.equal(enabled(github), expected, `draft=${draft}, head=${headRepo}`);
  }
});

test('actual workflow gate snippet handles bootstrap failure despite run_gate returning zero', (t) => {
  const f = fixture(t);
  const yaml = fs.readFileSync(path.join(repo, '.github/workflows/eta-mu-evidence-review.yml'), 'utf8');
  const match = /      evidence_gates_script: \|\n((?:        .*\n)+)/.exec(yaml);
  assert.ok(match, 'actual reusable input script');
  const snippet = match[1].replace(/^        /gm, '');
  f.put('curl', 'exit 22');
  const harness = `set +e\nstatuses=${quote(path.join(f.root, 'statuses'))}\n: > "$statuses"\nrun_gate() { local name="$1"; shift; "$@"; local rc=$?; echo "$name=$rc" >> "$statuses"; return 0; }\n${snippet}\ncat "$statuses"`;
  const result = spawnSync('/bin/bash', ['-c', harness], { cwd: f.root,
    env: { ...process.env, RUNNER_TEMP: path.join(f.root, 'runner'),
      JAVA_HOME_21_X64: f.javaHome, PATH: `${f.tools}:/usr/bin:/bin` }, encoding: 'utf8' });
  assert.equal(result.status, 0, 'source summarizes statuses independently');
  assert.match(result.stdout, /bootstrap=22/);
  for (const name of ['lint', 'test', 'caller_regressions']) assert.match(result.stdout, new RegExp(`${name}=125`));
});

for (const [name, lint, output, caller, expected] of [
  ['all completed', 0, '19 tests, 46 assertions, 0 failures.', 0, [0, 0, 0, 0]],
  ['lint failed', 47, '19 tests, 46 assertions, 0 failures.', 0, [0, 47, 0, 0]],
  ['empty unit failed', 0, '', 0, [0, 0, 1, 0]],
  ['caller regression failed', 0, '19 tests, 46 assertions, 0 failures.', 43, [0, 0, 0, 43]],
]) {
  test(`actual workflow status contract: ${name}`, (t) => {
    const f = fixture(t);
    // Isolate orchestration: bootstrap's real downloads/checksums are tested
    // separately. This disposable shim has no production override flag.
    const adapter = path.join(f.root, '.github/scripts/shx-review-evidence.sh');
    fs.writeFileSync(adapter, fs.readFileSync(adapter, 'utf8').replace('bootstrap) bootstrap ;;', 'bootstrap) exit 0 ;;'));
    fs.mkdirSync(path.join(f.root, 'test'));
    fs.mkdirSync(path.join(f.root, 'bin'));
    fs.writeFileSync(path.join(f.root, 'test/analyze_regression.sh'), 'exit 0\n');
    fs.writeFileSync(path.join(f.root, 'bin/analyze'), `#!/bin/bash\nexit ${lint}\n`, { mode: 0o755 });
    f.put('clojure', `printf '%s\\n' ${quote(output)}`);
    f.put('node', `if [[ "$1" == --test ]]; then exit ${caller}; else exec ${quote(node)} "$@"; fi`);
    const yaml = fs.readFileSync(path.join(repo, '.github/workflows/eta-mu-evidence-review.yml'), 'utf8');
    const snippet = /      evidence_gates_script: \|\n((?:        .*\n)+)/.exec(yaml)[1].replace(/^        /gm, '');
    const statuses = path.join(f.root, 'statuses');
    const harness = `set +e\nstatuses=${quote(statuses)}\n: > "$statuses"\nrun_gate() { local name="$1"; shift; "$@"; local rc=$?; echo "$name=$rc" >> "$statuses"; return 0; }\n${snippet}`;
    const result = spawnSync('/bin/bash', ['-c', harness], { cwd: f.root,
      env: { ...process.env, RUNNER_TEMP: path.join(f.root, 'runner'), JAVA_HOME_21_X64: f.javaHome,
        PATH: `${f.tools}:/usr/bin:/bin` }, encoding: 'utf8', timeout: 30000 });
    assert.equal(result.status, 0, result.stdout + result.stderr);
    assert.deepEqual(fs.readFileSync(statuses, 'utf8').trim().split('\n'),
      ['bootstrap', 'lint', 'test', 'caller_regressions'].map((key, i) => `${key}=${expected[i]}`));
  });
}
