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
        echo 'Build backend-demo'
        sh 'mvn clean package -Dmaven.test.skip=true'
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
        sh '''
mkdir -p deploy-bundle/scripts
cp appspec.yml ./deploy-bundle
cp target/backend-demo.jar ./deploy-bundle/
cp -rf scripts ./deploy-bundle
NOW=`date "+%Y%m%d-%H%M%S"`
echo "$NOW"
zip -r deploy-bundle.zip deploy-bundle
'''
        s3Upload(bucket: 'opsflex-cicd-mgmt', file: 'deploy-bundle.zip', path: 'backend')
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
        echo 'waiting codedeploy processing...'
        awaitDeploymentCompletion 'deploymentid-xxxx'
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