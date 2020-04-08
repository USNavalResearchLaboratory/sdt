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
 * Thrown to indicate that a method has been passed an illegal, inappropriate null argument.
 * <p>
 * This class standardizes the way illegal-null-argument exceptions are thrown and prevents boilerplate code like the
 * following:
 * </p>
 * 
 * <pre>
 * public void setArg(String arg)
 * {
 *   if (arg == null)
 *   {
 *     throw new IllegalArgumentException(String.class.getSimpleName() + " cannot be null");
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Instead, one can write:
 * </p>
 * 
 * <pre>
 * public void setArg(String arg)
 * {
 *   throw new NullArgumentException(String.class);
 * }
 * </pre>
 * 
 * @author mamaril
 * @since Jun 10, 2010
 * @see IllegalArgumentException
 */
public class NullArgumentException extends IllegalArgumentException
{
	private static final long serialVersionUID = 1L;


	/**
	 * Constructs a <code>NullArgumentException</code> with the specified <code>nullArgClass</code> in the detail
	 * message. For example, if <code>String</code> was sent in as an argument, the following would be the detail
	 * message: String cannot be null.
	 * 
	 * @param nullArgClass the class of the argument that cannot be <code>null</code>
	 * @throws NullPointerException if <code>nullArgClass</code> is <code>null</code>
	 */
	public NullArgumentException(Class<?> nullArgClass)
	{
		super(nullArgClass.getSimpleName() + " cannot be null");
	}


	/**
	 * Constructs a <code>NullArgumentException</code> with the specified arguments in the detail message. For example,
	 * if <code>String</code> and <code>"str"</code> were sent in as arguments, the following would be the detail
	 * message: String str cannot be null.
	 * <p>
	 * Even though <code>nullArgClass</code> cannot be <code>null</code>, <code>nullArgName</code> <strong>can</strong>
	 * be <code>null</code>, but the resulting detail message would not make any sense.
	 * </p>
	 * 
	 * @param nullArgClass the class of the argument that cannot be <code>null</code>
	 * @param nullArgName the name of the argument that cannot be <code>null</code>
	 * @throws NullPointerException if <code>nullArgClass</code> is <code>null</code>
	 */
	public NullArgumentException(Class<?> nullArgClass, String nullArgName)
	{
		super(nullArgClass.getSimpleName() + " " + nullArgName + " cannot be null");
	}
}
