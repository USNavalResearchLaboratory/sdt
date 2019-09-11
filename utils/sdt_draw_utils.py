"""
**********
SDT3D Python Graph Utilities
**********

Draw networks from networkx using sdt3D.
sdt3D is based upon NASA WorldWind.


See Also
--------

sdt3D:     http://cs.nrl.navy.mil/products
networkx     http://networkx.lanl.gov

"""
__author__ = """Joe Macker (jpmacker@gmail.com)"""

import networkx as nx
import math
import types
#import numpy as np
import matplotlib.cm as cmx
import matplotlib.colors as colors
import time

def strRgb(mag, cmin, cmax, cmap=None):
    """
       Return a tuple of strings to be used in sdt3D plots.
    """
    cNorm  = colors.Normalize(vmin=cmin, vmax=cmax)
    colormap=cmx.ScalarMappable(norm=cNorm, cmap=cmap)
#    colormap.set_clim(vmin=cmin,vmax=cmax)
    red, green, blue, alpha = colormap.to_rgba(mag, cmin, cmax)  
    return "%03d:%03d:%03d" % (red, green, blue)

def pan(sock,x,y,z,start=0,end=90, wait= 1.0):
    """
       Routine to play with panning the sdt3d camera.
    """
    for i in range(start,end):
        pitch = str(i)
        heading = str(i*4)
        sdt_com = 'lookAt ' + str(x) + ',' + str(y) + ',' + str(z)
        sdt_com = sdt_com + ',' + heading + ',' + pitch + ' '
        sock.sendall(sdt_com)
        time.sleep(wait)
    for i in range(start,end):
        pitch = str(end-i)
        heading = str((end-i)*4)
        sdt_com = 'lookAt ' + str(x) + ',' + str(y) + ',' + str(z)
        sdt_com = sdt_com + ',' + heading + ',' + pitch + ' '
        sock.sendall(sdt_com)
        time.sleep(wait)

from networkx.drawing.layout import shell_layout,\
    circular_layout,spectral_layout,spring_layout,random_layout
__author__ = """Joe Macker (jpmacker@gmail.com)"""
__all__ = ['draw_sdt_nx',
           'sdt_nx',
           'draw_sdt_nx_nodes',
           'draw_sdt_nx_edges',
           'draw_sdt_nx_node_labels',
           'draw_sdt_nx_edge_labels',
           'sdt_nx_circular',
           'sdt_nx_random',
           'sdt_nx_spectral',
           'sdt_nx_spring',
           'sdt_nx_shell']

def draw_sdt_nx(sock,G, pos=None, **kwds):
    """Draw the graph G with sdt3D over socket sock.

Draw the graph as a simple representation with optional attribute args


Parameters
----------
G : graph
A networkx graph

sock a socket handler for sdt3D connection

"""
    sdt_nx(sock,G,pos=pos,**kwds)
    return


def sdt_nx(sock,G, pos=None, with_labels=True, **kwds):
    """Draw the graph G using sdt3d with labels.

Draw the graph with sdt3d with options for node positions,
labeling, titles, and many other drawing features.
See draw() for simple drawing without labels or axes.

Parameters
----------
G : graph
A networkx graph

pos : dictionary, optional
A dictionary with nodes as keys and positions as values.
If not specified a 3-D spring layout positioning will be computed.
See networkx.layout for functions that compute node positions.

with_labels : bool, optional (default=True)
Set to True to draw labels on the nodes.

nodelist : list, optional (default G.nodes())
Draw only specified nodes

edgelist : list, optional (default=G.edges())
Draw only specified edges

node_size : scalar or array, optional (default=50)
Size of nodes. If an array is specified it must be the
same length as nodelist.

node_color : color string, or array of floats, (default='r')
Node color. Can be a single color format string,
or a sequence of colors with the same length as nodelist.
If numeric values are specified they will be mapped to
colors using the cmap and vmin,vmax parameters. See
matplotlib.scatter for more details.

node_symbol : string, optional (default='o')
The symbol for the node. See sdt3d users guide

width : float, width of link line, optional (default=1.0)
                        
edge_color: string or array of floats, optional (default="blue")
                        
style: line style

alpha : float, optional (default=1.0)
The node transparency

"""
    if pos is None:
        pos=nx.spring_layout(G,dim=3) # default to spring layout

    if with_labels:
        draw_sdt_nx_nodes(sock,G, pos, **kwds)
        draw_sdt_nx_edges(sock,G, pos, **kwds)
    return

def draw_sdt_nx_nodes(sock,G, pos, 
                        geoPos=False,
                        nodelist=None,
                        node_size=50,
                        node_color='red',
                        node_shape='sphere',
                        n_alpha=1.0,
                        l_alpha=1.0,
                        cmap=None,
                        vmin=None,
                        vmax=None,
                        linewidths=None,
                        label_c = False,
                        nodeLayer = None,
                        **kwds):
    """Draw the nodes of the graph G.

This draws only the nodes of the graph G.

Parameters
----------
G : graph
A networkx graph

pos : dictionary
A dictionary with nodes as keys and positions as values.
If not specified a spring layout positioning will be computed.
See networkx.layout for functions that compute node positions.

geoPos: tells whether x,y,z position is in geo coordinates
nodelist : list, optional
Draw only specified nodes (default G.nodes())



"""
    if nodelist is None:
        H = G
    elif not nodelist or len(nodelist)==0: # empty nodelist, no drawing
        return None  
    else:
        H = G.subgraph(nodelist)
        
# in this case attributes just point to original graph
# dont use this if we want to change attributes but this is just
# a graph utility.
    i=0
# Takes care of messy stuff when node names are strings not ints
    sdt_com=""
    for n,d in H.nodes(data=True):    
#check for color array
        if type(node_color) is types.ListType:
            n_color=strRgb(node_color[i],0.0,1.0,cmap=cmap)
        else:
            n_color = node_color 
        if type(node_size) is types.ListType:
            n_size=str(node_size[i])
        else:
            n_size = str(node_size)       
        sdt_com = sdt_com + "node " + str(n)
#apply label colors
        if label_c:
            sdt_com = sdt_com + ' label ' + n_color          
        if not geoPos:
            posx = "%.2f" % (pos[n][0]*1000.0)
            posy = "%.2f" % (pos[n][1]*1000.0)
        else:
            posx = str(pos[n][0])
            posy = str(pos[n][1])
        sdt_com = sdt_com + ' position ' + posx + "," + posy
# test for optional z parameter
        if len(pos[n])==3:
            if not geoPos:
                posz = "%.2f" % (pos[n][2]*1000.0) #TBD test for z and if there add
            else:
                posz = str(pos[n][2])
        else:
            posz="0.0"
# finish z test
        sdt_com = sdt_com + "," + posz
        if not geoPos: 
            sdt_com = sdt_com + ',c'
        else:
            sdt_com = sdt_com + ',g'
        sdt_com = sdt_com + " symbol " + node_shape + "," 
        sdt_com = sdt_com + n_color + ",3," + n_size + "," + n_size + "," + str(n_alpha)
        if nodeLayer:
            sdt_com = sdt_com + " nodeLayer " + str(nodeLayer)
        sdt_com = sdt_com + " "
#    print sdt_com
        i = i + 1
    sock.sendall(sdt_com)
#    print sdt_com
    return


def draw_sdt_nx_edges(sock,G, pos,
                        edgelist=None,
                        linewidths=1.0,
                        l_alpha=1.0,
                        edge_color='blue',
                        style='solid',
                        edge_cmap=None,
                        edge_vmin=None,
                        edge_vmax=None,
                        ax=None,
                        arrows=True,
                        label=None,
                        edgeLayer=None,
                        **kwds):
    """Draw the edges of the graph G.

This draws only the edges of the graph G.

Parameters
----------
G : graph
A networkx graph

pos : dictionary
A dictionary with nodes as keys and positions as values.
If not specified a spring layout positioning will be computed.
See networkx.layout for functions that compute node positions.

edgelist : collection of edge tuples
Draw only specified edges(default=G.edges())

width : integer [1,8]
l_alpha : float [0,1]
Line width of edges (default =1.0)


Notes
-----
For directed graphs, "arrows" (actually just thicker stubs) are drawn
at the head end. Arrows can be turned off with keyword arrows=False.
Yes, it is ugly but drawing proper arrows with Matplotlib this
way is tricky.
"""
    if edgelist is None:
        H = G
    elif not edgelist or len(edgelist)==0: # empty edgelist, no drawing
        return None  
    else:
        return

    sdt_com=""    
    for node,nbrsdict in H.adjacency_iter():
        for nbr,eattr in nbrsdict.items():
            sdt_com = sdt_com + "link " + str(node) + "," + str(nbr) + " line " + str(edge_color) + "," + str(linewidths) + "," + str(l_alpha) + " "
            if edgeLayer:
                sdt_com = sdt_com + "linkLayer " + str(edgeLayer)
            sdt_com = sdt_com + " "
    sock.sendall(sdt_com)
#    print sdt_com
    return

def draw_sdt_nx_node_labels(sock,G, pos,
                         labels=None,
                         font_size=12,
                         font_color='k',
                         font_family='sans-serif',
                         font_weight='normal',
                         alpha=1.0,
                         ax=None,
                         **kwds):
    """Draw node labels on the graph G.

Parameters
----------
G : graph
A networkx graph

pos : dictionary, optional
A dictionary with nodes as keys and positions as values.
If not specified a spring layout positioning will be computed.
See networkx.layout for functions that compute node positions.

labels : dictionary, optional (default=None)
Node labels in a dictionary keyed by node of text labels

"""

    pass
    return
# Add code toggle layers and optionally pass in explicit node labels

def draw_sdt_nx_edge_labels(sock,G, pos,
                              edge_labels=None,
                              label_pos=0.5,
                              font_size=10,
                              font_color='k',
                              font_family='sans-serif',
                              font_weight='normal',
                              alpha=1.0,
                              bbox=None,
                              ax=None,
                              rotate=True,
                              **kwds):
    """Draw edge labels.

Parameters
----------
G : graph
A networkx graph


"""
    pass
    return
# Add code toggle layers and optionally pass in explicit edge labels

def set_status(sock, status=""):
    """Set Status.

Parameters
----------
status = string to display in status field


"""
    sdt_com= 'status "' + status + '" '
    sock.sendall(sdt_com)
    return

# Add code toggle layers and optionally pass in explicit edge labels


def sdt_nx_circular(sock,G, **kwargs):
    """Draw the graph G with a circular layout."""
    draw_sdt_nx(sock,G,nx.circular_layout(G),**kwargs)

def sdt_nx_random(sock,G, **kwargs):
    """Draw the graph G with a random layout."""
    draw_sdt_nx(sock,G,nx.random_layout(G),**kwargs)

def sdt_nx_spectral(sock,G, **kwargs):
    """Draw the graph G with a spectral layout."""
    draw_sdt_nx(sock,G,nx.spectral_layout(G),**kwargs)

def sdt_nx_spring(sock,G, **kwargs):
    """Draw the graph G with a spring layout."""
    draw_sdt_nx(sock,G,nx.spring_layout(G),**kwargs)

def sdt_nx_shell(sock,G, **kwargs):
    """Draw networkx graph with shell layout."""
    nlist = kwargs.get('nlist', None)
    if nlist != None:
        del(kwargs['nlist'])
    draw_sdt_nx(sock,G,nx.shell_layout(G,nlist=nlist),**kwargs)
