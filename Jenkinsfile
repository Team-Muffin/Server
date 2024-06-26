void setBuildStatus(String message, String state) {
  step([
    $class: "GitHubCommitStatusSetter",
    reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/Team-Muffin/Server.git"],
    contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
    errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
    statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
  ])
}

def userApp = false
def boardApp = false
def challengeApp = false
def productApp = false
def creditApp = false
def utils = false
def failureCnt = 0

pipeline {
  agent any
  post {
    failure {
      setBuildStatus("Build failed", "FAILURE")
      slackSend (
        channel: 'C078D2K42MD',
        color: '#FF0000',
        message: "FAIL: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
      )
    }
    success {
      setBuildStatus("Build successful", "SUCCESS")
      slackSend (
        channel: 'C078D2K42MD',
        color: '#00FF00',
        message: "SUCCESS: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
      )
    }
  }

  stages {
    stage('init') {
      steps {
        echo 'init pipeline, check changes'
        script {
          def buildCause = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')
          echo "manually started by ${buildCause}"
          if (!buildCause.isEmpty()) {
            echo "triggered by user"
            userApp = true
            boardApp = true
            challengeApp = true
            productApp = true
            creditApp = true
            alertApp = true
          } else {
            def changedFiles = sh(returnStdout: true, script: 'git diff --name-only --diff-filter=ACMRT HEAD^ HEAD').trim().split('\n')
            def changedDirs = new HashSet()
            echo "files changed : ${changedFiles}"
            for (def file : changedFiles) {
              echo "new file : ${file}"
              if (file.startsWith('utils/')) {
                utils = true
                break
              } else if (file.startsWith('applications/')) {
                def dir = file.split('/')[1]
                changedDirs.add(dir)
                echo "modified : ${dir}"
              } else {
                changedDirs.add(file)
              }
            }
            userApp = changedDirs.contains('user-application')
            boardApp = changedDirs.contains('board-application')
            challengeApp = changedDirs.contains('challenge-application')
            productApp = changedDirs.contains('product-application')
            creditApp = changedDirs.contains('credit-application')
            alertApp = changedDirs.contains('alert-application')
          }
        }
        sh 'cp /var/jenkins_home/workspace/configs/server/s3-keys.properties ./utils/s3-utils/src/main/resources/application-keys.properties'
        echo "utils : ${utils}, user : ${userApp}, board : ${boardApp}, challenge : ${challengeApp}, product : ${productApp}, credit : ${creditApp}, alert: ${alertApp}"
      }
    }

    stage('build user app') {
      when {
        anyOf {
          expression { userApp }
          expression { utils }
        }
      }
      steps {
        echo 'copy configuration files for user app'
        sh 'cp /var/jenkins_home/workspace/configs/server/user/application.yml ./applications/user-application/src/main/resources/application.yml'
        echo 'start gradle build for user app'
        dir('./') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew :applications:user-application:build'
        }
        echo 'start docker build for user app'
        dir('./applications/user-application/') {
          sh 'docker build -t bkkmw/tofin-user-api .'
          sh 'docker push bkkmw/tofin-user-api'
        }
        echo 'publish over ssh for user app'
        script {
          try {
            publishOverSSH('user-api', 'tofin-user-api')
            echo "Publish over ssh Successful"
          } catch(Exception e) {
            echo "Publish over ssh failed : ${e.message}"
            currentBuild.result = 'FAILURE'
            // stop on failure
            failFast true
          }
        }
      }
    }

    stage('build board app') {
      when {
        anyOf {
          expression { boardApp }
          expression { utils }
        }
      }
      steps {
        echo 'copy configuration files for board app'
        sh 'cp /var/jenkins_home/workspace/configs/server/board/application.yml ./applications/board-application/src/main/resources/application.yml'
        echo 'start gradle build for board app'
        dir('./') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew :applications:board-application:build'
        }
        echo 'start docker build for board app'
        dir('./applications/board-application/') {
          sh 'docker build -t bkkmw/tofin-board-api .'
          sh 'docker push bkkmw/tofin-board-api'
        }
        echo 'publish over ssh for board app'
        script {
          try {
            publishOverSSH('board-api', 'tofin-board-api')
            echo "Publish over ssh Successful"
          } catch(Exception e) {
            echo "Publish over ssh failed : ${e.message}"
            currentBuild.result = 'FAILURE'
            // stop on failure
            failFast true
          }
        }

      }
    }

    stage('build product app') {
      when {
        anyOf {
          expression { productApp }
          expression { utils }
        }
      }
      steps {
        echo 'copy configuration files for board app'
        sh 'cp /var/jenkins_home/workspace/configs/server/product/application.yml ./applications/product-application/src/main/resources/application.yml'
        echo 'start gradle build for product app'
        dir('./') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew :applications:product-application:build'
        }
        echo 'start docker build for product app'
        dir('./applications/product-application/') {
          sh 'docker build -t bkkmw/tofin-prod-api .'
          sh 'docker push bkkmw/tofin-prod-api'
        }
        echo 'publish over ssh for product app'
        script {
          try {
            publishOverSSH('prod-api', 'tofin-prod-api')
            echo "Publish over ssh Successful"
          } catch(Exception e) {
            echo "Publish over ssh failed : ${e.message}"
            currentBuild.result = 'FAILURE'
            // stop on failure
            failFast true
          }
        }

      }
    }

    stage('build challenge app') {
      when {
        anyOf {
          expression { challengeApp }
          expression { utils }
        }
      }
      steps {
        echo 'copy configuration files for challenge app'
        sh 'cp /var/jenkins_home/workspace/configs/server/challenge/application.yml ./applications/challenge-application/src/main/resources/application.yml'
        echo 'start gradle build for challenge app'
        dir('./') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew :applications:challenge-application:build'
        }
        echo 'start docker build for challenge app'
        dir('./applications/challenge-application/') {
          sh 'docker build -t bkkmw/tofin-challenge-api .'
          sh 'docker push bkkmw/tofin-challenge-api'
        }
        echo 'publish over ssh for challenge app'
        script {
          try {
            publishOverSSH('challenge-api', 'tofin-challenge-api')
            echo "Publish over ssh Successful"
          } catch(Exception e) {
            echo "Publish over ssh failed : ${e.message}"
            currentBuild.result = 'FAILURE'
          }
        }

      }
    }

    stage('build credit app') {
      when {
        anyOf {
          expression { creditApp }
          expression { utils }
        }
      }
      steps {
        echo 'copy configuration files for credit app'
        sh 'cp /var/jenkins_home/workspace/configs/server/credit/application.yml ./applications/credit-application/src/main/resources/application.yml'
        echo 'start gradle build for credit app'
        dir('./') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew :applications:credit-application:build'
        }
        echo 'start docker build for credit app'
        dir('./applications/credit-application/') {
          sh 'docker build -t bkkmw/tofin-credit-api .'
          sh 'docker push bkkmw/tofin-credit-api'
        }
        echo 'publish over ssh for credit app'
        script {
          try {
            publishOverSSH('credit-api', 'tofin-credit-api')
            echo "Publish over ssh Successful"
          } catch(Exception e) {
            echo "Publish over ssh failed : ${e.message}"
            currentBuild.result = 'FAILURE'
          }
        }

      }
    }

    stage('build alert app') {
      when {
        anyOf {
          expression { alertApp }
          expression { utils }
        }
      }
      steps {
        echo 'copy configuration files for alert app'
        sh 'cp /var/jenkins_home/workspace/configs/server/alert/application.yml ./applications/alert-application/src/main/resources/application.yml'
        echo 'start gradle build for alert app'
        dir('./') {
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew :applications:alert-application:build'
        }
        echo 'start docker build for alert app'
        dir('./applications/alert-application/') {
          sh 'docker build -t bkkmw/tofin-alert-api .'
          sh 'docker push bkkmw/tofin-alert-api'
        }
        echo 'publish over ssh for alert app'
        script {
          try {
            publishOverSSH('alert-api', 'tofin-alert-api')
            echo "Publish over ssh Successful"
          } catch(Exception e) {
            echo "Publish over ssh failed : ${e.message}"
            currentBuild.result = 'FAILURE'
          }
        }

      }
    }

    stage('clean') {
      steps {
        echo 'clean unused image'
        sh 'docker image prune --force'
      }
    }
  }
}

def publishOverSSH(serverName, imageName) {
  sshPublisher(
    failOnError: true,
    publishers: [
      sshPublisherDesc(
        configName: serverName, // SSH server name
        verbose: true,
        transfers: [
          sshTransfer(
            cleanRemote: true, // clean remote dir
            excludes: '',
            execCommand: "/bin/bash /home/ubuntu/depl/deploy.sh ${imageName}",
            execTimeout: 200000,
            makeEmptyDirs: true,
            noDefaultExcludes: false,
            remoteDirectory: 'depl',
            remoteDirectorySDF: false,
            removePrefix: 'scripts',
            sourceFiles: 'scripts/deploy.sh'
          )
        ]
      )
    ]
  )
}