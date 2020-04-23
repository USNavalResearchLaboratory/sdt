/*
 * Model3DLayer.java
 *
 * Created on February 12, 2008, 10:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mil.navy.nrl.sdt3d;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

/**
 *
 * @author Laurie J Thompson
 * @date April 22, 2020
 */
public class Model3DLayer extends RenderableLayer //AbstractLayer
{
	private Vector<SdtSpriteModel> list;
	
	public Model3DLayer()
	{
		list = new Vector<SdtSpriteModel>();
	}

	
	public void addModel(SdtSpriteModel model)
	{
		list.add(model);
	}


	public void removeModel(SdtSpriteModel model)
	{
		list.remove(model);
	}


	public Iterable<SdtSpriteModel> getModels()
	{
		return java.util.Collections.unmodifiableCollection(this.list);
	}

	
	/*@Override
	public void render(DrawContext dc)
	{
		if (kmlController().getKmlRoot() != null)
		{
			kmlController().render(dc);
		}
		doRender(dc);
	}*/
	

	@Override
	protected void doRender(DrawContext dc)
	{
		try
		{
			beginDraw(dc);
			Iterator<SdtSpriteModel> it = list.iterator();

			while (it.hasNext())
			{
				//SdtSpriteModel theModel = it.next();
				//theModel.render(dc);	
				it.next().render(dc);
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
			endDraw(dc);
		}
	}

	//@Override
	protected void doRenderOrig(DrawContext dc)
	{
		/*
		try
		{
			beginDraw(dc);
			Iterator<SdtSpriteModel> it = list.iterator();

			while (it.hasNext())
			{
				//SdtSpriteModel theModel = it.next();
				//theModel.render(dc);	
				it.next().render(dc);
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
			endDraw(dc);
		}*/
	}

	// puts opengl in the correct state for this layer
	protected void beginDraw(DrawContext dc)
	{
		// we use dc.getGLU() to access the current glu rather than gl2
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		Vec4 cameraPosition = dc.getView().getEyePoint();

		gl.glPushAttrib(
			GL2.GL_TEXTURE_BIT |
				GL.GL_COLOR_BUFFER_BIT |
				GL.GL_DEPTH_BUFFER_BIT |
				GL2.GL_HINT_BIT |
				GL2.GL_POLYGON_BIT |
				GL2.GL_ENABLE_BIT |
				GL2.GL_CURRENT_BIT |
				GL2.GL_LIGHTING_BIT |
				GL2.GL_TRANSFORM_BIT);

		// float[] lightPosition = {0F, 100000000f, 0f, 0f};
		float[] lightPosition = { (float) (cameraPosition.x + 1000), (float) (cameraPosition.y + 1000), (float) (cameraPosition.z + 1000), 1.0f };

		/** Ambient light array */
		float[] lightAmbient = { 0.4f, 0.4f, 0.4f, 0.4f };
		/** Diffuse light array */
		float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
		/** Specular light array */
		float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };

		float[] model_ambient = { 0.5f, 0.5f, 0.5f, 1.0f };

		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);

		gl.glDisable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_NORMALIZE);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
	}


	// resets opengl state
	protected void endDraw(DrawContext dc)
	{
		// we use dc.getGLU() to access the current glu rather than gl2
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix();

		gl.glPopAttrib();
	}

}
