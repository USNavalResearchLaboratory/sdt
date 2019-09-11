#!/bin/sh
THISDIR=/home/$USER/sdt3d/
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=${THISDIR}/lib -Xmx512m ${THISDIR}/jars/sdt3d.jar LISTEN TCP,50000 $@
