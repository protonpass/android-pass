#!/bin/bash

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:tools/upload-protonapps::g')

APK_CERT_HASH="DC:C9:43:9E:C1:A6:C6:A8:D0:20:3F:34:23:EE:42:BC:C8:B9:70:62:8E:53:CB:73:A0:39:3F:39:8D:D5:B8:53"
GIT_COMMIT_AUTHOR="${GITLAB_USER_NAME}"
GIT_COMMIT_EMAIL="${GITLAB_USER_EMAIL}"

if [ -z "${PROTONAPPS_REPO}" ]; then
  echo "PROTONAPPS_REPO is not set."
  exit 1
fi

# Check if the script has received an argument and store it as TAG
if [ $# -eq 0 ]; then
    echo "No tag supplied. Usage: ./upload.sh <tag>"
    exit 1
fi

TAG="${1}"

# Check if git-lfs is available
if ! command -v git-lfs --help &> /dev/null; then
  echo "Installing git-lfs"
  apt update && apt install -y git-lfs
fi


# Find the APK
APK=$(find "${REPO_ROOT}" -name "*prod-release.apk" | head -n 1)

# Clone the download repo to a temporary directory
tmpDir=$(mktemp -d)
git clone --depth 1 "${PROTONAPPS_REPO}" $tmpDir

# Enter the temporary directory
pushd $tmpDir || exit 1

# Configure git
git config --local user.name "${GIT_COMMIT_AUTHOR}"
git config --local user.email "${GIT_COMMIT_EMAIL}"

# Prepare the PassAndroid directory
mkdir -p "${tmpDir}/PassAndroid"

# Copy the APK
cp "${APK}" "${tmpDir}/PassAndroid/ProtonPass-Android.apk"
git lfs track --filename "PassAndroid/ProtonPass-Android.apk"

# Prepare the version file notes
now=$(date +'%Y-%m-%dT%H:%M:%S+0000')
echo "{\"Version\": \"${TAG}\", \"ReleaseDate\": \"${now}\", \"SHA256\": \"${APK_CERT_HASH}\"}" > "${tmpDir}/PassAndroid/protonpass_android_version.json"

git add .
git commit -m "Pass Android ${TAG}"
git push -u origin deploy

slackMessage=$(git log -n 1 --pretty=format:"Dear deploy shifter can you please deploy https://protonmail.com/download  %s \`%h\`")

popd
rm -rf $tmpDir

curl -d "text=${slackMessage}" -d "channel=${SLACK_DEPLOY_CHANNEL}" -H "Authorization: Bearer ${SLACK_TOKEN}" -X POST "https://slack.com/api/chat.postMessage"

