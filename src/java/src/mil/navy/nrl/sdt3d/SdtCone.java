package mil.navy.nrl.sdt3d;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.prism.paint.Color;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class SdtCone extends SdtSymbol 
{
	private float mat_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };

	private float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };


	public SdtCone(String type)
	{
		super(type);
	}


	public SdtCone(String type, SdtNode theNode)
	{
		super(type, theNode);
	}
	
	
	@Override
	public void setLAzimuth(double d)
	{
		this.lAzimuth = d;
	}

	
	/**
	 * Convert the symbol heading to true up with
	 * node's heading: 0/360 is due north and 
	 * rotation is clockwise
	 * 
	 * @param heading
	 * @return convertedHeading
	 */
	private double convertToModelHeading(double heading)
	{
		// Add the node heading to our symbol heading
		if (!getAbsolutePositioning())
			heading = ((heading + sdtNode.getSymbolHeading()));
		
		heading = Math.abs(heading) % 360;

		// reverse symbol heading to true up with node heading
		heading = reverseRotation(heading,90);

		return heading;
	}
	

	@Override
	public void render(DrawContext dc)
	{ 
		// we use dc.getGLU() to access the current glu rather than gl2
		// GL initialization checks for GL2 compatibility.
		GL2 gl = dc.getGL().getGL2(); 
		
		initLighting(gl);
		
		buildCone(dc, gl);

		resetGLState(gl);
		
	}

	/**
	 * Initialize gl lighting
	 * 
	 * @param gl
	 */
	private void initLighting(GL2 gl)
	{
		// TODO: ljt improve cone rendering
		
		int attribMask = GL.GL_COLOR_BUFFER_BIT
			// For color write mask. If blending is enabled: 
			// for blending src and func, and alpha func.
			| GL2.GL_CURRENT_BIT // For current color.
			| GL2.GL_LINE_BIT // For line width, line smoothing.
			| GL2.GL_POLYGON_BIT // For polygon mode, polygon offset.
			| GL2.GL_TRANSFORM_BIT; // For matrix mode.
		gl.glPushAttrib(attribMask);

		//gl.glEnable(GL2.GL_LIGHTING); //Seems to actually disable lighting?
		gl.glEnable(GL2.GL_LIGHT0);

		
		// We need to use materials because we are enabling
		// lighting (e.g. glColor doesn't work)
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 25.0f);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);

		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);
		
		gl.glColorMaterial(GL.GL_FRONT, GL2.GL_DIFFUSE);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		
		float diffuseMaterial[] = { getColor().getRed(), getColor().getGreen(), getColor().getBlue(), new Float(getOpacity()) };
		gl.glColor4fv(diffuseMaterial, 0);
			    
		// Blending enables transparency
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();

	}
	
	
	/**
	 * OpenGL code to build the cone
	 * 
	 * @param dc
	 * @param gl
	 */
	private void buildCone(DrawContext dc, GL2 gl)
	{
		// topCenter is the origin of the cone
		Vec4 topCenter = dc.getGlobe().computePointFromPosition(
				getPosition().getLatitude(), 
				getPosition().getLongitude(),
				getPosition().getElevation());
		
		// The usual order of operations is:
		// Translate (move the origin to the right location)
		// Rotate (orient the coordinate axes right)
		// Scale (get the object to the right size)
		gl.glTranslated(topCenter.x, topCenter.y, topCenter.z);

		gl.glRotated(90 + getPosition().getLongitude().getDegrees(), 0, 1, 0);
		
		// rotation about x-axis is pitch/elevation
		// rotation about y-axis is heading/yaw
		// rotation about z-axis is roll/bank
		// glRotated(degrees, x, y, z) 
		
		// Convert heading so 0/360 is due north orientation clockwise
		double heading = convertToModelHeading(lAzimuth);
		
		// orientation/heading 
		gl.glRotated(heading, -1, 0, 0);
		gl.glRotated(getPosition().getLatitude().getDegrees() 
				* Angle.fromDegrees(heading).sin(), 0, 1, 0);		

		// elevation is 0-360 with 0 to the east 180 to the west 90 due north etc
		// degrees tilted up from the earth's surface
		double elevation = rAzimuth;
		gl.glRotated(elevation, 0, -1, 0);
		gl.glRotated(getPosition().getLatitude().getDegrees() 
				//* Angle.fromDegrees(elevation).cos(), 1, 0, 0);
				* Angle.fromDegrees(elevation).cos(), -1, 0, 0);
		
		
		// width = radius of cone base
		double currentWidth =  getWidth(); 
		// height = length from cone tip to center of base
		double currentHeight = getHeight();
		Vec4 loc = dc.getGlobe().computePointFromPosition(getPosition());
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		
		if (isScalable())
		{
			// If the symbol is scalable scale the symbol at the given dimensions
			// otherwise we scale based on icon size
			currentWidth *= dc.getView().computePixelSizeAtDistance(d);
			currentHeight *= dc.getView().computePixelSizeAtDistance(d);
			
			currentWidth = currentWidth < getWidth() ? getWidth() : currentWidth;	
			currentHeight = currentHeight < getHeight() ? getHeight() : currentHeight;
			
		}
		else
		{
			if (isIconHugging())
			{
				// Make a reasonably proportioned cone unless we're "fixed" scalable
				// TODO: Do we want to make cone base min equal to min dimension?
				currentHeight = currentWidth * 3.14159;
				currentWidth *= dc.getView().computePixelSizeAtDistance(d); // orientation
				currentHeight *= dc.getView().computePixelSizeAtDistance(d); // elevation			
			}
		}
		currentWidth = currentWidth * this.getScale();
		currentHeight = currentHeight * this.getScale();
				
		if (sdtNode.getSprite().getType() == SdtSpriteIcon.Type.MODEL)
		{	
			if (currentWidth < (getWidth() * this.getScale()))
			{
				currentHeight = ((getWidth() * 3.14159) * this.getScale());
			}
			if (currentHeight < (getHeight() * this.getScale()))
			{
				currentHeight = ((getWidth() * 3.14159) * this.getScale());
				currentWidth = (getWidth() * this.getScale());
			}
		}		
		GLUquadric quadric = dc.getGLU().gluNewQuadric();
		dc.getGLU().gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
		dc.getGLU().gluCylinder(quadric, 0, currentWidth, currentHeight, 30, 30);
		gl.glTranslated(0, 0, currentHeight);
		dc.getGLU().gluDisk(quadric, 0d, currentWidth, 30, 30);
		dc.getGLU().gluDeleteQuadric(quadric);

	}
	
	
	private void resetGLState(GL2 gl)
	{
		// reset our state
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_COLOR_MATERIAL);
		
		gl.glPopMatrix();
		gl.glPopAttrib();

	}
	
		
	@Override
	void updateSymbolCoordinates(DrawContext dc)
	{
		return;
	}
}
