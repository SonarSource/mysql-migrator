#!/usr/bin/env bash

set -euo pipefail

targetDir=$(dirname "$0")/tables-and-version

schemaPath=server/sonar-db-core/src/main/resources/org/sonar/db/version/schema-h2.ddl
dbMigrationsBasedir=server/sonar-db-migration/src/main/java/org/sonar/server/platform/db/migration/version
# used to find such files: server/sonar-db-migration/src/main/java/org/sonar/server/platform/db/migration/version/v67/DbVersion67.java

error() {
    echo "error: $@" >&2
    exit 1
}

(( $# > 0 )) || error "usage: $0 SQ_VERSION..."

ensureCorrectGitRepo() {
    local runInSonarQubeMessage="You must run this script in a SonarQube Git project."
    [[ -f "$schemaPath" ]] || error "schema file '$schemaPath' does not exist; $runInSonarQubeMessage"
    [[ -d "$dbMigrationsBasedir" ]] || error "base directory of db migrations '$dbMigrationsBasedir' does not exist; $runInSonarQubeMessage"
}

ensureCleanWorkingTree() {
    [[ -z $(git status -s) ]] || error "Git working tree is not completely clean. Please clean it first."
}

extractTables() {
    grep '^CREATE TABLE' "$schemaPath" | cut -d'"' -f2 | tr A-Z a-z
}

extractVersion() {
    local sqVersion=$1
    sqVersion=${sqVersion//./}

    local dbMigrationPath=$dbMigrationsBasedir/v$sqVersion/DbVersion$sqVersion.java
    [[ -f "$dbMigrationPath" ]] || error "db migration definition source code does not exist: $dbMigrationPath"

    local version=$(awk -F'[(,]' '/^[ \t]+\.add/ { version = $2 } END { print version }' < "$dbMigrationPath")
    [[ $version =~ ^[0-9]+$ ]] || error "got '$version' as version; expected numeric value"
    echo $version
}

extractTablesAndVersion() {
    local sqVersion=$1
    git checkout "$sqVersion"

    local tablesFile=$targetDir/$sqVersion.tables
    echo "extracting tables to $tablesFile ..."
    extractTables > "$tablesFile"

    local versionFile=$targetDir/$sqVersion.version
    echo "extracting latest migration version to $versionFile ..."
    extractVersion "$sqVersion" > "$versionFile"
}

ensureCorrectGitRepo
ensureCleanWorkingTree

for sqVersion; do
    extractTablesAndVersion "$sqVersion"
done
