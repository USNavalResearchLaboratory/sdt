/*
 * LoaderFactory.java
 *
 * Created on February 27, 2008, 10:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 * 
 * modified by Christian Doyle: added flt file type
 */

package builder.mil.nrl.atest.worldwind.openflight;

import builder.mil.nrl.atest.icon.Orientation;

import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.loader.MaxLoader;
import net.java.joglutils.model.loader.WaveFrontLoader;
import net.java.joglutils.model.loader.iLoader;

/**
 * 
 * @author Brian Wood
 */
public class LoaderFactory
{
	public static final int FILETYPE_UNKNOWN = -1;

	public static final int FILETYPE_3DS = 1;

	public static final int FILETYPE_OBJ = 2;

	public static final int FILETYPE_FLT = 3;

	public static final int FILETYPE_BUILDER = 4;


	public static Model load(String source, Orientation orientation) throws ModelLoadException
	{
		iLoader loader = getLoader(source, orientation);
		if (loader == null)
			return null;

		return loader.load(source);
	}


	private static iLoader getLoader(String path, Orientation orientation)
	{
		switch (determineFiletype(path))
		{
			case FILETYPE_3DS:
				return new MaxLoader();

			case FILETYPE_OBJ:
				return new WaveFrontLoader();

			case FILETYPE_FLT:
				return new OpenFlightLoader(orientation);

			case FILETYPE_BUILDER:
				return new BuilderModelLoader(orientation);

			default:
				return null;
		}
	}


	/**
	 * Parses the file suffix to determine what file format the model is in.
	 * 
	 * @param path File path info
	 * @return int Constant indicating file type
	 */
	public static int determineFiletype(String path)
	{
		int type;
		String tokens[] = path.split("\\.");

		switch (tokens[tokens.length - 1])
		{
			case "3ds":
				type = FILETYPE_3DS;
				break;
			case "obj":
				type = FILETYPE_OBJ;
				break;
			case "flt":
				type = FILETYPE_FLT;
				break;
			case BuilderModelLoader.EXTENSION:
				type = FILETYPE_BUILDER;
				break;
			default:
				type = FILETYPE_UNKNOWN;
		}

		return type;
	}
}
