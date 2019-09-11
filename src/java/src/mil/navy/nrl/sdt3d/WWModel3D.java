/*
 * WWModel3D.java
 *
 * Created on February 14, 2008, 9:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import net.java.joglutils.model.geometry.Model;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

//
/**
 *
 * @author RodgersGB
 */
public class WWModel3D {
	private AVList avlist = new AVListImpl();
    private Model model;
    
    // Adamson member additions for model size/orientation management
    
    public enum Axis {X, Y, Z};
    private SdtNode sdtNode = null;
    private double length = -1.0;
    private double iconRadius = -1.0;
    private double width = 32.0;  
    private double height = 32.0;
    private double modelLength = -1.0;
    
    private double headingOffset = 180.0;
    private double heading = 0.0;  // a.k.a. "yaw"
    private double pitch = 0.0;
    // if yaw is not set by controller it will be inferred
    // from heading
    private double yaw = 0.0;
    private double roll = 0.0;
    
    private double sizeScale = 1.0;
    
    /** Creates a new instance of WWModel3D */
    public WWModel3D(Model model, SdtNode theNode) {
        this.model = model;
        this.sdtNode = theNode;
 
    }

    public SdtNode getSdtNode()
    {
    	return sdtNode;
    }
  
    public Model getModel() {
        return model;
    }
    public Object getValue(String key)
    {
    	return avlist.getValue(key);
    }
    public void setValue(String key, Object value)
    {
    	avlist.setValue(key,value);
    }
    public void setPitch(double degrees)
        {pitch = degrees;}
    
    public double getPitch() 
        {return pitch;}
    
    // "heading" wr2 North (0.0)
    public void setHeading(double degrees)
        {heading = degrees;}
    public void setYaw(double degrees)
    {
    	// Note that if yaw is not set it will be inferred from degrees
    	// and that processing the orientation command will change a 0
    	// degree setting to 360.
    	yaw = degrees;
    }
    public double getYaw() 
    {
    	if (yaw > 0.0)
    		return yaw;
    	return ((heading + headingOffset) % 360.0);
    }
    public void setRoll(double degrees)
    {
    	roll = degrees;
    }
    public double getRoll()
    {
    	return roll;
    }
    
    public double getHeight() 
        {return height;}
    public double getWidth()
    	{return width;}
    void setModelLength(double lengthInMeters)
    {
    	modelLength = lengthInMeters;
    }
    public boolean isRealSize() {return (length > 0.0);}
    
    public void setSize(double width, double height, double length)
    {
    	this.width = width;
    	this.height = height;
    	// If no length is set, default to width
    	if (length < 0 && width > 0) length = this.width;
    	// If both length and width are set, default to width
    	if (length > 0 && width > 0) length = this.width;
    	// if length < 32, reset width & height to length
    	if (length > 0 && length < 32) {this.width = -length; this.height = -length;}
    	// if no length or width set use default width (32) as the h,w,l
    	double scale = getSdtNode().getSprite().getScale();
    	if (length < 0 && width < 0) {length = 32*scale;}
        setLength(length);

    }

    public double getLength()
    {
    	return this.length;
    }
    public void setUseLighting(boolean useLighting)
    {
    	model.setUseLighting(useLighting);
    }
    
    void setLength(double lengthInMeters)
    {

        if (lengthInMeters < 0.0)
        {
            sizeScale = getSdtNode().getSprite().getScale();
        	iconRadius = Math.sqrt(3*width*width) / 2.0;
            return;
        }
        net.java.joglutils.model.geometry.Vec4 bMin = model.getBounds().min;
        net.java.joglutils.model.geometry.Vec4 bMax = model.getBounds().max;
        double pHeight = Math.abs(bMax.z - bMin.z);
        double pLength = Math.abs(bMax.x - bMin.x);
        double pWidth = Math.abs(bMax.y - bMin.y);
        if (pLength < pWidth)
        {
            double temp = pLength;
            pLength = pWidth;
            pWidth = temp;
        }
        sizeScale = lengthInMeters / pLength;  // meters per pixel for this model
        length = lengthInMeters;
        if (pLength > length)
        	iconRadius = Math.sqrt(3*(width*sizeScale)*(width*sizeScale)) / 2.0;
        else
            iconRadius = Math.sqrt(3*width*width) / 2.0;
 
        height = pHeight * sizeScale;    
    }  // end WWModel3D.setLength()
    
    public double computeSizeScale(DrawContext dc, Vec4 loc) 
    {
    	if (modelLength > 0.0)
        {
            // A real-world length (in meters) was set
            // for this model
            return sizeScale;  // meters per pixel for this model
        } 
    	else
        {
            // Here we use the max(width,height) to compute
            // a scaling factor to produce a constant size
            // (in the view) 3-D "icon" instead of an 
            // actual size model
            double d = loc.distanceTo3(dc.getView().getEyePoint());
            double pSize = dc.getView().computePixelSizeAtDistance(d);
            // if (pSize < 1.0) pSize = 1.0; 
            double s = (pSize * iconRadius) /  this.model.getBounds().getRadius();
            // Don't let model get smaller than our requested size
            if ((length > 0.0) && (sizeScale > s)) s = sizeScale;
            return s;
        }
    }  // end WWModel3D.computeSizeScale()

}  // end class WWModel3D