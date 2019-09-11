package mil.navy.nrl.sdt3d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwindx.examples.PathPositionColors.ExamplePositionColors;
import gov.nasa.worldwindx.examples.util.DirectedPath;
import mil.navy.nrl.sdt3d.SdtSprite.Type;

public class SdtNode implements Renderable
{
	// Member variables
	private String nodeName;

	private String symbolType;

	private Position position = null;

	private boolean hasFocus = false;

	private boolean hasTrail = false;

	private boolean showTrailPositions = true;

	private int trailLength = -1;

	Color trailColor = Color.WHITE;

	private int trailPosScale = 2;

	private int trailOutlineWidth = 3;

	// The node's orientation is stored here. Model
	// overrides defined in any xml config are stored in the
	// sdtSprite 3d model subclasses
	private double pitch = 0.0;

	private double yaw = 0.0;

	private double roll = 0.0;

	// If yaw is not set we calculate heading from node position changes and use that
	private double heading = 0;

	private boolean useAbsoluteElevation = sdt3d.AppFrame.useAbsoluteElevation();

	private boolean cartesian = false;

	private float cartesianLat = 0.0f;

	private float cartesianLon = 0.0f;

	private boolean followTerrain = true;

	private double altitude = 0;

	private SdtSprite sprite = null;

	// We need unique renderables for each node so we
	// store the sprite icon, model, collada root here
	private UserFacingIcon icon = null;

	private KMLRoot kmlRoot = null;

	private KMLController kmlController = null;

	ColladaRoot colladaRoot = null;

	private boolean feedbackEnabled = sdt3d.AppFrame.followAll();

	private SdtSymbol symbol = null;

	private GlobeAnnotation label = null;

	ArrayList<Position> pathPositions = new ArrayList<Position>();

	Path path = new DirectedPath(pathPositions);

	private boolean showLabel = true;

	private boolean linkUpdate = false;

	private boolean nodeUpdate = false;

	private String labelText = null; // alternative to node name

	private Color labelColor = Color.YELLOW;

	Map<String, List<SdtLink>> linkTable = new TreeMap<String, List<SdtLink>>();

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


	void setLinkUpdate(boolean linkUpdate)
	{
		this.linkUpdate = linkUpdate;
	}


	boolean getLinkUpdate()
	{
		return this.linkUpdate;
	}


	/**
	 * Any node or node attribute change should setNodeUpdate
	 * so attribute changes take effect immediately during the
	 * next node render pass rather than waiting for a position
	 * update.
	 * 
	 * @param nodeUpdate
	 */
	void setNodeUpdate(boolean nodeUpdate)
	{
		this.nodeUpdate = nodeUpdate;
	}


	boolean getNodeUpdate()
	{
		return this.nodeUpdate;
	}


	private String getColorName(Color c)
	{
		String colorName = "RED";
		// Is it a valid hex color?
		/*
		 * add support for hex & rgb values?
		 *
		 * if (c.startsWith("0x") || c.startsWith("0X"))
		 * {
		 * c = c.trim();
		 * String x[] = c.split("0x");
		 * if (x.length < 2 ) {System.out.println("sdt3d::validateColor 0x required!"); return color;}
		 * boolean isHex = x[1].matches("[0-9A-Fa-f]+");
		 * if (c.length() != 8 || !isHex)
		 * {
		 * System.out.println("sdt3d::getColor() invalid hex value");
		 * return color;
		 * }
		 * color = Color.decode(c);
		 * return color;
		 * }
		 * // Is it a valid rgb value?
		 * String[] rgbVal = c.split(":");
		 * if (rgbVal.length > 1)
		 * {
		 * if (!(rgbVal.length == 3)) {System.out.println("getColor() Invalid color rgb values"); return color;}
		 * if ((Integer.valueOf(rgbVal[0]) < 0 || Integer.valueOf(rgbVal[0]) > 255
		 * || Integer.valueOf(rgbVal[1]) < 0 || Integer.valueOf(rgbVal[1]) > 255
		 * || Integer.valueOf(rgbVal[2]) < 0 || Integer.valueOf(rgbVal[2]) > 255))
		 * {System.out.println("getColor() Invalid color rgb values"); return color;}
		 * color = new Color(Integer.valueOf(rgbVal[0]),Integer.valueOf(rgbVal[1]),Integer.valueOf(rgbVal[2]));
		 * return color;
		 * }
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
			cmd = cmd + this.sprite.getName();
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

				lat = lat.substring(0, lat.length() - 1);
				lon = lon.substring(0, lon.length() - 1);
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
			cmd = cmd + getColorName(labelColor) + "," + getLabelText();
		cmd = cmd + "::";

		// symbol
		if (getSymbolType() != null && getSymbol() != null)
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
				cmd = cmd + this.pitch;
			else
				cmd = cmd + "x";

			if (getYaw() != null)
			{
				cmd = cmd + "," + this.yaw;
				if (this.sprite.useAbsoluteYaw())
					cmd = cmd + "a";
				else
					cmd = cmd + "r";
			}
			else
				cmd = cmd + ",x";

			if (getRoll() != null)
			{
				cmd = cmd + "," + this.roll;
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


	public void setFeedbackEnabled(boolean feedbackEnabled)
	{
		this.feedbackEnabled = feedbackEnabled;
		if (this.icon != null)
			this.icon.setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);

		if (this.sprite != null && this.sprite instanceof SdtSpriteModel)
			((SdtSpriteModel) this.sprite).setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);

		if (this.kmlController != null)
			this.kmlController.setValue(AVKey.FEEDBACK_ENABLED, feedbackEnabled);

	}


	public void setTrail(boolean makeTrail)
	{
		// Create and set an attribute bundle. Specify only the path's outline width; the position colors override
		// the outline color and opacity.
		if (makeTrail)
		{
			ShapeAttributes attrs = new BasicShapeAttributes();
			attrs.setOutlineWidth(trailOutlineWidth);
			// TODO: set stipple attribute?
			// attrs.setOutlineStipplePattern((short) 0xAAAA);
			// attrs.setOutlineStippleFactor(8);

			path.setAttributes(attrs);
		}
		else
		{
			// TODO: add trail clear command
			pathPositions = new ArrayList<Position>();

		}

		hasTrail = makeTrail;
	}


	public void deleteTrail()
	{
		pathPositions = new ArrayList<Position>();
		path = new Path(pathPositions);
		setTrail(false);

	}


	public boolean hasTrail()
	{
		if (hasTrail && pathPositions.size() > 0)
			return hasTrail;
		return false;
	}


	public void setTrailColor(Color theColor)
	{
		trailColor = theColor;
	}


	public void showTrailPosition(boolean showPos)
	{
		showTrailPositions = showPos;
	}


	public void addLayer(String val, SdtCheckboxNode theCheckboxNode)
	{
		if (!layerList.containsKey(val))
		{
			layerList.put(val, theCheckboxNode);
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
		/*
		 * return (!layerList.isEmpty() ||
		 * !labelLayerList.isEmpty() ||
		 * (this.getSymbol() != null && !this.getSymbol().getLayerList().isEmpty()));
		 */
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
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theCheckboxNode = e.nextElement();
				theCheckboxNode.removeNode(this);
			}
		}
	}


	public void addLabelLayer(String val, SdtCheckboxNode theCheckboxNode)
	{
		if (!labelLayerList.containsKey(val))
		{
			labelLayerList.put(val, theCheckboxNode);
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
			while (e.hasMoreElements())
			{
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
			while (e.hasMoreElements())
			{
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


	public UserFacingIcon getIcon()
	{
		if (this.icon == null)
			this.icon = sprite.getIcon(this.position, this.nodeName, feedbackEnabled);

		return this.icon;
	}


	public SdtSpriteModel getModel()
	{
		// Each node needs to have a copy of its model
		if (this.sprite instanceof SdtSpriteModel)
			return (SdtSpriteModel) this.sprite;
		else
			return null;
	}


	public KMLController getKmlController()
	{
		// Each node needs to have a copy of its kmlController, colladaRoot, and kmlRoot
		if (this.kmlRoot == null)
			this.kmlRoot = ((SdtSpriteKml) sprite).initializeKmlRoot();

		if (this.kmlRoot != null && this.kmlController == null)
			this.kmlController = new KMLController(this.kmlRoot);

		return kmlController;
	}


	/**
	 * Sdt nodes are put in a dummy layer called during each rendering pass to
	 * centralize updating the positions of all the node renderables (icon,
	 * symbol, etc.)
	 * 
	 * Check to make sure the node is in a visible layer and that there has
	 * been a node or a position update. Position updates also trigger the
	 * recreation of any links associated with the node.
	 * 
	 * The nodeUpdate boolean tells us that a node object or any of its
	 * attributes such as position, orientation, symbol etc. has changed.
	 *
	 * The linkUpdate boolean tells us that a link or any of its attributes
	 * (e.g. line color) has changed. Note that we need to go through the
	 * position calculations to determine agl, msl etc. before updating the
	 * link positions.
	 */
	@Override
	public void render(DrawContext dc)
	{
		if (!nodeInVisibleLayer()
			||
			!(getNodeUpdate() || getLinkUpdate()))
			return;

		Position oldPos = position;
		if (position == null)
			return;

		// Get the latest globe elevation and update the nodes elevation
		double globeElevation = dc.getGlobe().getElevation(position.getLatitude(), position.getLongitude());
		Position terrainElevation = new Position(position, globeElevation);
		Position aglPosition = new Position(position, getAltitude() + globeElevation);
		Position mslPosition = new Position(position, altitude);

		if (hasSprite())
		{
			switch (sprite.getType())
			{
				case ICON:

					if (getFollowTerrain())
					{
						if (this.icon != null)
							this.icon.setPosition(terrainElevation);
						position = terrainElevation;
					}
					else
					{
						if (!getUseAbsoluteElevation())
						{
							if (this.icon != null)
								this.icon.setPosition(aglPosition);
							position = aglPosition;
						}
						else
						{
							// else we're at absolute elevation
							if (this.icon != null)
								this.icon.setPosition(mslPosition);
							position = mslPosition;
						}
					}
					break;
				case MODEL:
					if (!getFollowTerrain())
					{
						if (!getUseAbsoluteElevation())
							position = aglPosition;
						else
							position = mslPosition;
					}
					else
					{
						double elevation = dc.getGlobe().getElevation(position.getLatitude(), position.getLongitude());
						if (this.sprite.isRealSize())
							elevation += (((SdtSpriteModel) this.sprite).getHeight() / 2.0);
						else
						{
							Vec4 loc = dc.getGlobe().computePointFromPosition(position);
							double localSize = (((SdtSpriteModel) this.sprite).computeSizeScale(dc, loc));
							elevation += localSize * 4;
						}
						position = new Position(position.getLatitude(), position.getLongitude(), elevation);
					}

					// The model3DLayer rendering code gets model position from the model sprite
					((SdtSpriteModel) this.sprite).setPosition(position);

					// For 3d models the sprite _IS_ the model so set p/y/r
					this.sprite.setHeading(this.heading, this.yaw, null);
					this.sprite.setRoll(this.roll);
					this.sprite.setPitch(this.pitch);
					break;
				case KML:
					if (this.colladaRoot == null)
						this.colladaRoot = ((SdtSpriteKml) this.sprite).getColladaRoot(this.kmlRoot);

					if (this.colladaRoot != null)
					{
						if (!getUseAbsoluteElevation())
							position = aglPosition;
						else
							position = mslPosition;
						colladaRoot.setPosition(position);
						((SdtSpriteKml) this.sprite).computeSizeScale(dc, this.colladaRoot, position);
						// This heading code could be cleaned up now that everything is working
						this.sprite.setHeading(this.yaw, this.heading, this.colladaRoot);
						// kml roll is the reverse of models (and our default)
						this.colladaRoot.setRoll(Angle.fromDegrees(-(this.roll + this.sprite.getModelRoll())));
						this.colladaRoot.setPitch(Angle.fromDegrees(this.pitch + this.sprite.getModelPitch()));
					}
					break;
				case NONE:
					// TBD: Do we really want to return here?
					System.out.println("SdtNode::Render() WARNING No valid sprite type assigned\n");
					return;
				default:
					System.out.println("SdtNode::Render() WARNING No valid sprite type assigned\n");
					return;
			}
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

				// LJT TEST - do this for kml too?
				if (this.sprite != null && this.sprite instanceof SdtSpriteModel
					&& ((SdtSpriteModel) this.sprite).isRealSize())
					alt += ((SdtSpriteModel) this.sprite).getHeight() / 2.0;
				getLabel().setPosition(new Position(position, alt));
			}
			else
			{ // Annotations always assume the elevation is the offset above
				// ground level. So we need to subtract the globe elevation
				// from the intended altitude if we're set to msl
				if (useAbsoluteElevation)
				{
					double altOffset = altitude - globeElevation;
					getLabel().setPosition(new Position(position, altOffset));
				}
				else
					getLabel().setPosition(new Position(position, altitude));
			}
		}

		if (hasSymbol())
		{
			// Update symbol coordinates
			getSymbol().updatePosition(dc);
		}

		if (!oldPos.equals(position) || getLinkUpdate())
		{
			updateLinkPositions(dc);
			setLinkUpdate(false);
		}

		setNodeUpdate(false);

	} // SdtNode::Render()


	public boolean hasPosition()
	{
		if (!nodeInVisibleLayer())
			return false;
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
		if (!hasPosition())
			return false;

		if (hasLabel())
			return true;

		if (hasSymbol())
			return true;

		if (hasSprite())
		{
			switch (sprite.getType())
			{
				case MODEL:
					return (this.sprite instanceof SdtSpriteModel);
				case ICON:
					return (null != this.icon);
				case KML:
					return (null != this.colladaRoot);
				case NONE:
					return false;
				default:
					return false;
			}
		}
		return false;

	}


	public double getAltitude()
	{
		return altitude;
	}


	public void setAltitude(double theAltitude)
	{
		altitude = theAltitude;
	}


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


	static double normalize(double i)
	{
		// find effective angle
		double d = Math.abs(i) % 360;

		if (i < 0)
			// return positive equivalent
			return 360 - d;
		else
			return d;

	} // normalize


	public void setDirected(boolean directed)
	{
		if (directed)
			path = new DirectedPath(pathPositions);
		else
			path = new Path(pathPositions);
		// Reset path attributes
		ShapeAttributes attrs = new BasicShapeAttributes();
		attrs.setOutlineWidth(trailOutlineWidth);
		path.setAttributes(attrs);

		path = getPath();
	}


	public void setTrailPosScale(int scale)
	{
		trailPosScale = scale;
		path.setShowPositionsScale(trailPosScale);

	}


	public void setTrailOutlineWidth(int width)
	{
		trailOutlineWidth = width;
		ShapeAttributes attrs = new BasicShapeAttributes();
		attrs.setOutlineWidth(trailOutlineWidth);

		path.setAttributes(attrs);
	}


	public Path getPath()
	{
		// Configure the path to draw its outline and position points in the colors below. We use colors that
		// are evenly distributed along the path's length and gradually increasing in opacity.

		Color[] colors = {
			new Color(trailColor.getRed(), trailColor.getGreen(), trailColor.getBlue(), 75),
			new Color(trailColor.getRed(), trailColor.getGreen(), trailColor.getBlue(), 125),
			new Color(trailColor.getRed(), trailColor.getGreen(), trailColor.getBlue(), 200),
			new Color(trailColor.getRed(), trailColor.getGreen(), trailColor.getBlue(), 250),

		};

		// TODO: set clamp to ground based on alt etc.
		path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
		path.setFollowTerrain(true);
		path.setShowPositions(showTrailPositions);
		path.setShowPositionsScale(trailPosScale);
		// To ensure that the arrowheads resize smoothly, refresh each time the path is drawn.
		path.setVisible(true);
		path.setPathType(AVKey.GREAT_CIRCLE);

		path.setPositions(pathPositions);
		path.setPositionColors(new ExamplePositionColors(colors, pathPositions.size()));

		return path;
	}


	public void updateTrail(Position pos)
	{
		// TODO: Make trail length configurable
		if (trailLength != -1 && pathPositions.size() > trailLength)
			pathPositions.subList(0, (pathPositions.size() - trailLength)).clear();
		pathPositions.add(pos);

	}


	public void setTrailLength(int length)
	{
		trailLength = length;
	}


	public void setPosition(Position pos)
	{
		if (hasTrail && pos != position && pos != null)
			updateTrail(pos);

		Position oldPos = position;
		this.position = pos;

		// Set our heading if pos changed and flag to recreate links
		if ((null != oldPos) && (!pos.equals(oldPos)))
		{
			this.computeHeading(this.position, oldPos);
			setLinkUpdate(true);
		}

	} // setPosition


	public Position getPosition()
	{
		if (!nodeInVisibleLayer())
			return null;
		return position;
	}


	public double getHeading()
	{
		return this.heading;
	}


	public double getSymbolHeading()
	{
		if (this.sprite.useAbsoluteYaw())
			return this.getYaw();
		else
			return (this.heading + this.getYaw());

	}


	public void recreateLinkLabels(sdt3d.AppFrame theApp)
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
							{
								theLink.getLinkLabelLayer().removeAnnotation(theLink.getLabel());
							}
							theLabel.setText(theLink.getLabelText());
							theLink.isCollapsed(false);
							theLink.getLinkLabelLayer().addAnnotation(theLink.getLabel());
						}
					}
					else if (sdt3d.AppFrame.collapseLinks && theLink.hasLabel())
					{
						theLink.getLinkLabelLayer().removeAnnotation(theLink.getLabel());
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

							Position pos = Position.interpolate(.5, theLink.getSrcNode().getPosition(), theLink.getDstNode().getPosition());
							theLink.getLabel().setPosition(pos);
							theLink.getLinkLabelLayer().addAnnotation(theLink.getLabel());
							break;
						}
					}
				}
			}
		}

	}


	public void updateLinkPositions(DrawContext dc)
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
				int numLinks = 0;
				int displayedLinks = 0;

				// Get the total number of displayed links so we know how
				// to far apart to splay multiple links
				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();

					if (theLink.linkInVisibleLayer())
					{
						displayedLinks++;
					}
				}

				// Update the positions of each displayed link
				itr = links.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();

					if (theLink.linkInVisibleLayer())
					{
						numLinks++;

						theLink.updatePositions(dc, numLinks, displayedLinks);
					}
				}
			}
		}
	}


	public void setPitch(Double pitch)
	{
		this.pitch = pitch;

	}


	public Double getPitch()
	{
		return this.pitch;
	}


	// Set yaw, or "heading"
	public void setYaw(Double yaw)
	{
		this.yaw = yaw;
	}


	public Double getYaw()
	{
		return this.yaw;
	}


	public void setRoll(Double roll)
	{
		this.roll = roll;
	}


	public Double getRoll()
	{
		return this.roll;
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
		double dLon = lon2 - lon1;

		// We can have a zero degree heading so minimize errors by
		// not calculating a heading when lons are the same
		if (dLon == 0)
			return 0.0;

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) -
			Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		double z = Math.atan2(y, x);
		// convert heading to degrees
		double heading = (z * (180.0 / Math.PI));
		// Convert to navigational heading
		heading = normalize(heading);
		heading = heading + 180;

		return heading;
	} // computeHeading


	public void setSprite(SdtSprite theSprite)
	{
		// In case we are changing the model type
		this.colladaRoot = null;
		this.kmlRoot = null;
		this.kmlController = null;

		// The node needs to have a unique Model to render so
		// copy the one the sprite has loaded. We get a little savings by loading the
		// model just once when we loaded the sprite initially.
		if (theSprite instanceof SdtSpriteModel)
		{
			this.sprite = new SdtSpriteModel((SdtSpriteModel) theSprite);
		}
		else
			this.sprite = theSprite;

		if (hasSymbol())
			getSymbol().setInitialized(false);

	}


	public boolean hasSprite()
	{
		if (sprite == null || (sprite.getType() == SdtSprite.Type.NONE))
			return false;
		else
			return (null != sprite);
	}


	public SdtSprite getSprite()
	{
		return this.sprite;
	}


	public void deleteSprite()
	{
		this.sprite = null; // ljt testing fix
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
	{
		return (null != symbol);
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
	{
		return (null != label);
	}


	public boolean showSymbol()
	{
		return (null != symbol);
	}


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
			label.getAttributes().setInsets(new Insets(10, 10, 10, 10));

			/*
			 * label = new GlobeAnnotation(getLabelText(), position);
			 * label.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
			 * label.getAttributes().setFrameShape(FrameFactory.SHAPE_RECTANGLE);
			 * label.getAttributes().setLeader(FrameFactory.LEADER_TRIANGLE);
			 * label.getAttributes().setCornerRadius(0);
			 * label.getAttributes().setFont(Font.decode("Ariel"));
			 * label.getAttributes().setTextColor(Color.BLACK);
			 * label.getAttributes().setBackgroundColor(labelColor);
			 * label.getAttributes().setScale(.8); // .8
			 * label.getAttributes().setLeaderGapWidth(10); // 15
			 * label.getAttributes().setDrawOffset(new Point(20,40));
			 * //label.getAttributes().setCornerRadius(10);
			 * label.getAttributes().setInsets(new Insets(5,5,5,5)); //10,10,10,10));
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


	public void removeNodeFromAllLayers(sdt3d.AppFrame theApp)
	{
		// Remove all layers from node and all associated elements
		removeLabelFromLayer();
		if (this.symbol != null)
			this.symbol.removeSymbolFromLayer();
		removeLinksFromLayers(theApp);
		// remove node itself
		removeNodeFromLayer();
	}


	public void removeLinksFromLayers(sdt3d.AppFrame theApp)
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
					theLink.removeRenderables();
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
			while (e.hasMoreElements())
			{
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
	{
		return ((null != labelText) ? labelText : getName());
	}


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
		}
		else
		{
			linkList = new ArrayList<SdtLink>();
			linkList.add(sdtLink);
			linkTable.put(dstNode.getName(), linkList);
		}
	}


	public List<SdtLink> getLinksTo(SdtNode dstNode)
	{
		return linkTable.get(dstNode.getName());
	}


	public void removeLinkTo(SdtNode dstNode, String linkId)
	{
		List<SdtLink> linkList = linkTable.get(dstNode.getName());

		if (linkId != null && linkId.equalsIgnoreCase("all"))
		{
			linkList.removeAll(linkList);
			if (linkList != null && linkList.isEmpty())
				linkTable.remove(dstNode.getName());
			return;
		}
		if (linkList == null || linkList.isEmpty())
			return;
		if (linkList.iterator() == null)
			return;
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


	public void removeDirectionalLinkTo(SdtNode dstNode, String linkId, boolean directional)
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
					// System.out.println("Removing link between " + tmpLink.getSrcNode().getName() + " and " +
					// tmpLink.getDstNode().getName() + " linkID " + tmpLink.getLinkID());
					link = tmpLink;
					link.removeRenderables();
					link.removeLinkFromCheckbox(link);
					break;
				}
			}
		}
		if (link != null)
			linkList.remove(link);

		if (linkList != null && linkList.isEmpty())
		{
			// System.out.println("removing dst node from link table " + dstNode.getName());
			linkTable.remove(dstNode.getName());

		}
	}


	public Map<String, List<SdtLink>> getLinkTable()
	{
		return linkTable;
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
				Iterator<SdtLink> itr = links.iterator();

				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();
					theLink.removeRenderables();
				}
			}
		}

		// Remove associated sprite,label,symbol,&links from wwj layers
		if (hasLabel())
		{
			theApp.getNodeLabelLayer().removeAnnotation(getLabel());
		}

		if (hasSprite())
		{
			switch (getSprite().getType())
			{
				case MODEL:
					if (this.sprite instanceof SdtSpriteModel)
						theApp.getNodeModelLayer().removeModel((SdtSpriteModel) this.sprite);
					break;
				case ICON:
					if (this.icon != null)
						theApp.getNodeIconLayer().removeIcon(this.icon);
					break;
				case KML:
					if (this.kmlController != null)
						theApp.getNodeKmlModelLayer().removeRenderable(this.kmlController);
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

		if (hasTrail())
			theApp.getTrailLayer().removeRenderable(getPath());

		theApp.getNodeLayer().removeRenderable(this);

	} // end RemoveRenderables


	public void drawRenderables(sdt3d.AppFrame theApp)
	{
		if (isDrawn())
			return;

		setDrawn(true);
		theApp.getNodeLayer().addRenderable(this);
		if (hasTrail())
		{
			theApp.getTrailLayer().removeRenderable(getPath());
			theApp.getTrailLayer().addRenderable(getPath());
		}
		if (hasSprite() && hasPosition())
		{
			// LJT TEST check for instance?
			switch (getSprite().getType())
			{
				case MODEL:
					if (this.sprite instanceof SdtSpriteModel)
						theApp.getNodeModelLayer().addModel((SdtSpriteModel) this.sprite);
					break;
				case ICON:
				{
					if (this.icon != null)
						theApp.getNodeIconLayer().addIcon(this.icon);
					break;
				}
				case KML:
				{
					if (this.kmlController != null)
						theApp.getNodeKmlModelLayer().addRenderable(this.kmlController);
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
						theLink.drawRenderables(false);
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
			if (hasTrail())
			{
				// LJT TESTING
				theApp.getTrailLayer().removeRenderable(getPath());
				theApp.getTrailLayer().addRenderable(getPath());
			}
			if (hasSprite())
			{
				switch (getSprite().getType())
				{
					case MODEL:
						if (this.sprite instanceof SdtSpriteModel)
							theApp.getNodeModelLayer().addModel((SdtSpriteModel) this.sprite);
						break;
					case ICON:
					{
						if (this.icon != null)
							theApp.getNodeIconLayer().addIcon(this.icon);
						break;
					}
					case KML:
					{
						if (this.kmlController != null)
							theApp.getNodeKmlModelLayer().addRenderable(this.kmlController);
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
						theLink.drawRenderables(true);
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
			if (null != label && (!nodeInVisibleLayer() || !labelInVisibleLayer()))
			{
				theApp.getNodeLabelLayer().addAnnotation(label);
			}

		}
	}
} // end class SdtNode
