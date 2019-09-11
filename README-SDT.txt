The Scripted Display Tools are open source software developed 
by the Naval Research Laboratory (NRL) PROTocol Engineering 
Advanced Networking (PROTEAN) group. SDT provides a simple 2D 
visualization capability using standard image files for a 
background and set of overlaid nodes. SDT3d provides a 3D 
visualization capability using Nasa's World Wind 3D interactive 
world viewer and set of overlaid nodes.  See the README-SDT3d.txt
file for details on SDT3d.

sdt - "The Scriptable Display Tool"

The sdt program is designed to provide realtime and
non-realtime  visualizations.  The command set allows a
background image to be specified and one or more "sprite"
images to be specified. After "sprite" images have been
specified, "nodes" may be specified  (each node with a
unique name) and assigned a sprite "type" (By default, a
new node will be assigned the default sprite type, which
is the _first_ sprite specified in the script).

Nodes can be placed on top of the background image and 
moved about the screen by specifying their position. 
Optionally, "links" represented by lines drawn connecting
the nodes, can be specified as desired as well.  By default,
the nodes displayed are labeled with their node name, but
this label can be suppressed as well.

Commands may be given to "sdt" on the command-line and/or
piped into "sdt" via stdin.  

The sdtcmd.exe utility may also be used to send real time 
commands over the sdt pipe, e.g.:

sdtcmd.exe node node01 symbol circle

By clicking on the image and dragging up or down, the background
image can be scaled in size.

TO BUILD:

To build "sdt", it is required that the wxWindows graphical
user interface  development header files and libraries are
installed on your system.  WxWindows is a freely available,
cross-platform, C++ graphical user interface programming
API and library.  It is available for a wide array of
platforms freely available at:

http://www.wxwindows.org/

The current release uses wxWidgets 2.8. Previous releases (1.1a1 and
above) use wxWidgets 2.6.x. sdt releases 1.0a1-1.0a8 use wxWidgets
2.4.x.

Please note that binary releases of wxWidgets do not include the
header files necessary to compile sdt, so you must download and build
the source code. Please see the wxWidgets readme for instructions. It
is recommended to compile wxWidgets using the "--disable-shared"
config option so that the binary may be moved to machines that do not
have wxWidgets installed.

Sdt also requires the protolib src code available on NRL's 
protean forge web site.  If you have developer access check 
protolib out of SVN directly:

SVN root = "https://pf.itd.nrl.navy.mil/svnroot/protolib"

Check out the /trunk folder and name it /sdt/protolib.  If you
don't have svn access you may get the nightly build 
available at:

http://downloads.pf.itd.nrl.navy.mil/proteantools.

BUILDING SDT ON WINDOWS : 

Microsoft Visual Studio project files are
available in the distribution in the makefiles/win32 directory. Load
the sdt solution file into Microsoft Visual Studio. Project files are
available that build sdt and sdtcmd.

BUILDING SDT ON LINUX :

To build under linux use the following command(s) from the 
makefiles directory:

make -f Makefile.linux sdt
make -f Makefile.linux sdtcmd

Note that "make -f Makefile all" will make sdt,sdtcmd, and sdt3d.  See
README-SDT3D.txt for sdt3d dependencies.

See the documentation for detailed directions
on building the application under windows.

BUILDING SDT ON MAC OSX

1.  You will need to compile wxWidgets

	Modify the configure file to build with the i386 arch option

                 OSX_UNIV_OPTS="-arch i386"

2.  Configure wxwidgets:
    
    ./configure-no86-d.sh --with-osx-cocoa --disable-shared --with-opengl --enable-universal_binary --with-macosx-sdk=/Developer/SDKs/MacOSX10.6.sdk/
    
3.  Compile sdt

	make -f Makefile.macosx sdt
   

Sdt should be fairly portable to any platform which is
supported by wxWindows. At this time, the code has been built
and tested in Windows, MacOS X and Linux GTK wxWindows 
environments. 

THE SDT SCRIPT COMMAND SET:

Note that you should probably check the sdt.html file in the doc
directory for the most up-to-date information...

===============================
BACKGROUND COMMANDS

Specify the background image:

bgimage <imageFile>

When a background image is set, the background size
will adopt that of the background image file)

--------------------------------
Set background size:

bgsize <width>,<height>

(Setting either <width> or <height> to -1 will cause the
background image's proportions to be preserved.

--------------------------------
Scale background size

bgscale <scalingFactor>

--------------------------------
Setting coordinate system bounds:

bgbounds <left>,<top>,<right>,<bottom>

These bounds are mapped to the corresponding
corners  of the background image.

================================
SPRITE COMMANDS

Create/set "current sprite" by name

sprite <spriteName>

Associate image with "current sprite"

image <imageFile>

Set "current sprite" size

size <width>,<height>

(Setting either <width> or <height> to -1 will cause the
sprite image's proportions to be preserved.

Scale "current sprite" size

scale <scalingFactor>

================================
NODE COMMANDS

Create/set "current node" by name

node <nodeName>

Assign sprite type to "current node"

type <spriteName>

Set nodes position

position <x>,<y>

Set node label

label on|off

================================
LINK COMMANDS

link <node1>,<node2>[,<linkId|all>,[<dir|all>]] [line <color>,<thickness>]

unlink <node1>,<node2>

================================
OTHER COMMANDS

input <scriptFile>

Causes sdt to load and parse the indicated text file
containing sdt commands.  This can be used as a config
file for sdt.

wait <msec>

Indicates that "sdt" should delay before parsing
further script input.

Example script
--------------

bgimage earth.jpg bgscale 0.5
bgbounds 0,0,600,600
sprite car image car.gif
sprite truck image truck.gif
node alpha type car 
node beta type truck
node alpha pos 100,100
node beta pos 200,200
link alpha,beta

================================
LEFT DOUBLE-CLICK

While the SDT window is up, you can click at anytime on any
node/text field and a short message will be written to 
stdout.  The message format is as follows:

node <nodename> doubleclick
============================

Example Double-Click Message
----------------------------

node car1 doubleclick
node clock doubleclick
node bob doubleclick

===============================
PICTURE RESIZING

The image can now be resized to either auto-fit to it's
maximum possible size on that window, or resized to fill
the window.  This can be done through the Options folder
of the Menu, or by using hot keys:


CTRL-F for Fill Screen
CTRL-S for Auto-Fit


Please provide any questions or comments to:

Brian Adamson
<adamson@itd.nrl.navy.mil>

