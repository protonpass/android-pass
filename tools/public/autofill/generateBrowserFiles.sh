#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:tools/public/autofill::g')

BROWSERS_INPUT_FILE_PATH="${REPO_ROOT}/tools/public/autofill/browsers.csv"

TEMPLATE_PLACEHOLDER="{{CONTENT}}"
NATIVE_TEMPLATE_PLACEHOLDER="{{NATIVE_CONTENT}}"
BROWSER_TEMPLATE_PLACEHOLDER="{{BROWSER}}"
VERSION_TEMPLATE_PLACEHOLDER="{{VERSION}}"
XML_TEMPLATE_PATH="${REPO_ROOT}/tools/public/autofill/templates/xml.tpl"
XML_ROW_TEMPLATE_PATH="${REPO_ROOT}/tools/public/autofill/templates/xml_row.tpl"
KT_TEMPLATE_PATH="${REPO_ROOT}/tools/public/autofill/templates/kt.tpl"
KT_ROW_TEMPLATE_PATH="${REPO_ROOT}/tools/public/autofill/templates/kt_row.tpl"
KT_NATIVE_ROW_TEMPLATE_PATH="${REPO_ROOT}/tools/public/autofill/templates/kt_native_row.tpl"

DEST_BASE_PATH="${REPO_ROOT}/pass/autofill/impl/src/main"

XML_DEST_PATH="${DEST_BASE_PATH}/res/xml/autofill_service.xml"
KT_DEST_PATH="${DEST_BASE_PATH}/kotlin/proton/android/pass/autofill/BrowserList.kt"

XML_CONTENT=""
KT_CONTENT=""
KT_NATIVE_CONTENT=""

LINE_COUNT=0
NATIVE_COUNT=0
while read -r line; do
  if [[ -z "$line" ]]; then continue; fi

  if [[ $LINE_COUNT -gt 0 ]]; then
    XML_CONTENT="${XML_CONTENT}\n"
    KT_CONTENT="${KT_CONTENT},\n"
  fi
  LINE_COUNT=$((LINE_COUNT+1))

  BROWSER=$(echo "$line" | cut -d"," -f1)
  VERSION=$(echo "$line" | cut -d"," -f2)
  NATIVE=$(echo "$line" | cut -d"," -f3)

  XML_LINE=$(sed "s|${BROWSER_TEMPLATE_PLACEHOLDER}|${BROWSER}|g" "${XML_ROW_TEMPLATE_PATH}")
  XML_LINE=$(echo "${XML_LINE}" | sed "s|${VERSION_TEMPLATE_PLACEHOLDER}|${VERSION}|g")
  XML_CONTENT+="${XML_LINE}"

  KT_LINE=$(sed "s|${TEMPLATE_PLACEHOLDER}|${BROWSER}|g" "${KT_ROW_TEMPLATE_PATH}")
  KT_CONTENT+="${KT_LINE}"

  if [[ "${NATIVE}" == "true" ]]; then
    if [[ $NATIVE_COUNT -gt 0 ]]; then
      KT_NATIVE_CONTENT="${KT_NATIVE_CONTENT},\n"
    fi
    NATIVE_COUNT=$((NATIVE_COUNT+1))

    KT_NATIVE_LINE=$(sed "s|${TEMPLATE_PLACEHOLDER}|${BROWSER}|g" "${KT_NATIVE_ROW_TEMPLATE_PATH}")
    KT_NATIVE_CONTENT+="${KT_NATIVE_LINE}"
  fi

done < "${BROWSERS_INPUT_FILE_PATH}"

XML_FINAL_CONTENT=$(sed "s|${TEMPLATE_PLACEHOLDER}|${XML_CONTENT}|g" "${XML_TEMPLATE_PATH}")
KT_FINAL_CONTENT=$(sed -e "s|${TEMPLATE_PLACEHOLDER}|${KT_CONTENT}|g" -e "s|${NATIVE_TEMPLATE_PLACEHOLDER}|${KT_NATIVE_CONTENT}|g" "${KT_TEMPLATE_PATH}")

echo "${XML_FINAL_CONTENT}" > "${XML_DEST_PATH}"
echo "${KT_FINAL_CONTENT}" > "${KT_DEST_PATH}"
