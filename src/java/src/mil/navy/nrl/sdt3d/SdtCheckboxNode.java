package mil.navy.nrl.sdt3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import gov.nasa.worldwind.render.GlobeAnnotation;

class SdtCheckboxNode extends DefaultMutableTreeNode
{
	/**
	 * Manages rendering of associated objects
	 */
	private static final long serialVersionUID = 1L;

	String text;

	boolean selected;

	boolean partiallyChecked;

	SdtLayerAction layerAction = null;

	private List<SdtNode> nodeSet = new ArrayList<SdtNode>();

	private List<SdtLink> linkSet = new ArrayList<SdtLink>();

	private List<SdtSymbol> symbolSet = new ArrayList<SdtSymbol>();

	private List<SdtNode> labelSet = new ArrayList<SdtNode>();

	private List<SdtRegion> regionSet = new ArrayList<SdtRegion>();

	private List<SdtTile> tileSet = new ArrayList<SdtTile>();

	private sdt3d.AppFrame ourApp = null;


	public SdtCheckboxNode(String text)
	{
		super(text);
		this.text = text;
		this.selected = false;
	}


	public SdtCheckboxNode(SdtLayerAction action, String text)
	{
		super(action);
		this.layerAction = action;
		this.text = text;
		this.selected = false;
	}


	public boolean isSelected()
	{
		return selected;
	}


	public boolean isAssigned()
	{
		// Have any elements been assigned to this checkbox?
		return !(nodeSet.isEmpty() && linkSet.isEmpty() && symbolSet.isEmpty()
			&& labelSet.isEmpty() && regionSet.isEmpty() && tileSet.isEmpty());

	}


	public void deleteAll()
	{
		// This function nonly removes the sdt object
		// associations, the objects themselves are not
		// deleted. See clearLayer
		Iterator<SdtNode> itr = nodeSet.iterator();
		while (itr != null && itr.hasNext())
		{
			SdtNode theNode = itr.next();
			theNode.removeLayer();
			itr.remove();
		}
		Iterator<SdtLink> linkItr = linkSet.iterator();
		while (linkItr != null && linkItr.hasNext())
		{
			SdtLink theLink = linkItr.next();
			theLink.removeLayer();
			linkItr.remove();
		}
		Iterator<SdtSymbol> symbolItr = symbolSet.iterator();
		while (symbolItr != null && symbolItr.hasNext())
		{
			SdtSymbol theSymbol = symbolItr.next();
			theSymbol.removeLayer();
			symbolItr.remove();
		}
		Iterator<SdtNode> labelItr = labelSet.iterator();
		while (labelItr != null && labelItr.hasNext())
		{
			SdtNode theNode = labelItr.next();
			theNode.removeLayer();
			labelItr.remove();
		}
		Iterator<SdtRegion> regionItr = regionSet.iterator();
		while (regionItr != null && regionItr.hasNext())
		{
			SdtRegion theRegion = regionItr.next();
			theRegion.removeLayer();
			regionItr.remove();
		}
		Iterator<SdtTile> tileItr = tileSet.iterator();
		while (tileItr != null && tileItr.hasNext())
		{
			SdtTile theTile = tileItr.next();
			theTile.removeLayer();
			tileItr.remove();
		}
	}


	public void clearLayer(sdt3d.AppFrame theApp)
	{

		// This function removes any objects associated
		// with the later from the sdt object lists. They
		// will NOT be available anywhere in sdt any longer.
		Iterator<SdtNode> itr = nodeSet.iterator();
		while (itr != null && itr.hasNext())
		{
			SdtNode theNode = itr.next();
			theNode.removeLayer();
			itr.remove();
			theApp.delete("node," + theNode.getName());
		}
		Iterator<SdtLink> linkItr = linkSet.iterator();
		while (linkItr != null && linkItr.hasNext())
		{
			SdtLink theLink = linkItr.next();
			theLink.removeLayer();
			linkItr.remove();
			theApp.delete("link," + theLink.getSrcNode() + "," + theLink.getDstNode() + ",all");
		}
		Iterator<SdtSymbol> symbolItr = symbolSet.iterator();
		while (symbolItr != null && symbolItr.hasNext())
		{
			SdtSymbol theSymbol = symbolItr.next();
			theSymbol.removeLayer();
			symbolItr.remove();
			// Named symbol deletion not supported
			// TBD disable symbolLayer?
			System.out.println("INFO: symbol assignments to layers not fully supported.  May not be cleared properly.");

		}
		Iterator<SdtNode> labelItr = labelSet.iterator();
		while (labelItr != null && labelItr.hasNext())
		{
			SdtNode theNode = labelItr.next();
			theNode.removeLayer();
			labelItr.remove();
			// Named label deletion not supported
			// TBD disable labelLayer?
			System.out.println("INFO: label assignment to layers not fully supported.  Label may not be cleared properly.");
		}
		Iterator<SdtRegion> regionItr = regionSet.iterator();
		while (regionItr != null && regionItr.hasNext())
		{
			SdtRegion theRegion = regionItr.next();
			theRegion.removeLayer();
			regionItr.remove();
			theApp.delete("region," + theRegion.getName());
		}
		Iterator<SdtTile> tileItr = tileSet.iterator();
		while (tileItr != null && tileItr.hasNext())
		{
			SdtTile theTile = tileItr.next();
			theTile.removeLayer();
			tileItr.remove();
			System.out.println("INFO: clearing tiles not supported.  Tile may not be properly cleared.");
		}
	}


	public void setPartiallyChecked(boolean newValue)
	{
		partiallyChecked = newValue;
	}


	public void setWWJSelection(boolean newValue)
	{
		this.selected = newValue;
		// Layer action manages disabling wwj layers.
		if (layerAction != null)
			layerAction.toggleLayer(newValue);
	}


	public void toggleSelected()
	{
		selected = !selected;
	}


	public void setSelected(boolean newValue)
	{
		// no change of state, return
		if (selected == newValue && !partiallyChecked)
			return;
		selected = newValue;

		// Layer action manages disabling wwj layers.
		if (layerAction != null)
			layerAction.toggleLayer(newValue);

		if (ourApp == null)
			return;

		// toggle renderables for all nodes associated with this checkbox
		Iterator<SdtNode> itr = nodeSet.iterator();
		while (itr != null && itr.hasNext())
		{
			SdtNode theNode = itr.next();
			if (!selected)
				theNode.removeRenderables(ourApp);
			else
				theNode.drawRenderables(ourApp);
		}
		// toggle any link renderables associated with this checkbox
		Iterator<SdtLink> linkItr = linkSet.iterator();
		while (linkItr != null && linkItr.hasNext())
		{
			SdtLink theLink = linkItr.next();
			if (!selected)
				theLink.removeRenderables();
			else
				theLink.drawRenderables(false);

		}
		// toggle any symbols associated with this checkbox
		Iterator<SdtSymbol> symbolItr = symbolSet.iterator();
		while (symbolItr != null && symbolItr.hasNext())
		{
			SdtSymbol theSymbol = symbolItr.next();
			if (!selected)
				ourApp.getSymbolLayer().removeRenderable(theSymbol);
			else
			{
				ourApp.getSymbolLayer().addRenderable(theSymbol);
				theSymbol.setInitialized(false);
			}
		}
		// toggle any labels in the label set
		Iterator<SdtNode> labelItr = labelSet.iterator();
		while (labelItr != null && labelItr.hasNext())
		{
			SdtNode theNode = labelItr.next();
			if (!selected)
			{
				if (theNode.hasLabel())
					ourApp.getNodeLabelLayer().removeAnnotation(theNode.getLabel());
			}
			else
			{
				GlobeAnnotation label = theNode.getLabel();
				if (null != label)
					ourApp.getNodeLabelLayer().addAnnotation(label);
			}
		}
		// toggle any regions associated with this checkbox
		Iterator<SdtRegion> regionItr = regionSet.iterator();
		while (regionItr != null && regionItr.hasNext())
		{
			SdtRegion theRegion = regionItr.next();
			if (!selected)
				ourApp.getRegionLayer().removeRenderables(theRegion);
			else
			{
				theRegion.setInitialized(false);
				ourApp.getRegionLayer().addRegion(theRegion);
				// ljt theRegion.initialize(ourApp.getWwd().getSceneController().getDrawContext());
				// ljt old ourApp.getRegionLayer().addRenderable(theRegion);
			}
		}

		// toggle any tiles associated with this checkbox
		Iterator<SdtTile> tileItr = tileSet.iterator();
		while (tileItr != null && tileItr.hasNext())
		{
			SdtTile theTile = tileItr.next();
			if (!selected)
				ourApp.getTileLayer().removeRenderable(theTile.getSurfaceImage());
			else
			{
				ourApp.getTileLayer().addRenderable(theTile.getSurfaceImage());
			}
		}
		ourApp.getWwd().redraw();
		if (ourApp.getSharedFrame() != null)
			ourApp.getSharedFrame().wwjPanel.wwd.redraw();

	} // set selected


	public boolean getSelected()
	{
		return selected;
	}


	public String getText()
	{
		return text;
	}


	public void setText(String newValue)
	{
		text = newValue;
	}


	@Override
	public String toString()
	{
		return getClass().getName() + "[" + text + "/" + isSelected() + "]";
	}


	public void addNode(SdtNode theNode, sdt3d.AppFrame theApp)
	{
		ourApp = theApp;
		nodeSet.add(theNode);
		if (!isSelected())
			theNode.removeRenderables(theApp);
		else
			theNode.drawRenderables(theApp);
	}


	public void addSymbol(SdtSymbol theSymbol, sdt3d.AppFrame theApp)
	{
		ourApp = theApp;
		symbolSet.add(theSymbol);
		if (!isSelected())
		{
			theSymbol.setInitialized(true);
			theApp.getSymbolLayer().removeRenderable(theSymbol);
		}
		else
		{
			theApp.getSymbolLayer().addRenderable(theSymbol);
			theSymbol.setInitialized(false);
		}
	}


	public void addLink(SdtLink theLink, sdt3d.AppFrame theApp)
	{
		ourApp = theApp;
		linkSet.add(theLink);
		if (!isSelected())
			theLink.removeRenderables();
		else
			theLink.drawRenderables(false);
	}


	public void addLabel(SdtNode theNode, sdt3d.AppFrame theApp)
	{
		ourApp = theApp;
		labelSet.add(theNode);
		if (!isSelected())
		{
			if (theNode.hasLabel())
				theApp.getNodeLabelLayer().removeAnnotation(theNode.getLabel());
		}
		else
		{ // ljt new
			if (theNode.hasLabel())
				theApp.getNodeLabelLayer().addAnnotation(theNode.getLabel());
		}

	}


	public void addRegion(SdtRegion theRegion, sdt3d.AppFrame theApp)
	{
		ourApp = theApp;
		regionSet.add(theRegion);
		if (!isSelected())
		{
			// theRegion.setInitialized(true); // ljt new
			ourApp.getRegionLayer().removeRenderables(theRegion);
		}
		else
		{ // ljt next new:
			// ourApp.getRegionLayer().addRegion(theRegion);
			// remove in case it was already drawn... needs to be cleaned up
			ourApp.getRegionLayer().removeRenderables(theRegion);
			// ourApp.getRegionLayer().addRenderable(theRegion);
			theRegion.setInitialized(false);

		}

	}


	public void addTile(SdtTile theTile, sdt3d.AppFrame theApp)
	{
		ourApp = theApp;
		tileSet.add(theTile);
		if (!isSelected())
			ourApp.getTileLayer().removeRenderable(theTile.getSurfaceImage());
		else // ljt new:
		{ // ljt temp fix?
			ourApp.getTileLayer().removeRenderable(theTile.getSurfaceImage());
			ourApp.getTileLayer().addRenderable(theTile.getSurfaceImage());
		}
	}


	public void removeNode(SdtNode theNode)
	{
		if (!isSelected())
			theNode.drawRenderables(ourApp);
		nodeSet.remove(theNode);
	}


	public void removeLink(SdtLink theLink)
	{
		if (!isSelected())
			theLink.drawRenderables(false);
		linkSet.remove(theLink);
	}


	public void removeSymbol(SdtSymbol theSymbol)
	{
		if (!isSelected())
		{
			theSymbol.setInitialized(false);
		}
		symbolSet.remove(theSymbol);
	}


	public void removeLabel(SdtNode theNode)
	{
		if (!isSelected())
			if (theNode.hasLabel())
				ourApp.getNodeLabelLayer().addAnnotation(theNode.getLabel());

		labelSet.remove(theNode);
	}


	public void removeRegion(SdtRegion theRegion)
	{
		// Return the region
		if (!isSelected())
		{
			theRegion.initialize(ourApp.getWwd().getSceneController().getDrawContext());
			ourApp.getRegionLayer().addRenderable(theRegion);
		}
		regionSet.remove(theRegion);
	}


	public void removeTile(SdtTile theTile)
	{
		if (!isSelected())
			ourApp.getTileLayer().addRenderable(theTile.getSurfaceImage());
		else
			ourApp.getTileLayer().removeRenderable(theTile.getSurfaceImage());
		tileSet.remove(theTile);
	}
}
