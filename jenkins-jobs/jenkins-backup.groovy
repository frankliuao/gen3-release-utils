/*
  Runs at 1 AM UTC every day
  Schedule: H H(0-5) * * *
  Would last have run at Thursday, March 18, 2021 1:06:49 AM UTC; would next run at Friday, March 19, 2021 1:06:49 AM UTC.
*/
#!groovy

pipeline {
  agent any

  stages {
    stage('BuildArchive'){
      steps {
        echo "BuildArchive $env.JENKINS_HOME"
        sh "tar cvJf backup.tar.xz --exclude '$env.JENKINS_HOME/jobs/[^/]*/builds/*' --exclude '$env.JENKINS_HOME/jobs/[^/]*/last*' --exclude '$env.JENKINS_HOME/workspace' --exclude '$env.JENKINS_HOME/war' --exclude '$env.JENKINS_HOME/logs' --exclude '$env.JENKINS_HOME/queue.xml' --exclude '$env.JENKINS_HOME/org.jenkinsci.plugins.workflow.flow.FlowExecutionList.xml' --exclude '$env.JENKINS_HOME/nodes' --exclude '$env.JENKINS_HOME/jobs/[^/]*/workspace/'  $env.JENKINS_HOME || [ -f backup.tar.xz ]"
      }
    }
    stage('UploadToS3') {
      steps {
        echo 'Upload to S3!'
        sh 'aws s3 cp --sse AES256 backup.tar.xz s3://cdis-terraform-state/JenkinsBackup/backup.$(date +%u).tar.xz'
      }
    }
    stage('Cleanup') {
      steps {
        echo 'Cleanup!'
        sh 'rm -f backup.tar.xz'
      }
    }
  }
  post {
    success {
      echo 'Jenkins backup pipeline succeeded'
    }
    failure {
      slackSend color: 'bad', message: 'Jenkins backup pipeline failed'
    }
    unstable {
      slackSend color: 'bad', message: 'Jenkins backup pipeline unstable'
    }
  }
}
