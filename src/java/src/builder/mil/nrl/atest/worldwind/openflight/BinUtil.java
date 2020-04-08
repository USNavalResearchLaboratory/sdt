package builder.mil.nrl.atest.worldwind.openflight;

/**
 * Contains utility methods for reading binary data
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class BinUtil
{

	/**
	 * Converts a byte array into a double.
	 * 
	 * @param data the byte array containing the data
	 * @param start the first index in data that the double is located in
	 * @param length the number of bytes that the double is contained in. This method will treat bytes start to start +
	 *        length - 1 as the data for the byte
	 * @return a double which is parsed from the data
	 */
	public static double doubleFromBytes(byte[] data, int start, int length)
	{
		double num = Double.longBitsToDouble(longFromBytes(data, start, length));

		return num;
	}


	/**
	 * Converts a byte array into a float.
	 * 
	 * @param data the byte array containing the data
	 * @param start the first index in data that the double is located in
	 * @param length the number of bytes that the double is contained in. This method will treat bytes start to start +
	 *        lengh - 1 as the data for the byte
	 * @return a double which is parsed from the data
	 */
	public static float floatFromBytes(byte[] data, int start, int length)
	{
		float num = Float.intBitsToFloat(intFromBytes(data, start, length));

		return num;
	}


	/**
	 * Converts two bytes into an integer
	 * 
	 * @param highByte the higher order byte in the integer. This is the byte that will be shifted into
	 * @param lowByte the lower order byte, this byte will not be shifted when converting to an integer
	 * @return an integer
	 */
	public static int intFromBytes(byte highByte, byte lowByte)
	{
		return ((highByte << 8) | (lowByte & 0x00ff));
	}


	/**
	 * Converts a byte array into an int.
	 * 
	 * @param data the byte array containing the data
	 * @param start the first index in data that the double is located in
	 * @param length the number of bytes that the double is contained in. This method will treat bytes start to start +
	 *        lengh - 1 as the data for the byte
	 * @return a double which is parsed from the data
	 */
	public static int intFromBytes(byte[] data, int start, int length)
	{
		int accum = 0;

		for (int i = 0; i < length; i++)
		{
			if (i > 0)
			{
				accum <<= 8;
			}
			if (start + i < data.length)
			{
				accum |= (data[start + i] & 0xFF);
			}
		}

		return accum;
	}


	/**
	 * Converts a byte array into a long.
	 * 
	 * @param data the byte array containing the data
	 * @param start the first index in data that the double is located in
	 * @param length the number of bytes that the double is contained in. This method will treat bytes start to start +
	 *        lengh - 1 as the data for the byte
	 * @return a double which is parsed from the data
	 */
	public static long longFromBytes(byte[] data, int start, int length)
	{
		long accum = 0;

		for (int i = 0; i < length; i++)
		{
			if (i > 0)
			{
				accum <<= 8;
			}
			accum |= (data[start + i] & 0xFF);
		}

		return accum;
	}
}
