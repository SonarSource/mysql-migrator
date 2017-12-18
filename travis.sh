#!/bin/bash

set -euo pipefail

function installTravisTools {
  mkdir  -p ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/v42 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}

installTravisTools
. installMaven35
regular_mvn_build_deploy_analyze
