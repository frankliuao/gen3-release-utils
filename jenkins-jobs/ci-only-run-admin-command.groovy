/*
  String parameter TARGET_ENVIRONMENT
    e.g., qa-anvil
  String parameter COMMAND
    e.g., gen3 secrets decode portal-config gitops.json | jq '.discoveryConfig.minimalFieldMapping.uid'
*/
pipeline {
    agent {
      node {
        label 'gen3-qa-worker'
      }
    }
    stages {
        stage('Clean old workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Initial setup') {
            steps {
                // cloud-automation
                checkout([
                  $class: 'GitSCM',
                  branches: [[name: 'refs/heads/master']],
                  doGenerateSubmoduleConfigurations: false,
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'cloud-automation']],
                  submoduleCfg: [],
                  userRemoteConfigs: [[credentialsId: 'PlanXCyborgUserJenkins', url: 'https://github.com/uc-cdis/cloud-automation.git']]
                ])
                sh '''#!/bin/bash +x
                    export GEN3_HOME=\$WORKSPACE/cloud-automation
                    export KUBECTL_NAMESPACE=\${TARGET_ENVIRONMENT}
                    source $GEN3_HOME/gen3/gen3setup.sh
                '''
            }
        }
        stage('Run command on adminvm') {
            steps {
                dir("run-command") {
                    script {
                        println env.COMMAND
                        def RESULT = sh (
                            script: "\${COMMAND}",
                            returnStdout: true
                        ).trim()
                        println RESULT
                    }
                }
            }
        }
    }
}