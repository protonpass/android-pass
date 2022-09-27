#!/bin/bash

git log --pretty=format:'%s' --since="1 day ago" | grep -v "Merge branch"

if [[ $? -ne 0 ]]; then
    echo "No changes"
fi

