# GitLab webhook 구성GitLab webhook 구성
GitLab에 jenkins webhook 구성을 연결하여 git에 commit이 일어나는 경우 자동 배포를 실행하게 한다.

## 1. GitLab 토큰 생성
> - Settings > Access Tokens
![Screenshot](../img/cicd-01-01.png)
> - GitLab에서 생성된 토큰 값은 다시 확인이 불가능함으로 복사해서 갖고있는다.
![Screenshot](../img/cicd-01-02.png)
## 2. Jenkins 설정
### 2-1) Credentials 등록
> - 등록 화면
![Screenshot](../img/cicd-01-03.png)
![Screenshot](../img/cicd-01-04.png)
> - GitLab에서 발급 받은 토큰 값 설정
![Screenshot](../img/cicd-01-05.png)
### 2-2) 시스템 GitLab 설정
> - jenkins credentials 선택
![Screenshot](../img/cicd-01-06.png)
![Screenshot](../img/cicd-01-07.png)

## 3. Jenkins webhook 설정
### 3-1) Jenkins webhook 정보 확인
> - Jenkins 화면에서 webhook url 정보를 확인한다.
![Screenshot](../img/cicd-01-08.png)
![Screenshot](../img/cicd-01-09.png)

### 3-2) GitLab 등록
> - gitlab 화면에서 jenkins webhook url을 등록한다.
> - 등록
![Screenshot](../img/cicd-01-10.png)
> - 테스트
![Screenshot](../img/cicd-01-11.png)
> - 결과
![Screenshot](../img/cicd-01-12.png)