# Example to show using sdt draw utils library from sdt/utils
# with a graphml file that has lat,long,altitude

import socket,optparse
import networkx as nx
import sys,time
import numpy as np

from sdt_draw_utils import draw_sdt_nx,draw_sdt_nx_nodes,draw_sdt_nx_edges,set_status

def main(argv):
    usagestr = "usage: %prog [-h] [options] [args]"
    parser = optparse.OptionParser(usage = usagestr)
    parser.set_defaults(addr = "127.0.0.1", port=50000, thresh=0.9)

    parser.add_option("-a", "--ipaddress", dest = "addr", type = str,
                      help = "addr")
    parser.add_option("-p", "--port", dest = "port", type = int,
                      help = "port number")
    parser.add_option("-t", "--threshold", dest = "thresh", type = float,
                      help = "link quality threshold")

    def usage(msg = None, err = 0):
        sys.stdout.write("\n")
        if msg:
            sys.stdout.write(msg + "\n\n")
        parser.print_help()
        sys.exit(err)

    # parse command line options
    (options, args) = parser.parse_args()
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    except socket.error, msg:
        print("[ERROR] %s\n" % msg[1])
        sys.exit(1)
# 
    try:
        sock.connect((options.addr, options.port))
    except socket.error, msg:
        sys.stderr.write("[ERROR] %s\n" % msg[1])
        sys.exit(2)


# Get some networkx graph
    gfile = "JavaScenarioprocessed00000.graphml"
    G = nx.read_graphml(gfile)
# Since all edges exist but with potentially very low quality filter the edges
# H will be a version of graph with edges above a threshold
#    print G.edges(data=True)    # this should show all possible edges
    print "number of unfiltered edges :" + str(len(G.edges()))
# keep minimum of two directed edges
    for n1,n2,d in G.edges_iter(data=True):
        if d['linkSuccessRate']<options.thresh:
            G.remove_edge(n1,n2)
    G=G.to_undirected(reciprocal=True)
    print "number of filtered edges :" + str(len(G.edges()))
    H=G
    longitude = nx.get_node_attributes(H,'location_longitude').values()
    latitude = nx.get_node_attributes(H,'location_latitude').values()
    altitude = nx.get_node_attributes(H,'location_z').values()
# add an altitude scale so we can see better
    a_scale = 100.0
    altitude = [(a * a_scale) for a in altitude]
# put the pos data together
    pos = zip(longitude,latitude,altitude)
# Because node names from dictionary maybe in different order we need to sort
    bc = []
#    bc_dict = nx.current_flow_betweenness_centrality(H)
    bc_dict = nx.betweenness_centrality(H,weight='linkSuccessRate')
    for node in H.nodes():
        bc.append(bc_dict[node])
    newbc = []
    newbs = []
#map to normalize values between 0-1 for colors
    for val in bc:
        newbc.append(np.interp(val,[min(bc),max(bc)],[0,1]))
#map to range 10-200 for size
# set min and max scales for size
    min_size = 2000
    max_size = 5000
    for val in bc:
        newbs.append(int(np.interp(val,[min(bc),max(bc)],[min_size,max_size])))

# Setup layers and clear things in sdt    
    sock.sendall('clear all ')
    sock.sendall('layer "All Layers::Sdt,off" ')
    sock.sendall('layer "Sdt::Node Symbols" ')
    sock.sendall('layer "Sdt::Network Links" ')
    sock.sendall('layer Worldwind,off ')
    sock.sendall('layer "All Layers::Worldwind::Atmosphere" ')
    sock.sendall('layer "All Layers::Worldwind::Blue Marble Image" ')
    sock.sendall('layer "All Layers::Worldwind::MS Virtual Earth Aerial" ')
#    sock.sendall("follow all ")
#    sock.sendall("flyto 102.51,-4.50,300000 ")
    sock.sendall("lookAt 102.51,-4.50,300000,10,70 ")
   
    draw_sdt_nx(sock,H,pos,geoPos=True,
            node_color=newbc,
            node_size=newbs,
            alpha=1.0,
            edge_color="white",
            linewidths=1,
            l_alpha=0.1)
    sock.sendall('title "3D graphml test with centrality" ')  #    sock.close()
    

if __name__ == '__main__':
    sys.exit(main(sys.argv))
