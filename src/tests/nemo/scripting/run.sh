#!/bin/bash

HDK=0
if [ -f ../../../../build.xml ]; then
	HDK=1
fi

JARFILE=../../../../jars/scripting.jar
BUILDFILE=builder.xml
TARGET=scripting
if [ $HDK -eq 1 ]; then
	JARFILE=../../../../hook_test.jar
	BUILDFILE=../../../../build.xml
	TARGET=""
fi

if [ ! -f $JARFILE ]; then
    echo "jarfile [$JARFILE] not found"
    echo "attempting rebuild"
    TARGETPATH=`echo $JARFILE | sed -e 's/jars\/scripting.jar//'`
    CWD=`pwd`
    cd $TARGETPATH
    ant -buildfile $BUILDFILE $TARGET
    cd $CWD
fi

if [ -f $JARFILE ]; then
    echo "jarfile [$JARFILE] not found"
    sed -e s@CURRENT_WORKING_DIR@$PWD@ massProducts.js > massProducts_compiled.js
    java -classpath $JARFILE massProducts_compiled.js
else
	echo "jarfile [$JARFILE] missing"
fi
