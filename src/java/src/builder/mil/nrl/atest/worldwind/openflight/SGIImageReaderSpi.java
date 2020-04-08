package builder.mil.nrl.atest.worldwind.openflight;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

/**
 * Class which defines the capabilities of the {@link SGIImageReader} and allows the {@link SGIImageReader} to hook into
 * the {@link IIORegistry}.
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class SGIImageReaderSpi extends ImageReaderSpi
{

	private static String[] formatNames = { "rgb", "RGB", "rgba", "RGBA", "sgi", "SGI", "bw", "BW", "int", "INT" };

	private static String[] entensions = { "rgb", "rgba", "sgi", "bw", "int" };

	private static String[] mimeType = { "image/sgi" };

	private boolean registered = false;


	public SGIImageReaderSpi()
	{
		super(
			"US Naval Research Laboratory",
			"1.0",
			formatNames,
			entensions,
			mimeType,
			"mil.nrl.atest.sgi.SGIImageReader",
			new Class<?>[] { ImageInputStream.class },
			null,
			false,
			null,
			null,
			null,
			null,
			false,
			"javax_imageio_sgi_1.0",
			null,
			null,
			null);
	}


	@Override
	public boolean canDecodeInput(Object source) throws IOException
	{
		if (!(source instanceof ImageInputStream))
		{
			return false;
		}

		ImageInputStream stream = (ImageInputStream) source;
		byte[] b = new byte[2];
		stream.mark();
		stream.readFully(b);
		stream.reset();

		return ((b[0] & 0xFF) == 0x01) && ((b[1] & 0xFF) == 0xDA);
	}


	@Override
	public ImageReader createReaderInstance(Object extension) throws IOException
	{
		return new SGIImageReader(this);
	}


	@Override
	public String getDescription(Locale locale)
	{
		return "RGB and RGBA Image Reader";
	}


	@Override
	public void onRegistration(ServiceRegistry registry, Class<?> category)
	{
		if (registered)
		{
			return;
		}
		registered = true;
	}

}
