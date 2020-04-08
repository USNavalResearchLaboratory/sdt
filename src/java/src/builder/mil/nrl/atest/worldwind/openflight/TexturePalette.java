package builder.mil.nrl.atest.worldwind.openflight;

import static builder.mil.nrl.atest.util.StringUtil.createEncodedString;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * The {@link TexturePalette} record stores information about all of the textures used in the model
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class TexturePalette
{
	private final static Logger log = Logger.getLogger(TexturePalette.class.getSimpleName());

	public static class TexturePaletteFactory
	{

		private TexturePaletteFactory()
		{
			// no op
		}


		public TexturePalette load(byte[] data)
		{
			final String fileName = createEncodedString(Arrays.copyOfRange(data, 4, findFileNameEnd(data)));
			final int index = BinUtil.intFromBytes(data, 204, 4);
			// final int x = BinUtil.intFromBytes(data, 208, 4);
			// final int y = BinUtil.intFromBytes(data, 212, 4);

			log.info("Texture File: " + fileName);
			log.info("index: " + index);

			return new TexturePalette(fileName, index);
		}


		private int findFileNameEnd(byte[] data)
		{
			for (int i = 4; i < 204; i++)
			{
				if (data[i] == 0x00)
				{
					return i;
				}
			}

			return 204;
		}
	}

	public static final TexturePaletteFactory FACTORY = new TexturePaletteFactory();

	private final String fileName;

	private final int index;


	public TexturePalette(String fileName, int index)
	{
		this.fileName = fileName;
		this.index = index;
	}


	public String getFileName()
	{
		return fileName;
	}


	public int getIndex()
	{
		return index;
	}
}
