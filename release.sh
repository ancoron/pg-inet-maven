#!/bin/bash

VERSION="$1"

error()
{
    echo "$1" >&2
    exit 1
}

if [ -z "$VERSION" ]; then
    error "Please provide a release version string"
fi

DEV_VERSION=$(mvn help:evaluate -Dexpression=project.version -N | sed -n '/^[0-9]\+.*-SNAPSHOT$/p')

mvn release:prepare \
    -DautoVersionSubmodules=true \
    -DdevelopmentVersion=${DEV_VERSION} \
    -DreleaseVersion=${VERSION} \
    -Dtag=release/${VERSION} \
    -DpreparationGoals="clean,install,verify" \
    -Darguments="-DskipTests" || error "Release preparation failed!"

mvn release:perform -Darguments="-DskipTests" || error "Release failed!"
