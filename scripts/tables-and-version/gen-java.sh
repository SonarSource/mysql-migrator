#!/usr/bin/env bash
#
# Generate Java code from the tables and versions extracted from SonarQube releases.
# Not ready-to-use code, but easy to copy-paste and reformat. If needed. Ever.
#

set -euo pipefail

cd "$(dirname "$0")"

error() {
    echo "error: $@" >&2
    exit 1
}

echo 'private static final Map<Integer, List<String>> TABLES_PER_VERSION = new HashMap<>();'
echo 'static {';

for versionFile in *.version; do
    sqVersion=${versionFile%.version}
    version=$(cat "$versionFile")
    echo "// SonarQube $sqVersion"
    echo "TABLES_PER_VERSION.put($version, Arrays.asList("
    mapfile -t tables < "$sqVersion.tables"
    for ((i = 0; i < ${#tables[@]}; i++)); do
        tables[$i]=\"${tables[i]}\"
    done
    (IFS=,; echo "${tables[*]}")
    echo "));"
    echo
done

echo '}'
