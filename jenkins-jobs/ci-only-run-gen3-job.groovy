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
            }
        }
        stage('Run Gen3 job') {
            steps {
                dir("run-gen3-job") {
                    script {
                        sh '''#!/bin/bash +x
                            set -e
                            export GEN3_HOME=\$WORKSPACE/cloud-automation
                            export KUBECTL_NAMESPACE=\${TARGET_ENVIRONMENT}
                            source $GEN3_HOME/gen3/gen3setup.sh
                            gen3 kube-setup-secrets
                            if [ $GEN3_ROLL_ALL == "true" ]; then
                                gen3 roll all
                            fi
                            gen3 job run \${JOB_NAME}
                            g3kubectl wait --for=condition=complete --timeout=-1s jobs/\${JOB_NAME}
                            gen3 job logs \${JOB_NAME}
                            echo "done"
                        '''
                    }
                }
            }
        }
    }
}