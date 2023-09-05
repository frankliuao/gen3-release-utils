/*
    String parameter NAMESPACE
        e.g., jenkins-brain
    String parameter REPO
        e.g., gitops-qa
    String parameter BRANCH
        e.g. chore/update_portal_qabrh

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
        stage('Unlock namespace') {
            steps {
                dir("unlock-namespace") {
                    script {
                        sh '''#!/bin/bash +x
                            export GEN3_HOME=\$WORKSPACE/cloud-automation
                            source \$GEN3_HOME/gen3/gen3setup.sh
                            echo "attempting to unlock namespace $NAMESPACE"
                            export KUBECTL_NAMESPACE="$NAMESPACE"
                            klockResult=$(bash "$GEN3_HOME/gen3/bin/klock.sh" "unlock" "jenkins" "$REPO_$BRANCH")
                            if [[ $klockResult -eq 0 ]]; then
                                echo "namespace $kubectlNamespace"
                                echo "$kubectlNamespace" > namespace.txt
                                return 0
                            else
                                # Unable to unlock namespace
                                echo "no available workspace, yet..."
                            fi
                        '''
                    }
                }
            }
        }
    }
}
