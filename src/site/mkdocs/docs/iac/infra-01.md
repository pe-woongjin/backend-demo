# IAM 역할만들기
> - Code-deploy를 사용하여 배포하기 위해서는 'EC2'와 'CODE-DEPLOY'에 역할이 생성되어야한다.

## 1. 공통 메뉴
![iam common](../img/infra-01-01.png)

## 2. EC2 역할 생성
> - EC2 역할은 aws lunch-template 생성시에 설정해준다.
![iam ec2](../img/infra-01-02.png)
![iam ec2](../img/infra-01-03.png)
![iam ec2](../img/infra-01-04.png)
![iam ec2](../img/infra-01-05.png)
>

## 3. code-deploy 역할 생성
> - code-deploy 역할 어플리케이션 생성시에 설정해준다.
![iam codedeploy](../img/infra-01-06.png)
![iam codedeploy](../img/infra-01-07.png)
![iam codedeploy](../img/infra-01-08.png)