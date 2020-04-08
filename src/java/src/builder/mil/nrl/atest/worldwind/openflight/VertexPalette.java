package builder.mil.nrl.atest.worldwind.openflight;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import builder.mil.nrl.atest.icon.Orientation;

import net.java.joglutils.model.geometry.Mesh;

/**
 * Represents a record which contains the palette of vertices. Every vertex in the model is contained in this palette.
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class VertexPalette
{
	private static final Logger log = Logger.getLogger(VertexPalette.class.getSimpleName());

	public static class VertexPaletteFactory
	{

		private VertexPaletteFactory()
		{
			// no op
		}


		public VertexPalette load(byte[] data, InputStream stream) throws IOException
		{
			final int length = BinUtil.intFromBytes(data, 4, 4);
			log.info("Vertex Palette Length: " + length);

			final byte[] palette = new byte[length];
			int offset = 8;
			int bufferSize = 1;
			int leftToRead = length - 8;

			while (leftToRead > 0)
			{
				while (stream.available() < bufferSize)
				{
					// Wait until bytes are available before reading.
				}
				int numBytesRead = stream.read(palette, offset, bufferSize);
				if (numBytesRead == -1)
					break;
				offset += numBytesRead;
				leftToRead -= numBytesRead;

				if (leftToRead < bufferSize)
				{
					bufferSize = leftToRead;
				}
			}

			return new VertexPalette(palette);
		}


		public Vertex processData(int opCode, byte[] data, Orientation orient)
		{
			Vertex vertex = null;

			switch (opCode)
			{
				case 68:
					log.info("Vertex with Color Record");
					break;

				case 69:
					log.info("Vertex with Color and Normal Record");
					vertex = VertexColorNormal.FACTORY.load(data, orient);
					break;

				case 70:
					log.info("Vertex with Color, Normal and UV Record");
					vertex = VertexColorNormalUV.FACTORY.load(data, orient);
					break;

				case 71:
					log.info("Vertex with Color and UV Record");
					break;

				default:
					log.warning("Unknown opcode: " + opCode);
					break;
			}

			return vertex;
		}
	}

	public static final VertexPaletteFactory FACTORY = new VertexPaletteFactory();

	/**
	 * Raw data from the palette
	 */
	private final byte[] palette;

	/**
	 * Map from the byte offset into the palette to the vertex data
	 */
	private final Map<Integer, Vertex> lookupTable;


	private VertexPalette(byte[] palette)
	{
		this.palette = palette;
		lookupTable = new LinkedHashMap<>();
	}


	/**
	 * Returns the index in the final palette of the given vertex
	 * 
	 * 
	 * @param offset the offset in the flt file
	 * @return the index that this vertex is located at in the {@link Mesh} file
	 */
	Vertex getVertex(int offset, Orientation orient)
	{
		Vertex v = lookupTable.get(Integer.valueOf(offset));
		if (v == null)
		{
			final int opCode = BinUtil.intFromBytes(palette, offset, 2);
			final int length = BinUtil.intFromBytes(palette, offset + 2, 2);
			v = FACTORY.processData(opCode, Arrays.copyOfRange(palette, offset, offset + length), orient);
			lookupTable.put(Integer.valueOf(offset), v);
		}
		else
		{
			log.info("Pulling vertex from palette");
		}

		return v;
	}


	public int size()
	{
		return lookupTable.values().size();
	}


	public Collection<Vertex> getVertices()
	{
		return lookupTable.values();
	}
}
