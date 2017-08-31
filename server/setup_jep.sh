#!/bin/bash

mkdir _tmp 2> /dev/null
cd _tmp

echo 'cloning jep repo'
git clone https://github.com/ninia/jep

cd jep
echo 'building jep'
python setup.py build

mkdir ../../libs 2> /dev/null
cp build/java/jep-3.7.0.jar ../../libs/
cp build/lib*/jep.so ../../
echo 'moved .jar file in libs/ and .so in root'

cd ../..
echo 'deleting cloned jep repo'
rm -rf _tmp
