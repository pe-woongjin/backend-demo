#!/bin/bash
echo "applicationStart"
cd '/home/ec2-user/app'
nohup java -jar demo-api.jar > /dev/null 2>&1 &