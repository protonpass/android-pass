#!/usr/bin/env bash
set -euo pipefail

SOURCE_URL="https://publicsuffix.org/list/public_suffix_list.dat"
TARGET_FILE="pass/data/impl/src/main/res/raw/public_suffix_list.txt"

tmp_file="$(mktemp)"
trap 'rm -f "${tmp_file}"' EXIT

curl -fsSL "${SOURCE_URL}" -o "${tmp_file}"

# Keep "*." (wildcard) and "!" (exception) rule markers intact — GetPublicSuffixListImpl
# interprets them. Only comment and blank lines are dropped.
content="$(grep -v '^//' "${tmp_file}" | grep -v '^[[:space:]]*$')"

if [[ -z "${content}" ]]; then
  echo "Downloaded public suffix list is empty, refusing to overwrite ${TARGET_FILE}"
  exit 1
fi

new_lines="$(printf '%s\n' "${content}" | wc -l)"
old_lines="$(wc -l < "${TARGET_FILE}")"
if (( new_lines < old_lines * 90 / 100 )); then
  echo "Downloaded list (${new_lines} lines) is much smaller than current (${old_lines}); refusing to overwrite ${TARGET_FILE}"
  exit 1
fi

printf '%s' "${content}" > "${TARGET_FILE}"
echo "Wrote $(wc -l < "${TARGET_FILE}") lines to ${TARGET_FILE}"
