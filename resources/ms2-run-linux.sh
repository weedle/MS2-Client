#!/bin/bash
# see http://askubuntu.com/questions/100751/minecraft-in-jdk-1-7-0-u2-x64
# run with bash ms2-run-linux.sh
# replace below path with your JVM
# you may need to change run permissions (http://www.cyberciti.biz/faq/how-do-i-make-a-linux-or-freebsd-file-an-executable-file/)
#export LD_LIBRARY_PATH="/usr/lib/jvm/jdk1.7.0/jre/lib/amd64"

java -jar mineshaftersquared.jar
read -n 1 -p "Press any key to continue... "