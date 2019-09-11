package mil.navy.nrl.sdt3d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

import mil.navy.nrl.sdt3d.SdtSprite.Type;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.kml.impl.KMLModelPlacemarkImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;

public class SdtNode implements Renderable
{
	// Member variables
	private String nodeName;
	private String symbolType;
	private Position position = null;
	private boolean hasFocus = false;
	// If yaw is not set we calculate heading from node position
	private double heading = 0;
	private double yaw = -1;
	private boolean useAbsoluteElevation = sdt3d.AppFrame.useAbsoluteElevation();
	private boolean useAbsoluteYaw = false;
	private boolean cartesian = false;
	private float cartesianLat = 0.0f;
	private float cartesianLon = 0.0f;
	public void setAbsoluteYaw(boolean useAbsolute) {useAbsoluteYaw = useAbsolute;}
	private boolean followTerrain = true;
	private boolean feedbackEnabled = sdt3d.AppFrame.followAll();
	private double altitude = 0;
	private SdtSprite sprite = null;
	private WWModel3D model = null;
	private ColladaRoot colladaRoot = null;
	private KMLPlacemark kmlPlacemark = null;
	private KMLController kmlController = null;
	private KMLRoot kmlRoot = null;
	private UserFacingIcon icon = null;
	private SdtSymbol symbol = null; 
	private GlobeAnnotation label = null;
	private boolean showLabel = true;
	private boolean positionUpdate = false;
	private String labelText = null;  // alternative to node name
	private Color labelColor = Color.YELLOW;
	Map<String, List<SdtLink>> linkTable = new HashMap<String, List<SdtLink>>();
	private Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();
	private Hashtable<String, SdtCheckboxNode> labelLayerList = new Hashtable<String, SdtCheckboxNode>();

	private boolean drawn = false;
	
	public SdtNode(String name)
	{
		this.nodeName = name;		
	}	
	public String getName()
	{
		return this.nodeName;
	}
	private String getColorName(Color c) 
	{	
		String colorName = "RED";
		Color color = Color.RED;
		// Is it a valid hex color?
		/*
		 * add support for hex & rgb values?
		 * 
		if (c.startsWith("0x") || c.startsWith("0X"))
		{
			c = c.trim();
			String x[] = c.split("0x");
			if (x.length < 2 ) {System.out.println("sdt3d::validateColor 0x required!"); return color;}
			boolean isHex = x[1].matches("[0-9A-Fa-f]+");
			if (c.length() != 8 || !isHex)
			{
				System.out.println("sdt3d::getColor() invalid hex value");
				return color;
			}
			color = Color.decode(c);
			return color;
		} 
		// Is it a valid rgb value?
		String[] rgbVal = c.split(":");
		if (rgbVal.length > 1)
		{
			if (!(rgbVal.length == 3)) {System.out.println("getColor() Invalid color rgb values"); return color;}
			if ((Integer.valueOf(rgbVal[0]) < 0 || Integer.valueOf(rgbVal[0]) > 255
				|| Integer.valueOf(rgbVal[1]) < 0 || Integer.valueOf(rgbVal[1]) > 255
				|| Integer.valueOf(rgbVal[2]) < 0 || Integer.valueOf(rgbVal[2]) > 255))					
				{System.out.println("getColor() Invalid color rgb values"); return color;}
			color = new Color(Integer.valueOf(rgbVal[0]),Integer.valueOf(rgbVal[1]),Integer.valueOf(rgbVal[2]));
			return color;
		} 
		*/
		if (Color.WHITE.equals(c))
			colorName = "WHITE";
		else if (Color.YELLOW.equals(c))
			colorName = "YELLOW";
		else if (Color.GREEN.equals(c))
			colorName = "GREEN";
		else if (Color.BLUE.equals(c))
			colorName = "BLUE";
		else if (Color.CYAN.equals(c))
			colorName = "CYAN";
		else if (Color.RED.equals(c))
			colorName = "RED";
		else if (Color.PINK.equals(c))
			colorName = "PINK";
		else if (Color.ORANGE.equals(c))
			colorName = "ORANGE";
		else if (Color.MAGENTA.equals(c))
			colorName = "MAGENTA";
		else if (Color.GRAY.equals(c))
			colorName = "GRAY";
		else if (Color.BLACK.equals(c))
			colorName = "BLACK";
		else
			colorName = "RED";
		
		return colorName;
	} //
	public String print()
	{
		String cmd;
		// nodeName::type::pos::label::symbol::orientation
		
		// nodeName
		cmd = "::" + this.getName() + "::";
		// type
		if (this.hasSprite())
			cmd = cmd  +  this.sprite.getName();
		cmd = cmd + "::";
		// position
		if (isCartesian())
		{
			cmd = cmd + this.getCartesianLat() + "," + this.getCartesianLon() + 
				"," + this.altitude + ",c";

			if (getUseAbsoluteElevation())
				cmd = cmd + ",agl ";
			else
				cmd = cmd + ",msl ";
		}	
		else
		{
			if (this.position != null)
			{
				String lat = this.position.getLatitude().toString();
				String lon = this.position.getLongitude().toString();

				lat = lat.substring(0, lat.length() -1);
				lon = lon.substring(0, lon.length() -1);
				cmd = cmd + lon + "," + lat + 
				"," + this.altitude + ",g";

				if (getUseAbsoluteElevation())
					cmd = cmd + ",agl ";
				else
					cmd = cmd + ",msl ";				
			}
		}
		cmd = cmd + "::";
		
		// label
		if (this.showLabel)
			cmd = cmd + getColorName(labelColor)+ "," + getLabelText();
		cmd = cmd + "::";
		
		// symbol
		if (getSymbolType() != null)
		{
			String width = Double.toString(getSymbol().getWidth());
			String height = Double.toString(getSymbol().getHeight());
			if (getSymbol().isScalable() | getSymbol().isIconHugging())
			{
				width = width + "s";
				height = height + "s";
			}
			
			cmd = cmd + getSymbolType() + "," + getColorName(getSymbol().getColor()) +
			"," + getSymbol().getOutlineWidth() + "," + width +
			"," + height + "," + getSymbol().getOpacity() +
			"," + getSymbol().getScale(); 
		
			cmd = cmd + ",";
			// orientation pitch,yaw[a|r],roll
			if (getPitch() != null)
				cmd = cmd + getPitch();
			else	
				cmd = cmd + "x";
			
			if (getYaw() != null)
			{
				cmd = cmd + "," + getYaw();
				if (this.useAbsoluteYaw)
					cmd = cmd + "a";
				else
					cmd = cmd + "r";
			}
			else
				cmd = cmd + ",x";
			
			
			if (getRoll() != null)
			{
				cmd = cmd + "," + getRoll();
			}
			else
				cmd = cmd + ",x";
		
		}
		cmd = cmd + "::";

		return cmd;
	}

	public void setDrawn(boolean theValue)
	{
		drawn = theValue;
	}
	public boolean isDrawn()
	{
		return drawn;
	}
	public void setFocus(boolean theFocus)
	{
		hasFocus = theFocus;
	}
	public boolean hasFocus()
	{
		return hasFocus;
	}
	public void addLayer(String val,SdtCheckboxNode theCheckboxNode)
	{		
		if (!layerList.containsKey(val))
		{
			layerList.put(val,theCheckboxNode);
		}
	}
	public Hashtable<String, SdtCheckboxNode> getLayerList()
	{
		return layerList;
	}
	public Hashtable<String, SdtCheckboxNode> getLabelLayerList()
	{
		return labelLayerList;
	}
	public boolean attributeAlreadyAssigned()
	{
		// symbol or label in layer
		return ((this.getSymbol() != null && !this.getSymbol().getLayerList().isEmpty()) 
				|| !labelLayerList.isEmpty());
			
	}
	public boolean labelAlreadyAssigned()
	{
		return (!labelLayerList.isEmpty());
	}
	public boolean symbolAlreadyAssigned()
	{
		return (this.getSymbol() != null && !this.getSymbol().getLayerList().isEmpty());
	}
	public boolean nodeAlreadyAssigned()
	{
		
		return (!layerList.isEmpty());
		// Has the node or any of it's components been assigned to a layer?
	/*	return (!layerList.isEmpty() ||
				!labelLayerList.isEmpty() ||
				(this.getSymbol() != null && !this.getSymbol().getLayerList().isEmpty())); */
	}
	public void removeLayer()
	{
		// only in 1 layer for now
		layerList.clear();
	}
	public void removeNodeFromLayer()
	{
		removeNodeFromCheckbox();
		removeLayer();		
	}
    public void removeNodeFromCheckbox()
    {
    	// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theCheckboxNode = e.nextElement();
				theCheckboxNode.removeNode(this);
			}
		}		
    }
	public void addLabelLayer(String val,SdtCheckboxNode theCheckboxNode)
	{		
		if (!labelLayerList.containsKey(val))
		{
			labelLayerList.put(val,theCheckboxNode);
		}
	}
	public void removeLabelLayer()
	{
		// only in 1 layer now
		labelLayerList.clear();
	}
	public void setSymbolType(String val)
	{
		symbolType = val;
	}
	public String getSymbolType()
	{
		return symbolType;
	}
	public boolean isCartesian()
	{
		return cartesian;
	}
	public void setCartesian(boolean isCartesian)
	{
		cartesian = isCartesian;
	}
	public float getCartesianLat()
	{
		return cartesianLat;
	}
	public float getCartesianLon()
	{
		return cartesianLon;
	}
	public void setCartesianLat(float inLat)
	{
		cartesianLat = inLat;
	}
	public void setCartesianLon(float inLon)
	{
		cartesianLon = inLon;
	}
	public boolean nodeInVisibleLayer()
	{		
		// ljt add visibility option to node so we don't have to go through this?
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theCheckboxNode = e.nextElement();

				if (theCheckboxNode.isSelected())// && !theNode.isPartiallyChecked())
					return true;
			}
			return false;
		}		
		return true;
	}
	public boolean labelInVisibleLayer()
	{

		if (!labelLayerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = labelLayerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theCheckboxNode = e.nextElement();

				if (theCheckboxNode.isSelected())// && !theNode.isPartiallyChecked())
					{
						return true;
					}
			}
			return false;
		}		
		return true;
	}

	public void render(DrawContext dc)
	{		
		if (!nodeInVisibleLayer()) return;

		// Get the latest globe elevation and update the nodes elevation
		Position oldPos = position;
		if (position == null) return;
		double globeElevation = dc.getGlobe().getElevation(position.getLatitude(), position.getLongitude());
		Position terrainElevation = new Position(position,globeElevation);
		Position aglPosition = new Position(position,getAltitude() + globeElevation);
		Position mslPosition = new Position(position,altitude);

		// Update sprite location
		if (getNodeIcon() != null)
		{
			if (getFollowTerrain())
			{
				// If node is at terrain level, reset position to latest globe elevation
				getNodeIcon().setPosition(terrainElevation);
				position = terrainElevation;
			}
			else
			{
				if (!getUseAbsoluteElevation())
				{   
					getNodeIcon().setPosition(aglPosition);
					position = aglPosition;
				}
				else
					// else we're at absolute elevation
					position = mslPosition;
			}	
		}
		// Update model location
        if ((getNodeModel() != null))
        {
        	if (!getFollowTerrain())
        	{
        		if (!getUseAbsoluteElevation())
         		// else if the node is at agl, add current globe elevation to node alt
 					position = aglPosition;
        		else
 					position = mslPosition;
        	} else
        	{
            	double elevation = dc.getGlobe().getElevation(position.getLatitude(),position.getLongitude());
            	if (model.isRealSize())
               		elevation += (model.getHeight() / 2.0);    	
            	else
            	{
                    Vec4 loc = dc.getGlobe().computePointFromPosition(position);
                    double localSize = model.computeSizeScale(dc, loc);
                    elevation += localSize*4;
            	}
            	position = new Position(position.getLatitude(),position.getLongitude(),elevation);       		
        	}
        }
		// Update kml location
        if (hasSprite() && getSprite().getType() == SdtSprite.Type.KML && (getColladaRoot() != null))
        {
        	// The kml renderer does it's own terrain position computations so
        	// set to absolute and use our calcs
        	if (colladaRoot != null)
        		colladaRoot.setAltitudeMode(WorldWind.ABSOLUTE);
        	if (!getUseAbsoluteElevation())
        		// else if the node is at agl, add current globe elevation to node alt
        		position = aglPosition;
        	else
        		position = mslPosition;
        	
        	Vec4 loc = dc.getGlobe().computePointFromPosition(position);
        	double localSize = getSprite().computeKmlSize(dc, loc);
        	
        	// regular sprite setscale function does some image calculation
        	// so we store scale separately for kml models... Fix
        	// when we properly subclass sprites..
        	Double scale = getSprite().getSpriteKml().getScale();
    		Vec4 modelScaleVec = new Vec4(localSize*scale,localSize*scale,localSize*scale);
        	if (getColladaRoot() != null)	
        		colladaRoot.setModelScale(modelScaleVec);        	
        	getColladaRoot().setPosition(position);
        	
       }

        // Else we only have a symbol, still need a location update
        if (getSprite() == null || getSprite().getType() == SdtSprite.Type.NONE)
        {
        	if (getFollowTerrain())
        		position = new Position(position, globeElevation);
        	else 
        	{
        		if (!getUseAbsoluteElevation())
        			position = aglPosition;
        		else
        			position = mslPosition;
        	}
        }
        // Update Label position
         if (hasLabel())
         {        	 
         	if (followTerrain)
        	{  	
        		double alt = 0.0;
        		if ((null != model)  && model.isRealSize())
        			alt += model.getHeight() / 2.0;
        		getLabel().setPosition(new Position(position, alt));
        	}
        	else
        	{   // Annotations always assume the elevation is the offset above
        		// ground level.  So we need to subtract the globe elevation 
        		// from the intended altitude if we're set to msl
        		if (useAbsoluteElevation)
        		{ 
        			double altOffset = altitude - globeElevation;
        			getLabel().setPosition(new Position(position,altOffset));
        		} 
        		else
        			getLabel().setPosition(new Position(position,altitude));
        	}           	
        }
        // If we repositioned the node after getting the latest elevation or
        // have not yet processed a position change, redraw the links & symbols
         
        // Symbols seem to "flicker" when the view changes if we con't do this... fix! 
        if (hasSymbol())
        {
         	// Update symbol coordinates
        	getSymbol().updatePosition(dc);
        }       
        if (!oldPos.equals(position) || positionUpdate)
		{
			updateLinkPositions(dc);
			positionUpdate = false;
		}

		// Otherwise, just leave the node where it is		
	}  // SdtNode::Render()
		
	public void setFeedbackEnabled(boolean val)
	{
		feedbackEnabled = val;
		if (sprite != null && sprite.getType().equals(SdtSprite.Type.ICON))
			getIcon().setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);
		if (sprite != null && sprite.getType().equals(SdtSprite.Type.MODEL))
			getModel().setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled); 
		if (sprite != null && sprite.getType().equals(SdtSprite.Type.KML))
			getKmlController().setValue(AVKey.FEEDBACK_ENABLED,feedbackEnabled);
	}
	public boolean isFeedbackEnabled()
	{
		return feedbackEnabled;
	}
	public boolean hasPosition()
	{
		if (!nodeInVisibleLayer()) return false;
		return (null != this.position);
	}
	public boolean getFollowTerrain()
	{
		return followTerrain;
	}
	public void setFollowTerrain(boolean follow_terrain)
	{
		followTerrain = follow_terrain;
	}
	public boolean getUseAbsoluteElevation()
	{
		return useAbsoluteElevation;
	}
	public void setUseAbsoluteElevation(boolean useAbsoluteElevationFlag)
	{
		useAbsoluteElevation = useAbsoluteElevationFlag;
	}
	public boolean isDisplayed() 
	{
		if (!hasPosition()) return false;

		if (hasLabel()) return true;
		
		if (hasSymbol()) return true;
		
		if (hasSprite())
		{
			switch (sprite.getType())
            {
	            case MODEL:
	                return (null != model);
	            case ICON:
	                return (null != icon);
	            case KML:
	            	return (null != sprite.getSpriteKml());
	            case NONE:
	            	return false;
	            default:
	            	return false;
            }
		}
		return false;

	}

    public double getAltitude() {return altitude;}
    public void setAltitude(double theAltitude) {altitude = theAltitude;}
	public double reverseRotation(double orientation,double rotation)
	{
		// convert to +0 to +360 range
	    orientation = normalize(orientation);
	    rotation = normalize(rotation);

	    // desired angle change 
	    double d1 = rotation - orientation;

	    // other (360 - abs d1 ) angle change in reverse (opp to d1) direction
	    double d2 = d1 == 0 ? 0 : Math.abs(360 - Math.abs(d1))*(d1/Math.abs(d1))*-1;

	    // give whichever has minimum rotation
	    if (Math.abs(d1) < Math.abs(d2))
	        return d1;
	    else
	        return d2;
	    	
	} //reverseRotation
	private static double normalize(double i){
	    // find effective angle 
	    double d = Math.abs(i) % 360;

	    if (i < 0)
	    // return positive equivalent
	        return 360 - d;
	    else 
	        return d;
	    
	} // normalize
    public void setPosition(Position pos)
	{
    	// ljt double heading = 0;
     	Position oldPos = position;
        this.position = pos;
        
     	if ((null != oldPos) && (!pos.equals(oldPos)))
     	{
    		double newHeading = this.computeHeading(this.position, oldPos);
    		if (newHeading > 0)
    		{
    			heading =  newHeading;
    		}
     	}
	    if (isDisplayed())
	    {
	        if (hasSprite())
	        {
	            switch (sprite.getType())
	            {
    	            case MODEL:
    	            	// in case we haven't created model yet 
    	            	if (model == null) getModel();
    	            	if (heading > -1)
    	           			model.setHeading(heading);
  
    	                break;
    	            case ICON:
    	            {
     	            	if (icon == null) getIcon();
     		        	icon.moveTo(this.position);
    	                break;
    	            }
    	            case KML:
    	            {
     	            	if (kmlRoot == null) getKmlRoot();
    	            	if (colladaRoot == null) getColladaRoot();
    	            	// ColladaRoot isn't created until first rendering pass (now?)
    	            	if (colladaRoot == null) break;
     	            	if (heading > -1)
     	            	{
     	            		// collada models rotate opposite direction from 3d
     	            		//reverseRotation(heading,0);
    	            		colladaRoot.setHeading(Angle.fromDegrees(-heading));
     	            	}
     	            	colladaRoot.setPosition(this.position);   
     	            }
				default:
					break;
	            }   
	        } 
	        positionUpdate = true;

	    }
	} // setPosition

	public Position getPosition() 
	{				
		// ljt testing 
		if (!nodeInVisibleLayer()) return null;
		return position;			
	}
	public double getHeading()
	{
		return heading;
	}
	public void recreateLinkLabels(AnnotationLayer annotationLayer)
	{
		
		// TODO Make sure collapsing links works with layer code
		Map<String, List<SdtLink>> linkTable = getLinkTable();
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				Iterator<SdtLink> itr = links.iterator();
				SdtLink theLink = null;
				while (itr != null && itr.hasNext())
				{
					theLink = itr.next();
					if (!sdt3d.AppFrame.collapseLinks)
					{
						GlobeAnnotation theLabel = theLink.getLabel();
						// we're tracking whether the link is collapsed
						// so we don't add the annotation twice (once
						// for each node in the link) 
						if (theLabel != null && theLink.isCollapsed())
						{
							// Remove any collapsed labels 
							if (theLabel.getText().equals("(collapsed)"))
								annotationLayer.removeAnnotation(theLink.getLabel());
							theLabel.setText(theLink.getLabelText());
							theLink.isCollapsed(false);
							annotationLayer.addAnnotation(theLink.getLabel());
						}
					} else
						if (sdt3d.AppFrame.collapseLinks && theLink.hasLabel())
						{
							annotationLayer.removeAnnotation(theLink.getLabel());
							theLink.isCollapsed(true);
						}

				}
				// If we have collapsed the links, readd a label for the collapsed link
				// if one exists
				if (sdt3d.AppFrame.collapseLinks)
				{
					itr = links.iterator();	
					while (itr != null && itr.hasNext())
					{
						theLink = itr.next();
						if (theLink.getLabel() != null)
						{
							theLink.getLabel().setText("(collapsed)");

							// ljt testing for lyaering stuff.. use has position?
							if (theLink.getSrcNode().getPosition() == null 
									|| theLink.getDstNode().getPosition() == null)
								continue;
							
			            	Position pos = Position.interpolate(.5,theLink.getSrcNode().getPosition(),theLink.getDstNode().getPosition());           	
			            	theLink.getLabel().setPosition(pos);
			            	annotationLayer.addAnnotation(theLink.getLabel());
							break;
						}
					}
				} 
			}
		}

	}
	public void removeLinkLabels(AnnotationLayer linkLabelLayer)
	{
		
		Map<String, List<SdtLink>> linkTable = getLinkTable();
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				Iterator<SdtLink> itr = links.iterator();
				SdtLink theLink = null;
				while (itr != null && itr.hasNext())
				{
					theLink = itr.next();
					if (theLink.getLabel() != null)
					{
						linkLabelLayer.removeAnnotation(theLink.getLabel());
					}
				}
			}
		}
	}
	public void updateLinkPositions(DrawContext dc)
	{
		// Update position of any links associated with this
		// node				
		Map<String, List<SdtLink>> linkTable = getLinkTable();
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				Iterator<SdtLink> itr = links.iterator();
				int numLinks = 0;
				int totalLinks = links.size();
				while (itr != null && itr.hasNext())
				{
					numLinks++;
					SdtLink theLink = itr.next();
					theLink.updatePositions(dc,numLinks,totalLinks);
				}
			}
		}			 				
	}
	
	public boolean hasModel() {return (null != model);}
	public boolean has3DModel() {return (null != model) || (null != colladaRoot);}
	public boolean is3DModel() {
		if (getSprite() == null) return false;
		if (getSprite().getType() == SdtSprite.Type.KML 
				|| getSprite().getType() == SdtSprite.Type.MODEL)
			return true;
		return false;			
	}
	public void setPitch(Double pitch)
	{
		if (model != null)
			model.setPitch(pitch);
		else if (colladaRoot != null)
			colladaRoot.setPitch(Angle.fromDegrees(pitch));
				
	}
	public Double getPitch()
	{
		if (model != null)
			return model.getPitch();
		else if (colladaRoot != null)
			return colladaRoot.getPitch().degrees;
		
		return null;
	}
	// Set yaw, or "heading"
	public void setYaw(Double inYaw)
	{
		yaw = inYaw;
		if (model != null)
			model.setYaw(yaw);
		else if (colladaRoot != null)
		{
			// Reverse heading so collada models true up with model
			// heading
			colladaRoot.setHeading(Angle.fromDegrees(-yaw));
		}
		
		// Note that heading is calculated from node position
		// if not set via orientation
		heading = yaw;
	}
	public Double getYaw()
	{
		if (model != null)
			return model.getYaw();
		else if (colladaRoot != null)
			return colladaRoot.getHeading().degrees;
		
		return null;
	}
	public void setRoll(Double roll)
	{
		if (model != null)
			model.setRoll(roll);
		else if (colladaRoot != null)
			colladaRoot.setRoll(Angle.fromDegrees(roll));
	}
	public Double getRoll()
	{
		if (model != null)
		{
			return model.getRoll();
		}
		else if (colladaRoot != null)
			return colladaRoot.getRoll().degrees;
		
		return null;
	}
	public boolean hasIcon() 
	{
		return (sprite != null && sprite.getType() == Type.ICON);
	}
	
	public double computeHeading(Position posPrev, Position posNext)
	{		
		double lat1 = posPrev.getLatitude().getRadians();
		double lon1 = posPrev.getLongitude().getRadians();
		double lat2 = posNext.getLatitude().getRadians();
		double lon2 = posNext.getLongitude().getRadians();
		double dLon = lon2-lon1;		
		
		// We can have a zero degree heading so minimize errors by
		// not calculating a heading when lons are the same
		if (dLon == 0)
			return -1;
		
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
				Math.sin(lat1) * Math.cos(lat2)*Math.cos(dLon);
		double z = Math.atan2(y,x);
		// convert heading to degrees
		z = (z*(180.0 / Math.PI));
		if (useAbsoluteYaw)
			return yaw;
		// Was orientation set?
		if (yaw > -1)
			z = z + yaw;
		return z;
//		return (z *(180.0 / Math.PI));

	}
	
	public void setSprite(SdtSprite theSprite) 
	{
		// TODO: Should we go ahead "getModel()"/ "getIcon()" here?
		if (null != this.sprite) 
		{
			switch (this.sprite.getType())
			{
				case MODEL:
					model = null;
					break;
				case ICON:
					icon = null;
					break;
				case KML:
					colladaRoot = null;
					break;
			default:
				break;
			}
		}
		this.sprite = theSprite;
	}
	public boolean hasSprite() 
	    {
			if (sprite == null || (sprite.getType() == SdtSprite.Type.NONE))
				return false;
			else	
				return (null != sprite);
		}
	public SdtSprite getSprite() 
	    {return this.sprite;}
	
	public void deleteSprite()
	{
		this.sprite = null;  // ljt testing fix
	}
	public void setSymbol(SdtSymbol theSymbol) 
	{
		// TODO: Should we go ahead "getModel()"/ "getIcon()" here?
		if (null != this.symbol) 
		{
			symbol = null;
		}
		this.symbol = theSymbol;
	}	
	public boolean hasSymbol()
    	{return (null != symbol);}

	public WWModel3D getNodeModel()
	{
		return model;
	}
	
	public WWModel3D getModel() 
	{
	    if ((null == model) && hasSprite() && (SdtSprite.Type.MODEL == sprite.getType()))
	    {
	    	model = new WWModel3D(this.sprite.getModel(),this);
	    	// Scale has already been applied to sprite h/w, but not model so do it here.  
	    	model.setSize(sprite.getIconSize().width, sprite.getIconSize().height, sprite.getFixedLength()*sprite.getScale());
	    	// Due to the convoluted way this model code is set up get the length
	    	// that was calculated in the model.setSize() method to set the sprite
	    	// size so symbols get sized correctly!!  (Don't want to break anything right now 
	    	// by fixing this mess)
	    	sprite.setModelSize((int)model.getLength(), (int)model.getLength());
	    	model.setModelLength(sprite.getFixedLength());
 	        model.setUseLighting(sprite.getModel().isUsingLighting());
 	        model.setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);
 	        if (hasSymbol())
 	        	getSymbol().setInitialized(false);
  	    }
	    return model;
	}
	public void setKmlRoot(KMLRoot theKmlRoot)
	{
		kmlRoot = theKmlRoot;
	}
	public void setColladaRoot(ColladaRoot theColladaRoot)
	{
		colladaRoot = theColladaRoot;
	}
	public void setKmlController(KMLController theKmlController)
	{
		kmlController = theKmlController;
	}
	public void getKmlRoot()
	{
		if ((kmlRoot == null) && hasSprite() && (SdtSprite.Type.KML == sprite.getType()))
		{
			String fileName = sprite.getSpriteKml().getFileName();
			try {
				kmlRoot = KMLRoot.createAndParse(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (kmlRoot == null)
			{
				System.out.println("kmlRoot == null!" + fileName);
				return;
			}
		}				
	}
	
	ColladaRoot getColladaRoot()
	{
		if (kmlRoot == null) getKmlRoot();
  		// Set collada root
		if (colladaRoot == null && kmlRoot.getFeature() != null)
		{			  
			KMLAbstractFeature kmlAbstractFeature = kmlRoot.getFeature();
			for (KMLAbstractFeature feature : ((KMLAbstractContainer) kmlAbstractFeature).getFeatures())
			{
				if (feature instanceof KMLPlacemark)
				{
				       List<KMLRenderable> rs = ((KMLPlacemark) feature).getRenderables();
				       if (rs != null)
				       	{
				    	   for (KMLRenderable r : rs)
				    	   {
				    		   if (r instanceof KMLModelPlacemarkImpl)
				    		   {
				    			   if (((KMLModelPlacemarkImpl)r).getColladaRoot() != null)
				    			   {
				    				   colladaRoot = ((KMLModelPlacemarkImpl)r).getColladaRoot();
				    				   if (colladaRoot != null)	
				    					   colladaRoot.setModelScale(getSprite().getSpriteKml().getModelScale());

				    				   return colladaRoot;
				    			   }
				    		   }				    	        
				    	   }
				       	}
				} 
			}
		} // end if feature != null

		// In case we already had it
		if (colladaRoot != null) return colladaRoot;
		return null;
	}

	public KMLController getKmlController()
	{		
		if (kmlRoot == null) getKmlRoot();			
		if (kmlRoot != null && kmlController == null) kmlController = new KMLController(kmlRoot);	
		
		return kmlController;
	}

	public UserFacingIcon getNodeIcon()
	{
		return icon;
	} 
	
	public UserFacingIcon getIcon() 
	{
	    if ((null == icon) && hasSprite() && (SdtSprite.Type.ICON == sprite.getType()))
	    {
            if (sprite.getIconURL() != null)
            {
            	try {
					BufferedImage image = ImageIO.read(sprite.getIconURL());
		           	icon = new UserFacingIcon(image, this.position);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }
            else
            	icon = new UserFacingIcon(sprite.getIconPath(), this.position);
            icon.setHighlightScale(1.5);
            //icon.setToolTipFont(font);  // TODO pretty up with a nice font
            icon.setToolTipText(this.nodeName);
            icon.setToolTipTextColor(java.awt.Color.YELLOW);
            icon.setSize(sprite.getIconSize());
			icon.setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);
 	    }

        return icon;
	}
	
	// node label management
	public void showLabel()
	    {
			showLabel = true;
	    }
	public void hideLabel()
	{
		showLabel = false;
	}
	public boolean hasLabel()
	    {return (null != label);}
	
 
	public boolean showSymbol()
		{return (null != symbol);}
	
	public GlobeAnnotation getLabel() 
	{
	    if (showLabel && (null == label) && position != null)
	    {
	        label = new GlobeAnnotation(getLabelText(), position); // ljt ??? getLabelPosition());
 	        label.getAttributes().setFont(Font.decode("Ariel"));
	        label.getAttributes().setTextColor(Color.BLACK);
	        label.getAttributes().setBackgroundColor(labelColor);
	        label.getAttributes().setScale(.8);  
	        label.getAttributes().setLeaderGapWidth(15);
	        label.getAttributes().setCornerRadius(10);
	        label.getAttributes().setInsets(new Insets(10,10,10,10));
	    
	  /*      label = new GlobeAnnotation(getLabelText(), position); 
	        label.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
	        label.getAttributes().setFrameShape(FrameFactory.SHAPE_RECTANGLE);
	        label.getAttributes().setLeader(FrameFactory.LEADER_TRIANGLE);
	        label.getAttributes().setCornerRadius(0);
 	        label.getAttributes().setFont(Font.decode("Ariel"));
	        label.getAttributes().setTextColor(Color.BLACK);
	        label.getAttributes().setBackgroundColor(labelColor);
	        label.getAttributes().setScale(.8);  // .8
	        label.getAttributes().setLeaderGapWidth(10); // 15
	        label.getAttributes().setDrawOffset(new Point(20,40));
	        //label.getAttributes().setCornerRadius(10);
	        label.getAttributes().setInsets(new Insets(5,5,5,5)); //10,10,10,10));
*/
	    }
	    if (null != label)	 
	    	label.setAlwaysOnTop(true);

	    return label;
	}
	
	public SdtSymbol getSymbol()
	{
		return this.symbol;
	}
	public void removeNodeFromAllLayers()
	{
		// Remove all layers from node and all associated elements
		removeLabelFromLayer();
		if (this.symbol != null)
			this.symbol.removeSymbolFromLayer();
		removeLinksFromLayers();
		// remove node itself 
		removeNodeFromLayer();		
	}
	public void removeLinksFromLayers()
	{
		Map<String, List<SdtLink>> linkTable = getLinkTable();				
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				// Remove the link from any associated layers 
				Iterator<SdtLink> itr = links.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();
					// LJT THIS IS REDUNDANT = We're trying to delete the same link multiple times!!
					//System.out.println("srcNode>" + theLink.getSrcNode().getName() + "dstNode>" + theLink.getDstNode().getName());
					theLink.removeLinkFromLayers();
				}
			}				
		}						
	}
	public void deleteLabel()
	{
		removeLabelFromLayer();
	    showLabel = false;
	    label = null;
	}
	public void removeLabelFromLayer()
	{
		if (!labelLayerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = labelLayerList.elements();
			while (e.hasMoreElements()) {
				SdtCheckboxNode theCheckboxNode = e.nextElement();
				theCheckboxNode.removeLabel(this);
			}
		}
		removeLabelLayer();
	}
	public void deleteSymbol()
	{
		symbol.removeSymbolFromLayer();
		symbol = null;		
	}
	
	public void setLabelText(String text)
	{
		if (text.equals(this.nodeName))
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
	
	// SdtLink stuff
	public void addLinkTo(SdtNode dstNode, SdtLink sdtLink)
    	{
			List<SdtLink> linkList;
			if (linkTable.containsKey(dstNode.getName()))
			{
				linkList = linkTable.get(dstNode.getName());
				linkList.add(sdtLink);
			} else
			{
				linkList = new ArrayList<SdtLink>();
				linkList.add(sdtLink);
				linkTable.put(dstNode.getName(),linkList);
			}
		}
	public List<SdtLink> getLinksTo(SdtNode dstNode)
    	{return linkTable.get(dstNode.getName());}

	public void removeLinkTo(SdtNode dstNode,String linkId)
	{
		List<SdtLink> linkList = linkTable.get(dstNode.getName());

		if (linkId != null && linkId.equalsIgnoreCase("all"))
		{
			linkList.removeAll(linkList);
			if (linkList != null && linkList.isEmpty())
				linkTable.remove(dstNode.getName());	
			return;
		}
		if (linkList == null || linkList.isEmpty()) return;
		if (linkList.iterator() == null) return;
		SdtLink link = null;
		Iterator<SdtLink> itr = linkList.iterator();

		if (linkList != null)
		{
			while (itr != null && itr.hasNext())
			{
				SdtLink tmpLink = itr.next();

				if ((tmpLink.getLinkID() == null && linkId == null) ||
					(linkId != null && tmpLink.getLinkID() != null && tmpLink.getLinkID().equals(linkId)))
				{
					link = tmpLink;
				}
			}					
		}
	    if (link != null)
	    {
	    	link.removeLinkFromLayers();
	    	linkList.remove(link);
	    } 
	    	
		if (linkList != null && linkList.isEmpty())
			linkTable.remove(dstNode.getName());	
	}
	public void removeDirectionalLinkTo(SdtNode dstNode,String linkId,boolean directional)
	{
		List<SdtLink> linkList = linkTable.get(dstNode.getName());

		SdtLink link = null;
		Iterator<SdtLink> itr = null;
		if (linkList != null)		
			itr = linkList.iterator();
		
		if (linkList != null)
		{
			while (itr != null && itr.hasNext())
			{
				SdtLink tmpLink = itr.next();

				if (((tmpLink.getLinkID() == null && linkId == null) ||
					(linkId != null && tmpLink.getLinkID() != null && tmpLink.getLinkID().equals(linkId)))
					&&
					((directional && tmpLink.isDirectional()) ||
					 (!directional && !tmpLink.isDirectional())))
				{
					//System.out.println("Removing link between " + tmpLink.getSrcNode().getName() + " and " + tmpLink.getDstNode().getName() + " linkID " + tmpLink.getLinkID());
					link = tmpLink;
					break;
				}
			}					
		}
	    if (link != null)
	    		linkList.remove(link);
	    
		if (linkList != null && linkList.isEmpty())
		{
			//System.out.println("removing dst node from link table " + dstNode.getName());
			linkTable.remove(dstNode.getName());	
	
		}
	}
	public Map<String, List<SdtLink>> getLinkTable()
    	{return linkTable;}
	// this function doesn't remove any associated markers and linkLabels
	// it is not currently being used, but let's keep it in case
	// we rearchitect.
	protected void finalize(RenderableLayer linkLayer, AnnotationLayer linkLabelLayer)
	{
	    // Iterate through linkTable and remove links from dstNodes' "linkTable"
	    Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
	    java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
	    
	    Collection<String> dstNodes = new ArrayList<String>();
	   
	    // build delete list
	    while (it.hasNext()) 
	    {
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				java.util.Iterator<SdtLink> itr = links.iterator();

				while (itr != null && itr.hasNext())
				{
					dstNodes.add(itr.next().getDstNode(this).getName());
				}					
			}			    	
	    }
	    
	    for (String name : dstNodes)
	    {
	    	List<SdtLink> links = linkTable.get(name);
	    	if (links != null)
	    	{
	    		java.util.Iterator<SdtLink> itr = links.iterator();
	    		while (itr != null && itr.hasNext())
	    		{
	    			SdtLink theLink = itr.next();
	    			Polyline line = theLink.getLine();
	    			if (null != line)
	    				linkLayer.removeRenderable(line);
	    			if (theLink.hasLabel())
	    			{
	    				linkLabelLayer.removeAnnotation(theLink.getLabel());
	    				theLink.removeLabel();
	    			}
	    		}	    		
	    	}	    	
	    	linkTable.remove(name);
	    }	 
	}
	public void removeRenderables(sdt3d.AppFrame theApp)
	{
		// Remove all renderables associated with the node from the wwj layers
		setDrawn(false);
		
		// Remove all links for the node
		Map<String, List<SdtLink>> linkTable = getLinkTable();				
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				Iterator<SdtLink>itr = links.iterator();

				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();
					theLink.removeRenderables(theApp);
				}					
			}							
		}							
						
		// Remove associated sprite,label,symbol,&links from wwj layers
		if (hasLabel()) 
		{
			theApp.getNodeLabelLayer().removeAnnotation(getLabel());
		}		
		
		if (hasSprite()) {
			switch (getSprite().getType()) {
			case MODEL:
				if (getNodeModel() != null)  // ljt otherwise getModel creates one - rework
					theApp.getNodeModelLayer().removeModel(getNodeModel());
				break;
			case ICON:
				if (getNodeIcon() != null) 
					theApp.getNodeIconLayer().removeIcon(getNodeIcon());
				break;
			case KML:
				if (getKmlController() != null)
					theApp.getNodeKmlModelLayer().removeRenderable(getKmlController());
				break;
			case NONE:
				break;
			case INVALID:
				System.out.println("sprite "
						+ getSprite().getName()
						+ " is INVALID!");
				break;
			}
		}
		
		if (hasSymbol())
			theApp.getSymbolLayer().removeSymbol(getSymbol());
		
		theApp.getNodeLayer().removeRenderable(this);
						
	} // end RemoveRenderables
	
	public void drawRenderables(sdt3d.AppFrame theApp)
	{
		if (isDrawn()) return;
		setDrawn(true);
		theApp.getNodeLayer().addRenderable(this);
		if (hasSprite() && hasPosition())
		{
		switch (getSprite().getType()) {
		case MODEL:
			theApp.getNodeModelLayer().addModel(getModel());
			break;
		case ICON:
		{
			theApp.getNodeIconLayer().addIcon(getIcon());
			break;
		}
		case KML:
		{
			if (getKmlController() != null)
				theApp.getNodeKmlModelLayer().addRenderable(getKmlController());
			break;
		}
		case NONE:
			break;
		case INVALID:
			System.out.println("sprite "
					+ getSprite().getName()
					+ " is INVALID!");
			break;
		}
		}
		// trigger symbol recreation
		if (hasSymbol())
		{
			theApp.getSymbolLayer().addSymbol(getSymbol());
		}
		GlobeAnnotation label = getLabel();
		if (null != label)
		{
			theApp.getNodeLabelLayer().addAnnotation(label);
		}

		Map<String, List<SdtLink>> linkTable = getLinkTable();
		if (linkTable != null)
		{
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				Iterator<SdtLink> itr = links.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();
					theLink.drawRenderables(theApp,false);
				}
			}
		}	
		}
	} // Draw renderables
	
	public void returnRenderables(sdt3d.AppFrame theApp)
	{
		setDrawn(true);
		
		if (!nodeInVisibleLayer())
		{
			theApp.getNodeLayer().addRenderable(this);
			if (hasSprite())
			{
				switch (getSprite().getType()) {
				case MODEL:
					theApp.getNodeModelLayer().addModel(getModel());
					break;
				case ICON:
				{
					theApp.getNodeIconLayer().addIcon(getIcon());
					break;
				}
				case KML:
				{
					if (getKmlController() != null)
						theApp.getNodeKmlModelLayer().addRenderable(getKmlController());
					break;
				}
				case NONE:
					break;
				case INVALID:
					System.out.println("sprite "
							+ getSprite().getName()
							+ " is INVALID!");
					break;
				}
			}
		}
		Map<String, List<SdtLink>> linkTable = getLinkTable();
		if (linkTable != null)
		{
		Set<Entry<String, List<SdtLink>>> set = linkTable.entrySet();
		java.util.Iterator<Entry<String, List<SdtLink>>> it = set.iterator();
		while (it.hasNext()) 
		{
			List<SdtLink> links = it.next().getValue();
			if (links != null)
			{
				Iterator<SdtLink> itr = links.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();
				//	System.out.println("Drawing renderables!!");
					// ljt ?? 11/5/11 if (!nodeInVisibleLayer() || !theLink.linkInVisibleLayer())
						// Force drawing the link
						theLink.drawRenderables(theApp,true);
				}
			}
		}	
		// trigger symbol recreation
		if (hasSymbol() && (!nodeInVisibleLayer() || !symbol.symbolInVisibleLayer()))
		{
			getSymbol().setInitialized(false);
			theApp.getSymbolLayer().addSymbol(getSymbol());
		}
		GlobeAnnotation label = getLabel();
		if (null != label && (!nodeInVisibleLayer() ||!labelInVisibleLayer()))
		{
			theApp.getNodeLabelLayer().addAnnotation(label);
		}


		}
	}
}  // end class SdtNode
