environment {
  S3_BUCKET = "opsflex-cicd-mgmt"
  S3_PATH = "backend"
  VERSION = "${env.BUILD_NUMBER}"
  BUNDLE_NAME = "deploy-bundle-${env.BUILD_NUMBER}.zip"
}


pipeline {
  agent any

  environment {
    VERSION = "2020"
    deployment_target = "demo-api-group-a"
    deploymentid      = "deploymentid-xxxxx"
  }

  stages {
    stage('Pre-Process') {
      steps {
        echo '* Backend-demo pipeline start.'
        echo 'BUILD_NUMBER: ${env.BUILD_NUMBER}'
        echo 'BUNDLE_NAME: ${env.BUNDLE_NAME}'
        echo 'deployment_target: ${env.deployment_target}'
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
zip -r "${env.BUNDLE_NAME}" deploy-bundle
'''
        s3Upload(bucket: 'opsflex-cicd-mgmt', file: '${env.BUNDLE_NAME}', path: 'backend')
      }
    }

    stage('Discovery-ActiveTarget') {
      steps {
        echo 'descovery active target-group for blue/green'
      }
    }

    stage('Deploy') {
      steps {
        echo 'Triggering codeDeploy ${env.deployment_target}'
        sh '''
      aws deploy create-deployment --application-name "demo-apne2-api-codedeploy" --deployment-group-name "demo-api-group-a" \
      --description "deploy backend-demo" \
      --s3-location bucket="demo-apne2-cicd-mgmt",key=backend/deploy-bundle.zip,bundleType=zip \
      --region ap-northeast-2 --output json > DEPLOYMENT_ID.json
      '''
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
