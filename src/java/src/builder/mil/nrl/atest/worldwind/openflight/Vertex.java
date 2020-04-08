package builder.mil.nrl.atest.worldwind.openflight;

import java.awt.Color;

import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

/**
 * Interface which contains common methods for all Vertex records
 * 
 * @author doyle
 * @since Aug 17, 2009
 * 
 */
public interface Vertex
{
	/**
	 * Returns the floating point values of the coords of this vertex
	 * 
	 * @return
	 */
	public Vec4 getVert();


	/**
	 * Returns the normal for this vertex
	 * 
	 * @return
	 */
	public Vec4 getNormal();


	/**
	 * @return the color of this vertex
	 */
	public Color getColor();


	/**
	 * @return returns the texture coords for this vertex
	 */
	public TexCoord getTextureCoord();


	/**
	 * @return true if this vertex has a normal
	 */
	public boolean hasNormal();


	/**
	 * @return true if this vertex has a color associated with it
	 */
	public boolean hasColor();


	/**
	 * @return true if this vertex has texture coords associated with it
	 */
	public boolean hasTexture();
}
