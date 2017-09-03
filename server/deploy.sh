#!/bin/bash

deploy_dir=$1

gradle uploadArchive
gradle copyRuntimeLibs

mkdir -p $deploy_dir 2> /dev/null

cp -r libsAll $deploy_dir
cp -r artifacts $deploy_dir
rm -rf libsAll
rm -rf artifacts
echo 'Copied libs and jars'

cp start_server.sh $deploy_dir
cp -r ../ml/ $deploy_dir
echo 'Copied ml scripts'

cp fts.properties $deploy_dir/
echo "Don't forget to add fts api password in $deploy_dir/fts.properties"

touch $deploy_dir/py_interface.properties
echo "ml_scripts_dir=ml/" >> $deploy_dir/py_interface.properties
echo "visualizer_scripts_dir=visualization/" >> $deploy_dir/py_interface.properties

mkdir $deploy_dir/data/ 2> /dev/null

mkdir $deploy_dir/native/ 2> /dev/null
cp libjep.so $deploy_dir/native/
