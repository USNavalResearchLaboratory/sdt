#!/bin/sh
THISDIR=`dirname $0`
export LD_LIBRARY_PATH=${THISDIR}
cd ${THISDIR}
cd ../
echo ${LD_LIBRARY_PATH}
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=./Resources/Java -Xmx512m ./Resources/Java/sdt3d.jar
