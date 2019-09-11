       SDT Graph Drawing Utility Library v 0.9.2

Author: Joe Macker

OVERVIEW
 
The SDT Graph Drawing Utilities are intended to provide a similar set of graph drawing utilities as the networkx graph analysis package but through a remote streaming socket to the SDT3D visualizer.

The drawing routines support cartesian representations of coordinates and take advantage of the NASA Worldwind 3D rendering provided inherently.  SDT3D provide methods to draw links, nodes, symbols, layers, and other features.  These utilities provide a means to take a graph represented in a networkx graph structure (networkx support several import formats such as graphml).

Dependencies:

networkx: Stores graph format. Also provides a large set of graph-based generation, analysis, and manipulation tools.

matplotlib: While the utility does not use matplotlib graphing capabilities it does use the flexible color mapping features within the matplotlib library.  These colormaps can be used to colorize nodes,links,etc within the utility functions.

draw_sdt_nx: draw graph in sdt3D, calls draw_sdt_nx_nodes and draw_sdt_nx_edges

if pos is not provided it will default to force layout

draw_sdt_nx_nodes: draw nodes of graph in sdt3D

draw_sdt_nx_edges: draw edges (links) of graph in sdt3D

draw_sdt_nx_node_labels: draw node labels

draw_sdt_nx_edge_labels: draw node labels

sdt_nx_circular: draw circular layout
sdt_nx_random: draw random layout
sdt_nx_spectral: draw spectral layout
sdt_nx_spring: draw spring layout
sdt_nx_shell: draw shell layout

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

node_symbol : string, optional (default=‘sphere’)
The symbol for the node. See sdt3d users guide

linewidth : float, width of link line, optional (default=1.0)
                        
edge_color: string or array of floats, optional (default="blue")
                        
style: line style

alpha : float, optional (default=1.0)
The node transparency

See test_sdt_utils.py for examples of cartesian coordinate use.
See graphml_sdt_utils_example.py for example using geo Positioning or lat,lon,alt

Joseph Macker <jpmacker@gmail.com> 30 November 2014




