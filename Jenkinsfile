#!/usr/bin/env groovy

@Library('SonarSource@1.2') _

def MAVEN_TOOL='Maven 3.3.9'

def dbs = ["oracle12c","mssql2016"]
def tasks = [:]

stage('build'){
  node('linux'){
    def scmVars = checkout scm
    sendAllNotificationQaStarted()

    for(dbSrc in dbs) {
        for(dbTarget in dbs) {
            echo "building task ${dbSrc}/${dbTarget}"
            tasks["${dbSrc}/${dbTarget}"] = {
                node('linux'){
                    stage('checkout') {
                        checkout scm
                    }
                    stage('Maven'){
                        echo "Building with Maven... ${dbSrc}/${dbTarget}"               
                        //sh "${tool MAVEN_TOOL}/bin/mvn package"  
                    }
                }
            }
        }
    }
    
    try{
      parallel tasks, 
      failFast: true
      sendAllNotificationQaResult()
    }catch (e) {
      sendAllNotificationQaResult()
    }
  }
}

def withJava(def body) {
  def javaHome = tool name: 'Java 8', type: 'jdk'
  withEnv(["JAVA_HOME=${javaHome}"]) {
    body.call()
  }
}