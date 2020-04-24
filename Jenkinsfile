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
        sh '''aws s3 cp ./target/backend-demo-1.0.0-SNAPSHOT.jar s3://ming2-bucket/backend-demo.jar --region ap-northeast-2
'''
      }
    }

  }
}