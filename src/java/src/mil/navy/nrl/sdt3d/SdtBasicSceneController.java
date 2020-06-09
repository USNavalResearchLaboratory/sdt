/*********************************************************************
 *
 * AUTHORIZATION TO USE AND DISTRIBUTE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: 
 *
 * (1) source code distributions retain this paragraph in its entirety, 
 *  
 * (2) distributions including binary code include this paragraph in
 *     its entirety in the documentation or other materials provided 
 *     with the distribution.
 * 
 *      "This product includes software written and developed 
 *       by Code 5520 of the Naval Research Laboratory (NRL)." 
 *         
 *  The name of NRL, the name(s) of NRL  employee(s), or any entity
 *  of the United States Government may not be used to endorse or
 *  promote  products derived from this software, nor does the 
 *  inclusion of the NRL written and developed software  directly or
 *  indirectly suggest NRL or United States  Government endorsement
 *  of this product.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * For more information send email to sdt_info@nrl.navy.mil
 *
 *
 * WWJ code:
 * 
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 ********************************************************************/

package mil.navy.nrl.sdt3d;

import java.awt.Color;
import java.util.logging.Level;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.StereoOptionSceneController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Laurie Thompson
 * 
 *         Overrides the BasicSceneController's draw method in order to force our
 *         symbol layer to be drawn last. (Otherwise the symbol layer hides the icons
 *         and anotations.)
 */
// public class SdtBasicSceneController extends BasicSceneController
public class SdtBasicSceneController extends StereoOptionSceneController
{
	protected Color clearColor = new Color(0, 0, 0, 0);


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


	@Override
	protected void draw(DrawContext dc)
	{
		// Override stereo options scene controller draw too
		// Capture the capabilities actually in use.
		if (this.capabilities == null)
		{
			this.capabilities = dc.getGLContext().getGLDrawable().getChosenGLCapabilities();
			this.hardwareStereo = this.capabilities.getStereo();

			this.inStereo = this.isHardwareStereo() ? true : this.isInStereo();
		}
		// end sosc
		try
		{
			if (dc.getLayers() != null)
			{
				for (Layer layer : dc.getLayers())
				{
					try
					{
						// We render symbols & region airspaces later last
						// TODO: I don't think we need to do this with the latest
						// wwj code -double check with 2.0
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
			drawLayer(dc, "Node Symbols");
			drawLayer(dc, "Regions");

			// Finally render all the symbol/region orderered airspace renderables added
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

				GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
				Model model = dc.getModel();

				float[] previousColor = new float[4];
				gl.glGetFloatv(GL2.GL_CURRENT_COLOR, previousColor, 0);

				for (SectorGeometry sg : dc.getSurfaceGeometry())
				{
					if (model.isShowWireframeInterior() || model.isShowWireframeExterior())
						sg.renderWireframe(dc, model.isShowWireframeInterior(), model.isShowWireframeExterior());

					if (model.isShowTessellationBoundingVolumes())
					{
						gl.glColor3d(1, 0, 0);
						sg.renderBoundingVolume(dc);
					}
				}

				gl.glColor4fv(previousColor, 0);
			}
		}
		catch (Throwable e)
		{
			Logging.logger().log(Level.SEVERE, Logging.getMessage("BasicSceneController.ExceptionDuringRendering"), e);
		}
	}


	protected void setStereo(boolean isStereo)
	{

		if (isStereo)
		{
			this.setStereoMode(AVKey.STEREO_MODE_RED_BLUE);
			// boolean hardwareStereo = dc.getGLContext().getGLDrawable().getChosenGLCapabilities().getStereo();
			this.hardwareStereo = this.capabilities.getStereo();
			this.inStereo = this.isHardwareStereo() ? true : this.isInStereo();
		}
		else
		{
			this.setStereoMode(null);
			this.hardwareStereo = this.capabilities.getStereo();
			this.inStereo = this.isHardwareStereo() ? true : this.isInStereo();
		}

	}


	// Override black background color
	protected void setClearColor(Color theColor)
	{
		this.clearColor = theColor;
	}


	protected Color getClearColor()
	{
		return this.clearColor;
	}


	@Override
	protected void clearFrame(DrawContext dc)
	{
		Color cc = this.getClearColor();
		dc.getGL().glClearColor(cc.getRed() / 255.f, cc.getGreen() / 255.f, cc.getBlue() / 255.f, cc.getAlpha() / 255.f);
		dc.getGL().glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
	}

}
