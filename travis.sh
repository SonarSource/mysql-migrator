#!/bin/bash

set -euo pipefail

function installTravisTools {
  mkdir ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/v21 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}

function strongEcho {
  echo ""
  echo "================ $1 ================="
}

installTravisTools

build "SonarSource/parent" "30"

if [ "${TRAVIS_BRANCH}" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  strongEcho 'Build and analyze commit in master'
  # this commit is master must be built and analyzed (with upload of report)
  mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify -Pcoverage-per-test -B -e -V

  export MAVEN_OPTS="-Xmx1G -Xms128m"
  mvn sonar:sonar -B -e -V \
     -Dsonar.host.url=$SONAR_HOST_URL \
     -Dsonar.login=$SONAR_LOGIN \
     -Dsonar.password=$SONAR_PASSWORD


elif [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "$SONAR_GITHUB_OAUTH" ]; then
  # For security reasons environment variables are not available on the pull requests
  # coming from outside repositories
  # http://docs.travis-ci.com/user/pull-requests/#Security-Restrictions-when-testing-Pull-Requests
  # That's why the analysis does not need to be executed if the variable SONAR_GITHUB_OAUTH is not defined.

  strongEcho 'Build and analyze pull request'
  # this pull request must be built and analyzed (without upload of report)
  mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify -Pcoverage-per-test -B -e -V

  mvn sonar:sonar -B -e -V \
      -Dsonar.analysis.mode=issues \
      -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
      -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
      -Dsonar.github.oauth=$SONAR_GITHUB_OAUTH \
      -Dsonar.host.url=$SONAR_HOST_URL \
      -Dsonar.login=$SONAR_LOGIN \
      -Dsonar.password=$SONAR_PASSWORD


else
  strongEcho 'Build, no analysis'
  # Build branch, without any analysis

  # No need for Maven goal "install" as the generated JAR file does not need to be installed
  # in Maven local repository
  mvn verify -B -e -V
fi
