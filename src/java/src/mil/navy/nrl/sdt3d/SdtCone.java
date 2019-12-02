package mil.navy.nrl.sdt3d;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class SdtCone extends SdtSymbol implements Renderable
{

	private Angle orientation = Angle.fromDegrees(0);

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
		orientation = Angle.fromDegrees(getLAzimuth()); // degrees tilted clockwise from due east

	}


	@Override
	public void render(DrawContext dc)
	{
		// we use dc.getGLU() to access the current glu rather than gl2
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		int attribMask = GL.GL_COLOR_BUFFER_BIT
			// For color write mask. If blending is enabled: for blending src and func, and alpha func.
			| GL2.GL_CURRENT_BIT // For current color.
			| GL2.GL_LINE_BIT // For line width, line smoothing.
			| GL2.GL_POLYGON_BIT // For polygon mode, polygon offset.
			| GL2.GL_TRANSFORM_BIT; // For matrix mode.
		gl.glPushAttrib(attribMask);

		init(dc);

		// Blending enables transparency
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();

		// lighting and stuff is set up - now build the cone

		// The cone orients as follows: when facing n, 0/360 is due east , 90 is N, 280 is S, 180 is W
		// So we need to add 270 to true up with sdt model orientation
		double azimuth = sdtNode.getSymbol().getLAzimuth();

		// Add the node heading to our symbol heading, multiply by -1 as orientation is different for cones
		if (!getAbsolutePositioning())
			azimuth = ((azimuth + sdtNode.getSymbolHeading()) * -1);
		else
			azimuth = azimuth * -1;
		azimuth = azimuth - 270;
		orientation = Angle.fromDegrees(azimuth);

		// topCenter is point at center of cylinder base
		Vec4 topCenter = dc.getGlobe().computePointFromPosition(getPosition().getLatitude(), getPosition().getLongitude(),
			getPosition().getElevation());
		gl.glTranslated(topCenter.x, topCenter.y, topCenter.z);

		// Not sure what this is doing
		Position p = dc.getGlobe().computePositionFromPoint(topCenter);
		gl.glRotated(90 + p.getLongitude().getDegrees(), 0, 1, 0);

		// elevation is 0-360 with 0 to the east 180 to the west 90 due north etc
		Angle elevation = Angle.fromDegrees(sdtNode.getSymbol().getRAzimuth()); // degrees tilted up from the surface

		// orientation = degrees tilted counterclockwise from due East
		gl.glRotated(orientation.getDegrees(), -1, 0, 0);
		gl.glRotated(p.getLatitude().getDegrees() * orientation.sin(), 0, 1, 0);
		// elevation = degrees tilted up from the surface
		gl.glRotated(elevation.getDegrees(), 0, -1, 0);
		//gl.glRotated(p.getLatitude().getDegrees() * elevation.sin(), -1, 0, 0);
        gl.glRotated(elevation.sin(), -1, 0, 0);
        
		Vec4 loc = dc.getGlobe().computePointFromPosition(getPosition());
		double d = loc.distanceTo3(dc.getView().getEyePoint());

		// width = radius of cone base
		double currentWidth = (sdtNode.getSymbol().getWidth());
		// height = length from cone tip to center of base
		double currentHeight = sdtNode.getSymbol().getHeight();

		if (isScalable())
		{
			// If the symbol is scalable scale the symbol at the given dimensions
			// otherwise we scale based on icon size
			currentWidth *= dc.getView().computePixelSizeAtDistance(d);
			currentHeight *= dc.getView().computePixelSizeAtDistance(d);

		}
		else

		if (isIconHugging())
		{
			// Make a reasonably proportioned cone unless we're "fixed" scalable
			// TODO: Do we want to make cone base min equal to min dimension?
			currentHeight = currentWidth * 3.14159;
			currentWidth *= dc.getView().computePixelSizeAtDistance(d); // orientation
			currentHeight *= dc.getView().computePixelSizeAtDistance(d); // elevation			
		}

		currentWidth = currentWidth * this.getScale();
		currentHeight = currentHeight * this.getScale();
				
		if (sdtNode.getSprite().getType() == SdtSprite.Type.MODEL)
		{	
			if (currentWidth < (getWidth() * this.getScale()))
			{
				currentWidth = (getWidth() * this.getScale());
				currentHeight = ((getWidth() * 3.14159) * this.getScale());
			}
			if (currentHeight < (getHeight() * this.getScale()))
			{
				currentHeight = ((getWidth() * 3.14159) * this.getScale());
				currentWidth = (getWidth() * this.getScale());
			}
		}


		// System.out.println(sdtNode.getName() + "H:" + getHeight() + " W: " + getWidth() + " lAz " + getLAzimuth() + "
		// rAz " + getRAzimuth() + " new h" + height + " new w " + width + " curH " + currentHeight + " curW " +
		// currentWidth + " max d " + getMaxDimension() + " min d " + getMinDimension());
		GLUquadric quadric = dc.getGLU().gluNewQuadric();
		dc.getGLU().gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
		dc.getGLU().gluCylinder(quadric, 0, currentWidth, currentHeight, 30, 30);
		gl.glTranslated(0, 0, currentHeight);
		dc.getGLU().gluDisk(quadric, 0d, currentWidth, 30, 30);
		dc.getGLU().gluDeleteQuadric(quadric);

		// reset our state
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_COLOR_MATERIAL);
		;
		gl.glPopMatrix();
		gl.glPopAttrib();
	}


	public void init(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		// We need to use materials because we are enabling
		// lighting (e.g. glColor doesn't work)

		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 25.0f);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glColorMaterial(GL.GL_FRONT, GL2.GL_DIFFUSE);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);

		float diffuseMaterial[] = { getColor().getRed(), getColor().getGreen(), getColor().getBlue(), new Float(getOpacity()) };

		gl.glColor4fv(diffuseMaterial, 0);

	}
}
