package builder.mil.nrl.atest.worldwind.openflight;

import java.util.logging.Logger;

import builder.mil.nrl.atest.icon.Orientation;

/**
 * Represents a record which contains a list of vertices
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class VertexList
{
	private static final Logger log = Logger.getLogger(VertexList.class.getSimpleName());

	public static class VertexListFactory
	{

		private VertexListFactory()
		{
			// no op
		}


		/**
		 * Loads the vertices into the face they belong in
		 * 
		 * @param data
		 * @param palette
		 * @param face
		 * @param orient
		 */
		public void load(byte[] data, VertexPalette palette, Face face, Orientation orient)
		{

			for (int i = 4; i < data.length; i += 4)
			{

				final int vertexOffset = BinUtil.intFromBytes(data, i, 4);

				log.info("Palette vertex offset: " + vertexOffset);

				final Vertex v = palette.getVertex(vertexOffset, orient);

				face.addVertex(v);
			}
		}
	}

	public static final VertexListFactory FACTORY = new VertexListFactory();
}
