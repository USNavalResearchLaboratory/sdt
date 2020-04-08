package builder.mil.nrl.atest.worldwind.openflight;

import static builder.mil.nrl.atest.util.StringUtil.createEncodedString;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Represents the header record in an open flight file
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class Header
{
	public static final Logger log = Logger.getLogger(Header.class.getSimpleName());

	public static class HeaderFactory
	{

		private HeaderFactory()
		{
			// no op
		}


		public void load(byte[] data)
		{
			final int revisionLevel = BinUtil.intFromBytes(data, 12, 4);
			String lastRevision = createEncodedString(Arrays.copyOfRange(data, 20, 32));

			log.info("Revision Level: " + revisionLevel);
			log.info("Last Revision: " + lastRevision);
		}
	}

	public static final HeaderFactory FACTORY = new HeaderFactory();
}
