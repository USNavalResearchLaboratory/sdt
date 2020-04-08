package builder.mil.nrl.atest.worldwind.openflight;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.java.joglutils.model.examples.DisplayListRenderer;

//import mil.nrl.atest.worldwind.jogl.DisplayListRenderer;

/**
 * Contains information about the flt file. This class keeps track of all of the vertex, face, material and other
 * related information in the flt file as it is parsed
 * 
 * @author doyle
 * @since Aug 7, 2009
 * 
 */
public class OpenFlightFile
{

	private List<Face> faces;

	private List<TexturePalette> texturePalettes;

	private VertexPalette vertexPalette;


	public OpenFlightFile()
	{
		this.faces = new ArrayList<>();
		this.texturePalettes = new ArrayList<>();
	}


	public void addVertexPalette(VertexPalette palette)
	{
		this.vertexPalette = palette;
	}


	public VertexPalette getVertexPalette()
	{
		return this.vertexPalette;
	}


	public Face getLastFace()
	{
		if (faces.size() > 0)
		{
			return faces.get(faces.size() - 1);
		}

		return null;
	}


	public void addFace(Face face)
	{
		faces.add(face);
	}


	public List<Face> getFaces()
	{
		return faces;
	}


	public void addTexturePalette(TexturePalette palette)
	{
		texturePalettes.add(palette);
	}


	public List<TexturePalette> getTexturePalettes()
	{
		return texturePalettes;
	}


	public void removeUnwantedTextures(String source)
	{
		texturePalettes = texturePalettes.stream()
				.filter(texPalette -> doesTextureExist(source, texPalette.getFileName()))
				.collect(Collectors.toList());

		// The number of faces is important. We must set faces that we do not want to
		// null. If we do not do this, the renderer assigns some faces the wrong
		// texture.
		faces = faces.stream()
				.map(face -> face == null ? null : (texturePalettes.stream()
						.anyMatch(texPalette -> texPalette.getIndex() == face.getTextureIndex()) ? face : null))
				.collect(Collectors.toList());
	}


	boolean doesTextureExist(String source, String textureFileName)
	{
		return DisplayListRenderer.findTextureFile(source, textureFileName, false).isPresent();
	}
}
