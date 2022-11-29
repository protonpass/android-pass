#!/bin/bash

set -eu

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts::g')

if [[ -z "$FLAVOUR" ]]; then
  echo "FLAVOUR not set"
  exit 1
fi

case "${FLAVOUR}" in
    dev)
      FIREBASE_APP_ID="${DEV_FIREBASE_APP_ID}"
      FIREBASE_TEST_GROUP="${DEV_FIREBASE_TEST_GROUP}" ;;
    alpha)
      FIREBASE_APP_ID="${ALPHA_FIREBASE_APP_ID}"
      FIREBASE_TEST_GROUP="${ALPHA_FIREBASE_TEST_GROUP}" ;;
    *)
      echo "Unsupported flavour"
      exit 1 ;;
esac

APK_DIR="${REPO_ROOT}/app/build/outputs/apk/${FLAVOUR}/release/"
APK_PATH=$(find "${APK_DIR}" -type f -name '*.apk')
RELEASE_NOTES_PATH="${REPO_ROOT}/release-notes.txt"

if [[ -z "$FIREBASE_CI_TOKEN" ]]; then
  echo "FIREBASE_CI_TOKEN not set"
  exit 1
fi

echo "Generating release notes"

/bin/bash $REPO_ROOT/scripts/ci/generateReleaseNotes.sh > "${RELEASE_NOTES_PATH}"

echo "Uploading APK: ${APK_PATH}"

firebase appdistribution:distribute "${APK_PATH}" \
    --app "${FIREBASE_APP_ID}" \
    --release-notes-file "${RELEASE_NOTES_PATH}" \
    --groups "${FIREBASE_TEST_GROUP}" \
    --token "${FIREBASE_CI_TOKEN}"
