#!/usr/bin/env groovy

@Library('SonarSource@1.2') _

def MAVEN_TOOL='Maven 3.3.9'

def dbs = ["oracle12c","mssql2016"]
def tasks = [:]

stage('build'){
  node('linux'){
    def scmVars = checkout scm
    sendAllNotificationQaStarted()
    
    try{
      for(dbSrc in dbs) {
        for(dbTarget in dbs) {
          def src=dbSrc
          def target=dbTarget
          echo "building task ${dbSrc}/${dbTarget}"
          tasks["${dbSrc}/${dbTarget}"] = {
            node('linux'){
              stage('checkout') {
                checkout scm
              }
              stage('Maven'){
                echo "building ${src}/${target}"
                //sh "${tool MAVEN_TOOL}/bin/mvn package"  
              }
            }              
          }
        }
      }
      tasks["failFast"]= true
      parallel tasks
      sendAllNotificationQaResult()
    }catch (e) {
      sendAllNotificationQaResult()
    }
  }
}
