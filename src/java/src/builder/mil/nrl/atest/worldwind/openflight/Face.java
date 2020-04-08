package builder.mil.nrl.atest.worldwind.openflight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a face record in an OpenFlight file
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class Face
{
	private final static Logger log = Logger.getLogger(Face.class.getSimpleName());

	public static class FaceFactory
	{

		private FaceFactory()
		{
			// no op
		}


		public Face load(byte[] data)
		{
			int textureIndex = BinUtil.intFromBytes(data, 28, 2);

			log.info("Texture index: " + textureIndex);

			return new Face(textureIndex);
		}

	}

	public static final FaceFactory FACTORY = new FaceFactory();

	private final List<Vertex> vertices;

	private final int textureIndex;


	private Face(int textureIndex)
	{
		this.textureIndex = textureIndex;
		vertices = new ArrayList<>();
	}


	public void addVertex(Vertex v)
	{
		vertices.add(v);
	}


	public int getNumVertices()
	{
		return vertices.size();
	}


	public Vertex getVertex(int i)
	{
		return vertices.get(i);
	}


	public int getTextureIndex()
	{
		return textureIndex;
	}


	public Collection<Vertex> getVertices()
	{
		return Collections.unmodifiableCollection(vertices);
	}
}
