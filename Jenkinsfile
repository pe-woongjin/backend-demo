import groovy.json.JsonSlurper

def ALB_ARN             = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:loadbalancer/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c"
def TARGET_GROUP_PRIFIX = "demo-apne2-dev-api"

def S3_BUCKET_NAME      = "opsflex-cicd-mgmt"
def S3_PATH             = "backend"
def BUNDLE_NAME         = "deploy-bundle-${BUILD_NUMBER}.zip"

def CURR_ASG_NAME       = ""
def NEXT_ASG_NAME       = ""
def ASG_DESIERD         = 1
def ASG_MIN             = 1
def CURR_TARGET_GROUP   = ""
def NEXT_TARGET_GROUP   = ""

// aws-codedeploy
def CD_APP_NAME         = "demo-apne2-dev-api-cd"
def CD_DG_NAME          = ""
def DEPLOYMENT_ID       = ""

@NonCPS
def toJson(String text) {
    def jsonSlurper = new JsonSlurper()
    def json = jsonSlurper.parseText (text)
    return json
}

def initVariables(String tgVal) {
  if("demo-apne2-dev-api-a-tg8080" == tgVal) {
    env.CURR_ASG_NAME     = "demo-apne2-dev-api-a-asg"
    env.NEXT_ASG_NAME     = "demo-apne2-dev-api-b-asg"
    env.NEXT_TARGET_GROUP = "demo-apne2-dev-api-b-tg8080"
    env.CD_DG_NAME        = "group-b"
  }
  else {
    env.CURR_ASG_NAME     = "demo-apne2-dev-api-b-asg"
    env.NEXT_ASG_NAME     = "demo-apne2-dev-api-a-asg"
    env.NEXT_TARGET_GROUP = "demo-apne2-dev-api-a-tg8080"
    env.CD_DG_NAME        = "group-a"
  }
}


def showVariables() {   
  echo """
CURR_ASG_NAME:     ${env.CURR_ASG_NAME}
NEXT_ASG_NAME:     ${env.NEXT_ASG_NAME}
NEXT_TARGET_GROUP: ${env.NEXT_TARGET_GROUP}
CD_DG_NAME:        ${env.CD_DG_NAME}"""

}

pipeline {
  agent any

  stages {

    stage('Pre-Process') {

      steps {
         echo "Preparing ..."
         script {
            showVariables()
            echo "Discovery Active Target-Group ---------------------"
            sh """
              aws elbv2 describe-target-groups --load-balancer-arn "${ALB_ARN}" \
                --query 'TargetGroups[?starts_with(TargetGroupName,`${TARGET_GROUP_PRIFIX}`)==`true`].[TargetGroupName]' \
                --region ap-northeast-2 --output json > TARGET_GROUP_NAME.json
               """
            def textValue = readFile("TARGET_GROUP_NAME.json")
            def jsonTG =toJson(textValue)
            def tgVal = "${jsonTG[0][0]}"
            echo "Initialize & Display variables --------------------"
            initVariables(tgVal)
            showVariables()
         }
      }

    }


    stage('Build') {

      steps {
        echo "Build backend-demo"
        sh "mvn clean package -Dmaven.test.skip=true"

        script {
          echo "Display variables --------------------"
          showVariables()
        }
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
        echo "Uploading Bundle '${BUNDLE_NAME}' to '${S3_PATH}/${BUNDLE_NAME}'"
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


    stage('Preparing Auto-Scale Stage') {

      steps {

         sh"""
         aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${env.NEXT_ASG_NAME} \
             --desired-capacity ${ASG_CAPACITY} \
             --min-size ${ASG_MIN} \
             --region ap-northeast-2
         """

         echo "Waiting boot-up ec2 instances: 1 mins."
         sh "sleep 60"
      }
    }


    stage('Deploy') {
      steps {
        
        echo "Triggering codeDeploy "
        sh"""
          aws deploy create-deployment \
              --s3-location bucket="${S3_BUCKET_NAME}",key=${S3_PATH}/${BUNDLE_NAME},bundleType=zip \
              --application-name "${CD_APP_NAME}" --deployment-group-name "${env.CD_DG_NAME}" \
              --region ap-northeast-2 --output json > DEPLOYMENT_ID.json
          """
        script {
          def textValue = readFile("DEPLOYMENT_ID.json")
          def jsonDI =toJson(textValue)
          env.DEPLOYMENT_ID = "${jsonDI.deploymentId}"
        }
      }
    }

    stage('Health-Check') {
      steps {
        echo 'health check target-group'
        script {
          echo 'Waiting codedeploy processing...'
          sh """
          awaitDeploymentCompletion "${env.DEPLOYMENT_ID}"
          """
        }

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
          sh"""
          aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${env.CURR_ASG_NAME}  \
              --desired-capacity 0 --min-size 0 --default-cooldown 120 \
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
