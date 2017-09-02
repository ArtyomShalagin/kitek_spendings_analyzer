#!/bin/bash

mkdir _tmp 2> /dev/null
cd _tmp

echo 'Cloning jep repo'
git clone https://github.com/ninia/jep

cd jep
echo 'Building jep, need sudo for installation'
sudo env "PATH=$PATH" python setup.py build

mkdir ../../libs 2> /dev/null
cp build/java/jep-3.7.0.jar ../../libs/
cp build/lib*/*.so ../../libjep.so
echo 'Moved .jar file in libs/ and .so in root'

cd ../..
echo 'deleting cloned jep repo'
sudo rm -rf _tmp
