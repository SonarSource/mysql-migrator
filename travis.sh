#!/bin/bash

set -euo pipefail

function installTravisTools {
  curl -sSL https://raw.githubusercontent.com/sonarsource/travis-utils/v10/install.sh | bash
  source /tmp/travis-utils/env.sh
}

installTravisTools

mvn verify -B -e -V
