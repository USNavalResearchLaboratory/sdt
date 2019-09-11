Scripted Display Tool(s)

This project contains source code and build scripts for the "Scripted
Display Tool" (both sdt 2d & sdt3d applications).  The applications
were developed by the Protocol Engineering Advanced Networking
(PROTEAN) Research Group of the Naval Research Laboratory (NRL).

The intent of these applications is to provide tools that support
real-time visualization of mobile communication network field tests,
emulations, or even simulations.  The applications may also prove
suitable for other purposes.


SDT3D

sdt3d is based upon the NASA WorldWind Java (WWJ) SDK available
from:

<http://worldwind.arc.nasa.gov/java/index.html> 

and joglutils available at:

https://kenai.com/projects/jogl/sources/jogl-utils-git

Versions of WWJ amd joglutils modified to work with the latest SDT3D
code are co-hosted with the SDT3D code in the project file release
section.  Details of modifications required to these distributions are
available in the docs directory.

The sdt3d software and documentation are available at:

<http://www.nrl.navy.mil/itd/ncs/products/sdt>

SDT (2D)

sdt is written in C++ using the freely-available, cross-platform 
wxWindows library for graphical user interface applications.

The software and documentation are available at:

<http://www.nrl.navy.mil/itd/ncs/products/sdt>


BUILDING

See the sdt documentation in the docs directory for the latest build 
instructions for both sdt and sdt3d or the README-SDT3D.txt and
README-SDT.txt files located in the src distribution.

TO DOWNLOAD THE LATEST SRC TREE:

If you have developer access, check out the latest sdt3d project out of 
SVN from our "pf.itd.nrl.navy.mil" server:

sdt3d : SVN root = "/svnroot/sdt/trunk", module = "sdt"

Otherwise get the nightly build from:

http://downloads.pf.itd.nrl.navy.mil/sdt/

Changes included in Release 2.2

 1. Sdt version updated to 2.2 as the performance enhancements are significant.

2. Multiple input flows (including multiple TCP clients) can now be successfully inteleaved.

   A gentleman's agreement to not clobber link state between two input feeds should be enforced.

3. Command logging

   logDebugOutput on,<fileName>

   If no fileName provided output will go to sdtout.

   Toggling logDebugOutput off from the file menu will disable any file logging.

   Toggling logDebugOutput on from the file menu will enable logging to sdtout as does the command line argument with no filename.

   If <fileName> exists, <fileName> will be incremented, e.g. <filename>.n

   Care should be taken when resetting sdt to clean up any obsolete debug files established in the sdt settings or configuration files.

   If no path provided, <fileName> will be colocated with the sdt3d app.

   The "wait" command interval is the delta between key value command pairs, irregardless of thread.  The "wait" command is not logged until the interval between commands is greater than 100 milliseconds.

   "Wait" commands are first logged when the first input command is received by any thread.  Depending on when scenario events are initially logged, it may be necessary to remove long wait intervals logged before the scenario began running.

	 
4.  “Hard” and “soft" reset menu option.

    A “soft" reset

      will disable debug logging

      remove user defined layers

      remove all nodes, regions, tiles, linkLables, kml files, and elevation tiles

      reload any configuration file.

    A "hard" reset will perform a soft reset and:

      shutdown any udp/tcp sockets

      stop reading any input file

      reset system state (offlineMode off, elevationData on, stereoMode off, background color, uncollapse links, unfollow any nodes, disable node focus, restart the view controller, and reset to basicOrbitVies)

      reload the any user preferences file

    Note the distinction between the user perferences file (sdt.settings generally) and the user configuration.  Any settings that should be applied only during a hard reset should be in the sdt.settings file while "reset" like commands should be in the user configuration file.

5. The "remove user defined layers" menu option will now clear all renderables from all user defined layers (useful in the event of dangling links or markers).

6.  Performance efficiencies:

    link line positions will now only be recalculated when a node position or link attribute changes.
    
    link line positions will only be recalculated for displayed links.

    the node render function (called during each rendering pass) will now only render visibible nodes that have had a node attribute change or a link attribute change.

    wwj redraw bug fixed.  Redraw is now done only at 100 ms POLL_INTERVAL

7.  Sdt node and link object attribute changes will be applied immediately (no position update requried).

8.  Lines can now be stippled.

    line <color>,<lineWidth>,<opacity>,<stippleFactor>,<stipplePattern>

    Stipple Factor: stipple pixle value.  use 0 for no stipple.  Default 0
	    
    Stipple Pattern: Numeric or hex, e.g. 0xABAB or 43947.  Default 0xAAAA

9.  Gazette is disabled by default.

10.  Compass and worldwind view controls can now be toggled on and off by layer checkbox and layer command, e.g. layer "Worldwind::Compass,off"

11. Tcp connection via menu item no longer required confirmation of success.

12. New eclipse formatter included in distribution.  All classes reformatted to new format.

13. Misc bug fixes:

    Marker heading bug fixed.

    Link initial hangle changed in support of many links.  Over threshold, links limited to 5 degrees aroudn radius.

    User defined directional links bug fixed.

    linkId hashMap now TreeMap

14. User defined links distinguished from sdt layers.  User defined checkboxes added.

15. TODO:

   complete implementation of separation of user defined layers and sdt layers (e.g. removing user
defined layers removes node and links from sdt layers) Other bugs.  Currently only user defined links are distinct from sdt link layer.  Do we need to support other objects likewise?  E.g. nodes, symbols etc.

   Occasional link flicker problem

   create new distributions

   test out multi-frame support

   checkbox tree improvements

   true "sdt player"

   multi-color line stipple

   update documentation

   add ability to set log file name from gui.



Updates in Release 2.0 include:

kml models can be assigned to sprites

kml models can be loaded and positioned independently of sprites (e.g. buildings/cities)

symbol cone - defines the new cone symbol

symbol cylinder - defines the new cylinder symbol

orientation can be set for a node (pitch, yaw[a|r], roll)

pos <lat,lon,[alt],[g|c],[msl|agl] - objects can be positioned at geodesic and
cartesian coordinates and at MSL or AGL

origin <coords> - geodesic coordinate from which to offset cartesian
coordinates

symbol sphere,x,x,300s,300s - new "scaleable" fixed size symbols.  

symbols can be oriented.  Symbol orientation can be relative to a node's orientation or absolute.

symbol <symbolType|none>[,<color>,[<thickness> [,<x_radius[s]>[,<y_radius[s]>[,<opacity>[,<scale> [<orientation[a|r]>,[<elevation>]]]]]]]] - expanded symbol attribute list 

path <imageFilePath> - sets directory prefix to search for files

instance <instanceName> - overrides the default pipe name of "sdt"

region <regionName> <attributes> - creates a region object

listen [udp,|tcp,][off|<addr>/]<port> - directs sdt3d to listen to the
given udp port, multicast address, or tcp port

link <node1>,<node1>[,<linkID|all>[,<dir,all>]] - multiple directed or
bidirectional links are now supported

link color thickness and opacity can be set

links can be assigned a linkId

multiple links can be collapsed from the view menu

colors can be specified in any rgb or hex value

delete <objectType|all>,<objectName|all>

clear <objectType|all>

listen [udp|tcp],[off|<addr>/]<port> - tcp support

follow <nodeName> - the view can be told to "follow" specified nodes

lookat <attrs> - sets the camera position

kml - limited kml standard supported (tour)

geoTiff - image or elevation geoTIFF overlay

tile <imageTile> <attributes> - overlays a specified image over terrain

popup - popup window attributes

user defined layers now supported

nodes, symbols, layers, links, regions, and tiles can be assigned to
user defined layers

user defined layers can be nested

views can be bookmarked and saved to disk for subsequent reload

flat earth view support

background color can be changed (useful for non-geodesic views)

user preferences file available (e.g. path, load default icons, set
view)

user Configuration File loaded upon system reset

elevation model (terrain) can be disabled

Open Street Map layer available

Stereoscopic viewing mode available

See the documentation for the full command set.

Commands not yet supported are simply ignored.
