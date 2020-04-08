package builder.mil.nrl.atest.worldwind.openflight;

import java.awt.Color;
import java.util.logging.Logger;

import builder.mil.nrl.atest.icon.Orientation;

import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

/**
 * Class which represents record op code 70 a Vertex with color, normal and texture
 * 
 * @author doyle
 * @since Jul 28, 2009
 * 
 */
public class VertexColorNormalUV implements Vertex
{
	private final static Logger log = Logger.getLogger(VertexColorNormalUV.class.getSimpleName());

	public static class VertexColorNormalUVFactory
	{

		private VertexColorNormalUVFactory()
		{
			// no op
		}


		public VertexColorNormalUV load(byte[] data, Orientation orient)
		{

			// int colorNameIndex = BinUtil.intFromBytes(data[4], data[5]);
			// int flags = BinUtil.intFromBytes(data[6], data[7]);

			double xCoord = orient.getX(BinUtil.doubleFromBytes(data, 8, 8), BinUtil.doubleFromBytes(data, 16, 8));
			double yCoord = orient.getY(BinUtil.doubleFromBytes(data, 8, 8), BinUtil.doubleFromBytes(data, 16, 8));
			double zCoord = BinUtil.doubleFromBytes(data, 24, 8);

			float iNormal = BinUtil.floatFromBytes(data, 32, 4);
			float jNormal = BinUtil.floatFromBytes(data, 36, 4);
			float kNormal = BinUtil.floatFromBytes(data, 40, 4);

			float uTexture = BinUtil.floatFromBytes(data, 44, 4);
			float vTexture = BinUtil.floatFromBytes(data, 48, 4);

			int alpha = (data[52] & 0xFF);
			int blue = (data[53] & 0xFF);
			int green = (data[54] & 0xFF);
			int red = (data[55] & 0xFF);

			Color c = new Color(red, green, blue, alpha);

			log.info("Vertex: (" + xCoord + ", " + yCoord + ", " + zCoord + ") Normal: (" + iNormal + ", " + jNormal + ", " + kNormal + ") Color: "
				+ c);

			return new VertexColorNormalUV(new Vec4((float) xCoord, (float) yCoord, (float) zCoord), new Vec4(iNormal, jNormal, kNormal),
				new TexCoord(uTexture, vTexture), c);
		}
	}

	public static final VertexColorNormalUVFactory FACTORY = new VertexColorNormalUVFactory();

	private final Vec4 vertex;

	private final Vec4 normal;

	private final TexCoord texCoord;

	private final Color color;


	public VertexColorNormalUV(Vec4 coord, Vec4 normal, TexCoord texCoord, Color color)
	{
		super();
		this.vertex = coord;
		this.normal = normal;
		this.texCoord = texCoord;
		this.color = color;
	}


	@Override
	public Vec4 getVert()
	{
		return vertex;
	}


	@Override
	public Vec4 getNormal()
	{
		return normal;
	}


	@Override
	public TexCoord getTextureCoord()
	{
		return texCoord;
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
		return true;
	}


	@Override
	public Color getColor()
	{
		return color;
	}
}
