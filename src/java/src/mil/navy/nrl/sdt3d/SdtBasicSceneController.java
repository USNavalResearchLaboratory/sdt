/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package mil.navy.nrl.sdt3d;

import java.util.logging.Level;

import javax.media.opengl.GL;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.BasicSceneController;
import gov.nasa.worldwind.render.ScreenCreditController;
/**
 * @author Laurie Thompson
 *  
 *  Overrides the BasicSceneController's draw method in order to force our
 *  symbol layer to be drawn last.  (Otherwise the symbol layer hides the icons
 *  and anotations.)
 */
public class SdtBasicSceneController extends BasicSceneController
{
	protected void drawLayer(DrawContext dc, String theLayer)
	{
        if (dc.getLayers() != null)
        {
            for (Layer layer : dc.getLayers())
            {
                try
                {
                    if (layer != null 
                    && 
	                (layer.getName().compareTo(theLayer) == 0))
                  {
                        dc.setCurrentLayer(layer);
                        layer.render(dc);
                    }
                }
                catch (Exception e)
                {
                    String message = Logging.getMessage("SceneController.ExceptionWhileRenderingLayer",
                        (layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown")));
                    Logging.logger().log(Level.SEVERE, message, e);
                    // Don't abort; continue on to the next layer.
                }
            }
            dc.setCurrentLayer(null);
        }  		
	}
	
	  protected void draw(DrawContext dc)
	    {
	        try
	        {
	            if (dc.getLayers() != null)
	            {
	                for (Layer layer : dc.getLayers())
	                {
	                    try
	                    {
	                    	//  We render symbols & regions later last
	                        if (layer != null && 
		                    (!(layer.getName().compareTo("Node Symbols") == 0))
		                     &&
		                    (!(layer.getName().compareTo("Regions") == 0)))
	                        {
	                            dc.setCurrentLayer(layer);
	                            layer.render(dc);
	                        }
	                    }
	                    catch (Exception e)
	                    {
	                        String message = Logging.getMessage("SceneController.ExceptionWhileRenderingLayer",
	                            (layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown")));
	                        Logging.logger().log(Level.SEVERE, message, e);
	                        // Don't abort; continue on to the next layer.
	                    }
	                }

	                dc.setCurrentLayer(null);
	            }
	            
	           // Draw the deferred/ordered surface renderables
	            this.drawOrderedSurfaceRenderables(dc);
	            
	            if (this.getScreenCreditController() != null)
	                this.getScreenCreditController().render(dc);

	            
	            // Render all the orderered renderables added in the first
	            // rendering pass
	            dc.setOrderedRenderingMode(true);
	            while (dc.peekOrderedRenderables() != null)
	            {
	          	  dc.pollOrderedRenderables().render(dc);
	            }
	            dc.setOrderedRenderingMode(false);
		        drawLayer(dc,"Node Symbols");
		        drawLayer(dc,"Regions");
		        
	            // Finally render all the symbol/region orderered renderables added 
	            dc.setOrderedRenderingMode(true);
	            while (dc.peekOrderedRenderables() != null)
	            {
	          	  dc.pollOrderedRenderables().render(dc);
	            }
	            dc.setOrderedRenderingMode(false);

	            // Diagnostic displays.
	            if (dc.getSurfaceGeometry() != null && dc.getModel() != null && (dc.getModel().isShowWireframeExterior() ||
	                dc.getModel().isShowWireframeInterior() || dc.getModel().isShowTessellationBoundingVolumes()))
	            {
	                Model model = dc.getModel();

	                float[] previousColor = new float[4];
	                dc.getGL().glGetFloatv(GL.GL_CURRENT_COLOR, previousColor, 0);

	                for (SectorGeometry sg : dc.getSurfaceGeometry())
	                {
	                    if (model.isShowWireframeInterior() || model.isShowWireframeExterior())
	                        sg.renderWireframe(dc, model.isShowWireframeInterior(), model.isShowWireframeExterior());

	                    if (model.isShowTessellationBoundingVolumes())
	                    {
	                        dc.getGL().glColor3d(1, 0, 0);
	                        sg.renderBoundingVolume(dc);
	                    }
	                }

	                dc.getGL().glColor4fv(previousColor, 0);
	            }
	        }
	        catch (Throwable e)
	        {
	            Logging.logger().log(Level.SEVERE, Logging.getMessage("BasicSceneController.ExceptionDuringRendering"), e);
	        }

          
	    }	  	
}
