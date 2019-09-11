#!/bin/sh
# Modify this dir to the directory containing your sdt3d.app
#THISDIR=`dirname $0`
THISDIR=../../../makefiles/sdt3d.app/Contents/Resources
export LD_LIBRARY_PATH=${THISDIR}
java -jar -Dsun.java2d.noddraw=true -Djava.library.path=${THISDIR}/Java -Xmx512m ${THISDIR}/Java/sdt3d.jar $@
