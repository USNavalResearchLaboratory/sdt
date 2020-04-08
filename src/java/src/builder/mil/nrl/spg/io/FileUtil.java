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

package builder.mil.nrl.spg.io;

import static java.lang.Character.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.universalchardet.UniversalDetector;
import static builder.mil.nrl.spg.logging.LoggerExt.warning;

import builder.mil.nrl.atest.util.DeveloperError;
import builder.mil.nrl.atest.util.NullArgumentException;
import builder.mil.nrl.spg.logging.LoggerExt;
import builder.mil.nrl.spg.util.NoNullList;

/**
 * simple utility class for working with local files
 *
 * @author bowen
 * @since Feb 25, 2009
 *
 */
public class FileUtil
{
	private static final Logger logger = LoggerExt.getLogger();

	/**
	 * Characters that are invalid in filenames for one of the major operating systems. This list is equivalent to the
	 * list of invalid characters for windows files--windows is the most restrictive.
	 */
	public final static List<Character> ALL_OS_INVALID_FILENAME_CHARACTERS = Collections
			.unmodifiableList(Arrays.asList(valueOf('\\'),
				valueOf('/'),
				valueOf(':'),
				valueOf('*'),
				valueOf('?'),
				valueOf('"'),
				valueOf('<'),
				valueOf('>'),
				valueOf('|')));

	public final static String DEFAULT_ENCODING = "UTF-8";

	public final static String DEFAULT_WINDOWS_ENCODING = "WINDOWS-1252";

	private static final Random RANDOM = new Random();


	private FileUtil()
	{
		// no instance methods in this class
	}


	/**
	 * Tries to detect the encoding of the given file, but defaults to UTF-8 if the encoding can't be determined. UTF-8
	 * is what Java assumes anyway, unless explicitly told otherwise, so its the best assumption unless more information
	 * is known.
	 * 
	 * @param f file to test
	 * @return string representation of the character set
	 * @throws IOException
	 */
	public static String detectEncoding(File f) throws IOException
	{
		return detectEncoding(f, DEFAULT_ENCODING);
	}


	public static String detectEncoding(File f, String defaultEncoding) throws IOException
	{
		byte[] buf = new byte[4096];
		String encoding = defaultEncoding;
		try (FileInputStream fis = new FileInputStream(f))
		{
			UniversalDetector detector = new UniversalDetector(null);
			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone())
			{
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();

			encoding = detector.getDetectedCharset();
			if (encoding == null)
			{
				encoding = defaultEncoding;
			}
			detector.reset();
		}
		return encoding;
	}


	/**
	 * find files recursively
	 * 
	 * @param directory
	 * @param filter
	 * @param recurse
	 * @return
	 */
	public static Collection<File> findFiles(final File directory, final FilenameFilter filter, final boolean recurse)
	{
		final Set<File> files = new TreeSet<>();
		for (final File entry : listFiles(directory))
		{
			if (entry.isFile())
			{
				if (filter == null || filter.accept(directory, entry.getName()))
				{
					files.add(entry);
				}
			}
			else if (recurse && entry.isDirectory())
			{
				files.addAll(findFiles(entry, filter, recurse));
			}
		}
		return files;
	}


	/**
	 * changes the path of the file, using the specified extension
	 * 
	 * @param file
	 * @param ext
	 * @return
	 */
	public static File changeFileExtension(final File file, final String ext)
	{
		String path = file.getPath();
		final int index = path.lastIndexOf('.');
		if (index > 0)
		{
			path = path.substring(0, index) + "." + ext;
		}
		return new File(path);
	}


	public static File createTemporaryFolder(String prefix)
	{
		File tempFolder = null;
		try
		{
			tempFolder = File.createTempFile(prefix, "");
			if (!tempFolder.delete())
			{
				logger.warning("Could not delete " + tempFolder);
			}
			if (!tempFolder.mkdir())
			{
				logger.warning("Could not make directory " + tempFolder);
			}
		}
		catch (IOException e)
		{
			logger.warning(LoggerExt.buildStackTraceMessage("Failed to create temporary folder", e));
		}
		return tempFolder;
	}


	/**
	 * Creates a file with a unique name by testing for the existence of the proposed name and adding a (count) at the
	 * end (like firefox does).
	 * 
	 * @param toDir
	 * @param leafFileName
	 * @param extension
	 * @return
	 * @throws IOException
	 */
	public static File ensureUniqueFile(final File toDir, final String leafFileName, final String extension)
	{
		// attempt to get absolute file name
		File dest = new File(toDir, leafFileName + extension);
		try
		{
			dest = dest.getCanonicalFile();
		}
		catch (Exception e)
		{
			// ignore, but probably not good news
			logger.log(Level.WARNING, "Unable to get canonical file name from [" + dest + "].", e);
		}

		int count = 1;
		while (dest.exists())
		{
			dest = new File(toDir, leafFileName + "-" + count + extension);
			count++;
		}
		return dest;
	}


	/**
	 * Gets the base name of the file, that is to say the part of the file name excluding the extension
	 * 
	 * For example:
	 * <table border="1">
	 * <tr>
	 * <td>Input:</td>
	 * <td>Output:</td>
	 * </tr>
	 * <tr>
	 * <td>test.txt</td>
	 * <td>test</td>
	 * </tr>
	 * <tr>
	 * <td>test.in.txt</td>
	 * <td>test.in</td>
	 * </tr>
	 * <tr>
	 * <td>test</td>
	 * <td>test</td>
	 * </tr>
	 * <tr>
	 * <td>.test</td>
	 * <td>.test</td>
	 * </tr>
	 * <tr>
	 * <td>null</td>
	 * <td>null</td>
	 * </tr>
	 * </table>
	 * 
	 * @param file
	 * @return
	 */
	public static String getBaseName(File file)
	{
		if (file == null)
		{
			return null;
		}

		int dotIndex = file.getName().lastIndexOf('.');

		if (dotIndex <= 0)
		{
			return file.getName();
		}
		return file.getName().substring(0, dotIndex);
	}


	/**
	 * Returns a file with a unique name. Unlike File#createTempFile this method will not actually
	 * make the file.
	 * 
	 * @param directory the parent directory for the file
	 * @param prefix the string to append to the start of the file name
	 * @param suffix the string to append to the end of the file name
	 * @return a file with a unique name in the given directory
	 */
	public static File getTempFile(File directory, String prefix, String suffix)
	{
		final Random random = new Random();
		long next = random.nextLong();
		if (next < 0)
		{
			next *= -1;
		}

		return FileUtil.ensureUniqueFile(directory, prefix + next, suffix);
	}


	/**
	 * saves the URI to the specified directory, generating a new random file as necessary
	 * 
	 * @param src
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static File saveToAndEnsureUniqueFile(final URI src, final File dir) throws IOException
	{
		if ("file".equalsIgnoreCase(src.getScheme()))
		{
			final File in = new File(src);
			final File out = new File(dir, in.getName());
			return copyAndEnsureUniqueFile(in, out);
		}
		final File tmp = File.createTempFile("file-unique-copy-", ".data");
		tmp.deleteOnExit();
		try
		{
			FileUtils.copyURLToFile(src.toURL(), tmp);
			final long crc = FileUtils.checksumCRC32(tmp);
			// src.getPath() can sometimes return null
			// (e.g., when
			// jar:file:/C:/git/atest/ATESTBase/Core/lib/xbean.jar!/org/apache/xmlbeans/message.properties)
			String ext = FilenameUtils.getExtension(src.getPath());
			ext = ext == null ? "" : ext;
			final String name = Long.toHexString(Math.abs(crc)).toLowerCase() + (ext.isEmpty() ? "" : "." + ext);
			final File out = new File(dir, name);
			return copyAndEnsureUniqueFile(tmp, out);
		}
		finally
		{
			FileUtils.deleteQuietly(tmp);
		}
	}


	/**
	 * copies the file from source to destination path, ensuring that if the file and destination are the same (but the
	 * contents are not equal) the method will modify the destination file name to ensure we do not needlessly overwrite
	 * over the target file contents
	 * 
	 * @param src
	 * @param dest
	 * @return
	 * @throws IOException
	 */
	public static File copyAndEnsureUniqueFile(final File src, final File dest) throws IOException
	{
		File target = dest;
		if (dest.exists())
		{
			if (FileUtils.contentEquals(src, dest))
			{
				// these are the exact same file - do not needlessly copy
				return dest;
			}
			target = FileUtil.ensureUniqueFile(target);
		}
		FileUtils.copyFile(src, target);
		return target;
	}


	public static File moveAndEnsureUniqueFile(final File src, final File dest) throws IOException
	{
		File target = dest;
		if (dest.exists())
		{
			if (FileUtils.contentEquals(src, dest))
			{
				// these are the exact same file - do not needlessly copy
				return dest;
			}
			target = FileUtil.ensureUniqueFile(target);
		}
		FileUtils.moveFile(src, target);
		return target;
	}


	/**
	 * see {@link #ensureUniqueFile(File, String, String)}
	 */
	public static File ensureUniqueFile(final File target)
	{
		final String pref = FilenameUtils.getBaseName(target.getName());
		final String ext = FilenameUtils.getExtension(target.getName());
		return ensureUniqueFile(target.getParentFile(), pref, ext == null || ext.isEmpty() ? "" : "." + ext);
	}


	/**
	 * determines if the specified file should be treated as a temporary file
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isTemporaryFile(final File path)
	{
		final File tempDir = FileUtils.getTempDirectory();
		try
		{
			if (path.getParentFile() != null && path.getParentFile().getCanonicalFile().compareTo(tempDir.getCanonicalFile()) == 0)
			{
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			return false;
		}
	}


	public static boolean isValidFilenameOnAnyOS(String str)
	{
		if (str == null || str.isEmpty())
		{
			return false;
		}
		for (Character invalid : ALL_OS_INVALID_FILENAME_CHARACTERS)
		{
			if (str.contains(invalid.toString()))
			{
				return false;
			}
		}
		return true;
	}


	public static String sanitizeFilename(String str)
	{
		return sanitizeFilenameWithReplacement(str, '_');
	}


	public static String sanitizeFilenameNoSpaces(String str)
	{
		List<Character> allInvalids = new ArrayList<>();
		allInvalids.addAll(ALL_OS_INVALID_FILENAME_CHARACTERS);
		allInvalids.add(valueOf(' '));
		return doSanitize(str, allInvalids, '_');
	}


	public static String sanitizeFilenameWithReplacement(String str, char replacement)
	{
		List<Character> allInvalids = ALL_OS_INVALID_FILENAME_CHARACTERS;
		return doSanitize(str, allInvalids, replacement);
	}


	private static String doSanitize(String str, List<Character> allInvalids, char replacement)
	{
		// Unfortunately I can't directly reference WindowsOperatingSystem from here because when running the
		// unit tests on non-windows operating systems you get an UnsatisfiedLinkError thanks to
		// kernel32 being loaded statically.

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++)
		{
			if (allInvalids.contains(valueOf(str.charAt(i))))
			{
				builder.append(replacement);
			}
			else
			{
				builder.append(str.charAt(i));
			}
		}
		return builder.toString();
	}


	public static void createNewFile(File file) throws IOException
	{
		if (!file.createNewFile())
		{
			logger.warning("Could not create " + file + "; it already exists");
		}
	}


	public static void delete(File file)
	{
		if (!file.delete())
		{
			logger.warning("Could not delete " + file);
		}
	}


	public static void mkdir(File file)
	{
		if (!file.exists() && !file.mkdir())
		{
			logger.warning("Could not make directory " + file);
		}
	}


	public static void mkdirs(File file)
	{
		if (!file.exists() && !file.mkdirs())
		{
			logger.warning("Could not make the directories in " + file);
		}
	}


	public static void renameTo(File file, File dest)
	{
		if (!file.renameTo(dest))
		{
			logger.warning("Could not rename " + file + " to " + dest);
		}
	}


	public static InputStreamReader createEncodedInputStreamReader(InputStream in) throws IOException
	{
		return new InputStreamReader(in, DEFAULT_ENCODING);
	}


	public static InputStreamReader createEncodedInputStreamReader(File file) throws IOException
	{
		return createEncodedInputStreamReader(new FileInputStream(file));
	}


	public static OutputStreamWriter createEncodedOutputStreamWriter(OutputStream out) throws IOException
	{
		return new OutputStreamWriter(out, DEFAULT_ENCODING);
	}


	public static OutputStreamWriter createEncodedOutputStreamWriter(File file) throws IOException
	{
		return createEncodedOutputStreamWriter(new FileOutputStream(file));
	}


	public static FilenameFilter createSuffixFilenameFilter(String suffix0, String... suffixes)
	{
		if (suffix0 == null)
		{
			throw new NullArgumentException(String.class, "suffix0");
		}
		final List<String> validSuffixes = new NoNullList<>(new ArrayList<String>(), true);
		validSuffixes.add(suffix0);
		if (suffixes != null)
		{
			validSuffixes.addAll(Arrays.asList(suffixes));
		}
		return new FilenameFilter()
			{
				@Override
				public boolean accept(final File dir, final String name)
				{
					for (String validSuffix : validSuffixes)
					{
						if (name.toLowerCase().endsWith(validSuffix.toLowerCase()))
						{
							return true;
						}
					}
					return false;
				}
			};
	}


	private static void appendExistingZipEntry(ZipEntry e, ZipFile zip, ZipOutputStream append) throws IOException
	{
		append.putNextEntry(e);
		if (!e.isDirectory())
		{
			IOUtils.copy(zip.getInputStream(e), append);
		}
		append.closeEntry();
	}


	private static void appendNewZipEntry(String entryName, File file, ZipOutputStream append) throws IOException
	{
		FileInputStream in = null;
		try
		{
			in = new FileInputStream(file);
			ZipEntry e = new ZipEntry(entryName);
			append.putNextEntry(e);
			IOUtils.copy(in, append);
			append.closeEntry();
		}
		finally
		{
			IOUtils.closeQuietly(in);
		}
	}


	private static void appendFilesToTempZip(File zipFile, OutputStream tempStream, Map<String, File> entryNameToFileMap) throws IOException
	{
		ZipFile zip = null;
		ZipOutputStream append = null;
		try
		{
			zip = new ZipFile(zipFile);
			append = new ZipOutputStream(tempStream);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements())
			{
				appendExistingZipEntry(entries.nextElement(), zip, append);
			}
			for (Map.Entry<String, File> entryNameToFile : entryNameToFileMap.entrySet())
			{
				appendNewZipEntry(entryNameToFile.getKey(), entryNameToFile.getValue(), append);
			}
		}
		finally
		{
			IOUtils.closeQuietly(append);
			if (zip != null)
			{
				try
				{
					zip.close();
				}
				catch (IOException e)
				{
					// close quietly
				}
			}
		}
	}


	/**
	 * NOTE: This is not dead code. This code is used in builder/pom.xml.
	 */
	public static void appendFilesToZip(File zipFile, Map<String, File> entryNameToFileMap) throws IOException
	{
		File tempFile = new File(FileUtils.getTempDirectory(), Long.toHexString(RANDOM.nextLong()));
		FileOutputStream tempStream = null;
		boolean success = false;
		try
		{
			tempStream = new FileOutputStream(tempFile);
			appendFilesToTempZip(zipFile, tempStream, entryNameToFileMap);
			success = true;
		}
		finally
		{
			IOUtils.closeQuietly(tempStream);
		}
		if (success)
		{
			FileUtils.copyFile(tempFile, zipFile);
			FileUtils.deleteQuietly(tempFile);
		}
	}


	public static List<File> listFiles(File dir)
	{
		if (!dir.isDirectory())
		{
			throw new DeveloperError("Expected a directory, not a file: " + dir);
		}
		File[] files = dir.listFiles();
		if (files == null)
		{
			warning("There was probably an I/O error when listing the files in " + dir);
			return emptyList();
		}
		return asList(files);
	}


	public static List<File> listFiles(File dir, FilenameFilter filter)
	{
		if (!dir.isDirectory())
		{
			throw new DeveloperError("Expected a directory, not a file: " + dir);
		}
		File[] files = dir.listFiles(filter);
		if (files == null)
		{
			warning("There was probably an I/O error when listing the files in " + dir);
			return emptyList();
		}
		return asList(files);
	}


	public static List<String> list(File dir)
	{
		if (!dir.isDirectory())
		{
			throw new DeveloperError("Expected a directory, not a file: " + dir);
		}
		String[] files = dir.list();
		if (files == null)
		{
			warning("There was probably an I/O error when listing the files in " + dir);
			return emptyList();
		}
		return asList(files);
	}


	public static List<String> list(File dir, FilenameFilter filter)
	{
		if (!dir.isDirectory())
		{
			throw new DeveloperError("Expected a directory, not a file: " + dir);
		}
		String[] files = dir.list(filter);
		if (files == null)
		{
			warning("There was probably an I/O error when listing the files in " + dir);
			return emptyList();
		}
		return asList(files);
	}


	/**
	 * Reads the contents of the file into a String and constructs a secure one-way hash using the MD5 algorithm
	 * 
	 * @param f the file to read using UTF-8 encoding
	 * @return String representing the one-way hash constructed for the file
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String md5DigestForFile(File f) throws NoSuchAlgorithmException, IOException
	{
		return md5Digest(readFileToString(f, DEFAULT_ENCODING));
	}


	/**
	 * Computes a secure one-way hash using the MD5 algorithm
	 * 
	 * @param text the text to be hashed
	 * @return the String resulting from the hash computation
	 * @throws NoSuchAlgorithmException
	 */
	public static String md5Digest(String text) throws NoSuchAlgorithmException
	{
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		algorithm.update(text.getBytes(Charset.forName("UTF-8")));

		byte[] digest = algorithm.digest();
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < digest.length; i++)
		{
			String hex = Integer.toHexString(0xFF & digest[i]);
			if (hex.length() == 1)
			{
				hexString.append("0").append(hex);
			}
			else
			{
				hexString.append(hex);
			}
		}
		return hexString.toString();
	}


	/**
	 * Reads the contents of the file into a String and constructs a secure one-way hash using the MD5 algorithm
	 * 
	 * @param file the file to read using UTF-8 encoding
	 * @return String representing the one-way hash constructed for the file or an empty String if an error occurs
	 */
	public static String md5HashForFile(File file)
	{
		try
		{
			return md5DigestForFile(file);
		}
		catch (NoSuchAlgorithmException | IOException e)
		{
			warning("An error occurred while creating an md5 hash for the file " + file + ": " + e.getMessage());
			return "";
		}
	}
}
