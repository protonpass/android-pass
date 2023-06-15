#!/bin/bash

set -eu

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts/ci::g')
KEYSTORE_DIR="${REPO_ROOT}/keystore"

mkdir -p "${KEYSTORE_DIR}"
echo "${UPLOAD_KEYSTORE_B64}" | base64 -d > "${KEYSTORE_DIR}/upload-keystore"
