#!/usr/bin/env bash

set -eo pipefail

IGNORE_FILE="$1"

# Read the ignore file and prefix each line with --path
args=()
while IFS= read -r line; do
    # Ignore empty lines
    if [ -z "$line" ]; then
        continue
    fi
    args+=(--path "$line")
done < "${IGNORE_FILE:-/dev/stdin}"

if [ -z "${args[*]}" ]; then
    echo "No paths to filter out"
    exit 1;
fi;

# https://github.com/newren/git-filter-repo/blob/main/Documentation/git-filter-repo.txt
git filter-repo "${args[@]}" --invert-paths --force

