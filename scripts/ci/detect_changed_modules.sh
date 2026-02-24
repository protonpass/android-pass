#!/usr/bin/env bash
# Detect which Gradle modules changed between BASE_SHA and HEAD_SHA.
#
# Env:  BASE_SHA (default: origin/main), HEAD_SHA (default: HEAD)
# Out:  one :module:path per line (stdout)
#       writes GLOBAL_INVALIDATE=1 to $TMPDIR/ci_global_invalidate if infra changed
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASE_SHA="${BASE_SHA:-origin/main}"
HEAD_SHA="${HEAD_SHA:-HEAD}"
GLOBAL_INVALIDATE_FILE="${TMPDIR:-/tmp}/ci_global_invalidate${CI_JOB_ID:+_${CI_JOB_ID}}"

# Changes to any of these trigger full re-run (prefix-matched)
GLOBAL_TRIGGERS=( "build.gradle.kts" "settings.gradle.kts" "gradle/libs.versions.toml"
                  "build-logic/" "gradle/wrapper/" )

log() { echo "[detect_changed_modules] $*" >&2; }
die() { echo "[detect_changed_modules] ERROR: $*" >&2; exit 1; }

cd "$REPO_ROOT"
git rev-parse --git-dir >/dev/null 2>&1 || die "Not inside a git repository"
RESOLVED_BASE=$(git rev-parse "$BASE_SHA" 2>/dev/null) || die "Cannot resolve BASE_SHA='$BASE_SHA'"
RESOLVED_HEAD=$(git rev-parse "$HEAD_SHA" 2>/dev/null) || die "Cannot resolve HEAD_SHA='$HEAD_SHA'"
log "Comparing $RESOLVED_BASE..$RESOLVED_HEAD"

# Build directory -> :module:path map from all build.gradle.kts locations
declare -A DIR_TO_MODULE=()
while IFS= read -r gradle_file; do
  rel_dir="$(dirname "$gradle_file")"; rel_dir="${rel_dir#./}"
  [ -z "$rel_dir" ] || [ "$rel_dir" = "." ] && continue
  DIR_TO_MODULE["$rel_dir"]=":$(echo "$rel_dir" | tr '/' ':')"
done < <(find . -name "build.gradle.kts" -not -path "./.gradle/*" -not -path "*/build/*" | sed 's|^\./||' | sort)
log "Discovered ${#DIR_TO_MODULE[@]} modules"

# Check for global invalidation
GLOBAL_INVALIDATE=0; printf '' > "$GLOBAL_INVALIDATE_FILE"
changed_files_all=$(git diff --name-only "$RESOLVED_BASE" "$RESOLVED_HEAD")
for trigger in "${GLOBAL_TRIGGERS[@]}"; do
  if echo "$changed_files_all" | grep -qE "^(${trigger}|${trigger}.*)$"; then
    log "Global invalidation triggered by: $trigger"; GLOBAL_INVALIDATE=1; break
  fi
done
echo "GLOBAL_INVALIDATE=$GLOBAL_INVALIDATE" > "$GLOBAL_INVALIDATE_FILE"

if [ "$GLOBAL_INVALIDATE" = "1" ]; then
  log "Global invalidation — caller should run all modules."; exit 0
fi

# Map each changed file to its owning module (walk up to nearest build.gradle.kts)
declare -A affected_modules=()
while IFS= read -r changed_file; do
  [ -z "$changed_file" ] && continue
  dir="$(dirname "$changed_file")"
  while true; do
    dir="${dir#./}"
    [ -z "$dir" ] || [ "$dir" = "." ] && { log "  (no module for: $changed_file)"; break; }
    if [ -n "${DIR_TO_MODULE[$dir]+set}" ]; then
      log "  $changed_file -> ${DIR_TO_MODULE[$dir]}"
      affected_modules["${DIR_TO_MODULE[$dir]}"]=1; break
    fi
    dir="$(dirname "$dir")"
  done
done <<< "$changed_files_all"

[ ${#affected_modules[@]} -eq 0 ] && { log "No modules changed."; exit 0; }
for module in "${!affected_modules[@]}"; do echo "$module"; done | sort
