#!/usr/bin/env bash
# BFS over the reverse dependency graph to find all affected modules.
#
# Usage: compute_affected_modules.sh <graph_file> [module1 module2 ...]
#        or pipe module list via stdin
# Out:   affected module paths, one per line, sorted
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/graph_utils.sh"

log() { echo "[compute_affected_modules] $*" >&2; }
die() { echo "[compute_affected_modules] ERROR: $*" >&2; exit 1; }

[ $# -ge 1 ] || die "Usage: $0 <graph_file> [module1 ...]"
GRAPH_FILE="$1"; shift
[ -f "$GRAPH_FILE" ] || die "Graph file not found: $GRAPH_FILE"

declare -a CHANGED_MODULES=()
if [ $# -gt 0 ]; then
  for m in "$@"; do CHANGED_MODULES+=("$m"); done
else
  while IFS= read -r line; do
    for token in $line; do [ -n "$token" ] && CHANGED_MODULES+=("$token"); done
  done
fi
[ ${#CHANGED_MODULES[@]} -eq 0 ] && { log "No input modules."; exit 0; }
log "Changed: ${CHANGED_MODULES[*]}"

declare -A reverse_graph=()
build_reverse_graph "$GRAPH_FILE" reverse_graph
print_graph_stats "$GRAPH_FILE" reverse_graph

declare -A affected=()
bfs_traversal CHANGED_MODULES reverse_graph affected

for module in "${!affected[@]}"; do echo "$module"; done | sort
