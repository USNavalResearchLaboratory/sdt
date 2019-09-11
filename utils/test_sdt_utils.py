import socket,optparse
import networkx as nx
import sys,time
import numpy as np
import matplotlib.cm as cmx

from sdt_draw_utils import draw_sdt_nx,draw_sdt_nx_nodes,draw_sdt_nx_edges,draw_sdt_nx_node_labels,draw_sdt_nx_edge_labels,sdt_nx_circular,sdt_nx_random,sdt_nx_spectral,sdt_nx_spring,sdt_nx_shell,set_status,pan



def main(argv):
    usagestr = "usage: %prog [-h] [options] [args]"
    parser = optparse.OptionParser(usage = usagestr)
    parser.set_defaults(addr = "127.0.0.1", port=50000)

    parser.add_option("-a", "--ipaddress", dest = "addr", type = str,
                      help = "addr")
    parser.add_option("-p", "--port", dest = "port", type = int,
                      help = "port number")

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
    per = 5
# Get some networkx graph
    G = nx.erdos_renyi_graph(100, 0.02)
    bc = nx.betweenness_centrality(G,normalized=True).values()
    newbc = []
    newbs = []
#map to normalize values between 0-1 for colors
    for val in bc:
        newbc.append(np.interp(val,[min(bc),max(bc)],[0,1]))
#map to range 10-200 for size
    for val in bc:
        newbs.append(int(np.interp(val,[min(bc),max(bc)],[10,100])))
#clear things    
    sock.sendall('clear all ')
#    sock.sendall('clear nodes ')    
    sock.sendall('layer Worldwind,off ')
    sock.sendall('layer Sdt::Kml,off ')
    sock.sendall('layer "All Layers::Sdt::Node Labels,off" ')
    sock.sendall('backgroundColor gray ')
    sock.sendall('origin 0.0,0.0,0.0 ')
    sock.sendall('center 0.0,0,0.0,0.0,c ')
#    sock.sendall('flyto 0.005,0.005,3000.0 ')
    sock.sendall('lookAt 0.005,0.005,3000.0 ')
#    sock.sendall("follow all,on ") 
#    draw_sdt_nx(sock,G,node_size=20,alpha=0.7,edge_color="blue",width=0.3)
#Try a colormap
    colormap = cmx.cool
    sdt_nx_circular(sock,G,node_size=20,
            alpha=0.5,
            edge_color="blue",
            l_alpha=0.4,
            linewidths=1,
            cmap=colormap)
    set_status(sock,"Erdos Circular Layout")
    sock.sendall('title "Erdos-renyi Graph with Circular Layout" ')
#DONE
    time.sleep(per)

#clear things    
    sock.sendall('clear all ')
    draw_sdt_nx(sock,G,
                node_color=newbc,
                node_size=newbs,
                alpha=0.4,
                edge_color="blue",
                linewidths=1,
                cmap=colormap)
    set_status(sock,"Erdos Spring layout")
    sock.sendall('title "Erdos-renyi Graph Spring Layout" ')
    time.sleep(per)

#clear things do a chemical network   
    sock.sendall('clear all ')
    G=nx.Graph()
    G.add_nodes_from([1,2,3,4,5,6],size=100,color=0.9)
    G.add_nodes_from([7,8],size=300,color=0.5)
    G.add_nodes_from([0],size=200,color=0.1)
    n_sizes=nx.get_node_attributes(G,'size').values()
    n_colors=nx.get_node_attributes(G,'color').values()
    G.add_edges_from([(1,7),(2,7),(3,7),(7,8),(4,8),(5,8),(8,0),(0,6)])
    draw_sdt_nx(sock,G,
                node_color=n_colors,
                node_size=n_sizes,
                alpha=0.4,
                edge_color="blue",
                linewidths=1,
                cmap=colormap)
    set_status(sock,"Alcohol Molecule")
    sock.sendall('title "Molecule - Alcohol" ')
    time.sleep(per)
    sock.sendall('clear all ')
    sock.sendall('backgroundColor black ')
    G = nx.barabasi_albert_graph(300,6)
    pos=nx.spring_layout(G,dim=3,iterations=100)
    bc = nx.betweenness_centrality(G).values()
    newbc = []
    newbs = []
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
                edge_color="white",
                linewidths=1,
                l_alpha=0.1,
                cmap=colormap)
    set_status(sock,"Barabasi Graph")
    sock.sendall('title "Barabasi Graph" ')
    time.sleep(per)
    sock.sendall('clear all ')
# Now do a random geometric graph in 3D
# Get some networkx graph
    G = nx.random_geometric_graph(400,0.2,dim=3)
    pos=nx.get_node_attributes(G,'pos')
    bc = nx.betweenness_centrality(G).values()
    newbc = []
    newbs = []
#map to normalize values between 0-1 for colors
    for val in bc:
        newbc.append(np.interp(val,[min(bc),max(bc)],[0,1]))
#map to range 10-200 for size
    for val in bc:
        newbs.append(int(np.interp(val,[min(bc),max(bc)],[10,100])))
    draw_sdt_nx(sock,G,pos,
                node_color=newbc,
                node_size=newbs,
                alpha=0.9,
                edge_color="white",
                linewidths=1,
                l_alpha=0.3,
                cmap=colormap)
#    sock.sendall(sock,'title "Colormap Tests" ')
    sock.sendall('follow all ')
    pan (sock, 0.005,0.005,3000.0, 0, 90,wait=0.1)
    sock.sendall('clear all ')
    sock.sendall('title "Colormap Tests" ')
    colormaps=[cmx.hot,
                cmx.afmhot,
                cmx.autumn,
                cmx.bone,
                cmx.copper,
                cmx.gray,
                cmx.pink,
                cmx.spring,
                cmx.summer,
                cmx.winter]

    for colormap in colormaps:
        draw_sdt_nx(sock,G,pos,
                    node_color=newbc,
                    node_size=newbs,
                    alpha=0.9,
                    edge_color="white",
                    linewidths=1,
                    l_alpha=0.3,
                    cmap=colormap)
        time.sleep(per)  
#    sock.close()
    
    pan (sock, 0.005,0.005,3000.0, 0, 90,wait=0.1)

if __name__ == '__main__':
    sys.exit(main(sys.argv))
