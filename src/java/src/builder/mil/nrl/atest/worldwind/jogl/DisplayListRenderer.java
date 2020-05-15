/*
 *  =======================================================================
 *  ==                                                                   ==
 *  ==                   Classification: UNCLASSIFIED                    ==
 *  ==                   Classified By:                                  ==
 *  ==                   Declassify On:                                  ==
 *  ==                                                                   ==
 *  =======================================================================
 *
 *  Developed by:
 *
 *  Naval Research Laboratory
 *  Tactical Electronic Warfare Division
 *  Electronic Warfare Modeling and Simulation Branch
 *  Advanced Tactical Environmental Simulation Team (Code 5774)
 *  4555 Overlook Ave SW
 *  Washington, DC 20375
 *
 *  For more information call (202)767-2897 or send email to
 *  BuilderSupport@nrl.navy.mil.
 *
 *  The U.S. Government retains all rights to use, duplicate,
 *  distribute, disclose, or release this software.
 */
/*

 */
package builder.mil.nrl.atest.worldwind.jogl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLProfile;
import javax.media.opengl.fixedfunc.GLLightingFunc;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import builder.mil.nrl.spg.logging.LoggerExt;

import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import net.java.joglutils.model.ResourceRetriever;
import net.java.joglutils.model.geometry.Bounds;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.geometry.Vec4;

/**
 * DisplayListRenderer.java
 * 
 * Created on February 27, 2008, 11:05 PM
 * 
 * R. Wathelet added line 123 gl.glDisable(GL.GL_TEXTURE_2D);
 * 
 * @author RodgersGB modifications made by Brian Wood and Z-Knight modifications made by Eterna2 (added
 *         renderPickerList, etc - for display list without texture for picking in WWJ) modifications by Christian
 *         Doyle: fixed issue where picking would not work correctly if one had loaded different models
 * 
 *         Copied from joglutils and modified to use WorldWind Vec4 and Material classes on June 3, 2010 by Ian Will
 */
public class DisplayListRenderer
{

	private Logger log = Logger.getLogger(DisplayListRenderer.class.getName());

	private static DisplayListRenderer instance = new DisplayListRenderer();

	private DisplayListCache listCache = new DisplayListCache();

	private HashMap<Integer, Texture> texture;

	/**
	 * offset in the glCallList to access the model bounds
	 */
	private static final int MODEL_BOUNDS_OFFSET = 1;

	/**
	 * offset in the glCallList to access the object bounds
	 */
	private static final int OBJECT_BOUNDS_OFFSET = 2;

	/**
	 * offset in the glCallList to access a texture less object for picking
	 */
	private static final int PICKER_OFFSET = 3;

	private boolean isDebugging = true;


	/** Creates a new instance of DisplayListModel3D */
	public DisplayListRenderer()
	{
	}


	public static DisplayListRenderer getInstance()
	{
		return instance;
	}


	public void debug(boolean value)
	{
		this.isDebugging = value;
	}


	public void clearList(GL2 gl, Model model)
	{
		listCache.remove(model, gl, 4);
	}


	public void render(DrawContext context, Model model)
	{
		GL2 gl = context.getGL().getGL2();

		if (gl == null)
		{
			return;
		}

		if (model == null)
		{
			return;
		}

		int displayList = listCache.get(model);

		if (displayList < 0 )
		{
			displayList = initialize(context, gl, context.getGlobe(), model);
			if (this.isDebugging)
			{
				log.info("Initialized the display list for model: " + model.getSource());
			}
		}

		// save some current state variables
		boolean isTextureEnabled = gl.glIsEnabled(GL.GL_TEXTURE_2D);
		boolean isLightingEnabled = gl.glIsEnabled(GLLightingFunc.GL_LIGHTING);
		boolean isMaterialEnabled = gl.glIsEnabled(GLLightingFunc.GL_COLOR_MATERIAL);

		// check lighting
		if (!model.isUsingLighting())
		{
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
		}

		// check texture
		if (model.isUsingTexture())
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		else
		{
			gl.glDisable(GL.GL_TEXTURE_2D);
		}

		// check wireframe
		if (model.isRenderingAsWireframe())
		{
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
		}
		else
		{
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
		}

		// check for unit size model
		if (model.isUnitizeSize())
		{
			float scale = 1.0f / model.getBounds().getRadius();
			gl.glScalef(scale, scale, scale);
		}

		if (model.isCentered())
		{
			Vec4 center = model.getCenterPoint();
			gl.glTranslated(-center.x, -center.y, -center.z);
		}

		if (context.isPickingMode())
		{
			gl.glDisable(GL.GL_TEXTURE_2D); // added by R. Wathelet
			gl.glCallList(displayList + PICKER_OFFSET);
		}
		else
		{

			if (model.isRenderModel())
			{
				gl.glCallList(displayList);
			}

			// Disabled lighting for drawing the boundary lines so they are all white (or whatever I chose)
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
			if (model.isRenderModelBounds())
			{
				gl.glCallList(displayList + MODEL_BOUNDS_OFFSET);
			}
			if (model.isRenderObjectBounds())
			{
				gl.glCallList(displayList + OBJECT_BOUNDS_OFFSET);
			}
		}

		// Reset the flags back for lighting and texture
		if (isTextureEnabled)
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
		}
		else
		{
			gl.glDisable(GL.GL_TEXTURE_2D);
		}

		if (isLightingEnabled)
		{
			gl.glEnable(GLLightingFunc.GL_LIGHTING);
		}
		else
		{
			gl.glDisable(GLLightingFunc.GL_LIGHTING);
		}

		if (isMaterialEnabled)
		{
			gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
		}
		else
		{
			gl.glDisable(GLLightingFunc.GL_COLOR_MATERIAL);
		}

		if (model.isRenderingAsWireframe())
		{
			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
		}
	}


	/**
	 * Load the model and associated materials, etc
	 * 
	 * @param dc
	 * @param gl
	 * @param globe
	 * @param model
	 * @return
	 */
	private int initialize(DrawContext dc, GL2 gl, Globe globe, Model model)
	{
		if (this.isDebugging)
		{
			log.info("Initialize Model: " + model.getSource());
		}
		int numMaterials = model.getNumberOfMaterials();
		if (this.isDebugging && numMaterials > 0)
		{
			log.info("\n    Loading " + numMaterials + " Materials:");
		}
		texture = new HashMap<>();
		for (int i = 0; i < numMaterials; i++)
		{
			if (model.getMaterial(i).strFile != null)
			{
				String file = model.getSource();
				final String textureFileName = model.getMaterial(i).strFile;
				URL result = findTextureFile(file, textureFileName, isDebugging).orElse(null);

				if (result != null && !result.getPath().endsWith("/") && !result.getPath().endsWith("\\"))
				{
					loadTexture(result, model.getMaterial(i).textureId);
					if (this.isDebugging)
					{
						log.info(" ... done. Texture ID: " + i);
					}
				}
				else if (this.isDebugging)
				{
					log.info(" ... failed (no result for material)");
				}
			}
		}

		if (this.isDebugging && numMaterials > 0)
		{
			log.info("    Load Materials: Done");
		}

		if (this.isDebugging)
		{
			log.info("\n    Generate Lists:");
		}
		int compiledList = listCache.generateList(model, gl, 4);

		if (this.isDebugging)
		{
			log.info("        Model List");
		}
		gl.glNewList(compiledList, GL2.GL_COMPILE);
		genList(dc, gl, globe, model, true);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("        Boundary List");
		}
		gl.glNewList(compiledList + MODEL_BOUNDS_OFFSET, GL2.GL_COMPILE);
		genModelBoundsList(gl, model);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("        Object Boundary List");
		}
		gl.glNewList(compiledList + OBJECT_BOUNDS_OFFSET, GL2.GL_COMPILE);
		genObjectBoundsList(gl, model);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("        Picker Render List");
		}
		gl.glNewList(compiledList + PICKER_OFFSET, GL2.GL_COMPILE);
		genList(dc, gl, globe, model, false);
		gl.glEndList();

		if (this.isDebugging)
		{
			log.info("    Generate Lists: Done");
			log.info("Load Model: Done");
		}

		return compiledList;
	}

	public static Optional<URL> findLocalTextureFile(String modelFileName, String textureFileName, boolean debug)
	{
		// Get path to local textureFileName from model path
		Path modelFilePath = FileSystems.getDefault().getPath(modelFileName);
		String textureFilePath = modelFilePath.getParent() + File.separator + textureFileName;
		
		File textureFile = new File(textureFilePath);
		if (textureFile.exists())
		{
			try {
				return Optional.of(ResourceRetriever.getResourceAsUrl(textureFilePath.replaceAll("#", "%23")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return Optional.empty();
	}
	
	
	public static Optional<URL> findTextureFile(String file, String textureFileName, boolean debug)
	{
		int lastIndex = textureFileName.lastIndexOf("/");
		String strippedTextureFileName;
		if (lastIndex != -1)
		{
			strippedTextureFileName = textureFileName.substring(lastIndex + 1);
		}
		else
		{
			int backslashIndex = textureFileName.lastIndexOf("\\");
			if (backslashIndex != -1)
			{
				strippedTextureFileName = textureFileName.substring(backslashIndex + 1);
			}
			else
			{
				strippedTextureFileName = textureFileName;
			}
		}
		int rgbaIndex = strippedTextureFileName.indexOf(".rgba");
		int rgbIndex = strippedTextureFileName.indexOf(".rgb");
		int intIndex = strippedTextureFileName.indexOf(".int");
		if (rgbaIndex != -1)
		{
			strippedTextureFileName = strippedTextureFileName.substring(0, rgbaIndex + 5);
		}
		else
		{
			if (rgbIndex != -1)
			{
				strippedTextureFileName = strippedTextureFileName.substring(0, rgbIndex + 4);
			}
			else
			{
				if (intIndex != -1)
				{
					strippedTextureFileName = strippedTextureFileName.substring(0, intIndex + 4);
				}
			}
		}

		int jarInd = file.indexOf(".jar!/");

		if (jarInd == -1)
		{
			return findLocalTextureFile(file, strippedTextureFileName, debug);
		}
		
		String jarPath = file.substring(0, jarInd + 6);
			
		StringBuilder textureFilePath = new StringBuilder(jarPath);
		try
		{
			URL jarUrl = new URL(jarPath);
			JarURLConnection jarURLConnection = (JarURLConnection) jarUrl.openConnection();
			File jarFile = new File(jarURLConnection.getJarFileURL().toURI());
			try (ZipFile zipFile = new ZipFile(jarFile))
			{
				Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				while (zipEntries.hasMoreElements())
				{
					ZipEntry zipEntry = zipEntries.nextElement();
					String zipEntryName = zipEntry.getName();
					if (zipEntry.isDirectory() || !(zipEntry.getName().contains(".rgb") || zipEntry.getName().contains(".int")))
					{
						continue;
					}
					int index = zipEntryName.lastIndexOf("/");
					String nameOfCurrentFile = "";
					if (index != -1)
					{
						nameOfCurrentFile = zipEntryName.substring(index + 1);
					}
					else
					{
						nameOfCurrentFile = zipEntryName;
					}
					if (nameOfCurrentFile.equalsIgnoreCase(strippedTextureFileName))
					{
						textureFilePath.append(zipEntryName);
						LoggerExt.info(""
							+ DisplayListRenderer.class.getSimpleName()
							+ " is loading texture "
							+ strippedTextureFileName
							+ ".");
						break;
					}
				}
				if (textureFilePath.toString().equals(jarPath))
				{
					LoggerExt.warning(""
						+ DisplayListRenderer.class.getSimpleName()
						+ " could not find texture "
						+ strippedTextureFileName
						+ ".");
				}
			}
		}
		catch (IOException | URISyntaxException e)
		{
			LoggerExt.warningWithFineThrowable(e);
		}
		try
		{
			if (!textureFilePath.toString().equals(jarPath))
			{
				return Optional.of(ResourceRetriever.getResourceAsUrl(textureFilePath.toString().replaceAll("#", "%23")));
			}
		}
		catch (IOException e)
		{
			if (debug)
			{
				LoggerExt.warning(" ... failed");
			}
		}
		return Optional.empty();
	}


	/**
	 * Load a texture given by the specified URL and assign it to the texture id that is passed in.
	 * 
	 * @param url
	 * @param id
	 */
	private void loadTexture(URL url, int id)
	{
		if (url != null)
		{
			BufferedImage bufferedImage;
			try
			{
				bufferedImage = ImageIO.read(url.openStream());
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, " ... FAILED loading texture " + url + " with exception: ", e);
				return;
			}
			try (ByteArrayOutputStream os = new ByteArrayOutputStream())
			{
				ImageIO.write(bufferedImage, "png", os);
				try (InputStream is = new ByteArrayInputStream(os.toByteArray()))
				{
					texture.put(Integer.valueOf(id), TextureIO.newTexture(is, true, "png"));
				}
			}
			catch (IOException e)
			{
				LoggerExt.warningWithFineThrowable(e);
			}
		}
	}


	private void genList(DrawContext dc, GL2 gl, Globe globe, Model model, boolean isFullRender)
	{
		final Vec4[] vertices;
		
		vertices = model.getMesh(0).vertices;

		genList(gl, model, vertices, isFullRender);
	}


	/**
	 * Generate the model display lists
	 * 
	 * @param gl
	 */
	private void genList(GL2 gl, Model model, Vec4[] vertices, boolean isFullRender)
	{
		for (int i = 0; i < model.getNumberOfMeshes(); i++)
		{
			Mesh mesh = model.getMesh(i);
			if (mesh.numOfFaces == 0)
			{
				log.warning("Mesh: " + mesh.name + " has no faces");
				continue;
			}

			for (int j = 0; j < mesh.numOfFaces; j++)
			{
				if (mesh.faces[j] != null)
				{
					// If the object has a texture, then do nothing till later...else
					// apply the material property to it.
					if (mesh.hasTexture && isFullRender)
					{
						loadTexture(gl, mesh.faces[j]);
					}

					int indexType = 0;
					int vertexIndex = 0;
					int normalIndex = 0;
					int textureIndex = 0;

					if (isFullRender)
					{

						gl.glEnable(GL.GL_BLEND);
						gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					}

					gl.glBegin(GL2.GL_POLYGON);
					for (int whichVertex = 0; whichVertex < mesh.faces[j].vertIndex.length; whichVertex++)
					{
						// LJT
						//if (mesh.faces[j] instanceof mil.nrl.atest.worldwind.jogl.ATESTFace && mesh instanceof mil.nrl.atest.worldwind.jogl.ATESTMesh)
						//{
					//		if (isFullRender && ((mil.nrl.atest.worldwind.jogl.ATESTFace) mesh.faces[j]).colorIndex != null)
					//		{
					//			int colorIndex = ((mil.nrl.atest.worldwind.jogl.ATESTFace) mesh.faces[j]).colorIndex[whichVertex];
					//			Color vertColor = ((mil.nrl.atest.worldwind.jogl.ATESTMesh) mesh).getColor(colorIndex);
					//			gl.glColor4f(intToFloat(vertColor.getRed()), intToFloat(vertColor.getGreen()), intToFloat(vertColor.getBlue()),
					//				intToFloat(vertColor.getAlpha()));
					//		}
					//	}

						vertexIndex = mesh.faces[j].vertIndex[whichVertex];

						try
						{
							normalIndex = mesh.faces[j].normalIndex[whichVertex];

							indexType = 0;
							if (mesh.normals[normalIndex] != null)
							{
								gl.glNormal3d(mesh.normals[normalIndex].x, mesh.normals[normalIndex].y, mesh.normals[normalIndex].z);
							}

							if (mesh.hasTexture)
							{
								if (mesh.texCoords != null)
								{
									textureIndex = mesh.faces[j].coordIndex[whichVertex];
									indexType = 1;
									if (mesh.texCoords[textureIndex] != null)
									{
										gl.glTexCoord2f(mesh.texCoords[textureIndex].u, mesh.texCoords[textureIndex].v);
									}
								}
							}
							indexType = 2;
							if (vertices[vertexIndex] != null)
							{
								gl.glVertex3d(vertices[vertexIndex].x, vertices[vertexIndex].y, vertices[vertexIndex].z);
							}
						}
						catch (Exception e)
						{
							log.log(Level.WARNING, "", e);
							switch (indexType)
							{
								case 0:
									log.warning("Normal index " + normalIndex + " is out of bounds");
									break;

								case 1:
									log.warning("Texture index " + textureIndex + " is out of bounds");
									break;

								case 2:
									log.warning("Vertex index " + vertexIndex + " is out of bounds");
									break;

								default:
							}
						}

					}
					gl.glEnd();

					if (isFullRender)
					{
						gl.glDisable(GL.GL_BLEND);
					}

					if (mesh.hasTexture && texture.get(Integer.valueOf(mesh.faces[j].materialID)) != null && isFullRender)
					{
						Texture t = texture.get(Integer.valueOf(mesh.faces[j].materialID));
						if (t != null)
						{
							t.disable(gl);
						}

						gl.glMatrixMode(GL.GL_TEXTURE);
						gl.glPopMatrix();
					}

				}
			}

		}

		// Try this clearing of color so it won't use the previous color
		gl.glColor3f(1.0f, 1.0f, 1.0f);
	}


	private void loadTexture(GL2 gl, net.java.joglutils.model.geometry.Face face)
	{
		if (face != null)
		{
			if (texture.get(Integer.valueOf(face.materialID)) != null)
			{
				Texture t = texture.get(Integer.valueOf(face.materialID));

				// switch to texture mode and push a new matrix on the stack
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glPushMatrix();

				// check to see if the texture needs flipping
				if (t.getMustFlipVertically())
				{
					gl.glScaled(1, -1, 1);
					gl.glTranslated(0, -1, 0);
				}

				// This is required to repeat textures...because some are not and so only
				// part of the model gets filled in....Might be a way to check if this is
				// required per object but I'm not sure...would need to research this.
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

				// enable, bind and get texture coordinates
				t.enable(gl);
				t.bind(gl);
			}
		}
	}


	/**
	 * Render the desired object of the model (specified by an id number of the object)
	 * 
	 * @param gl
	 * @param id
	 */
	public void renderBoundsOfObject(GL2 gl, int id, Model model)
	{
		if (id >= 0 && id <= model.getNumberOfMeshes())
		{
			if (model.getMesh(id).bounds != null)
			{
				drawBounds(gl, model.getMesh(id).bounds);
			}
		}
	}


	/**
	 * Draw the boundary of the model (the large box representing the entire model and not the object in it)
	 * 
	 * @param gLDrawable
	 */
	// This is commented out in builder code too
	// private void genModelBoundsList(GLAutoDrawable gLDrawable, Model model)
	// {
	// GL gl = gLDrawable.getGL();
	// drawBounds(gl, model.getBounds());
	// }

	/**
	 * Draw the boundary of the model (the large box representing the entire model and not the object in it)
	 * 
	 * @param gl
	 */
	private void genModelBoundsList(GL2 gl, Model model)
	{
		drawBounds(gl, model.getBounds());
	}


	/**
	 * Draw the boundaries over all of the objects of the model
	 * 
	 * @param gLDrawable
	 */
	// This is commented out in Builder code too
	// private void genObjectBoundsList(GLAutoDrawable gLDrawable, Model model)
	// {
	// GL gl = gLDrawable.getGL();
	// genObjectBoundsList(gl, model);
	// }

	/**
	 * Draw the boundaries over all of the objects of the model
	 * 
	 * @param gl
	 */
	private void genObjectBoundsList(GL2 gl, Model model)
	{
		for (int i = 0; i < model.getNumberOfMeshes(); i++)
		{
			if (model.getMesh(i).bounds != null)
			{
				drawBounds(gl, model.getMesh(i).bounds);
			}
		}
	}


	/**
	 * Draws the bounding box of the object using the max and min extrema points.
	 * 
	 * @param gl
	 * @param bounds
	 */
	private void drawBounds(GL2 gl, Bounds bounds)
	{
		// Front Face
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(bounds.min.x, bounds.min.y, bounds.min.z);
		gl.glVertex3d(bounds.max.x, bounds.min.y, bounds.min.z);
		gl.glVertex3d(bounds.max.x, bounds.max.y, bounds.min.z);
		gl.glVertex3d(bounds.min.x, bounds.max.y, bounds.min.z);
		gl.glEnd();

		// Back Face
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3d(bounds.min.x, bounds.min.y, bounds.max.z);
		gl.glVertex3d(bounds.max.x, bounds.min.y, bounds.max.z);
		gl.glVertex3d(bounds.max.x, bounds.max.y, bounds.max.z);
		gl.glVertex3d(bounds.min.x, bounds.max.y, bounds.max.z);
		gl.glEnd();

		// Connect the corners between the front and back face.
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(bounds.min.x, bounds.min.y, bounds.min.z);
		gl.glVertex3d(bounds.min.x, bounds.min.y, bounds.max.z);

		gl.glVertex3d(bounds.max.x, bounds.min.y, bounds.min.z);
		gl.glVertex3d(bounds.max.x, bounds.min.y, bounds.max.z);

		gl.glVertex3d(bounds.max.x, bounds.max.y, bounds.min.z);
		gl.glVertex3d(bounds.max.x, bounds.max.y, bounds.max.z);

		gl.glVertex3d(bounds.min.x, bounds.max.y, bounds.min.z);
		gl.glVertex3d(bounds.min.x, bounds.max.y, bounds.max.z);
		gl.glEnd();
	}


	/**
	 * Convert an Unsigned byte to integer
	 * 
	 * @param b
	 * @return
	 */
	public int unsignedByteToInt(byte b)
	{
		return b & 0xFF;
	}


	/**
	 * Convert integer to float
	 * 
	 * @param i
	 * @return
	 */
	public float intToFloat(int i)
	{
		return i / 255.0f;
	}

	public static class DisplayListCache
	{

		private HashMap<Object, Integer> listCache;


		/**
		 * Creates a new instance of WWDisplayListCache
		 */
		private DisplayListCache()
		{
			listCache = new HashMap<>();
		}


		public void clear()
		{
			listCache.clear();
		}


		public int get(Object objID)
		{
			if (listCache.containsKey(objID))
			{
				return listCache.get(objID).intValue();
			}
			return -1;
		}


		public void remove(Object objID, GL2 gl, int howMany)
		{
			Integer list = listCache.get(objID);

			if (list != null)
			{
				gl.glDeleteLists(list.intValue(), howMany);
			}

			listCache.remove(objID);
		}


		/**
		 * Returns an integer identifier for an OpenGL display list based on the object being passed in. If the object
		 * already has a display list allocated, the existing ID is returned.
		 */
		public int generateList(Object objID, GL2 gl, int howMany)
		{
			Integer list;

			list = listCache.get(objID);
			if (list == null)
			{
				list = Integer.valueOf(gl.glGenLists(howMany));
				listCache.put(objID, list);
			}

			return list.intValue();
		}

	}
}
