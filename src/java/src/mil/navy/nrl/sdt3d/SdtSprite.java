package mil.navy.nrl.sdt3d;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

public class SdtSprite 
{
	public enum Type {MODEL, ICON, NONE, INVALID}
	
	private String spriteName;
	private Type spriteType = Type.INVALID;
	
	private Model spriteModel = null;
	private double modelLength = -1.0;  // in meters
	boolean modelUseLighting = false;
	
	private String iconPath = null;  // path to validate icon source
	// default icon size preserves source image aspect ratio
	// with a fixed minimum dimension of 32 pixels
	private int iconWidth = -32;
	private int iconHeight = -32;
	private int imageWidth = 0;
    private int imageHeight = 0;
    private float scale = 0;
    
    public SdtSprite(String name)
	{
		this.spriteName = name;
	}
	public String getName() {return this.spriteName;}
	public Type getType() {return this.spriteType;}
	public void setType(Type theType) {this.spriteType = theType;}
	
	public Model getModel() {return this.spriteModel;}
	public double getModelLength() {return modelLength;}
	public void setModelLength(double length)
	    {modelLength = length;}
	public void setModelUseLighting(boolean state)
	{
		if (null != spriteModel)
			spriteModel.setUseLighting(state);
		modelUseLighting = state;
	}
	public String getIconPath() {return this.iconPath;}
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
	void setIconSize(int width, int height)
	{
		// TODO: dynamically re-calculate these depending
		//       upon values and imageWidth/imageHeight
	    if (scale > 0)
	    {
	    	iconWidth = (int)((float)width * scale);
	    	iconHeight = (int)((float)height * scale);
	    } 
	    else
	    {
	    	iconWidth = width;
	    	iconHeight = height;
	    }    	   
	}
	public void setScale(float theScale) 
	{		
		// Reset icon to original dimensions
		if (scale != 0)
		{
			iconWidth = (int)((float)iconWidth/scale);
			iconHeight = (int)((float)iconHeight/scale);
		}
		scale = theScale;
	    if (scale > 0)
	    {
	    	iconWidth = (int)((float)iconWidth * scale);
	    	iconHeight = (int)((float)iconHeight * scale);
	    } 

	}
	public float getScale() {return scale;}
	// Try to load it as a Model, if not as an Icon, else use default Model
	boolean Load(String spritePath)
	{
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
		else
		{
		    // It wasn't a model, so lets see if it is a valid image file
		    
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
		    else
		    {
		        System.out.println("Unable to open image file: " + spritePath);
		    }
		}
		return false;
	}  // end SdtSprite.Load()
    
    
}  // end class SdtSprite
