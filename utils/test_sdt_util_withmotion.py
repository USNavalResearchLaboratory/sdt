import socket
import networkx as nx
import sys,time
import numpy as np
import matplotlib.cm as cmx
import math

from sdt_draw_utils import draw_sdt_nx,draw_sdt_nx_nodes,draw_sdt_nx_edges,draw_sdt_nx_node_labels,draw_sdt_nx_edge_labels,sdt_nx_circular,sdt_nx_random,sdt_nx_spectral,sdt_nx_spring,sdt_nx_shell,set_status



def main(argv):

    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    except socket.error, msg:
        print("[ERROR] %s\n" % msg[1])
        sys.exit(1)
# 
    try:
        sock.connect(("127.0.0.1", 50000))
    except socket.error, msg:
        sys.stderr.write("[ERROR] %s\n" % msg[1])
        sys.exit(2)

    sock.sendall('clear all ')
#    sock.sendall('clear nodes ')    
    sock.sendall('layer Worldwind,off ')
    sock.sendall('layer Sdt::Kml,off ')
    sock.sendall('layer "All Layers::Sdt::Node Labels,off" ')
    sock.sendall('backgroundColor gray ')
    sock.sendall('origin 0.0,0.0,0.0 ')
    sock.sendall('center 0.0,0,0.0,0.0,c ')
    sock.sendall('flyto 0.005,0.005,3000.0 ')
#    sock.sendall("follow all,on ")
#    draw_sdt_nx(sock,G,node_size=20,alpha=0.7,edge_color="blue",width=0.3)
# Get some networkx graph

    G = nx.random_geometric_graph(400,0.2,dim=3)
    set_status(sock,"3D RandomGeometric")     
    pos=nx.get_node_attributes(G,'pos')
    dictbc = nx.betweenness_centrality(G)
    bc = dictbc.values()
    newbc = []
    newbs = []
    import operator
    import random
    maxnode = max(dictbc.iteritems(), key=operator.itemgetter(1))[0]
#map to normalize values between 0-1 for colors
    for val in bc:
        newbc.append(np.interp(val,[min(bc),max(bc)],[0,1]))
#map to range 10-200 for size
    for val in bc:
        newbs.append(int(np.interp(val,[min(bc),max(bc)],[10,100])))
    draw_sdt_nx(sock,G,pos,
                node_color=newbc,
                node_size=newbs,
                alpha=0.4,
                edge_color="blue",
                linewidths=1,
                l_alpha=0.3)

#    sock.close()
    step = 0.03
    while True:
        location = G.node[maxnode]['pos']
        location[0] = location[0] + random.uniform(-step,step)
        if location[0] > 1.0:
             location[0] = 1.0
        elif  location[0] < 0.0:
             location[0] = 0.0    
        location[1] = location[1] + random.uniform(-step,step)
        if location[1] > 1.0:
             location[1] = 1.0
        elif  location[1] < 0.0:
             location[1] = 0.0 
        location[2] = location[2] + random.uniform(-step,step)
        if location[2] > 1.0:
             location[2] = 1.0
        elif  location[2] < 0.0:
             location[2] = 0.0
        G.node[maxnode]['pos'] = location
        pos=nx.get_node_attributes(G,'pos')
        G = nx.random_geometric_graph(400,0.2,pos=pos,dim=3)
        dictbc = nx.betweenness_centrality(G)
        bc = dictbc.values()
        newbc = []
        newbs = []
#map to normalize values between 0-1 for colors
        for val in bc:
            newbc.append(np.interp(val,[min(bc),max(bc)],[0,1]))
#map to range 10-200 for size
        for val in bc:
            newbs.append(int(np.interp(val,[min(bc),max(bc)],[10,100])))
        nbrs = G.neighbors(maxnode)
        for nbr in nbrs:   
            sdt_com = 'unlink ' + str(maxnode) + "," + str(nbr) + " "
            sock.sendall(sdt_com)       
        draw_sdt_nx(sock,G,pos,
                node_color=newbc,
                node_size=newbs,
                alpha=0.4,
                edge_color="blue",
                linewidths=1,
                l_alpha=0.3)
        time.sleep(0.1)              
if __name__ == '__main__':
    sys.exit(main(sys.argv))
