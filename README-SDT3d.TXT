The Worldwind software is Copyright (C) 2001 United 
States Government as represented by the Administrator 
of the National Aeronautics and Space Administration.  
All Rights Reserved.

The Scripted Display Tools are open source software developed 
by the Naval Research Laboratory (NRL) PROTocol Engineering 
Advanced Networking (PROTEAN) group. SDT provides a simple 2D 
visualization capability using standard image files for a 
background and set of overlaid nodes. SDT3d provides a 3D 
visualization capability using Nasa's World Wind 3D
interactive world viewer and set of overlaid nodes.  See 
the README-SDT.txt file for details on SDT (2d).


SDT3d - "The Scriptable Display Tool"

SDT3d provides a 3D visualization capability using Nasa's World 
Wind 3D interactive world viewer and set of overlaid nodes.  
Nodes are assigned iconic images for the display sourced from 
standard format image files (e.g. JPEG, PNG, GIF, etc).  Nodes 
are placed at cartesian coordinates and positions can be dynamically 
updated to "move" their associated icons about the globe.  

Displayed nodes can also be dynamically "linked" and "unlinked" 
with lines of user-specified color and thickness.  This makes the 
sdt tools well-suited for one of its intended purposes which 
is to provide a real-time visualization of dynamic, possibly 
mobile data communication networks.  

Commands may be given to "SDT3d" from an input script, in realtime
via a pipe, socket, or interactively by the sdtcmd.exe utility, e.g.

sdtcmd.exe node node01 symbol circle

SDT3D is written in Java using WorldWinds opensource JDK. 
Versions of the libraries are available for most Unix, MacOS, 
and Win32 platforms.

For Linux, a Makefile is provided that will unpack the appropriate
jar and source files and build a sdt3d.jar file: (Note that you must
have JAVA_HOME defined and javac and jar available in your path)
See "BUILDING SDT3D UNDER LINUX" for details build instructions.

cd sdt/makefiles
make -f Makefile

Eclipse is the easiest way to build the application under windows and
Mac OSX.  See "BUILDING THE SDT3D ECLIPSE PROJECT"


BUILDING SDT3d UNDER LINUX

1.  Download joglutils.zip, worldwind.zip, and protolib.tgz 
    from the latest sdt3d file release on the protean forge website.  
	
	https://pf.itd.nrl.navy.mil/sf/frs/do/viewSummary/projects.proteantools/frs
	
	Place in the sdt makefiles directory.  The makefile will 
	unzip and process these files.  (The worldwind zip file may
	contain the WWJ Version number in the file.  Make sure the
	files are named "worldwind.zip", "joglutils.zip", and
	"protolib.tgz" after the download.)

2.  For Linux, a Makefile is provided that will unpack the appropriate
jar and source files and build a sdt3d.jar file: (Note that you must
have JAVA_HOME defined and javac and jar available in your path)

cd sdt/makefiles
make -f Makefile.linux sdt3d

(To make both the sdt and sdt3d projects make all");

The sdt3d.sh helper script can be used to launch the sdt3d jar file.

Note that if you have developer access, you may check the protolib-jni code out
of SVN from our "pf.itd.nrl.navy.mil" server and build it yourself - but note
that you will need to change the above referenced makefile:

SVN root = "/svnroot/protolib/trunk/"

See "BUILDING PROTOLIB-JNI WITH VISUAL STUDIO" for more detailed instructions


BUILDING THE SDT3D ECLIPSE PROJECT.

(Note that eclipse .classpath and .project files are available in
/sdt3d/makefiles/win32)

1.  Use SVN to load sdt3d into Eclipse.  sdt3d java src files 
	are available in the proteantools sdt project on the 
	protean forge website:
	
	https://pf.itd.nrl.navy.mil/svnroot/proteantools/trunk/sdt
	
	Check out the /src/java folder and name it sdt3d.
	
2.  Download the joglutils zip file from the latest sdt3d file 
	release on the protean forge website. Load into eclipse 
	using the build.xml file provided in the joglutils make
	directory.  This project should be relative to the sdt3d
	project and be called joglutils.

    Alternatively, joglutils is available at https://github.com/sgothel/jogl-utils
    
		
3.  Use SVN to load protolib-jni into Eclipse.  Protolib-jni is available 
	on the protean forge website in the protolib project:

	https://pf.itd.nrl.navy.mil/svnroot/protolib
	
	Check out the /trunk/src/java folder and name it protolib-jni

4.  Download the worlwind zip file in the latest sdt file release 
	on the protean forge website to the eclipse workspace directory.  
	Unzip the worldwind project relative to the sdt3d project.
	Import the project using the build.xml ant file in the worldwind 
	directory.

   Note that the original worldwind distribution is available at:
   <http://worldwind.arc.nasa.gov/java/index.html> 


5.  Modify the joglutils java build path to find the java jre (rt.jar) 
	library in the appropriate location for your system.  (Add the 
	jar file to the jogutils project properties java build path 
	libraries section)
	
	Also add the jogl.jar file in the worldwind workspace to the 
	joglutils project.
	
6.  Modify the sdt3d java build path to find the java jre libraries 
	in the appropriate location for your system if necessary. 
	(sdt3d project properties java build path libraries section)


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


BUILDING SDT3D JAR FILE IN ECLIPSE (WINDOWS)

1.  Use the eclipse export JAR file option.
    Include the sdt3d src, protolib-jni src and joglutils src trees.  
	Disable other content selected by eclipse (e.g. .classpath, 
	jogutils demo etc.)
	
2.  Check:
	Export generated class files and resources
	Export Java source files and resources

3.  When exporting the jar elect to use the existing manifest.mft 
	in the project, otherwise eclipse will overwrite the class path.  
	This file should contain:
 
 Manifest-Version: 1.0
 Class-Path: protolib.jar protolibJni.jar gluegen-rt.jar jogl.jar world
   wind.jar joglutils.jar
 Main-Class: mil.navy.nrl.sdt3d.sdt3d    

BUILDING SDT3D JAR FILE IN ECLIPSE (MAC OSX)

1.  From within Eclipse use the "export" option to export the sdt3d application.
    Eclipse will create a sdt3d.app directory
    
2.  Copy the sdt3d.icns icon file to the Resources directory.

3.  Copy relevant jar and jnilib files to the Resources directory.


BUILDING SDT3D JAR FILE LINUX

A waf script is provided in the sdt makefiles directory that will build an 
sdt3d.jar file in the build directory subtree.

make -f Makefile.linux sdt3d

BUILDING PROTOLIB-JNI WITH VISUAL STUDIO

2) protolib-jni - Available via SVN from protolib 

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

2.  You may increase the airspace geometry cache

in worldwind.src.config.worldwind.xml

gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize is set to 32000000

Supposedly this will help airspace performance - I've increased it to 200000000 
with no real performance increase as far as I could tell (running a very large scenario)  
(tag on the wwj board recommended 100000000)

Tag also says "You can set the cache size programatically, but it's better to set it in 
the configuration file because the cache is lazily created by the Airspace code. "

3.  To add open street map to new versions of WWJ copy config/Earth/OpenStreetMapHybridLayer.xml
    to config/earth.  Modify worldwind.layers.xml to href this xml actuate onRequest

CHANGES TO WWJ TOOLKIT

1.  Worldwind/src/config/worldwind.layers.xml

    USGSDigitalOrthoLayer.xml
    OpenStreetMapHybridLayer.xml
    USGSTopographicMapsLayer.xml

    Were added, available upon request. 

2.  ColladaMeshShape class

    The following function was added to the ColladaMeshShape class.
    This fix corrects a kml (collada) model positioning bug in WWJ 1.4.  
    This bug is fixed in WWJ 1.5 and not applicable in earlier versions.

/** {@inheritDoc} Overridden to invalidate cached geometry when the model position is changed. */
   @Override
   public void setModelPosition(Position modelPosition)
   {
       if (modelPosition != this.modelPosition)
       {
           this.modelPosition = modelPosition;
           this.reset();
       }
   }

3.  The BasicSceneController was also extended 
   (mil.navy.nrl.sdt3d.SdtBasicSceneController)
    and its draw method was overridden so that the symbol layer (and others)
	would be drawn last.  This change was necessary so that icons/labels 
	could be seen through 3d objects.

4.  This change is no longer required in the latest wwj distribution. (0.6.538.13577)

Angle::equals bug throwing the Geom.Line.DirectionIsZeroVector exception (in the Line constructor) 
    was fixed per:

http://forum.worldwindcentral.com/showthread.php?t=22535

   public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Angle angle = (Angle) o;

        //noinspection RedundantIfStatement
       // if (angle.degrees != this.degrees)  <- replaced
        //    return false;                   <- replaced 

        // ljt bug fix
        if (Math.abs(angle.degrees - this.degrees) > 1e-9)
            return false;

        return true;
    }
	
	HISTORICAL NOTES:
	
	1.  This following code fix was removed.  (sdt3d now uses the wwj 0.6 icon rendering and 
	layering code.)

    The WWJ IconLayer.java and IconRenderer.java classes were duplicated in the
    sdt3d code set to support the rendering of Icons and Annotations at altitude
    rather than elevation above terrain.  Currently the fix is applied for all
    Icons/Altitudes.  A fix for this in the WWJ code set is due in the next
    WWJ release at such time the code should be reworked to take advantage
    of this new functionality.
    
    For Icons, the drawMany function in the IconLayer was overridden.   Various methods in the 
    IconRenderer were also changed to protected rather than private or to public. (Ordered Icons)

	The IconRenderer in the IconLayer was also changed to protected.
    
    The changes to render annotations at altitude were localized in the following
    function:

    BasicAnnotationRenderer.getAnnotationDrawPoint
    
    TODO: this class still needs to be relocated in sdt3d

Brian Adamson
<adamson@itd.nrl.navy.mil>
