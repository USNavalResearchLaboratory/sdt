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
 * Delegates all methods to the wrapped stream. Helpful class to extend if you want to wrap a stream to do something,
 * since you only have to override the read method.
 * 
 * @author will
 * @since Mar 29, 2012
 * 
 */
public abstract class InputStreamWrapper extends InputStream
{
	protected final InputStream original;


	public InputStreamWrapper(InputStream original)
	{
		if (original == null)
		{
			throw new IllegalArgumentException("Stream may not be null");
		}
		this.original = original;
	}


	@Override
	public int available() throws IOException
	{
		return original.available();
	}


	@Override
	public void close() throws IOException
	{
		original.close();
	}


	@Override
	public boolean markSupported()
	{
		return original.markSupported();
	}


	@Override
	public synchronized void reset() throws IOException
	{
		original.reset();
	}


	@Override
	public synchronized void mark(int readlimit)
	{
		original.mark(readlimit);
	}
}
