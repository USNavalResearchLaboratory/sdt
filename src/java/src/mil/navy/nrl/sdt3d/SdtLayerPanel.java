package mil.navy.nrl.sdt3d;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;

public class SdtLayerPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SdtCheckboxTree layerTree = null;

	private WorldWindow wwd;


	public SdtLayerPanel(WorldWindow wwd)
	{
		// Make a panel at a default size.
		super(new BorderLayout());
		this.wwd = wwd;
		buildLayerPanel();
	}


	private WorldWindow getWwd()
	{
		return this.wwd;
	}


	private void buildLayerPanel()
	{

		initCheckboxTree();
		JScrollPane scrollPane = new JScrollPane(this.layerTree);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		add(scrollPane, BorderLayout.CENTER);
	}


	public void removeLayer()
	{
		// ljt test!!
		// layerTree.removeAll();
	}


	public SdtCheckboxTree getLayerTree()
	{
		return layerTree;
	}


	private void initCheckboxTree()
	{

		SdtCheckboxNode layersRoot = new SdtCheckboxNode("All Layers");
		layerTree = new SdtCheckboxTree(layersRoot);
		layerTree.setCellRenderer(new SdtCheckboxCellRenderer(layerTree));

		Set<String> sdtLayers = new HashSet<String>(9);
		sdtLayers.add("Network Links");
		sdtLayers.add("Link Labels");
		sdtLayers.add("Markers");
		sdtLayers.add("Node Labels");
		sdtLayers.add("Node Icons");
		sdtLayers.add("Node Models");
		sdtLayers.add("Node Kml Models");
		sdtLayers.add("Node Symbols");
		sdtLayers.add("Node Trails");
		sdtLayers.add("Regions");
		sdtLayers.add("Images");
		sdtLayers.add("Tiles");
		sdtLayers.add("Kml");
		sdtLayers.add("Kml Panel");
		addLayer("All Layers::Worldwind", false);

		// fill the layers panel with the titles of all layers in the world window's current model.
		for (Layer layer : getWwd().getModel().getLayers())
		{
			if (layer.getName().equalsIgnoreCase("USGS NAIP Plus"))
				addLayer("All Layers::Worldwind::" + layer.getName(), true);
			else if (layer.getName().equalsIgnoreCase("Bing Imagery"))
				addLayer("All Layers::Worldwind::" + layer.getName(), true);
			else
			// Node layer is not actually a renderable layer - it just manages rendering other layers
			if (!sdtLayers.contains(layer.getName()) &&
				!layer.getName().equalsIgnoreCase("Node Layer") &&
				!layer.getName().equalsIgnoreCase("User Defined Link Labels") &&
				!layer.getName().equalsIgnoreCase("User Defined Network Links") &&
				!layer.getName().equalsIgnoreCase("User Defined Markers"))
				addLayer("All Layers::Worldwind::" + layer.getName(), layer.isEnabled());
		}

		addLayer("All Layers::Sdt", false);

		addLayer("All Layers::Sdt::Network Links", true);
		addLayer("All Layers::Sdt::Link Labels", false);
		addLayer("All Layers::Sdt::Markers", false);
		addLayer("All Layers::Sdt::Node Labels", true);
		addLayer("All Layers::Sdt::Node Icons", true);
		addLayer("All Layers::Sdt::Node Models", true);
		addLayer("All Layers::Sdt::Node Kml Models", true);
		addLayer("All Layers::Sdt::Node Symbols", true);
		addLayer("All Layers::Sdt::Node Trails", true);
		addLayer("All Layers::Sdt::Regions", true);
		addLayer("All Layers::Sdt::Images", true);
		addLayer("All Layers::Sdt::Tiles", true);
		addLayer("All Layers::Sdt::Kml", true);
		addLayer("All Layers::Sdt::Kml Panel", false);

		addLayer("All Layers::User Defined", true);
		addLayer("All Layers::User Defined::User Defined Link Labels", true);
		addLayer("All Layers::User Defined::User Defined Network Links", true);
		addLayer("All Layers::User Defined::User Defined Markers", true);

		layerTree.expandPath(getPath(findNode("All Layers")));
		layerTree.expandPath(getPath(findNode("Sdt")));
		layerTree.collapsePath(getPath(findNode("Worldwind")));
		layerTree.expandPath(getPath(findNode("User Defined")));

	}


	public boolean contains(String val)
	{
		// We must be toggling an entire section.
		if (val.equals("All Layers") ||
			val.equals("Worldwind") ||
			val.equals("Sdt") ||
			val.equals("User Defined"))
			return true;

		if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
		{
			val = "All Layers::" + val;
			if (contains(val))
			{
				return true;
			}
			val = "User Defined::" + val;
		}
		String[] attrs = val.split("::");
		if (attrs.length < 2)
			return false; // no attr
		boolean found = false;
		String childStr = null;
		DefaultMutableTreeNode child = null;
		// Find the top level node of this name
		DefaultMutableTreeNode parent = findNode(attrs[0]);

		int index = 0;
		while (attrs.length > index)
		{
			if (index + 1 == attrs.length)
				break;
			childStr = attrs[index + 1];
			found = false;
			child = null;

			Enumeration<?> children = parent.children();
			while (children.hasMoreElements())
			{
				child = (DefaultMutableTreeNode) children.nextElement();
				if (childStr.equals(((SdtCheckboxNode) child).getText()))
				{
					parent = child;
					found = true;
					break;
				}
			}
			index = index + 1;
		}

		return found;
	}


	public DefaultMutableTreeNode findLayer(String val)
	{
		// if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
		// val = "User Defined::" + val;

		String[] attrs = val.split("::");
		if (attrs.length < 2)
			return null; // no attr
		boolean found = false;
		String childStr = null;
		DefaultMutableTreeNode child = null;
		// Find the top level node of this name
		DefaultMutableTreeNode parent = findNode(attrs[0]);

		int index = 0;
		while (attrs.length > index)
		{
			if (index + 1 == attrs.length)
				break;
			childStr = attrs[index + 1];
			found = false;
			child = null;

			Enumeration<?> children = parent.children();
			while (children.hasMoreElements())
			{
				child = (DefaultMutableTreeNode) children.nextElement();
				if (childStr.equals(((SdtCheckboxNode) child).getText()))
				{
					parent = child;
					found = true;
					break;
				}
			}
			index = index + 1;
		}
		if (found)
			return parent;
		else
			return null;

	}


	// ljt merge these 4 find functions (contain, findNode, findLeaf, findLayer)
	// get rid of findNode entirely?
	public DefaultMutableTreeNode findLeaf(String val)
	{
		if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
			val = "User Defined::" + val;

		String[] attrs = val.split("::");
		String childStr = null;
		DefaultMutableTreeNode child = null;
		// Find the top level node of this name
		DefaultMutableTreeNode parent = findNode(attrs[0]);

		int index = 0;
		while (attrs.length > index)
		{
			if (index + 1 == attrs.length)
				break;
			childStr = attrs[index + 1];
			child = null;

			Enumeration<?> children = parent.children();
			while (children.hasMoreElements())
			{
				child = (DefaultMutableTreeNode) children.nextElement();
				if (childStr.equals(((SdtCheckboxNode) child).getText()))
				{
					parent = child;
					break;
				}
			}
			index = index + 1;
		}

		return child;
	}


	public boolean addLayer(String val, Boolean selected)
	{
		if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
		{
			val = "User Defined::" + val;
			// Until we add checkbox control expand user defined layers
			// when new ones added (temporary hack)
			layerTree.expandPath(getPath(findNode("User Defined")));

		}
		String[] attrs = val.split("::");
		if (attrs.length < 2)
			return false; // no attr
		boolean found = false;
		boolean assigned = false;
		String childStr = null;
		DefaultMutableTreeNode child = null;
		// Find the top level instance of this named node
		DefaultMutableTreeNode parent = findNode(attrs[0]);

		int index = 0;
		while (attrs.length > index)
		{
			if (index + 1 == attrs.length)
				break;
			childStr = attrs[index + 1];
			found = false;
			assigned = false;
			child = null;

			// If any node in this tree has an sdt object assigned,
			// abort - we cannot add further nested layers
			if (((SdtCheckboxNode) parent).isAssigned())
			{
				System.out.println("A node in the layer stack has an element assigned.  Nested layers can not be added");
				return false;
			}
			Enumeration<?> children = parent.children();
			while (children.hasMoreElements())
			{
				child = (DefaultMutableTreeNode) children.nextElement();
				if (childStr.equals(((SdtCheckboxNode) child).getText()))
				{
					parent = child;
					if (((SdtCheckboxNode) parent).isAssigned())
					{
						System.out.println("A node in the layer stack has an element assigned.  Nested layers can not be added");
						return false;
					}
					found = true;
					break;
				}
			}

			if (found == false)
			{
				parent = addObject(parent, childStr, selected);
			}

			index = index + 1;
		}

		return true;
	}


	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			String child, boolean selected)
	{

		SdtLayerAction action = new SdtLayerAction(child, wwd, selected);
		SdtCheckboxNode childNode = new SdtCheckboxNode(action, child);

		((DefaultTreeModel) layerTree.getModel()).insertNodeInto(childNode, parent,
			parent.getChildCount());

		childNode.setSelected(selected);
		// Tell the toggle function we're adding a new node
		((SdtCheckboxCellRenderer) layerTree.getCellRenderer()).toggleCheckbox(getPath(childNode), true);

		return childNode;
	}


	public DefaultMutableTreeNode findNode(String nodeStr)
	{
		DefaultMutableTreeNode node = null;
		// Get the enumeration
		TreeNode root = (TreeNode) layerTree.getModel().getRoot();
		Enumeration<?> eNum = ((DefaultMutableTreeNode) root).breadthFirstEnumeration();
		while (eNum.hasMoreElements())
		{
			// Find requested node node
			node = (DefaultMutableTreeNode) eNum.nextElement();
			if (nodeStr.equals(((SdtCheckboxNode) node).getText()))
			{
				return node;
			}
		}
		return null;
	}


	// Returns a TreePath containing the specified node.
	public TreePath getPath(TreeNode node)
	{
		List<TreeNode> list = new ArrayList<TreeNode>();

		// Add all nodes to list
		while (node != null)
		{
			list.add(node);
			node = node.getParent();
		}
		Collections.reverse(list);

		// Convert array of nodes to TreePath
		return new TreePath(list.toArray());
	}


	void toggleLayer(String layerName, boolean selected)
	{

		String[] attrs = layerName.split(",");

		// top most layer, specifically toggle lower layers
		if (attrs[0].equalsIgnoreCase("All Layers"))
		{
			toggleLayer("Worldwind", selected);
			toggleLayer("Sdt", selected);
			toggleLayer("User Defined", selected);
			return;
		}

		layerName = attrs[0];
		DefaultMutableTreeNode theNode = findLayer(layerName);
		if (theNode == null)
		{
			// First check to see if it's a user defined layer
			if (!layerName.startsWith("User Defined") && !layerName.startsWith("All Layers"))
			{
				String tmpName = "User Defined::" + layerName;
				theNode = findLayer(tmpName);
			}
			// Is it a wwj layer?
			if (theNode == null)
			{
				layerName = "All Layers::" + layerName;
				theNode = findLayer(layerName);
			}

		}
		if (theNode != null)
		{
			((SdtCheckboxNode) theNode).setSelected(selected);

			((SdtCheckboxCellRenderer) layerTree.getCellRenderer()).toggleCheckbox(getPath(theNode), false);
		}
	}


	private void resetWorldwindLayers(WorldWindow wwd)
	{

		// Basic Model will recreate the label layer list as defined in the config file
		BasicModel model = new BasicModel();
		// fill the layers panel with the titles of all layers in the world window's current model.
		for (Layer layer : model.getLayers())
		{
			// Get the node for the layer
			DefaultMutableTreeNode theNode = findNode(layer.getName());
			if (theNode == null)
			{
				// Not sure how we are getting empty nodes..
				continue;
			}
			// Order here is important
			((SdtCheckboxCellRenderer) layerTree.getCellRenderer()).resetCheckbox(getPath(theNode), false);
			((SdtCheckboxNode) theNode).setWWJSelection(layer.isEnabled());

			if ((layer.getName().equalsIgnoreCase("USGS Urban Area Ortho")
				&& ((SdtCheckboxNode) theNode).getText().equalsIgnoreCase("USGS Urban Area Ortho"))
				||
				(layer.getName().equalsIgnoreCase("Bing Imagery") && ((SdtCheckboxNode) theNode).getText().equalsIgnoreCase("Bing Imagery"))
				||
				(layer.getName().equalsIgnoreCase("MS Virtual Earth Aerial")
					&& ((SdtCheckboxNode) theNode).getText().equalsIgnoreCase("MS Virtual Earth Aerial")))
			{
				((SdtCheckboxCellRenderer) layerTree.getCellRenderer()).resetCheckbox(getPath(theNode), true);
				((SdtCheckboxNode) theNode).setWWJSelection(true);
			}

		} // end for wwj layers

	}


	public void update(WorldWindow wwd, String selection)
	{

		if (selection.equals("wwj"))
			// Reset worldwind layers to default selections
			this.resetWorldwindLayers(wwd);
		else if (selection.equals("all"))
		{
			// Remove all user defined layers and rebuild panel with default wwj layers
			removeAll();
			buildLayerPanel();
		}
		else
		{
			// remove specified layer (leafs only!) from the checkbox tree
			if (!selection.startsWith("All Layers"))
				selection = "All Layers::" + selection;

			DefaultMutableTreeNode layer = findLayer(selection);
			if (layer == null)
				return;
			TreePath path = getPath(layer);
			((DefaultTreeModel) layerTree.getModel()).removeNodeFromParent(layer);
			((SdtCheckboxCellRenderer) layerTree.getCellRenderer()).resetCheckbox(path, false);
		}

		this.revalidate();
		this.repaint();
	}

}
