#!/usr/bin/env bash
# Shared graph utilities. Source this; do not execute directly.

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  echo "ERROR: graph_utils.sh is a library — source it, don't execute it." >&2; exit 1
fi

# build_reverse_graph <graph_file> <assoc_array_nameref>
# Reads "SOURCE DEP" lines; builds DEP -> "SOURCE1 SOURCE2 ..." map.
build_reverse_graph() {
  local graph_file="$1"
  local -n _rev_graph="$2"          # bash 4.3+ nameref
  local source dep existing
  [ -f "$graph_file" ] || { echo "[graph_utils] ERROR: graph not found: $graph_file" >&2; return 1; }
  while IFS=' ' read -r source dep; do
    [ -z "$source" ] || [ -z "$dep" ] && continue
    existing="${_rev_graph[$dep]+${_rev_graph[$dep]}}"
    if [ -z "$existing" ]; then
      _rev_graph["$dep"]="$source"
    elif ! echo " $existing " | grep -qF " $source "; then
      _rev_graph["$dep"]="$existing $source"
    fi
  done < "$graph_file"
  echo "[graph_utils] Reverse graph: ${#_rev_graph[@]} nodes with dependents" >&2
}

# bfs_traversal <seeds_array_nameref> <rev_graph_nameref> <result_assoc_nameref>
# BFS from seeds through reverse graph. result[module]=1 for each hit.
bfs_traversal() {
  local -n _seeds="$1"; local -n _rev="$2"; local -n _result="$3"
  local -a queue=(); local -i head=0; local current dep dependents
  for m in "${_seeds[@]}"; do
    [ -z "${_result[$m]+set}" ] && { _result["$m"]=1; queue+=("$m"); }
  done
  while [ "$head" -lt "${#queue[@]}" ]; do
    current="${queue[$head]}"; head=$((head + 1))
    dependents="${_rev[$current]+${_rev[$current]}}"; [ -z "$dependents" ] && continue
    for dep in $dependents; do
      [ -z "${_result[$dep]+set}" ] && { _result["$dep"]=1; queue+=("$dep"); }
    done
  done
  echo "[graph_utils] BFS: ${#_result[@]} nodes reached from ${#_seeds[@]} seeds" >&2
}

# print_graph_stats <graph_file> <rev_graph_nameref>
print_graph_stats() {
  local -n _rev="$2"
  echo "[graph_utils] edges=$(wc -l < "$1" | tr -d ' ') nodes=$(awk '{print $1; print $2}' "$1" | sort -u | wc -l | tr -d ' ') nodes_with_dependents=${#_rev[@]}" >&2
}
