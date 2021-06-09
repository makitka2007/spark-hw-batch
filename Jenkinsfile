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

      publishHTML (target: [
          allowMissing: false,
          alwaysLinkToLastBuild: false,
          keepAll: true,
          reportDir: 'target',
          reportFiles: 'scalastyle-result.xml',
          reportName: "Scala style report"
        ])
    }
  }
}