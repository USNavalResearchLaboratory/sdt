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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.text.CaseUtils;

import builder.mil.nrl.spg.io.FileUtil;

/**
 * @author will
 * @since Nov 4, 2013
 * 
 */
public class StringUtil
{
	public static final char COMMA = ',';

	public static final char SPACE = ' ';

	public static final int HELP_RECOMMENDED_LENGTH = 76;


	public static String normalizeNewlines(String str)
	{
		return str.replaceAll("\\r\\n?", "\n");
	}


	/**
	 * 
	 * @param strs - a list of strings
	 * @param conjunction - the conjunction word - usually "or" or "and"
	 * @return a formatted readable string in the form of string1, string2, string3 conjunction string4
	 */
	public static String conjunctiveString(List<String> strs, String conjunction)
	{
		if (strs == null)
		{
			return "";
		}
		int ct = strs.size();
		if (ct == 0)
		{
			return "";
		}
		else if (ct == 1)
		{
			return strs.get(0);
		}
		else
		{
			StringBuffer sb = new StringBuffer("");
			int index = 0;
			while (ct > index)
			{
				sb.append(strs.get(index++));
				if (index < (ct - 1))
				{
					sb.append(COMMA).append(SPACE);
				}
				else if (index < ct)
				{
					if (conjunction != null && !conjunction.isEmpty())
					{
						sb.append(' ').append(conjunction).append(' ');
					}
					else
					{
						sb.append(COMMA).append(SPACE);
					}
				}
			}
			return sb.toString();
		}
	}


	/**
	 * Removes trailing slashes from a string.
	 * 
	 * @param path The string to remove the trailing slashes from
	 * @return A string without trailing slashes.
	 */
	public static String removeTrailingSlash(String path)
	{
		String p = path;
		if (p != null)
		{
			// Also handle the case where the string ends with multiple slashes
			while (p.endsWith("/") || p.endsWith("\\"))
			{
				p = p.substring(0, p.length() - 1);
			}
		}
		return p;
	}


	public static String removeTrailingHashComment(String s)
	{
		if (s.charAt(0) == '#')
		{
			return "";
		}
		int i = -1;
		while (true)
		{
			i = s.indexOf("#", i + 1);
			if (i == -1)
			{
				return s;
			}
			int countOfSlashes = 0;
			while (s.charAt(i - countOfSlashes - 1) == '\\')
			{
				countOfSlashes++;
				if (i - countOfSlashes == 0)
				{
					break;
				}
			}
			if (countOfSlashes % 2 == 0)
			{
				return s.substring(0, i);
			}
		}
	}


	public static String createEncodedString(byte[] bytes)
	{
		return new String(bytes, Charset.forName(FileUtil.DEFAULT_ENCODING));
	}


	public static String wrapHelp(String help)
	{
		return wrap(help, HELP_RECOMMENDED_LENGTH);
	}


	public static String wrap(String text, int wrapLength)
	{
		// If we don't split on new lines like we're doing here,
		// then the new lines are included in the HELP_RECOMMENDED_LENGTH.
		// This is why we wrap individual lines and recombine.
		return Arrays.stream(text.split("\n"))
				.map(line -> WordUtils.wrap(line, wrapLength))
				.reduce((a, b) -> a + "\n" + b)
				.get();
	}


	/**
	 * Remove any non-alphanumeric characters from the given text
	 * 
	 * @param text
	 * @return a String with non-alphanumeric characters replaced by the space character
	 */
	public static String removeNonAlphaNumeric(String text)
	{
		return text.replaceAll("\\P{Alnum}", String.valueOf(SPACE));
	}


	/**
	 * Converts the given text with words delimited by the space character to camelCase
	 * 
	 * @param text
	 * @return a String with no spaces where the first word contains all lowercase characters and each subsequent word
	 *         in the text consists of a titlecase character and then a series of lowercase characters.
	 */
	public static String toCamelCase(String text)
	{
		return CaseUtils.toCamelCase(text, false, SPACE);
	}


	/**
	 * Removes any nonalphanumeric characters from the given text and converts to camelCase
	 * 
	 * @param text
	 * @return A String in camelCase with no spaces or non-alphanumeric characters
	 */
	public static String toAlphaNumericCamelCase(String text)
	{
		return toCamelCase(removeNonAlphaNumeric(text));
	}

}
