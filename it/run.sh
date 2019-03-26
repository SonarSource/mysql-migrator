#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

./gradlew integrationTest \
    -Dsonar.runtimeVersion=DEV \
    -Dorchestrator.configUrl.source="file://$PWD/do-not-merge/mysql.properties" \
    -Dorchestrator.configUrl.target="file://$PWD/do-not-merge/postgresql.properties" \
    "$@"
