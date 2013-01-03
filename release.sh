#!/bin/bash

VERSION="$1"

error()
{
    echo -e "$1" >&2
    exit 1
}

getProjectVersionXPath()
{
    xpath -q -e '/project/version' pom.xml | sed -e 's/^<version>//' -e 's/<\/version>.*$//' -e 's/-SNAPSHOT//'
}

getProjectVersionMaven()
{
    mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version 2>/dev/null | sed '/^\[/d'
}

getProjectVersion()
{
    [ -n "$(which xpath 2>/dev/null)" ] && getProjectVersionXPath || getProjectVersionMaven
}

getLastReleaseVersion()
{
    git log --grep="maven-release-plugin" pom.xml | sed -n 's/^.*maven-release-plugin.*prepare release release\///p' | head -1
}

if [ -z "$VERSION" ]; then
    error "Please provide a release version string\n--> suggesting: \"$(getProjectVersion)\"\n--> last release: \"$(getLastReleaseVersion)\""
fi

DEV_VERSION=$(mvn help:evaluate -Dexpression=project.version -N | sed -n '/^[0-9]\+.*-SNAPSHOT$/p')

mvn release:prepare \
    -DautoVersionSubmodules=true \
    -DdevelopmentVersion=${DEV_VERSION} \
    -DreleaseVersion=${VERSION} \
    -Dtag=release/${VERSION} \
    -DpreparationGoals="clean,install,verify" \
    -Darguments="-DskipTests=true" || error "Release preparation failed!"

mvn release:perform -Darguments="-DskipTests=true" || error "Release failed!"
