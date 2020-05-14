import groovy.json.JsonSlurper

def APP_DOMAIN_NAME     = "demo-api-dev.mingming.shop"
def TARGET_GROUP_PREFIX = "demo-apne2-dev-api"

def ALB_LISTENER_ARN    = "arn:aws:elasticloadbalancing:ap-northeast-2:144149479695:listener/app/comp-apne2-prod-mgmt-alb/d76ec25af38db29c/d15a5636f3b71341"
def TARGET_RULE_ARN     = ""
def S3_BUCKET_NAME      = "opsflex-cicd-mgmt"
def S3_PATH             = "demo-api"
def BUNDLE_NAME         = "deploy-bundle-${BUILD_NUMBER}.zip"
def CODE_DEPLOY_NAME    = "demo-apne2-dev-api-cd"

def DEPLOY_GROUP_NAME   = ""
def DEPLOYMENT_ID       = ""
def ASG_DESIRED         = 0     /* 현재 ELB에 연결된 BLUE 스테이지의 엑티브 인스턴스 수로 Green 스테이지에 배포시 동일하게 생성 한다 */
def ASG_MIN             = 1
def STAGED_ACTIVE_CNT   = 0     /* 현재 ELB에 연결되지 않은 Green 스테이지의 인스턴스 수로 배포를 위해선 반드시 0 이여야 한다. */
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

def initVariables(def tgList) {
    tgList.each { tg ->
        String lbARN  = tg.LoadBalancerArns[0]
        String tgName = tg.TargetGroupName
        String tgARN  = tg.TargetGroupArn

        if(lbARN != null && lbARN.startsWith("arn:aws")) {
            env.ALB_ARN = lbARN
            if(tgName.startsWith("demo-apne2-dev-api-a")) {
                env.DEPLOY_GROUP_NAME = "group-b"
                env.CURR_ASG_NAME     = "demo-apne2-dev-api-a-asg"
                env.NEXT_ASG_NAME     = "demo-apne2-dev-api-b-asg"
            } else {
                env.DEPLOY_GROUP_NAME = "group-a"
                env.CURR_ASG_NAME     = "demo-apne2-dev-api-b-asg"
                env.NEXT_ASG_NAME     = "demo-apne2-dev-api-a-asg"
            }
        } else {
            env.NEXT_TG_ARN       = tgARN
            env.NEXT_TARGET_GROUP = tgName
        }
    }
}

def discoveryTargetRuleArn(def listenerARN, def tgPrefix) {
  script {
    return sh(
      script: """aws elbv2 describe-rules --listener-arn ${listenerARN} \
                   --query 'Rules[].{RuleArn: RuleArn, Actions: Actions[?contains(TargetGroupArn,`${tgPrefix}`)==`true`]}' \
                   --region ap-northeast-2 \
                   --output text | grep -B1 "ACTIONS"  | grep -v  "ACTIONS"   """, 
      returnStdout: true).trim()
    }
}

def showVariables() {
  echo """
>   CURR_ASG_NAME:       ${env.CURR_ASG_NAME}
    NEXT_ASG_NAME:       ${env.NEXT_ASG_NAME}
    DEPLOY_GROUP_NAME:   ${env.DEPLOY_GROUP_NAME}
    ALB_ARN:             ${env.ALB_ARN}
    NEXT_TG_ARN:         ${env.NEXT_TG_ARN}
    NEXT_TARGET_GROUP:   ${env.NEXT_TARGET_GROUP}
    ASG_DESIRED:         ${env.ASG_DESIRED}
    STAGED_ACTIVE_CNT:   ${env.STAGED_ACTIVE_CNT}
    """
}

pipeline {
    agent any
    stages {
        stage('Pre-Process') {
            steps {
                script {
                    echo """BRANCH: ${GIT_BRANCH}
----- [Pre-Process] Discovery Active Target Group -----"""

                    def target_rule_arn = discoveryTargetRuleArn( ALB_LISTENER_ARN, TARGET_GROUP_PREFIX )
                    env.TARGET_RULE_ARN = target_rule_arn

                    sh"""
                    aws elbv2 describe-target-groups \
                    --query 'TargetGroups[?starts_with(TargetGroupName,`${TARGET_GROUP_PREFIX}`)==`true`]' \
                    --region ap-northeast-2 --output json > TARGET_GROUP_LIST.json

                    cat ./TARGET_GROUP_LIST.json
                    """

                    def textValue = readFile("TARGET_GROUP_LIST.json")
                    def tgList = toJson( textValue )
                    echo "----- [Pre-Process] Initialize Variables -----"
                    initVariables( tgList )
                    
                    env.ASG_DESIRED = 0
                    
                    /*
                    
                    def desiredCnt = sh(script: """aws autoscaling describe-auto-scaling-instances --query 'AutoScalingInstances[?starts_with(AutoScalingGroupName,`${env.CURR_ASG_NAME}`)==`true`]' \
                     --query 'AutoScalingInstances[?LifecycleState==`InService`].InstanceId' \
                     --region ap-northeast-2 \
                     --output json | wc -l   """,  returnStdout: true)
                    
                    env.ASG_DESIRED = (desiredCnt < 1 ? 1 : desiredCnt)

                    echo "desiredCnt: ${desiredCnt}"
                    */
                    
                }
            }
        }

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

        /*
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

        stage('Preparing Auto-Scale Stage') {
            steps {
                script {
                    echo "----- [Auto-Scale] Preparing Next Auto-Scaling-Group: ${env.NEXT_ASG_NAME} -----"

                    sh"""
                    aws autoscaling update-auto-scaling-group --auto-scaling-group-name ${env.NEXT_ASG_NAME} \
                    --desired-capacity ${env.ASG_DESIRED} \
                    --min-size ${ASG_MIN} \
                    --region ap-northeast-2
                    """

                    echo "----- [Auto-Scale] Waiting boot-up ec2 instances: 60 secs. -----"
                    sh "sleep 60"
                }
            }
        }

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
