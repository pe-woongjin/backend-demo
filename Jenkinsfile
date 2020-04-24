pipeline {
  agent any
  stages {
    stage('Pre-Process') {
      steps {
        echo '* Backend-demo pipeline start.'
      }
    }

    stage('Build') {
      steps {
        echo '* Backend-demo build.'
        sh 'mvn package -Dspring.profiles.active=dev'
      }
    }

    stage('Post-Process') {
      steps {
        echo '* Backend-demo jar upload to S3.'
      }
    }

  }
}