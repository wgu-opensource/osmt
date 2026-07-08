#!/usr/bin/env bash
# Tags the current git commit with the next patch version.
# Only works on the main branch.
# Usage: tag-next-version.sh
# Env: BRANCH (default: main), CI (for git config), MAJOR_MINOR (default: 4.0)

set -euo pipefail

MAJOR_MINOR="${MAJOR_MINOR:-4.0}"
TAG_PREFIX="${MAJOR_MINOR}."

normalize_version() {
  local v="$1"
  v="${v#v}"
  echo "$v"
}

CURRENT_BRANCH="${BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
if [ "$CURRENT_BRANCH" != "main" ]; then
  echo "Error: This script can only be run on the main branch" >&2
  echo "Current branch: $CURRENT_BRANCH" >&2
  exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
  echo "Error: There are uncommitted changes" >&2
  exit 1
fi

git fetch --tags origin 2>/dev/null || true

LAST_TAG=$(git tag -l "${TAG_PREFIX}*" 2>/dev/null | sort -V | tail -n1 || true)
if [ -z "$LAST_TAG" ]; then
  NEXT_TAG="${TAG_PREFIX}0"
else
  LAST_PATCH=$(normalize_version "$LAST_TAG" | sed "s/^${MAJOR_MINOR}\.//" | cut -d'-' -f1)
  NEXT_PATCH=$((10#${LAST_PATCH} + 1))
  NEXT_TAG="${TAG_PREFIX}${NEXT_PATCH}"
fi

CURRENT_COMMIT=$(git rev-parse HEAD)
if git tag --points-at "$CURRENT_COMMIT" | grep -qE "^v?[0-9]+\.[0-9]+\.[0-9]+$"; then
  echo "Error: A version tag already exists for this commit" >&2
  exit 1
fi

echo "Creating new tag: $NEXT_TAG"

if [ "${CI:-}" = "true" ]; then
  git config --local user.email "github-actions[bot]@users.noreply.github.com"
  git config --local user.name "github-actions[bot]"
fi

git tag "$NEXT_TAG"
git push origin "$NEXT_TAG"

echo "Successfully created and pushed tag: $NEXT_TAG"
