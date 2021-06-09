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
    }

    stage('Test') {
      steps {
        sh "sbt test"
      }
    }

    stage('Scala Style') {
      steps {
        sh "sbt scalastyle"
      }
    }
  }
}