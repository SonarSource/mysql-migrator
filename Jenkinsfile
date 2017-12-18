#!/usr/bin/env groovy

@Library('SonarSource@1.2') _

def MAVEN_TOOL='Maven 3.3.9'

stage('build'){
  node('linux'){
    def scmVars = checkout scm
    sendAllNotificationQaStarted()
    
    try{
      parallel maven: {
        node('linux'){
          stage('checkout') {
            checkout scm
          }
          stage('Maven'){
            echo 'Building with Maven...'              
            //sh "${tool MAVEN_TOOL}/bin/mvn package"  
          }
        }
      }, 
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