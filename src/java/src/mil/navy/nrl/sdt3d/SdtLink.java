package mil.navy.nrl.sdt3d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.geom.Angle;

public class SdtLink
{
    private SdtPolyline line = null;
    private SdtNode node1 = null;
    private SdtNode node2 = null;
    private String linkID = null;
    private boolean directional = false;
    private Color lineColor = Color.red;
    private double lineWidth = 1;
	private boolean stipple = false;
	private double opacity = 1;
	private GlobeAnnotation label = null;
	private Marker marker = null;
	private boolean showLabel = false;
	private String labelText = null;  // alternative to link name
	private Color labelColor = null;
	private boolean collapsed = false;
	private Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();
	private boolean drawn= false;

	
    public SdtLink(SdtNode n1, SdtNode n2,String link_id)
    {
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
	public boolean alreadyAssigned()
	{
		return (!layerList.isEmpty());
		// ljt check nodes too?||
		//!labelLayerList.isEmpty() ||
			//	(this.getSymbol() != null && !this.getSymbol().getLayerList().isEmpty()));
	}
    public String getLinkID()
    {
    	return this.linkID;
    }
    public boolean isDirectional() {return directional;}
    public void setDirectional(boolean flag) {directional = flag;}
    public boolean isCollapsed() { return collapsed;}
    public void isCollapsed(boolean theValue) { collapsed = theValue;}
    
    SdtNode getDstNode() {return node2;}
    SdtNode getSrcNode() {return node1;}
    SdtNode getDstNode(SdtNode srcNode)
        {return ((srcNode == node1) ? node2 : node1);}

    public void setDrawn(boolean theValue)
	{
		drawn = theValue;
	}
	public boolean isDrawn()
	{
		return drawn;
	}
	public void removeRenderables(sdt3d.AppFrame theApp)
	{
		setDrawn(false);
		if (line != null)
		{	
			theApp.getLinkLayer().removeRenderable(line);
		}
		if (getLabel() != null)
		{
			theApp.getLinkLabelLayer().removeAnnotation(getLabel());
		}
		if (getMarker() != null)
		{
			theApp.getMarkers().remove(getMarker());
		}		
	}
	public void drawRenderables(sdt3d.AppFrame theApp, boolean forceDraw)
	{
		if (isDrawn()) return;
		if (!getSrcNode().nodeInVisibleLayer() || !getDstNode().nodeInVisibleLayer()) 
			if (!forceDraw) return;
		if (!forceDraw && !linkInVisibleLayer()) return;
		
		Polyline line = getLine();
		if (null != line) 
		{
			setDrawn(true);
			theApp.getLinkLayer().addRenderable(line);
			if (getMarker() != null)
			{
				theApp.getMarkers().add(getMarker());
				theApp.getMarkerLayer().setMarkers(theApp.getMarkers());
			}
			if (showLabel() && hasLabel() && hasPosition() && !sdt3d.AppFrame.collapseLinks) // ljt we don't add labels for collapsed links??
			{
				GlobeAnnotation label = getLabel();
				theApp.getLinkLabelLayer().addAnnotation(label);
			}
		}		
	}
    public void addLayer(String val,SdtCheckboxNode theNode)
	{
		if (!layerList.containsKey(val))
			layerList.put(val,theNode);
	}
	public void removeLinkFromLayers()
	{
		removeLinkFromCheckbox(this);
		removeLayer();
	}
    public void removeLayer()
	{
	//	if (!layerList.containsKey(val))
		// Can only be in one layer 
		layerList.clear();
	}	
    public void removeLinkFromCheckbox(SdtLink theLink)
    {
    	// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theCheckboxNode = e.nextElement();
				theCheckboxNode.removeLink(theLink);
			}

		}		
    }
	public boolean linkInVisibleLayer()
	{
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
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
    private String getName()
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
		return showLabel;
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
			// when multiple links are recreated.  If this is a problem we'll
			// need to recalc the link offset & center position etc.
		{
			if ((getSrcNode() == null || getSrcNode().getPosition() == null)
				|| (getDstNode() == null || getDstNode().getPosition() == null))
			{
				// No position for both nodes yet...
				return null;
			}
			return Position.interpolate(.5,getSrcNode().getPosition(),getDstNode().getPosition());
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
		if (showLabel && (null == label) && hasPosition() && getLabelText() != null)
		{	// probably dont' need to set all these...
			label = new GlobeAnnotation(getLabelText(), getLabelPosition());
			label.getAttributes().setAdjustWidthToText(Annotation.SIZE_FIT_TEXT);
			label.getAttributes().setOpacity(.7);
			// For some reason SHAPE_NONE disrupts display of icons!!
			//label.getAttributes().setFrameShape(FrameFactory.SHAPE_NONE);
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
	    //labelColor = null;
	}
	public void setLabelText(String text)
	{		
		if (text.equals(getName()))
			labelText = null;
		else
			labelText = text;
	    if (null != label)
	        label.setText(text);
	}
	public String getLabelText()
	    {return ((null != labelText) ? labelText : getName());}

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
        BasicMarkerAttributes markerAttributes = new BasicMarkerAttributes(new Material(lineColor), BasicMarkerShape.ORIENTED_SPHERE, 1d, 5, 2.5);
    	markerAttributes.setHeadingMaterial(new Material(lineColor));
    	double node1Heading = node1.computeHeading(node1.getPosition(), node2.getPosition());

    	Angle heading = Angle.fromDegrees(node1Heading);
    	marker = new BasicMarker(new Position(node2.getPosition(),node2.getAltitude()), markerAttributes, heading); 
    	return true;
	}
    public Polyline getLine()
    {
    	
         if (null == line)
        {
            if (node1.hasPosition() && node2.hasPosition())
            {
                ArrayList<Position> lp = new ArrayList<Position>();
                lp.add(node1.getPosition());
                lp.add(node2.getPosition());
                line = new SdtPolyline();
                line.setPositions(lp);
                line.setToolTipText(getName());
                if ((0.0 == node1.getPosition().getElevation()) &&
                    (0.0 == node2.getPosition().getElevation())
                    ||
                    node1.getFollowTerrain() &&
                    node2.getFollowTerrain())
                    line.setFollowTerrain(true);
                else
                    line.setFollowTerrain(false);
                line.setColor(lineColor);
                line.setLineWidth(lineWidth);
                line.setNumSubsegments(1);
                line.setPathType(Polyline.GREAT_CIRCLE);
                if (isDirectional())
                {
                	makeMarker();
                }	
              }	
        }
        return line;
    }
             
    public void updatePositions(DrawContext dc,int linkNum,int totalLinks)
    {    	
        if (null != line)
        {
        	if (getSrcNode().getPosition() == null || getDstNode().getPosition() == null) return;
        	
         	Globe globe = dc.getGlobe();
            ArrayList<Position> lp = new ArrayList<Position>();
            lp.add(node1.getPosition());  

            double distance = LatLon.ellipsoidalDistance(getSrcNode().getPosition(), getDstNode().getPosition(),
		    		globe.getEquatorialRadius(),globe.getPolarRadius());
            // initialAngle is the angle degree used to offset links around a circle (was 20.0)            
            double angle,radius,interval = 0, initialAngle = 20.0;
            
            // toggle the links around the radius 
         	if (linkNum%2 == 0)  // even link #
         		angle = Math.PI * ((-initialAngle * linkNum)/180.0);
         	else
         		angle = Math.PI * ((initialAngle * linkNum)/180.0);

            if ((0.0 == node1.getAltitude())  &&
                (0.0 == node2.getAltitude()))
            {
                      line.setFollowTerrain(true);
            }
            else
            {
                     line.setFollowTerrain(false);
            }
             
         	if (!sdt3d.AppFrame.collapseLinks && totalLinks > 1)  
         	{
         		// Number of segments per "parabolic" half of link
         		int numSegments = 10;

         		for (double i = 0, x = -1.0, stepSize = 1.0/(double)numSegments; 
         			x <= 1.0 && !sdt3d.AppFrame.collapseLinks; i++, x += (double)stepSize)
         		{	
         			// This gets a pt for each segment along a straight line between the two nodes
         			interval = (Double.valueOf(i).doubleValue())/(numSegments*2);           	
         			Position pos = Position.interpolate(interval,getSrcNode().getPosition(),getDstNode().getPosition());           	
            	            	
         			double alpha = 0.01;
         			double beta = 1.0; 
         			double distanceOffset = alpha*(Math.log(beta*distance));   
         			radius = distance * (distanceOffset * (1.0 - (x*x)));
            	           	
         			// Convert pos into radians and apply offset
         			Vec4 pt = globe.computePointFromPosition(pos);
         			Vec4 newPt = new Vec4(pt.getX() + radius*Math.sin(angle),pt.getY() + radius*Math.cos(angle),pt.getZ());     
         			Position newPos = globe.computePositionFromPoint(newPt);  
 
         			// If we are below sea level and not following the terrain, don't add 
         			// the link point (otherwise links disappear below the ocean surface)
              	
         			// Note that if the nodes are near the ocean surface, and we have more than
         			// four links, some links may be obscured by others.  As we don't expect this
         			// many links in the near future, let this go for now.  Maybe wwj will be fixed...
         			if (newPos.getElevation() < 0)
         				newPos = new Position(newPos,0);
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
         		if (showLabel() && hasLabel()) getLabel().setPosition(Position.interpolate(.5,getSrcNode().getPosition(),getDstNode().getPosition())); 
         		
         	}	
 
            lp.add(node2.getPosition());
            
            line.setPositions(lp);
            if (isDirectional())
            {
            	Position pos = node2.getPosition();
    			double globeElevation = dc.getGlobe().getElevation(pos.getLatitude(),pos.getLongitude());
    			double node1Heading = node1.computeHeading(node1.getPosition(), node2.getPosition());
    			// If we've gotten here before making a marker, make one now
    			if (getMarker() == null)
    				if (!makeMarker()) 
    				{
    					System.out.println("SdtLink::UpdatePositions() Error making marker");
    					return;
    				}
    			if (node1Heading > -1)
    			{
    				Angle heading = Angle.fromDegrees(node1Heading);
    				marker.setHeading(heading);
    			}

    			if (node2.getAltitude() == 0.0)
    				marker.setPosition(new Position(node2.getPosition(),globeElevation));
    			else
    				marker.setPosition(new Position(node2.getPosition(),node2.getPosition().getElevation()));
             }
        }
    }
       
    public void setStipple(boolean theValue) 
    {
    	stipple = theValue;
	}
    public void setColor(Color color)
    {
    	    	
        lineColor = color;
         if (line != null)
        {
            line.setColor(color);
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
        if (null != line) line.setLineWidth(width);
    }
	public void setOpacity(double theOpacity)
	{
		if (theOpacity == 0) return;
		if (theOpacity < 0.0 || theOpacity > 1.0)
		{
			System.out.print("sdt3d.ArgumentOutOfRange opacity=" + theOpacity + "valid range [0,1]");
			return;
		}
		
		opacity = theOpacity*255;
		lineColor = new Color(lineColor.getRed(),lineColor.getGreen(),lineColor.getBlue(),(int) opacity);
		line.setColor(lineColor);
		
	}

	public Double getOpacity()
	{
		return opacity;
	} 

}  // end class SdtLink
