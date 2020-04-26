import groovy.json.JsonSlurper

def APP_DOMAIN_NAME     = "demo-api-dev.mingming.shop"
def TARGET_GROUP_PRIFIX = "demo-apne2-dev-api"
def TARGET_RULE_ARN     = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:listener-rule/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c/d15a5636f3b71341/1b22b561377e078e"
def S3_BUCKET_NAME      = "opsflex-cicd-mgmt"
def S3_PATH             = "backend"
def BUNDLE_NAME         = "deploy-bundle-${BUILD_NUMBER}.zip"
def CODE_DEPLOY_NAME    = "demo-apne2-dev-api-cd"

def DEPLOY_GROUP_NAME   = ""
def DEPLOYMENT_ID       = ""
def ASG_DESIRED         = 1
def ASG_MIN             = 1
def CURR_ASG_NAME       = ""
def NEXT_ASG_NAME       = ""
def NEXT_TG_ARN         = ""
def ALB_ARN             = ""
def TG_RULE_ARN         = ""

@NonCPS
def toJson(String text) {
    def parser = new JsonSlurper()
    def json = parser.parseText (text)
    return json
}

def initVariables(def tgList) {
  tgList.each { tg ->
      String lbARN  = tg.LoadBalancerArns[0]
      String tgName = tg.TargetGroupName
      String tgARN  = tg.TargetGroupArn

      if( lbARN != null && lbARN.startsWith("arn:aws")) {
        env.ALB_ARN = lbARN
        if(tgName.startsWith("demo-apne2-dev-api-a")) {
          env.DEPLOY_GROUP_NAME = "group-b"
          env.CURR_ASG_NAME     = "demo-apne2-dev-api-a-asg"
          env.NEXT_ASG_NAME     = "demo-apne2-dev-api-b-asg"
        }
        else {
            env.DEPLOY_GROUP_NAME = "group-a"
            env.CURR_ASG_NAME     = "demo-apne2-dev-api-b-asg"
            env.NEXT_ASG_NAME     = "demo-apne2-dev-api-a-asg"
        }
      }
      else {
        env.NEXT_TG_ARN       = tgARN
        env.NEXT_TARGET_GROUP = tgName
      }
  }
}

def showVariables() {
  echo """
>   CURR_ASG_NAME:     ${env.CURR_ASG_NAME}
    NEXT_ASG_NAME:     ${env.NEXT_ASG_NAME}
    CODE_DEPLOY_NAME:  ${CODE_DEPLOY_NAME}
    DEPLOY_GROUP_NAME: ${env.DEPLOY_GROUP_NAME}
    BUNDLE_NAME:       ${BUNDLE_NAME}
    ALB_ARN:           ${env.ALB_ARN}
    NEXT_TG_ARN:       ${env.NEXT_TG_ARN}
    NEXT_TARGET_GROUP: ${env.NEXT_TARGET_GROUP}
    """
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
              aws elbv2 describe-target-groups \
                --query 'TargetGroups[?starts_with(TargetGroupName,`${TARGET_GROUP_PRIFIX}`)==`true`]' \
                --region ap-northeast-2 --output json > TARGET_GROUP_LIST.json
               """
            def textValue = readFile("TARGET_GROUP_LIST.json")
            def tgList = toJson(textValue)
            echo "Initialize Variables --------------------"
         }
      }

    }


    stage('Build') {

      steps {
        script {
          echo "Display variables --------------------"
          showVariables()
        }
        echo "Build backend-demo"
        sh "mvn clean package -Dmaven.test.skip=true"
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
        echo "Preparing Next Auto-Scaling-Group: ${env.NEXT_ASG_NAME}"

         sh"""
         aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${env.NEXT_ASG_NAME} \
             --desired-capacity ${ASG_DESIRED} \
             --min-size ${ASG_MIN} \
             --region ap-northeast-2
         """

         echo "Waiting boot-up ec2 instances: 2 mins."
         sh "sleep 120"
      }
    }


    stage('Deploy') {
      steps {
        echo "Triggering CodeDeploy "
        sh"""
          aws deploy create-deployment \
              --s3-location bucket="${S3_BUCKET_NAME}",key=${S3_PATH}/${BUNDLE_NAME},bundleType=zip \
              --application-name "${CODE_DEPLOY_NAME}" --deployment-group-name "${env.DEPLOY_GROUP_NAME}" \
              --region ap-northeast-2 --output json > DEPLOYMENT_ID.json

          sleep 1
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
        echo "DEPLOYMENT_ID ${env.DEPLOYMENT_ID}"
        sh"sleep 60"
        script {
          echo 'Waiting codedeploy processing...'
          // sh """awaitDeploymentCompletion '${env.DEPLOYMENT_ID}'"""
        }

      }
    }

    stage('Change LB-Routing') {

      steps {
        echo "Change load-balancer routing path"
        script {
          sh"""
          aws elbv2 modify-rule --rule-arn ${TARGET_RULE_ARN} \
              --conditions Field=host-header,Values=${APP_DOMAIN_NAME} \
              --actions Type=forward,TargetGroupArn=${env.NEXT_TG_ARN} \
              --region ap-northeast-2 --output json > CHANGED_LB_TARGET_GROUP.json

          cat ./CHANGED_LB_TARGET_GROUP.json
          """
        }
      }

    }

    stage('Stopping Blue Stage') {
      steps {
        echo "Stopping Blue Stage. Auto-Acaling-Group: ${env.CURR_ASG_NAME}"
        sh"sleep 30"
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
