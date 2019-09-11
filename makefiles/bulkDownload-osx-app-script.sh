#!/bin/sh
THISDIR=`dirname $0` 
export LD_LIBRARY_PATH=${THISDIR}/Resources/Java
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=${THISDIR}/Resources/Java -Xmx512m ${THISDIR}/Resources/Java/bulkDownload.jar
