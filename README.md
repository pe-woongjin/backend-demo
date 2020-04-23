# Woongjin backend-demo
    Framework : SpringBoot 
    Build Tool : Maven
    API Document Tool : Swagger
    Test : JUnit5
    gg
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
    $ mvn clean
    $ mvn package
    
##### 3. jar 실행
    $ cd ~/workspace/backend-demo/target
    $ java -jar backend-demo-1.0.0-SNAPSHOT.jar
    
##### 4. 확인
    127.0.0.1:8080/swagger-ui.html 에서 REST API 확인이 가능합니다.
jjjj
