#!/bin/bash
echo "beforeInstall"

echo "beforeInstall" > beforeinstall
echo whoami >> beforeinstall
mkdir /home/ec2-user/app -p
