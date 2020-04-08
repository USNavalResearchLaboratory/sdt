/*
 *  =======================================================================
 *  ==                                                                   ==
 *  ==                   Classification: UNCLASSIFIED                    ==
 *  ==                   Classified By:                                  ==
 *  ==                   Declassify On:                                  ==
 *  ==                                                                   ==
 *  =======================================================================
 *
 *  Developed by:
 *
 *  Naval Research Laboratory
 *  Tactical Electronic Warfare Division
 *  Electronic Warfare Modeling and Simulation Branch
 *  Advanced Tactical Environmental Simulation Team (Code 5774)
 *  4555 Overlook Ave SW
 *  Washington, DC 20375
 *
 *  For more information call (202)767-2897 or send email to
 *  BuilderSupport@nrl.navy.mil.
 *
 *  The U.S. Government retains all rights to use, duplicate,
 *  distribute, disclose, or release this software.
 */

package builder.mil.nrl.atest.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author will
 * @since Mar 29, 2012
 * 
 */
public class TwosComplimentInputStream extends InputStreamWrapper
{
	public TwosComplimentInputStream(InputStream stream)
	{
		super(stream);
	}


	@Override
	public int read() throws IOException
	{
		int orig = original.read();
		if (orig == -1)
		{
			return orig;
		}
		return 0x000000ff & ~(orig - 0x01);

	}
}
