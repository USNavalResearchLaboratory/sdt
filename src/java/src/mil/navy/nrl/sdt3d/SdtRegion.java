package mil.navy.nrl.sdt3d;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceShape;
import gov.nasa.worldwind.render.SurfaceSquare;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;

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
    public double getAltitude(DrawContext dc)
    {
		double terrainElev = dc.getGlobe().getElevation(getPosition().getLatitude(), getPosition().getLongitude());
		terrainElev = terrainElev + pos.getAltitude();
		
		switch (getType(symbolType))
		{
		case SPHERE:
		case ELLIPSE:
			airspaceShape.setAltitude(terrainElev);
			break;
		case CYLINDER:
		case CUBE:
		case BOX:
			airspaceShape.setAltitudes(terrainElev - this.getHeight()/2, 
					terrainElev + this.getHeight()/2);			

			break;
		case NONE:
			break;
		default:
			break;
		}		
		// TODO: ljt This is only used by renderCone until we figure out how non airspace symbols should be handled.
		return terrainElev;
    }
	// get coordinates relative to given position
	protected List<LatLon> transformLocations(DrawContext dc)
	{
		Globe globe = dc.getGlobe();
		Matrix transform = Matrix.IDENTITY;
	    transform = transform.multiply(globe.computeModelCoordinateOriginTransform(getPosition()));	
		
		double widthOver2 = this.getWidth()/ 2;
		double heightOver2 = this.getHeight()/ 2;

		Vec4[] points = new Vec4[]
		{
				new Vec4(-widthOver2, -heightOver2, 0.0).transformBy4(transform), // lower left
				new Vec4(widthOver2,  -heightOver2, 0.0).transformBy4(transform), // lower right
				new Vec4(widthOver2,   heightOver2, 0.0).transformBy4(transform), // upper right
				new Vec4(-widthOver2,  heightOver2, 0.0).transformBy4(transform)  // upper left
		};

		LatLon[] locations = new LatLon[points.length];
		for (int i = 0; i < locations.length; i++)
		{
			locations[i] = new LatLon(globe.computePositionFromPoint(points[i]));
		}

        return Arrays.asList(locations);
    }  // end transformLocations

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
			{
				super.initialize(dc);
				updatePosition(dc);
				break;
			}
			case BOX:
			case CUBE:
			{
				super.initialize(dc);	
				getAltitude(dc);
				((Polygon)airspaceShape).setLocations(transformLocations(dc));
				break;
			}
			case CYLINDER:
			{
				airspaceShape = new PartialCappedCylinder();
				airspaceShape.setAttributes(getDefaultAirspaceAttributes());
				((PartialCappedCylinder)airspaceShape).setCenter(pos);
				((PartialCappedCylinder)airspaceShape).setTerrainConforming(false,false);
				// azimuth is clockwise from 0,180,-180,0
				((PartialCappedCylinder)airspaceShape).setAzimuths(Angle.fromDegrees(getLAzimuth()),Angle.fromDegrees(getRAzimuth()));
				((PartialCappedCylinder)airspaceShape).setRadius(getWidth()/2);
				getAltitude(dc); 
	       		isInitialized = true;	
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

			default:
				break;
			}			
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
	// overrides SdtSymbol::removeFromCheckbox()
	public boolean isSelected()
	{
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) 
			{
				SdtCheckboxNode theNode = e.nextElement();
				return theNode.isSelected();
			}
		}
		return true;
	}
	public boolean alreadyAssigned()
	{
		return !layerList.isEmpty();
	}
	public void removeFromCheckbox()
	{
		// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theNode = e.nextElement();
				theNode.removeRegion(this);
			}

		}		
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
		if (text.equalsIgnoreCase("CYLINDER"))
		{
			return Type.CYLINDER;
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
