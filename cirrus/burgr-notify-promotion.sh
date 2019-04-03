#!/bin/bash

. ./cirrus/cirrus-env.sh PROMOTE

# Set the list of artifacts to display in BURGR
ARTIFACTS=org.sonarsource.sqdbmigrator:mysql-migrator:zip

# Compute the version of the project
PROJECT_VERSION=$(cat gradle.properties | sed -ne 's/^version=//p')
if [[ "${PROJECT_VERSION}" == *"-SNAPSHOT" ]]; then
  PROJECT_VERSION=${PROJECT_VERSION%-SNAPSHOT}
  DIGITS=(${PROJECT_VERSION//./ })
  if [[ ${#DIGITS[@]} = 2 ]]; then
    PROJECT_VERSION=$PROJECT_VERSION.0
  fi
  PROJECT_VERSION=$PROJECT_VERSION.$BUILD_NUMBER
fi

# Create the URL of an artifact given a GAC (i.e. org.sonarsource.sonarqube:sonar-application:zip or com.sonarsource.sonarqube:sonarqube-enterprise:yguard:xml)
createArtifactURL() {
  ARTIFACT=$1
  GAQE=(${ARTIFACT//:/ })
  GROUP_ID=${GAQE[0]}
  ARTIFACT_ID=${GAQE[1]}
  GROUP_ID_PATH=${GROUP_ID//./\/}

  FILENAME=$ARTIFACT_ID-$PROJECT_VERSION
  if [[ ${#GAQE[@]} = 4 ]];then
    CLASSIFIER=${GAQE[2]}
    EXTENSION=${GAQE[3]}
    FILENAME=$FILENAME-$CLASSIFIER
  else
    EXTENSION=${GAQE[2]}
  fi
  FILENAME=$FILENAME.$EXTENSION

  echo "$ARTIFACTORY_URL/sonarsource/$GROUP_ID_PATH/$ARTIFACT_ID/$PROJECT_VERSION/$FILENAME"
}

# Create a comma-separated list of URLs of artifacts
createArtifactsURLS() {
  ARTIFACTS=(${1//,/ })
  URLS=()
  for artifact in "${ARTIFACTS[@]}"; do
    URLS+=($(createArtifactURL "$artifact"))
  done
  local IFS=,
  echo "${URLS[*]}"
}

URLS=
if [[ $ARTIFACTS ]]; then
  URLS=$(createArtifactsURLS "$ARTIFACTS")
fi

BURGR_FILE=promote.burgr
cat > "$BURGR_FILE" <<EOF
{
  "version":"$PROJECT_VERSION",
  "url":"$URLS",
  "buildNumber":"$BUILD_NUMBER"
}
EOF

set -x
HTTP_CODE=$(curl -s -o /dev/null -w %{http_code} -X POST -d @"$BURGR_FILE" -H "Content-Type:application/json" -u"${BURGR_USERNAME}:${BURGR_PASSWORD}" "${BURGR_URL}/api/promote/${CIRRUS_REPO_OWNER}/${CIRRUS_REPO_NAME}/${CIRRUS_BUILD_ID}")
if [[ "$HTTP_CODE" != "200" ]]; then
  echo "Cannot notify BURGR ($HTTP_CODE)"
else
  echo "BURGR notified"
fi
