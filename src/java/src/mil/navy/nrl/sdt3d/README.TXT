Copyright (C) 2001 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.

2.1 NOTES

1.  Requires WWJ 2.1

2.  WWJ 2.1 should still be patched as described in 2.0 notes or can manually be patched as 
	described below in step 5.

3.  You may need to add javac to the worldwind/build.xml file after downloading worldwind.zip 
	from the file release section.

<project name="worldwind" default="build" basedir=".">
    <description>
        Build script for World Wind Java. Assembles and tests the World Wind source code, creates World Wind API
        documentation, and bundles World Wind library JAR files.
    </description>
    <javac scrdir="src" destdir="bin" />  <----

4. modify build.properties jdk to your jdk (e.g. 1.7, 1.8) (if jre is used may not be compatable with
	command line java.

5. If not using wwj from file release, get worldwind 2.1 from nasa site and modify
	gov.nasa.worldwind.ogc.kml.impl.KMLModelPlacemarkImpl.java change getColladaRoot to public

6. on windows both the sdt and sdtCmd must be run as administrator.  Use visual studio to build sdtcmd.

	both sdtcmd and sdt must be built for the same architecture (x86 or x64)

2.0 NOTES

1. This release requires a modified version of WWJ 2.0 hosted in the file release section.

The KMLModelPlacemarkImpl::GetColladaRoot function has been changed to "public".  To generate 
a new worldwind.jar file containing this patch run the build.xml ant script included in the
WWJ distribution.

2. This release requires joglutils.zip hosted in the file release section.

This release is a based on the joglutils tree hosted at 

https://kenai.com/projects/jogl/sources/jogl-utils-git/show

and has been modified to work with the latest wwj jogl 2.0 code.  A svn patch of the modifications
is included in the sdt docs directory.


The README-SDT3d.TXT file in the sdt project directory contains the latest build instructions.  Check in the
new location for the latest content.

1.2 NOTES


SETTING UP THE SDT3D ECLIPSE PROJECT.

1.  Use SVN to load sdt3d into Eclipse.  sdt3d java src files are available in the proteantools sdt project 
	on the protean forge website:
	
	https://pf.itd.nrl.navy.mil/svnroot/proteantools/trunk/sdt
	
	Check out the /src/java folder and name it sdt3d.
	
2.  Download the joglutils project from the latest sdt3d file release on the protean forge website 
    into the eclipse workspace directory.  This should be relative to the sdt3d project
    and be called joglutils.  Load the project into svn using the build.xml file located in the 
	joglutils make directory.
	
3.  Use SVN to load protolib-jni into Eclipse available on the protean forge website.

	https://pf.itd.nrl.navy.mil/svnroot/protolib
	
	Check out the /trunk/src/java folder and name it protolib-jni

4.  Download the worlwind tarball in the latest sdt3d file release on the protean forge website
	to the eclipse workspace directory.  Unzip the worldwind project relative to the sdt3d project.
	Import the project using the build.xml ant file in the worldwind directory.

5.  Modify the joglutils java build path to find the java jre (rt.jar) library in the appropriate
    location for your system.  (Add the jar file to the jogutils project properties java build path 
	libraries section)
	
	Also add the jogl and gluegen jar files in the worldwind workspace to the joglutils project.
	
6.  Modify the sdt3d java build path to find the java jre libraries in the appropriate location
    for your system if necessary. (sdt3d project properties java build path libraries section)

7.  Optionally you may check the sdt documenatation and examples directory out of the sdt project.

	https://pf.itd.nrl.navy.mil/svnroot/proteantools/trunk/sdt
	
	Check out the /doc or the /doc/examples folders.

8.  In the Kepler Eclipse environment it may be necessary to change the joglutils 
	project properties Resources->Linked Resources-> pathnames from joglutils to
	<eclipseProjectDirectoryForYourEnvironment>/joglutils

9.	On some linux and windows platforms, gdal.jar and worldwindx.jar might need to be added
	to the sdt3d project jar references.  (gdal.jar is released with worldwind)
	
10. For WWJ 1.05 On 64-bit linux platforms unzip the gluegen-rt-natives-linux-amd64.jar and 
	jogl-natives-linux-amd64.jar files in the worldwind directory.
	
11. For WWJ 2.0 on windows, when generating the jar file - use the included manifest file
	as appropriate for your windows version (amd64 or i586) and copy the appropriate 
	amd64 or i586 jar files to the sdt3d jar file directory in the distribution directory.
	
12. You may need to change the java compiler compliance settings from 1.5 to 1.6 if you 
	are getting override superclass method errors.  (project, java settings, compiler
	settings)

BUILDING SDT3D JAR FILE WINDOWS 

1.  Use the export JAR file option.
    Include the sdt3d src, protolib-jni src and joglutils src trees.  Disable other content selected by 
    eclipse (e.g. .classpath, jugutils demo etc.)
2.  Check:
	Export generated class files and resources
	Export Java source files and resources
3.  When exporting the jar elect to use the existing manifest.mft in the project, otherwise
    eclipse will overwrite the class path.  This file should contain:
 
4.  When generating the jar file - modify the manifest.txt file
   	as appropriate for your windows version (amd64 or i586) and copy the appropriate 
   	amd64 or i586 jar files to the sdt3d jar file directory in the distribution directory.
 
 Manifest-Version: 1.0
 Class-Path: protolib.jar protolibJni.jar gluegen-rt.jar jogl.jar world
   wind.jar joglutils.jar
 Main-Class: mil.navy.nrl.sdt3d.sdt3d    

BUILDING THE SDT3D JAR FILE LINUX

A makefile script is provided in the sdt makefiles directory that will build an sdt3d.jar file 
in the build directory subtree.

make -f Makefile.linux_<os> sdt3d


BUILDING THE SDT3D JAR FILE MACOSX

A makefile script is provided in the sdt makefiles directory that will build an sdt3d.jar
application in the build directory subtree.  (Note that XCODE is required)

make -f Makefile.macos sdt3d.app

Alternatively:

1.  From within Eclipse use the "export" option to export a macosx application bundle.
    Eclipse will create a sdt3d.app directory
    
2.  Copy the sdt3d.icns icon file to the Resources directory.

3.  Copy macosx-universal jar files from the wwj directory and protolib jnilib files to the 
	applications Contenst/Java/Resources directory.


CHANGE TO ECLIPSE PROJECT SETUP

1.  To solve the java "flicker" problem add:  

-Dsun.java2d.noddraw=true

to the Sdt3d project configuration Run/Debug Settings for the sdt3d launch configuration settings
(the VM arguments command line)

CHANGES TO WWJ 2.0 toolkit

1.0 setColladaRoot changed to public

Change required to set the position, heading, scale etc of kml objects.

diff -r worldwind-1.4-tmp/src/gov/nasa/worldwind/ogc/kml/impl/KMLModelPlacemarkImpl.java worldwind/src/gov/nasa/worldwind/ogc/kml/impl/KMLModelPlacemarkImpl.java
123c123
<     protected void setColladaRoot(ColladaRoot root)
---
>     public void setColladaRoot(ColladaRoot root)
139c139
<     protected ColladaRoot getColladaRoot()
---
>     public ColladaRoot getColladaRoot()
:

CHANGES TO WWJ 1.4/1.5 toolkit

1.0  ColladaMeshShape class

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

2.0 IconRendered drawIcon method overridden in sdstIconRenderer to no apply screen
	offset to icons not positioned at terrain.
	
	Changed uicon.point and uicon.icon to public from protected.
	
	Change visibility of import gov.nasa.worldwind.render.IconRenderer.OrderedIcon; 
	to public
	
	(in wwj 1.5)

3.0  This following code fix to WWJ was removed

	(sdt3d now uses the wwj 0.6 icon rendering and layering code.)

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

WWJ EXTENSIONS:

1.0  The BasicSceneController was also extended (mil.navy.nrl.sdt3d.SdtBasicSceneController)
    and its draw method was overridden so that the symbol layer would be drawn last.  
    This change was necessary so that icons/labels could be seen through 3d objects.

2.0  See the code for other extensions

MISCELLANEOUS

1.  To bring up the wwj dashboard (statistics panel) use the ctl+alt+shift+left-click in the wwd window

2.  You may increase the airspace geometry cache

in worldwind.src.config.worldwind.xml

gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize is set to 32000000

Supposedly this will help airspace performance - I've increased it to 200000000 
with no real performance increase as far as I could tell (running a very large scenario)  
(tag on the wwj board recommended 100000000)

He also says "You can set the cache size programatically, but it's better to set it in 
the configuration file because the cache is lazily created by the Airspace code. "

3.  To add open street map to new versions of WWJ copy config/Earth/OpenStreetMapHybridLayer.xml
    to config/earth.  Modify worldwind.layers.xml to href this xml actuate onRequest
    
    USGSDigitalOrthoLayer.xml appears to no longer be available.  Worth another test at
    another time.  This layer will need to manually added to 

4.  To add subversion to eclipse 

4a. mars

Within Eclipse install new software:

get subversion from the collaboration group at:

http://download.eclipse.org/releases/mars

get subversive connectors from www.polarion.com

http://community.polarion.com/projects/subversive/download/eclipse/5.0/update-site/

Supposedly you just need the svnkit but that didn't work so I ended up 
getting everything and the Eclipse sorted out what I needed.

4b. older versions

eclipse/help/add from http://subclipse.tigris.org/upate_1.0.x

select subclipse plugin

5.  May need to add java to system path under windows

e.g. add path to javaw.exe to path environment variable

6. The worldwind zip file in sdt3d file releases contains eclipse references to local class path - should fix (release 1.2)

7. http://help.eclipse.org/juno/topic/org.eclipse.jdt.doc.user/reference/ref-properties-build-path.htm

Change eclipse to use java system library rather than deprecated jre

8. wwj 1.5 requires jsse.jar add as jar file in wwj prefs from in 
  /system/library/java/javavirtualmachines/1.6.0.jdk/Contents/Classes/jsse.jar
  
9.  Changed the joglutils LoaderFactory::determinFileType function to ignore case on 3ds & obj comparisons

    /**
     * Parses the file suffix to determine what file format the model is in.
     *
     * @param path File path info
     * @returns int Constant indicating file type
     */
    private static int determineFiletype(String path) {
	int type = FILETYPE_UNKNOWN;
	String tokens[] = path.split("\\.");	
	
	if(tokens[tokens.length - 1].equalsIgnoreCase("3ds"))
	    type = FILETYPE_3DS;
	else if(tokens[tokens.length - 1].equalsIgnoreCase("obj"))
	    type = FILETYPE_OBJ;
	    
	return type;
    }
    
10.0  Joglutils-2.0 need gluegen-rt for buffer factory.  

11.0 joglutils-2.0 was modified to work with WWJ 2.0 (as this version moved to jogl 2.0)
	Based on joglutils source hosted at:

https://kenai.com/projects/jogl/sources/jogl-utils-git/show/src/net/java/joglutils?rev=a33fade58b3cf22b72241767e6615042a5ed22c1


12.0  On certain OSX implementations there is some conflict after upgrading from
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

13.0 wwj 2.1 build.xml does not define javac any longer.  Add a javac directive to worldwind/build.xml in order to import the project into Eclipse.

<project name="worldwind" default="build" basedir=".">
    <description>
        Build script for World Wind Java. Assembles and tests the World Wind source code, creates World Wind API
        documentation, and bundles World Wind library JAR files.
    </description>
    <javac srcdir="src" destdir="bin" />  <---------

