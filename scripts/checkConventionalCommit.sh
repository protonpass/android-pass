#!/bin/bash

set -euo pipefail

CONVENTIONAL_REGEX='^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-z0-9._/-]+\))?(!)?: .+'

is_valid_subject() {
  local subject="$1"
  [[ "$subject" =~ $CONVENTIONAL_REGEX ]] || [[ "$subject" =~ ^Merge[[:space:]] ]] || [[ "$subject" =~ ^Revert[[:space:]] ]]
}

validate_subject() {
  local subject="$1"
  if ! is_valid_subject "$subject"; then
    echo "Invalid commit message subject:"
    echo "  $subject"
    echo
    echo "Expected Conventional Commit format, for example:"
    echo "  feat(auth): add biometric fallback"
    echo "  fix: prevent null pointer in vault sync"
    echo "  chore!: drop deprecated API"
    return 1
  fi
}

validate_file() {
  local commit_msg_file="$1"
  local subject
  subject=$(head -n 1 "$commit_msg_file")
  validate_subject "$subject"
}

validate_range() {
  local range="$1"
  local has_failures=0
  local subject
  while IFS= read -r subject; do
    [[ -z "$subject" ]] && continue
    if ! validate_subject "$subject"; then
      has_failures=1
    fi
  done < <(git log --format=%s "$range")

  if [[ "$has_failures" -ne 0 ]]; then
    return 1
  fi
}

if [[ "$#" -gt 0 && -f "$1" ]]; then
  validate_file "$1"
  exit 0
fi

if [[ -n "${CI_MERGE_REQUEST_DIFF_BASE_SHA:-}" && -n "${CI_COMMIT_SHA:-}" ]]; then
  if [[ "${CI_MERGE_REQUEST_DIFF_BASE_SHA}" != "0000000000000000000000000000000000000000" ]]; then
    validate_range "${CI_MERGE_REQUEST_DIFF_BASE_SHA}..${CI_COMMIT_SHA}"
    exit 0
  fi
fi

if [[ -n "${CI_COMMIT_BEFORE_SHA:-}" && -n "${CI_COMMIT_SHA:-}" ]]; then
  if [[ "${CI_COMMIT_BEFORE_SHA}" != "0000000000000000000000000000000000000000" ]]; then
    validate_range "${CI_COMMIT_BEFORE_SHA}..${CI_COMMIT_SHA}"
    exit 0
  fi
fi

if [[ -n "${CI_COMMIT_TITLE:-}" ]]; then
  validate_subject "${CI_COMMIT_TITLE}"
  exit 0
fi

echo "No commit message input found."
exit 1
