package mil.navy.nrl.sdt3d;


import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.swing.ImageIcon;

import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

public class SdtSprite 
{
	public enum Type {MODEL, ICON, KML, NONE, INVALID}
	
	private String spriteName;
	private Type spriteType = Type.INVALID;
	
	private Model spriteModel = null;
	private SdtKml spriteKml = null;
	private double fixedLength = -1.0;  // in meters
	boolean modelUseLighting = false;
	
	private String iconPath = null;  // path to validate icon source
	private java.net.URL iconURL = null; // path to images retrieved from jar files 
	// default icon size preserves source image aspect ratio
	// with a fixed minimum dimension of 32 pixels
	private int iconWidth = -32;
	private int iconHeight = -32;
	private int imageWidth = 0;
    private int imageHeight = 0;
    private float scale = 1;
    
    public SdtSprite(String name)
	{
		this.spriteName = name;
	}
	public String getName() {return this.spriteName;}
	public Type getType() {return this.spriteType;}
	public void setType(Type theType) {this.spriteType = theType;}
	public void setSpriteKml(SdtKml theKml) {spriteKml = theKml;}
	public Model getModel() {return this.spriteModel;}
	public SdtKml getSpriteKml() {return this.spriteKml;}
	public double getFixedLength() {return fixedLength;}
	public void setFixedLength(double length) {fixedLength = length;}
	public void setModelUseLighting(boolean state)
	{
		if (null != spriteModel)
			spriteModel.setUseLighting(state);
		modelUseLighting = state;
	}
	public String getIconPath() {return this.iconPath;}
	public java.net.URL getIconURL() {return this.iconURL;}
	
	public double computeKmlSize(DrawContext dc, Vec4 loc) 
	{
		
		if (loc == null)
		{
			System.err.println("Null location when computing size of model");
			return 1;
		}
		double currentSize = 0;
		double currentWidth = 60;
		if (getWidth() > 0)
			currentWidth = getWidth();
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		if (fixedLength > 0)
			currentSize = fixedLength;
		else
			currentSize = currentWidth * dc.getView().computePixelSizeAtDistance(d);
		
		if (currentSize < 2)
			currentSize = 2;
		return currentSize;  
		
	} // computeSize

	Dimension getIconSize() 
	{
		return new Dimension(iconWidth,iconHeight);
	}	
	public double getWidth() 
	{
		return iconWidth;
	}
	public double getHeight() 
	{
		return iconHeight;
	}
	// Due to the overly convoluted model code override the
	// sprite size so we have the correct dimensions for 
	// calculating symbol size.  Don't want to fix this mess
	// and break other code like model sizing right now!
	void setModelSize(int width, int height)
	{	
		iconWidth = width;
		iconHeight = height;

	}
	void setIconSize(int width, int height)
	{
		if (width < 0 && height < 0)
		{
			System.out.println("Invalid icon dimension");
			return;
		}
		float scaleHeight = 0;
		float scaleWidth = 0;
		if (width < 0)
		{
			scaleHeight = (float)height/imageHeight;
			scaleWidth = scaleHeight;
		}
		if (height < 0)
		{
			scaleWidth = (float)width/imageWidth;
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
		//       upon values and imageWidth/imageHeight
	    if (scale > 0)
	    {
	    	iconWidth = (int)((float)iconWidth * scale);
	    	iconHeight = (int)((float)iconHeight * scale);

	    } 

	}

	public void setScale(float theScale) 
	{	
		// We have to set scale separately for kml as we don't have
		// the collada root to scale until after the first render pass...
		if (getType() == SdtSprite.Type.KML)
		{
			spriteKml.setScale(theScale);
			return;
		}
		// Reset icon to original dimensions
		if (scale == 1)
		{
			iconWidth = (int)((float)iconWidth/scale);
			iconHeight = (int)((float)iconHeight/scale);
		}
		scale = theScale;
	    if (scale > 1)
	    {
	    	iconWidth = (int)((float)iconWidth * scale);
	    	iconHeight = (int)((float)iconHeight * scale);
	    }
	}
	public float getScale() {return scale;}
	// Load sprite from jar
	boolean LoadURL(java.net.URL spritePath) throws IOException
	{
		iconWidth = -32;
		iconHeight = -32;
		imageWidth = 0;
	    imageHeight = 0;

		ImageIcon img = new ImageIcon(spritePath);
		Image image = img.getImage ();
		if (null != image)
		{
			imageWidth = image.getWidth(null);
			imageHeight = image.getHeight(null);

			int sizeRule = (iconWidth < 0) ? 1 : 0;
			sizeRule += (iconHeight < 0) ? 2 : 0;
			switch (sizeRule)
			{
			case 0:  // non-zero width & height
				break;
			case 1:  // non-zero height, free-form width
				double scale = (double)iconHeight / (double)imageHeight;
				iconWidth = (int)(scale * (double)imageWidth + 0.5);
				break;
			case 2:  // non-zero width, free-form height
				scale = (double)iconWidth / (double)imageWidth;
				iconHeight = (int)(scale * (double)imageHeight + 0.5);
				break;
			case 3:  // free-form width and height (use default size for min dimension
				if (imageWidth < imageHeight)
				{
					scale = 32.0 / (double)imageWidth;
					iconWidth = 32;
					iconHeight = (int)(scale * (double)imageHeight);
				}
				else
				{
					scale = 32.0 / (double)imageHeight;
					iconHeight = 32;
					iconWidth = (int)(scale * (double)imageWidth);
				}
				break;
			}

			this.spriteType = Type.ICON;
			this.iconURL = spritePath;

			return true;
		}
		    
		this.spriteName = "";
		return false;
	}  // end SdtSprite.LoadURL()
	// Try to load it as a Model, kml/kmz, or an Icon, else use default Model
	boolean Load(String spritePath) throws IOException
	{
		iconWidth = -32;
		iconHeight = -32;
		imageWidth = 0;
	    imageHeight = 0;

		try 
		{
			spriteModel = ModelFactory.createModel(spritePath);
		} 
		catch (ModelLoadException e) 
		{
			spriteModel = null;
			e.printStackTrace();
		}
		if (null != spriteModel)
		{
			//System.out.println("SdtSprite.Load() loaded new spriteModel ...");
		    spriteModel.setUnitizeSize(false);
		    spriteModel.centerModelOnPosition(true);
		    spriteModel.setUseLighting(modelUseLighting);
		    this.spriteType = Type.MODEL;
            return true;
		} 
		if (spritePath.endsWith("kml") || spritePath.endsWith("kmz")
				|| spritePath.endsWith("KML") || spritePath.endsWith("KMZ"))
		{
			spriteKml = new SdtKml(spritePath);
			this.spriteType = Type.KML;
			return true;

			// we are doing extra kml parsing here since we have to create the kml root for
			// each node - readd these checks when we get the final solution.  For
			// now assume we're getting a valid kml file.
		/*	if (spriteKml.isValid())
			{
				this.spriteType = Type.KML;
				return true;
			}
			*/
		}
		// It wasn't kml or a model, so lets see if it is a valid image file
		    
		// Using an ImageIcon to load the image will block until the
		// image is loaded (as opposed to getImage) but then we will
		// have the dimensions of the image available to us
		// Toolkit toolkit = Toolkit.getDefaultToolkit();
		// Image image = toolkit.getImage(spritePath);

		ImageIcon img = new ImageIcon(spritePath);
		Image image = img.getImage ();
		if (null != image)
		{
			imageWidth = image.getWidth(null);
			imageHeight = image.getHeight(null);

			int sizeRule = (iconWidth < 0) ? 1 : 0;
			sizeRule += (iconHeight < 0) ? 2 : 0;
			switch (sizeRule)
			{
			case 0:  // non-zero width & height
				break;
			case 1:  // non-zero height, free-form width
				double scale = (double)iconHeight / (double)imageHeight;
				iconWidth = (int)(scale * (double)imageWidth + 0.5);
				break;
			case 2:  // non-zero width, free-form height
				scale = (double)iconWidth / (double)imageWidth;
				iconHeight = (int)(scale * (double)imageHeight + 0.5);
				break;
			case 3:  // free-form width and height (use default size for min dimension
				if (imageWidth < imageHeight)
				{
					scale = 32.0 / (double)imageWidth;
					iconWidth = 32;
					iconHeight = (int)(scale * (double)imageHeight);
				}
				else
				{
					scale = 32.0 / (double)imageHeight;
					iconHeight = 32;
					iconWidth = (int)(scale * (double)imageWidth);
				}
				break;
			}

			this.spriteType = Type.ICON;
			this.iconPath = spritePath;

			return true;
		}
		    
		this.spriteName = "";
		return false;
	}  // end SdtSprite.Load()
	private Object getNodeKmlModelLayer() {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}  // end class SdtSprite
