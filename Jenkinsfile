pipeline {
  agent { label 'master' }
  environment {
    gitRepository = 'ssh://git@bitbucket.org/concentric-sky/osmt-core.git'
    projectName = 'osmt'
    dockerhubCredentials = credentials('dockerhub')
  }

  stages {
    stage('Checkout') {
      steps {
        // Cleaning build context
        deleteDir()

        // Checkout the osmt repo
        checkout([$class: 'GitSCM',
                  branches: [[name: env.BRANCH_NAME]],
                  doGenerateSubmoduleConfigurations: false,
                  submoduleCfg: [],
                  userRemoteConfigs: [[credentialsId: 'jenkins',
                  url:  env.gitRepository]]])
      }
    }
    stage('Setup') {
      steps {
        script {
          gitHash = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%H'").trim()
          hasTag = sh(returnStatus: true, script: "git describe  --exact-match --tags ${gitHash}")
          if (hasTag == 0) {
            gitLabel = sh(returnStdout: true, script: "git describe --exact-match --tags ${gitHash}").trim()
          } else {
            gitLabel = sh(returnStdout: true, script: "git log -n 1 --pretty=format:'%h'").trim()
          }
          echo gitLabel
        }
      }
    }
    stage('Build') {
      steps {
        sh """
          set +x
          docker login --username ${dockerhubCredentials_USR} --password ${dockerhubCredentials_PSW}
          docker build . \
            -t concentricsky/${projectName}:${gitLabel}
        """
      }
    }
    stage('Publish'){
      steps {
        sh """
            set +x
            docker login --username ${dockerhubCredentials_USR} --password ${dockerhubCredentials_PSW}
            set -x
            docker tag concentricsky/${projectName}:${gitLabel} concentricsky/${projectName}:latest
            docker push concentricsky/${projectName}:${gitLabel}
            docker push concentricsky/${projectName}:latest
        """
      }
    }
  }
  post {
    always {
      sh """
      docker rmi concentricsky/${projectName}:latest
      docker rmi concentricsky/${projectName}:${gitLabel}
      """
    }
  }
}