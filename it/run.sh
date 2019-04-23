#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

source=mysql56
target=postgresql93

if [[ -z ${SQ_RUNTIME+x} ]]; then
    SQ_RUNTIME=LATEST_RELEASE
fi

./gradlew --no-daemon --info integrationTest \
    -Dsonar.runtimeVersion="$SQ_RUNTIME" \
    -Dorchestrator.configUrl.source="$ARTIFACTORY_URL/orchestrator.properties/orch-$source.properties" \
    -Dorchestrator.configUrl.target="$ARTIFACTORY_URL/orchestrator.properties/orch-$target.properties" \
    "$@"
