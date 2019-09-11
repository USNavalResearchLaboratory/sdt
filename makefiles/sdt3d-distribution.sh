#!/bin/sh
THISDIR=`dirname $0` 
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=${THISDIR}/lib -Xmx512m ${THISDIR}/jars/sdt3d.jar $@
