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

package builder.mil.nrl.atest.util;

/**
 * A <code>DeveloperError</code> is a subclass of <code>Error</code> that indicates serious problems that Builder should
 * not try to catch. Most such errors are abnormal conditions.
 * <p>
 * This is a convenience class. Now one does not have to copy and paste the message
 * "Call a developer! This should never happen." everywhere.
 * </p>
 * 
 * @author mamaril
 * @since Nov 29, 2010
 * @see Error
 */
public class DeveloperError extends Error
{
	private static final long serialVersionUID = 1L;

	private static final String ERROR_MESSAGE = "Call a developer! This should never happen.";


	/**
	 * See {@link Error#Error()}.
	 */
	public DeveloperError()
	{
		super(ERROR_MESSAGE);
	}


	/**
	 * See {@link Error#Error(Throwable)}.
	 */
	public DeveloperError(Throwable cause)
	{
		super(ERROR_MESSAGE, cause);
	}


	/**
	 * See {@link Error#Error(String)}.
	 */
	public DeveloperError(String msg)
	{
		super(msg);
	}
}
