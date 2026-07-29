#!/usr/bin/env bash
# Selective Android instrumentation test entrypoint for CI.
# Replaces: ./gradlew assembleDebugAndroidTest assembleDevDebugAndroidTest && ./gradlew runFlank
#
# Env:  BASE_SHA, HEAD_SHA, FLAVOUR (default: dev), DRY_RUN (1=print only),
#       ALWAYS_RUN_MODULES, STRICT_MISSING_TESTS, SMOKE_TEST_MODULE (:app)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
GLOBAL_INVALIDATE_FILE="${TMPDIR:-/tmp}/ci_global_invalidate${CI_JOB_ID:+_${CI_JOB_ID}}"
SMOKE_FALLBACK_FILE="${TMPDIR:-/tmp}/ci_smoke_fallback${CI_JOB_ID:+_${CI_JOB_ID}}"
FLAVOUR="${FLAVOUR:-dev}"
DRY_RUN="${DRY_RUN:-0}"
SMOKE_TEST_MODULE="${SMOKE_TEST_MODULE:-:app}"

log() { echo "[selective_tests] $*" >&2; }
die() { echo "[selective_tests] ERROR: $*" >&2; exit 1; }

capitalize() { local s="$1"; echo "$(tr '[:lower:]' '[:upper:]' <<< "${s:0:1}")${s:1}"; }

run_gradle() {
  if [ "$DRY_RUN" = "1" ]; then log "[DRY_RUN] ./gradlew $*"
  else log "Running: ./gradlew $*"; cd "$REPO_ROOT" && ./gradlew "$@"; fi
}

run_full_suite() {
  log "Running full instrumented test suite."
  run_gradle assembleDebugAndroidTest "assemble$(capitalize "$FLAVOUR")DebugAndroidTest"
  run_gradle runFlank
}

# -- Step 1: Detect changed modules ----------------------------------------
log "=== Step 1: Detect changed modules ==="
detector_out=$("${BASH:-bash}" "$SCRIPT_DIR/detect_changed_modules.sh") || {
  log "ERROR: detect_changed_modules.sh failed (exit $?) — falling back to full test run."
  run_full_suite; exit 0
}

GLOBAL_INVALIDATE=0
[ -f "$GLOBAL_INVALIDATE_FILE" ] && source "$GLOBAL_INVALIDATE_FILE"

if [ "$GLOBAL_INVALIDATE" = "1" ]; then
  log "Global invalidation — running all instrumented tests."
  run_full_suite; exit 0
fi

CHANGED_MODULES=()
[ -n "$detector_out" ] && mapfile -t CHANGED_MODULES <<< "$detector_out"

if [ ${#CHANGED_MODULES[@]} -eq 0 ]; then
  log "No changed modules — skipping instrumented tests."; exit 0
fi
log "Changed: ${CHANGED_MODULES[*]}"

# -- Step 2: Extract / load dependency graph --------------------------------
log "=== Step 2: Load dependency graph ==="
GRAPH_FILE=$("${BASH:-bash}" "$SCRIPT_DIR/extract_module_graph.sh")
log "Graph: $GRAPH_FILE"

# -- Step 3: BFS to find transitively affected modules ---------------------
log "=== Step 3: Compute affected modules (BFS) ==="
mapfile -t AFFECTED_MODULES < <(
  "${BASH:-bash}" "$SCRIPT_DIR/compute_affected_modules.sh" "$GRAPH_FILE" "${CHANGED_MODULES[@]}"
)
log "Affected (${#AFFECTED_MODULES[@]}): ${AFFECTED_MODULES[*]}"

# -- Step 4: Apply hardening rules -----------------------------------------
log "=== Step 4: Apply hardening rules ==="
export REPO_ROOT
hardening_out=$("${BASH:-bash}" "$SCRIPT_DIR/apply_hardening_rules.sh" "${AFFECTED_MODULES[@]}") || {
  log "ERROR: apply_hardening_rules.sh failed (exit $?) — falling back to full test run."
  run_full_suite; exit 0
}

TESTABLE_MODULES=()
[ -n "$hardening_out" ] && mapfile -t TESTABLE_MODULES <<< "$hardening_out"

if [ -f "$SMOKE_FALLBACK_FILE" ] && source "$SMOKE_FALLBACK_FILE" && [ "${SMOKE_FALLBACK:-0}" = "1" ]; then
  log "Smoke fallback — running app-level smoke test only."
  run_gradle "${SMOKE_TEST_MODULE}:assemble$(capitalize "$FLAVOUR")BlackDebug" \
             "${SMOKE_TEST_MODULE}:assemble$(capitalize "$FLAVOUR")BlackDebugAndroidTest"
  run_gradle runFlank; exit 0
fi

[ ${#TESTABLE_MODULES[@]} -eq 0 ] && { log "No testable modules — skipping."; exit 0; }
log "Testable (${#TESTABLE_MODULES[@]}): ${TESTABLE_MODULES[*]}"

# -- Step 5: Build test APKs for affected modules only ---------------------
log "=== Step 5: Build test APKs ==="
FLAVOR_CAP=$(capitalize "$FLAVOUR")

# Returns true if a module's build.gradle.kts defines the given product flavor.
# Modules with flavors need assemble<Flavor>DebugAndroidTest; otherwise the task is ambiguous.
module_has_flavor() {
  local module_dir="$1" flavor="$2"
  grep -q "\"${flavor}\"" "$module_dir/build.gradle.kts" 2>/dev/null
}

# Counts @Test methods under a module's androidTest source sets.
count_module_tests() {
  local module_dir="$1"
  local dirs=()
  while IFS= read -r dir; do dirs+=("$dir"); done < <(
    find "$module_dir/src" -maxdepth 1 -type d -iname "androidtest*" 2>/dev/null
  )
  [ ${#dirs[@]} -eq 0 ] && { echo 0; return; }
  grep -rhoE --include="*.kt" "@Test\b" "${dirs[@]}" 2>/dev/null | wc -l | tr -d ' '
}

# Always build the main debug APK (Fladle requires it as debugApk)
declare -a GRADLE_TASKS=( ":app:assemble${FLAVOR_CAP}BlackDebug" )

for module in "${TESTABLE_MODULES[@]}"; do
  module_dir="$REPO_ROOT/$(echo "$module" | sed 's/^://' | tr ':' '/')"
  if module_has_flavor "$module_dir" "$FLAVOUR"; then
    # Module has multiple product flavors — qualify to avoid "ambiguous task" error
    GRADLE_TASKS+=( "${module}:assemble${FLAVOR_CAP}DebugAndroidTest" )
  else
    GRADLE_TASKS+=( "${module}:assembleDebugAndroidTest" )
  fi
done
log "Tasks: ${GRADLE_TASKS[*]}"
run_gradle "${GRADLE_TASKS[@]}"

# -- Step 6: Run Fladle with only the APKs we just built -------------------
log "=== Step 6: Run Fladle ==="
# numUniformShards doesn't guarantee an even split even when shard count
# doesn't exceed a module's test count — Test Orchestrator's assignment
# isn't a perfect round-robin, and Firebase marks a 0-test shard as failed
# outright. Confirmed in production: a module with exactly 5 tests and
# numUniformShards=5 still produced an empty, failed shard. Splitting a
# module's tests only makes sense once there are enough of them to keep a
# safety margin above the shard count; below that, force a single
# (unsharded) run instead — small modules run fast regardless.
SHARD_MIN_TESTS=30
LARGE_APKS=()
SMALL_APKS=()
LARGE_TOTAL=0
LARGE_MIN_COUNT=0
for module in "${TESTABLE_MODULES[@]}"; do
  module_dir="$REPO_ROOT/$(echo "$module" | sed 's/^://' | tr ':' '/')"
  apk=$(find "$module_dir/build/outputs/apk/androidTest" -name "*-androidTest.apk" 2>/dev/null | sort | head -1)
  [ -z "$apk" ] && continue
  module_count=$(count_module_tests "$module_dir")
  if [ "$module_count" -ge "$SHARD_MIN_TESTS" ]; then
    LARGE_APKS+=("$apk")
    LARGE_TOTAL=$((LARGE_TOTAL + module_count))
    [ "$LARGE_MIN_COUNT" -eq 0 ] || [ "$module_count" -lt "$LARGE_MIN_COUNT" ] && LARGE_MIN_COUNT=$module_count
  else
    SMALL_APKS+=("$apk")
  fi
done

[ $(( ${#LARGE_APKS[@]} + ${#SMALL_APKS[@]} )) -eq 0 ] && die "No test APKs found after build — assembleDebugAndroidTest may have failed silently."

# The sharded and unsharded runs are independent Flank invocations, so run
# them concurrently (each gets its own build dir via selective.flank.runId,
# see root build.gradle.kts, to avoid racing on the generated flank.yml).
LARGE_PID=""
SMALL_PID=""

if [ ${#LARGE_APKS[@]} -gt 0 ]; then
  TARGET_TESTS_PER_SHARD=15
  NUM_SHARDS=$(( (LARGE_TOTAL + TARGET_TESTS_PER_SHARD - 1) / TARGET_TESTS_PER_SHARD ))
  [ "$NUM_SHARDS" -gt 16 ] && NUM_SHARDS=16
  # Keep at least 2 tests/shard of margin against the smallest large module.
  SAFE_MAX=$(( LARGE_MIN_COUNT / 2 ))
  [ "$NUM_SHARDS" -gt "$SAFE_MAX" ] && NUM_SHARDS=$SAFE_MAX
  [ "$NUM_SHARDS" -lt 2 ] && NUM_SHARDS=2
  LARGE_APKS_COMMA=$(IFS=','; echo "${LARGE_APKS[*]}")
  log "Sharded run: ${#LARGE_APKS[@]} APKs, ~$LARGE_TOTAL tests → $NUM_SHARDS shards"
  ( run_gradle runFlank "-Pselective.test.apks=$LARGE_APKS_COMMA" "-Pflank.numUniformShards=$NUM_SHARDS" "-Pselective.flank.runId=large" ) &
  LARGE_PID=$!
fi

if [ ${#SMALL_APKS[@]} -gt 0 ]; then
  SMALL_APKS_COMMA=$(IFS=','; echo "${SMALL_APKS[*]}")
  log "Unsharded run: ${#SMALL_APKS[@]} APKs (each under $SHARD_MIN_TESTS tests)"
  ( run_gradle runFlank "-Pselective.test.apks=$SMALL_APKS_COMMA" "-Pselective.flank.runId=small" ) &
  SMALL_PID=$!
fi

RUN_FAILED=0
[ -n "$LARGE_PID" ] && { wait "$LARGE_PID" || RUN_FAILED=1; }
[ -n "$SMALL_PID" ] && { wait "$SMALL_PID" || RUN_FAILED=1; }
[ "$RUN_FAILED" -eq 1 ] && die "One or more Flank runs failed."
log "=== Done ==="
