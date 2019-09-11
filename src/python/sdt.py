import subprocess 
import socket
import time
import sys
class Position:
    def __init__(self, lat, lon,alt="X",cartesian="X",agl="X"):
        """Instantiate a position
        """
        self.lat = lat
        self.lon = lon
        self.alt = alt
        self.cartesian = cartesian
        self.agl = agl

    def format(self):
        cmd = " pos %s,%s,%s,%s,%s" %(self.lat, self.lon, self.alt, self.cartesian,self.agl)
        return cmd

class Orientation:
    def __init__(self,pitch="X",yaw="X",roll="X"):
        self.pitch = pitch
        self.yaw = yaw # yaw [a|r] for now
        self.roll = roll
        #self.absolute = "X"

    def format(self):
        return "orientation %s,%s,%s " %(self.pitch, self.yaw,self.roll)

class Symbol:
    def __init__(self, symbolType,color="X",thickness="X",x_radius="X",y_radius="X",
                 opacity="X",scale="X",orientation="X",elevation="X"):
        """Instantiate a symbol
        """
        self.type = symbolType
        self.color = color
        self.thickness = thickness
        self.x_radius = x_radius # x_radius[s] for now
        self.y_radius = y_radius # y_radius[s] for now
        self.opacity = opacity
        self.scale = scale
        self.orientation = orientation #cyclinder and cone only orientation[a|r] for now
        #self.absolute = False # orientation relative to nodes' heading?
        self.elevation = elevation

    def format(self):
        cmd = "symbol %s,%s,%s,%s,%s,%s,%s,%s,%s" %(self.type,
                                                     self.color,
                                                     self.thickness,
                                                     self.x_radius,
                                                     self.y_radius,
                                                     self.opacity,
                                                     self.scale,
                                                     self.orientation,
                                                     self.elevation)

        return cmd
        
class Node:
    def __init__(self, name):
        """Instantiate a sdt.node
        The 'instance' name is rquired.
        """
        self.name = name
        self.type = None
        self.position = None
        self.orientation = None
        self.label = None # on|color|off,text for now
        self.symbol = None
        self.nodeLayer = None
        self.symbolLayer = None
        self.labelLayer = None
    

    def format(self):
        cmd = "node %s" %(self.name)
        varList = vars(self)
        for var,val in varList.iteritems():
            # We need node as the first item
            if var == 'name':
                continue
            if val is None:
                continue
            # TODO: find a better way to determine is class instance?
            if (hasattr(val,'__dict__')):
                cmd = "%s %s" %(cmd,val.format())
            else:
                cmd = "%s %s %s" %(cmd, var, val)
        return cmd

class Link:
    def __init__(self,node1,node2,linkID="X",dir="X"):
        """
        :param node1:
        :param node2:
        :return:
        """
        self.node1 = node1
        self.node2 = node2
        self.linkID = linkID
        self.dir = dir

    def format(self):
        cmd = "link %s,%s,%s,%s" %(self.node1,self.node2,self.linkID,self.dir)
        return cmd

class Sdt3d:
    def __init__(self, ip="127.0.0.1", port="50000"):
        """
         Startup a sdt3d app
        """
        self.ip = ip
        self.port = port
        self.sdtSock = None
        self.sdtApp = None

    def startSdt3d(self,script="/usr/local/bin/sdt3d.sh"):
        self.sdtApp = subprocess.Popen(script)

    def connect(self,ip="127.0.0.1",port="50000"):
        """ Try to connect to a sdt3d application
        """
        if self.sdtSock == None:
            try:
                self.sdtSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            except socket.error, errmsg:
                print 'Failed to create socket.  Error %s' % errmsg
                sys.exit()
        i = 0
        while (i < 5):
            i += 1
            try:
                self.sdtSock.connect((self.ip,int(self.port)))
                # is it really true this is the only way to tell if the socket connected?
                if self.sdtSock.sendall(" "):
                    return self.sdtSock
                else:
                    print "Socket connect failed, attempting reconnect in five seconds"
                    time.sleep(5)
            except socket.error, errmsg:
                if (errmsg.errno == 56): # already connected
                    return self.sdtSock
                else:
                    print "Socket connect failed %s, attempting reconnect in five seconds" %(errmsg)
                    time.sleep(5)
        else:
            return self.sdtSock


    def sendCmd(self,cmd):

        if not self.sdtSock is None:
            try:
                print cmd
                self.sdtSock.sendall(' ' + cmd + ' ' )
            except socket.error, errmsg:
                print 'sendCmd() failed Error: %s' %errmsg
                sys.exit()
        else:
            print "Sdt3d socket not connected"