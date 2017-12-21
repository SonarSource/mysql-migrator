#!/usr/bin/env groovy

@Library('SonarSource@1.2') _

def MAVEN_TOOL='Maven 3.3.x'

def dbs = ["postgresql93", "mysql56", "mssql2014", "mssql2016", "oracle11g", "oracle12c"]
def ignoredTuples = [
    //  [ Source       , Destination]

    //  Not testing copy to same DB engine
        ["postgresql93", "postgresql93"],
        ["mysql56"     , "mysql56"],
        ["mssql2014"   , "mssql2014"],
        ["mssql2016"   , "mssql2016"],
        ["oracle11g"   , "oracle11g"],
        ["oracle12c"   , "oracle12c"],

    //  Not testing DB engine upgrades/downgrades
        ["mssql2014"   , "mssql2016"],
        ["mssql2016"   , "mssql2014"],
        ["oracle11g"   , "oracle12c"],
        ["oracle12c"   , "oracle11g"]
]
// TODO Un-restrict SQ versions
def sqVersions = ["LTS"] // , "DEV"]

def tasks = [:]

stage('Notify') {
  node('linux') {
    sendAllNotificationQaStarted()
  }
}

stage('QA'){
  node('linux'){
    def scmVars = checkout scm

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
            def withOjdbc = [src, target].contains("oracle11g") ? "-DwithOjdbc=6 " :
                    [src, target].contains("oracle12c") ? "-DwithOjdbc=8 " :
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
                          "-DjavaVersion=LATEST_RELEASE " +
                          "-Dorchestrator.configUrl.source=http://infra.internal.sonarsource.com/jenkins/orch-${src}.properties " +
                          "-Dorchestrator.configUrl.destination=http://infra.internal.sonarsource.com/jenkins/orch-${target}.properties " +
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
    } finally {
      sendAllNotificationQaResult()
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
