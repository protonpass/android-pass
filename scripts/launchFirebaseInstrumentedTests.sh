#!/bin/bash

set -eu

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts::g')

APK_PATH=$(find "${REPO_ROOT}/app/build/outputs/apk/devBlack/debug" -type f -name '*.apk')
DATA_APK_PATH=$(find "${REPO_ROOT}/pass/data/impl/build/outputs/apk/androidTest/debug" -type f -name '*.apk')
CRYPTO_APK_PATH=$(find "${REPO_ROOT}/pass/crypto/impl/build/outputs/apk/androidTest/debug" -type f -name '*.apk')

gcloud firebase test android run \
  --app "${APK_PATH}" \
  --test "${DATA_APK_PATH}" \
  --type=instrumentation \
  --device model=Pixel2,version=28 \
  --use-orchestrator \
  --num-flaky-test-attempts=1 \
  --timeout 10m \
  --no-record-video

gcloud firebase test android run \
  --app "${APK_PATH}" \
  --test "${CRYPTO_APK_PATH}" \
  --type=instrumentation \
  --device model=Pixel2,version=28 \
  --use-orchestrator \
  --num-flaky-test-attempts=1 \
  --timeout 10m \
  --no-record-video
