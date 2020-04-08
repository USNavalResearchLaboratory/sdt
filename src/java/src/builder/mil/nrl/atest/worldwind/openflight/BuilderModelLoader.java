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

package builder.mil.nrl.atest.worldwind.openflight;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import builder.mil.nrl.atest.icon.Orientation;
import builder.mil.nrl.atest.io.TwosComplimentInputStream;

/**
 * @author will
 * @since Mar 28, 2012
 *
 */
public class BuilderModelLoader extends OpenFlightLoader
{
	public static final String EXTENSION = "3db";


	public BuilderModelLoader(Orientation orientation)
	{
		super(orientation);
	}


	@Override
	protected InputStream openSource(String source) throws IOException
	{
		return new TwosComplimentInputStream(new URL(source).openStream());
	}
}
