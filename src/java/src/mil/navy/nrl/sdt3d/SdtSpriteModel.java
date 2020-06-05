/*
 * WWModel3D.java
 *
 * Created on February 14, 2008, 9:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mil.navy.nrl.sdt3d;

import java.awt.Rectangle;

import javax.media.opengl.GL2;

import builder.mil.nrl.atest.worldwind.jogl.DisplayListRenderer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.render.DrawContext;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
//
/**
 *
 * @author RodgersGB
 */

public class SdtSpriteModel extends SdtModel
{	
	private AVList avlist = new AVListImpl();

	private Model model = null;

	// Adamson member additions for model size/orientation management
	public enum Axis {
			X, Y, Z
	};

	protected double modelRadius = -1.0;
	
	protected double heading = 0.0; // a.k.a. "yaw"

	protected double modelPitch = 999.0;

	// if yaw is not set by controller it will be inferred
	// from heading
	protected double modelYaw = 999.0;

	protected double modelRoll = 999.0;

	// Set by node render code and added to any model p/r
	// WE have to do this due to the 3dModelLayer old style code
	protected double pitch = 0.0;

	protected double roll = 0.0;

	private double sizeScale = 1.0;
	
	private double minimumSizeScale = 1.0;

	boolean useLighting = false;

	// Used to calclate the right symbol size when model
	// is at "real-world" length
	boolean viewAtRealSize = false;

	boolean isRealSize = false;
	
	protected Position position;

	public SdtSpriteModel(SdtSpriteModel template)
	{
		super(template);
		this.model = template.model;
		this.position = template.position;
		this.modelRadius = template.modelRadius;
		this.isRealSize = template.isRealSize;
		this.iconWidth = template.iconWidth;
		this.iconHeight = template.iconHeight;
		this.heading = template.heading;
		this.modelPitch = template.modelPitch;
		this.modelYaw = template.modelYaw;
		this.modelRoll = template.modelRoll;
		this.sizeScale = template.sizeScale;
		this.minimumSizeScale = template.minimumSizeScale;
		this.useLighting = template.useLighting;
		this.spriteType = Type.MODEL;
	}


	public SdtSpriteModel(String name)
	{
		super(name);
		this.spriteType = Type.MODEL;
	}


	public SdtSpriteModel(Model model, String name)
	{
		super(name);
		this.model = model;
		this.spriteType = Type.MODEL;
	}

	
	public SdtSpriteModel(SdtSprite template) 
	{
		super(template);
		this.spriteType = Type.MODEL;
	}

	@Override 
	boolean isValid()
	{
		return model != null;
	}
	
	
	public void setModel(Model model)
	{
		this.model = model;
		this.model.setUnitizeSize(false);
		this.model.centerModelOnPosition(true);
		this.model.setUseLighting(useLighting);
	}


	@Override
	protected void setPosition(Position position)
	{
		// The model3DLayer needs model position.
		// this is called by node render function
		this.position = position;
	}


	protected Position getPosition()
	{
		return this.position;
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


	public double getModelPitch()
	{
		if (modelPitch != 999.0)
		{
			return this.modelPitch;
		}

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


	//@Override
	public void setRoll(double degrees)
	{
		this.roll = degrees;
	}


	public double getRoll()
	{
		return this.roll + this.getModelRoll();
	}


	//@Override
	public void setPitch(double degrees)
	{
		this.pitch = degrees;
	}


	public double getPitch()
	{
		return -(this.pitch + this.getModelPitch());
	}


	// "heading" wr2 North (0.0)
	//@Override
	public void setHeading(double newHeading, double nodeYaw)
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


	//@Override
	public double getYaw()
	{
		if (this.modelYaw != 999.0)
			return this.modelYaw;
		return 0.0;
	}

	// We override this here so that once we know the sprite is a model
	// we can set the model radius correctly when only real world dimension
	// is set.
	public void setFixedLength(double length)
	{
		fixedLength = length;
		setModelRadius();
	}
	
	public void setSize(double width, double height)
	{		
		//if (width <= 0) width = 32;
		//if (height <= 0) height = 32;
		
		// We don't (currently) have kml model sizing info
		// so setSize and setLength only applicable to 3d models
		this.iconWidth = width;
		this.iconHeight = height;
		
/*
		// if no length or width set use default width (32) as the h,w,l
		if (inLength < 0 && width < 0)
		{
			length = 32 * getScale();
		}
		*/
		setModelRadius();

	}
	
	@Override
	public void setRealSize(boolean isRealSizeIn) 
	{
		isRealSize = isRealSizeIn;
		
	}



	@Override
	public boolean isRealSize()
	{
		// TODO: ljt is viewAtRealSize used by symbols still?
		return (isRealSize || viewAtRealSize);
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
	 * Called by setModelRadius to get the "real-world" size
	 * to use when calculating the model radius.  We do this
	 * separate step as we don't know when processing sdt 
	 * commands whether one or both will be set.
	 * 
	 * The model radius is used during the rendering pass to 
	 * compute the model's size in pixels (and symbol via node
	 * render.
	 * 
	 * "fixedLength" defines the real-world physical size of the model 
	 * in meters
	 * 
	 * "size" controls the model's size in pixels as the view changes
	 * where real-world size = max h/w in pixels
	 * 
	 * When both size and fixed length are set
	 *  a) if the rendered length is less than "size" in pixels, size is used
	 *  b) else; model is rendered in real-world size
	 * 
	 */
	double getLength()
	{
		double lengthInMeters = getFixedLength();
		
		if (lengthInMeters > 0 && getWidth() > 0 || lengthInMeters < 0)
		{
			lengthInMeters = iconWidth;
			
			if (lengthInMeters < 0)
			{
				lengthInMeters = 32 * getScale();
			}
		} 
		
		return lengthInMeters;
		
	}
	
	@Override
	double getModelRadius()
	{
		return modelRadius;
	}
	
	
	/*
	 * Called when we set the size or the fixed length of the 
	 * model.  The modelRadius is used by the SdtSpriteModel()
	 * computeSizeScale to compute model pixel size during
	 * rendering
	 */
	void setModelRadius()
	{		
		double lengthInMeters = getLength();
				
		if (model == null)
		{
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
		
		sizeScale = lengthInMeters / pLength; // meters per pixel for this model
		minimumSizeScale = sizeScale;
		
		// We override the minimumSizeScale to the "fixed length" if 
		// hybrid sizing has been set.
		if (getFixedLength() > 0 && getWidth() > 0)
		{
			minimumSizeScale = getFixedLength() / pLength; 
		}
		
		if (pLength > lengthInMeters)
			modelRadius = Math.sqrt(3 * (lengthInMeters * sizeScale) * (lengthInMeters * sizeScale)) / 2.0;
		else
			modelRadius = Math.sqrt(3 * lengthInMeters * lengthInMeters) / 2.0;
		
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
			if (isRealSize() && getFixedLength() > 0)
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
	 * computeSizeScale() is called by the node render function to 
	 * get model size for elevation offset for models following terrain and 
	 * by the model3d layer to scale and calculate feedback rectangle
	 */
	@Override
	public double computeSizeScale(DrawContext dc, Vec4 loc)
	{
		// Needed for valid symbol size
		viewAtRealSize = false;
				
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double pSize = dc.getView().computePixelSizeAtDistance(d);
						
		// If no dimensions were set for the model (e.g. modelRadius == -1
		// calculate modelRadius now
		if (modelRadius == -1)
		{
			setModelRadius();
		}
			
		double modelSize = (pSize * modelRadius) / this.model.getBounds().getRadius();
						
		// Don't let model get smaller than our minimum requested size
		if (modelSize < minimumSizeScale)
		{
			viewAtRealSize = true;
			modelSize = minimumSizeScale;
		}
			
		return modelSize;
			
	} // end SdtObjModel.computeSizeScale()
	

	//@Override
	public void render(DrawContext dc) 
	{		
			
		if (position == null)
			return;
		
		draw(dc);
		
		// Determine Cartesian position from the surface geometry if the
		// icon is near the surface, otherwise draw it from the globe.
		
		Vec4 modelPoint = null;
		if (position.getElevation() < dc.getGlobe().getMaxElevation())
			modelPoint = dc.getSurfaceGeometry().getSurfacePoint(position);
		if (modelPoint == null)
			modelPoint = dc.getGlobe().computePointFromPosition(position);

		Vec4 screenPoint = dc.getView().project(modelPoint);
		GL2 gl = dc.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		Vec4 loc = dc.getGlobe().computePointFromPosition(position);
		double localSize = computeSizeScale(dc, loc);

		double width = getWidth();
		double height = getHeight();
		gl.glLoadIdentity();
		gl.glTranslated((int) (screenPoint.x - width / 2), screenPoint.y + height, 0d);
		Rectangle rect = new Rectangle((int) (screenPoint.x),
				(int) (screenPoint.y), (int) (width * localSize),
				(int) (height * localSize));
				
		// TODO??
		this.recordFeedback(dc, this, modelPoint, rect);
			
		
	}

	// draw this layer
	protected void draw(DrawContext dc)
	{
		// we use dc.getGLU() to access the current glu rather than gl2
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		Position pos = getPosition();

		if (pos == null)
			return;
		Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
		double localSize = computeSizeScale(dc, loc);
		if (dc.getView().getFrustumInModelCoordinates().contains(loc))
		{
			dc.getView().pushReferenceCenter(dc, loc);
			gl.glRotated(pos.getLongitude().degrees, 0, 1, 0);
			gl.glRotated(-pos.getLatitude().degrees, 1, 0, 0);
			gl.glScaled(localSize, localSize, localSize);

			gl.glRotated(getHeading(), 0, 0, 1);
			gl.glRotated(getPitch(), 1, 0, 0);
			gl.glRotated(getRoll(), 0, 1, 0);

			// Get an instance of the display list renderer
			DisplayListRenderer.getInstance().render(dc,model);

			dc.getView().popReferenceCenter(dc);
		}

	}

	
	// Eventually we should extend WWIcon for WWModel3D... These are duplicate functions from the renderer
	/**
	 * Returns true if the ModelRenderer should record feedback about how the specified WWModel3D has been processed.
	 *
	 * @param dc the current DrawContext.
	 * @param model the WWModel3D to record feedback information for.
	 *
	 * @return true to record feedback; false otherwise.
	 */
	protected boolean isFeedbackEnabled(DrawContext dc, SdtSpriteModel model)
	{
		if (dc.isPickingMode())
			return false;

		Boolean b = (Boolean) model.getValue(AVKey.FEEDBACK_ENABLED);
		return (b != null && b);
	}


	/**
	 * If feedback is enabled for the specified WWModel3D, this method records feedback about how the specified
	 * WWModel3D has
	 * been processed.
	 *
	 * @param dc the current DrawContext.
	 * @param model the model which the feedback information refers to.
	 * @param modelPoint the model's reference point in model coordinates.
	 * @param screenRect the models's bounding rectangle in screen coordinates.
	 */
	protected void recordFeedback(DrawContext dc, SdtSpriteModel model, Vec4 modelPoint, Rectangle screenRect)
	{
		if (!this.isFeedbackEnabled(dc, model))
			return;

		this.doRecordFeedback(dc, model, modelPoint, screenRect);
	}


	/**
	 * Records feedback about how the specified WWIcon has been processed.
	 *
	 * @param dc the current DrawContext.
	 * @param icon the icon which the feedback information refers to.
	 * @param modelPoint the icon's reference point in model coordinates.
	 * @param screenRect the icon's bounding rectangle in screen coordinates.
	 */
	@SuppressWarnings({ "UnusedDeclaration" })
	protected void doRecordFeedback(DrawContext dc, SdtSpriteModel model, Vec4 modelPoint, Rectangle screenRect)
	{
		model.setValue(AVKey.FEEDBACK_REFERENCE_POINT, modelPoint);
		model.setValue(AVKey.FEEDBACK_SCREEN_BOUNDS, screenRect);
	}


	public Object getColladaRoot() {
		// TODO Auto-generated method stub
		return null;
	}


	public KMLController getKmlController() {
		// TODO Auto-generated method stub
		return null;
	}

} // end class SdtObjModel