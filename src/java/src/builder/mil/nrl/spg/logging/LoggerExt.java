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
package builder.mil.nrl.spg.logging;

import static java.text.MessageFormat.format;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Extension to {@link java.util.logging.Logger}.
 * 
 * @author mamaril
 * @since Jun 29, 2009
 */
public class LoggerExt
{
	private static Function<String, Logger> logSupplier = javaUtilLogSupplier();


	static void changeLogSupplierForTestingOnly(Function<String, Logger> mockedSupplier)
	{
		logSupplier = mockedSupplier;
	}


	static void restoreDefaultLogSupplier()
	{
		logSupplier = javaUtilLogSupplier();
	}


	private static Function<String, Logger> javaUtilLogSupplier()
	{
		return s -> Logger.getLogger(s);
	}


	private static synchronized StackTraceElement getClientElement()
	{
		return getClientElement(Thread.currentThread().getStackTrace());
	}


	/**
	 * Call this from Thread.getStackTrace
	 */
	private static synchronized StackTraceElement getClientElement(StackTraceElement[] elements)
	{
		// Stack trace index:
		// elements[0] = Thread.getStackTrace
		// elements[1] = LoggerExt.getLoggerAtStackDepth
		// elements[1] = <internal calling method, e.g. LoggerExt.warningWithStacktrace>
		// elements[2] = calling method
		// Find first element whose class is not LoggerExt. That'll be the calling
		// class whose log settings we want to use
		for (int i = 0; i < elements.length; i++)
		{
			String eleClass = elements[i].getClassName();
			if (!eleClass.equals(LoggerExt.class.getName())
				&& !eleClass.equals(Thread.class.getName()))
			{
				return elements[i];
			}
		}
		// Apparently all the elements in the stack are from LoggerExt, so return
		// a logger for the last element
		return elements[elements.length - 1];
	}


	private static synchronized Logger getClientLogger()
	{
		return logSupplier.apply(getClientElement().getClassName());
	}


	/**
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 */
	private static synchronized void log(Level level, String msg, Optional<Throwable> thrown)
	{
		log(level, msg, thrown, getClientElement());
	}


	private static synchronized void log(Level level, String msg, Optional<Throwable> thrown, StackTraceElement client)
	{
		Logger logger = logSupplier.apply(client.getClassName());
		logger.log(record(level, msg, thrown, Optional.of(client)));
	}


	private static synchronized LogRecord record(Level level, String msg, Optional<Throwable> thrown, Optional<StackTraceElement> client)
	{
		LogRecord record = new LogRecord(level, msg);
		client.ifPresent(e -> record.setSourceClassName(e.getClassName()));
		client.ifPresent(e -> record.setSourceMethodName(e.getMethodName()));
		thrown.ifPresent(t -> record.setThrown(t));
		return record;
	}


	public static synchronized Logger getLogger()
	{
		return getClientLogger();
	}


	public static synchronized Logger getLogger(final Class<?> clazz)
	{
		return logSupplier.apply(clazz.getName());
	}


	public static synchronized String getCurrentMethodName()
	{
		return getClientElement().getMethodName();
	}


	public static synchronized String getCurrentClassMethodName()
	{
		return getClientElement().getMethodName();
	}


	/**
	 * Logs a warning if the specified collection of <code>listeners</code> is nonempty. Use this method before call
	 * {@code listeners.clear()}. Clearing a nonempty listener list is an indicator that an object is not cleaning up.
	 * If a programmer was lazy enough to not clean up his listeners, who knows what else he may have forgotten to clean
	 * up.
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 * 
	 * @param listeners list of listeners to warn about if the list is nonempty
	 */
	public static synchronized void logClearingNonemptyListenerList(final Collection<? extends Object> listeners)
	{
		if (listeners == null || listeners.isEmpty())
		{
			return;
		}
		final StackTraceElement clientEle = getClientElement();
		final Logger log = logSupplier.apply(clientEle.getClassName());
		log.log(Level.WARNING, format( //
			"{0} is clearing a listener list with {1} element(s). " //
				+ "This is an indicator that an object is not cleaning up its listeners correctly, " //
				+ "which may or may not have lingering ramifications. " //
				+ "Call a remove-listener method where applicable.", //
			clientEle.getClassName() + "#" + clientEle.getMethodName(), // {0}
			Integer.valueOf(listeners.size()))); // {1}
	}


	/**
	 * Logs the given message at the warning level, if the logging level is currently set to fine the throwable will be
	 * logged as well.
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 * 
	 * @param warningMsg
	 * @param t
	 */
	public static synchronized void warningWithFineThrowable(String warningMsg, Throwable t)
	{
		StackTraceElement client = getClientElement();
		Logger logger = logSupplier.apply(client.getClassName());
		if (logger.isLoggable(Level.FINE))
		{
			logger.log(record(Level.FINE, warningMsg, Optional.of(t), Optional.of(client)));
		}
		else if (logger.isLoggable(Level.WARNING))
		{
			logger.log(record(Level.WARNING,
				warningMsg + " (Set the logging level to FINE for exception details.)",
				Optional.empty(), Optional.of(client)));
		}
	}


	public static synchronized void warningWithStacktrace(Throwable t)
	{
		log(Level.WARNING, t.getMessage(), Optional.of(t));
	}


	public static synchronized void warningWithFineThrowable(Throwable t)
	{
		StackTraceElement client = getClientElement();
		final Logger logger = logSupplier.apply(client.getClassName());

		if (logger.isLoggable(Level.FINE))
		{
			log(Level.FINE, t.getMessage(), Optional.of(t));
		}
		else if (logger.isLoggable(Level.WARNING))
		{
			log(Level.WARNING, t.getMessage() + " (Set the logging level to FINE for exception details.)", Optional.of(t));
		}
	}


	/**
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 */
	public static synchronized void severe(String severeMsg)
	{
		log(Level.SEVERE, severeMsg, Optional.empty());
	}


	/**
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 */
	public static synchronized void severe(String severeMsg, Throwable thrown)
	{
		log(Level.SEVERE, severeMsg, Optional.ofNullable(thrown));
	}


	/**
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 */
	public static synchronized void warning(String warningMsg)
	{
		log(Level.WARNING, warningMsg, Optional.empty());
	}


	/**
	 * <p>
	 * <strong>Performance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 */
	public static synchronized void info(String infoMsg)
	{
		log(Level.INFO, infoMsg, Optional.empty());
	}


	/**
	 * <p>
	 * <strong>Perfomance Consideration:</strong> This method should only be used in code that is not called often, in
	 * exceptional cases. The logger to use is determined by creating a stack trace, which is an expensive operation.
	 * </p>
	 */
	public static synchronized void fine(String fineMsg)
	{
		log(Level.FINE, fineMsg, Optional.empty());
	}


	private static String buildStackTraceOptionalMessage(Optional<String> prefix, Throwable t)
	{
		StringWriter sw = new StringWriter();
		prefix.ifPresent(p -> sw.append(String.format("%s%n", prefix)));
		sw.append(String.format("%s: %s%n", t.getClass().getName(), t.getMessage()));
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}


	public static String buildStackTraceMessage(Throwable t)
	{
		return buildStackTraceOptionalMessage(Optional.empty(), t);
	}


	public static String buildStackTraceMessage(String prefix, Throwable t)
	{
		return buildStackTraceOptionalMessage(Optional.of(prefix), t);
	}

}
