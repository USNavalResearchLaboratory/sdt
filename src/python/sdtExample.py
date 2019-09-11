import sdt
import time
import subprocess


# start sdt3d helper class
sdtApp = sdt.Sdt3d()

# start a sdt3d app
#sdtApp.startSdt3d()

# Wait for socket to get set up, the connect code isn't working as expected
# it seems if the connection is rejected we can't wait and try to connect
# again, even if we close and reopen the socket.
# time.sleep(5)
# connect to sdt3d app
sdtApp.connect()

sdtApp.sendCmd("status testing")
sdtApp.sendCmd(" bgbounds -77.028633,38.828533,-77.003298,38.817720")

# build a node
node = sdt.Node("ljt")
node.position = sdt.Position("-77.005610","38.824472")
node.symbol = sdt.Symbol("sphere")
node.symbol.color = "Blue"
node.orientation = sdt.Orientation(0,0,0)

# build a node
node2 = sdt.Node("ljt2")
node2.position = sdt.Position("-77.003610","38.824472")
node2.symbol = sdt.Symbol("sphere")
node2.symbol.color = "Red"
node2.orientation = sdt.Orientation(0,0,0)

link = sdt.Link(node.name,node2.name,"eth1")

sdtApp.sendCmd(node.format())
sdtApp.sendCmd(node2.format())
sdtApp.sendCmd(link.format())