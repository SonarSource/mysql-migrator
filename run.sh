#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

program=./build/install/mysql-migrator/bin/mysql-migrator

installed() {
    [ -x "$program" ]
}

if ! installed; then
    echo "It seems the project was not installed. Installing now..."
    ./gradlew install -x test
fi

if ! installed; then
    echo "Fatal: no such file: $program"
    exit 1
fi

"$program" "$@"
