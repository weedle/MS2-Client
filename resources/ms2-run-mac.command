#!/bin/bash
# see http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR
java -jar mineshaftersquared.jar
read -n 1 -p "Press any key to continue... "