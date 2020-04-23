# Woongjin backend-demo
    Framework : SpringBoot 
    Build Tool : Maven
    API Document Tool : Swagger
    Test : JUnit5
    
#### 빌드 방법
##### 1. Git checkout
    /* make directory */
    $ cd ~
    $ mkdir workspace
    $ cd workspace
    
    /* git clone */
    $ git clone https://github.com/pe-woongjin/backend-demo.git
    $ cd ~/workspace/backend-demo
    
##### 2. Maven build
    $ cd ~/workspace/backend-demo
    $ mvn clean package
    
##### 3. jar 실행
    $ cd ~/workspace/backend-demo
    
    각 환경에 맞춰서 jar 실행
    [development] 
    $ java -jar -Dspring.profiles.active=dev ./target/backend-demo-1.0.0-SNAPSHOT.jar
    [staging]
    $ java -jar -Dspring.profiles.active=stg ./target/backend-demo-1.0.0-SNAPSHOT.jar
    [production]
    $ java -jar -Dspring.profiles.active=prd ./target/backend-demo-1.0.0-SNAPSHOT.jar
    
##### 4. 확인
    127.0.0.1:8080/swagger-ui.html 에서 REST API 확인이 가능하며,
    어떠한 환경에서 진행 중인지 좌측 상단의 'UI Information'에서 확인이 가능합니다.