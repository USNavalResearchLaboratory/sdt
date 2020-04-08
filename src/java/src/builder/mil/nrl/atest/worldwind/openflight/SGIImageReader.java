package builder.mil.nrl.atest.worldwind.openflight;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import builder.mil.nrl.spg.logging.LoggerExt;


/**
 * Class which reads a SGI Image .rgb, .rgba, .sgi, .bw, and .int files
 * <p>
 * See <a href="ftp://ftp.sgi.com/graphics/SGIIMAGESPEC">ftp://ftp.sgi.com/graphics/SGIIMAGESPEC</a>.
 * </p>
 * 
 * @author doyle
 * @since Aug 6, 2009
 */
public class SGIImageReader extends ImageReader
{
	private static final int RUN_LENGTH_ENCODED = 1;

	private static final int BYTES_PER_INT = 4;

	private static final int HEADER_LENGTH = 512;

	/**
	 * True if the header has been read
	 */
	private boolean readHeader;

	/**
	 * MAGIC - This is the decimal value 474 saved as a short. This identifies the file as an SGI image file.
	 */
	// private int magic;

	/**
	 * STORAGE - specifies whether the image is stored using run length encoding (RLE) or not (VERBATIM). If RLE is
	 * used, the value of this byte will be 1. Otherwise the value of this byte will be 0. The only allowed values for
	 * this field are 0 or 1.
	 */
	private int storage;

	/**
	 * BPC - describes the precision that is used to store each channel of an image. This is the number of bytes per
	 * pixel component. The majority of SGI image files use 1 byte per pixel component, giving 256 levels. Some SGI
	 * image files use 2 bytes per component. The only allowed values for this field are 1 or 2.
	 */
	private int bpc;

	/**
	 * DIMENSION - described the number of dimensions in the data stored in the image file. The only allowed values are
	 * 1, 2, or 3. If this value is 1, the image file consists of only 1 channel and only 1 scanline (row). The length
	 * of this scanline is given by the value of XSIZE below. If this value is 2, the file consists of a single channel
	 * with a number of scanlines. The width and height of the image are given by the values of XSIZE and YSIZE below.
	 * If this value is 3, the file consists of a number of channels. The width and height of the image are given by the
	 * values of XSIZE and YSIZE below. The number of channels is given by the value of ZSIZE below.
	 */
	// private int dimension;

	/**
	 * XSIZE - The width of the image in pixels
	 */
	private int xSize;

	/**
	 * YSIZE - The height of the image in pixels
	 */
	private int ySize;

	/**
	 * ZSIZE - The number of channels in the image. B/W (greyscale) images are stored as 2 dimensional images with a
	 * ZSIZE or 1. RGB color images are stored as 3 dimensional images with a ZSIZE of 3. An RGB image with an ALPHA
	 * channel is stored as a 3 dimensional image with a ZSIZE of 4. There are no inherent limitations in the SGI image
	 * file format that would preclude the creation of image files with more than 4 channels.
	 */
	private int zSize;

	/**
	 * PINMIN - The minimum pixel value in the image. The value of 0 may be used if no pixel has a value that is smaller
	 * than 0.
	 */
	// private int pixMin;

	/**
	 * PINMAX - The maximum pixel value in the image. The value of 255 may be used if no pixel has a value that is
	 * greater than 255. This is the value that is considered to be full brightness in the image.
	 */
	// private int pixMax;

	/**
	 * DUMMY - This 4 bytes of data should be set to 0.
	 */
	// private String dummy1;

	/**
	 * IMAGENAME - An null terminated ascii string of up to 79 characters terminated by a null may be included here.
	 * This is not commonly used.
	 */
	// private String imageName;


	/**
	 * COLORMAP - This controls how the pixel values in the file should be interpreted. It can have one of these four
	 * values:
	 * 
	 * 0: NORMAL - The data in the channels represent B/W values for images with 1 channel, RGB values for images with 3
	 * channels, and RGBA values for images with 4 channels. Almost all the SGI image files are of this type.
	 * 
	 * 1: DITHERED - The image will have only 1 channel of data. For each pixel, RGB data is packed into one 8 bit
	 * value. 3 bits are used for red and green, while blue uses 2 bits. Red data is found in bits[2..0], green data in
	 * bits[5..3], and blue data in bits[7..6]. This format is obsolete.
	 * 
	 * 2: SCREEN - The image will have only 1 channel of data. This format was used to store color-indexed pixels. To
	 * convert the pixel values into RGB values a colormap must be used. The appropriate color map varies from image to
	 * image. This format is obsolete.
	 * 
	 * 3: COLORMAP - The image is used to store a color map from an SGI machine. In this case the image is not
	 * displayable in the conventional sense.
	 */
	// private int colorMap;

	/**
	 * DUMMY - This 404 bytes of data should be set to 0. This makes the header exactly 512 bytes.
	 */
	// private String dummy2;

	protected SGIImageReader(ImageReaderSpi originatingProvider)
	{
		super(originatingProvider);

	}


	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException
	{
		final ImageInputStream in = (ImageInputStream) getInput();

		readHeader(in);

		final BufferedImage i = new BufferedImage(xSize, ySize, getImageType());

		if (storage == RUN_LENGTH_ENCODED)
		{
			readRunLengthEncodedImageData(in, i.getWritableTile(0, 0));
		}
		else
		{
			readVerbatimImageData(in, i.getWritableTile(0, 0));
		}

		return i;
	}


	private int getImageType()
	{
		int imgType;

		if (zSize == 1)
		{
			imgType = BufferedImage.TYPE_BYTE_GRAY;
		}
		else if (zSize == 3)
		{
			imgType = BufferedImage.TYPE_INT_RGB;
		}
		else
		{
			imgType = BufferedImage.TYPE_INT_ARGB;
		}
		return imgType;
	}


	private void readHeader(ImageInputStream in) throws IOException
	{
		if (readHeader)
			return;

		final byte[] data = new byte[HEADER_LENGTH];

		if (in.read(data, 0, data.length) == -1)
		{
			LoggerExt.getLogger().warning("Encountered unexpected end of file");
		}

		// magic = BinUtil.intFromBytes(data[0], data[1]);
		storage = (data[2] & 0xFF);
		bpc = (data[3] & 0xFF);
		// dimension = BinUtil.intFromBytes(data[4], data[5]);
		xSize = BinUtil.intFromBytes(data[6], data[7]);
		ySize = BinUtil.intFromBytes(data[8], data[9]);
		zSize = BinUtil.intFromBytes(data[10], data[11]);
		// pixMin = BinUtil.intFromBytes(data, 12, 4);
		// pixMax = BinUtil.intFromBytes(data, 16, 4);
		// dummy1 = new String(Arrays.copyOfRange(data, 20, 23));
		// imageName = new String(Arrays.copyOfRange(data, 24, 103));
		// colorMap = BinUtil.intFromBytes(data, 104, 4);
		// dummy2 = new String(Arrays.copyOfRange(data, 108, 404));

		if (bpc == 2)
		{
			// TODO see ftp://ftp.sgi.com/graphics/SGIIMAGESPEC for information on how to implement this
			throw new IOException("SGI Image files with 2 bytes per pixel are not supported.");
		}

		readHeader = true;
	}


	/**
	 * Reads data from this file when it is stored in the run length encoded format
	 * 
	 * @param in
	 * @param r
	 * @throws IOException
	 */
	private void readRunLengthEncodedImageData(ImageInputStream in, WritableRaster r) throws IOException
	{
		final int[] startTab = readLongTab(in);
		/* final int[] lengthTab = */readLongTab(in);

		final byte[] rleData = readRLEData(in);

		// z = 0 red, z = 1 green, z = 2 blue, z = 3 alpha
		for (int z = 0; z < zSize; z++)
		{
			// 0 = bottom scanline of image, ySize - 1 = top scanline
			for (int y = 0; y < ySize; y++)
			{
				byte[] scanline = expandRow(rleData, computeAdjustedOffset(startTab[y + z * ySize]));

				readScanlineIntoImage(r, scanline, y, z);
			}
		}
	}


	/**
	 * All offsets in the start table are indexed from the start of the file, since the rleData
	 * is only the compressed image data and not the whole file, the offset needs to be adjusted
	 * to take this into consideration
	 * 
	 * @param startTabOffset the offset from the start table
	 * @return the adjusted offset that compensates for the header and the tables
	 */
	private int computeAdjustedOffset(int startTabOffset)
	{
		return startTabOffset - HEADER_LENGTH - (2 * BYTES_PER_INT * zSize * ySize);
	}


	/**
	 * Reads a table from the file (start or length). The table will
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private int[] readLongTab(ImageInputStream in) throws IOException
	{
		final int[] table = new int[ySize * zSize];
		final byte[] buffer = new byte[ySize * zSize * BYTES_PER_INT];

		if (in.read(buffer, 0, buffer.length) == -1)
		{
			LoggerExt.getLogger().warning("Encountered unexpected end of file");
		}

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (int i = 0; i < table.length; i++)
		{
			table[i] = BinUtil.intFromBytes(buffer, i * BYTES_PER_INT, BYTES_PER_INT);

			if (table[i] > max)
			{
				max = table[i];
			}

			if (table[i] < min)
			{
				min = table[i];
			}
		}

		return table;
	}


	/**
	 * Reads all of the data in the image into a buffer so it can be indexed into
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private byte[] readRLEData(ImageInputStream in) throws IOException
	{
		final List<Byte> data = new ArrayList<>();

		final byte[] buffer = new byte[512];

		int read = in.read(buffer, 0, buffer.length);

		while (read > 0)
		{
			for (int i = 0; i < read; i++)
			{
				data.add(Byte.valueOf(buffer[i]));
			}

			read = in.read(buffer, 0, buffer.length);
		}

		final byte[] result = new byte[data.size()];

		for (int i = 0; i < data.size(); i++)
		{
			result[i] = data.get(i).byteValue();
		}

		return result;
	}


	/**
	 * Creates a byte array representing the scanline located at offset from the
	 * compressed rle data
	 * 
	 * @param data
	 * @param offset
	 * @return
	 */
	private byte[] expandRow(byte[] data, int offset)
	{
		byte[] scanline = new byte[xSize];

		int pixel;
		int count;
		int inPos = offset;
		int outPos = 0;

		pixel = data[inPos] & 0xFF;
		count = pixel & 0x7F; // mask off the 7 lower order bits
		inPos++;
		while (true)
		{
			if (count == 0)
			{
				break;
			}
			// copy count number of bytes bytes
			else if ((pixel >> 7) == 1)
			{
				while (count > 0)
				{
					scanline[outPos] = data[inPos];
					outPos++;
					inPos++;
					count--;
				}
			}
			// repeat the next byte count times
			else
			{
				pixel = data[inPos];
				while (count > 0)
				{
					scanline[outPos] = (byte) pixel;
					outPos++;
					count--;
				}
				inPos++;
			}

			pixel = data[inPos] & 0xFF;
			count = pixel & 0x7F;
			inPos++;
		}

		return scanline;
	}


	private void readVerbatimImageData(ImageInputStream in, WritableRaster r) throws IOException
	{
		final byte[] scanline = new byte[xSize];

		// z = 0 red, z = 1 green, z = 2 blue, z = 3 alpha
		for (int z = 0; z < zSize; z++)
		{
			for (int y = 0; y < ySize; y++)
			{
				if (in.read(scanline, 0, xSize) == -1)
				{
					LoggerExt.getLogger().warning("Encountered unexpected end of file");
				}

				readScanlineIntoImage(r, scanline, y, z);
			} // for each scan line
		} // for each channel (color)
	}


	/**
	 * Adds the data in the scanline to the raster
	 * 
	 * @param r
	 * @param scanline
	 * @param y
	 * @param z
	 */
	private void readScanlineIntoImage(WritableRaster r, byte[] scanline, int y, int z)
	{
		for (int x = 0; x < xSize; x++)
		{
			int color = (scanline[x] & 0xFF);
			int[] pixel = r.getPixel(x, ySize - y - 1, (int[]) null);

			pixel[z] = color;
			r.setPixel(x, ySize - y - 1, pixel);
		}
	}


	@Override
	public int getHeight(int imageIndex) throws IOException
	{
		readHeader((ImageInputStream) getInput());
		return ySize;
	}


	@Override
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException
	{
		return null;
	}


	@Override
	public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException
	{
		return null;
	}


	@Override
	public int getNumImages(boolean allowSearch) throws IOException
	{
		return 1;
	}


	@Override
	public IIOMetadata getStreamMetadata() throws IOException
	{
		return null;
	}


	@Override
	public int getWidth(int imageIndex) throws IOException
	{
		readHeader((ImageInputStream) getInput());
		return xSize;
	}
}
