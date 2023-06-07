#!/bin/bash

# Make sure dependencies are in place
if ! command -v rg --help &> /dev/null; then
  echo "Could not find rg"
  exit 1
fi

# Array to store unused strings
unused_strings=()

# Find all strings.xml files in the project
string_files=$(find . -name "strings.xml" -path "*res/values/*")

# Iterate over each strings.xml file
for file in $string_files; do
    module_dir=$(dirname "$file")

    # Extract the module name from the file path
    module_name=$(basename "$module_dir")

    # Get the list of string names from the strings.xml file
    # -o: only print matching part
    # -N: do not print line number
    # -r '$1': Only output the captured group 1, which is the string name in the regex
    string_names=$(rg -oN 'name="([\w_]+)"' -r '$1' "$file")

    # Check if each string is used in the project
    for string_name in $string_names; do
        # Search for usages of R.string.<string_name> in the project using rg
        usage_count=$(rg -l "R\.string\.$string_name" | wc -l)

        # If the string is used, continue to the next string
        if [ $usage_count -gt 0 ]; then
          continue 1
        fi

        # Search for usages of R.plurals.<string_name> in the project using rg
        usage_count=$(rg -l "R\.plurals\.$string_name" | wc -l)
        # If the string is used, continue to the next string
        if [ $usage_count -gt 0 ]; then
          continue 1
        fi

        # Search for usages of @string/<string_name> in the project using rg
        usage_count=$(rg -l "@string/$string_name" | wc -l)

        # If after all the checks the resource is not used, add it to the unused_strings array
        if [ $usage_count -eq 0 ]; then
            echo "$string_name: $file"
            unused_strings+=("$string_name: $file")
        fi
    done
done

# Print the unused strings and their file paths
if [ ${#unused_strings[@]} -gt 0 ]; then
    exit 1
else
    echo "No unused strings found."
    exit 0
fi
