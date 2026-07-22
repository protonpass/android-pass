#!/bin/bash

set -u

ROOT="${1:-.}"
cd "$ROOT" || exit 1

# Make sure dependencies are in place
if ! command -v rg --help &> /dev/null; then
  echo "Could not find rg"
  exit 1
fi

DECLARED_FILE=$(mktemp)
USED_FILE=$(mktemp)
NAME_TO_FILE=$(mktemp)
trap 'rm -f "$DECLARED_FILE" "$USED_FILE" "$NAME_TO_FILE"' EXIT

# Find all strings.xml files in the project, excluding this script's own
# test fixtures (which deliberately contain unused strings and would
# otherwise be reported as permanent, unfixable findings) and generated
# build intermediates (which aren't real source and can contain stale copies)
string_files=$(find . -name "strings.xml" -path "*res/values/*" -not -path "*/scripts/tests/fixtures/*" -not -path "*/build/*")

# Collect declared string names, remembering which file declared each one
for file in $string_files; do
    string_names=$(rg -oN 'name="([\w_]+)"' -r '$1' "$file")
    for string_name in $string_names; do
        echo "$string_name" >> "$DECLARED_FILE"
        printf '%s\t%s\n' "$string_name" "$file" >> "$NAME_TO_FILE"
    done
done

# Collect every string name referenced anywhere in the repo, across the
# three ways a string resource can be referenced. Exactly 3 full-repo rg
# passes total, regardless of how many strings are declared.
rg -oIN -g '!scripts/tests/fixtures/**' -g '!**/build/**' -r '$1' 'R\.string\.([A-Za-z0-9_]+)' . >> "$USED_FILE" 2>/dev/null
rg -oIN -g '!scripts/tests/fixtures/**' -g '!**/build/**' -r '$1' 'R\.plurals\.([A-Za-z0-9_]+)' . >> "$USED_FILE" 2>/dev/null
rg -oIN -g '!scripts/tests/fixtures/**' -g '!**/build/**' -r '$1' '@string/([A-Za-z0-9_]+)' . >> "$USED_FILE" 2>/dev/null

# unused = declared - used
unused_names=$(comm -23 <(sort -u "$DECLARED_FILE") <(sort -u "$USED_FILE"))

unused_strings=()
if [ -n "$unused_names" ]; then
    while IFS=$'\t' read -r string_name file; do
        if printf '%s\n' "$unused_names" | grep -qxF "$string_name"; then
            echo "$string_name: $file"
            unused_strings+=("$string_name: $file")
        fi
    done < "$NAME_TO_FILE"
fi

if [ ${#unused_strings[@]} -gt 0 ]; then
    echo "Found unused strings"
    exit 1
else
    echo "No unused strings found."
    exit 0
fi
