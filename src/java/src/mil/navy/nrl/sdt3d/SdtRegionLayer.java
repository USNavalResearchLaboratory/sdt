package mil.navy.nrl.sdt3d;

import java.util.Iterator;
import java.util.Vector;
import javax.media.opengl.GL;


import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.AirspaceLayer;

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
	    	else
	    		if (region.getAirspaceShape() != null)
	    			airspaceLayer.removeAirspace(region.getAirspaceShape());
	    }
	    private void addRenderable(SdtRegion region)
	    {
			if (region.getSurfaceShape() != null)	
				super.addRenderable(region.getSurfaceShape());
			else 
				if (region.getAirspaceShape() != null)
					airspaceLayer.addAirspace(region.getAirspaceShape());
	    }
	    protected void doRender(DrawContext dc) 
	    {
	    	Iterator<SdtRegion> it = list.iterator();
	    	if (!it.hasNext()) return;
	    	try {
	         //   beginDraw(dc);
	            while (it.hasNext())
	            {
	            	SdtRegion region = it.next();
	            	if (region.hasPosition()) 
	            	{
	            		if (!region.isInitialized())
	            		{
	            			region.initialize(dc);
	            			addRenderable(region);
	            		}
            			if (region.getAirspaceShape() != null)
            				airspaceLayer.render(dc);
            			else
	            		if (region.getSurfaceShape() != null)
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
	    //    	endDraw(dc);
	        }	        
	    }

	    protected void beginDraw(DrawContext dc)
		{
			GL gl = dc.getGL();
			Vec4 cameraPosition = dc.getView().getEyePoint();

			if (dc.isPickingMode())
			{
				this.pickSupport.beginPicking(dc);

				gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_TRANSFORM_BIT);
				gl.glDisable(GL.GL_TEXTURE_2D);
				gl.glDisable(GL.GL_COLOR_MATERIAL);
			}
			else
			{
				gl.glPushAttrib(
							GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_TRANSFORM_BIT);
				gl.glDisable(GL.GL_TEXTURE_2D);
		            	            	            
				float[] lightPosition =
					{(float) (cameraPosition.x * 2), (float) (cameraPosition.y / 2), (float) (cameraPosition.z), 0.0f};
				float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
				float[] lightAmbient = {1.0f, 1.0f, 1.0f, 1.0f};
				float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};

				gl.glDisable(GL.GL_COLOR_MATERIAL);

				gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
				gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
				gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
				gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);

				gl.glDisable(GL.GL_LIGHT0);
				gl.glEnable(GL.GL_LIGHT1);
				gl.glEnable(GL.GL_LIGHTING);
				gl.glEnable(GL.GL_NORMALIZE);
			}

			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPushMatrix();
		}

		protected void endDraw(DrawContext dc)
		{
			GL gl = dc.getGL();

			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPopMatrix();

			if (dc.isPickingMode())
			{
				this.pickSupport.endPicking(dc);
			}
			else
			{
				gl.glDisable(GL.GL_LIGHT1);
				gl.glEnable(GL.GL_LIGHT0);
				gl.glDisable(GL.GL_LIGHTING);
				gl.glDisable(GL.GL_NORMALIZE);

			}

			gl.glPopAttrib();
		}


}
