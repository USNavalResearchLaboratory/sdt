package mil.navy.nrl.sdt3d;
/*!
 * Known KML limitiations:
 * 
 *  1. Images (icons) can not be associated with placemarks
 *  2. Have been having trouble getting hrefs to work correctly...
 *  
 *  /
 */
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLLookAt;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
//import gov.nasa.worldwind.ogc.kml.custom.CustomKMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.kml.impl.KMLModelPlacemarkImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.KMLLayerTreeNode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JMenuItem;
import javax.xml.stream.XMLStreamException;

public class SdtKml {

	String name = null;
	String fileName = null;
	File kmlFile = null;
	String userDir = System.getProperty("user.dir");
	KMLRoot kmlRoot = null;
	KMLLookAt lookAt = null;
	KMLController kmlController = null;
	ColladaRoot colladaRoot = null;
	JMenuItem kmlMenuItem = null;
	KMLLayerTreeNode layerNode = null;
	Double scale = 1.0;
	public SdtKml()
	{
	}
	public void setScale(double theScale)
	{	
		scale = theScale;

	}
	public Double getScale()
	{
		return scale;
	}
	// Collada root requires model scale in Vec4
	public Vec4 getModelScale()
	{
		Double x = scale;
		Double y = x;
		Double z = x;
		Vec4 modelScale = new Vec4(
			x != null ? x : 1.0,
            y != null ? y : 1.0,
            z != null ? z : 1.0);
		
    	return modelScale;	

	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getFileName()
	{
		return fileName;
	}
	public SdtKml(String fileName)
	{
		this.fileName = fileName;
		
	}
	public void setHeading(double theHeading)
	{
		// we don't have colladaRoot until the node is first rendered (at least yet)
		if (colladaRoot == null)
		{
			colladaRoot = getColladaRoot();
			if (colladaRoot == null)
			{
				System.out.println("Collada Root not found!");
				return;
			}
		}
		
		colladaRoot.setHeading(Angle.fromDegrees(theHeading));
	}

	public ColladaRoot getColladaRoot()
	{
  		// Set collada root
		if (kmlRoot != null && kmlRoot.getFeature() != null)
		{			  
			KMLAbstractFeature kmlAbstractFeature = kmlRoot.getFeature();
			for (KMLAbstractFeature feature : ((KMLAbstractContainer) kmlAbstractFeature).getFeatures())
			{
				if (feature instanceof KMLPlacemark)
				{
				       List<KMLRenderable> rs = ((KMLPlacemark) feature).getRenderables();
				       if (rs != null)
				       	{
				    	   for (KMLRenderable r : rs)
				    	   {
				    		   if (r instanceof KMLModelPlacemarkImpl)
				    		   {
				    			   if (((KMLModelPlacemarkImpl)r).getColladaRoot() != null)
				    			   {
				    				   colladaRoot = ((KMLModelPlacemarkImpl)r).getColladaRoot();
				    				   colladaRoot.setModelScale(getModelScale());
				    			   }
				    		   }				    	        
				    	   }
				       	}
				} 
			}
		} // end if feature != null
		return colladaRoot;
	}
	public void setLayerNode(KMLLayerTreeNode theLayerNode)
	{
		layerNode = theLayerNode;
	}
	public KMLLayerTreeNode getLayerNode()
	{
		return layerNode;
	}
	public boolean hasController()
	{
		return kmlController != null;
	}
	// NOT used for sprite  kml
	public KMLController getKmlController()
	{
		if (kmlController == null) 
			kmlController = new KMLController(kmlRoot);	
		 
		return kmlController;
	}
	public String getName()
	{
		return this.name;
	}
	public String getMenuName()
	{
		// If we've loaded the kml from the menu, set the menu item to the file name only
		// otherwise we have a user defined kml name..
		if (getName().contains(System.getProperty("file.separator")))
		{
			return getKmlFile().getName();
		}		
		return this.name;
	}
	public File getKmlFile()
	{
		return kmlFile;
	}
	public KMLLookAt getLookAt()
	{
		return lookAt;
	}
	public JMenuItem getKmlMenuItem()
	{
		return kmlMenuItem;
	}
    
	public boolean setKmlFile(Object fileName)
	{
		// Used when kml is not associated with a node
		try {
			kmlRoot = KMLRoot.createAndParse(fileName);
			if (kmlRoot == null)
			{
				System.out.println("Unable to parse kml file " + fileName);
				return false;
            }
 			kmlRoot.setField(AVKey.DISPLAY_NAME, formName(kmlFile, kmlRoot));
             
			KMLAbstractFeature kmlFeature = kmlRoot.getFeature();
			lookAt = (KMLLookAt) kmlFeature.getField("AbstractView");
            kmlController = new KMLController(kmlRoot);	
            // Keep a reference to the menu items so we can delete them
    		kmlMenuItem = new JMenuItem(getName());
 
            return true;
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	protected static String formName(Object kmlSource, KMLRoot kmlRoot)
	{
		KMLAbstractFeature rootFeature = kmlRoot.getFeature();

		if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName()))
			return rootFeature.getName();

		if (kmlSource instanceof File)
			return ((File) kmlSource).getName();

		if (kmlSource instanceof URL)
			return ((URL) kmlSource).getPath();

		if (kmlSource instanceof String && WWIO.makeURL((String) kmlSource) != null)
			return WWIO.makeURL((String) kmlSource).getPath();

		return "KML Layer ";
	}

		public KMLRoot getKmlRoot() {
			return kmlRoot;
		}	
		public boolean isValid() {return kmlRoot != null;}
}
