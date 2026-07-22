#!/bin/bash
set -u

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
TARGET_SCRIPT="${SCRIPT_DIR}/../checkUnusedStrings.sh"
FIXTURES_DIR="${SCRIPT_DIR}/fixtures/unused_strings"

pass_count=0
fail_count=0

check() {
  local name="$1"
  local expected_exit="$2"
  local expect_contains="${3:-}"

  local output
  output=$("$TARGET_SCRIPT" "${FIXTURES_DIR}/${name}" 2>&1)
  local actual_exit=$?

  if [[ "$actual_exit" -ne "$expected_exit" ]]; then
    echo "FAIL: $name - expected exit $expected_exit, got $actual_exit"
    echo "$output"
    fail_count=$((fail_count + 1))
    return
  fi

  if [[ -n "$expect_contains" ]] && [[ "$output" != *"$expect_contains"* ]]; then
    echo "FAIL: $name - expected output to contain '$expect_contains'"
    echo "$output"
    fail_count=$((fail_count + 1))
    return
  fi

  echo "PASS: $name"
  pass_count=$((pass_count + 1))
}

check_count() {
  local name="$1"
  local expected_exit="$2"
  local pattern="$3"
  local expected_count="$4"

  local output
  output=$("$TARGET_SCRIPT" "${FIXTURES_DIR}/${name}" 2>&1)
  local actual_exit=$?
  local actual_count
  actual_count=$(grep -c "$pattern" <<< "$output")

  if [[ "$actual_exit" -ne "$expected_exit" ]]; then
    echo "FAIL: $name - expected exit $expected_exit, got $actual_exit"
    echo "$output"
    fail_count=$((fail_count + 1))
    return
  fi

  if [[ "$actual_count" -ne "$expected_count" ]]; then
    echo "FAIL: $name - expected '$pattern' to appear $expected_count times, got $actual_count"
    echo "$output"
    fail_count=$((fail_count + 1))
    return
  fi

  echo "PASS: $name"
  pass_count=$((pass_count + 1))
}

check "has_unused" 1 "unused_string: ./res/values/strings.xml"
check "has_unused" 1 "Found unused strings"
check "all_used" 0
check "no_strings" 0
check_count "dup_unused_across_files" 1 "^dup_unused:" 2

echo ""
echo "${pass_count} passed, ${fail_count} failed"

if [[ "$fail_count" -gt 0 ]]; then
  exit 1
fi
exit 0
