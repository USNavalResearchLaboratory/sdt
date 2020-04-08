package builder.mil.nrl.atest.worldwind.openflight;

import java.awt.Color;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import builder.mil.nrl.spg.logging.LoggerExt;
import net.java.joglutils.model.geometry.Bounds;
import net.java.joglutils.model.geometry.Material;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

/**
 * Creates a {@link Model} from an {@link VertexPalette} and list of {@link Face}s
 * 
 * @author doyle
 * @since Jul 29, 2009
 * 
 */
public class ModelBuilder
{

	/**
	 * Converts the flt file into a Model which can be displayed in jogl
	 * 
	 * @return a JOGL model with textures, faces, and vertices loaded
	 */
	public Model build(URL source, VertexPalette vertPalette, List<Face> faces, List<TexturePalette> texPalette)
	{
		final Model model = new Model(source.toString());
		final Mesh mesh = new Mesh();
		final Map<Vertex, Integer> vertLookupTable = loadVerticesAndNormals(vertPalette, mesh);
		final Map<TexCoord, Integer> texLookupTable = createTextureLookupTable(vertPalette);
		loadTexCoords(texLookupTable, mesh);
		loadFaces(faces, vertLookupTable, texLookupTable, mesh);
		loadMaterials(texPalette, model);
		model.addMesh(mesh);
		model.setBounds(getBounds(vertPalette));

		return model;
	}


	/**
	 * Loads the textures from the palette into the {@link Model} in the form of {@link Material}s
	 * 
	 * @param palettes the set of textures read in from the file
	 * @param model the JOGL model we are building
	 */
	private void loadMaterials(List<TexturePalette> palettes, Model model)
	{
		for (TexturePalette palette : palettes)
		{
			model.addMaterial(createMaterial(palette));
		}
	}


	/**
	 * Creates the material from the {@link TexturePalette}
	 * 
	 * @param palette the texture to build a material from
	 * @return a {@link Material} object created from the {@link TexturePalette}
	 */
	private Material createMaterial(TexturePalette palette)
	{
		final Material m = new Material();
		m.ambientColor = Color.white;
		m.diffuseColor = Color.white;
		m.specularColor = Color.white;
		m.strFile = palette.getFileName();
		m.textureId = palette.getIndex();

		return m;
	}


	/**
	 * Converts the {@link Vertex} in {@link VertexPalette} to a form that the {@link Mesh} can use, as a side effect
	 * creates a lookup table of the locations of the original {@link Vertex} in the new array.
	 * 
	 * @param palette return a lookup table from the {@link Vertex} to the position in the {@link Mesh}'s array
	 */
	private Map<Vertex, Integer> loadVerticesAndNormals(VertexPalette palette, Mesh m)
	{
		Map<Vertex, Integer> lookupTable = new LinkedHashMap<>();
		if (palette != null)
		{
			final Vec4[] vertices = new Vec4[palette.size()];
			final Vec4[] normals = new Vec4[palette.size()];
			int i = 0;
			if (palette.getVertices() != null)
			{
				for (Vertex v : palette.getVertices())
				{
					if (v != null)
					{
						vertices[i] = v.getVert();
						normals[i] = v.getNormal();
						lookupTable.put(v, Integer.valueOf(i));
					}
					i++;
				}
			}
			m.vertices = vertices;
			m.normals = normals;
			m.numOfVerts = palette.size();
		}

		return lookupTable;
	}


	/**
	 * Creates a lookup table for all of the texture coordinates
	 * 
	 * @param palette
	 * @return
	 */
	private Map<TexCoord, Integer> createTextureLookupTable(VertexPalette palette)
	{

		Map<TexCoord, Integer> lookupTable = new LinkedHashMap<>();
		if (palette != null)
		{
			int i = 0;
			for (Vertex v : palette.getVertices())
			{
				if (v != null && !lookupTable.containsKey(v.getTextureCoord()))
				{
					lookupTable.put(v.getTextureCoord(), Integer.valueOf(i));
					i++;
				}
			}
		}

		return lookupTable;
	}


	/**
	 * Loads all of the texture coordinates into the {@link Mesh}. Returns a lookup table to make references to the
	 * index of
	 * the coord in the table
	 * 
	 * @param lookupTable
	 * @param m
	 * @return
	 */
	private void loadTexCoords(Map<TexCoord, Integer> lookupTable, Mesh m)
	{
		final TexCoord[] coords = new TexCoord[lookupTable.size()];

		for (Map.Entry<TexCoord, Integer> coord : lookupTable.entrySet())
		{
			if (coord.getValue().intValue() < coords.length)
			{
				coords[coord.getValue().intValue()] = coord.getKey();
			}
			else
			{
				LoggerExt.warning(""
					+ "We tried to add to coords outside of its array bounds (index="
					+ coord.getValue()
					+ "; coords.length="
					+ coords.length
					+ ").");
			}
		}
		m.hasTexture = true;
		m.texCoords = coords;
		m.numTexCoords = coords.length;
	}


	/**
	 * Loads all of the faces from the list into an array of {@link Face}, these are then added to the {@link Mesh}
	 * 
	 * @param faces a list of {@link Face} objects obtained from reading the file
	 * @param vertLookupTable a Map<{@link Vertex}, {@link Integer}> that contains all vertices belonging to the model
	 * @param texLookupTable a Map<{@link TexCoord}, {@link Integer}> that contains all texture coordinates that belong
	 *        to the mesh
	 * @param m the {@link Mesh} of the model
	 */
	private void loadFaces(List<Face> faces, Map<Vertex, Integer> vertLookupTable, Map<TexCoord, Integer> texLookupTable, Mesh m)
	{
		final net.java.joglutils.model.geometry.Face[] faceArray = new net.java.joglutils.model.geometry.Face[faces.size()];
		int i = 0;
		for (Face face : faces)
		{
			if (face != null)
			{
				faceArray[i] = createMeshFace(face, vertLookupTable, texLookupTable);
				i++;
			}
		}

		m.faces = faceArray;
		m.numOfFaces = faces.size();
	}


	/**
	 * Creates the {@link Face} object
	 * 
	 * @param face the {@link Face} object from the OpenFlight model
	 * @param vertLookupTable a Map<{@link Vertex}, {@link Integer}> that contains all vertices belonging to the model
	 * @return the JOGL {@link net.java.joglutils.model.geometry.Face} object
	 */
	private net.java.joglutils.model.geometry.Face createMeshFace(Face face, Map<Vertex, Integer> vertLookupTable,
			Map<TexCoord, Integer> coordLookupTable)
	{
		final net.java.joglutils.model.geometry.Face newFace = new net.java.joglutils.model.geometry.Face(face.getNumVertices());
		newFace.materialID = face.getTextureIndex();

		int i = 0;
		for (Vertex v : face.getVertices())
		{
			if (v != null)
			{
				int vertIndex = vertLookupTable.get(v).intValue();
				newFace.vertIndex[i] = vertIndex;
				newFace.normalIndex[i] = vertIndex;
				if (v.hasTexture())
				{
					int coordIndex = coordLookupTable.get(v.getTextureCoord()).intValue();
					newFace.coordIndex[i] = coordIndex;
				}
			}
			i++;
		}
		return newFace;
	}


	/**
	 * Searches the vertex palette to find the bounds of the object
	 * 
	 * @param palette the {@link VertexPalette} of the model, which holds all the vertices
	 * @return the {@link Bounds} of all the vertices
	 */
	private Bounds getBounds(VertexPalette palette)
	{
		float minX = 0.0f;
		float minY = 0.0f;
		float minZ = 0.0f;

		float maxX = 0.0f;
		float maxY = 0.0f;
		float maxZ = 0.0f;

		if (palette != null)
		{
			for (Vertex v : palette.getVertices())
			{
				if (v != null)
				{
					if (v.getVert().x < minX)
					{
						minX = v.getVert().x;
					}

					if (v.getVert().y < minY)
					{
						minY = v.getVert().y;
					}

					if (v.getVert().z < minZ)
					{
						minZ = v.getVert().z;
					}

					if (v.getVert().x > maxX)
					{
						maxX = v.getVert().x;
					}

					if (v.getVert().y > maxY)
					{
						maxY = v.getVert().y;
					}

					if (v.getVert().z > maxZ)
					{
						maxZ = v.getVert().z;
					}
				}

			}
		}

		return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
