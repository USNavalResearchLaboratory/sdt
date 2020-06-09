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

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import builder.mil.nrl.atest.icon.Orientation;
import builder.mil.nrl.atest.worldwind.openflight.LoaderFactory;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.UserFacingIcon;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

public class SdtSprite implements Renderable
{
	protected String spriteName;

	public enum Type {
		MODEL, ICON, KML, NONE, INVALID
	}

	protected Type spriteType = Type.INVALID;

	// We need to define fixedLength in SdtSprite rather than SdtSpriteModel
	// as we don't know when processing sdt sprite setLength commands what 
	// kind of sprite we have.  (Loading the model gives us that info).
	protected double fixedLength = -1.0; // in meters

	// default icon size preserves source image aspect ratio
	// with a fixed minimum dimension of 32 pixels
	protected double iconWidth = -32;

	protected double iconHeight = -32;

	protected int imageWidth = 0;

	protected int imageHeight = 0;
	
	protected float scale = 1;	
	
	boolean applyOffset = false;
	
	private Float offsetX = 0.0f;
	
	private Float offsetY = 0.0f;
	
	private Float offsetZ = 0.0f;
	
	protected String spritePath = null; // path to validate sprite source

	private java.net.URL iconURL = null; // path to images retrieved from jar files

	
	public java.net.URL getIconURL()
	{
		return this.iconURL;
	}


	public SdtSprite(String name)
	{
		this.spriteName = name;
	}
	
	
	public SdtSprite(SdtSprite template)
	{
		this.spriteName = template.spriteName;
		this.spriteType = template.spriteType;
		this.fixedLength = template.fixedLength;
		this.iconWidth = template.iconWidth;
		this.iconHeight = template.iconHeight;
		this.imageWidth = template.imageWidth;
		this.imageHeight = template.imageHeight;
		this.scale = template.scale;
		this.spritePath = template.spritePath;
		this.iconURL = template.iconURL;
		this.offsetX = template.offsetX;
		this.offsetY = template.offsetY;
		this.offsetZ = template.offsetZ;
		this.applyOffset = template.applyOffset;
	}
	
	
	public void setSpritePath(String spritePath)
	{
		this.spritePath = spritePath;
	}

	
	public String getSpritePath()
	{
		return this.spritePath;
	}

	
	// Try to load it as a Model, kml/kmz, or an Icon, else use default Model
	SdtSprite load(String fileName) throws IOException
	{
		// First see if it is an xml configuration file pointing
		// to the model and setting orientation
		if (fileName.endsWith(".xml") | fileName.endsWith(".XML"))
		{
			spritePath = sdt3d.AppFrame.findFile(fileName);
			return LoadXMLFile(spritePath);
		}


		imageWidth = 0;
		imageHeight = 0;

		SdtSprite theSprite = loadModel(fileName);
		if (theSprite != null)
		{
			return theSprite;
		}
		
		// Double check that the file exists now that we've checked the jar file
		// for the given path.  Store the fully qualified name in the spritePath
		spritePath = sdt3d.AppFrame.findFile(fileName);
		if (spritePath == null)
		{
			System.out.println("SdtSprite::load() sprite file " + spritePath + " does not exist.");
			return null;
		}

		theSprite = loadKml(spritePath);
		if (theSprite != null)
		{
			return theSprite;
		}

		// It wasn't kml or a model, so lets see if it is a valid image file
		theSprite = loadImage(spritePath);
		if (theSprite != null)
		{
			return theSprite;
		}
		
		this.spriteName = "";
		return null;
		
	} // end SdtSprite.load()
	
	
	// Load sprite from jar
	SdtSprite LoadURL(java.net.URL spritePath) throws IOException
	{
		iconURL = spritePath;

		imageWidth = 0;
		imageHeight = 0;

		ImageIcon img = new ImageIcon(spritePath);
		Image image = img.getImage();
		if (image != null)
		{
			setSpriteDimensions(image);
		}
		SdtSpriteIcon spriteIcon = new SdtSpriteIcon(this);
		return spriteIcon;
	}

	
	private SdtSprite loadImage(String spritePath)
	{
		// Using an ImageIcon to load the image will block until the
		// image is loaded (as opposed to toolkit getImage) but then we will
		// have the dimensions of the image available to us
		// Toolkit toolkit = Toolkit.getDefaultToolkit();
		// Image image = toolkit.getImage(spritePath);
		
		ImageIcon img = new ImageIcon(spritePath);
		Image image = img.getImage();
		if (image != null)
		{
			setSpriteDimensions(image);
		}

		SdtSpriteIcon spriteIcon = new SdtSpriteIcon(this);
		return spriteIcon;
	}
	
	
	void setSpriteDimensions(Image image)
	{
		if (null != image)
		{
			imageWidth = image.getWidth(null);
			imageHeight = image.getHeight(null);

			int sizeRule = (iconWidth < 0) ? 1 : 0;
			sizeRule += (iconHeight < 0) ? 2 : 0;
			switch (sizeRule)
			{
				case 0: // non-zero width & height
					break;
				case 1: 
				{	
					// non-zero height, free-form width
					double scale = (double) iconHeight / (double) imageHeight;
					iconWidth = (int) (scale * imageWidth + 0.5);
					break;
				}
				case 2: 
				{
					// non-zero width, free-form height
					double scale = (double) iconWidth / (double) imageWidth;
					iconHeight = (int) (scale * imageHeight + 0.5);
					break;
				}
				case 3: 
				{
					// free-form width and height (use default size for min dimension
					if (imageWidth < imageHeight)
					{
						double scale = 32.0 / imageWidth;
						iconWidth = 32;
						iconHeight = (int) (scale * imageHeight);
					}
					else
					{
						double scale = 32.0 / imageHeight;
						iconHeight = 32;
						iconWidth = (int) (scale * imageWidth);
					}
					break;
				}
			}
		}
	} // SdtSprite::loadImage
	
	
	/**
	 * We load the model once.  DisplayListRenderer keeps track of
	 * initializing the model if not done so already.
	 */
	private SdtSprite loadModel(String spritePath)
	{
		Model theModel = null;
		
		try
		{
			// Check to see if the model exists on disk
			String fileName = sdt3d.AppFrame.findFile(spritePath);
			String url = fileName;

			// If not check the jar file.
			if (fileName == null)
			{
				// Check to see if it is a model in a jar file
				String jarFile[] = sdt3d.AppFrame.MODEL_JAR_FILE_PATH.split("jar:file:",2);
				if (jarFile.length == 2)
				{
					String jarFilePath = jarFile[1].replace("!","");
					File modelJarFile = new File(jarFilePath);
					if (!modelJarFile.exists())
					{
						// TODO: fix this when jar access is properly implemented
						// No file or jar file
						System.out.println("SdtSprite::loadModel() No model jar file");
						return null;
					}
				}
				
				url = sdt3d.AppFrame.MODEL_JAR_FILE_PATH + spritePath;
			}
			
			// Load the model
			theModel = LoaderFactory.load(url, Orientation.Y_AXIS);
		}
		catch (ModelLoadException e)
		{
			e.printStackTrace();
		}	
	
		if (null != theModel)
		{
			SdtSpriteModel spriteModel = new SdtSpriteModel(this);
			spriteModel.setModel(theModel);
			// Explicitly save our possibly jar relative sprite path
			// the main loading code saves a fully qualified path.
			spriteModel.setSpritePath(spritePath);
			return spriteModel;
		}
	
		return null;
	} // SdtSprite::LoadModel
	
	
	SdtSprite LoadXMLFile(String spritePath) throws IOException
	{
		Document doc = null;
		try
		{
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			// Parse using builder to get DOM representation of the xml
			doc = db.parse(spritePath);
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (SAXException se)
		{
			se.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		if (doc == null)
			return null;

		// Get the root element
		Element docElem = doc.getDocumentElement();
		NodeList nl = docElem.getElementsByTagName("model");

		// Perhaps we want to load a list of models and access the attributes
		// as they are assigned? At this point just get first element
		if (nl != null && nl.getLength() > 0)
		{
			for (int i = 0; i < nl.getLength(); i++)
			{
				// Get the first model element for now
				Element el = (Element) nl.item(i);
				String type = getTextValue(el, "type");
				if (type == null)
				{
					System.out.println("SdtSprite::LoadXMLFile(); invalid model type " + spritePath);
					return null;
				}

				String name = getTextValue(el, "file");
				if (name == null)
				{
					System.out.println("SdtSprite::LoadXMLFile() invalid file path in kml.");
				}
				
				SdtSprite theSprite = null;
				theSprite = load(name);

				if (theSprite == null)
				{
					System.out.println("SdtSprite::LoadXMLFile() no valid model found\n");
					return null;
				}

				setXMLSpriteDimensions(el, theSprite);
				
				if (theSprite instanceof SdtModelDimensions)
				{
					setXMLSpriteModelAttributes(el, (SdtModel) theSprite, type);
				}

				return theSprite;
			}
		}

		return null;
	}



	/**
	 * For an xml element and the name, return the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' 
	 * will return John.
	 */
	private String getTextValue(Element ele, String tagName)
	{
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			Element el = (Element) nl.item(0);
			if (el.getFirstChild() != null)
			{
				textVal = el.getFirstChild().getNodeValue();
			}
		}

		return textVal;
	}

	/**
	 * Validate that the tax exists in the element
	 * 
	 * @param ele An XML Element
	 * @param tagName String tagName
	 * @return
	 */
	private boolean hasTag(Element ele, String tagName)
	{
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			return true;
		}
		return false;
	}

	
	/**
	 * Calls getTextValue and returns a int value
	 */
	private int getIntValue(Element ele, String tagName)
	{
		// in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele, tagName));
	}

	/*
	 * Set any sprite offset to be applied
	 * to node position
	 */
	void setOffset(Float x, Float y, Float z)
	{
		this.offsetX = x;
		this.offsetY = y;
		this.offsetZ = z;
		this.applyOffset = true;
	}
	
	
	void disableOffset()
	{
		this.applyOffset = false;
	}

	
	boolean hasOffset()
	{
		return this.applyOffset;
	}
	
	Position getOffsetPosition(Position nodePosition)
	{		
		if (!applyOffset)
		{
			return nodePosition;
		}
		
		String origLatStr = nodePosition.getLatitude().toDecimalDegreesString(14);
		origLatStr = origLatStr.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", "");
		double origLat = Double.parseDouble(origLatStr);

		String origLonStr = nodePosition.getLongitude().toDecimalDegreesString(14);
		origLonStr = origLonStr.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", "");
		double origLon = Double.parseDouble(origLonStr);

		// average radius of the earth from center to pole
		double latsPerDeg = 111226.0;
		double latsRel = offsetY / latsPerDeg;
		double lat = origLat + latsRel;

		// normalize the latitude... but dont account for pole crossings yet...
		while (lat < -180)
			lat += 360;
		while (lat > 180)
			lat -= 360;

		// Radius of the earth from center to the equator...
		double lonsPerDeg = Math.cos(Math.PI * lat / 180) * 111320;
		double lonRel = offsetX / lonsPerDeg;
		double lon = origLon + lonRel;

		// handle pole crossings...
		if (lat > 90)
		{
			lat = 180 - lat;
			lon += 180;
		}
		else if (lat < -90)
		{
			lat = -180 - lat;
			lon += 180;
		}

		// normalize the longitude
		while (lon < 180)
			lon += 360;
		while (lon > 180)
			lon -= 360;
		
		Double altOffset = nodePosition.getElevation() + offsetZ;

		Position newPos = Position.fromDegrees(lat, lon, altOffset);
		return newPos;
	}

	/**
	 * 
	 * Set dimensions common to all sprites
	 */
	private void setXMLSpriteDimensions(Element el, SdtSprite theSprite)
	{
		try 
		{
			// get the length attribute
			int length = 0;
			if (hasTag(el, "length"))
			{
				length = getIntValue(el, "length");
			}
			if (length > 0)
			{
				theSprite.setFixedLength(length);
			}
		
			int scale = 0;
			if (hasTag(el, "scale"))
			{
				scale = getIntValue(el, "scale");
			}
			if (scale > 0)
			{
				theSprite.setScale(scale);
			}
		
			String size = null;
			if (hasTag(el, "size"))
			{
				size = getTextValue(el, "size");
			}
			if (size != null)
			{
				Integer width = -1;
				Integer height = -1;
				String[] dim = size.split(",");
				// <dimensions> is in format "width,height"
				if (dim.length == 1)
				{
					width = new Integer(dim[0]);
					height = new Integer(dim[0]);
				} 
				if (dim.length == 2)
				{
					width = new Integer(dim[0]);
					height = new Integer(dim[1]);
				}
				theSprite.setSize(width.intValue(), height.intValue()); 
				theSprite.setRealSize(false);
			}
			String offset = null;
			if (hasTag(el, "offset"))
			{
				offset = getTextValue(el, "offset");
			}
			if (offset != null)
			{
				String[] offsetArr = offset.split(",");
				// "x,y,z"
				if (offsetArr.length == 3)
				{
					if (!offsetArr[0].equalsIgnoreCase("x"))
						theSprite.offsetX = new Float(offsetArr[0]);
					if (!offsetArr[1].equalsIgnoreCase("x"))
						theSprite.offsetY = new Float(offsetArr[1]);
					if (!offsetArr[2].equalsIgnoreCase("x"))
						theSprite.offsetZ = new Float(offsetArr[2]);
					theSprite.applyOffset = true;
				}
				else
				{
					System.out.println("SdtSprite::setDimension() bad offset in sprite xml.");
				}
			}
		} 
		catch (Exception e)
		{
			System.out.println("SdtSprite::setSpriteDimensions() invalid xml file");
		}
	}
	
	
	private void setXMLSpriteModelAttributes(Element el, SdtModel theSprite, String type)
	{
		// Initially use the icon size to calculate the model length in
		// case no model length is given
		theSprite.setSize(theSprite.getIconSize().width,
				theSprite.getIconSize().height);

		String lighting = getTextValue(el, "light");
		
		if (lighting != null)
		{
			if (lighting.equalsIgnoreCase("on"))
			{
				theSprite.setUseLighting(true);
			}
			else 
			{
				theSprite.setUseLighting(false);
			}
		}
		
		// set orientation
		String orientation = getTextValue(el, "orientation");
		String[] coord = orientation.split(",");
		if (coord.length > 0 && !coord[0].equalsIgnoreCase("x"))
		{
			theSprite.setModelPitch(new Double(coord[0]));
		}
		
		// set yaw
		if (coord.length > 1 && !coord[1].equalsIgnoreCase("x"))
		{
			if (coord[1].endsWith("a"))
			{
				coord[1] = coord[1].replace("a", "");
				theSprite.setAbsoluteYaw(true);
			}
			else if (coord[1].endsWith("r"))
			{
				coord[1] = coord[1].replace("r", "");
				theSprite.setAbsoluteYaw(false);
			}
			theSprite.setModelYaw(new Double(coord[1]));
		}

		// set roll
		if (coord.length > 2 && !coord[2].equalsIgnoreCase("x"))
		{
			theSprite.setModelRoll(new Double(coord[2]));
		}
	}

	
	private SdtSprite loadKml(String spritePath)
	{
		if (spritePath.endsWith("kml") || spritePath.endsWith("kmz")
			|| spritePath.endsWith("KML") || spritePath.endsWith("KMZ"))
		{
			SdtSpriteKml spriteKml = new SdtSpriteKml(this);
			return spriteKml;

		}
		return null;
		
	} // SdtSprite.loadKml()
	
	

	
	public void setSize(double width, double height)
	{
		if (width < 0 && height < 0)
		{
			// System.out.println("Invalid icon dimension");
			return;
		}
		float scaleHeight = 0;
		float scaleWidth = 0;
		if (width < 0)
		{
			scaleHeight = (float) height / imageHeight;
			scaleWidth = scaleHeight;
		}
		if (height < 0)
		{
			scaleWidth = (float) width / imageWidth;
			scaleHeight = scaleWidth;
		}
		if (scaleWidth > 0 && scaleHeight > 0)
		{
			iconWidth = Math.round(imageWidth * scaleWidth);
			iconHeight = Math.round(imageHeight * scaleWidth);
		}
		else
		{
			iconWidth = width;
			iconHeight = height;
		}
		// TODO: dynamically re-calculate these depending
		// upon values and imageWidth/imageHeight
		if (scale > 0)
		{
			iconWidth = (int) (iconWidth * scale);
			iconHeight = (int) (iconHeight * scale);
		}
	}
	
	
	public void setScale(float theScale)
	{
		this.scale = theScale;
		// Reset icon to original dimensions
		if (scale == 1)
		{
			iconWidth = (int) (iconWidth / scale);
			iconHeight = (int) (iconHeight / scale);
		}
		if (scale > 1)
		{
			iconWidth = (int) (iconWidth * scale);
			iconHeight = (int) (iconHeight * scale);
		}
	}

	double getWidth()
	{
		return iconWidth;
	}


	double getHeight()
	{
		return iconHeight;
	}
	
	
	Dimension getIconSize()
	{
		return new Dimension((int) iconWidth, (int) iconHeight);
	}


	
	public float getScale()
	{
		return scale;
	}

	public String getName()
	{
		return this.spriteName;
	}


	public Type getType()
	{
		return this.spriteType;
	}


	public void setType(Type theType)
	{
		this.spriteType = theType;
	}


	public double getFixedLength()
	{
		return fixedLength;
	}


	public void setFixedLength(double length)
	{
		fixedLength = length;
	}


	public double getSymbolSize()
	{
		double size = iconWidth > iconHeight ? iconWidth : iconHeight;
		
		// if symbol size not set - use default
		if (size <= 0)
		{
			size = 32.0;
		}
		
		return size;
	}
		
	
	boolean isValid() 
	{
		return false;
	}


	public boolean isRealSize() {
		// TODO Auto-generated method stub
		return false;
	}


	protected void setPosition(Position modelPosition) {
		// TODO Auto-generated method stub
		
	}


	public void setHeading(double heading, double yaw) {
		// TODO Auto-generated method stub
		
	}


	public void setRoll(double roll) {
		// TODO Auto-generated method stub
		
	}


	public void setPitch(double pitch) {
		// TODO Auto-generated method stub
	}


	public boolean useAbsoluteYaw() {
		// TODO Auto-generated method stub
		return false;
	}


	public void setRealSize(boolean b) {
		// TODO Auto-generated method stub
		
	}


	public void setAbsoluteYaw(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	public UserFacingIcon getIcon()
	{
		// TODO Auto-generated method stub
		return  null;
	}


	public void render(DrawContext dc) {
		// TODO Auto-generated method stub
		
	}


	public void loadIcon(Position position, String nodeName, boolean feedbackEnabled) {
		// TODO Auto-generated method stub
		
	}

	
	public double computeSizeScale(DrawContext dc, Vec4 loc) {
		// TODO Auto-generated method stub
		return 0;
	}


	double getSymbolSize(DrawContext dc) 
	{	
		return 0.0;
	}
}
