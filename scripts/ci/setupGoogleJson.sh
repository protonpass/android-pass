#!/bin/bash

set -eu

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts/ci::g')
DEST_FILE_PATH="${REPO_ROOT}/app/google-services.json"

if [[ -z "${GOOGLE_SERVICE_JSON_B64}" ]]; then
  echo "GOOGLE_SERVICE_JSON_B64 not set"
  exit 1
fi

echo "${GOOGLE_SERVICE_JSON_B64}" | base64 -d > "${DEST_FILE_PATH}"
