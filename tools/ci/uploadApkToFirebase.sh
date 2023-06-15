#!/bin/bash

set -u

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts::g')

if [[ -z "$1" ]]; then
  echo "FLAVOUR not set"
  exit 1
fi

case "$1" in
    devBlack)
      FIREBASE_APP_ID="${DEV_FIREBASE_APP_ID}"
      FIREBASE_TEST_GROUP="${DEV_FIREBASE_TEST_GROUP}" ;;
    alphaProd)
      FIREBASE_APP_ID="${ALPHA_FIREBASE_APP_ID}"
      FIREBASE_TEST_GROUP="${ALPHA_FIREBASE_TEST_GROUP}" ;;
    playBlack)
      FIREBASE_APP_ID="${PLAY_FIREBASE_APP_ID}"
      FIREBASE_TEST_GROUP="${PLAY_FIREBASE_TEST_GROUP}" ;;
    *)
      echo "Unsupported flavour"
      exit 1 ;;
esac

APK_DIR="${REPO_ROOT}/app/build/outputs/apk/$1/release/"
APK_PATH=$(find "${APK_DIR}" -type f -name '*.apk')
RELEASE_NOTES_PATH="${REPO_ROOT}/release-notes.txt"

if [[ -z "$FIREBASE_CI_TOKEN" ]]; then
  echo "FIREBASE_CI_TOKEN not set"
  exit 1
fi

echo "Generating release notes"

RELEASE_NOTES=$(git log --pretty=format:'%s' --since="1 day ago" | grep -v "Merge branch")

if [[ $? -ne 0 ]]; then
  echo "There are no changes. Not uploading the APK"
  exit 0
fi

echo "${RELEASE_NOTES}" > "${RELEASE_NOTES_PATH}"

echo "Uploading APK: ${APK_PATH}"

firebase appdistribution:distribute "${APK_PATH}" \
    --app "${FIREBASE_APP_ID}" \
    --release-notes-file "${RELEASE_NOTES_PATH}" \
    --groups "${FIREBASE_TEST_GROUP}" \
    --token "${FIREBASE_CI_TOKEN}"
