#!/usr/bin/env bash
# Applies hardening rules to the affected module list:
#   1. Adds ALWAYS_RUN_MODULES (space-separated env var)
#   2. Removes meta-modules (no androidTest capability)
#   3. Warns or fails if non-meta module lacks src/androidTest/kotlin
#   4. Triggers smoke fallback if final list is empty
#
# Env:  ALWAYS_RUN_MODULES  (space-sep, default: ":pass:data:impl")
#       STRICT_MISSING_TESTS (1 = exit 1, default: 0 = warn)
#       REPO_ROOT            (auto-detected)
# In:   module paths (positional args or stdin)
# Out:  filtered module list (stdout); side-effect: $TMPDIR/ci_smoke_fallback
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${REPO_ROOT:-$(cd "$SCRIPT_DIR/../.." && pwd)}"
SMOKE_FALLBACK_FILE="${TMPDIR:-/tmp}/ci_smoke_fallback${CI_JOB_ID:+_${CI_JOB_ID}}"
ALWAYS_RUN_MODULES="${ALWAYS_RUN_MODULES:-:pass:data:impl}"
STRICT_MISSING_TESTS="${STRICT_MISSING_TESTS:-0}"

log()  { echo "[apply_hardening] $*" >&2; }
warn() { echo "[apply_hardening] WARN: $*" >&2; }
die()  { echo "[apply_hardening] ERROR: $*" >&2; exit 1; }

# Returns 0 if module IS a meta-module (should be excluded)
is_meta_module() {
  local module="$1"
  local leaf="${module##*:}"
  local module_dir="$REPO_ROOT/$(echo "$module" | sed 's/^://' | tr ':' '/')"

  # Explicit meta-modules
  case "$module" in
    :pass:protos|:pass:common-test|:appmacrobenchmark) log "  META: $module"; return 0 ;;
  esac

  # Pattern-based
  echo "$module" | grep -qE "(benchmark|microbenchmark)" && { log "  META (benchmark): $module"; return 0; }
  echo "$module" | grep -qE "(demo|e2e-app|test-app)"   && { log "  META (demo/app): $module"; return 0; }
  [ "$leaf" = "fakes" ] && { log "  META (fakes): $module"; return 0; }

  # File-based: pure JVM (no Android) or explicitly disabled
  local build_file="$module_dir/build.gradle.kts"
  if [ -f "$build_file" ]; then
    grep -q 'id("org.jetbrains.kotlin.jvm")' "$build_file" && { log "  META (jvm-only): $module"; return 0; }
    grep -q 'enableAndroidTest = false'       "$build_file" && { log "  META (enableAndroidTest=false): $module"; return 0; }
  fi
  return 1
}

# Returns 0 if module has actual androidTest .kt source
has_android_tests() {
  local module_dir="$REPO_ROOT/$(echo "$1" | sed 's/^://' | tr ':' '/')"
  local dir="$module_dir/src/androidTest/kotlin"
  [ -d "$dir" ] && find "$dir" -name "*.kt" -maxdepth 8 | grep -q .
}

# Collect input modules
declare -A module_set=()
if [ $# -gt 0 ]; then
  for m in "$@"; do [ -n "$m" ] && module_set["$m"]=1; done
else
  while IFS= read -r line; do
    for token in $line; do [ -n "$token" ] && module_set["$token"]=1; done
  done
fi
# Add always-run modules
for m in $ALWAYS_RUN_MODULES; do [ -n "$m" ] && module_set["$m"]=1; done
log "Input (after always-run merge): ${#module_set[@]} modules"

# Apply filters
declare -A testable=()
for module in "${!module_set[@]}"; do
  is_meta_module "$module" && continue
  if has_android_tests "$module"; then
    testable["$module"]=1
  else
    if [ "$STRICT_MISSING_TESTS" = "1" ]; then
      die "$module is non-meta but has no src/androidTest/kotlin. Add tests or add to is_meta_module()."
    else
      warn "$module has no src/androidTest/kotlin — skipping. Add to is_meta_module() if intentional."
    fi
  fi
done
log "Testable modules after hardening: ${#testable[@]}"

if [ ${#testable[@]} -eq 0 ]; then
  log "Empty testable set — smoke fallback activated."
  echo "SMOKE_FALLBACK=1" > "$SMOKE_FALLBACK_FILE"; exit 0
fi
rm -f "$SMOKE_FALLBACK_FILE"
for module in "${!testable[@]}"; do echo "$module"; done | sort
