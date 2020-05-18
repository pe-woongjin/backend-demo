# GitOps 환경 구성

GitLab Webhook을 통해 Git 저장소의 베이스 브랜치(develop, release, master)에 PR(Pull Request) 
머지가 되거나 변경이 되었을 때 Jenkins 에서 자동 배포가 이루어 지도록 구성 합니다.

## 1. 사전 준비 작업
1. 인프라 리소스(VPC, ACM, ALB, 및 네트워크 리소스 등...)가 정상적으로 생성 되어 있어야 합니다. 
2. code-deploy 어플리케이션과 배포 그룹 및 Auto-Scaling-Group 등이 정상적으로 생성 되어 있어야 합니다.  
3. application(demo-api) 프로젝트에 Jenkinsfile을 설정 해야 합니다.  
4. code-deploy agent 가 정상 수행 되려면 프로젝트(demo-api)에 appspec.yml 설정 파일을 구성 해야 합니다.   


## 2. GitOps 환경 구성을 위한 주요 단계 

1. Gitlab 토큰 생성 
2. Jenkins Credentials 등록
3. Jenkins CICD 구성 
4. Jenkins Webhook 구성

