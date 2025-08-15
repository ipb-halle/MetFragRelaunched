#!/bin/bash
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: $0 <release-version> <next-snapshot-version>"
  exit 1
fi

RELEASE_VERSION="$1"
NEXT_SNAPSHOT_VERSION="$2"

# 1. Auf dev: Release-Version setzen und committen
git checkout dev
mvn versions:set -DnewVersion="$RELEASE_VERSION"
mvn versions:commit
git add pom.xml */pom.xml
git commit -m "Release version $RELEASE_VERSION"
git push origin dev

# 2. Nach main mergen
git checkout master
git merge dev
git push origin master

# 3. Release taggen und auf GitHub veröffentlichen
git tag -a "v$RELEASE_VERSION" -m "Release version $RELEASE_VERSION"
git push origin "v$RELEASE_VERSION"
gh release create "v$RELEASE_VERSION" --title "Release v$RELEASE_VERSION" --notes "Release $RELEASE_VERSION"

# 4. Zurück zu dev und nächste SNAPSHOT-Version setzen
git checkout dev
mvn versions:set -DnewVersion="$NEXT_SNAPSHOT_VERSION"
mvn versions:commit
git add pom.xml
git commit -m "Prepare for next development iteration: $NEXT_SNAPSHOT_VERSION"
git push origin dev