#!/bin/bash

if [[ $# -eq 0 ]]; then
    echo "Invalid usage. ${0} VERSION"
    exit 1
fi

VERSION=$1

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts::g')

VERSION_CODE=$("${REPO_ROOT}/scripts/versionCode.sh" "${VERSION}")

echo -e "versionName=${VERSION}\nversionCode=${VERSION_CODE}" > metadata/fdroid_version.txt

touch "metadata/en-US/changelogs/${VERSION_CODE}.txt"
