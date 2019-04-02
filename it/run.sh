#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

# TODO provision (in Docker) and verify
source=mysql56
# TODO all axes: "postgresql93", "mysql56", "mssql2014", "mssql2016", "oracle12c"
target=postgresql93

./gradlew --no-daemon --info integrationTest \
    -Dsonar.runtimeVersion=DEV \
    -Dorchestrator.configUrl.source="$ARTIFACTORY_URL/orchestrator.properties/orch-$source.properties" \
    -Dorchestrator.configUrl.target="$ARTIFACTORY_URL/orchestrator.properties/orch-$target.properties" \
    "$@"
