#!/bin/bash
if [ ! -d "libs" ];
then
	echo "Run this from the project root"
	exit -1
fi

echo "Downloading libs for Mineshafter Squared Universal Launcher..."
echo

cd libs

MIRROR=http://apache.sunsite.ualberta.ca
LIBS=( "configuration-1.9" "cli-1.2" "io-2.4" "lang-2.6" "logging-1.1.3" )

for each in "${LIBS[@]}"
do
	arr=(${each//-/ })
	name=commons-${arr[0]}-${arr[1]}
	file=$name-bin.tar.gz
	if [ -f $file ];
	then
		echo "$file already exists (skipping)"
	else
		wget $MIRROR//commons/${arr[0]}/binaries/$file
	fi
	if [ -d $name ];
	then
		echo "$name already unzipped"
	else
		tar -zxvf $file
	fi
	jar=$name.jar
	if [ -f $jar ];
	then
		echo "$name.jar already extracted"
	else
		cp $name/$jar $jar
	fi
	echo
done

git clone https://github.com/Raekye/SimpleAPI-Java.git
if [ -f "simpleapi.jar" ];
then
	echo "simpleapi.jar already exists"
else
	cd SimpleAPI-Java
	ant
	cd ..
	cp SimpleAPI-Java/dist/simpleapi.jar simpleapi.jar
fi
echo

if [ -d "google-gson-2.2.4" ];
then
	echo "Google GSON download already exists"
else
	wget --no-check-certificate "https://google-gson.googlecode.com/files/google-gson-2.2.4-release.zip"
	unzip "google-gson-2.2.4-release.zip"
fi
if [ -f "gson-2.2.4.jar" ];
then
	echo "Google GSON jar already exists"
else
	cp "google-gson-2.2.4/gson-2.2.4.jar" "gson-2.2.4.jar"
fi
echo

echo "Done getting libs for Mineshafter Squared Universal Launcher"
