#!/bin/bash

#
# Copyright (c) 2025 Proton AG
# This file is part of Proton AG and Proton Pass.
#
# Proton Pass is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Proton Pass is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
#

SOURCE_DIR="path/to/source/res"
TARGET_DIR="path/to/target/res"

merge_strings() {
    for values_dir in $(ls "$SOURCE_DIR" | grep '^values'); do
        SOURCE_FILE="$SOURCE_DIR/$values_dir/strings.xml"
        TARGET_FILE="$TARGET_DIR/$values_dir/strings.xml"

        if [[ ! -f "$SOURCE_FILE" ]]; then
            continue
        fi

        mkdir -p "$TARGET_DIR/$values_dir"

        if [[ -f "$TARGET_FILE" ]]; then
            TMP_FILE="$(mktemp)"
            sed '$d' "$TARGET_FILE" > "$TMP_FILE"
            grep '<string name=' "$SOURCE_FILE" >> "$TMP_FILE"
            echo "</resources>" >> "$TMP_FILE"
            mv "$TMP_FILE" "$TARGET_FILE"
        else
            cp "$SOURCE_FILE" "$TARGET_FILE"
        fi

        echo "Appended strings into $TARGET_FILE"
    done
}

merge_strings
