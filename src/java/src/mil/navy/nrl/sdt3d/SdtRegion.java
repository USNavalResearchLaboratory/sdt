package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.SurfaceSquare;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.Airspace;

public class SdtRegion extends SdtSymbol
{

	private SurfaceShape surfaceShape = null;
	private String regionName;
	private Position pos;

	public SdtRegion(String name)
	{
		super("INVALID");
		this.regionName = name;
		this.isIconHugging = false;
	}
    private ShapeAttributes getDefaultShapeAttributes()
    {
		ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setDrawInterior(true);
        attributes.setDrawOutline(true);
        attributes.setInteriorMaterial(new Material(getInteriorColor()));
        attributes.setOutlineMaterial(new Material(getBorderColor()));
        attributes.setInteriorOpacity(opacity);
        attributes.setOutlineOpacity(0.4);
 		attributes.setOutlineWidth(outlineWidth);   
    	return attributes;
    }

	public void initialize(DrawContext dc)
	{
		// Implement surface ellipses when we figure out more how
		// we plan to use regions.  
			airspaceShape = null;
			surfaceShape = null;
	 		switch (getType(symbolType))
			{
			case SQUARE:
			{
				surfaceShape = new SurfaceSquare(pos,getWidth());
				surfaceShape.setAttributes(getDefaultShapeAttributes());
				isInitialized = true;
				break;
			}
			case CIRCLE:
			{
				surfaceShape = new SurfaceCircle(pos,getWidth()/2);
				surfaceShape.setAttributes(getDefaultShapeAttributes());
				isInitialized = true;
				break;
			}
			case RECTANGLE:
			{
				surfaceShape = new SurfaceQuad();
				((SurfaceQuad)surfaceShape).setCenter(pos);
				((SurfaceQuad)surfaceShape).setSize(getWidth(),getHeight());
				surfaceShape.setAttributes(getDefaultShapeAttributes());
				isInitialized = true;
				break;
			}	
			case SPHERE:
			case BOX:
			case CUBE:
			{
				super.initialize(dc);
				break;
			}
			case NONE:
			{		
				surfaceShape = null;
				airspaceShape = null;
				break;
			}
			case INVALID:
				System.out.println("region is INVALID!");
				return;
			}			
		}

	public void render(DrawContext dc)
	{
      	if (surfaceShape != null)  
       	{
       		surfaceShape.preRender(dc);
       		surfaceShape.render(dc);
       	}
       	else
       		if (airspaceShape != null)
       			airspaceShape.render(dc);		
	}

	public void setPosition(Position thePos)
	{
		pos = thePos;
		
	}	
	public Position getPosition() 
	{
		return pos;
	}
	public boolean hasPosition()
	{
		return (null != this.pos);
	}

	public void setSurfaceShape(SurfaceShape theShape)
	{
			surfaceShape = theShape;
	}
	public SurfaceShape getSurfaceShape()
	{
		return surfaceShape;
	}
	public Airspace getAirspaceShape()
	{
		return airspaceShape;
	}
	public void removeShape()
	{
		surfaceShape = null;
		airspaceShape = null;
	}

	public String getName()
	{
		return regionName;
	}

	public static Type getType(String text)
	{		
		// ljt settle on types...
		if (text.equalsIgnoreCase("CIRCLE"))
		{
			return Type.CIRCLE;
		}
		if (text.equalsIgnoreCase("SPHERE"))
		{
			return Type.SPHERE;
		}	
		if (text.equalsIgnoreCase("SQUARE"))
		{
			return Type.SQUARE;
		}
		if (text.equalsIgnoreCase("CUBE"))
		{
			return Type.CUBE;
		}
		if (text.equalsIgnoreCase("BOX"))
		{
			return Type.BOX;
		}
		if (text.equalsIgnoreCase("RECTANGLE"))
		{
			return Type.RECTANGLE;
		}				
		if (text.equalsIgnoreCase("RNDSQUARE"))
		{
			return Type.SQUARE;
		}
		if (text.equalsIgnoreCase("RNDRECTANGLE"))
		{
			return Type.RECTANGLE;
		}		
		if (text.equalsIgnoreCase("NONE"))
		{
			return Type.NONE;
		}
		return Type.INVALID;
		
	}	
}
