#!/usr/bin/env bash
set -euo pipefail

SOURCE_URL="https://www.gstatic.com/gpm-passkeys-privileged-apps/apps.json"
TARGET_FILE="pass/browser-allowlist/impl/src/main/res/raw/passkey_privileged_browsers_allowlist.json"
# F-Droid flavour override: same resource name in the app's fdroid source set shadows the
# library snapshot via resource merging, so F-Droid re-signing keys are trusted ONLY in
# fdroid builds; play/quest/nogms keep warning for re-signed browsers.
FDROID_TARGET_FILE="app/src/fdroid/res/raw/passkey_privileged_browsers_allowlist.json"
BASE_EXTRAS="scripts/ci/browser_cert_extras.json"
FDROID_EXTRAS="scripts/ci/browser_cert_extras_fdroid.json"
MERGER="scripts/ci/merge_privileged_browser_allowlist.py"
MODE="${PASSKEY_PRIVILEGED_ALLOWLIST_MODE:-check}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT
upstream_file="${tmp_dir}/upstream.json"
curl -fsSL "${SOURCE_URL}" -o "${upstream_file}"

merged_default="${tmp_dir}/default.json"
merged_fdroid="${tmp_dir}/fdroid.json"

cp "${upstream_file}" "${merged_default}"
python3 "${MERGER}" "${merged_default}" "${BASE_EXTRAS}"

cp "${upstream_file}" "${merged_fdroid}"
python3 "${MERGER}" "${merged_fdroid}" "${BASE_EXTRAS}" "${FDROID_EXTRAS}"

drift=0
check_or_update() {
    local merged="$1" target="$2" label="$3"
    if cmp -s "${merged}" "${target}" 2>/dev/null; then
        echo "${label} allowlist is up to date."
        return
    fi
    drift=1
    if [[ "${MODE}" == "update" ]]; then
        mkdir -p "$(dirname "${target}")"
        cp "${merged}" "${target}"
        echo "Updated ${target}; review the diff before committing."
        git diff -- "${target}"
    fi
}

check_or_update "${merged_default}" "${TARGET_FILE}" "Passkey privileged browser"
check_or_update "${merged_fdroid}" "${FDROID_TARGET_FILE}" "F-Droid flavour browser"

if [[ "${drift}" -eq 0 ]]; then
    exit 0
fi

if [[ "${MODE}" == "update" ]]; then
    exit 0
fi

echo "Privileged browser allowlists are out of date."
echo "Run PASSKEY_PRIVILEGED_ALLOWLIST_MODE=update scripts/ci/checkPasskeyPrivilegedBrowserAllowlist.sh"
echo "and review the resulting diff."
diff -u "${TARGET_FILE}" "${merged_default}" || true
diff -u "${FDROID_TARGET_FILE}" "${merged_fdroid}" 2>/dev/null || true
exit 1
