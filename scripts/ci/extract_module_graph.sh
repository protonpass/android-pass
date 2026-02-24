#!/usr/bin/env bash
# Builds the module dependency graph from build.gradle.kts files (cached).
# Bash + awk only — no Python/Node/jq.
#
# Cache: .gradle/ci-graph/  (covered by GitLab CI Gradle cache paths)
# Out:   path to graph file (stdout)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CACHE_DIR="$REPO_ROOT/.gradle/caches/ci-graph"
CACHE_HASH_FILE="$CACHE_DIR/graph.cache_hash"
GRAPH_FILE="$CACHE_DIR/module_graph.txt"

log() { echo "[extract_module_graph] $*" >&2; }
die() { echo "[extract_module_graph] ERROR: $*" >&2; exit 1; }
cd "$REPO_ROOT"

# Cache key: hash of all build.gradle.kts contents + settings + this script.
declare -a HASH_INPUT_FILES=()
while IFS= read -r file; do HASH_INPUT_FILES+=("$file"); done < <(
  find . -name "build.gradle.kts" -not -path "./.gradle/*" -not -path "*/build/*" | sort
)
[ -f "settings.gradle.kts" ] && HASH_INPUT_FILES+=("settings.gradle.kts")
HASH_INPUT_FILES+=("$SCRIPT_DIR/extract_module_graph.sh")

current_hash=$(printf '%s\0' "${HASH_INPUT_FILES[@]}" | xargs -0 sha256sum 2>/dev/null | sha256sum | awk '{print $1}')
log "Build hash: $current_hash"

if [ -f "$CACHE_HASH_FILE" ] && [ -f "$GRAPH_FILE" ] && [ "$(cat "$CACHE_HASH_FILE")" = "$current_hash" ]; then
  log "Cache hit — reusing $GRAPH_FILE"; echo "$GRAPH_FILE"; exit 0
fi
log "Cache miss — rebuilding graph"
mkdir -p "$CACHE_DIR"; : > "$GRAPH_FILE"

# AWK program: camelCase -> kebab-case converter + accessor -> module path
read -r -d '' AWK_PROGRAM << 'AWKEOF' || true
function camel_to_kebab(s,    i, c, result) {
    result = ""
    for (i = 1; i <= length(s); i++) {
        c = substr(s, i, 1)
        if (c ~ /[A-Z]/) {
            if (result != "") result = result "-"
            result = result tolower(c)
        } else {
            result = result c
        }
    }
    return result
}
function accessor_to_path(accessor,    parts, n, i, path) {
    sub(/^projects\./, "", accessor)          # remove "projects." prefix
    n = split(accessor, parts, ".")
    path = ""
    for (i = 1; i <= n; i++) { path = path ":" camel_to_kebab(parts[i]) }
    return path
}
{
    dep = accessor_to_path($0)
    if (dep != source) print source " " dep   # skip self-references
}
AWKEOF

module_count=0
while IFS= read -r gradle_file; do
  rel_dir="$(dirname "$gradle_file")"; rel_dir="${rel_dir#./}"
  [ -z "$rel_dir" ] || [ "$rel_dir" = "." ] && continue
  source_module=":$(echo "$rel_dir" | tr '/' ':')"
  module_count=$((module_count + 1))

  # Extract all projects.xxx.yyy accessors, convert to paths, write edges
  grep -oE 'projects\.[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*' "$gradle_file" 2>/dev/null \
    | sort -u \
    | awk -v source="$source_module" "$AWK_PROGRAM" \
    >> "$GRAPH_FILE" || true

done < <(find . -name "build.gradle.kts" -not -path "./.gradle/*" -not -path "*/build/*" | sed 's|^\./||' | sort)

sort -u "$GRAPH_FILE" -o "$GRAPH_FILE"   # deduplicate
log "Graph built: $module_count modules, $(wc -l < "$GRAPH_FILE") unique edges"
echo "$current_hash" > "$CACHE_HASH_FILE"
echo "$GRAPH_FILE"
