package mil.navy.nrl.sdt3d;

import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;

public class SdtRegionLayer extends RenderableLayer
{

	private Vector<SdtRegion> list;

	private AirspaceLayer airspaceLayer;


	/** Creates a new instance of RegionLayer */
	public SdtRegionLayer()
	{
		list = new Vector<SdtRegion>();
		// we'll manage our own airspace layer
		airspaceLayer = new AirspaceLayer();
	}


	public void addRegion(SdtRegion region)
	{
		if (!list.contains(region))
		{
			list.add(region);
		}
	}


	public void removeRegion(SdtRegion region)
	{
		list.remove(region);
	}


	public void removeRenderables(SdtRegion region)
	{
		if (region.getSurfaceShape() != null)
			super.removeRenderable(region.getSurfaceShape());
		else if (region.getAirspaceShape() != null)
			airspaceLayer.removeAirspace(region.getAirspaceShape());
	}


	void addRenderable(SdtRegion region)
	{
		if (region.getSurfaceShape() != null)
			super.addRenderable(region.getSurfaceShape());
		else if (region.getAirspaceShape() != null)
			airspaceLayer.addAirspace(region.getAirspaceShape());
	}


	@Override
	protected void doRender(DrawContext dc)
	{
		Iterator<SdtRegion> it = list.iterator();
		if (!it.hasNext())
			return;
		try
		{
			// beginDraw(dc);
			while (it.hasNext())
			{
				SdtRegion region = it.next();
				// only render the region if it has a position
				// and any associated layer is visible (e.g. theRegion.isSelected)
				if (region.hasPosition() && region.isSelected())
				{
					if (!region.isInitialized())
					{
						region.initialize(dc);
						addRenderable(region);
					}
					if (region.getAirspaceShape() != null)
						airspaceLayer.render(dc);
					else if (region.getSurfaceShape() != null)
						super.doRender(dc);

				}
			}
		}
		// handle any exceptions
		catch (Exception e)
		{
			// handle
			e.printStackTrace();
		}
		// we must end drawing so that opengl
		// states do not leak through.
		finally
		{
			// endDraw(dc);
		}
	}


	protected void beginDraw(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
		Vec4 cameraPosition = dc.getView().getEyePoint();

		if (dc.isPickingMode())
		{
			this.pickSupport.beginPicking(dc);

			gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_TRANSFORM_BIT);
			gl.glDisable(GL.GL_TEXTURE_2D);
			gl.glDisable(GL2.GL_COLOR_MATERIAL);
		}
		else
		{
			gl.glPushAttrib(
				GL2.GL_TEXTURE_BIT | GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_LIGHTING_BIT | GL2.GL_TRANSFORM_BIT);
			gl.glDisable(GL.GL_TEXTURE_2D);

			float[] lightPosition = { (float) (cameraPosition.x * 2), (float) (cameraPosition.y / 2), (float) (cameraPosition.z), 0.0f };
			float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
			float[] lightAmbient = { 1.0f, 1.0f, 1.0f, 1.0f };
			float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };

			gl.glDisable(GL2.GL_COLOR_MATERIAL);

			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
			gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);

			gl.glDisable(GL2.GL_LIGHT0);
			gl.glEnable(GL2.GL_LIGHT1);
			gl.glEnable(GL2.GL_LIGHTING);
			gl.glEnable(GL2.GL_NORMALIZE);
		}

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
	}


	protected void endDraw(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix();

		if (dc.isPickingMode())
		{
			this.pickSupport.endPicking(dc);
		}
		else
		{
			gl.glDisable(GL2.GL_LIGHT1);
			gl.glEnable(GL2.GL_LIGHT0);
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glDisable(GL2.GL_NORMALIZE);

		}

		gl.glPopAttrib();
	}

}
