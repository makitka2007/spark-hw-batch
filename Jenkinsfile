pipeline {
  agent any

  environment {
    SBT_HOME="${tool 'sbt'}"
    PATH="${env.SBT_HOME}/bin:${env.PATH}"
  }

  stages {
    stage('Compile') {
      steps {
        sh "sbt compile"
      }
    }
  }
}