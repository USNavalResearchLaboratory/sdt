See the README-SDT3d.TXT and README-SDT.TXT files in the root project
directory for instructions for building the applications.  html and
pdf build instructions are also available in the docs directory.

The perferred java implementation for sdt3d 2.0 is opendjdk java 1.8

hint:
in .bashrc

set JAVA_HOME="$(/usr/libexec/java_home -v 1.8"

export JAVA_HOME=`/usr/libexec/java_home -v 1.8`


For java versions note:

On certain OSX implementations there is some conflict after upgrading from
OSX's 1.6 Java JDK to Oracle's 1.7 JDK.

To resolve this:

Install OSX's 1.6 runtime environment in its default location

/System/Library/Java/JavaVirtualMachines/1.6.0jdk

Install Oracle's 1.7 JDK in its default location

/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk

Make a symlink from 1.6 jdk include directory to jdk 1.7 include dir

/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/include: symbolic link to `/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/include' 

Finally be sure to compile against 1.6 jdk

export JAVA_HOME=`/usr/libexec/java_home -v 1.6`

or 1.8

export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

----------

Under Ubuntu 20.04 until the distribution is fixed modify SdtLayerPanel.java:395 to compile with java 11

(Enumaration<DefaultMultableTreeNode>)(Object)parentNode.children();

w/sudo apt install openjdk-11-jdk got a linker mismatch

Finally uninstalled openjdk-11 and installed openjdk-8 instead.


