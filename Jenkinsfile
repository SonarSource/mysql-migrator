#!/usr/bin/env groovy

@Library('SonarSource@1.2') _

def MAVEN_TOOL='Maven 3.3.x'

// TODO Un-restrict DB engines
def dbs = ["postgresql93", "mysql56"] // , "mssql2014", "mssql2016", "oracle11g", "oracle12c"]
// TODO Un-restrict SQ versions
def sqVersions = ["LTS"] // , "DEV"]

def tasks = [:]

stage('build'){
  node('linux'){
    def scmVars = checkout scm
    sendAllNotificationQaStarted()

    try{
      for(sqVersion in sqVersions) {
        for (dbSrc in dbs) {
          for (dbTarget in dbs) {
            if (dbTarget == dbSrc) {
              echo "not building ${dbSrc}/${dbTarget}"
              continue
            }
            def src = dbSrc
            def target = dbTarget
            echo "building task ${sqVersion}/${src}/${target}"
            tasks["${sqVersion}/${src}/${target}"] = {
              node('linux') {
                stage('checkout') {
                  checkout scm
                }
                stage('Maven') {
                  dir('it') {
                    echo "building ${sqVersion}/${src}/${target}"
                    withMaven(maven: MAVEN_TOOL) {
                      // Set version number according to build number
                      mavenSetBuildVersion()
                      // Get specific version number
                      buildVersion = mavenGetProjectVersion()
                      withEnv(['SONARSOURCE_QA=true']) {
                        sh "mvn " +
                          "-Dsonar.dbCopyVersion=${buildVersion} " +
                          "-Dsonar.runtimeVersion=${sqVersion} " +
                          "-Dorchestrator.configUrl.source=http://infra.internal.sonarsource.com/jenkins/orch-${dbSrc}.properties " +
                          "-Dorchestrator.configUrl.destination=http://infra.internal.sonarsource.com/jenkins/orch-${dbTarget}.properties " +
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
    } catch (ignored) {
      sendAllNotificationQaResult()
    }
  }
}
