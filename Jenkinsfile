#!/usr/bin/env groovy

@Library('SonarSource@2.1.1') _

def MAVEN_TOOL='Maven 3.3.x'

def dbs = ["postgresql93", "mysql56", "mssql2014", "mssql2016", "oracle12c"]
def ignoredTuples = [
    //  [ Source       , Destination]

    //  Not testing copy to same DB engine
        ["postgresql93", "postgresql93"],
        ["mysql56"     , "mysql56"],
        ["mssql2014"   , "mssql2014"],
        ["mssql2016"   , "mssql2016"],
        ["oracle12c"   , "oracle12c"],

    //  Not testing DB engine upgrades/downgrades
        ["mssql2014"   , "mssql2016"],
        ["mssql2016"   , "mssql2014"]
]

def sqVersions = ["LATEST_RELEASE[6.7]", "DEV"]

def tasks = [:]

stage('Notify') {
  node('linux') {
    sendAllNotificationQaStarted()
  }
}

stage('QA'){
  node('linux'){
    try{
      for(sqVersion in sqVersions) {
        for (dbSrc in dbs) {
          for (dbTarget in dbs) {
            if (ignoredTuples.contains([dbSrc, dbTarget])) {
              echo "not building ${dbSrc}/${dbTarget}"
              continue
            }
            def src = dbSrc
            def target = dbTarget
            def sqVer = sqVersion
            def withOjdbc = [src, target].contains("oracle12c") ? "-DwithOjdbc=8 " :
                    " "
            echo "building task ${sqVersion}/${src}/${target}"
            tasks["${sqVer}/${src}/${target}"] = {
              node('linux') {
                stage('checkout') {
                  checkout scm
                }
                stage('Maven') {
                  dir('it') {
                    echo "building ${sqVer}/${src}/${target}"
                    withMaven(maven: MAVEN_TOOL) {
                      // Set version number according to build number
                      mavenSetBuildVersion()
                      // Get specific version number
                      buildVersion = mavenGetProjectVersion()
                      withEnv(['SONARSOURCE_QA=true']) {
                        sh "mvn " +
                          "-Dsonar.dbCopyVersion=${buildVersion} " +
                          "-Dsonar.runtimeVersion=${sqVer} " +
                          "-Dorchestrator.configUrl.source=${env.ARTIFACTORY_URL}/orchestrator.properties/orch-${src}.properties " +
                          "-Dorchestrator.configUrl.destination=${env.ARTIFACTORY_URL}/orchestrator.properties/orch-${target}.properties " +
                          withOjdbc +
                          "-Dmaven.test.redirectTestOutputToFile=false " +
                          "clean verify -e -V "
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      parallel tasks
      sendAllNotificationQaResult()
    } catch (e) {
      currentBuild.result = "FAILURE"
      sendAllNotificationQaResult()
      error "Build failed"
    }
  }
}

stage('Promote') {
  node('linux') {
    try {
      repoxPromoteBuild()
    } finally {
      sendAllNotificationPromote()
    }
  }
}
