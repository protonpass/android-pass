#!/bin/bash

set -eu

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts/ci::g')

echo "${SENTRY_PROPERTIES_B64}" | base64 -d > "${REPO_ROOT}/sentry.properties"
