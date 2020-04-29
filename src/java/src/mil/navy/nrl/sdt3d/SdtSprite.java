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
import gov.nasa.worldwind.render.UserFacingIcon;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

public class SdtSprite
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
	
	private String spritePath = null; // path to validate sprite source

	private java.net.URL iconURL = null; // path to images retrieved from jar files

	public java.net.URL getIconURL()
	{
		return this.iconURL;
	}


	public SdtSprite(String name)
	{
		this.spriteName = name;
	}
	
	protected float scale = 1;	

	
	public SdtSprite(SdtSprite template)
	{
		// TODO Auto-generated constructor stub
		this.spriteName = template.spriteName;
		this.spriteType = template.spriteType;
		this.fixedLength = template.fixedLength;
		this.spritePath = template.spritePath;
		this.iconWidth = template.iconWidth;
		this.iconHeight = template.iconHeight;
		this.imageWidth = template.imageWidth;
		this.imageHeight = template.imageHeight;
		this.scale = template.scale;
		this.iconURL = template.iconURL;
		this.spriteType = template.spriteType;
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
	SdtSprite load(String spritePath) throws IOException
	{
		// First see if it is an xml configuration file pointing
		// to the model and setting orientation
		if (spritePath.endsWith(".xml") | spritePath.endsWith(".XML"))
		{
			spritePath = sdt3d.AppFrame.findFile(spritePath);
			return LoadXMLFile(spritePath);
		}

		imageWidth = 0;
		imageHeight = 0;

		SdtSprite theSprite = loadModel(spritePath);
		if (theSprite != null)
		{
			return theSprite;
		}
		
		// TODO: fix this
		// Double check that the sprite exists now that we've checked the jar file
		// for the given path.
		spritePath = sdt3d.AppFrame.findFile(spritePath);
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
	
	
	private SdtSprite loadImage(String spritePath)
	{
		// Using an ImageIcon to load the image will block until the
		// image is loaded (as opposed to getImage) but then we will
		// have the dimensions of the image available to us
		// Toolkit toolkit = Toolkit.getDefaultToolkit();
		// Image image = toolkit.getImage(spritePath);

		
		ImageIcon img = new ImageIcon(spritePath);
		Image image = img.getImage();
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
				case 1: // non-zero height, free-form width
					double scale = (double) iconHeight / (double) imageHeight;
					iconWidth = (int) (scale * imageWidth + 0.5);
					break;
				case 2: // non-zero width, free-form height
					scale = (double) iconWidth / (double) imageWidth;
					iconHeight = (int) (scale * imageHeight + 0.5);
					break;
				case 3: // free-form width and height (use default size for min dimension
					if (imageWidth < imageHeight)
					{
						scale = 32.0 / imageWidth;
						iconWidth = 32;
						iconHeight = (int) (scale * imageHeight);
					}
					else
					{
						scale = 32.0 / imageHeight;
						iconHeight = 32;
						iconWidth = (int) (scale * imageWidth);
					}
					break;
			}

			SdtSpriteIcon spriteIcon = new SdtSpriteIcon(this);
			spriteIcon.setType(Type.ICON);
			spriteIcon.setSpritePath(spritePath);
			return spriteIcon;
		}
		return null;

	} // SdtSprite::loadImage
	
	
	private SdtSprite loadModel(String spritePath)
	{
		Model theModel = null;

		try
		{
			String fileName = sdt3d.AppFrame.findFile(spritePath);
			String url = fileName;

			if (fileName == null)
			{
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
			theModel = LoaderFactory.load(url, Orientation.Y_AXIS);
		}
		catch (ModelLoadException e)
		{
			e.printStackTrace();
		}	
	
		if (null != theModel)
		{
			// clean up
			SdtSpriteModel spriteModel = new SdtSpriteModel(this);
			spriteModel.setModel(theModel);
			spriteModel.setType(Type.MODEL);
			spriteModel.setSize(spriteModel.getIconSize().width,
			spriteModel.getIconSize().height);
		
			// We need the spritePath so we can reload the model when
			// we assign the model to the node.
			System.out.println("Fix this broken model code");
			spriteModel.setSpritePath(spritePath);

			return spriteModel;
		}
	
		return null;
	}
	
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

				setSpriteDimensions(el, theSprite);
				
				if (theSprite instanceof SdtModelDimensions)
				{
					setSpriteModelAttributes(el, (SdtModel) theSprite, type);
				}

				return theSprite;
			}
		}

		return null;
	}

	/*
	 * Set dimensions common to all sprites
	 */
	private void setSpriteDimensions(Element el, SdtSprite theSprite)
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
				String[] dim = size.split(",");
				// <dimensions> is in format "width,height"
				if (dim.length == 2)
				{
					Integer width = new Integer(dim[0]);
					Integer height = new Integer(dim[1]);
					theSprite.setSize(width.intValue(), height.intValue()); 
				}
				else
				{
					System.out.println("SdtSprite::setDimension() Bad size dimensions in sprite kml.\n");
				}
			}
			
		} 
		catch (Exception e)
		{
			System.out.println("SdtSprite::setSpriteDimensions() invalid kml file");
		}
	}

	/**
	 * I take a xml element and the tag name, look for the tag and get
	 * the text content
	 * i.e for <employee><name>John</name></employee> xml snippet if
	 * the Element points to employee node and tagName is 'name' I will return John
	 */
	private String getTextValue(Element ele, String tagName)
	{
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			Element el = (Element) nl.item(0);
			if (el.getFirstChild() != null)
				textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	
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


	
	private void setSpriteModelAttributes(Element el, SdtModel theSprite, String type)
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
				|| spritePath.endsWith("dae")
			|| spritePath.endsWith("KML") || spritePath.endsWith("KMZ"))
		{
			// Note - we have to create a separate kml root for each node given
			// the current wwj kml code. The collada root will be created in
			// the first rendering pass by the kml function that fires (ljt details)
			SdtSpriteKml spriteKml = new SdtSpriteKml(this);
			// Move up to sprite?
			spriteKml.setKmlFilename(spritePath);
			spriteKml.setType(Type.KML);
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

	// Load sprite from jar
	boolean LoadURL(java.net.URL spritePath) throws IOException
	{
		//iconWidth = -32;
		//iconHeight = -32;
		imageWidth = 0;
		imageHeight = 0;

		ImageIcon img = new ImageIcon(spritePath);
		Image image = img.getImage();
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
				case 1: // non-zero height, free-form width
					double scale = (double) iconHeight / (double) imageHeight;
					iconWidth = (int) (scale * imageWidth + 0.5);
					break;
				case 2: // non-zero width, free-form height
					scale = (double) iconWidth / (double) imageWidth;
					iconHeight = (int) (scale * imageHeight + 0.5);
					break;
				case 3: // free-form width and height (use default size for min dimension
					if (imageWidth < imageHeight)
					{
						scale = 32.0 / imageWidth;
						iconWidth = 32;
						iconHeight = (int) (scale * imageHeight);
					}
					else
					{
						scale = 32.0 / imageHeight;
						iconHeight = 32;
						iconWidth = (int) (scale * imageWidth);
					}
					break;
			}

			spriteType = Type.ICON;
			iconURL = spritePath;

			return true;
		}

		spriteName = "";
		return false;
	} // end SdtSprite.LoadURL()


	public double getSymbolSize()
	{
		double size = iconWidth > iconHeight ? iconWidth : iconHeight;
		
		// if symbol size not set - use default 32??
		
		if (size <= 0)
		{
			size = 32.0;
		}
		
		return size;
		//return iconWidth > iconHeight ? iconWidth : iconHeight;
	}
		
	
	boolean isValid() {
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
	
	public UserFacingIcon getIcon(Position pos, String nodeName, boolean feedbackEnabled)
	{
		// TODO Auto-generated method stub
		return  null;
	}


}
