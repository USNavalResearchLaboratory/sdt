package builder.mil.nrl.atest.worldwind.openflight;

import static builder.mil.nrl.atest.util.StringUtil.createEncodedString;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Represents a Comment record in an OpenFlight file
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class Comment
{
	private static final Logger log = Logger.getLogger(Comment.class.getSimpleName());

	public static class CommentFactory
	{

		private CommentFactory()
		{
			// no op
		}


		public void load(byte[] data)
		{
			String comment = createEncodedString(Arrays.copyOfRange(data, 4, data.length));
			log.info(comment);
		}
	}

	public static final CommentFactory FACTORY = new CommentFactory();
}
