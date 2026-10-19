#!/bin/bash

echo "Running detekt check..."
fileArray=($@)
detektInput=$(IFS=,;printf  "%s" "${fileArray[*]}")
echo "Input files: $detektInput"

DETEKT_VERSION=$(detekt --version 2>/dev/null | head -n 1 | tr -d '[:space:]')
FORMATTING_DIR="${XDG_CACHE_HOME:-$HOME/.cache}/proton-pass/detekt"
FORMATTING_JAR="$FORMATTING_DIR/detekt-formatting-$DETEKT_VERSION.jar"
FORMATTING_URL="https://repo1.maven.org/maven2/io/gitlab/arturbosch/detekt/detekt-formatting/$DETEKT_VERSION/detekt-formatting-$DETEKT_VERSION.jar"

if [ -z "$DETEKT_VERSION" ]; then
  echo "Could not determine the installed detekt version"
  exit 1
fi

if [ ! -f "$FORMATTING_JAR" ]; then
  echo "Downloading detekt-formatting $DETEKT_VERSION..."
  mkdir -p "$FORMATTING_DIR"
  if ! curl -sfL -o "$FORMATTING_JAR.tmp" "$FORMATTING_URL"; then
    rm -f "$FORMATTING_JAR.tmp"
    echo "Failed to download $FORMATTING_URL"
    exit 1
  fi
  mv "$FORMATTING_JAR.tmp" "$FORMATTING_JAR"
fi

OUTPUT=$(detekt --input "$detektInput" --config "./config/detekt/config.yml" --plugins "$FORMATTING_JAR" 2>&1)
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  echo "${OUTPUT}"
  echo "***********************************************"
  echo "                 Detekt failed                 "
  echo " Please fix the above issues before committing "
  echo "***********************************************"
  exit $EXIT_CODE
fi
