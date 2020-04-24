#!/bin/bash
echo "applicationStart"
echo "Starting Spring Boot app"
cd '/home/ec2-user/app'
java -jar backend-demo.jar &
