package mil.navy.nrl.sdt3d;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;
import mil.navy.nrl.sdt3d.SdtSprite.Type;

/**
 * SdtSprites objects are initially created when the sprite type is defined.
 * 
 * 3D Models are loaded at this time and "owned" by the SdtSprite in the 
 * sprite table.
 * 
 * When a SdtModelSprite is assigned to a node, we create a _copy_ of the sprite
 * and keep a _reference_ to the model in the node's sprite.  (This way the model 
 * is only loaded once and we only keep references to the common model "mesh")
 * 
 * KML ColladaRoots however cannot share _references_ to their model vectors (at 
 * least without needing to change a lot of WWJ code IIRC) so a node's SdtSpriteKml
 * returns a reference to a new colladaRoot object to be managed by an SdtNode.
 * 
 * Historically (and currently until this is rewritten) we had a common object
 * managing kml models that could be assigned to a node or be independently rendered.
 * As the code evolved, we used a SdtSpriteKML to manage both.  "independent"
 * SdtSpriteKML types keep a KML collada root in the sprite class while an sdt
 * node keeps calls SdtSpriteKML::GetColladaRoot to get a new collada root
 * managed by the sdt node.  This "shared" class should be
 * split apart.
 * 
 * Each sdt node keeps a unique sprite object (that may be an icon SdtSprite, a
 * SdtSpriteModel, or a SdtSpriteKML).  The sprite classes manage sprite specific
 * things like calculatingRealSize (which is handled differently for kml and 
 * sprite models), or managing heading, pitch roll which are node position 
 * dependent.
 * 
 * Sdt nodes are put in a "dummy" renderable layer.  The sdtNode's render function
 * is responsible for setting the position of all its renderables (symbols, labels,
 * sprites, etc) considering agl, msl, model size, etc.  There is some redundancy
 * here that could be cleaned up.  For example SdtNode::Render calculates model
 * size to get the position offset for models rendered on the globe surface (so
 * 3d models don't disappear below the surface when rendered at terrain).  It then
 * passes this overridden position to the symbol as well as the model size so that
 * any associated symbols can be rendered correctly when the symbol is rendered in
 * its rendering pass.
 * 
 * Model3DLayer maintains a list of SdtSpriteModels as its list
 * of renderables.  It gets the models w,h orientation etc from the SdtSpriteModel
 * when transforming it. 
 * 
 * It may make sense to have a common layer here rather than a model and
 * a symbol layer... TBD.  WWJ has moved away from having specific layers for
 * specific renderable types so this may now be simpler to do.
 * 
 * There is _plenty_ of additional clean up we could do as time allows.
 * 
 * @author ljt
 *
 */

public class SdtSpriteIcon extends SdtSprite implements SdtSpriteDimensions
{
	private UserFacingIcon icon = null;
		
	public SdtSpriteIcon(SdtSpriteIcon template)
	{
		super(template);
		this.spriteType = Type.ICON;
	}

	public SdtSpriteIcon(SdtSprite template) 
	{
		super(template);
		this.spriteType = Type.ICON;

	}

	
	public SdtSpriteIcon(String spriteName) 
	{
		super(spriteName);
		this.spriteType = Type.ICON;
	}

	public UserFacingIcon getIcon()
	{
		//if (this.icon == null)
		//	this.icon = loadIcon(position, nodeName, feedbackEnabled);

		return this.icon;
	}


	
	@Override
	public void loadIcon(Position pos, String nodeName, boolean feedbackEnabled)
	{
		// We create the icon when we assign it to a node as
		// we need to set pos, name, and feedback
		
		if (getIconURL() != null)
		{
			try
			{
				BufferedImage image = ImageIO.read(getIconURL());
				icon = new UserFacingIcon(image, pos);

			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			icon = new UserFacingIcon(getSpritePath(), pos);
			icon.setHighlightScale(1.5);
			// icon.setToolTipFont(font); // TODO pretty up with a nice font
			icon.setToolTipText(nodeName);
			icon.setToolTipTextColor(java.awt.Color.YELLOW);
			icon.setSize(getIconSize());
			icon.setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);
		}

	}

	
	double getLength()
	{
		// no length for icon sprites
		return iconWidth;
	}
	
	
	protected void setPosition(Position pos) 
	{
		icon.setPosition(pos);
		
	}


	boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
	
} // end class SdtSprite
