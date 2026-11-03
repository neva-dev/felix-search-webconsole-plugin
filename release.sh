#!/usr/bin/env sh
set -e

VERSION="$1"
VERSION_CURRENT=$(git describe --tags --abbrev=0 2>/dev/null || echo "0.0.0")
VERSION_CURRENT="${VERSION_CURRENT#v}"

if [ -z "$VERSION" ]; then
    echo "$VERSION_CURRENT"
    exit 0
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$BRANCH" != "main" ]; then
    echo "Unable to release. Current branch is '$BRANCH', expected 'main'!" >&2
    exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
    echo "Unable to release. Uncommitted changes detected!" >&2
    exit 1
fi

VERSION_TAG="v${VERSION#v}"

echo "Releasing $VERSION_TAG (previous: $VERSION_CURRENT)"
git push origin main
git tag "$VERSION_TAG"
git push origin "$VERSION_TAG"
