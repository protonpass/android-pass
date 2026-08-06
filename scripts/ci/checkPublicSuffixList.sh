#!/usr/bin/env bash
set -euo pipefail

SOURCE_URL="https://publicsuffix.org/list/public_suffix_list.dat"
TARGET_FILE="pass/data/impl/src/main/res/raw/public_suffix_list.txt"

tmp_file="$(mktemp)"
trap 'rm -f "${tmp_file}"' EXIT

curl -fsSL "${SOURCE_URL}" -o "${tmp_file}"

content="$(grep -v '^//' "${tmp_file}" | grep -v '^!' | grep -v '^[[:space:]]*$' | sed 's/\*\.//g')"

if [[ -z "${content}" ]]; then
  echo "Downloaded public suffix list is empty, refusing to overwrite ${TARGET_FILE}"
  exit 1
fi

printf '%s' "${content}" > "${TARGET_FILE}"
echo "Wrote $(wc -l < "${TARGET_FILE}") lines to ${TARGET_FILE}"
