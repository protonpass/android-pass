#!/usr/bin/env bash

# We must always mirror the full repository when publishing. This is because the entire
# history is rewritten with the repo filtering and it must always be up-to-date with
# references to branches and tags in the mirrored repository.
#
# The repo filtering produces the same results as long as the ignore file and history are
# the same as from the previous run, but when it does change, we don't want this to break.
#
# The drawback is that this can leak commits that are not released yet. However it's still
# an improvement compare to pre-monorepo because we no longer publish all branches and only do it on releases.

set -eo pipefail
set -x

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
REPO_ROOT=$(echo "${SCRIPT_DIR}" | sed 's:tools/publish::g')

IGNORE_FILE="$REPO_ROOT/.publishignore"
FILTER_SCRIPT="$SCRIPT_DIR/filter-repo.sh"

BRANCH="main"
DIR="$1"
PUBLIC_REPO_URL="$2"

if [ -z "$DIR" ] || [ -z "$PUBLIC_REPO_URL" ]; then
    echo "usage: $0 <CLONE_DIR> <PUBLIC_REPO_URL>"
    echo "$0 /tmp/app-clone git@github.com:ProtonMail/my-repo.git /tmp/deploy-key"
    exit 1
fi

rm -rf "$DIR" || true

origin=$(git remote get-url origin)

echo "Fetching full history"
git clone "$origin" "$DIR"
cd "$DIR"

# Ensure we have all LFS objects
git lfs fetch --all "$origin" "$BRANCH"

# Ensure all the release branches are tracked
for i in $(git branch -a | grep remote | grep -v HEAD | grep -v main | grep release/); do
    git branch --track "${i#remotes/origin/}" "$i"
done
# Remove origin to avoid remote branches in mirror
git remote remove origin

echo "Filtering repository"
# Delete all branches except main and release branches
(git branch | grep -v "$BRANCH" | grep -v "release/" | xargs git branch -D) || true

## Filter out everything sensitive
"$FILTER_SCRIPT" < "$IGNORE_FILE"

echo "Add new remote public $PUBLIC_REPO_URL"
git remote add public "$PUBLIC_REPO_URL"

echo "Pushing branches"

for i in $(git branch --format='%(refname:short)'); do
    echo "Pushing branch $i"
    git push --force public "$i"
done

echo "Pushing tags"
git push --force public --tags

