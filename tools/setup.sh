#!/bin/bash

unameOut="$(uname -s)"

if ! command -v pre-commit &> /dev/null; then
  case "${unameOut}" in
      Linux*)     pip install --user pre-commit;;
      Darwin*)    brew install pre-commit;;
      *)          echo "Unsupported platform. Please install pre-commit yourself.";;
  esac
else
  echo "pre-commit already available"
fi


if ! command -v detekt &> /dev/null; then
  case "${unameOut}" in
      Linux*)
        VERSION="1.21.0"
        tmpDir=$(mktemp -d)
        pushd $tmpDir || exit 1
        curl -sSLO "https://github.com/detekt/detekt/releases/download/v${VERSION}/detekt-cli-${VERSION}.zip"
        unzip -q "detekt-cli-${VERSION}.zip"
        mkdir -p "${HOME}/.local/bin"
        mkdir -p "${HOME}/.local/lib"
        mv "detekt-cli-${VERSION}/bin/detekt-cli" "${HOME}/.local/bin/detekt-cli"
        mv detekt-cli-${VERSION}/lib/*.jar "${HOME}/.local/lib/"
        popd || exit 1
        rm -rf $tmpDir

        ln -sf "${HOME}/.local/bin/detekt-cli" "${HOME}/.local/bin/detekt"
        ;;
      Darwin*)    brew install detekt;;
      *)          echo "Unsupported platform. Please install detekt yourself.";;
  esac
else
  echo "detekt already available"
fi

pre-commit install
