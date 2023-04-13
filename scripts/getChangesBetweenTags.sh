#!/bin/bash

set -euo pipefail

# CHATGPT PROMPT
#You will act as a product manager that will write the release notes for our app. I will send you the changelog for the new version, and I would like you to separate them into Features, Bugfixes and Other. The input will be of the form:
#
#TAG(COMPONENT): MESSAGE
#
#The COMPONENT will be optional, and regardless if it's there or not, you can ignore it. Please output a version with emojis and another one without. Skip prose

if [[ $# -ne 2 ]]; then
  echo "Invalid usage: ${0} NEW_TAG OLD_TAG"
  exit 1
fi

NEW_TAG="${1}"
OLD_TAG="${2}"

git log --pretty=format:"%s" ${NEW_TAG}...${OLD_TAG} | grep -E -v 'test[:(]|ci[:(]|build[:(]|bump[:(]|refactor[(:]|style[(:]|Bump |update dependency'
