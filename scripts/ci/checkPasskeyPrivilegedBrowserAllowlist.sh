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

# SHA-256 of the checked-in AOSP test certs (build/target/product/security/*.x509.pem):
# platform, testkey, media, shared. Their private keys are public, so any app could forge
# them to claim a privileged web origin — they must never be trusted signers. Upstream marks
# some as `release`, so we strip them on every sync. Keep in sync with PasskeyOriginVerifierTest.
PUBLIC_TEST_KEYS = {
    "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8",
    "A4:0D:A8:0A:59:D1:70:CA:A9:50:CF:15:C1:8C:45:4D:47:A3:9B:26:98:9D:8B:64:0E:CD:74:5B:A7:1B:F5:DC",
    "46:59:83:F7:79:1F:2A:BE:B4:3E:A2:CB:DC:7F:21:A8:26:0B:72:BC:08:A5:5C:83:9F:C1:A4:3B:C7:41:A8:1E",
    "28:BB:FE:4A:7B:97:E7:46:81:DC:55:C2:FB:B6:CC:B8:D6:C7:49:63:73:3F:6A:F6:AE:74:D8:C3:A6:E8:79:FD",
}

path = sys.argv[1]
with open(path, "r", encoding="utf-8") as file:
    root = json.load(file)

apps = root.get("apps")
if not isinstance(apps, list) or not apps:
    raise SystemExit("Allowlist must contain a non-empty apps array")

# Strip publicly-known AOSP test-key signers, dropping entries left with no signatures.
deny = {fp.upper() for fp in PUBLIC_TEST_KEYS}
sanitized = []
for app in apps:
    info = app.get("info", {})
    info["signatures"] = [
        sig for sig in info.get("signatures", [])
        if sig.get("cert_fingerprint_sha256", "").upper() not in deny
    ]
    if info["signatures"]:
        sanitized.append(app)
root["apps"] = apps = sanitized

if not apps:
    raise SystemExit("Allowlist is empty after removing public test-key signers")

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

# Reserialize the sanitized allowlist so check/update operate on the stripped content.
with open(path, "w", encoding="utf-8") as file:
    file.write(json.dumps(root, indent=2, ensure_ascii=False))
    file.write("\n")
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
