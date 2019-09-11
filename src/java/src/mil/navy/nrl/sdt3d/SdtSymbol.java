package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class SdtSymbol {
	public enum Type {CIRCLE, ELLIPSE, SPHERE, CYLINDER, SQUARE, RECTANGLE, RNDRECTANGLE, RNDSQUARE, CUBE, BOX, CONE, NONE, INVALID}
	private SdtNode sdtNode = null;
	protected Airspace airspaceShape = null;
	boolean isIconHugging = true;
	protected String symbolType = null;	
    public enum Axis {X, Y, Z};
    // For now we assume our models' are oriented along x or y axis
    protected boolean isInitialized = false;
    private Color symbolColor = Color.RED; // default
    double scale = 1.5;              // default
    double width = 32;
	double height = 32;
	double opacity = 0.3;
	int outlineWidth = 1;
	List<LatLon> latLonList = null;	
	
	public SdtSymbol(String type)
	{
		this.symbolType = type;
	}
	
	public SdtSymbol(String type,SdtNode theNode)
	{
		this.symbolType = type;
		this.sdtNode = theNode;
	}
	public Airspace getAirspace()
	{
		return airspaceShape;
	}
	public void setType(String type)
	{
		this.symbolType = type;
	}

	public void isIconHugging(boolean iconHugging)
	{
		isIconHugging = iconHugging;
	}
	public void setWidth(double theRadius)
	{
		width = theRadius;
	}
	public void setHeight(double theRadius)
	{
		height = theRadius;
	}
	public void setOpacity(double theOpacity)
	{
		opacity = theOpacity;
	}
	public void setOutlineWidth(int theWidth)
	{
		outlineWidth = theWidth;
	}
	public Double getOpacity()
	{
		return opacity;
	}
    public void setLatLon(List<LatLon> latLon)
    {
    	latLonList = latLon;
    }
    public List<LatLon> getLatLon()
    {
    	return latLonList;
    }
	public double getWidth()
	{
		// If we're an icon hugging symbol use the sprite's width as the width 
		if (isIconHugging && sdtNode != null && sdtNode.getSprite() != null && sdtNode.getSprite().getWidth() > 0)
			return sdtNode.getSprite().getWidth();
		else
			return width;
		
	}
	public double getHeight()
	{
		// if we're an icon hugging symbol use the sprite's width as the width
		if (isIconHugging && sdtNode != null && sdtNode.getSprite() != null && sdtNode.getSprite().getHeight() > 0)
			return sdtNode.getSprite().getHeight();

		if (height == 0)
			return getWidth();
		else
			return height;

	}
	public SdtNode getSdtNode()
	{
		return sdtNode;
	}
	public void setColor(Color color)
    {
        symbolColor = color;
    }
	public Color getColor()
	{
		return symbolColor;
	}   	
	public boolean isInitialized()
	{
		return isInitialized;
	}
	public void setInitialized(boolean value)
	{
		isInitialized = value;  
	}	
    protected void render(DrawContext dc)
    {
    	airspaceShape.render(dc);
    }
    protected Position getPosition()
    {
    	
    	// add exception handling
    	if (sdtNode != null && sdtNode.getPosition() != null)
    		return sdtNode.getPosition();
    	else 
    	{
    		return new Position(LatLon.ZERO,0);
    	}
    }
	// get coordinates relative to given position
	protected List<LatLon> getPolyCoordinates(DrawContext dc)
	{
		Angle heading = dc.getView().getHeading();
		Globe globe = dc.getGlobe();
		Matrix transform = Matrix.IDENTITY;
	    transform = transform.multiply(globe.computeModelCoordinateOriginTransform(getPosition()));	       
		transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));
		
 		double width = (getWidth() > getHeight()) ? getWidth() : getHeight();
		double widthOver2, heightOver2 = 0;
		if (isIconHugging)
		{
			Position pos = getPosition();
			Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
			double d = loc.distanceTo3(dc.getView().getEyePoint());
			widthOver2 = (width*scale)/2 * dc.getView().computePixelSizeAtDistance(d);
			heightOver2 = (getHeight()*scale)/2 * dc.getView().computePixelSizeAtDistance(d);
		}
		else 
		{		
			widthOver2 = getWidth() / 2.0;
			heightOver2 = getHeight() / 2.0;
		}	
		airspaceShape.setAltitudes(getPosition().getElevation() - (heightOver2), 
				getPosition().getElevation() + (heightOver2));
		
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
    }
	public double getRadius(DrawContext dc)
	{
		
		double currentSize = 2;
      	if (isIconHugging)
       	{
      		Vec4 loc = dc.getGlobe().computePointFromPosition(getPosition());	        		        
      		double d = loc.distanceTo3(dc.getView().getEyePoint());

      		currentSize = (getWidth()*scale)/2 * dc.getView().computePixelSizeAtDistance(d);
      		if (currentSize < 2)	        	
      			currentSize = 2;    
      		((SphereAirspace)airspaceShape).setRadius(currentSize);      				
       	}
      	else
      		currentSize = getWidth()/2;
      	
      	return currentSize;
 	}
	public void updatePosition(DrawContext dc)
	{	
		Position pos = getPosition();
		// If we've changed attributes we might not have reinitialized yet...
		if (!isInitialized()) initialize(dc);
		
		if (airspaceShape != null)
		{
			// Although airspaces can support agl/msl we need to toggle
			// the terrain confirming attribute to take advantage of this
			// this is a performance hog however, so we'll manage setting
			// elevation ourselves in the node render pass
			airspaceShape.setTerrainConforming(false);
			airspaceShape.setAltitude(pos.getElevation());
			switch (getType(symbolType))
			{
			case SPHERE:
	      		((SphereAirspace)airspaceShape).setRadius(getRadius(dc));      				
				((SphereAirspace)airspaceShape).setLocation(pos);
				break;
			case CYLINDER:
		        ((CappedCylinder)airspaceShape).setRadius(getRadius(dc));
				((CappedCylinder)airspaceShape).setCenter(pos);
				break;
			case CUBE:
			case BOX:
				((Polygon)airspaceShape).setLocations(getPolyCoordinates(dc));
				break;
			case NONE:
				break;
			default:
				break;
			}
		}		
	}
	public String getType()
	{
		return symbolType;
	}
	public static Type getType(String text)
	{
		if (text.equalsIgnoreCase("SPHERE") || text.equalsIgnoreCase("CIRCLE"))
		{
			return Type.SPHERE;
		}	
		if (text.equalsIgnoreCase("BOX"))
		{
			return Type.BOX;
		}					
		if (text.equalsIgnoreCase("CUBE") || text.equalsIgnoreCase("SQUARE"))
		{
			return Type.CUBE;
		}
		if (text.equalsIgnoreCase("CYLINDER"))
		{
			return Type.CYLINDER;
		}
		if (text.equalsIgnoreCase("NONE"))
		{
			return Type.NONE;
		}
		return Type.INVALID;		
	}	

	public void initialize(DrawContext dc)
	{
		Position pos = getPosition();
		// We might be assigning symbols before we've given the node a position.
		// Create a null position for now, we'll reset the symbol position when
		// a position is assigned
		if (pos == null) pos = new Position(LatLon.ZERO,0);
		
		switch (getType(symbolType))
		{
		case SPHERE:
			if ((airspaceShape != null && !airspaceShape.getClass().getSimpleName().equals("SphereAirspace"))
					|| airspaceShape == null)
			{
				airspaceShape = new SphereAirspace(pos,getWidth()/2.0);
			}
			((SphereAirspace)airspaceShape).setLocation(pos);
      		((SphereAirspace)airspaceShape).setRadius(getRadius(dc));  
			airspaceShape.setAltitude(pos.getElevation());
			break;
		case CYLINDER:
			if (airspaceShape != null && "CappedCylinder" != airspaceShape.getClass().getSimpleName()
					|| airspaceShape == null)
			{
				airspaceShape = new CappedCylinder();
			}
  			break;
		case CUBE:
		case BOX:
			if (airspaceShape != null && "Polygon" != airspaceShape.getClass().getSimpleName()
					|| airspaceShape == null)
			{
				airspaceShape = new Polygon(latLonList);
			}
			((Polygon)airspaceShape).setLocations(getPolyCoordinates(dc));
			break;
		case NONE:
			isInitialized = false; // so we don't keep trying to init
			airspaceShape = null;
			break;
		case INVALID:
			airspaceShape = null;
			System.out.println("airspace is INVALID!");
			break;
		}
 		if (airspaceShape != null)
 		{
			//airspaceShape.setTerrainConforming(false);
			//airspaceShape.setAltitude(pos.getElevation());
			airspaceShape.setAttributes(getDefaultAirspaceAttributes());
 			isInitialized = true;
 		}
	} // initialize
	public Color getInteriorColor()
	{
		return new Color(symbolColor.getRed()/255,symbolColor.getGreen()/255, symbolColor.getBlue()/255, 0.3f); 		
	}
	public Color getBorderColor()
	{
		return new Color(symbolColor.getRed()/255,symbolColor.getGreen()/255, symbolColor.getBlue()/255, 0.4f);		
	}
	private AirspaceAttributes getDefaultAirspaceAttributes()
    {   	
        AirspaceAttributes attributes = new BasicAirspaceAttributes();
        attributes.setMaterial(new Material(getInteriorColor()));
        attributes.setOutlineMaterial(new Material(getBorderColor()));
        attributes.setDrawOutline(true);
        attributes.setOpacity(opacity);
        attributes.setOutlineOpacity(0.35);
        attributes.setOutlineWidth(outlineWidth);
        return attributes;
    }
	
	public String getSymbolType()
	{
		return symbolType;
	}

}