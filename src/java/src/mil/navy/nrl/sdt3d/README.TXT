Copyright (C) 2001 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.

The README.TXT file in the sdt project directory now contains this content.  Check in the
new location for update to date content.

Setting up the sdt3d Eclipse project.

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
	
	Also add the jogl.jar file in the worldwind workspace to the joglutils project.
	
6.  Modify the sdt3d java build path to find the java jre libraries in the appropriate location
    for your system if necessary. (sdt3d project properties java build path libraries section)

7.  Optionally you may check the sdt documenatation and examples directory out of the sdt project.

	https://pf.itd.nrl.navy.mil/svnroot/proteantools/trunk/sdt
	
	Check out the /doc or the /doc/examples folders.


Building SDT3d Jar File Windows OS-X

1.  Use the export JAR file option.
    Include the sdt3d src, protolib-jni src and joglutils src trees.  Disable other content selected by 
    eclipse (e.g. .classpath, jugutils demo etc.)
2.  Check:
	Export generated class files and resources
	Export Java source files and resources
3.  When exporting the jar elect to use the existing manifest.mft in the project, otherwise
    eclipse will overwrite the class path.  This file should contain:
 
 Manifest-Version: 1.0
 Class-Path: protolib.jar protolibJni.jar gluegen-rt.jar jogl.jar world
   wind.jar joglutils.jar
 Main-Class: mil.navy.nrl.sdt3d.sdt3d    

Building the SDT3d Jar File Linux

A waf script is provided in the sdt makefiles directory that will build an sdt3d.jar file 
in the build directory subtree.

make -f Makefile.linux sdt3d

(Note you may need to build the protolib.jnilib "make -f Makefile.linux")

Building the SDT3d Jar File MacOSX

1.  From within Eclipse use the "export" option to export the sdt3d application.
    Eclipse will create a sdt3d.app directory
    
2.  Copy the sdt3d.icns icon file to the Resources directory.

3.  Copy relevant jar and jnilib files to the Resources directory.

Changes to Eclipse Project setup.

1.  To solve the java "flicker" problem add:  

-Dsun.java2d.noddraw=true

to the Sdt3d project configuration Run/Debug Settings for the sdt3d launch configuration settings
(the VM arguments command line)

Changes to WWJ toolkit

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
    
2.  The BasicSceneController was also extended (mil.navy.nrl.sdt3d.SdtBasicSceneController)
    and its draw method was overridden so that the symbol layer would be drawn last.  
    This change was necessary so that icons/labels could be seen through 3d objects.

3.  This change is no longer required in the latest wwj distribution. (0.6.538.13577)


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

Building sdt on macosx

1.  You will need to compile wxWidgets

	Modify the configure file to build with the i386 arch option

                 OSX_UNIV_OPTS="-arch i386"

2.  Configure wxwidgets:
    
    ./configure-no86-d.sh --with-osx-cocoa --disable-shared --with-opengl --enable-universal_binary --with-macosx-sdk=/Developer/SDKs/MacOSX10.6.sdk/
    
3.  Compile sdt

	make -f Makefile.macosx sdt
    
Miscellaneous

1.  To bring up the wwj dashboard (statistics panel) use the ctl+alt+shift+left-click in the wwd window

2.  You may increase the airspace geometry cache

in worldwind.src.config.worldwind.xml

gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize is set to 32000000

Supposedly this will help airspace performance - I've increased it to 200000000 
with no real performance increase as far as I could tell (running a very large scenario)  
(tag on the wwj board recommended 100000000)

He also says "You can set the cache size programmatically, but it's better to set it in 
the configuration file because the cache is lazily created by the Airspace code. "
