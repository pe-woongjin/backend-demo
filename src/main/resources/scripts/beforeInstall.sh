#!/bin/bash
echo "beforeInstall"

echo "beforeInstall" > beforeinstall
echo whoami >> beforeinstall
pkill -9 java
mkdir /home/ec2-user/app -p
