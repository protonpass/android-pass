#!/bin/bash

set -eu

VERSION="11.9.0"
HASH="11bdc89110c3ce079b17cce8f5bead839e348f4e76b728941774a87021f4cbfb"

tmpDir=$(mktemp -d)
pushd $tmpDir || exit 1

wget -q "https://github.com/firebase/firebase-tools/releases/download/v${VERSION}/firebase-tools-linux"
echo "$HASH firebase-tools-linux" | sha256sum -c
chmod +x firebase-tools-linux
mv firebase-tools-linux /usr/bin/firebase

popd || exit 1
rm -rf $tmpDir
