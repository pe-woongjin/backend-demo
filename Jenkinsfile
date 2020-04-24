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

    stage('Inspection') {
      parallel {
        stage('Inspection') {
          steps {
            echo 'Execute Code-Inspection like sonarqube'
          }
        }

        stage('Coverage') {
          steps {
            echo 'Unit test like JUnit'
          }
        }

      }
    }

    stage('Upload-Bundle') {
      steps {
        echo 'build codedeploy bundle'
        sh '''mkdir deploy/scripts
cp appspec.yml deploy
cp target/backend-demo.jar ./deploy/
cp -rf scripts ./deploy
zip -r deploy.zip deploy'''
        s3Upload(bucket: 'opsflex-cicd-mgmt', file: 'deploy.zip', path: 'backend')
      }
    }

    stage('Discovery-ActiveTarget') {
      steps {
        echo 'descovery active target-group for blue/green'
      }
    }

    stage('Deploy') {
      steps {
        echo 'Triggering codeDeploy'
      }
    }

    stage('Health-Check') {
      steps {
        echo 'health check target-group'
      }
    }

    stage('Change Routing') {
      steps {
        echo 'change blue-green load-balancer routing path'
      }
    }

    stage('Stopping Blue instances') {
      steps {
        echo 'stop blue target-group instances.'
      }
    }

    stage('Post-Process') {
      steps {
        echo 'post-process'
      }
    }

  }
}