#!/bin/sh
THISDIR=`dirname $0` 
export LD_LIBRARY_PATH=${THISDIR}/lib
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=${THISDIR}/lib -Xmx512m ${THISDIR}/jars/bulkDownload.jar
