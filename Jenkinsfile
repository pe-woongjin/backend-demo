import groovy.json.JsonSlurper

def S3_BUCKET_NAME      = "opsflex-cicd-mgmt"
def S3_PATH             = "demo-api"
def BUNDLE_NAME         = "deploy-bundle-${BUILD_NUMBER}.zip"
def CODE_DEPLOY_NAME    = ""

def APP_DOMAIN_NAME     = ""
def TARGET_GROUP_PREFIX = ""
def ALB_LISTENER_ARN    = ""
def TARGET_RULE_ARN     = ""

def PROFILE             = ""
def AWS_PROFILE         = ""
def DEPLOY_GROUP_NAME   = ""
def DEPLOYMENT_ID       = ""
def ASG_DESIRED         = 0     /* 현재 ELB에 연결된 BLUE 스테이지의 엑티브 인스턴스 수로 Green 스테이지에 배포시 동일하게 생성 한다 */
def ASG_MIN             = 1
def VALID_TARGET_STAGE  = false /* 현재 ELB에 연결되지 않은 Target 스테이지(Green)는 인스턴스가 없어야 */
def CURR_ASG_NAME       = ""
def NEXT_ASG_NAME       = ""
def NEXT_TG_ARN         = ""
def ALB_ARN             = ""
def TG_RULE_ARN         = ""

@NonCPS
def toJson(String text) {
    def parser = new JsonSlurper()
    return parser.parseText( text )
}

def initEnvironments(String buildBranch) {
    echo "Build-Branch: ${buildBranch} -----"
    if(buildBranch == "release") {
      env.PROFILE             = "stg"
      env.AWS_PROFILE         = "--profile stage"
      env.APP_DOMAIN_NAME     = "demo-api-stg.mingming.shop"
      env.CODE_DEPLOY_NAME    = "demo-apne2-stg-api-cd"
      env.TARGET_GROUP_PREFIX = "demo-apne2-stg-api"
      env.ALB_LISTENER_ARN    = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:listener/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c/stg21323232321313"
    }
    else if(buildBranch == "master") {
      env.PROFILE             = "prd"
      env.AWS_PROFILE         = "--profile production"
      env.APP_DOMAIN_NAME     = "demo-api-prd.mingming.shop"
      env.CODE_DEPLOY_NAME    = "demo-apne2-prd-api-cd"
      env.TARGET_GROUP_PREFIX = "demo-apne2-stg-api"
      env.ALB_LISTENER_ARN    = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:listener/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c/prd23423424434343"
    }
    else {
      env.PROFILE             = "dev"
      env.AWS_PROFILE         = ""
      env.APP_DOMAIN_NAME     = "demo-api-dev.mingming.shop"
      env.CODE_DEPLOY_NAME    = "demo-apne2-dev-api-cd"
      env.TARGET_GROUP_PREFIX = "demo-apne2-dev-api"
      env.ALB_LISTENER_ARN    = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:listener/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c/d15a5636f3b71341"
    }
}

def initVariables(def tgList, String profile) {
    tgList.each { tg ->
        String lbARN  = tg.LoadBalancerArns[0]
        String tgName = tg.TargetGroupName
        String tgARN  = tg.TargetGroupArn

        if(lbARN != null && lbARN.startsWith("arn:aws")) {
            env.ALB_ARN = lbARN
            if(tgName.startsWith("demo-apne2-${profile}-api-a")) {
                env.DEPLOY_GROUP_NAME = "group-b"
                env.CURR_ASG_NAME     = "demo-apne2-${profile}-api-a-asg"
                env.NEXT_ASG_NAME     = "demo-apne2-${profile}-api-b-asg"
            } else {
                env.DEPLOY_GROUP_NAME = "group-a"
                env.CURR_ASG_NAME     = "demo-apne2-${profile}-api-b-asg"
                env.NEXT_ASG_NAME     = "demo-apne2-${profile}-api-a-asg"
            }
        } else {
            env.NEXT_TG_ARN       = tgARN
            env.NEXT_TARGET_GROUP = tgName
        }
    }
}

def discoveryTargetRuleArn() {
  return sh(
    script: """
    aws elbv2 describe-rules --listener-arn ${env.ALB_LISTENER_ARN} \
       --query 'Rules[].{RuleArn: RuleArn, Actions: Actions[?contains(TargetGroupArn,`${env.TARGET_GROUP_PREFIX}`)==`true`]}' \
       --region ap-northeast-2 ${env.AWS_PROFILE} \
       --output text | grep -B1 "ACTIONS"  | grep -v "ACTIONS"
       """,
    returnStdout: true).trim()
}

def discoveryTargetGroup() {
  sh"""
  aws elbv2 describe-target-groups \
  --query 'TargetGroups[?starts_with(TargetGroupName,`${TARGET_GROUP_PREFIX}`)==`true`]' \
  --region ap-northeast-2 ${env.AWS_PROFILE} \
  --output json > TARGET_GROUP_LIST.json
  cat ./TARGET_GROUP_LIST.json
  """
  return readFile("TARGET_GROUP_LIST.json")
}

def currentAsgActiveInstances() {
  return sh(script: """
     aws autoscaling describe-auto-scaling-groups \
     --query 'AutoScalingGroups[?starts_with(AutoScalingGroupName,`${env.CURR_ASG_NAME}`)==`true`].Instances[?LifecycleState==InService]' \
     --region ap-northeast-2 ${env.AWS_PROFILE} \
     --output text |grep InService | wc -l
    """, returnStdout: true).toInteger()
}

def checkAsgBootupInstances() {
    for (int i = 1; i <= 3; i++) {
        sh "sleep 10"
        echo "${i} - Wating for bootup instances"
        def cnt = sh(script: """
             aws autoscaling describe-auto-scaling-groups \
             --query 'AutoScalingGroups[?starts_with(AutoScalingGroupName,`${env.NEXT_ASG_NAME}`)==`true`].Instances[?LifecycleState==InService]' \
             --region ap-northeast-2 ${env.AWS_PROFILE} \
             --output text |grep InService | wc -l
            """, returnStdout: true).toInteger()
        if(cnt > 0 || i == 2) {
            break
        }        
    }
    return true
}

def validateTargetAutoScalingStage() {
  def validTargetStage = sh(script: """
       aws autoscaling describe-auto-scaling-instances --query 'AutoScalingInstances[?AutoScalingGroupName==`${env.NEXT_ASG_NAME}`].InstanceId' \
       --region ap-northeast-2 ${env.AWS_PROFILE} \
       --output text | wc -l
      """, returnStdout: true).toInteger()
  return (validTargetStage < 1 ? true : false)
}

def showVariables() {
  echo """
showVariables -----
CURR_ASG_NAME:       ${env.CURR_ASG_NAME}
NEXT_ASG_NAME:       ${env.NEXT_ASG_NAME}
DEPLOY_GROUP_NAME:   ${env.DEPLOY_GROUP_NAME}
ALB_ARN:             ${env.ALB_ARN}
NEXT_TG_ARN:         ${env.NEXT_TG_ARN}
NEXT_TARGET_GROUP:   ${env.NEXT_TARGET_GROUP}
ASG_DESIRED:         ${env.ASG_DESIRED}
VALID_TARGET_STAGE:  ${env.VALID_TARGET_STAGE}
   """
}

def validate() {
  echo "Validate -----"

  def valid = true;
  if(env.ALB_ARN == "") {
      error "ELB 를 찾을 수 없습니다. [AWS 관리 콘솔 > EC2 > 로드밸런서]를 확인 하세요."
  }
  if(env.NEXT_ASG_NAME == "") {
    error "배포될 타겟 AutoScalingGroup을 확인 할 수 없습니다. [AWS 관리 콘솔 > EC2 > Auto Scaling 그룹]을 확인 하세요. "
  }
  if(env.VALID_TARGET_STAGE == false) {
    error "배포될 타겟 AutoScalingGroup에 인스턴스가 존재 합니다. [AWS 관리 콘솔 > EC2 > Auto Scaling 그룹] '${env.NEXT_ASG_NAME}'에서 모든 인스턴스가 정리된 후 배포를 진행 하세요. "
  }
}

pipeline {
    agent any
    stages {
        stage('Pre-Process') {
            steps {
                script {
                    initEnvironments( "${GIT_BRANCH}" )

                    echo "Discovery Active Target Group -----"

                    def target_rule_arn = discoveryTargetRuleArn()
                    env.TARGET_RULE_ARN = target_rule_arn

                    def textValue = discoveryTargetGroup()
                    def tgList = toJson( textValue )

                    initVariables( tgList, env.PROFILE )
                }
            }
        }

        stage('Validate-Env') {
          steps {
            script {
              def desiredAsg  = currentAsgActiveInstances()
              env.ASG_DESIRED = (desiredAsg > 0 ? desiredAsg : 1)
              env.VALID_TARGET_STAGE = validateTargetAutoScalingStage()
              showVariables()
              validate()

            }
          }
        }

/*
        stage('Build') {
            steps {
                script {
                    echo "----- [Build] showVariables -----"
                    showVariables()

                    echo "----- [Build] Build demo-api -----"
                    sh "mvn clean package -Dmaven.test.skip=true"
                }
            }
        }

        stage('Inspection') {
            parallel {
                stage('Inspection') {
                    steps {
                        echo "----- [Inspection] Execute Code-Inspection like sonarqube -----"
                    }
                }

                stage('Coverage') {
                    steps {
                        echo "----- [Coverage] Unit test like JUnit -----"
                    }
                }
            }
        }

        stage('Upload-Bundle') {
            steps {
                script {
                    echo "----- [Upload] Uploading Bundle '${BUNDLE_NAME}' to '${S3_PATH}/${BUNDLE_NAME}' -----"
                    sh """
                    rm -rf ./deploy-bundle
                    mkdir -p deploy-bundle/scripts
                    cp ./appspec.yml ./deploy-bundle
                    cp ./target/demo-api.jar ./deploy-bundle/
                    cp -rf ./src/main/resources/scripts ./deploy-bundle
                    cd ./deploy-bundle
                    zip -r ${BUNDLE_NAME} ./
                    """
                }
                s3Upload(bucket: "${S3_BUCKET_NAME}", file: "./deploy-bundle/${BUNDLE_NAME}", path: "${S3_PATH}/${BUNDLE_NAME}")
            }
        }
*/

        stage('Preparing Auto-Scale Stage') {
            /*
            steps {
                script {
                    echo "----- [Auto-Scale] Preparing Next Auto-Scaling-Group: ${env.NEXT_ASG_NAME} -----"

                    sh"""
                    aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${env.NEXT_ASG_NAME} \
                    --desired-capacity ${env.ASG_DESIRED} \
                    --min-size ${ASG_MIN} \
                    --region ap-northeast-2
                    """

                    echo "----- [Auto-Scale] Waiting boot-up ec2 instances: 80 secs. -----"
                    sh "sleep 80"
                }
            }
            */
            
            steps {
                script {
                    waitUntil {
                        try {
                            checkAsgBootupInstances()
                        } catch(e) {
                            throw e                            
                        }                       
                    }
                    echo "boot-up ec2 instances ======="
                }
                
            }
        }        
        
/*
        stage('Deploy') {
            steps {
                script {
                    echo "----- [Deploy] Triggering CodeDeploy -----"
                    sh"""
                    aws deploy create-deployment \
                    --s3-location bucket="${S3_BUCKET_NAME}",key=${S3_PATH}/${BUNDLE_NAME},bundleType=zip \
                    --application-name "${CODE_DEPLOY_NAME}" --deployment-group-name "${env.DEPLOY_GROUP_NAME}" \
                    --region ap-northeast-2 --output json > DEPLOYMENT_ID.json
                    """

                    def textValue = readFile("DEPLOYMENT_ID.json")
                    def jsonDI =toJson(textValue)
                    env.DEPLOYMENT_ID = "${jsonDI.deploymentId}"
                }
            }
        }

        stage('Health-Check') {
            steps {
                echo "----- [Health-Check] DEPLOYMENT_ID ${env.DEPLOYMENT_ID} -----"
                echo "----- [Health-Check] Waiting codedeploy processing -----"
                timeout(time: 3, unit: 'MINUTES'){                                         
                  awaitDeploymentCompletion("${env.DEPLOYMENT_ID}")
                }
            }
        }

        stage('Change LB-Routing') {
            steps {
                script {
                    echo "----- [LB] Change load-balancer routing path -----"
                    sh"""
                    aws elbv2 modify-rule --rule-arn ${env.TARGET_RULE_ARN} \
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
                script{
                    echo "----- [Stopping Blue] Stopping Blue Stage. Auto-Acaling-Group: ${env.CURR_ASG_NAME} -----"
                    sh"sleep 30"
                    sh"""
                    aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${env.CURR_ASG_NAME}  \
                    --desired-capacity 0 --min-size 0 \
                    --region ap-northeast-2
                    """
                }
            }
        }
*/
        stage('Post-Process') {
            steps {
                echo "----- [Post-Process] post-process -----"
            }
        }
    }
}
