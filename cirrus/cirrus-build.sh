#!/bin/bash

. ./cirrus/cirrus-env.sh BUILD

SONAR_HOST_URL=https://sonarcloud.io
SONAR_TOKEN=$SONARCLOUD_TOKEN

gradle_common_params=(
    "-PjacocoEnabled=true"
    "-DbuildNumber=$BUILD_NUMBER"
    "-Dsonar.host.url=$SONAR_HOST_URL"
    "-Dsonar.login=$SONAR_TOKEN"
    "-Dsonar.organization=sonarsource"
    "-Dsonar.analysis.buildNumber=$BUILD_NUMBER"
    "-Dsonar.analysis.pipeline=$CIRRUS_BUILD_ID"
    "-Dsonar.analysis.repository=$GITHUB_REPO"
    "-Dsonar.analysis.sha1=$GIT_SHA1"
    "-Dsonar.exclusions=src/generated/**"
    )

gradle_additional_params=()
if [[ "$PULL_REQUEST" ]]; then
    git fetch origin "${GITHUB_BASE_BRANCH}"
    gradle_additional_params+=("-Dsonar.analysis.prNumber=${PULL_REQUEST}")
fi

gradle --no-build-cache --no-daemon --console plain \
    build sonarqube artifactoryPublish \
    "${gradle_common_params[@]}" \
    "${gradle_additional_params[@]}"
