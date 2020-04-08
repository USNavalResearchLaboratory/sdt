package builder.mil.nrl.atest.worldwind.openflight;

import java.awt.Color;
import java.util.logging.Logger;

import builder.mil.nrl.atest.icon.Orientation;

import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

/**
 * Represents a record of a Vertex with a Color and Normal. Also contains methods for reading this data
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public class VertexColorNormal implements Vertex
{
	private final static Logger log = Logger.getLogger(VertexColorNormal.class.getSimpleName());

	public static class VertexColorNormalFactory
	{

		private VertexColorNormalFactory()
		{
			// no op
		}


		public Vertex load(byte[] data, Orientation orient)
		{

			// int colorNameIndex = BinUtil.intFromBytes(data[4], data[5]);
			// int flags = BinUtil.intFromBytes(data[6], data[7]);

			double xCoord = orient.getX(BinUtil.doubleFromBytes(data, 8, 8), BinUtil.doubleFromBytes(data, 16, 8));
			double yCoord = orient.getY(BinUtil.doubleFromBytes(data, 8, 8), BinUtil.doubleFromBytes(data, 16, 8));
			double zCoord = BinUtil.doubleFromBytes(data, 24, 8);

			float iNormal = BinUtil.floatFromBytes(data, 32, 4);
			float jNormal = BinUtil.floatFromBytes(data, 36, 4);
			float kNormal = BinUtil.floatFromBytes(data, 40, 4);

			int alpha = (data[44] & 0xFF);
			int blue = (data[45] & 0xFF);
			int green = (data[46] & 0xFF);
			int red = (data[47] & 0xFF);

			Color c = new Color(red, green, blue, alpha);

			log.info("Vertex: (" + xCoord + ", " + yCoord + ", " + zCoord + ") Normal: (" + iNormal + ", " + jNormal + ", " + kNormal + ") Color: "
				+ c);

			return new VertexColorNormal(new Vec4((float) xCoord, (float) yCoord, (float) zCoord), new Vec4(iNormal, jNormal, kNormal), c);
		}

	}

	public static final VertexColorNormalFactory FACTORY = new VertexColorNormalFactory();

	private final Vec4 vertex;

	private final Vec4 normal;

	private final Color color;


	private VertexColorNormal(Vec4 vertex, Vec4 normal, Color color)
	{
		this.vertex = vertex;
		this.normal = normal;
		this.color = color;
	}


	@Override
	public Color getColor()
	{
		return color;
	}


	@Override
	public Vec4 getNormal()
	{
		return normal;
	}


	@Override
	public TexCoord getTextureCoord()
	{
		return null;
	}


	@Override
	public Vec4 getVert()
	{
		return vertex;
	}


	@Override
	public boolean hasColor()
	{
		return true;
	}


	@Override
	public boolean hasNormal()
	{
		return true;
	}


	@Override
	public boolean hasTexture()
	{
		return false;
	}
}
