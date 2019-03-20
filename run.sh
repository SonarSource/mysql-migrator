#!/usr/bin/env bash

cd "$(dirname "$0")"

jar_path_pattern=target/*-jar-with-dependencies.jar

find_jar() {
    jar_path=($jar_path_pattern)
}

find_jar

if [ ! -f "$jar_path" ]; then
    echo "Could not find jar: $jar_path_pattern"
    echo "Perhaps project was not built? Building now..."
    mvn install -DskipTests
    find_jar
fi

if [ ! -f "$jar_path" ]; then
    echo "Fatal: no such file: $jar_path"
    exit 1
fi

java -jar "$jar_path" "$@"
