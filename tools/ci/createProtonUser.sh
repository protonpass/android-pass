#!/bin/bash

set -eu

if [[ $# -ne 1 ]]; then
  echo "Invalid usage. $0 <USERNAME>"
  exit 1
fi

if [[ -z "${PROTON_BASE_URL}" ]]; then
  echo "PROTON_BASE_URL not defined"
  exit 1
fi

USERNAME="$1"
URL="${PROTON_BASE_URL}/user:create?--name=${USERNAME}&--password=${USERNAME}&-k=Curve25519"

curl -X GET $URL
