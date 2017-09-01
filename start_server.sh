#!/bin/bash

KITEK_DIR=$1

gradle -p $KITEK_DIR/server uploadArchive
gradle -p $KITEK_DIR/server copyRuntimeLibs

mkdir _tmp 2> /dev/null
cd _tmp

cp -r $KITEK_DIR/server/libsAll .
cp -r $KITEK_DIR/server/artifacts .
rm -rf $KITEK_DIR/server/libsAll
rm -rf $KITEK_DIR/server/artifacts

echo 'starting server'
java -cp "./libsAll/*:artifacts/kitek-server-0.1-SNAPSHOT.jar" MainKt 
