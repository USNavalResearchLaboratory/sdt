package mil.navy.nrl.sdt3d;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.DetailLevel;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;

public class SdtEllipseAirspace extends SphereAirspace {

	private double xRatio = 1.0;
	private double yRatio = 1.0;
	private double zRatio = 1.0;
    public SdtEllipseAirspace(Position pos, double d) {
		// TODO Auto-generated constructor stub
    	super(pos,d);
	}
    public void setXYRatio(double xratio,double yratio, double zratio)
    {
    	xRatio = xratio;
    	yRatio = yratio;
    	zRatio = zratio;
    }
	protected void drawSphere(DrawContext dc)
    {
 
        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        boolean[] terrainConformant = this.isTerrainConforming();
        int subdivisions = this.getSubdivisions();
 
        if (this.isEnableLevelOfDetail())
        {
            DetailLevel level = this.computeDetailLevel(dc);

            Object o = level.getValue(SUBDIVISIONS);
            if (o != null && o instanceof Integer)
                subdivisions = (Integer) o;

            //o = level.getValue(DISABLE_TERRAIN_CONFORMANCE);
            //if (o != null && o instanceof Boolean && (Boolean) o)
            //    terrainConformant[0] = terrainConformant[1] = false;
        }

        Vec4 centerPoint = this.computePointFromPosition(dc,
            this.getLocation().getLatitude(), this.getLocation().getLongitude(), altitudes[0], terrainConformant[0]);
 
		 
        Position centerPos = dc.getGlobe().computePositionFromPoint(centerPoint);
  
        Matrix modelview = dc.getView().getModelviewMatrix();
        modelview = modelview.multiply(dc.getGlobe().computeModelCoordinateOriginTransform(centerPos)); 

         // Transform ellipse to view heading
		Angle heading = dc.getView().getHeading();
		modelview = modelview.multiply(Matrix.fromRotationZ(heading.multiply(-1)));
        modelview = modelview.multiply(Matrix.fromScale((this.getRadius() * xRatio),(this.getRadius() * yRatio),this.getRadius() * zRatio));
        
        double[] matrixArray = new double[16];
        modelview.toArray(matrixArray, 0, false);

        this.setExpiryTime(-1L); // Sphere geometry never expires.

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPushAttrib(GL2.GL_POLYGON_BIT | GL2.GL_TRANSFORM_BIT);
        try
        {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glFrontFace(GL.GL_CCW);

            // Were applying a scale transform on the modelview matrix, so the normal vectors must be re-normalized
            // before lighting is computed. In this case we're scaling by a constant factor, so GL_RESCALE_NORMAL
            // is sufficient and potentially less expensive than GL_NORMALIZE (or computing unique normal vectors
            // for each value of radius). GL_RESCALE_NORMAL was introduced in OpenGL version 1.2.
            gl.glEnable(GL2.GL_RESCALE_NORMAL);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            try
            {
                gl.glLoadMatrixd(matrixArray, 0);
                this.drawUnitSphere(dc, subdivisions);
            }
            finally
            {
               gl.glPopMatrix();
            }
        }
        finally
        {
            gl.glPopAttrib();
        }
 
    }
 
}
