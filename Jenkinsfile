pipeline {
  agent any

  environment {
    SBT_HOME="${tool 'sbt'}"
    PATH="${env.SBT_HOME}/bin:${env.PATH}"
  }

  stages {
    stage('Build') {
      steps {
        sh "sbt compile"
      }

      post {
        failure {
          sh "exit 1"
        }
      }
    }

    stage('Test') {
      steps {
        sh "sbt test"
      }

      post {
        failure {
          sh "exit 1"
        }
      }
    }

    stage('Scala Style') {
      steps {
        sh "sbt scalastyle"
      }

      post {
        failure {
          sh "exit 1"
        }
      }
    }
  }
}