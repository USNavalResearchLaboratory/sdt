package mil.navy.nrl.sdt3d;

import java.awt.Color;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

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
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;

public class SdtSymbol
{

	public enum Type {
			CIRCLE, ELLIPSE, SPHERE, CYLINDER, SQUARE, RECTANGLE, RNDRECTANGLE, RNDSQUARE, CUBE, BOX, CONE, NONE, INVALID
	}

	protected SdtNode sdtNode = null;

	protected Airspace airspaceShape = null;

	protected boolean isIconHugging = true;

	protected boolean isScalable = false;

	protected boolean isAbsolutePositioning = false;

	protected Angle orientation = Angle.fromDegrees(0);

	protected String symbolType = null;

	public enum Axis {
			X, Y, Z
	};

	// For now we assume our models' are oriented along x or y axis
	protected boolean isInitialized = false;

	private Color symbolColor = Color.RED; // default

	double scale = 1;

	// lAzimuth,rAzimtuh each correspond to clockwise locations from due north (0 thru 180, -180 thru 0)
	// e.g. half circle from north to south
	// -180,0
	// 20 degree circle from due north
	// 0,20
	// 30 degree circle facing west
	// -90,-70
	double lAzimuth = 0;

	double rAzimuth = 0;

	double width = 32;

	double height = 32;

	double opacity = 0.3;

	int outlineWidth = 1;

	List<LatLon> latLonList = null;

	protected Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();


	public SdtSymbol(String type)
	{
		this.symbolType = type;
	}


	public SdtSymbol(String type, SdtNode theNode)
	{
		if (theNode == null)
			System.out.println("NULL NODE!! symbol init");
		this.symbolType = type;
		this.sdtNode = theNode;
	}


	public Hashtable<String, SdtCheckboxNode> getLayerList()
	{
		return layerList;
	}


	public Airspace getAirspace()
	{
		return airspaceShape;
	}


	public void setType(String type)
	{
		this.symbolType = type;
	}

	
	public Type getSymbolType()
	{
		return SdtSymbol.getType(symbolType);
	}


	public void setIconHugging(boolean iconHugging)
	{
		isIconHugging = iconHugging;
	}


	public boolean isIconHugging()
	{
		// if a real world length has been set for the sprite (kml & 3ds), don't scale the symbol
		if (sdtNode != null && sdtNode.hasSprite() && sdtNode.getSprite().getFixedLength() > 0)
			return false;
		return isIconHugging;
	}


	public void setScalable(boolean scalable)
	{
		isScalable = scalable;
	}


	public boolean isScalable()
	{
		// if a real world length has been set for the sprite (kml & 3ds), don't scale the symbol
		if (sdtNode != null && sdtNode.hasSprite() && sdtNode.getSprite().getFixedLength() > 0)
			return false;
		return isScalable;
	}


	public void setAbsolutePositioning(boolean absolutePositioning)
	{
		isAbsolutePositioning = absolutePositioning;
	}


	public boolean getAbsolutePositioning()
	{
		return isAbsolutePositioning;
	}


	public void setWidth(double theWidth)
	{
		width = theWidth;
	}


	public void setHeight(double theHeight)
	{
		height = theHeight;
	}


	public void setOpacity(double theOpacity)
	{
		if (theOpacity < 0.0 || theOpacity > 1.0)
		{
			System.out.print("sdt3d.ArgumentOutOfRange opacity=" + theOpacity + "valid range [0,1]");
			return;
		}
		opacity = theOpacity;
	}


	public void setOutlineWidth(int theWidth)
	{
		outlineWidth = theWidth;
	}


	public int getOutlineWidth()
	{
		return outlineWidth;
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


	public void setScale(double d)
	{
		this.scale = d;
	}


	public double getScale()
	{
		if (isIconHugging())
			return this.scale * 1.5;
		else
			return this.scale;
	}


	public void setLAzimuth(double d)
	{
		this.lAzimuth = d;
		// ljt we reference orientation in 0-360
		orientation = Angle.fromDegrees(this.lAzimuth);
		// Convert from 0-360 what the cylinder airspace wants for azimuth -180 180
		while (this.lAzimuth > 180)
			this.lAzimuth -= 360;
	}


	public void setRAzimuth(double d)
	{
		this.rAzimuth = d;
	}


	public double getLAzimuth()
	{
		return this.lAzimuth;
	}


	public double getRAzimuth()
	{
		return this.rAzimuth;
	}


	public double getMaxDimension()
	{
		double max = (getWidth() > getHeight()) ? getWidth() : getHeight();
		return max;
	}


	public double getMinDimension()
	{
		double min = (getWidth() > getHeight()) ? getHeight() : getWidth();
		return min; // * 1.5;
	}


	public double getWidth()
	{

		if (sdtNode != null && sdtNode.getSprite() != null)
		{
			// If we're an icon hugging symbol use the sprite's width
			if (isIconHugging() && sdtNode.getSprite().getWidth() > 0)
			{
				return sdtNode.getSprite().getWidth();
			}
			// if the default width has not been changed return model length if set.
			// Fix this when we subclass sprites...
			if (sdtNode.getSprite().getFixedLength() > 0 && width == 32)
				return sdtNode.getSprite().getFixedLength();
		}
		// else return the default width or any scaleable symbol size set
		return width;

	}


	public double getHeight()
	{
		// if we're an icon hugging symbol use the sprite's height
		if ((isIconHugging())
			&& sdtNode != null && sdtNode.getSprite() != null && sdtNode.getSprite().getHeight() > 0)
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


	public void addLayer(String val, SdtCheckboxNode theNode)
	{

		if (!layerList.containsKey(val))
		{
			layerList.put(val, theNode);
		}
	}


	public void removeLayer()
	{
		layerList.clear();
	}


	public void removeSymbolFromLayer()
	{
		removeFromCheckbox();
		// only in one layer right now
		layerList.clear();
	}


	// Overridden in SdtRegion
	public void removeFromCheckbox()
	{
		// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theNode = e.nextElement();
				theNode.removeSymbol(this);
			}
		}
	}


	public boolean symbolInVisibleLayer()
	{
		// As we only have one layer, maybe remove layer list?
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theNode = e.nextElement();

				if (theNode.isSelected())
					return true;
			}
			return false;
		}
		return true;
	}


	protected Position getPosition()
	{
		// add exception handling
		if (sdtNode != null && sdtNode.getPosition() != null)
			return sdtNode.getPosition();
		else
			return new Position(LatLon.ZERO, 0);

	}


	// get coordinates relative to given position
	protected List<LatLon> transformLocations(DrawContext dc)
	{
		Angle heading = dc.getView().getHeading();
		Globe globe = dc.getGlobe();
		Matrix transform = Matrix.IDENTITY;
		transform = transform.multiply(globe.computeModelCoordinateOriginTransform(getPosition()));
		// If no node attached we must be a region. We don't want to change the heading
		// we want all the cube regions to face the same direction for now
		if (this.getSdtNode() != null)
			transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));

		double widthOver2 = (this.getWidth() * this.getScale()) / 2;
		double heightOver2 = (this.getHeight() * this.getScale()) / 2;

		if (isIconHugging() || isScalable())
		{
			Position pos = getPosition();
			Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
			double d = loc.distanceTo3(dc.getView().getEyePoint());
			widthOver2 = widthOver2 * dc.getView().computePixelSizeAtDistance(d);
			heightOver2 = heightOver2 * dc.getView().computePixelSizeAtDistance(d);
		}

		Vec4[] points = new Vec4[] {
			new Vec4(-widthOver2, -heightOver2, 0.0).transformBy4(transform), // lower left
			new Vec4(widthOver2, -heightOver2, 0.0).transformBy4(transform), // lower right
			new Vec4(widthOver2, heightOver2, 0.0).transformBy4(transform), // upper right
			new Vec4(-widthOver2, heightOver2, 0.0).transformBy4(transform) // upper left
		};

		LatLon[] locations = new LatLon[points.length];
		for (int i = 0; i < locations.length; i++)
		{
			locations[i] = new LatLon(globe.computePositionFromPoint(points[i]));
		}

		return Arrays.asList(locations);
	} // end transformLocations


	protected double getAltitude(DrawContext dc)
	{
		if (sdtNode == null || !sdtNode.hasPosition())
			return 0;
		double terrainElev = dc.getGlobe().getElevation(sdtNode.getPosition().getLatitude(), sdtNode.getPosition().getLongitude());

		Vec4 loc = dc.getGlobe().computePointFromPosition(getPosition());
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double iconCurrentSize = 2, iconHeight = 32;

		if (sdtNode.hasSprite())
			iconHeight = sdtNode.getSprite().getHeight();

		iconCurrentSize = (iconHeight * this.getScale()) / 2 * dc.getView().computePixelSizeAtDistance(d);
		if (iconCurrentSize < 2)
			iconCurrentSize = 2;

		// Offset the symbol elevation by 1/2 the
		// icon height at the distance and add any altitude
		terrainElev = terrainElev + iconCurrentSize / 2 + getSdtNode().getAltitude();

		// Remove icon offset for models above terrain
		if (sdtNode.hasSprite() && sdtNode.getSprite().getType() == SdtSprite.Type.MODEL && sdtNode.getAltitude() != 0)
			terrainElev = terrainElev - iconCurrentSize / 2;

		// Remove icon offset for icons when symbol offset is disabled
		if (sdtNode.hasSprite() && sdtNode.getSprite().getType() == SdtSprite.Type.ICON && !sdt3d.AppFrame.symbolOffset)
			terrainElev = terrainElev - iconCurrentSize / 2;

		// subtract terrain elevation if at msl
		if (sdtNode.getUseAbsoluteElevation() && !sdtNode.getFollowTerrain())
			terrainElev = terrainElev - dc.getGlobe().getElevation(sdtNode.getPosition().getLatitude(), sdtNode.getPosition().getLongitude());

		switch (getType(symbolType))
		{
			case SPHERE:
			case ELLIPSE:
				if (sdtNode.hasSprite() && sdtNode.getSprite().getType() == SdtSprite.Type.MODEL && sdtNode.getAltitude() == 0)
				{
					// Use the elevation we calculated in the node render pass for models
					airspaceShape.setAltitude(sdtNode.getPosition().getElevation());
				}
				else
				{
					// Note we removed the icon offset above
					airspaceShape.setAltitude(terrainElev);
				}
				break;
			case CYLINDER:
			case CUBE:
			case BOX:
				double symbolHeightOver2 = (this.getHeight() * this.getScale()) / 2;

				// If icon hugging calculate symbol height at distance
				if (isIconHugging() || isScalable())
					symbolHeightOver2 = symbolHeightOver2 * dc.getView().computePixelSizeAtDistance(d);

				// TODO: make cylinder factor variable?
				if (isIconHugging() || isScalable())
					if (this.getSymbolType() == SdtSymbol.Type.CYLINDER)
						symbolHeightOver2 = symbolHeightOver2 * 0.10;

				airspaceShape.setAltitudes(terrainElev - symbolHeightOver2,
					terrainElev + symbolHeightOver2);

				break;
			case NONE:
				break;
			default:
				break;
		}
		// TODO: LJT Until we decide how to handle non airspace symbols, renderCone will use this terrainElev
		return terrainElev;
	} // end getAltitude()


	public double getRadius(DrawContext dc)
	{
		double currentSize = 2;
		if (dc.getView() == null)
		{
			// View has not been initialized yet - let's make a non fixed one
			System.out.println("SdtSymbol::getRadius() view not yet initialized.");

			return getMaxDimension() / 2;
		}

		if (isIconHugging() || isScalable())
		{
			Vec4 loc = dc.getGlobe().computePointFromPosition(getPosition());
			double d = loc.distanceTo3(dc.getView().getEyePoint());

			currentSize = ((getMaxDimension() * this.getScale()) / 2) * dc.getView().computePixelSizeAtDistance(d);

			// TODO: ask the model SdtSpriteModel what its current size is
			// to get accurate sphere size as we zoom in.
			
			if (sdtNode.getSprite().getType() == SdtSprite.Type.MODEL)
			{	
				if (currentSize < ((getMaxDimension() * this.getScale()) / 2))
				{
					currentSize = ((getMaxDimension() * this.getScale()) / 2);
				}
			}
			
			if (currentSize < 2)
				currentSize = 2;
		}
		else
		{
			currentSize = getWidth() * this.getScale() / 2;
		}
		return currentSize;
	} // getRadius


	public double reverseRotation(double orientation, double rotation)
	{
		// convert to +0 to +360 range
		orientation = normalize(orientation);
		rotation = normalize(rotation);

		// desired angle change
		double d1 = rotation - orientation;

		// other (360 - abs d1 ) angle change in reverse (opp to d1) direction
		double d2 = d1 == 0 ? 0 : Math.abs(360 - Math.abs(d1)) * (d1 / Math.abs(d1)) * -1;

		// give whichever has minimum rotation
		if (Math.abs(d1) < Math.abs(d2))
			return d1;
		else
			return d2;

	} // reverseRotation


	private static double normalize(double i)
	{
		// find effective angle
		double d = Math.abs(i) % 360;

		if (i < 0)
			// return positive equivalent
			return 360 - d;
		else
			return d;

	} // normalize


	public void updatePosition(DrawContext dc)
	{
		Position pos = getPosition();
		// If we've changed attributes we might not have reinitialized yet...
		if (!isInitialized())
			initialize(dc);

		if (airspaceShape != null)
		{
			// Although airspaces can support agl/msl we need to toggle
			// the terrain confirming attribute to take advantage of this
			// this is a performance hog however, so we'll manage setting
			// elevation ourselves in the node render pass
			airspaceShape.setTerrainConforming(false);
			getAltitude(dc);

			switch (getType(symbolType))
			{
				case SPHERE:
					((SphereAirspace) airspaceShape).setRadius(getRadius(dc));
					((SphereAirspace) airspaceShape).setLocation(pos);

					break;
				case ELLIPSE:
					((SphereAirspace) airspaceShape).setRadius(getRadius(dc));
					((SphereAirspace) airspaceShape).setLocation(pos);

					if (this.getWidth() > this.getHeight())
						((SdtEllipseAirspace) airspaceShape).setXYRatio(this.getWidth() / this.getHeight(), 1, 1);
					else
						((SdtEllipseAirspace) airspaceShape).setXYRatio(1, this.getHeight() / this.getWidth(), this.getHeight() / this.getWidth());

					break;
				case CYLINDER:
					((PartialCappedCylinder) airspaceShape).setRadius(getRadius(dc));

					// When facing n, 0/360 due east, 90 is N, 270 is S, 180 is W
					// double leftAzimuth = getLAzimuth();

					Angle symbolOrientation = Angle.fromDegrees(getLAzimuth());
					Angle heading = Angle.fromDegrees(sdtNode.getSymbolHeading());
					// We don't want to change last heading if node heading == 0; e.g.
					// node heading has not changed
					if (isAbsolutePositioning)
						orientation = symbolOrientation;
					else if (sdtNode.getSymbolHeading() != 0)
						orientation = symbolOrientation.add(heading);

					// Reverse the rotation of the cylinder so it trues up with
					// cone rotation
					double leftAzimuth = reverseRotation(orientation.degrees, 90);
					double tmpLeftAzimuth = leftAzimuth;
					double rightAzimuth = getRAzimuth();

					// Calculate cylinder base on converted orientation & width of cylinder wedge
					leftAzimuth = leftAzimuth + rightAzimuth / 2;
					rightAzimuth = tmpLeftAzimuth - rightAzimuth / 2;

					while (leftAzimuth > 180)
						leftAzimuth -= 360;
					while (rightAzimuth > 180)
						rightAzimuth -= 360;

					((PartialCappedCylinder) airspaceShape).setAzimuths(Angle.fromDegrees(rightAzimuth), Angle.fromDegrees(leftAzimuth));
					((PartialCappedCylinder) airspaceShape).setCenter(pos);

					break;
				// case CUBE:
				case BOX:
					((Polygon) airspaceShape).setLocations(transformLocations(dc));
					break;
				case CUBE:
				case NONE:
					break;
				default:
					break;
			}
		}
	}


	public static Type getType(String text)
	{
		if (text.equalsIgnoreCase("SPHERE") || text.equalsIgnoreCase("CIRCLE"))
		{
			return Type.SPHERE;
		}
		if (text.equalsIgnoreCase("ELLIPSE"))
		{
			return Type.ELLIPSE;
		}
		if (text.equalsIgnoreCase("CONE"))
		{
			return Type.CONE;
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
		// a position is assigned and generate the remaining symbol dimensions
		// from the render draw context in updatePosition
		if (pos == null)
			pos = new Position(LatLon.ZERO, 0);

		switch (getType(symbolType))
		{
			case SPHERE:
				if ((airspaceShape != null && !airspaceShape.getClass().getSimpleName().equals("SphereAirspace"))
					|| airspaceShape == null)
					airspaceShape = new SphereAirspace(pos, getMaxDimension() / 2.0);
				break;
			case ELLIPSE:
				if ((airspaceShape != null && !airspaceShape.getClass().getSimpleName().equals("SdtEllipseAirspace"))
					|| airspaceShape == null)
					// TODO: ljt should we set scale to 1.0 for ellipse?
					airspaceShape = new SdtEllipseAirspace(pos, getMinDimension() / 2.0);
				break;
			case CYLINDER:
				// Region cylinders are initialized by SdtRegion
				if ((airspaceShape != null && !airspaceShape.getClass().getSimpleName().equals("PartialCappedCylinder"))
					|| airspaceShape == null)
					airspaceShape = new PartialCappedCylinder();
				break;
			case CUBE:
			case BOX:
				if ((airspaceShape != null && !airspaceShape.getClass().getSimpleName().equals("Polygon"))
					|| airspaceShape == null)
					airspaceShape = new Polygon(latLonList);
				break;
			case CONE:
				airspaceShape = null;
				isInitialized = true; // ljt???
				break;
			case NONE:
				isInitialized = false; // so we don't keep trying to init
				airspaceShape = null;
				break;
			case INVALID:
				airspaceShape = null;
				System.out.println("airspace is INVALID!");
				break;
			default:
				break;
		}
		if (airspaceShape != null)
		{
			airspaceShape.setAttributes(getDefaultAirspaceAttributes());
			isInitialized = true;
		}
	} // initialize


	public Color getInteriorColor()
	{

		return new Color(Integer.valueOf(symbolColor.getRed()),
			Integer.valueOf(symbolColor.getGreen()),
			Integer.valueOf(symbolColor.getBlue()), Integer.valueOf((int) (0.3 * 255)));
		// return new Color(symbolColor.getRed()/255,symbolColor.getGreen()/255, symbolColor.getBlue()/255, 0.3f);
	}


	public Color getBorderColor()
	{
		return new Color(Integer.valueOf(symbolColor.getRed()),
			Integer.valueOf(symbolColor.getGreen()),
			Integer.valueOf(symbolColor.getBlue()), Integer.valueOf((int) (0.4 * 255)));
	}


	protected AirspaceAttributes getDefaultAirspaceAttributes()
	{
		AirspaceAttributes attributes = new BasicAirspaceAttributes();
		attributes.setMaterial(new Material(getInteriorColor()));
		attributes.setOutlineMaterial(new Material(getBorderColor()));
		attributes.setDrawOutline(true);
		attributes.setOpacity(opacity);
		attributes.setOutlineOpacity(0.35);
		attributes.setOutlineWidth(getOutlineWidth());
		return attributes;
	}

}