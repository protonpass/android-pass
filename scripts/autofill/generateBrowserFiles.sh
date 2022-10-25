#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:scripts/autofill::g')

BROWSERS_INPUT_FILE_PATH="${REPO_ROOT}/scripts/autofill/browsers.csv"

TEMPLATE_PLACEHOLDER="{{CONTENT}}"
BROWSER_TEMPLATE_PLACEHOLDER="{{BROWSER}}"
VERSION_TEMPLATE_PLACEHOLDER="{{VERSION}}"
XML_TEMPLATE_PATH="${REPO_ROOT}/scripts/autofill/templates/xml.tpl"
XML_ROW_TEMPLATE_PATH="${REPO_ROOT}/scripts/autofill/templates/xml_row.tpl"
KT_TEMPLATE_PATH="${REPO_ROOT}/scripts/autofill/templates/kt.tpl"
KT_ROW_TEMPLATE_PATH="${REPO_ROOT}/scripts/autofill/templates/kt_row.tpl"

DEST_BASE_PATH="${REPO_ROOT}/passwordManager/autofill/implementation/src/main"

XML_DEST_PATH="${DEST_BASE_PATH}/res/xml/autofill_service.xml"
KT_DEST_PATH="${DEST_BASE_PATH}/kotlin/me/proton/pass/autofill/BrowserList.kt"

XML_CONTENT=""
KT_CONTENT=""

LINE_COUNT=0
while read -r line; do
  if [[ -z "$line" ]]; then continue; fi

  if [[ $LINE_COUNT -gt 0 ]]; then
    XML_CONTENT="${XML_CONTENT}\n"
    KT_CONTENT="${KT_CONTENT},\n"
  fi
  LINE_COUNT=$((LINE_COUNT+1))

  BROWSER=$(echo "$line" | cut -d"," -f1)
  VERSION=$(echo "$line" | cut -d"," -f2)

  XML_LINE=$(sed "s|${BROWSER_TEMPLATE_PLACEHOLDER}|${BROWSER}|g" "${XML_ROW_TEMPLATE_PATH}")
  XML_LINE=$(echo "${XML_LINE}" | sed "s|${VERSION_TEMPLATE_PLACEHOLDER}|${VERSION}|g")
  XML_CONTENT+="${XML_LINE}"

  KT_LINE=$(sed "s|${TEMPLATE_PLACEHOLDER}|${BROWSER}|g" "${KT_ROW_TEMPLATE_PATH}")
  KT_CONTENT+="${KT_LINE}"

done < "${BROWSERS_INPUT_FILE_PATH}"

XML_FINAL_CONTENT=$(sed "s|${TEMPLATE_PLACEHOLDER}|${XML_CONTENT}|g" "${XML_TEMPLATE_PATH}")
KT_FINAL_CONTENT=$(sed "s|${TEMPLATE_PLACEHOLDER}|${KT_CONTENT}|g" "${KT_TEMPLATE_PATH}")

echo "${XML_FINAL_CONTENT}" > "${XML_DEST_PATH}"
echo "${KT_FINAL_CONTENT}" > "${KT_DEST_PATH}"
