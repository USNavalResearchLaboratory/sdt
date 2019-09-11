BUILDING SDT3D

The sdt3d application has external dependencies on NASA's Worldwind
JDK, joglutils (a library that provides Java bindings for OpenGL and
3d Model support), and protolib-jni (a library that provides java
native interface to NRL's protolib protoPipe implementation).

 The latest open source version of the NASA Worldwind JDK can be found
 at http://worldwind.arc.nasa.gov/java/index.html and the protolib
 source at http://downloads.pf.itd.nrl.navy.mil/protolib/. The
 location of the joglutils library is unreliable at best but the
 latest source may be found at:
 https://github.com/sgothel/jogl-utils/.

LINUX BUILD

For Linux, a Makefile is provided that will build a sdt3d.jar file:
(Note that you must have JAVA_HOME defined and javac and jar available
in your path). First download the latest worldwind and joglutils zip
files. Unzip the release in the sdt/makefiles directory and ensure the
zip files are named joglutils.zip and worldwind.zip. From the
makefiles directory make sdt3d. "make -f Makefile all" will make sdt,
sdtcmd, and sdt3d.

cd sdt/makefiles 
make -f Makefile.linux sdt3d 

The sdt3d.sh helper script in the makefiles directory can then be used to launch the sdt3d jar file.

./sdt3d.sh

WINDOWS BUILD

The easiest way to build sdt3d under windows is to use the Eclipse
tool with the following four projects:

sdt3d

Within Eclipse, load the src/java folder into a new project and name
it sdt3d. 

The java folder contains Eclipse .project and .classpath configuration
files that should be used to build the sdt3d project. (They expect the
worldwind, joglutils, and protolib-jni project directories to be
relative to the sdt3d as described in the following steps).

Modify the sdt3d java build path to find the java jre libraries in the
appropriate location for your system if necessary. (See the sdt3d
project properties java build path libraries section)

protolib-jni

Get the nightly build distribution from
http://downloads.pf.itd.nrl.navy.mil/protolib/ and load the source
files directly from /protolib/src/java.

Protolib-jni dll, jar, and library files are provided the src/java
directory for your convenience and are also available in the protolib
binary distribution. They should be loaded into your protolib project.

If you have need to build protolib-jni, use a Visual Studio CMD (so
that you have the correct environment variables set) and change
directory to the top level protolib directory. Clean the distribution
and configure waf to build the protolib-jni. Finally run waf to create
the protolib-jni dll, jar, and library files in the build/default
directory.

waf distclean
waf configure --disable-wx --disable-vif --disable-manet --static --build-java
waf

joglutils

Download the joglutils distribution and unzip the project relative to
the sdt3d project in the Eclipse workspace directory. Load into the
Eclipse workspace using the build.xml file provided in the joglutils
make directory. This project should be relative to the sdt3d project
and be called joglutils.

Modify the joglutils java build path to find the java jre (rt.jar)
library in the appropriate location for your system. (See the
joglutils project properties java build path libraries section).

worldwind

Download the worldwind zip file. Unzip the project relative to the
sdt3d project in your Eclipse workspace directory. Load into the
Eclipse workspace using the build.xml ant file provided in the
worldwind directory.

Add the jogl.jar file in the worldwind workspace to the joglutils
project. (See the joglutils project properties java build path
libraries section).

BUILDING SDT AND SDTCMD

To build the sdt application, you will need the wxWidgets library for
your platform freely-available from:

http://www.wxwidgets.org

The current release depends on wxWidgets 2.9 or wxWidgets
2.8. Previous releases (1.1a1 and above) use wxWidgets 2.6.x. sdt
releases 1.0a1-1.0a8 use wxWidgets 2.4.x.

Please note that binary releases of wxWidgets do not include the
header files necessary to compile sdt, so you must download and build
the source code. Please see the wxWidgets readme for instructions. It
is recommended to compile wxWidgets using the "--disable-shared"
config option so that the binary may be moved to machines that do not
have wxWidgets installed.

WINDOWS BUILD

Microsoft Visual Studio project files are available in the
distribution in the makefiles/win32 directory. Load the sdt solution
file into Microsoft Visual Studio. Project files are available that
build sdt and sdtcmd.

LINUX BUILD

From the makefiles directory make sdt and sdtcmd. "make -f Makefile.linux
all" will make sdt,sdtcmd, and sdt3d.

cd sdt/makefiles 
make -f Makefile sdt
make -f Makefile sdtcmd

MACOSX BUILD

From the makefiles directory make sdt and sdtcmd. "make -f Makefile.macosx
all" will make sdt,sdtcmd.

cd sdt/makefiles 
make -f Makefile sdt
make -f Makefile sdtcmd
