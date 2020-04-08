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

package builder.mil.nrl.atest.icon;

/**
 * Used to determine how the model is facing Y means that the model is pointing down the Y axis, X means the model is
 * pointing down the X axis.
 * 
 * @author doyle
 * @since May 10, 2012
 */
public enum Orientation {
		Y_AXIS
		{
			@Override
			public double getX(double x, double y)
			{
				return x;
			}


			@Override
			public double getY(double x, double y)
			{
				return y;
			}
		},
		X_AXIS
		{
			@Override
			public double getX(double x, double y)
			{
				return y;
			}


			@Override
			public double getY(double x, double y)
			{
				return x;
			}
		};

	public static Orientation parse(String orientationStr)
	{
		if (orientationStr.trim().toLowerCase().equals("x"))
		{
			return X_AXIS;
		}
		return Y_AXIS;
	}


	public abstract double getX(double x, double y);


	public abstract double getY(double x, double y);
}
