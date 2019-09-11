Scripted Display Tool(s)

This project contains source code and build scripts for the "Scripted
Display Tool" (both sdt 2d & sdt3d applications).  The applications
were developed by the Protocol Engineering Advanced Networking
(PROTEAN) Research Group of the Naval Research Laboratory (NRL).

The intent of these applications is to provide tools that support
real-time visualization of mobile communication network field tests,
emulations, or even simulations.  The applications may also prove
suitable for other purposes.

SDT (2D)

sdt is written in C++ using the freely-available, cross-platform 
wxWindows library for graphical user interface applications.

Documentation is available at:

<http://cs.itd.nrl.navy.mil/work/sdt/index.php>

SDT3D

sdt3d is based upon the NASA WorldWind Java (WWJ) SDK available
from:

<http://worldwind.arc.nasa.gov/java/index.html> 

and joglutils available at:

https://github.com/sgothel/jogl-utilswpr 

This application uses the simple visualization scripting language of
the original 2-dimensional Scripted Display Tool (sdt) as described
at:

<http://cs.itd.nrl.navy.mil/work/sdt/index.php>

BUILDING

See the sdt documentation in the docs directory for the latest build 
instructions for both sdt and sdt3d or the README-SDT3D.txt and
README-SDT.txt files colocated with this file.

TO DOWNLOAD THE LATEST SRC TREE:

If you have developer access, check out the latest  sdt3d project out of 
SVN from our "pf.itd.nrl.navy.mil" server:

sdt3d : SVN root = "/svnroot/proteantools/trunk", module = "sdt"

Otherwise get the nightly build from:

http://downloads.pf.itd.nrl.navy.mil/proteantools/


Some added "newer" commands in Release 1.2 include:

path <imageFilePath> - sets directory prefix to search for files
instance <instanceName> - overrides the default pipe name of "sdt"
region <regionName> <attributes> - creates a region overlay
listen <udpPort> - directs sdt3d to listen to the given udp port
link <node1>,<node1>[,<linkID|all>[,<dir,all>]] - multiple directed or
bidirectional links are now supported
symbol <symbolType>[,<color>[,<thickness>[,x_radius[,y_radius>[,opacity]]] - sym
bols can be associated with nodes
delete <objectType|all>,<objectName|all>
clear <objectType|all>
tile <imageTile> <attributes> - overlays a specified image at the given
lat lon coordinates
pos <lon>,<lat>,<agl|msl> - nodes can be positioned at MSL or AGL
follow <nodeName> - the view can be told to "follow" specified nodes 


Commands not yet supported are simply ignored.

