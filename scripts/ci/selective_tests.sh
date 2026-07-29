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
# Collect paths of every test APK produced by the selective build.
# Passed to Fladle via -Pselective.test.apks so it overrides Fulladle's
# full-project auto-discovery (which would list modules we never built).
BUILT_APKS=()
for module in "${TESTABLE_MODULES[@]}"; do
  module_dir="$REPO_ROOT/$(echo "$module" | sed 's/^://' | tr ':' '/')"
  while IFS= read -r apk; do
    BUILT_APKS+=("$apk")
  done < <(find "$module_dir/build/outputs/apk/androidTest" -name "*-androidTest.apk" 2>/dev/null | sort)
done

[ ${#BUILT_APKS[@]} -eq 0 ] && die "No test APKs found after build — assembleDebugAndroidTest may have failed silently."
log "Test APKs (${#BUILT_APKS[@]}): ${BUILT_APKS[*]}"
APKS_COMMA=$(IFS=','; echo "${BUILT_APKS[*]}")

# Size shards by actual @Test count, not APK/module count — tests are
# unevenly distributed across modules (e.g. 85 vs 6).
TOTAL_TEST_COUNT=0
MIN_MODULE_TEST_COUNT=0
for module in "${TESTABLE_MODULES[@]}"; do
  module_dir="$REPO_ROOT/$(echo "$module" | sed 's/^://' | tr ':' '/')"
  module_count=$(count_module_tests "$module_dir")
  TOTAL_TEST_COUNT=$((TOTAL_TEST_COUNT + module_count))
  if [ "$module_count" -gt 0 ] && { [ "$MIN_MODULE_TEST_COUNT" -eq 0 ] || [ "$module_count" -lt "$MIN_MODULE_TEST_COUNT" ]; }; then
    MIN_MODULE_TEST_COUNT=$module_count
  fi
done
[ "$TOTAL_TEST_COUNT" -eq 0 ] && TOTAL_TEST_COUNT=1
[ "$MIN_MODULE_TEST_COUNT" -eq 0 ] && MIN_MODULE_TEST_COUNT=1

TARGET_TESTS_PER_SHARD=15
NUM_SHARDS=$(( (TOTAL_TEST_COUNT + TARGET_TESTS_PER_SHARD - 1) / TARGET_TESTS_PER_SHARD ))
[ "$NUM_SHARDS" -gt 16 ] && NUM_SHARDS=16
[ "$NUM_SHARDS" -lt 2 ] && NUM_SHARDS=2
# numUniformShards doesn't clamp to a module's test count - excess shards
# come back empty, and Firebase fails empty shards outright. Cap to the
# smallest changed module's count so that can't happen.
[ "$NUM_SHARDS" -gt "$MIN_MODULE_TEST_COUNT" ] && NUM_SHARDS=$MIN_MODULE_TEST_COUNT
log "Total tests (~$TOTAL_TEST_COUNT, smallest module ~$MIN_MODULE_TEST_COUNT) → $NUM_SHARDS shards"
run_gradle runFlank "-Pselective.test.apks=$APKS_COMMA" "-Pflank.numUniformShards=$NUM_SHARDS"
log "=== Done ==="
