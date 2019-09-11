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


Some added "newer" commands in Release 2.0 include:

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
