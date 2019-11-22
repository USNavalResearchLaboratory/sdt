/*
 * WWModel3D.java
 *
 * Created on February 14, 2008, 9:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.render.DrawContext;
import net.java.joglutils.model.geometry.Model;

//
/**
 *
 * @author RodgersGB
 */

public class SdtSpriteModel extends SdtSprite
{
	private AVList avlist = new AVListImpl();

	private Model model = null;

	// Adamson member additions for model size/orientation management
	public enum Axis {
			X, Y, Z
	};

	private Position position = null;

	//private double length = -1.0;

	private double modelRadius = -1.0;
	
	private double heading = 0.0; // a.k.a. "yaw"

	private double modelPitch = 0.0;

	// if yaw is not set by controller it will be inferred
	// from heading
	private double modelYaw = 0.0;

	private double modelRoll = 0.0;

	// Set by node render code and added to any model p/r
	// WE have to do this due to the 3dModelLayer old style code
	private double pitch = 0.0;

	private double roll = 0.0;

	private double sizeScale = 1.0;

	boolean useLighting = false;

	boolean viewAtRealSize = false;

	public SdtSpriteModel(SdtSprite template)
	{
		super(template);
		//this.iconWidth = 32;
		//this.iconHeight = 32;
	}


	public SdtSpriteModel(SdtSpriteModel template)
	{
		super(template);
		this.model = template.model;
		this.position = template.position;
		this.modelRadius = template.modelRadius;
		this.iconWidth = template.iconWidth;
		this.iconHeight = template.iconHeight;
		this.heading = template.heading;
		this.modelPitch = template.modelPitch;
		this.modelYaw = template.modelYaw;
		this.modelRoll = template.modelRoll;
		this.sizeScale = template.sizeScale;
		this.useLighting = template.useLighting;
	}


	public SdtSpriteModel(String name)
	{
		super(name);
	}


	public SdtSpriteModel(Model model, String name)
	{
		super(name);
		this.model = model;
	}


	public void setModel(Model model)
	{
		this.model = model;
		this.model.setUnitizeSize(false);
		this.model.centerModelOnPosition(true);
		this.model.setUseLighting(useLighting);
	}


	public Model getModel()
	{
		return this.model;
	}


	public void setPosition(Position pos)
	{
		// The model3DLayer needs model position.
		// this is called by node render function
		this.position = pos;
	}


	public Position getPosition()
	{
		return this.position;
	}


	@Override
	public void whoAmI()
	{
		System.out.println("I am a model sprite");
	}


	public Object getValue(String key)
	{
		return avlist.getValue(key);
	}


	public void setValue(String key, Object value)
	{
		avlist.setValue(key, value);
	}


	public void setModelPitch(double degrees)
	{
		modelPitch = degrees;
	}


	@Override
	public double getModelPitch()
	{
		if (this.modelPitch != 999.0)
			return this.modelPitch;

		return 0.0;
	}


	public void setModelRoll(double degrees)
	{
		modelRoll = degrees;
	}


	@Override
	public double getModelRoll()
	{
		if (this.modelRoll != 999.0)
			return this.modelRoll;
		return 0.0;
	}


	@Override
	public void setRoll(double degrees)
	{
		this.roll = degrees;
	}


	public double getRoll()
	{
		return this.roll + this.getModelRoll();
	}


	@Override
	public void setPitch(double degrees)
	{
		this.pitch = degrees;
	}


	public double getPitch()
	{
		return -(this.pitch + this.getModelPitch());
	}


	// "heading" wr2 North (0.0)
	@Override
	public void setHeading(double newHeading, double nodeYaw, ColladaRoot notUsed)
	{
		// Called by node rendering function

		if (useAbsoluteYaw())
		{
			// Heading is the node absolute yaw wet by any orientation command
			// and the model yaw set in any xml config file
			this.heading = (nodeYaw + getYaw());

		}
		else
		{
			// Heading is as above but relative to any node heading
			this.heading = ((nodeYaw + getYaw()) + newHeading);
		}

		this.heading = -this.heading;
		this.heading = SdtNode.normalize(this.heading);

	}


	public double getHeading()
	/*
	 * The node render function puts the calculated yaw to render in the heading field.
	 * model3d layer uses this (node yaw stores any node orientation yaw, sprite yaw
	 * stores any model override set in an associated xml file
	 */
	{
		return this.heading;
	}


	public void setModelYaw(double degrees)
	{
		this.modelYaw = degrees;
	}


	public double getModelYaw()
	{
		if (this.modelYaw != 999.0)
			return this.modelYaw;
		return 0.0;
	}


	@Override
	public double getYaw()
	{
		if (this.modelYaw != 999.0)
			return this.modelYaw;
		return 0.0;
	}

	// We override this here so that once we know the sprite is a model
	// we can set the model radius correctly when only real world dimensiont
	// is set.
	public void setFixedLength(double length)
	{
		fixedLength = length;
		setModelRadius();
	}
	
	public void setSize(double width, double height)
	{
		// We don't (currently) have kml model sizing info
		// so setSize and setLength only applicable to 3d models
		this.iconWidth = width;
		this.iconHeight = height;
		
		// If no length is set, default to width
		/*
		if (inLength < 0 && width > 0)
		{
			length = this.iconWidth;
		}
		// If both length and width are set, default to width
		if (inLength > 0 && width > 0)
			length = this.iconWidth;
		
		// if length < 32, reset width & height to length
		if (inLength > 0 && length < 32)
		{
			this.iconWidth = -length;
			this.iconHeight = -length;
		}
		// if no length or width set use default width (32) as the h,w,l
		if (inLength < 0 && width < 0)
		{
			length = 32 * getScale();
		}
		*/
		setModelRadius();

	}


	@Override
	public boolean isRealSize()
	{
		// TODO: Make real size boolean for clarity
		if ((getFixedLength() > 0.0 && iconWidth == -32)
				|| viewAtRealSize)
		{
			System.out.println("We are real size");
		}
		
		return (viewAtRealSize || (getFixedLength() > 0.0 && iconWidth == -32));
	}


	public void setUseLighting(boolean useLighting)
	{
		this.useLighting = useLighting;
		model.setUseLighting(useLighting);
	}


	public boolean getUseLighting()
	{
		return this.useLighting;
	}

	/*
	 * modelRadius is used by SdtSpriteModel::computeSizeScale()	
	 * to get size
	 */
	void setModelRadius()
	{
		sizeScale = 1.0;
		double lengthInMeters = iconWidth;
		if (getFixedLength() > 0)
		{
			lengthInMeters = getFixedLength();
		}
		System.out.println("lengthInMeters>" + lengthInMeters);
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
		lengthInMeters = lengthInMeters * sizeScale;
		sizeScale = lengthInMeters / pLength; // meters per pixel for this model
		
		System.out.println("lengthInMeters> " + lengthInMeters + " sizeScale>" + sizeScale + " name>" + getName());
		
		if (pLength > lengthInMeters)
			modelRadius = Math.sqrt(3 * (this.iconWidth * sizeScale) * (this.iconWidth * sizeScale)) / 2.0;
		else
			modelRadius = Math.sqrt(3 * this.iconWidth * this.iconWidth) / 2.0;

		System.out.println("Model radius>" + modelRadius);
		
		this.iconHeight = pHeight * sizeScale;
		
	} // end WWModel3D.setLength()


	@Override
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

		// Reset the size now that we have a scale
		setSize(getIconSize().width,getIconSize().height);

	}

	public double getSymbolSize()
	{
		double size = iconWidth > iconHeight ? iconWidth : iconHeight;
		
		if (getFixedLength() > 0) 
		{
			if (viewAtRealSize)
			{
				size = getFixedLength();
			}
			else
			{
				if (iconWidth > 0)
				{
					size = iconWidth;
				}
				else
				{
					size = getFixedLength();
				}
					
			}
		}
		return size;
	}
	

	/*
	 * computeSizeScale() is called by the node render function to get model elevation
	 * for models following terrain and by the model3d layer to
	 * scale and calculate feedback rectangle
	 */
	public double computeSizeScale(DrawContext dc, Vec4 loc)
	{
		viewAtRealSize = false;
		
		// TODO: Use a real world indicator?
		if (getFixedLength() > 0.0 && iconWidth == -32)
		{
			// A real-world length (in meters) was set
			// for this model
			return sizeScale; // meters per pixel for this model
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
			double s = (pSize * modelRadius) / this.model.getBounds().getRadius();
			// Don't let model get smaller than our requested size
			double length = (iconWidth > iconHeight) ? iconWidth : iconHeight;

			// If both length and size were set use the max?
			length = (getFixedLength() > length) ? getFixedLength() : length;
			
			if ((length > 0.0) && (sizeScale > s))
			{
				viewAtRealSize = true;
				s = sizeScale;
			}
			return s;
		}
	} // end WWModel3D.computeSizeScale()

} // end class WWModel3D