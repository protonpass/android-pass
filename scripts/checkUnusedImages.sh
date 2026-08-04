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
SEEN_KEYS_FILE=$(mktemp)
trap 'rm -f "$DECLARED_FILE" "$USED_FILE" "$NAME_TO_FILE" "$SEEN_KEYS_FILE"' EXIT

# Find all drawable resource files across all density variants, excluding
# this script's own test fixtures (which deliberately contain unused
# drawables and would otherwise be reported as permanent, unfixable
# findings) and generated build intermediates (which aren't real source
# and can contain stale copies)
drawable_files=$(find . -type f \( -name "*.xml" -o -name "*.webp" -o -name "*.png" \) -path "*/res/drawable*/*" -not -path "*/scripts/tests/fixtures/*" -not -path "*/build/*")

# Collect declared drawable names (filename without extension), remembering
# which file declared each one. The same name in multiple density variants
# (drawable-hdpi, drawable-xhdpi, ...) within the SAME module is a legitimate
# duplicate - first file found wins. But the same name in DIFFERENT modules
# should be reported separately. We track by (module_dir, drawable_name) key.
for file in $drawable_files; do
    base_name=$(basename "$file")
    drawable_name="${base_name%.*}"
    drawable_name="${drawable_name%.9}" # strip the .9 from nine-patch files (foo.9.png)
    module_dir="${file%/res/drawable*}"
    key="${module_dir}|${drawable_name}"
    if ! grep -qxF "$key" "$SEEN_KEYS_FILE" 2>/dev/null; then
        echo "$key" >> "$SEEN_KEYS_FILE"
        echo "$drawable_name" >> "$DECLARED_FILE"
        printf '%s\t%s\n' "$drawable_name" "$file" >> "$NAME_TO_FILE"
    fi
done

# Collect every drawable name referenced anywhere in the repo, across the
# two ways a drawable resource can be referenced. Exactly 2 full-repo rg
# passes total, regardless of how many drawables are declared.
rg -oIN -g '!scripts/tests/fixtures/**' -g '!**/build/**' -r '$1' 'R\.drawable\.([A-Za-z0-9_]+)' . >> "$USED_FILE" 2>/dev/null
rg -oIN -g '!scripts/tests/fixtures/**' -g '!**/build/**' -r '$1' '@drawable/([A-Za-z0-9_]+)' . >> "$USED_FILE" 2>/dev/null

# unused = declared - used
unused_names=$(comm -23 <(sort -u "$DECLARED_FILE") <(sort -u "$USED_FILE"))

unused_images=()
if [ -n "$unused_names" ]; then
    while IFS=$'\t' read -r drawable_name file; do
        if printf '%s\n' "$unused_names" | grep -qxF "$drawable_name"; then
            echo "$drawable_name: $file"
            unused_images+=("$drawable_name: $file")
        fi
    done < "$NAME_TO_FILE"
fi

if [ ${#unused_images[@]} -gt 0 ]; then
    echo "Found unused images"
    exit 1
else
    echo "No unused images found."
    exit 0
fi
