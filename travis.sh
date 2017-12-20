#!/bin/bash

set -euo pipefail

function installTravisTools {
  mkdir  -p ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/ceb2aff | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}

installTravisTools
. installMaven35
sonarcloud_mvn_build_deploy_analyze
