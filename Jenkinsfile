@Library('SonarSource@github') _

pipeline {
    agent { 
        label 'linux' 
    }
    parameters {
        string(name: 'GIT_SHA1', description: 'Git SHA1 (provided by travisci hook job)')
        string(name: 'CI_BUILD_NAME', defaultValue: 'sonar-license', description: 'Build Name (provided by travisci hook job)')	
        string(name: 'CI_BUILD_NUMBER', description: 'Build Number (provided by travisci hook job)')
        string(name: 'GITHUB_BRANCH', defaultValue: 'master', description: 'Git branch (provided by travisci hook job)')
        string(name: 'GITHUB_REPOSITORY_OWNER', defaultValue: 'SonarSource', description: 'Github repository owner(provided by travisci hook job)')
    }
    environment { 
        SONARSOURCE_QA = 'true'
        MAVEN_TOOL = 'Maven 3.3.x'
    }
    stages {    
        stage('NotifyBurgr') {
            steps {                
                burgrNotifyQaStarted()    
                githubNotifyQaPending()
            }
        }
        stage('QA') {            
            parallel {            
                stage('LTS') {
                    agent {
                        label 'linux'
                    }
                    steps {                          
                        withMaven(maven: MAVEN_TOOL) {
                            mavenSetBuildVersion()
                            dir('it') {
                                sh 'mvn -Dsonar.runtimeVersion="LTS" -Dmaven.test.redirectTestOutputToFile=false clean verify -e -V'
                            }
                        }                        
                    }
                }
                stage('DEV') {
                    agent {
                        label 'linux'
                    }
                    steps {
                        withMaven(maven: MAVEN_TOOL) {
                            mavenSetBuildVersion()
                            dir('it') {
                                sh 'mvn -Dsonar.runtimeVersion="DEV" -Dmaven.test.redirectTestOutputToFile=false clean verify -e -V'
                            }
                        }
                    }
                }
            }                
            post {
                success {                
                    burgrNotifyQaPassed()
                    githubNotifyQaSuccess()
                }
                failure {
                    burgrNotifyQaFailed()
                    githubNotifyQaFailed()
                }
                unstable {
                    burgrNotifyQaFailed()
                    githubNotifyQaFailed()
                }
                aborted {
                    burgrNotifyQaAborted()
                    githubNotifyQaError()
                }
            }
            
        }
        stage('Promote') {
            steps {
                repoxPromoteBuild()
            }
            post {
                success {
                    burgrNotifyPromotePassed()
                    githubNotifyPromoteSuccess()
                }
                failure {
                    burgrNotifyPromoteFailed()
                    githubNotifyPromoteFailed()
                }
            }
        }
    }
}