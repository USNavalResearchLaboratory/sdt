/*********************************************************************
 *
 * AUTHORIZATION TO USE AND DISTRIBUTE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: 
 *
 * (1) source code distributions retain this paragraph in its entirety, 
 *  
 * (2) distributions including binary code include this paragraph in
 *     its entirety in the documentation or other materials provided 
 *     with the distribution.
 * 
 *      "This product includes software written and developed 
 *       by Code 5520 of the Naval Research Laboratory (NRL)." 
 *         
 *  The name of NRL, the name(s) of NRL  employee(s), or any entity
 *  of the United States Government may not be used to endorse or
 *  promote  products derived from this software, nor does the 
 *  inclusion of the NRL written and developed software  directly or
 *  indirectly suggest NRL or United States  Government endorsement
 *  of this product.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * For more information send email to sdt_info@nrl.navy.mil
 *
 *
 * WWJ code:
 * 
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 ********************************************************************/

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
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;
import gov.nasa.worldwind.util.Logging;

public class SdtSymbol implements Renderable
{

	public enum Type {
			CIRCLE, ELLIPSE, SPHERE, CYLINDER, SQUARE, RECTANGLE, RNDRECTANGLE, RNDSQUARE, CUBE, BOX, CONE, NONE, INVALID
	}

	protected SdtNode sdtNode = null;

	protected Airspace airspaceShape = null;

	protected boolean isIconHugging = true;

	protected boolean isScalable = false;

	protected boolean isAbsolutePositioning = false;
	
	protected Position position = null;

	protected Angle orientation = Angle.fromDegrees(0);

	protected String symbolType = null;

	public enum Axis {
			X, Y, Z
	};

	protected boolean isInitialized = false;

	private Color symbolColor = Color.RED; // default

	private double scale = 1;

	protected double lAzimuth = 0;

	protected double rAzimuth = 0;

	private double width = -32;

	private double height = -32;

	protected double opacity = 0.3;

	protected int outlineWidth = 1;

	private List<LatLon> latLonList = null;

	protected Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();


	public SdtSymbol(String type)
	{
		this.symbolType = type;
	}


	SdtSymbol(String type, SdtNode sdtNode)
	{
		if (sdtNode == null)
		{
			String message = Logging.getMessage("SdtSymbol::SdtSymbol() theNode nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		this.symbolType = type;
		this.sdtNode = sdtNode;
	}


	Hashtable<String, SdtCheckboxNode> getLayerList()
	{
		return layerList;
	}

	
	static Type getType(String text)
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


	void setType(String type)
	{
		this.symbolType = type;
	}

	
	Type getSymbolType()
	{
		return SdtSymbol.getType(symbolType);
	}


	void setIconHugging(boolean iconHugging)
	{
		isIconHugging = iconHugging;
	}


	boolean isIconHugging()
	{
		return isIconHugging;
	}


	void setScalable(boolean scalable)
	{
		isScalable = scalable;
	}


	boolean isScalable()
	{
		// if a real world length has been set for the sprite (kml & 3ds), don't scale the symbol
		if (sdtNode != null && sdtNode.getSprite() != null) 
		{
			if (sdtNode.getSprite().isRealSize())
			{
				return false;
			}
		}
		return isScalable;
	}


	void setAbsolutePositioning(boolean absolutePositioning)
	{
		isAbsolutePositioning = absolutePositioning;
	}


	boolean getAbsolutePositioning()
	{
		return isAbsolutePositioning;
	}


	void setWidth(double theWidth)
	{
		width = theWidth;
	}

	
	double getWidth()
	{
		
		if (sdtNode != null && sdtNode.getSprite() != null)
		{
			// Get the width from the sprite.  If the sprite is a model getSymbolSize
			// checks to see if we are rendering in real-world size or not.
			if (isIconHugging())
			{
				return sdtNode.getSprite().getSymbolSize();
			}
		}
		// else return the default width or any scaleable symbol size set
		return width;

	}

	void setHeight(double theHeight)
	{
		height = theHeight;
	}


	double getHeight()
	{
		// if we're an icon hugging symbol use the sprite's height
		if (sdtNode != null && sdtNode.getSprite() != null && sdtNode.getSprite().getHeight() > 0)
		{
			if (isIconHugging)
			{
				return sdtNode.getSprite().getHeight();
			}
		}

		if (height == 0)
			return getWidth();
		else
			return height;

	}
	
	
	void setOpacity(double theOpacity)
	{
		if (theOpacity < 0.0 || theOpacity > 1.0)
		{
			System.out.print("sdt3d.ArgumentOutOfRange opacity=" + theOpacity + "valid range [0,1]");
			return;
		}
		opacity = theOpacity;
	}


	void setOutlineWidth(int theWidth)
	{
		outlineWidth = theWidth;
	}


	int getOutlineWidth()
	{
		return outlineWidth;
	}


	Double getOpacity()
	{
		return opacity;
	}


	void setLatLon(List<LatLon> latLon)
	{
		latLonList = latLon;
	}


	List<LatLon> getLatLon()
	{
		return latLonList;
	}


	void setScale(double d)
	{
		this.scale = d;
	}


	double getScale()
	{
		if (isIconHugging())
			return scale * 1.5;
		else
			return scale;
	}

	
	// Overridden by SdtCone
	void setLAzimuth(double d)
	{
		lAzimuth = d;
		// We reference orientation in 0-360
		orientation = Angle.fromDegrees(lAzimuth);
		// Convert from 0-360 what the cylinder airspace wants for azimuth -180 180
		while (lAzimuth > 180)
			lAzimuth -= 360;
	}

	
	protected double getLAzimuth()
	{
		return lAzimuth;
	}

	
	void setRAzimuth(double d)
	{
		this.rAzimuth = d;
	}
	

	double getRAzimuth()
	{
		return rAzimuth;
	}


	double getMaxDimension()
	{
		// Fist set to symbol size
		double size = width > height ? width : height;
		
		if (sdtNode != null && sdtNode.getSprite() != null)
		{	
			if (isIconHugging())
			{
				double spriteWidth = sdtNode.getSprite().getWidth();
				double spriteHeight = sdtNode.getSprite().getHeight();
				size = spriteWidth > spriteHeight ? spriteWidth : spriteHeight;
			}
			
			if (sdtNode.getSprite().getType() != SdtSpriteIcon.Type.ICON)
			{
				if (width > 0 || height > 0)
				{
					// Use symbol size if set
					size = width > height ? width : height;
				}
				else
				{
					// Use icon size
					size = sdtNode.getSprite().getSymbolSize();
				}
			}
		}
		if (size <= 0)
		{
			size = 32;
		}
		return size;		
	}


	double getMinDimension()
	{
		double min = (getWidth() > getHeight()) ? getHeight() : getWidth();
		return min;
	}


	protected SdtNode getSdtNode()
	{
		return sdtNode;
	}


	void setColor(Color color)
	{
		symbolColor = color;
	}


	Color getColor()
	{
		return symbolColor;
	}


	boolean isInitialized()
	{
		return isInitialized;
	}


	void setInitialized(boolean value)
	{
		isInitialized = value;
	}


	void addLayer(String val, SdtCheckboxNode theNode)
	{

		if (!layerList.containsKey(val))
		{
			layerList.put(val, theNode);
		}
	}


	void removeLayer()
	{
		layerList.clear();
	}


	void removeSymbolFromLayer()
	{
		removeFromCheckbox();
		// only in one layer right now
		layerList.clear();
	}


	// Overridden in SdtRegion
	void removeFromCheckbox()
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


	boolean symbolInVisibleLayer()
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

	
	/* 
	 * Called by sdtNode render pass to update symbol position
	 */
	protected void setPosition(Position pos)
	{
		this.position = pos;
	}
	
	
	protected Position getPosition()
	{
		if (position == null)
		{
			// If our position has not yet been set in the node render
			// pass use the node's position.
			if (sdtNode != null)
			{
				return sdtNode.getPosition();
			}
			return new Position(LatLon.ZERO,0);

		}
		return position;
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
		if (sdtNode != null)
			transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));

		double widthOver2 = (getWidth() * getScale()) / 2;
		double heightOver2 = (getHeight() * getScale()) / 2;

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

		iconCurrentSize = (iconHeight * getScale()) / 2 * dc.getView().computePixelSizeAtDistance(d);
		if (iconCurrentSize < 2)
			iconCurrentSize = 2;

		// Offset the symbol elevation by 1/2 the
		// icon height at the distance and add any altitude
		terrainElev = terrainElev + iconCurrentSize / 2 + sdtNode.getAltitude();

		// Remove icon offset for models above terrain
		if (sdtNode.hasSprite() && sdtNode.getSprite().getType() == SdtSpriteIcon.Type.MODEL && sdtNode.getAltitude() != 0)
			terrainElev = terrainElev - iconCurrentSize / 2;

		// Remove icon offset for icons when symbol offset is disabled
		if (sdtNode.hasSprite() && sdtNode.getSprite().getType() == SdtSpriteIcon.Type.ICON && !sdt3d.AppFrame.symbolOffset)
			terrainElev = terrainElev - iconCurrentSize / 2;

		// subtract terrain elevation if at msl
		if (sdtNode.getUseAbsoluteElevation() && !sdtNode.getFollowTerrain())
			terrainElev = terrainElev - dc.getGlobe().getElevation(sdtNode.getPosition().getLatitude(), sdtNode.getPosition().getLongitude());

		switch (getType(symbolType))
		{
			case SPHERE:
			case ELLIPSE:
				if (sdtNode.hasSprite() && sdtNode.getSprite().getType() == SdtSpriteIcon.Type.MODEL && sdtNode.getAltitude() == 0)
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
			case CONE:
			case CYLINDER:
			case CUBE:
			case BOX:
				double symbolHeightOver2 = (getHeight() * getScale()) / 2;

				// If icon hugging calculate symbol height at distance
				if (isIconHugging() || isScalable())
					symbolHeightOver2 = symbolHeightOver2 * dc.getView().computePixelSizeAtDistance(d);

				// TODO: make cylinder factor variable?
				if (isIconHugging() || isScalable())
					if (getSymbolType() == SdtSymbol.Type.CYLINDER)
						symbolHeightOver2 = symbolHeightOver2 * 0.10;

				airspaceShape.setAltitudes(terrainElev - symbolHeightOver2,
					terrainElev + symbolHeightOver2);

				break;
			case NONE:
				break;
			default:
				break;
		}
		return terrainElev;
	} // end getAltitude()

	/*
	 * This still does not render symbol size correctly when kml models
	 * get to real size and "scalable" symbol sizes still have some issues.
	 */
	double getRadius(DrawContext dc)
	{
		double currentSize = 2;
		if (dc.getView() == null)
		{
			return getMaxDimension() / 2;
		}

		Vec4 loc = dc.getGlobe().computePointFromPosition(getPosition());
		double d = loc.distanceTo3(dc.getView().getEyePoint());

		if (isIconHugging() || isScalable())
		{

			if (sdtNode != null && sdtNode.getSprite().isRealSize())
			{
				// Just use scale here as iconHugging scale factor
				// doesn't apply for realSize sprites
				currentSize = getMaxDimension() * getScale();
				
				if (sdtNode.getSprite().getType() != SdtSpriteIcon.Type.ICON)
				{
					currentSize = currentSize / 2;
				}
				
			}
			else
			{
				if (sdtNode.getSprite().getType() == SdtSpriteIcon.Type.ICON)
				{
					currentSize = (getMaxDimension() * getScale()) * dc.getView().computePixelSizeAtDistance(d);
				}
				else
				{
					// If symbol size was explicitly set, use that
					if (width > 0 || height > 0)
					{
						currentSize = ((getMaxDimension() * getScale()) / 2) * dc.getView().computePixelSizeAtDistance(d);
					}
					else
					{
						currentSize = sdtNode.getSprite().getSymbolSize(dc); 
					}

				}
			}

			if (currentSize < 2)
				currentSize = 2;
		}
		else
		{
			currentSize = (getMaxDimension() * getScale()); // * 1.5; 
			// * dc.getView().computePixelSizeAtDistance(d);; 
		}
		return currentSize;
	} // getRadius

	
	
	double reverseAzimuth(double heading)
	{		
		if (heading < 180)
		{
			heading = heading + 180;
		}
		else
		{
			heading = heading - 180;
		}
		
		return heading;
	}

	
	double reverseRotation(double orientation, double rotation)
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


	double normalize(double i)
	{
		// find effective angle
		double d = Math.abs(i) % 360;

		if (i < 0)
			// return positive equivalent
			return 360 - d;
		else
			return d;

	} // normalize


	void updateSymbolCoordinates(DrawContext dc)
	{
		Position pos = getPosition();
		
		// If we've changed attributes we might not have reinitialized yet...
		if (!isInitialized())
		{
			initialize(dc);
		}
		
		if (airspaceShape == null)
		{
			String message = "SdtSymbol::updateSymbolCoordinates() theNode nullValue.StringIsNull";
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		
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

			if (getWidth() > getHeight())
				((SdtEllipseAirspace) airspaceShape).setXYRatio(getWidth() / getHeight(), 1, 1);
			else
				((SdtEllipseAirspace) airspaceShape).setXYRatio(1, getHeight() / getWidth(), getHeight() / getWidth());

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
			double leftAzimuth = orientation.degrees; //reverseRotation(orientation.degrees, 90);
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
		case CUBE:
		case BOX:
			((Polygon) airspaceShape).setLocations(transformLocations(dc));
			break;
		case NONE:
			break;
		default:
			break;
		}
		
	}


	void initialize(DrawContext dc)
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
				isInitialized = true; 
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
			
			/** 
			 * Although airspaces can support agl/msl this is (was? tbd)
			 * a performance hog however, so we'll manage setting
			 * elevation ourselves in the render pass so disable
			 * the terrain confirming attribute 
			 */
			airspaceShape.setTerrainConforming(false);

		}
	} // initialize


	Color getInteriorColor()
	{

		return new Color(Integer.valueOf(symbolColor.getRed()),
			Integer.valueOf(symbolColor.getGreen()),
			Integer.valueOf(symbolColor.getBlue()), Integer.valueOf((int) (0.3 * 255)));
	}


	Color getBorderColor()
	{
		return new Color(Integer.valueOf(symbolColor.getRed()),
			Integer.valueOf(symbolColor.getGreen()),
			Integer.valueOf(symbolColor.getBlue()), Integer.valueOf((int) (0.4 * 255)));
	}


	protected AirspaceAttributes getDefaultAirspaceAttributes()
	{
		AirspaceAttributes attributes = new BasicAirspaceAttributes();
		attributes.setInteriorMaterial(new Material(getInteriorColor()));
		attributes.setOutlineMaterial(new Material(getBorderColor()));
		attributes.setDrawOutline(true);
		attributes.setInteriorOpacity(opacity);
		attributes.setOutlineOpacity(0.35);
		attributes.setOutlineWidth(getOutlineWidth());
		return attributes;
	}


	@Override
	public
	void render(DrawContext dc) 
	{
		updateSymbolCoordinates(dc);
		airspaceShape.render(dc);
		
	}

}