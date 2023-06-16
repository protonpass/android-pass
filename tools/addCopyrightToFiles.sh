#!/bin/bash

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:tools::g')

HEADER="Copyright (c) 2023 Proton AG"

# This script adds the following header to all files in the current directory
COPYRIGHT="/*
 * ${HEADER}
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */
"

# Get all kotlin files in the repo
for f in $(find "${REPO_ROOT}" -name "*.kt"); do
  # Check if the file already contains the header
  if ! grep -q "$HEADER" "$f"; then
    # Add the header to the file
    echo "Adding header to $f"
    echo "$COPYRIGHT" | cat - "$f" > temp && mv temp "$f"
  else
      echo "$f already contains header"
  fi

done
