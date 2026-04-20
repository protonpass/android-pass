#!/usr/bin/env bash
set -euo pipefail

SOURCE_URL="https://www.gstatic.com/gpm-passkeys-privileged-apps/apps.json"
TARGET_FILE="pass/features/credentials/src/main/res/raw/passkey_privileged_browsers_allowlist.json"
MODE="${PASSKEY_PRIVILEGED_ALLOWLIST_MODE:-check}"

tmp_file="$(mktemp)"
trap 'rm -f "${tmp_file}"' EXIT

curl -fsSL "${SOURCE_URL}" -o "${tmp_file}"

python3 - "${tmp_file}" <<'PY'
import json
import sys

path = sys.argv[1]
with open(path, "r", encoding="utf-8") as file:
    root = json.load(file)

apps = root.get("apps")
if not isinstance(apps, list) or not apps:
    raise SystemExit("Allowlist must contain a non-empty apps array")

first = apps[0]
if first.get("type") != "android":
    raise SystemExit("Allowlist entries must use type=android")

info = first.get("info", {})
if not info.get("package_name"):
    raise SystemExit("Allowlist entries must contain info.package_name")

signatures = info.get("signatures")
if not isinstance(signatures, list) or not signatures:
    raise SystemExit("Allowlist entries must contain signatures")

if not signatures[0].get("cert_fingerprint_sha256"):
    raise SystemExit("Allowlist signatures must contain cert_fingerprint_sha256")
PY

if cmp -s "${tmp_file}" "${TARGET_FILE}"; then
    echo "Passkey privileged browser allowlist is up to date."
    exit 0
fi

if [[ "${MODE}" == "update" ]]; then
    cp "${tmp_file}" "${TARGET_FILE}"
    echo "Updated ${TARGET_FILE}; review the diff before committing."
    git diff -- "${TARGET_FILE}"
    exit 0
fi

echo "Passkey privileged browser allowlist is out of date."
echo "Run PASSKEY_PRIVILEGED_ALLOWLIST_MODE=update scripts/ci/checkPasskeyPrivilegedBrowserAllowlist.sh"
echo "and review the resulting diff."
diff -u "${TARGET_FILE}" "${tmp_file}" || true
exit 1
