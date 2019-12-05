package mil.navy.nrl.sdt3d;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

public class SdtSprite
{
	public enum Type {
			MODEL, ICON, KML, NONE, INVALID
	}

	private String spriteName;

	private Type spriteType = Type.INVALID;

	// We need to define fixedLength in SdtSprite rather than SdtSpriteModel
	// as we don't know when processing sdt sprite setLength commands what 
	// kind of sprite we have.  (Loading the model gives us that info).
	protected double fixedLength = -1.0; // in meters

	private String spritePath = null; // path to validate sprite source

	private java.net.URL iconURL = null; // path to images retrieved from jar files
	
	// default icon size preserves source image aspect ratio
	// with a fixed minimum dimension of 32 pixels
	protected double iconWidth = -32;

	protected double iconHeight = -32;

	private int imageWidth = 0;

	private int imageHeight = 0;

	protected float scale = 1;	
	
	// Default to useAbsoluteYaw to false so any node heading will be used
	// if no orientation is set
	private boolean useAbsoluteYaw = false;


	public void setAbsoluteYaw(boolean useAbsolute)
	{
		this.useAbsoluteYaw = useAbsolute;
	}


	public boolean useAbsoluteYaw()
	{
		return this.useAbsoluteYaw;
	}


	public boolean isRealSize()
	{
		// Only applies to models.
		return false;
	}


	public SdtSprite(String name)
	{
		this.spriteName = name;
	}


	public SdtSprite(SdtSprite template)
	{
		// TODO Auto-generated constructor stub
		this.spriteName = template.spriteName;
		this.spriteType = template.spriteType;
		this.fixedLength = template.fixedLength;
		this.spritePath = template.spritePath;
		this.iconURL = template.iconURL;
		this.iconWidth = template.iconWidth;
		this.iconHeight = template.iconHeight;
		this.imageWidth = template.imageWidth;
		this.imageHeight = template.imageHeight;
		this.scale = template.scale;
		this.useAbsoluteYaw = template.useAbsoluteYaw;

	}


	public void whoAmI()
	{
		System.out.println("I am am icon sprite");
	}


	public double getYaw()
	{
		System.out.println("getYaw not implemented for non 3d sprites");
		return 0.0;
	}


	public void setHeading(double newHeading, double nodeYaw, ColladaRoot theParam)
	{
		System.out.println("setHeading() not implemented for non 3d sprites\n");
	}


	public void setRoll(double roll)
	{
		// overriden in model sprites
		System.out.println("setRoll() not implemented for non 3d sprites\n");
	}


	public double getModelRoll()
	{

		return 0.0;
	}


	public void setPitch(double pitch)
	{
		// overridden in model sprites
		System.out.println("setPitch() not implemented for non 3d sprites\n");
	}


	public double getModelPitch()
	{
		return 0.0;
	}


	public UserFacingIcon getIcon(Position pos, String nodeName, boolean feedbackEnabled)
	{
		UserFacingIcon icon = null;
		if (getIconURL() != null)
		{
			try
			{
				BufferedImage image = ImageIO.read(getIconURL());
				icon = new UserFacingIcon(image, pos);

			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			icon = new UserFacingIcon(getSpritePath(), pos);
			icon.setHighlightScale(1.5);
			// icon.setToolTipFont(font); // TODO pretty up with a nice font
			icon.setToolTipText(nodeName);
			icon.setToolTipTextColor(java.awt.Color.YELLOW);
			icon.setSize(getIconSize());
			icon.setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);
		}

		return icon;
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


	public String getSpritePath()
	{
		return this.spritePath;
	}


	public java.net.URL getIconURL()
	{
		return this.iconURL;
	}


	Dimension getIconSize()
	{
		return new Dimension((int) iconWidth, (int) iconHeight);
	}


	double getWidth()
	{
		return iconWidth;
	}


	double getHeight()
	{
		return iconHeight;
	}
	
	double getLength()
	{
		// no length for icon sprites
		return iconWidth;
	}
	
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


	public float getScale()
	{
		return scale;
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

			this.spriteType = Type.ICON;
			this.iconURL = spritePath;

			return true;
		}

		this.spriteName = "";
		return false;
	} // end SdtSprite.LoadURL()


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


	/**
	 * Calls getTextValue and returns a int value
	 */
	private int getIntValue(Element ele, String tagName)
	{
		// in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele, tagName));
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

				// The xml file name may or may not be fully
				// qualified - check to make sure we have a full path
				String fileName = sdt3d.AppFrame.findFile(name);
				if (fileName == null)
					return null;

				SdtSprite theSprite = null;
				if (type.equalsIgnoreCase("kml"))
				{
					// Try load the kml model
					theSprite = Load(fileName);
				}
				else if (type.equalsIgnoreCase("3ds"))
				{
					theSprite = Load(fileName);

				}
				if (theSprite == null)
				{
					System.out.println("SdtSprite::LoadXMLFile() no valid model found\n");
					return null;
				}

				// get the length attribute
				int length = getIntValue(el, "length");
				if (length > 0)
					theSprite.setFixedLength(length);

				int scale = getIntValue(el, "scale");
				if (scale > 0)
					theSprite.setScale(scale);

				String size = getTextValue(el, "size");
				if (size != null)
				{
					String[] dim = size.split(",");
					// <dimensions> is in format "width,height"
					if (dim.length < 2)
					{
						System.out.println("Bad size dimensions in sprite kml!\n");
						return null;
					}
					Integer width = new Integer(dim[0]);
					Integer height = new Integer(dim[1]);

					theSprite.setSize(width.intValue(), height.intValue()); 
				}
				if (type.equalsIgnoreCase("3ds"))
				{
					// Initially use the icon size to calculate the model length in
					// case no model length is given
					((SdtSpriteModel) theSprite).setSize(theSprite.getIconSize().width,
						theSprite.getIconSize().height);

					String lighting = getTextValue(el, "light");
					if (lighting != null)
						if (lighting.equalsIgnoreCase("on"))
							((SdtSpriteModel) theSprite).setUseLighting(true);
						else if (lighting.equalsIgnoreCase("off"))
							((SdtSpriteModel) theSprite).setUseLighting(false);
				}
				// get the orientation
				String orientation = getTextValue(el, "orientation");
				String[] coord = orientation.split(",");

				if (coord.length > 0 && !coord[0].equalsIgnoreCase("x"))
				{
					if (type.equalsIgnoreCase("3ds"))
						((SdtSpriteModel) theSprite).setModelPitch(new Double(coord[0]));
					else
						((SdtSpriteKml) theSprite).setModelPitch(new Double(coord[0]));

				}
				if (coord.length > 1 && !coord[1].equalsIgnoreCase("x"))
				{
					if (coord[1].endsWith("a"))
					{
						coord[1] = coord[1].replace("a", "");
						theSprite.setAbsoluteYaw(true);
						if (type.equalsIgnoreCase("3ds"))
							((SdtSpriteModel) theSprite).setModelYaw(new Double(coord[1]));
						else
							((SdtSpriteKml) theSprite).setModelYaw(new Double(coord[1]));

					}
					else if (coord[1].endsWith("r"))
					{
						coord[1] = coord[1].replace("r", "");
						theSprite.setAbsoluteYaw(false);
						if (type.equalsIgnoreCase("3ds"))
							((SdtSpriteModel) theSprite).setModelYaw(new Double(coord[1]));
						else
							((SdtSpriteKml) theSprite).setModelYaw(new Double(coord[1]));
					}
					else
					{
						// Else we use the default useAbsoluteYaw setting
						if (type.equalsIgnoreCase("3ds"))
							((SdtSpriteModel) theSprite).setModelYaw(new Double(coord[1]));
						else
							((SdtSpriteKml) theSprite).setModelYaw(new Double(coord[1]));
					}
				}

				if (coord.length > 2 && !coord[2].equalsIgnoreCase("x"))
				{
					if (type.equalsIgnoreCase("3ds"))
						((SdtSpriteModel) theSprite).setModelRoll(new Double(coord[2]));
					else
						((SdtSpriteKml) theSprite).setModelRoll(new Double(coord[2]));

				}
				return theSprite;
			}
		}

		return null;
	}

	public void setModelElevation(DrawContext dc)
	{
		return;
	}

	// Try to load it as a Model, kml/kmz, or an Icon, else use default Model
	SdtSprite Load(String spritePath) throws IOException
	{
		// First see if it is an xml configuration file pointing
		// to the model and setting orientation

		if (spritePath.endsWith(".xml") | spritePath.endsWith(".XML"))
			return LoadXMLFile(spritePath);

		imageWidth = 0;
		imageHeight = 0;

		// LJT TEST MOVE THIS TO WWMODEL3d?
		// See if we have a valid model
		Model theModel = null;
		try
		{
			theModel = ModelFactory.createModel(spritePath);
		}
		catch (ModelLoadException e)
		{
			theModel = null;
			e.printStackTrace();
		}
		if (null != theModel)
		{
			SdtSpriteModel spriteModel = new SdtSpriteModel(this);
			spriteModel.setModel(theModel);
			spriteModel.setType(Type.MODEL);
			spriteModel.setSize(spriteModel.getIconSize().width,
				spriteModel.getIconSize().height);
			
			// We need the spritePath so we can reload the model when
			// we assign the model to the node.
			spriteModel.setSpritePath(spritePath);

			return spriteModel;
		}
		if (spritePath.endsWith("kml") || spritePath.endsWith("kmz")
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
		// It wasn't kml or a model, so lets see if it is a valid image file

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

			this.spriteType = Type.ICON;
			this.setSpritePath(spritePath);

			return this;
		}

		this.spriteName = "";
		return null;
	} // end SdtSprite.Load()


	public void setSpritePath(String spritePath)
	{
		this.spritePath = spritePath;
	}


	public void setRealSize(boolean isRealSize) {
		// TODO Auto-generated method stub
		
	}

} // end class SdtSprite
