#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DETEKT_JAR="$SCRIPT_DIR/detekt-cli-1.23.5-all.jar"

echo "Running detekt check..."
fileArray=($@)
detektInput=$(IFS=,;printf  "%s" "${fileArray[*]}")
echo "Input files: $detektInput"

OUTPUT=$(java -jar "$DETEKT_JAR" --input "$detektInput" --config "./config/detekt/config.yml" 2>&1)
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
  echo "${OUTPUT}"
  echo "***********************************************"
  echo "                 Detekt failed                 "
  echo " Please fix the above issues before committing "
  echo "***********************************************"
  exit $EXIT_CODE
fi
