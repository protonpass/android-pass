#!/bin/bash

set -eu

slackMessage="Publishing Android version: ${CI_COMMIT_TAG}"

curl -d "text=${slackMessage}" -d "channel=${SLACK_ID_TEAM_ANDROID}" -H "Authorization: Bearer ${SLACK_TOKEN}" -X POST "https://slack.com/api/chat.postMessage"

