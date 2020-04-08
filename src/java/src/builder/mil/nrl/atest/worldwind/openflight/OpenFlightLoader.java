package builder.mil.nrl.atest.worldwind.openflight;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.spi.IIORegistry;

import builder.mil.nrl.atest.icon.Orientation;
import builder.mil.nrl.spg.logging.LoggerExt;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.loader.iLoader;

/**
 * This class loads a OpenFlight (.flt) file from a file.
 * 
 * @author doyle
 * @since Aug 7, 2009
 * 
 */
public class OpenFlightLoader implements iLoader
{
	private final static Logger log = LoggerExt.getLogger();

	private final Orientation orientation;


	public OpenFlightLoader(Orientation orientation)
	{
		this.orientation = orientation;
	}


	protected InputStream openSource(String source) throws IOException
	{
		return new URL(source).openStream();
	}


	private boolean readFourBytes(InputStream inputStream, byte[] bytes) throws IOException
	{
		int blockSize = 4;
		int numBytesRead = inputStream.read(bytes, 0, blockSize);
		if (numBytesRead == -1)
			return false;
		int offset = numBytesRead;
		while (offset < blockSize)
		{
			numBytesRead = inputStream.read(bytes, offset, blockSize - offset);
			if (numBytesRead == -1)
				return false;
			offset += numBytesRead;
		}
		return true;
	}


	@Override
	public Model load(String source) throws ModelLoadException
	{
		final IIORegistry registry = IIORegistry.getDefaultInstance();
		registry.registerServiceProvider(new SGIImageReaderSpi());

		final OpenFlightFile flt = new OpenFlightFile();

		Model model;
		try (InputStream inputStream = openSource(source))
		{
			final int blockSize = 4;
			final byte[] bytes = new byte[blockSize];

			int offset;
			while (readFourBytes(inputStream, bytes))
			{
				final int opCode = BinUtil.intFromBytes(bytes[0], bytes[1]);
				final int length = BinUtil.intFromBytes(bytes[2], bytes[3]);

				byte[] data;
				if (length > 0)
				{
					data = new byte[length];
					offset = blockSize;
					int bufferSize = 1;
					int leftToRead = length - blockSize;

					while (leftToRead > 0)
					{
						int numBytesRead = inputStream.read(data, offset, bufferSize);
						if (numBytesRead == -1)
							break;
						offset += numBytesRead;
						leftToRead -= numBytesRead;

						if (leftToRead < bufferSize)
						{
							bufferSize = leftToRead;
						}
					}
					if (data.length >= bytes.length)
					{
						System.arraycopy(bytes, 0, data, 0, bytes.length);
					}
					else
					{
						log.warning("data.length=" + data.length + " < bytes.length=" + bytes.length + " for " + source + ".");
					}
					processRecord(flt, opCode, data, inputStream);
				}
				else
				{
					log.warning("length=" + length + " is not positive for " + source + ".");
				}
			}
			flt.removeUnwantedTextures(source);
			model = new ModelBuilder().build(new URL(source), flt.getVertexPalette(), flt.getFaces(), flt.getTexturePalettes());
		}
		catch (IOException | RuntimeException e)
		{
			log.log(Level.WARNING, "", e);
			throw new ModelLoadException(e.getMessage());
		}

		return model;
	}


	/**
	 * Given the opcode and data, will load the information the the data byte array into the OpenFlightFile. How the
	 * information is parsed is based on the opcode.
	 * 
	 * @param opCode the code of the record
	 * @param data a byte array consisting of the data, note this includes the opcode length and data this way the
	 *        offsets will match up with those in the file
	 * @throws IOException
	 */
	private void processRecord(OpenFlightFile flt, int opCode, byte[] data, InputStream stream) throws IOException
	{
		switch (opCode)
		{
			case 1:
				log.info("Header Record");
				Header.FACTORY.load(data);
				break;

			case 2:
				log.info("Group Record");
				break;

			case 4:
				log.info("Object Record");
				break;

			case 5:
				log.info("Face Record");
				final Face face = Face.FACTORY.load(data);
				flt.addFace(face);
				break;

			case 10:
				log.info("Push Level Record");
				break;

			case 11:
				log.info("Pop Level Record");
				break;

			case 14:
				log.info("Degree of Freedom Record");
				break;

			case 19:
				log.info("Push Subface Record");
				break;

			case 20:
				log.info("Pop Subface Record");
				break;

			case 21:
				log.info("Push Extension Record");
				break;

			case 22:
				log.info("Header Record");
				break;

			case 23:
				log.info("Continuation Record");
				break;

			case 31:
				log.info("Comment Record");
				Comment.FACTORY.load(data);
				break;

			case 32:
				log.info("Color Palette Record");
				break;

			case 33:
				log.info("Long ID Record");
				break;

			case 49:
				log.info("Matrix Record");
				break;

			case 50:
				log.info("Vector Record");
				break;

			case 52:
				log.info("Multitexture Record");
				break;

			case 53:
				log.info("UV List Record");
				break;

			case 55:
				log.info("Binary Separating Plane Record");
				break;

			case 60:
				log.info("Replicate Record");
				break;

			case 61:
				log.info("Instance Reference Record");
				break;

			case 62:
				log.info("Instance Definition Record");
				break;

			case 63:
				log.info("External Reference Record");
				break;

			case 64:
				log.info("Texture Palette Record");
				final TexturePalette texPalette = TexturePalette.FACTORY.load(data);
				flt.addTexturePalette(texPalette);
				break;

			case 67:
				log.info("Vertex Palette Record");
				flt.addVertexPalette(VertexPalette.FACTORY.load(data, stream));
				break;

			case 68:
				log.info("Vertex with Color Record");
				break;

			case 69:
				log.info("Vertex with Color and Normal Record");
				break;

			case 70:
				log.info("Vertex with Color, Normal and UV Record");
				break;

			case 71:
				log.info("Vertex with Color and UV Record");
				break;

			case 72:
				log.info("Vertex List Record");
				if (flt.getLastFace() != null)
				{
					VertexList.FACTORY.load(data, flt.getVertexPalette(), flt.getLastFace(), orientation);
				}
				break;

			case 73:
				log.info("Level of Detail Record");
				break;

			case 74:
				log.info("Bounding Box Record");
				break;

			case 76:
				log.info("Rotate About Edge Record");
				break;

			case 78:
				log.info("Translate Record");
				break;

			case 79:
				log.info("Scale Record");
				break;

			case 80:
				log.info("Rotate About Point Record");
				break;

			case 81:
				log.info("Rotate and/or Scale to Point Record");
				break;

			case 82:
				log.info("Put Record");
				break;

			case 83:
				log.info("Eyepoint and Trackplane Palette Record");
				break;

			case 84:
				log.info("Mesh Record");
				break;

			case 85:
				log.info("Local Vertex Pool Record");
				break;

			case 86:
				log.info("Mesh Primitive Record");
				break;

			case 87:
				log.info("Road Segment Record");
				break;

			case 88:
				log.info("Road Zone Record");
				break;

			case 89:
				log.info("Morph Vertex List Record");
				break;

			case 90:
				log.info("Linkage Palette Record");
				break;

			case 91:
				log.info("Sound Record");
				break;

			case 92:
				log.info("Road Path Record");
				break;

			case 93:
				log.info("Sound Palette Record");
				break;

			case 94:
				log.info("General Matrix Record");
				break;

			case 95:
				log.info("Text Record");
				break;

			case 96:
				log.info("Switch Record");
				break;

			case 97:
				log.info("Line Style Palette Record");
				break;

			case 98:
				log.info("Clip Region Record");
				break;

			case 100:
				log.info("Extension Record");
				break;

			case 101:
				log.info("Light Source Record");
				break;

			case 102:
				log.info("Light Source Palette Record");
				break;

			case 103:
				log.info("103 Reserved");
				break;
			case 104:
				log.info("104 Reserved");
				break;
			case 105:
				log.info("Bounding Sphere Record");
				break;

			case 106:
				log.info("Bounding Cylinder Record");
				break;

			case 107:
				log.info("Bounding Convex Hull Record");
				break;

			case 108:
				log.info("Bounding Volume Center Record");
				break;

			case 109:
				log.info("Bounding Volume Orientation Record");
				break;

			case 110:
				log.info("110 Reserved");
				break;
			case 111:
				log.info("Light Point Record");
				break;

			case 112:
				log.info("Texture Mapping Palette Record");
				break;

			case 113:
				log.info("Material Palette Record");
				break;

			case 114:
				log.info("Name Table Record");
				break;

			case 115:
				log.info("CAT Record");
				break;

			case 116:
				log.info("CAT Data Record");
				break;

			case 117:
				log.info("117 Reserved");
				break;

			case 118:
				log.info("118 Reserved");
				break;

			case 119:
				log.info("Bounding Histogram Record");
				break;

			case 120:
				log.info("120 Reserved");
				break;

			case 121:
				log.info("121 Reserved");
				break;

			case 122:
				log.info("Push Attribute Record");
				break;

			case 123:
				log.info("Pop Attribute Record");
				break;

			case 124:
				log.info("124 Reserved");
				break;

			case 125:
				log.info("125 Reserved");
				break;

			case 126:
				log.info("Curve Record");
				break;

			case 127:
				log.info("Road Construction Record");
				break;

			case 128:
				log.info("Light Point Appearance Palette Record");
				break;

			case 129:
				log.info("Light Point Animation Palette Record");
				break;

			case 130:
				log.info("Indexed Light Point Record");
				break;

			case 131:
				log.info("Light Point System Record");
				break;

			case 132:
				log.info("Indexed String Record");
				break;

			case 133:
				log.info("Shader Palette Record");
				break;

			case 134:
				log.info("134 Reserved");
				break;
			case 135:
				log.info("Extended Material Header Record");
				break;

			case 136:
				log.info("Extended Material Ambient Record");
				break;

			case 137:
				log.info("Extended Material Diffuse Record");
				break;

			case 138:
				log.info("Extended Material Specular Record");
				break;

			case 139:
				log.info("Extended Material Emissive Record");
				break;

			case 140:
				log.info("Extended Material Alpha Record");
				break;

			case 141:
				log.info("Extended Material Light Map Record");
				break;

			case 142:
				log.info("Extended Material Normal Map Record");
				break;

			case 143:
				log.info("Extended Material Bump Map Record");
				break;

			case 144:
				log.info("144 Reserved");
				break;

			case 145:
				log.info("Extended Material Shadow Map Record");
				break;

			case 146:
				log.info("146 Reserved");
				break;

			case 147:
				log.info("Extended Material Reflection Map Record");
				break;

			default:
				log.warning("Unknown opcode: " + opCode);
				break;

		}

	}
}
