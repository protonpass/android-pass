#!/bin/bash

if [[ $# -eq 0 ]]; then
    echo "Invalid usage. ${0} VERSION"
    exit 1
fi

VERSION=$1

MAJOR=$(echo "$VERSION" | cut -d. -f1)
MINOR=$(echo "$VERSION" | cut -d. -f2)
PATCH=$(echo "$VERSION" | cut -d. -f3)

MAJOR_CODE=$((MAJOR * 10000000))
MINOR_CODE=$((MINOR * 100000))
PATCH_CODE=$((PATCH * 1000))

VERSION_CODE=$((MAJOR_CODE + MINOR_CODE + PATCH_CODE))

echo -n "${VERSION_CODE}"
