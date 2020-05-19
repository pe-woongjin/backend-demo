# Demo API 소개 

본 가이드는 Demo용 API 어플리케이션 서비스를 AWS 클라우드 환경에서 Jenkins + CodeDeploy를 통해 Blue/Green 무정지 배포 구성을 보여 줍니다.

"아키텍처"를 통해 전체 구성을 이해 합니다.    

```js
1. 인프라 아키텍처
   클라우드 인프라스트럭처 구조를 이해 합니다.

2. CICD 아키텍처
   툴체인 파이프라인 흐름을 이해 합니다.

3. 어플리케이션 아키텍처
   어플리케이션 구조를 이해 합니다.
```

Jenkins + CodeDeploy를 통한 무정지 배포 환경 구성 과정은 아래와 같습니다.

```js
1. 인프라 구성
   - IAM 구성
   - VPC 리소스 구성
   - CodeDeploy 구성

2. 베이스 이미지 구성
   (demo-api 애플리케이션이 구동 되는 런-타임 환경 구성)

3. CICD 구성
   - GitLab 구성
   - Jenkins 구성
     
```


