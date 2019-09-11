package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.render.SurfaceImage;

import java.util.Enumeration;
import java.util.Hashtable;

public class SdtTile {

	private String tileName;
	private Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();
	private SurfaceImage theImage = null;
	
	public SdtTile(String name)
	{
		this.tileName = name;
	}
	public SurfaceImage getSurfaceImage()
	{
		return theImage;
	}
	public void addSurfaceImage(SurfaceImage theSurfaceImage)
	{
		theImage = theSurfaceImage;
	}
	public void addLayer(String val,SdtCheckboxNode theNode)
	{
		
		if (!layerList.containsKey(val))
		{
			layerList.put(val,theNode);
		}
	}
	public Hashtable<String, SdtCheckboxNode> getLayerList()
	{
		return layerList;
	}
	public void removeLayer()
	{
		// only in one layer for now
		removeFromCheckbox();
		layerList.clear();
	}	
	
	public boolean alreadyAssigned()
	{
		return !layerList.isEmpty();
	}

	public void removeFromCheckbox()
	{
		// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theNode = e.nextElement();
				theNode.removeTile(this);
			}

		}		
	}	
	public boolean tileInVisibleLayer()
	{

		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theNode = e.nextElement();

				if (theNode.isSelected())
					{
						return true;
					}
			}
			return false;
		}		
		return true;
	}
		
}
