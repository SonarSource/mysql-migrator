#!/usr/bin/env bash

set -euo pipefail

error() {
    echo "error: $@" >&2
    exit 1
}

(( $# >= 2 )) || error "usage: $0 path/to/source.properties path/to/target.properties"

[[ -f "$1" ]] || error "file '$1' does not exist; expected path to source.properties"
[[ -f "$2" ]] || error "file '$2' does not exist; expected path to target.properties"

abspath() {
    local maybeRelativePath=$1
    echo "$(cd "$(dirname "$maybeRelativePath")"; pwd)/$(basename "$maybeRelativePath")"
}

sourcePath=$(abspath "$1"); shift
targetPath=$(abspath "$1"); shift

cd "$(dirname "$0")"

echoAndRun() {
    echo "$@"
    "$@"
}

echoAndRun ./gradlew --no-daemon --info integrationTest \
    -Dsonar.runtimeVersion=DEV \
    -Dorchestrator.configUrl.source="file://$sourcePath" \
    -Dorchestrator.configUrl.target="file://$targetPath" \
    "$@"
