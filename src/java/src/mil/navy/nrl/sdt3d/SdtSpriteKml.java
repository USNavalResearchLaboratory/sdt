/*********************************************************************
 *
 * AUTHORIZATION TO USE AND DISTRIBUTE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: 
 *
 * (1) source code distributions retain this paragraph in its entirety, 
 *  
 * (2) distributions including binary code include this paragraph in
 *     its entirety in the documentation or other materials provided 
 *     with the distribution.
 * 
 *      "This product includes software written and developed 
 *       by Code 5520 of the Naval Research Laboratory (NRL)." 
 *         
 *  The name of NRL, the name(s) of NRL  employee(s), or any entity
 *  of the United States Government may not be used to endorse or
 *  promote  products derived from this software, nor does the 
 *  inclusion of the NRL written and developed software  directly or
 *  indirectly suggest NRL or United States  Government endorsement
 *  of this product.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * For more information send email to sdt_info@nrl.navy.mil
 *
 *
 * WWJ code:
 * 
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 ********************************************************************/

package mil.navy.nrl.sdt3d;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JMenuItem;
import javax.xml.stream.XMLStreamException;

import gov.nasa.worldwind.WorldWind;
/*!
 * Known KML limitations:
 * 
 *  1. Images (icons) can not be associated with placemarks
 *  2. Have been having trouble getting hrefs to work correctly...
 *  
 *  /
 */
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLLookAt;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.kml.impl.KMLModelPlacemarkImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.KMLLayerTreeNode;

public class SdtSpriteKml extends SdtSpriteModel 
{
	// KMLController manages rendering of the collada root
	KMLController kmlController = null;

	KMLRoot kmlRoot = null;

	ColladaRoot colladaRoot = null;
		
	// For kml not associated with a node
	File kmlFile = null;

	KMLLookAt lookAt = null;

	JMenuItem kmlMenuItem = null;

	KMLLayerTreeNode layerNode = null;
	
	Vec4 modelScaleVector = new Vec4(0.0, 0.0, 0.0, 0.0);

	public SdtSpriteKml(SdtSpriteKml template)
	{
		super(template);
		this.spriteType = Type.KML;
	}


	public SdtSpriteKml(String name)
	{
		super(name);
		this.spriteType = Type.KML;
	}

	
	public SdtSpriteKml(SdtSprite template) 
	{
		super(template);
		this.spriteType = Type.KML;
	}


	public KMLRoot initializeKmlRoot()
	{
		KMLRoot kmlRoot = null;
		try
		{
			if (spritePath != null)
			{
				kmlRoot = KMLRoot.createAndParse(spritePath);
			}
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

	
	@Override
	public ColladaRoot getColladaRoot()
	{
		return colladaRoot;
	}
	
	
	ColladaRoot getColladaRootFromPlacemark(KMLPlacemark feature)
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
		return colladaRoot;
	}
	
	
	/*
	 * Kml collada roots cannot be shared as 3d model meshs can.
	 */
	public ColladaRoot getColladaRootFromKmlRoot(KMLRoot kmlRoot)
	{
		if (colladaRoot != null)
			return colladaRoot;
		
		KMLAbstractFeature feature = kmlRoot.getFeature();
		
		if (feature instanceof KMLPlacemark)
		{
			colladaRoot = getColladaRootFromPlacemark((KMLPlacemark)feature);
		}
		else if (feature instanceof KMLAbstractContainer)
		{
			for (KMLAbstractFeature abstractFeature : ((KMLAbstractContainer) feature).getFeatures())
			{
				if (abstractFeature instanceof KMLPlacemark)
				{
					colladaRoot = getColladaRootFromPlacemark((KMLPlacemark) abstractFeature);
				}
			}
		}
		
		return colladaRoot;
	}


	public double getPitch()
	{
		if (modelPitch != 999.0)
		{
			return modelPitch;
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
		if (modelRoll != 999.0)
		{
			return modelRoll;
		}
		return 0.0;
	}


	/**
	 *  Collada root requires model scale in Vec4
	 * @return Vec4 modelScale
	 */
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
	

	@Override
	public void setFixedLength(double length)
	{
		fixedLength = length;
	}

	
	/*
	 * Called by rendering function
	 */	
	public Vec4 computeSizeVector(DrawContext dc, Vec4 loc)
	{		
		viewAtRealSize = false;
		Vec4 modelScaleVec;
		if (getFixedLength() > 0.0 && isRealSize)
		{
			// if "real-world" size use fixed length
			double localSize = getFixedLength();
			Double scale = (double) getScale();
			modelScaleVec = new Vec4(localSize * scale, localSize * scale, localSize * scale);
		}
		else
		{			
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
			modelScaleVec = new Vec4(pSize * scale, pSize * scale, pSize * scale);
		}
		
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double pSize = dc.getView().computePixelSizeAtDistance(d);

		modelRadius = modelScaleVec.x * pSize;

		if (modelRadius < minimumSizeScale)
		{
			viewAtRealSize = true;

		}

		return modelScaleVec;
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


	/**
	 * Note: Each kmlController that manages its own collada root.  
	 * The collada root is be created in the first prerender rendering pass.
	 */
	@Override
	public KMLController getKmlController()
	{
		if (kmlRoot == null)
			kmlRoot = initializeKmlRoot();

		if (kmlRoot != null && kmlController == null)
			this.kmlController = new KMLController(kmlRoot);

		return kmlController;
	}


	/**
	 *  Used by "fixed" kml objects
	 * @return
	 */
	public KMLRoot getKmlRoot()
	{
		return kmlRoot;
	}

	
	@Override
	public void render(DrawContext dc) 
	{
		if (getColladaRoot() == null)
		{
			if (getKmlController() != null)
			{
				getKmlController().preRender(dc);
			}
		}
		
		/*
		 * Now that we've prerendered the KML try get the
		 * colladaRoot.
		 */
		if (colladaRoot == null)
		{
			colladaRoot = getColladaRootFromKmlRoot(kmlController.getKmlRoot());
		}
		
		if (colladaRoot == null)
		{
			return;
		}
		
		Vec4 loc = dc.getGlobe().computePointFromPosition(position);

		// Set model position & size
		colladaRoot.setPosition(position);
		modelScaleVector = this.computeSizeVector(dc, loc);
		colladaRoot.setModelScale(modelScaleVector);

		// Set model orientation
		// Change kml heading to clockwise like models
		colladaRoot.setHeading(Angle.fromDegrees(-heading));;
		// kml roll is the reverse of models (and our default)
		colladaRoot.setRoll(Angle.fromDegrees(-(roll + getModelRoll())));
		colladaRoot.setPitch(Angle.fromDegrees(pitch + getModelPitch()));
		
		Vec4 modelPoint = null;
		if (position.getElevation() < dc.getGlobe().getMaxElevation())
			modelPoint = dc.getSurfaceGeometry().getSurfacePoint(position);
		if (modelPoint == null)
			modelPoint = dc.getGlobe().computePointFromPosition(position);

		Vec4 screenPoint = dc.getView().project(modelPoint);
		Vec4 modelScale = colladaRoot.getModelScale();
		Rectangle rect = new Rectangle((int) (screenPoint.x), (int) (screenPoint.y),
			(int) (modelScale.x), (int) (modelScale.y));

		this.recordFeedback(dc, this, modelPoint, rect);
		kmlController.render(dc);
	}

	
	@Override
	boolean isValid()
	{
		return colladaRoot != null;
	}

	
	/*
	 * Called when we set the size or the fixed length of the 
	 * model.  The modelRadius is used by the SdtSpriteModel()
	 * computeSizeScale to compute model pixel size during
	 * rendering
	 */
	@Override
	void setModelRadius()
	{		

		double lengthInMeters = getLength();

		sizeScale = lengthInMeters;
		minimumSizeScale = sizeScale * getScale();

		// We override the minimumSizeScale to the "fixed length" if 
		// hybrid sizing has been set.
		if (getFixedLength() > 0 && getWidth() > 0)
		{
			minimumSizeScale = getFixedLength() * getScale(); 
		}

	} // end WWModel3D.setLength()

	
	
	@Override
	double getSymbolSize(DrawContext d)
	{
		if (isRealSize())
		{
			double realScale =  Math.sqrt(3 * (getFixedLength() * modelScaleVector.x) *
					(getFixedLength() * modelScaleVector.x));
			return realScale;
		}
		
		modelRadius = Math.sqrt(3 * (modelScaleVector.x * sizeScale) *
					(modelScaleVector.x * sizeScale)) / 2.0;
		
		// For NOW they are all the same
		return modelRadius / 2.0;
	}
}
