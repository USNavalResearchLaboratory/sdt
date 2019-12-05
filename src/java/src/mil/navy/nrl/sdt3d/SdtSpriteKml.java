package mil.navy.nrl.sdt3d;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JMenuItem;
import javax.xml.stream.XMLStreamException;

import gov.nasa.worldwind.WorldWind;
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

public class SdtSpriteKml extends SdtSprite
{
	String fileName = null;

	File kmlFile = null;

	String userDir = System.getProperty("user.dir");

	// The node stores its own copies of kmlController, kmlRoot, and colladaRoot
	// These references are used by the "stand-alone" kml renderables.
	KMLController kmlController = null;

	KMLRoot kmlRoot = null;

	ColladaRoot colladaRoot = null;

	KMLLookAt lookAt = null;

	JMenuItem kmlMenuItem = null;

	KMLLayerTreeNode layerNode = null;

	// The model p/y/r fiels are set by any xml config file associated with
	// the sprite. They are added to any node position orientation settings
	// during the node rendering pass.
	private Double modelPitch = 999.0;

	private Double modelYaw = 999.0;

	private Double modelRoll = 999.0;

	boolean isRealSize = true;

	public SdtSpriteKml(SdtSprite template)
	{
		super(template);
	}


	public SdtSpriteKml(String name)
	{
		super(name);
	}


	@Override
	public void whoAmI()
	{
		System.out.println("I am a kml sprite");
	}


	public KMLRoot initializeKmlRoot()
	{
		KMLRoot kmlRoot = null;
		try
		{
			kmlRoot = KMLRoot.createAndParse(fileName);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XMLStreamException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kmlRoot;
	}


	public ColladaRoot getColladaRoot(KMLRoot kmlRoot)
	{
		ColladaRoot colladaRoot = null;
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
								if (((KMLModelPlacemarkImpl) r).getColladaRoot() != null)
								{
									colladaRoot = ((KMLModelPlacemarkImpl) r).getColladaRoot();
									if (colladaRoot != null)
									{
										colladaRoot.setModelScale(this.getModelScale());
										// The kml renderer does it's own terrain position computations
										// so set the root to absolute and use our calcs from
										// the node render pass
										colladaRoot.setAltitudeMode(WorldWind.ABSOLUTE);

									}
								}
							}
						}
					}
				}
			}
		} // end if feature != null
		return colladaRoot;
	}


	public double getSymbolSize()
	{
		double size = iconWidth > iconHeight ? iconWidth : iconHeight;
		size = getFixedLength() > size ? getFixedLength() : size;
		return size;
	}
	
	public double getPitch()
	{
		if (this.modelPitch != 999.0)
		{
			return this.modelPitch;
		}
		return 0.0;
	}


	@Override
	public double getYaw()
	{
		// Sprites are offset by 180 degrees
		if (this.modelYaw != 999.0)
		{
			return this.modelYaw + 180.0;
		}
		return 180.0;
	}


	public double getRoll()
	{
		if (this.modelRoll != 999.0)
		{
			return this.modelRoll;
		}
		return 0.0;
	}


	public void setModelPitch(Double pitch)
	{
		this.modelPitch = pitch;
	}


	public void setModelYaw(Double yaw)
	{
		this.modelYaw = yaw;
	}


	public void setModelRoll(Double roll)
	{
		this.modelRoll = roll;
	}


	// Collada root requires model scale in Vec4
	public Vec4 getModelScale()
	{
		Double x = (double) scale;
		Double y = x;
		Double z = x;
		Vec4 modelScale = new Vec4(
			x != null ? x : 1.0,
			y != null ? y : 1.0,
			z != null ? z : 1.0);

		return modelScale;

	}


	public String getFileName()
	{
		return fileName;
	}


	public void setKmlFilename(String fileName)
	{
		this.fileName = fileName;
	}
	
	@Override
	public void setRealSize(boolean isRealSize)
	{
		this.isRealSize = isRealSize;
	}
	
	
	/*
	 * modelRadius is used by SdtSpriteModel::computeSizeScale()	
	 * to get size
	 */
	
	// Called by node rendering function
	public void computeSizeScale(DrawContext dc, ColladaRoot nodeColladaRoot, Position position)
	{
		if (getFixedLength() > 0.0 && isRealSize)
		{
			// if "real-world" size use fixed length
			double localSize = getFixedLength();
			Double scale = (double) getScale();
			Vec4 modelScaleVec = new Vec4(localSize * scale, localSize * scale, localSize * scale);
			nodeColladaRoot.setModelScale(modelScaleVec);

		}
		else
		{
			Vec4 loc = dc.getGlobe().computePointFromPosition(position);
			if (	loc == null)
				return;
			
			double d = loc.distanceTo3(dc.getView().getEyePoint());
			double pSize = dc.getView().computePixelSizeAtDistance(d);			

			// First see if psize is less than our fixed length
			double fixedLength = getFixedLength();
			double width = (iconWidth > iconHeight) ? iconWidth : iconHeight;
			if (fixedLength < 0.0 && width > 0) fixedLength = iconWidth;
			
			pSize = pSize * fixedLength;
			if (pSize < fixedLength)
			{
				pSize = fixedLength;
			}
			else
			{
				// If not calculate psize for iconWidth
				d = loc.distanceTo3(dc.getView().getEyePoint());
				pSize = dc.getView().computePixelSizeAtDistance(d);			
				width = (iconWidth > iconHeight) ? iconWidth : iconHeight;
				pSize = pSize * width;
				if (pSize < width)
					pSize = width;
			}

			// Finally scale the model 
			
			// TODO: scale is not working properly - models get turned upside down
			// and scale size behaves erractically (when same kml is loaded
			// multiple times??)
			Double scale = (double) getScale();
			Vec4 modelScaleVec = new Vec4(pSize * scale, pSize * scale, pSize * scale);
			nodeColladaRoot.setModelScale(modelScaleVec);
		}
	}


	// Called by node rendering function
	@Override
	public void setHeading(double nodeYaw, double newHeading, ColladaRoot nodeColladaRoot)
	{
		if (useAbsoluteYaw())
		{
			nodeColladaRoot.setHeading(Angle.fromDegrees((getYaw() + nodeYaw)));
		}
		else
		{
			nodeColladaRoot.setHeading(Angle.fromDegrees((getYaw() + nodeYaw + SdtNode.normalize(newHeading))));

		}
	} // setHeading()


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


	public String getMenuName()
	{
		// If we've loaded the kml from the menu, set the menu item to the file name only
		// otherwise we have a user defined kml name..
		if (getName().contains(System.getProperty("file.separator")))
		{
			return getKmlFile().getName();
		}
		return getName();
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
		try
		{
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

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (XMLStreamException e)
		{
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


	// Used by "fixed" kml objects
	public KMLController getKmlController()
	{
		if (kmlRoot == null)
			initializeKmlRoot();

		if (kmlRoot != null && kmlController == null)
			this.kmlController = new KMLController(kmlRoot);

		return kmlController;
	}


	// Used by "fixed" kml objects
	public KMLRoot getKmlRoot()
	{
		return kmlRoot;
	}


	public boolean isValid()
	{
		return kmlRoot != null;
	}
}
