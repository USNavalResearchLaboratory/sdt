#!/bin/sh
THISDIR=`dirname $0` 
export LD_LIBRARY_PATH=${THISDIR}/build/sdt3d/lib
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=${THISDIR}/build/sdt3d/lib -Xmx512m ${THISDIR}/build/sdt3d/jars/bulkDownload.jar
