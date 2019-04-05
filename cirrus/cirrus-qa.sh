#!/bin/bash

. ./cirrus/cirrus-env.sh QA

target_db=$1

if [[ "$target_db" == oracle* ]]; then
  # Need ~ 10 minutes to have a running instance of Oracle
  sleep 600
fi

source_config=file://$CIRRUS_WORKING_DIR/it/config/mysql57.properties
target_config=file://$CIRRUS_WORKING_DIR/it/config/$target_db.properties

gradle --no-daemon --info --console plain build install

cd it
gradle --no-daemon --info --console plain integrationTest \
    -Dsonar.runtimeVersion=$SQ_RUNTIME \
    -Dorchestrator.configUrl.source="$source_config" \
    -Dorchestrator.configUrl.target="$target_config"
