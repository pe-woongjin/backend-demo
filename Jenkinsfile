
def VERSION         = "${BUILD_NUMBER}"
def BUNDLE_NAME     = "deploy-bundle-${BUILD_NUMBER}.zip"


// 
def ALB_ARN         = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:loadbalancer/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c"
def TARGET_GROUP    = "demo-apne2-dev-api"

// aws-autoscaling-group
def ASG_A_NAME      = "demo-apne2-dev-api-a-asg"
def ASG_B_NAME      = "demo-apne2-dev-api-b-asg"
def ASG_CAPACITY    = 1
def ASG_MIN         = 1

// aws-codedeploy
def CD_APP_NAME     = "demo-apne2-dev-api-cd"
def S3_BUCKET_NAME  = "opsflex-cicd-mgmt"
def S3_PATH         = "backend/${BUILD_NUMBER}"

pipeline {
  agent any
  
  stages {

    stage('Pre-Process') {

      steps {
        echo '* Backend-demo pipeline start.'
        echo "BUILD_NUMBER: ${BUILD_NUMBER}"
        echo "BUNDLE_NAME: ${BUNDLE_NAME}"
        echo "S3_PATH: ${S3_PATH}"
        echo "VERSION: ${VERSION}"        
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
            script {
              VERSION = "1.0.0-snapshot"
            }
          }
        }

        stage('Coverage') {
          steps {
            echo 'Unit test like JUnit'
            echo "VERSION: ${VERSION}"
          }
        }

      }
    }

    stage('Upload-Bundle') {
      steps {
        echo "build codedeploy bundle: ${BUILD_NUMBER}"
        sh """
rm -rf ./deploy-bundle
mkdir -p deploy-bundle/scripts
cp ./appspec.yml ./deploy-bundle
cp ./target/backend-demo.jar ./deploy-bundle/
cp -rf ./scripts ./deploy-bundle
cd ./deploy-bundle
zip -r ${BUNDLE_NAME} ./
"""
        s3Upload(bucket: "${S3_BUCKET_NAME}", file: "./deploy-bundle/${BUNDLE_NAME}", path: "${S3_PATH}/${BUNDLE_NAME}")
      }
    }

    stage('Discovery-ActiveTarget') {
      steps {
        echo "descovery active target-group for blue/green"
        
        sh"""
          aws elbv2 describe-target-groups --load-balancer-arn "${ALB_ARN}" \
              --query 'TargetGroups[?starts_with(TargetGroupName,`${TARGET_GROUP}`)==`true`].[TargetGroupName]' \
              --region ap-northeast-2 --output json  > TARGET_GROUP_NAME.json
        """
        script {
          def resultTgName = script {sh "cat TARGET_GROUP_NAME.json"}
          echo "${resultTgName}"
        }
      }
    }

    stage('Deploy') {
      steps {
        
        sh"""
        aws autoscaling update-auto-scaling-group --auto-scaling-group-name demo-apne2-dev-api-a-asg  \
            --desired-capacity ${ASG_CAPACITY} \
            --min-size ${ASG_MIN} \
            --region ap-northeast-2
        
        sleep 60
        """
        
        echo "Triggering codeDeploy: "
        sh"""
          aws deploy create-deployment \
              --s3-location bucket="${S3_BUCKET_NAME}",key=${S3_PATH}/${BUNDLE_NAME},bundleType=zip \
              --application-name "${CD_APP_NAME}" \
              --deployment-group-name "group-a" \
              --description "CodeDeploy triggered ${CD_APP_NAME}.group-a Bundle: backend/${BUNDLE_NAME}" \
              --region ap-northeast-2 --output json > DEPLOYMENT_ID.json
          """
        script {
            // def DEPLOYMENT_ID = readJSON file: './DEPLOYMENT_ID.json'
            // echo "DEPLOYMENT_ID: ${DEPLOYMENT_ID.deploymentId}"
          
          def aaa = script {sh "cat DEPLOYMENT_ID.json"}
          echo "${aaa}"
        }
      }
    }

    stage('Health-Check') {
      steps {
        echo 'health check target-group'
        echo 'waiting codedeploy processing...'
        // awaitDeploymentCompletion 'deploymentid-xxxx'
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
       script{
         """
        aws autoscaling update-auto-scaling-group --auto-scaling-group-name demo-apne2-dev-api-b-asg  \
            --desired-capacity 0 --min-size 0 --default-cooldown 90 \
            --region ap-northeast-2 
         """
       }
      }
    }

    stage('Post-Process') {
      steps {
        echo 'post-process'
      }
    }

  }
}
