#!/bin/bash
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: $0 <release-version> <next-snapshot-version>"
  echo "Current version is $(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
  exit 1
fi

RELEASE_VERSION="$1"
NEXT_SNAPSHOT_VERSION="$2"

# 1. set version in dev and commit
git checkout dev
mvn versions:set -DnewVersion="$RELEASE_VERSION"
mvn versions:commit
git add pom.xml */pom.xml
git commit -m "Release version $RELEASE_VERSION"
git push origin dev

# 2. merge to main
git checkout master
git merge dev
git push origin master

# 3. tag release and publish
git tag -a "v$RELEASE_VERSION" -m "Release version $RELEASE_VERSION"
git push origin "v$RELEASE_VERSION"
gh release create "v$RELEASE_VERSION" --title "Release v$RELEASE_VERSION" --notes "Release $RELEASE_VERSION"

# 4. go back to dev and set next SNAPSHOT version
git checkout dev
mvn versions:set -DnewVersion="$NEXT_SNAPSHOT_VERSION"
mvn versions:commit
git add pom.xml */pom.xml
git commit -m "Prepare for next development iteration: $NEXT_SNAPSHOT_VERSION"
git push origin dev

