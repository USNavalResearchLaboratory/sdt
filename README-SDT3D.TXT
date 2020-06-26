The Worldwind software is Copyright (C) 2001 United States Government
as represented by the Administrator of the National Aeronautics and
Space Administration.  All Rights Reserved.

The Scripted Display Tools are open source software developed by the
Naval Research Laboratory (NRL) PROTocol Engineering Advanced
Networking (PROTEAN) group. SDT provides a simple 2D visualization
capability using standard image files for a background and set of
overlaid nodes. SDT3d provides a 3D visualization capability using
Nasa's World Wind 3D interactive world viewer and set of overlaid
nodes.  See the README-SDT.txt file for details on SDT (2d).


SDT3d - "The Scriptable Display Tool"

SDT3d provides a 3D visualization capability using Nasa's World Wind
3D interactive world viewer and set of overlaid nodes.  Nodes are
assigned iconic images for the display sourced from standard format
image files (e.g. JPEG, PNG, GIF, etc).  Nodes are placed at geodesic
(or cartesian) coordinates and positions can be dynamically updated to
"move" their associated icons about the globe.

Displayed nodes can also be dynamically "linked" and "unlinked" with
lines of user-specified color and thickness.  This makes the sdt tools
well-suited for one of its intended purposes which is to provide a
real-time visualization of dynamic, possibly mobile data communication
networks.

Commands may be given to "SDT3d" from an input script, in realtime via
a pipe, UDP/TCP socket, or interactively by the sdtcmd.exe utility, e.g.

sdtcmd.exe node node01 symbol circle

SDT3D is written in Java using WorldWinds opensource JDK.  Versions of
the libraries are available for most Unix, MacOS, and Win32 platforms.

For Linux and OSX, Makefiles are provided that will build the
application. See "BUILDING SDT3D UNDER LINUX/OSX" and "BUILDING SDT3D
JAR FILE IN ECLIPSE" for detailed build instructions.

Eclipse is the easiest way to build the application under windows.
See "SETTING UP THE SDT3D ECLIPSE PROJECT"  (Short notes:  Add jogl-all.zip
from worldwind to the joglutils project libraries and the worldwind gdal
gluegen-rt jogl-all to the worldwind project.  Add the jre system library
add library add jre system library)

A "sdtcmd" helper utility that will send sdt commands over a command
pipe to a sdt3d application is included in the release.  Use the
makefiles to build the utility

make -f Makefile.<os> sdtcmd



BUILDING SDT3d UNDER LINUX/OSX

1.  By default the script will download and patch source dependencies
from freely available sites if joglutils.zip, worldwind.zip, and
protolib.tgz files do not exist in the makefiles directory.

The scripts to download and patch the source files require that "git"
"ant", "javac", "g++", and "wget" packages to be installed on your system.

(The "prep-dependencies-linux" helper script may be used to install
linux dependencies via apt-get prior to running the makefile.)

On certain Ubuntu distributions package libvecmath-java maybe required:

apt-get install libvecmath-java

On OSX Mavericks and above you will need to get vecmath.jar available at:

http://jogamp.org/deployment/java3d/1.6.0-pre11/

Place the jar in /System/Library/Java/Extensions prior to El Capitan.  Place in /Library/Java/Extensions in El Capitan.

Under Ubuntu you may also need to set java home via update-alternatives, e.g.

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/
update-alternatives --display java
update-alternatives --display javac
update-alternatives --display jar

The pthread and netfilter-queue packages are required to build protolibd:

apt-get install libpcap-dev
apt-get install libnetfilter-queue-dev

Alternatively, you may download prepatched worldwind.zip and
joglutils.zip and the latest protolib.tgz from sdt3d file release
section on the protean forge website.
	
	https://pf.itd.nrl.navy.mil/sf/frs/do/viewSummary/projects.sdt/frs
	
Place in the sdt makefiles directory.  The makefile will unzip and
process these files.  (Make sure the files are named "worldwind.zip",
"joglutils.zip", and "protolib.tgz" after the download.)

You may need to add a javac command to the worldwind/build.xml file, e.g:

<project name="worldwind" default="build" basedir=".">
	<javac srcdir="src" destdir="bin" />
    <description>


2.  A Makefile is provided that will unpack the appropriate jar and
source files and build a sdt3d application (including a sdt3d.jar):
(Note that you must have JAVA_HOME defined and javac and jar available
in your path)

Java 7 is currently required.  Instructions available to build with
Java 6 available upon request.

cd sdt/makefiles

Linux 64 bit: 
      make -f Makefile.linux_amd64 sdt3d
Linux 32 bit:
      make -f Makefile.linux_i586i sdt3d
Osx:   
      make -f Makefile.macos sdt3d.app

(To make both the sdt and sdt3d projects use "make all".  Note to make sdt 2D
you must have wxWidgets installed);

To rebuild just the sdt3d.jar file:

make -f Makefile.<platform> build_sdt3d

If something goes wrong with the above build step, you may need to
recover via:

make -f Makefile.<arch> distclean

On Linux the sdt3d.sh helper script can be used to launch the sdt3d
jar file.  All the files required to run the application are also
bundled into a sdt3d.zip file.  (The sdt3d.sh in the zip file will
need to be modified to point to the deployed jar file location.)

On Mac Osx a sdt3d osx application (sdt3d.app) will be created.

To build the sdtcmd utility substitude sdt3d or sdt3d.app with sdtcmd in the
above build instructions.

SETTING UP THE SDT3D ECLIPSE PROJECT.

(Note that eclipse .classpath and .project files are available in
/sdt3d/makefiles/win32)

1.  Use SVN to import sdt3d into Eclipse.  sdt3d java src files are
available in the proteantools sdt project on the protean forge
website:
	
	https://pf.itd.nrl.navy.mil/svnroot/sdt/trunk
	
	Check out the /src/java folder and name it sdt3d.
	
2.  Download the joglutils zip file from the latest sdt3d file release
on the protean forge website. Load into eclipse using the build.xml
file provided in the joglutils make directory.  This project should be
relative to the sdt3d project and be called joglutils.

Note that the original joglutils distribution that was
modified to work with WWJ 2.0 and JOGL 2.0 is available at:

<https://kenai.com/projects/jogl/sources/jogl-utils-git>

3.  Use SVN to load protolib-jni into Eclipse.  Protolib-jni is
available on the protean forge website in the protolib project:

	https://pf.itd.nrl.navy.mil/svnroot/protolib
	
	Check out the /trunk/src/java folder and name it protolib-jni.  

4.  Download the worlwind zip file in the latest sdt file release on
the protean forge website to the eclipse workspace directory.  Unzip
the worldwind project relative to the sdt3d project.  Import the
project using the build.xml ant file in the worldwind directory.

Note that the original worldwind distribution is available at:
<http://worldwind.arc.nasa.gov/java/index.html> 

5.  You may need to modify the joglutils java build path to find the
java jre (rt.jar) library in the appropriate location for your system.
(Add the jar file to the jogutils project properties java build path
libraries section).

You may also need to add the jogl and gluegen jar files in the
worldwind workspace to the joglutils project.
	
6.  Modify the sdt3d java build path to find the java jre libraries in
the appropriate location for your system if necessary.  (sdt3d project
properties java build path libraries section)

7.  sdt3d also relies on the following jar files.  See the getDependencies file for jar locations for manual download.

Add these to your project.

apache:
commons-io-2.6.jar 
commons-lang3-3.10.jar 
commons-text-1.8.jar 

mozilla:
juniversalchardet-1.0.3.jar 


The "sdt3d" project uses relative paths for the referenced native
libraries (Java OpenGL, Protolib-jni, etc) and should build.  The
WorldWind SDK provides the native libaries for Java OpenGL and related
classes.  The Protolib-jni code provides a Java instantiation of the
Protolib "ProtoPipe" we use for interprocess communications that
serves as the "remote control" interface for sdt3d at this time (We
may add a native Java interprocess interface in the future).

The JOGUTILS project provides support for 3D models that "sdt3d" can
display in addition to icons to represent network nodes (or other
items).

You may need to add worldwindx.jar to the sdt3d project.


BUILDING SDT3D JAR FILE IN ECLIPSE (WINDOWS)

1.  Use the eclipse export JAR file option.  Include the sdt3d src,
protolib-jni src and joglutils src trees.  Disable other content
selected by eclipse (e.g. .classpath, jogutils demo etc.)
	
2.  Check:
	Export generated class files and resources
	Export Java source files and resources

3.  When exporting the jar elect to use the included manifest.mft
specific to your os in the project, otherwise eclipse will overwrite
the class path.  This file should contain:
 
 Manifest-Version: 1.0
 Class-Path: gdal.jar gluegen-rt.jar gluegen-rt-natives-<os>.jar jogl-all.jar jogl-all-natives-<os>.jar joglutils.jar protolib-jni.jar worldwind.jar worldwindx.jar
 Main-Class: mil.navy.nrl.sdt3d.sdt3d    


BUILDING SDT3D JAR FILE IN ECLIPSE (MAC OSX)

1.  From within Eclipse use the "export" option to export a sdt3d osx
application.  Eclipse will create a sdt3d.app directory
    
2.  Copy the sdt3d.icns icon file to the Resources directory.

3.  Copy relevant jar and jnilib files to the Resources directory.  
(See Manifest-macosx-universal.txt for details).


BUILDING PROTOLIB-JNI WITH VISUAL STUDIO

The waf script included in the sdt3d distribution should build the
protolib-jni library successfully in the linux and osx environments.
On windows, Visual Studio may be used:

1) protolib-jni - Available via SVN from protolib 

   SVN root = "/svnroot/protolib/trunk/", module = "protolib"

To build:

   a.  Load the protolib project into Visual Studio.
   b.  Using a Visual Studio cmd (so that you have the correct
       environment variables set) change to the protolib
       directory.  Run "waf distclean"
   d.  Configure waf:
           "waf configure --disable-wx --disable-vif
            --disable-manet --static --build-java"
   e.  Build protolib-jni with the configured waf utility:
           "waf"
       The protolib-jni dll, jar, and library files will be
       created in the build/default directory.  Reference
       these files in your sdt3d project.


SOLVE THE WWJ/JAVA FLICKER PROBLEM:
 
 1.  To solve the java "flicker" problem add:  

-Dsun.java2d.noddraw=true

to the Sdt3d project configuration Run/Debug Settings for the sdt3d 
launch configuration settings(the VM arguments command line)

MISCELLANEOUS

1.  To bring up the wwj dashboard (statistics panel) 
    ctl+alt+shift+left-click in the wwd window

2.  If running into performance problems when using many symbols you
may try increasing the airspace geometry cache in
worldwind.src.config.worldwind.xml

gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize is set to 32000000

Brian Adamson
<adamson@itd.nrl.navy.mil>
01 Apr 2014
