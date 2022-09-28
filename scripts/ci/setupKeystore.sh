#!/bin/bash

set -eu

KEYSTORE_DIR="${REPO_ROOT}/keystore"

mkdir -p "${KEYSTORE_DIR}"
echo "${RELEASE_KEYSTORE_B64}" | base64 -d > "${KEYSTORE_DIR}/ProtonMail.keystore"
