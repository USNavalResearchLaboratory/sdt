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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Path.PositionColors;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;

public class SdtLink
{
	private SdtPath line = null;

	private SdtNode node1 = null;

	private SdtNode node2 = null;

	private String linkID = null;

	private boolean directional = false;

	private Color lineColor = Color.red;

	private double lineWidth = 1;

	private int stippleFactor = 0; // solid

	private short stipplePattern = (short) 0xAAAA;

	private double opacity = 1;

	private GlobeAnnotation label = null;

	private Marker marker = null;

	private boolean showLabel = false;

	private String labelText = null; // alternative to link name

	private Color labelColor = null;

	private boolean collapsed = false;

	private Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();

	private boolean drawn = false;

	final private sdt3d.AppFrame theApp;


	public SdtLink(sdt3d.AppFrame theApp, SdtNode n1, SdtNode n2, String link_id)
	{
		this.theApp = theApp;
		this.node1 = n1;
		this.node2 = n2;
		this.linkID = link_id;
		n1.addLinkTo(n2, this);
		n2.addLinkTo(n1, this);
	}


	public Hashtable<String, SdtCheckboxNode> getLayerList()
	{
		return layerList;
	}


	public boolean linkInUserDefinedLayer()
	{
		return (!layerList.isEmpty());
	}


	public String getLinkID()
	{
		return this.linkID;
	}


	public boolean isDirectional()
	{
		return directional;
	}


	public void setDirectional(boolean flag)
	{
		directional = flag;
	}


	public boolean isCollapsed()
	{
		return collapsed;
	}


	public void isCollapsed(boolean theValue)
	{
		collapsed = theValue;
	}


	SdtNode getDstNode()
	{
		return node2;
	}


	SdtNode getSrcNode()
	{
		return node1;
	}


	SdtNode getDstNode(SdtNode srcNode)
	{
		return ((srcNode == node1) ? node2 : node1);
	}


	public void setDrawn(boolean theValue)
	{
		drawn = theValue;
	}


	public boolean isDrawn()
	{
		return drawn;
	}


	private RenderableLayer getLinkLayer()
	{
		if (linkInUserDefinedLayer())
		{
			return theApp.getUserDefinedLinkLayer();
		}
		return theApp.getLinkLayer();
	}


	AnnotationLayer getLinkLabelLayer()
	{
		if (linkInUserDefinedLayer())
		{
			return theApp.getUserDefinedLinkLabelLayer();
		}
		return theApp.getLinkLabelLayer();
	}


	private List<Marker> getMarkers()
	{
		if (linkInUserDefinedLayer())
		{
			return theApp.getUserDefinedMarkers();
		}

		return theApp.getMarkers();
	}


	private MarkerLayer getMarkerLayer()
	{
		if (linkInUserDefinedLayer())
		{
			return theApp.getUserDefinedMarkerLayer();
		}

		return theApp.getMarkerLayer();
	}


	public void removeRenderables()
	{
		// What a mess! When we are deleting multi-directional links each
		// end of the node knows about the link - when we delete it from
		// one nodes perspective, we also remove it from the user defined
		// layer - but we have not yet removed the renderable. When we
		// then try to delete the link from the other nodes perspective
		// (where renderables are deleted) the link doesn't think it is in
		// a user defined layer any longer because we removed the reference
		// to the layer. TBD: For NOW, let's just attempt to remove directly
		// from the user defined layer even though it may not be there.

		// TBD: Just do this by default in the remove renderable function
		// in the first place?
		setDrawn(false);
		if (line != null)
		{
			getLinkLayer().removeRenderable(line);
		}
		if (getLabel() != null)
		{
			getLinkLabelLayer().removeAnnotation(getLabel());
		}
		if (getMarker() != null)
		{
			getMarkers().remove(getMarker());
			getMarkerLayer().setMarkers(getMarkers());
			this.marker = null;
		}
	}


	public void drawRenderables(boolean forceDraw)
	{
		if (isDrawn())
			return;

		if (!getSrcNode().nodeInVisibleLayer() || !getDstNode().nodeInVisibleLayer())
			if (!forceDraw)
				return;
		if (!forceDraw && !linkInVisibleLayer())
			return;

		Path line = getLine();
		if (null != line)
		{
			setDrawn(true);

			getLinkLayer().addRenderable(line);
			if (isDirectional())
			{
				if (marker == null)
				{
					makeMarker();
				}
				getMarkers().add(getMarker());
				getMarkerLayer().setMarkers(getMarkers());
			}
			if (showLabel() && hasPosition() && !sdt3d.AppFrame.collapseLinks) // ljt we don't add labels for collapsed
																				// links??
			{
				GlobeAnnotation label = getLabel();
				if (label != null)
					getLinkLabelLayer().addAnnotation(label);
			}

		}
	}


	public void addLayer(String val, SdtCheckboxNode theNode)
	{
		if (!layerList.containsKey(val))
		{
			layerList.put(val, theNode);
		}
	}


	public void removeLinkFromLayers()
	{
		removeLinkFromCheckbox(this);

		removeLayer();

		setDrawn(false);
	}


	public void removeLayer()
	{
		// Can only be in one layer
		layerList.clear();
	}


	public void removeLinkFromCheckbox(SdtLink theLink)
	{
		// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theCheckboxNode = e.nextElement();
				theCheckboxNode.removeLink(theLink);
			}

		}
	}


	public boolean linkInVisibleLayer()
	{
		if (!linkInUserDefinedLayer())
		{
			return theApp.getLinkLayer().isEnabled();
		}

		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theNode = e.nextElement();
				if (theNode.isSelected()) // && !theNode.isPartiallyChecked()) // ljt 9/8
				{
					return true;
				}
			}
			return false;
		}
		return true;
	}


	private String getLinkIDText()
	{
		if (linkID != null)
			return new String(linkID);

		return null;
	}


	public void setShowLabel()
	{
		showLabel = true;
	}


	public boolean showLabel()
	{
		if (linkInVisibleLayer())
		{
			return showLabel;
		}
		return false;
	}


	public boolean hasLabel()
	{
		return (null != label);
	}


	public Position getLabelPosition()
	{
		if (label != null)
			return label.getPosition();
		else
		// For now approximate the position - we'll reposition the label
		// when multiple links are recreated. If this is a problem we'll
		// need to recalc the link offset & center position etc.
		{
			if ((getSrcNode() == null || getSrcNode().getPosition() == null)
				|| (getDstNode() == null || getDstNode().getPosition() == null))
			{
				// No position for both nodes yet...
				return null;
			}
			return Position.interpolate(.5, getSrcNode().getPosition(), getDstNode().getPosition());
		}
	}


	public boolean isHidden()
	{
		return (!node1.hasPosition() || !node2.hasPosition());
	}


	public boolean hasPosition()
	{
		return (null != this.getLabelPosition());
	}


	public GlobeAnnotation getLabel()
	{
		if (showLabel() && (null == label) && hasPosition() && getLabelText() != null)
		{ // probably dont' need to set all these...
			label = new GlobeAnnotation(getLabelText(), getLabelPosition());
			label.getAttributes().setAdjustWidthToText(Annotation.SIZE_FIT_TEXT);
			label.getAttributes().setOpacity(.7);
			// For some reason SHAPE_NONE disrupts display of icons!!
			// label.getAttributes().setFrameShape(FrameFactory.SHAPE_NONE);
			label.getAttributes().setFrameShape(FrameFactory.SHAPE_RECTANGLE);
			label.getAttributes().setLeader(FrameFactory.LEADER_NONE);
			label.getAttributes().setInsets(new Insets(0, 0, 0, 0));
			label.getAttributes().setScale(.8);
			label.getAttributes().setDrawOffset(new Point(0, -20));
			label.getAttributes().setFont(Font.decode("Arial-Bold-14"));
			label.getAttributes().setTextColor(Color.BLACK);
			if (labelColor != null)
				label.getAttributes().setBackgroundColor(labelColor);
			else
				label.getAttributes().setBackgroundColor(lineColor);
			label.getAttributes().setBorderColor(Color.GRAY);
			label.getAttributes().setCornerRadius(1);
			label.setAltitudeMode(WorldWind.ABSOLUTE);
		}
		if (null != label)
		{
			// is there a reason this is done here?
			label.setAlwaysOnTop(true);
		}
		return label;
	}


	public void removeLabel()
	{
		showLabel = false;
		label = null;
		// labelColor = null;
	}


	public void setLabelText(String text)
	{
		if (text.equals(getLinkIDText()))
			labelText = null;
		else
			labelText = text;
		if (null != label)
			label.setText(text);
	}


	public String getLabelText()
	{
		return ((null != labelText) ? labelText : getLinkIDText());
	}


	public void setLabelColor(Color color)
	{
		labelColor = color;
		if (null != label)
			label.getAttributes().setBackgroundColor(color);
	}


	public Marker getMarker()
	{
		return marker;
	}


	public boolean makeMarker()
	{		
		if (node2.getPosition() == null)
		{
			System.out.println("SdtLink::makeMarker() Warning node2 position is null");
			return false;
		}
		BasicMarkerAttributes markerAttributes = new BasicMarkerAttributes(new Material(lineColor), BasicMarkerShape.ORIENTED_SPHERE, 1d, 5, 2.5);
		markerAttributes.setHeadingMaterial(new Material(lineColor));
		double node2Heading = node2.getHeading();

		Angle heading = Angle.fromDegrees(node2Heading);
		marker = new BasicMarker(new Position(node2.getPosition(), node2.getAltitude()), markerAttributes, heading);
		return true;
	}


	public Path getLine()
	{
		if (null == line)
		{
			if (node1.hasPosition() && node2.hasPosition())
			{
				ArrayList<Position> lp = new ArrayList<Position>();
				lp.add(node1.getPosition());
				lp.add(node2.getPosition());
				line = new SdtPath();
				line.setPositions(lp);
				line.setToolTipText(getLinkIDText());
				if ((0.0 == node1.getPosition().getElevation()) &&
					(0.0 == node2.getPosition().getElevation())
					||
					node1.getFollowTerrain() &&
						node2.getFollowTerrain())
				{
					line.setFollowTerrain(true);
				}
				else
				{
					line.setFollowTerrain(false);
				}
				
				ShapeAttributes attrs = new BasicShapeAttributes();
				attrs.setOutlineWidth(lineWidth);
				attrs.setOutlineStipplePattern(getStipplePattern());
				attrs.setOutlineStippleFactor(getStippleFactor());
				attrs.setInteriorMaterial(new Material(lineColor));
				attrs.setOutlineMaterial(new Material(lineColor));
				attrs.setInteriorOpacity(getOpacity());
				attrs.setOutlineOpacity(getOpacity());
				line.setPathType(AVKey.GREAT_CIRCLE);

				if (node1.getAltitude() == 0.0 && 
					node2.getAltitude() == 0.0)
				{
					line.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
				}
				else
				{
					line.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
				}
				line.setNumSubsegments(1);
				line.setAttributes(attrs);
			}
		}
		
		if (isDirectional() && getMarker() == null)
		{
			makeMarker();
		}

		return line;
	}


	/**
	 * Sets the link line and marker position points
	 * 
	 * @param dc
	 * @param linkNum link number in the set of total displayed links
	 * @param totalLinks total number of links currently displayed
	 */
	public void updatePositions(DrawContext dc, int linkNum, int totalLinks)
	{

		if (null != line)
		{
			if (getSrcNode().getPosition() == null || getDstNode().getPosition() == null)
				return;

			Globe globe = dc.getGlobe();
			ArrayList<Position> lp = new ArrayList<Position>();
			lp.add(node1.getPosition());

			double distance = LatLon.ellipsoidalDistance(getSrcNode().getPosition(), getDstNode().getPosition(),
				globe.getEquatorialRadius(), globe.getPolarRadius());
			// initialAngle is the angle degree used to offset links around a circle (was 20.0)
			double angle, radius, interval = 0, initialAngle = 20.0; // 10.0;

			// TODO: We ~could~ tie initial angle to total visible
			// links but the 10 degree offset seems to work fine and
			// we can't really visualize much below 5.0
			if (totalLinks > 18)
			{
				initialAngle = 10.0;
			}

			if (totalLinks > 36)
			{
				initialAngle = 5.0;
			}

			// toggle the links around the radius
			if (linkNum % 2 == 0) // even link #
			{
				angle = Math.PI * ((-initialAngle * linkNum) / 180.0);
			}
			else
			{
				angle = Math.PI * ((initialAngle * linkNum) / 180.0);
			}

			if ((0.0 == node1.getAltitude()) &&
				(0.0 == node2.getAltitude()))
			{
				line.setFollowTerrain(true);
				line.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
			}
			else
			{
				line.setFollowTerrain(false);
				line.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
			}

			if (!sdt3d.AppFrame.collapseLinks && totalLinks > 1)
			{
				// Number of segments per "parabolic" half of link
				int numSegments = 10;

				for (double i = 0, x = -1.0, stepSize = 1.0 / numSegments; x <= 1.0 && !sdt3d.AppFrame.collapseLinks; i++, x += stepSize)
				{
					// This gets a pt for each segment along a straight line between the two nodes
					interval = (Double.valueOf(i).doubleValue()) / (numSegments * 2);
					Position pos = Position.interpolate(interval, getSrcNode().getPosition(), getDstNode().getPosition());

					double alpha = 0.01;
					double beta = 1.0;
					double distanceOffset = alpha * (Math.log(beta * distance));
					radius = distance * (distanceOffset * (1.0 - (x * x)));

					// Convert pos into radians and apply offset
					Vec4 pt = globe.computePointFromPosition(pos);
					Vec4 newPt = new Vec4(pt.getX() + radius * Math.sin(angle), pt.getY() + radius * Math.cos(angle), pt.getZ());
					Position newPos = globe.computePositionFromPoint(newPt);

					// If we are below sea level and not following the terrain, don't add
					// the link point (otherwise links disappear below the ocean surface)

					// Note that if the nodes are near the ocean surface, and we have more than
					// four links, some links may be obscured by others. As we don't expect this
					// many links in the near future, let this go for now. Maybe wwj will be fixed...
					if (newPos.getElevation() < 0)
					{
						newPos = new Position(newPos, 0);
					}

					if ((!(newPos.getElevation() < 0) ||
						(0.0 == node1.getAltitude()) && (0.0 == node2.getAltitude())))
					{
						lp.add(newPos);
					}

					// If the link has a label reset its position to midpoint of link
					if (getLabel() != null && i == numSegments)
					{
						getLabel().setPosition(newPos);
					}
				}
			}
			else
			{
				if (showLabel() && hasLabel())
					getLabel().setPosition(Position.interpolate(.5, getSrcNode().getPosition(), getDstNode().getPosition()));

			}

			lp.add(node2.getPosition());

			line.setPositions(lp);
			if (isDirectional())
			{
				// If we've gotten here before making a marker, make one now
				if (getMarker() == null)
				{
					if (!makeMarker())
					{
						System.out.println("SdtLink::UpdatePositions() Error making marker");
						return;
					}
				}
				// Get elevation for link end
				Position pos = node2.getPosition();
				double globeElevation = dc.getGlobe().getElevation(pos.getLatitude(), pos.getLongitude());

				// Use SdtNode's compute heading function for now
				double markerHeading = node2.computeHeading(node1.getPosition(), node2.getPosition());
				markerHeading = markerHeading + 180.0;
				marker.setHeading(Angle.fromDegrees(markerHeading));
				if (node2.getAltitude() == 0.0)
				{
					marker.setPosition(new Position(node2.getPosition(), globeElevation));
				}
				else
				{
					marker.setPosition(new Position(node2.getPosition(), node2.getPosition().getElevation()));
				}
			}
		}
	}


	public void setStippleFactor(int stippleFactor)
	{
		this.stippleFactor = stippleFactor;

		if (line != null)
		{			
			line.getAttributes().setOutlineStippleFactor(stippleFactor);
		}

	}


	public int getStippleFactor()
	{
		return stippleFactor;
	}


	public void setStipplePattern(short stipplePattern)
	{
		this.stipplePattern = stipplePattern;

		if (line != null)
		{
			line.getAttributes().setOutlineStipplePattern(stipplePattern);
		}

	}


	public short getStipplePattern()
	{
		// For nodes with multiple links the stipple pattern gets lost - possibly
		// due to the short segments within the line?

		return stipplePattern;
	}


	public void setColor(Color color)
	{
		this.lineColor = color;
		if (line != null)
		{
			line.getAttributes().setOutlineMaterial(new Material(lineColor));
			// reset label color if it has not been explicity set
			if (label != null && labelColor == null)
				label.getAttributes().setBackgroundColor(lineColor);

		}
		if (marker != null)
		{
			marker.getAttributes().setMaterial(new Material(lineColor));
			marker.getAttributes().setHeadingMaterial(new Material(lineColor));
		}
	}


	public void setWidth(double width)
	{
		lineWidth = width;
		if (null != line)
		{
			line.getAttributes().setOutlineWidth(lineWidth);
		}
	}


	public void setOpacity(double theOpacity)
	{
		if (theOpacity == 0)
			return;
		if (theOpacity < 0.0 || theOpacity > 1.0)
		{
			System.out.print("sdt3d.ArgumentOutOfRange opacity=" + theOpacity + "valid range [0,1]");
			return;
		}

		if (line != null)
		{
			line.getAttributes().setOutlineOpacity(theOpacity);
		}
	}


	public Double getOpacity()
	{
		return opacity;
	}

} // end class SdtLink
