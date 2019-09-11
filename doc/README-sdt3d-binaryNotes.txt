Scripted Display Tool - 3D

This project contains the "Scripted Display Tool - 3D" (sdt3d)
application.  The application was developed by the Protocol
Engineering Advanced Networking (PROTEAN) Research Group of the Naval
Research Laboratory (NRL).

The intent of this application is to provide a tool that supports
real-time visualization of mobile communication network field tests,
emulations, or even simulations.  The application may also prove
suitable for other purposes.

This tool is based upon the NASA WorldWind Java (WWJ) SDK available
from:

<http://worldwind.arc.nasa.gov/java/index.html> 

This application uses the simple visualization scripting language of
the original 2-dimensional Scripted Display Tool (set).  The latest
command set definition is described at:

http://pf.itd.nrl.navy.mil/sdt/sdt.html

USING:

The "sdt3d" application listens on a ProtoPipe named "sdt" to which
SDT commands may be sent.  The application can also be told to listen
to a UDP socket for commands or to load an input file containing sdt
commands.

The application can be started using the sdt3d.bat (windows), the mac
osx sdt3d application, or the sdt3d.sh (linux) script included in the
distribution.

Note that when using the sdt3d.bat windows file, you will need to set
the path to java in the sdt3d.bat file if not already set in your
environment.  Note that the directory must be enclosed in quotes if
there are any spaces, e.g. 

"C:\Program Files (x86)\Java\jre6\bin\java.exe"

SDTCMD Utility:

The sdtcmd utility can be used to send real-time commands to the sdt3d app:

sdtcmd node node01 symbol sphere,blue

Windows (sdtcmd.exe), macosx, and linux (sdtcmd) versions of sdtcmd are provided in the respective binary distributes.

EXAMPLES:

An examples directory containing a few sample sdt scripts files and
imagery is provided.  You may need to change the path in the example
scripts to point to your examples directory.

Some added commands include:

path <imageFilePath> - sets directory prefix to search for files
instance <instanceName> - overrides the default pipe name of "sdt"
region <regionName> <attributes> - creates a region overlay
listen <udpPort> - directs sdt3d to listen to the given udp port
link <node1>,<node1>[,<linkID|all>[,<dir,all>]] - multiple directed or
bidirectional links are now supported
symbol <symbolType>[,<color>[,<thickness>[,x_radius[,y_radius>[,opacity]]] - symbols can be associated with nodes
delete <objectType|all>,<objectName|all>
clear <objectType|all>
tile <imageTile> <attributes> - overlays a specified image at the given
lat lon coordinates
pos <lon>,<lat>,<agl|msl> - nodes can be positioned at MSL or AGL
follow <nodeName> - the view can be told to "follow" specified nodes 

See the documentation for more information
