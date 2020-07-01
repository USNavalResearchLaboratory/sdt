/*
 /*
 * sdt3d.java
 *
 * Created on February 12, 2008, 10:47 PM
 *
 * To change this template, choose Tools | Template Managers
 * and open the template in the editor.
 * WWJ code:
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
*/
package mil.navy.nrl.sdt3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.util.awt.Screenshot;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.AnimationSupport;
import gov.nasa.worldwind.animation.BasicAnimator;
import gov.nasa.worldwind.animation.Interpolator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.ExtentHolder;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.SkyColorLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.AbstractBrowserBalloon;
import gov.nasa.worldwind.render.BalloonAttributes;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.GlobeBrowserBalloon;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.terrain.BathymetryFilterElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.KMLLayerTreeNode;
import gov.nasa.worldwind.util.layertree.KMLNetworkLinkTreeNode;
import gov.nasa.worldwind.util.layertree.LayerTree;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.FlatOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.applications.sar.OSXAdapter;
import gov.nasa.worldwindx.examples.WMSLayersPanel;
import gov.nasa.worldwindx.examples.util.BalloonController;
import gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport;
import gov.nasa.worldwindx.examples.util.HotSpotController;

// Google protoBuf example
//import mil.navy.nrl.sdtCommands.sdtCommandsProtos;

/**
 * 
 * @author Brian Adamson, Laurie Thompson
 */

public class sdt3d extends SdtApplication
{
	// "Override" of SdtApplication.AppFrame.start
	public static AppFrame start(String appName, Class<AppFrame> appFrameClass,
			String[] args)
	{
		// TODO Auto-generated method stub
		if (Configuration.isMacOS() && appName != null)
		{
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
		}

		try
		{
			final AppFrame frame = appFrameClass.getDeclaredConstructor(String[].class).newInstance((Object) args);

			frame.setTitle(appName);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			java.awt.EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						frame.setVisible(true);
						if (frame.sharedFrame != null)
							frame.sharedFrame.setVisible(true);
					}
				});

			return frame;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	// Our AppFrame will maintain most of the state and handle commands, etc
	protected static class AppFrame extends SdtApplication.AppFrame
			implements
			ActionListener
	{
		/**
		 * 
		 */
		// TODO: ljt properly implement
		private FileWriter fileWriter = null;

		private PrintWriter logDebugFile = null;

		private String version = "2.2";

		private String wwjVersion = "2.1d";

		private JFrame popupFrame = new JFrame("sdt3d popUp");

		private SdtLayerPanel sdtLayerPanel;

		private StatusPanel statusPanel;

		private ScenarioPlaybackPanel scenarioPlaybackPanel;

		private JSplitPane horizontalSplitPane;

		private JPanel westPanel;

		private JPanel logoPanel;

		private JPanel smallLogoPanel;
		
		private JPanel sdtViewControlsPanel;

		private SdtApplication.AppFrame sharedFrame;

		private static final int logoPixelSize = 120;

		// For wms server layer frame
		private JFrame wmsFrame;

		protected final Dimension wmsPanelSize = new Dimension(300, 500);

		protected JTabbedPane tabbedPane;

		protected int previousTabIndex;

		protected static final String[] servers = new String[] {
			// "http://www2.demis.nl/WMS/wms.asp?WMS=WorldMap&WMTVER=1.0.0&request=capabilities",

			"http://129.206.228.72/cached/osm?",
			"http://ows.terrestris.de/osm/service?",

			// "https://neowms.sci.gsfc.nasa.gov/wms/wms",
			"https://sedac.ciesin.columbia.edu/geoserver/wcs"
		};

		enum ObjectList {
				ALL, NODES, LABELS, SPRITES, SYMBOLS, LINKS, LINKLABELS, TRAILS, REGIONS, TILES, ELEVATIONOVERLAY, GEOTIFF, KML, INVALID
		};

		enum CmdType {
				CMD_INVALID, CMD_ARG, CMD_NOARG
		};

		private final HashMap<Integer, String> int2Cmd = new HashMap<>();
		
		private final HashMap<String, Integer> cmd2Int = new HashMap<>();
				
		String CMD_LIST[] = {
			"+bgbounds",
			"+collapseLinks",
			"+flyto",
			"+zoom",
			"+heading",
			"+pitch",
			"+instance",
			"+sprite",
			"+image",
			"+size",
			"+length",
			"+scale",
			"+node",
			"+type",
			"+position",
			"+pos",
			"+focus",
			"+follow",
			"+center",
			"+path",
			"+label",
			"+symbol",
			"+shape",
			"+region",
			"+delete", // remove node and any links to it
			"+clear", // remove all nodes and links
			"+link",
			"+light",
			"+linklabel",
			"+loadCache",
			"+logDebugOutput",
			"+logfile",
			"+unlink",
			"+line",
			"+wait",
			"+input",
			"+tile",
			"+tileImage",
			"+trail",
			"+sector",
			"+status",
			"+defaultAltitudeType",
			"+listen",
			"-off",
			"+popup",
			"-popdown",
			"+view",
			"+viewSource",
			"+viewXml",
			"+layer",
			"+nodeLayer",
			"+linkLayer",
			"+symbolLayer",
			"+labelLayer",
			"+regionLayer",
			"+tileLayer",
			"+title",
			"+elevationOverlay",
			"+file",
			"+geoTiff",
			"+geoTiffFile",
			"+backgroundColor",
			"+flatEarth",
			"+elevationData",
			"+kml",
			"+kmlFile",
			"+stereo",
			"+offlineMode",
			"+origin",
			"-reset",
			"-resetPerspective",
			"+lookat",
			"+userConfigFile",
			"+symbolOffset",
			"+orientation",
			"+enableSdtViewControls",
			"+showLayerPanel",
			"+showSdtPanel",
			"+showStatusPanel",
			"+showScenarioPlaybackPanel",
			"+showWmsPanel",
			"+showSdtControlPanel",
			"+multiFrame",
			"+recordScenario",
			null
		};

		private String pipe_name = "sdt";

		private boolean pipeCmd = false;

		private SdtSprite currentSprite = null;

		private SdtNode currentNode = null;

		private SdtRegion currentRegion = null;

		private SdtSymbol currentSymbol = null;

		private SdtTile currentTile = null;

		private String currentView = null;

		private String currentGeoTiff = null;

		private SdtSpriteKml currentKml = null;

		private List<SdtLink> currentLinkSet = new ArrayList<SdtLink>();

		private Position focusPosition = null;

		private double focusPitch = 999.0;

		private double focusRoll = 999.0;

		private double focusYaw = 999.0;

		private List<Marker> markers = new ArrayList<Marker>();

		private List<Marker> userDefinedMarkers = new ArrayList<Marker>();

		private SdtModelBuilder elevationBuilder;

		private WWIcon lastPickedIcon;

		StringWriter buffer = new StringWriter();

		PrintWriter out = new PrintWriter(buffer);

		private static final long serialVersionUID = 1L;

		private JMenuBar menuBar;

		private JMenu fileMenu, configFileMenu, viewMenu, layerMenu, bookmarkMenu, globeMenu,
				flatEarthMenu, kmlMenu;

		private JMenuItem openItem, appendItem, loadConfigItem, clearConfigItem, hardResetItem, resetItem, resetPerspectiveItem,
				screenItem, listenUdpItem, listenTcpItem, exitItem, sdtLayersItem, backgroundColorItem,
				showLayersItem, removeUDLayersItem, bookmarkItem,
				loadBookmarkItem, clearSpriteTableItem,
				loadDefaultBookmarksItem, loadKMLFileItem, loadKMLUrlItem, loadCacheItem;

		private JCheckBoxMenuItem showStatusItem, showScenarioPlaybackItem, elevationItem, globeItem, mercatorItem, sinusoidalItem,
				showWmsItem,
				modSinusoidalItem, latLonItem, stereoItem, collapseLinksItem, symbolOffsetItem, offlineModeItem,
				showSdtViewControlsItem, debugItem, showSdtPanelItem, multiFrameItem;

		static boolean collapseLinks = false;

		static boolean symbolOffset = true;

		private boolean multiFrame = false;

		private boolean logDebugOutput = false;
		
		boolean recordScenario = true;
		// clean up these booleans
		boolean playbackOnly = false;
				
		boolean playbackScenario = false;
		
		boolean playbackStopped = false;
				
		private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
		
		// used to calculate wait interval when writing log file
		long lastTime, currentTime = 0;

		private String configDirName = System.getProperty("user.home") + System.getProperty("file.separator") +
			".config" + System.getProperty("file.separator") + "sdt3d";

		private String sdtPropertiesFile = "sdt.properties";

		private String userPreferencesFile = "sdt.settings";

		private String currentConfigFile = null;

		final static JFileChooser fc = new JFileChooser();

		private PipeThread pipeThread = null;

		private SocketThread udpSocketThread = null;

		private SocketThread tcpSocketThread = null;

		private int socketPort = 0;

		private String socketMulticastAddr = "";

		private FileThread fileThread = null;

		private ScenarioThread scenarioThread = null;
		
		SdtApplication.AppFrame getSharedFrame()
		{
			return this.sharedFrame;
		}


		// the pipe thread needs to know if we are reading an input file
		// from the command line - if so, we ignore piped input until
		// it is complete
		public boolean readingCmdInputFile()
		{
			if (fileThread != null)
				return fileThread.readingCmdInputFile();
			else
				return false;
		}

		private Globe roundGlobe;

		private FlatGlobe flatGlobe;

		private String projection = "LAT_LON";

		private BathymetryFilterElevationModel noDepthModel;

		// agl elevation is the default.
		private static boolean useAbsoluteElevation = false;

		private static boolean followAll = false;

		private String focusNode = null;

		private boolean enableSdtViewControls = true;

		protected ViewController viewController;

		private RenderableLayer nodeLayer = null;

		private RenderableLayer trailLayer = null;

		private Model3DLayer nodeModelLayer = null;

		private SdtKmlLayer nodeKmlModelLayer = null;

		private IconLayer nodeIconLayer = null;

		// These could be common but we want to be able to turn them on/off separately
		private AnnotationLayer nodeLabelLayer = null;

		private AnnotationLayer linkLabelLayer = null;

		private AnnotationLayer userDefinedLinkLabelLayer = null;

		private RenderableLayer linkLayer = null;

		private RenderableLayer userDefinedLinkLayer = null;

		private SdtRegionLayer regionLayer = null;

		private SdtSymbolLayer symbolLayer = null;

		private RenderableLayer tileLayer = null;

		private MarkerLayer markerLayer = null;

		private MarkerLayer userDefinedMarkerLayer = null;

		private RenderableLayer kmlPanelLayer = null;

		private static RenderableLayer kmlLayer = null;

		protected LayerTree kmlPanelLayerTree;

		protected HotSpotController hotSpotController;

		protected SdtKMLApplicationController sdtKmlAppController;

		protected SdtKMLViewController sdtKmlViewController;

		protected BalloonController balloonController;
		
		private ScenarioController scenarioController;
		
		private String defaultSprite = null;

		Hashtable<String, SdtNode> nodeTable = new Hashtable<String, SdtNode>();

		Hashtable<String, SdtSprite> spriteTable = new Hashtable<String, SdtSprite>();

		Hashtable<String, SdtRegion> regionTable = new Hashtable<String, SdtRegion>();

		Hashtable<String, String> viewTable = new Hashtable<String, String>();

		Hashtable<String, SdtTile> tileTable = new Hashtable<String, SdtTile>();

		Hashtable<String, SdtSpriteKml> kmlTable = new Hashtable<String, SdtSpriteKml>();

		private String viewState;

		private static File openFile = null;

		private File cacheDir = null;

		private static String imagePathPrefix = null;

		Position originLocation = Position.fromDegrees(0.0, 0.0, 0);

		private Timer pollTimer = null;

		private ViewControlsSelectListener mainSelectListener;

		// move to balloon layer
		private RenderableLayer layer;
		


		public AppFrame(String[] args) 
		{
			// Our main frame is not shared
			super(null, null, true, false, false);
			
			initialize();
			
			CmdParser parser = new CmdParser();

			for (int i = 0; i < args.length; i++)
			{
				onInput(args[i], parser, false);
			}
		}

		
		private void initialize() 
		{			
			// Build application panels, set up controllers and listeners etc.
			buildApplicationGui();
			
			// initialize command maps
			initialize_cmd_maps();
			
			// Start listening for commands on a protopipe
			startProtoPipe();
	
			// Show our config directory in the file chooser
			fc.setFileHidingEnabled(false);
			
			loadDummySprite();

			if (!initializeConfiguration())
			{
				System.out.println("sdt3d::initialize() Error loading configuration files.");
			}
				
			setGlobeType();


			// Start pollTimer to control wwd redraw
			startPollTimer();

		} // end initialize



		protected ViewControlsSelectListener addViewControlLayer(SdtApplication.AppFrame theFrame)
		{
			ViewControlsLayer layer = new ViewControlsLayer();
			layer.setName("View Controls");
			theFrame.wwjPanel.wwd.getModel().getLayers().add(layer);
			
			// Add to our checkbox tree as we are adding these after the
			// worldwind tree is built.
			setLayer("All Layers::Worldwind::View Controls,on");
			ViewControlsSelectListener selectListener = new ViewControlsSelectListener(theFrame.wwjPanel.wwd, layer);
			theFrame.wwjPanel.wwd.addSelectListener(selectListener);
			return selectListener;
		}


		protected void removeViewControlLayer(SdtApplication.AppFrame theFrame)
		{
			for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers())
			{
				if (layer instanceof ViewControlsLayer)
				{
					this.getWwd().getModel().getLayers().remove(layer);
				}
			}
			theFrame.wwjPanel.wwd.removeSelectListener(this.mainSelectListener);
		}


		private void startPollTimer()
		{
			final int POLL_INTERVAL = 100;
			// ...
			pollTimer = new Timer(POLL_INTERVAL, new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						getWwd().redraw();
						if (sharedFrame != null)
						{
							sharedFrame.wwjPanel.wwd.redraw();
						}
						pollTimer.stop(); // one-shot redraw
						
					}
				});

		}

		
		private void initialize_cmd_maps()
		{
			int x = 0;
			for (String cmd : CMD_LIST)
			{
				if (cmd == null)
				{
					continue;
				}
				x++;
				// Load our cmd maps and remove the leading +/- 
				cmd2Int.put(cmd.substring(1), x);
				int2Cmd.put(x, cmd.substring(1));
			}
		}

		
		private void createViewController()
		{
			// We extended KMLViewController to provide gx:tour support
			sdtKmlViewController = new SdtKMLViewController(this.getWwd());

			// Set up a view controller to keep the nodes in view.
			this.viewController = new ViewController(getWwd());
			this.viewController.setIcons(getNodeIconLayer().getIcons());
			this.viewController.setModels(getNodeModelLayer().getModels());
			this.viewController.setKmlModels(getNodeKmlModelLayer().getRenderables());

		}
		
		private void createElevationBuilder()
		{
			// Create SdtElevationBuilder
			final Model model = this.getWwd().getModel();
			final Globe globe = model.getGlobe();
			// TODO: LJT Deal with flat/round globe changes

			ElevationModel firstElev = globe.getElevationModel();
			CompoundElevationModel compElev = new CompoundElevationModel();
			if (firstElev != null)
			{
				compElev.addElevationModel(firstElev);
			}
			globe.setElevationModel(compElev);
			RenderableLayer imageLayer = new RenderableLayer();
			imageLayer.setName("Elevation");
			imageLayer.setPickEnabled(false);
			this.elevationBuilder = new SdtModelBuilder(compElev, imageLayer);
			insertBeforeCompass(getWwd(), imageLayer);
		}
		
		
		private void eliminateBathymetryData()
		{
			// Eliminate bathymetry elevation data otherwise
			// links get rendered under the ocean...
			ElevationModel currentElevationModel = this.getWwd().getModel().getGlobe().getElevationModel();
			noDepthModel = new BathymetryFilterElevationModel(currentElevationModel);
			this.getWwd().getModel().getGlobe().setElevationModel(noDepthModel);
			// this.getWwd().getSceneController().setVerticalExaggeration(5d);
		}
		
		
		private void createAppPanels() 
		{
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);

			// create the west panel
			this.westPanel = new JPanel(new BorderLayout());

			// Create logo image
			logoPanel = new JPanel(new BorderLayout());
			java.net.URL imageURL = getClass().getResource("/images/sdtLogo.jpg");

			ImageIcon image = null;
			if (imageURL != null)
			{
				image = new ImageIcon(imageURL);
				image = new ImageIcon(image.getImage().getScaledInstance(logoPixelSize, logoPixelSize, 0));
			}
			logoPanel.add(new JLabel(image));

			// Create small logo panel for use by popup frame
			smallLogoPanel = new JPanel(new BorderLayout());
			imageURL = getClass().getResource("/images/sdtLogo.jpg");

			image = null;
			if (imageURL != null)
			{
				image = new ImageIcon(imageURL);
				image = new ImageIcon(image.getImage().getScaledInstance(64, 64, 0));
			}
			smallLogoPanel.add(new JLabel(image));

			// Must put the components in a container to prevent scroll panel from stretching their vertical spacing.
			JPanel dummyPanel = new JPanel(new BorderLayout());
			dummyPanel.add(this.logoPanel, BorderLayout.NORTH);

			this.statusPanel = new StatusPanel();
			dummyPanel.add(statusPanel, BorderLayout.CENTER);

			this.sdtLayerPanel = new SdtLayerPanel(getWwd());
			this.sdtLayerPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), new TitledBorder("Layers")));
			this.sdtLayerPanel.setBackground(westPanel.getBackground());

			// Set up view controls checkbox
			this.sdtViewControlsPanel = new JPanel(new BorderLayout());
			final JCheckBox sdtViewControlsCheckbox = new JCheckBox("SDT view controls");
			sdtViewControlsCheckbox.setSelected(true);
			sdtViewControlsCheckbox.setHorizontalTextPosition(SwingConstants.LEFT);
			this.enableSdtViewControls = sdtViewControlsCheckbox.isSelected();
			sdtViewControlsCheckbox.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent actionEvent)
					{
						if (sdtViewControlsCheckbox.isSelected())
							setEnableSdtViewControls("on");
						else
							setEnableSdtViewControls("off");
					}
				});
			this.sdtViewControlsPanel.add(sdtViewControlsCheckbox);
			JPanel dummySVCPanel = new JPanel(new BorderLayout());
			dummySVCPanel.add(this.sdtViewControlsPanel, BorderLayout.EAST);

			this.westPanel.add(dummyPanel, BorderLayout.NORTH);
			this.westPanel.add(sdtLayerPanel);
			this.westPanel.add(dummySVCPanel, BorderLayout.SOUTH);

			// Create a horizontal split pane containing the west(sdt) panel and the WorldWindow panel.
			horizontalSplitPane = new JSplitPane();
			horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			// Make sure divider can be dragged all the way to both sides
			Dimension minimumSize = new Dimension(0, 0);
			westPanel.setMinimumSize(minimumSize);
			wwjPanel.setMinimumSize(minimumSize);
			horizontalSplitPane.setLeftComponent(westPanel);
			horizontalSplitPane.setRightComponent(wwjPanel);
			horizontalSplitPane.setOneTouchExpandable(true);
			horizontalSplitPane.setContinuousLayout(true); // prevents the pane's being obscured when expanding right

			this.getContentPane().add(horizontalSplitPane, BorderLayout.CENTER);
			this.scenarioPlaybackPanel = new ScenarioPlaybackPanel(); 
			this.getContentPane().add(scenarioPlaybackPanel, BorderLayout.SOUTH);
			this.scenarioPlaybackPanel.setVisible(true);
			
			this.pack();

			// Center the application on the screen.
			WWUtil.alignComponent(null, this, AVKey.CENTER);
			this.setResizable(true);

			buildMenuBar();

			// Create a layer tree to store the kml nodes
			this.kmlPanelLayerTree = new LayerTree();
			this.kmlPanelLayer.addRenderable(this.kmlPanelLayerTree);

			WorldWind.setOfflineMode(false);
			
			// We need to store the main select listener so we can
			// remove it if we create a multi-frame.

			this.mainSelectListener = this.addViewControlLayer(this);

		}
				
		
		private void addControllers()
		{	
			// Add a controller to handle input events on the layer selector and on browser balloons.
			this.hotSpotController = new HotSpotController(this.getWwd());
			// Add a controller to handle common KML application events.
			this.sdtKmlAppController = new SdtKMLApplicationController(this.getWwd());
			// Add a controller to display balloons when placemarks are clicked. We override the method addDocumentLayer
			// so that loading a KML document by clicking a KML balloon link displays an entry in the on-screen layer
			// tree.
			this.balloonController = new BalloonController(this.getWwd())
				{
					@Override
					protected void addDocumentLayer(KMLRoot document)
					{
						addKMLLayer(document);
					}
				};

			// Give the KML app controller a reference to the BalloonController so that the app controller can open
			// KML feature balloons when feature's are selected in the on-screen layer tree.
			this.sdtKmlAppController.setBalloonController(balloonController);

			// Add a controller to interact with our scenario panel
			this.scenarioController = new ScenarioController(this, scenarioPlaybackPanel);

		}
		
		
		private void buildApplicationGui() 
		{
			createDefaultSdtLayers(getWwd());

			createElevationBuilder();
			
			createViewController();
			
			createAppPanels();
			
			eliminateBathymetryData();
			
			addControllers();
			
			createSelectListener();
			
			createPropertyChangeListener();

		}
		
		/**
		* Load dummy icon as the default sprite. This ensures that nodes with no assigned
		* sprites can be followed and have associated symbols. As the first entry in the
		* sprite table it will be used as the default sprite.
		*/
		private void loadDummySprite()
		{
			java.net.URL dummyIcon = getClass().getResource("/images/dummyIcon.png");
			if (dummyIcon != null)
			{
				setSprite("default");
				try
				{
					currentSprite.LoadURL(dummyIcon);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					System.out.println("sdt3d::loadDummySprite() Error loading default sprite.");
					e.printStackTrace();
				}
			}
			

		}
		
		
		private void setGlobeType()
		{
			// TODO: ljt change model for multiframe
			// TODO: LJT Deal with flat/round globe changes in multiframe?
			if (isFlatGlobe())
			{
				this.flatGlobe = (FlatGlobe) getWwd().getModel().getGlobe();
				this.roundGlobe = new Earth();
			}
			else
			{
				this.flatGlobe = new EarthFlat();
				this.roundGlobe = getWwd().getModel().getGlobe();
			}
		}
		

		protected void buildWmsLayerFrame()
		{
			this.tabbedPane = new JTabbedPane();

			this.tabbedPane.add(new JPanel());
			this.tabbedPane.setTitleAt(0, "+");
			this.tabbedPane.addChangeListener(new ChangeListener()
				{
					@Override
					public void stateChanged(ChangeEvent changeEvent)
					{
						if (tabbedPane.getSelectedIndex() != 0)
						{
							previousTabIndex = tabbedPane.getSelectedIndex();
							return;
						}

						String server = JOptionPane.showInputDialog("Enter wms server URL");
						if (server == null || server.length() < 1)
						{
							tabbedPane.setSelectedIndex(previousTabIndex);
							return;
						}

						// Respond by adding a new WMSLayerPanel to the tabbed pane.
						if (addTab(tabbedPane.getTabCount(), server.trim()) != null)
							tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
					}
				});

			// Create a tab for each server and add it to the tabbed panel.
			for (int i = 0; i < servers.length; i++)
			{
				this.addTab(i + 1, servers[i]); // i+1 to place all server tabs to the right of the Add Server tab
			}

			// Display the first server pane by default.
			this.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() > 0 ? 1 : 0);
			this.previousTabIndex = this.tabbedPane.getSelectedIndex();

			// Add the tabbed pane to a frame separate from the world window.
			wmsFrame = new JFrame();
			wmsFrame.getContentPane().add(tabbedPane);
			wmsFrame.pack();
			wmsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			wmsFrame.setVisible(true);
		}


		protected WMSLayersPanel addTab(int position, String server)
		{
			// Add a server to the tabbed dialog.
			try
			{
				WMSLayersPanel layersPanel = new WMSLayersPanel(this.getWwd(), server, wmsPanelSize);
				this.tabbedPane.add(layersPanel, BorderLayout.CENTER);
				String title = layersPanel.getServerDisplayString();
				this.tabbedPane.setTitleAt(position, title != null && title.length() > 0 ? title : server);

				return layersPanel;
			}
			catch (URISyntaxException e)
			{
				JOptionPane.showMessageDialog(null, "Server URL is invalid", "Invalid Server URL",
					JOptionPane.ERROR_MESSAGE);
				tabbedPane.setSelectedIndex(previousTabIndex);
				return null;
			}
		}


		protected void makeBrowserBalloon()
		{
			String BROWSER_BALLOON_CONTENT_PATH = "mil/navy/nrl/sdt3d/sdt3dDocBalloon.html";
			// = "/Users/ljt/testBalloon.html";

			String htmlString = null;
			InputStream contentStream = null;

			try
			{
				// Read the URL content into a String using the default encoding (UTF-8).
				contentStream = WWIO.openFileOrResourceStream(BROWSER_BALLOON_CONTENT_PATH, this.getClass());
				htmlString = WWIO.readStreamToString(contentStream, null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				WWIO.closeStream(contentStream, BROWSER_BALLOON_CONTENT_PATH);
			}

			if (htmlString == null)
				htmlString = Logging.getMessage("generic.ExceptionAttemptingToReadFile", BROWSER_BALLOON_CONTENT_PATH);

			Position balloonPosition = Position.fromDegrees(38.824002, -77.008974);
			// Create a Browser Balloon attached to the globe, and pointing to the sdt3d documentation
			// We use the balloon page's URL as its resource resolver to handle relative paths in the page content.
			AbstractBrowserBalloon balloon = new GlobeBrowserBalloon(htmlString, balloonPosition);
			// Size the balloon to provide enough space for its content.
			BalloonAttributes attrs = new BasicBalloonAttributes();
			attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null));
			balloon.setAttributes(attrs);

			// Create a placemark on the globe that the user can click to open the balloon.
			PointPlacemark placemark = new PointPlacemark(balloonPosition);
			placemark.setLabelText("Click to open balloon");
			// Associate the balloon with the placemark by setting AVKey.BALLOON. The BalloonController looks for this
			// value when an object is clicked.
			placemark.setValue(AVKey.BALLOON, balloon);

			this.layer.addRenderable(balloon);
			this.layer.addRenderable(placemark);

		}


		/**
		 * Load user configuration files.  Create config directories, sdt.properties if
		 * they don't exist.
		 * 
		 * Look in sdt.properties file for userPreferencesFile
		 * 
		 * @return boolean
		 */
		boolean initializeConfiguration()
		{
			File configDir = new File(configDirName);
			String propertiesFileName = configDirName + System.getProperty("file.separator") + sdtPropertiesFile;

			if (!configDir.exists())
			{
				System.out.println("Creating directory " + configDir);
				if (!(new File(configDirName)).mkdirs())
				{
					System.out.println("Creation of " + configDirName + " failed!");
					return false;
				}
				File propertiesFile = new File(propertiesFileName);
				if (!propertiesFile.exists())
				{
					// Create properties file
					try
					{
						// If file does not exist set default userPreferencesFile to sdt.settings
						BufferedWriter out = new BufferedWriter(new FileWriter(propertiesFileName));
						out.write("userPreferencesFile sdt.settings");
						out.close();
					}
					catch (IOException e)
					{
						System.out.println("Error creating " + propertiesFileName);
						return false;
					}
				}
			}
			// Now look in properties file for userPreferences file (It might have been
			// changed by user we didn't just create it in the above step).
			File propertiesFile = new File(propertiesFileName);
			if (propertiesFile.exists())
			{
				// Find user preferences file name
				try
				{
					String line = null;
					BufferedReader br = new BufferedReader(new FileReader(propertiesFileName));
					while ((line = br.readLine()) != null)
					{
						String[] keyVal = line.split(" ");
						if (keyVal.length > 0 && keyVal[0].equalsIgnoreCase("userPreferencesFile"))
						{
							// For now we only have one "property" just do a quick lookup
							userPreferencesFile = keyVal[1];
						}
					}
				}
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			loadUserPreferencesFile();
			
			return true;
		}

		/**
		 *  Load the userPreferences file assigned in initializeConfiguration
		 */
		void loadUserPreferencesFile()
		{
			// First look in config directory
			String fileName = findFile(configDirName + System.getProperty("file.separator") + userPreferencesFile);

			// If file not found, follow normal file lookup rules
			if (fileName == null)
			{
				loadInputFile(userPreferencesFile, false);
			}
			else
			{
				// Otherwise load the file found in the user config directory
				loadInputFile(fileName, false);
			}

			// Reset the lastTime a command was processed so we don't have
			// huge wait times in our debug file.
			lastTime = 0;

		} // end loadUserPreferncesFile()


		/*
		 * This function should only be used to load the user configuration file
		 * when the userConfigFile command is encountered, as we don't
		 * want to stop reading input file, and reset state as we do when config
		 * menu items are invoked. In the command case we only associate the file
		 * with the command and load the file.
		 */
		public boolean loadUserConfigFile(String val)
		{
			if (val == null || val.equalsIgnoreCase("none"))
			{
				currentConfigFile = null;
				return true;
			}

			// First look in config directory
			String fileName = findFile(configDirName + System.getProperty("file.separator") + val);
			if (fileName != null)
			{
				currentConfigFile = fileName;
				loadInputFile(fileName, false);
				lastTime = 0;
				return true;
			}

			// If file not found, follow normal file lookup rules
			currentConfigFile = val;
			loadInputFile(val, false);

			// Reset the lastTime a command was processed so we don't have
			// huge wait times in our debug file.
			lastTime = 0;
			return true;

		} // end loadUserConfigFile()


		/**
		 * Adds the specified <code>kmlRoot</code> to this app frame's <code>WorldWindow</code> as a new
		 * <code>Layer</code>, and adds a new <code>KMLLayerTreeNode</code> for the <code>kmlRoot</code> to this app
		 * frame's on-screen layer tree.
		 * <p/>
		 * This expects the <code>kmlRoot</code>'s <code>AVKey.DISPLAY_NAME</code> field to contain a display name
		 * suitable for use as a layer name.
		 *
		 * @param kmlRoot the KMLRoot to add a new layer for.
		 */
		protected void addKMLLayer(KMLRoot kmlRoot)
		{
			// Create a KMLController to adapt the KMLRoot to the World Wind renderable interface.
			KMLController kmlController = new KMLController(kmlRoot);

			// Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list. This
			// retrieve's the layer name from the KMLRoot's DISPLAY_NAME field.
			RenderableLayer layer = new RenderableLayer();
			layer.setName((String) kmlRoot.getField(AVKey.DISPLAY_NAME));
			layer.addRenderable(kmlController);
			this.getWwd().getModel().getLayers().add(layer);

			// Adds a new layer tree node for the KMLRoot to the on-screen layer tree, and makes the new node visible
			// in the tree. This also expands any tree paths that represent open KML containers or open KML network
			// links.
			KMLLayerTreeNode layerNode = new KMLLayerTreeNode(layer, kmlRoot);
			this.kmlPanelLayerTree.getModel().addLayer(layerNode);
			this.kmlPanelLayerTree.makeVisible(layerNode.getPath());
			layerNode.expandOpenContainers(this.kmlPanelLayerTree);

			// Listens to refresh property change events from KML network link nodes. Upon receiving such an event this
			// expands any tree paths that represent open KML containers. When a KML network link refreshes, its tree
			// node replaces its children with new nodes created form the refreshed content, then sends a refresh
			// property change event through the layer tree. By expanding open containers after a network link refresh,
			// we ensure that the network link tree view appearance is consistent with the KML specification.
			layerNode.addPropertyChangeListener(AVKey.RETRIEVAL_STATE_SUCCESSFUL, new PropertyChangeListener()
				{
					@Override
					public void propertyChange(PropertyChangeEvent event)
					{
						if (event.getSource() instanceof KMLNetworkLinkTreeNode)
							((KMLNetworkLinkTreeNode) event.getSource()).expandOpenContainers(kmlPanelLayerTree);
					}
				});
		}


		private void createDefaultSdtLayers(WorldWindow wwd)
		{
			// Create a background layer
			RenderableLayer backgroundLayer = new RenderableLayer();
			Object color = backgroundLayer.getValue(AVKey.WMS_BACKGROUND_COLOR);
			backgroundLayer.setValue(AVKey.WMS_BACKGROUND_COLOR, Color.RED);
			// backgroundLayer.setOpacity(1);
			insertBeforeCompass(wwd, backgroundLayer);

			// Create a renderable layer to manage node component rendering
			setNodeLayer(new RenderableLayer());
			getNodeLayer().setName("Node Layer");
			insertBeforeCompass(wwd, getNodeLayer());
			
			// Create a renderable layer for node trails
			setTrailLayer(new RenderableLayer());
			getTrailLayer().setName("Node Trails");
			insertBeforeCompass(wwd, getTrailLayer());

			// Create renderable layer for "links" (bottom layer)
			setLinkLayer(new RenderableLayer());
			getLinkLayer().setName("Network Links");
			insertBeforeCompass(wwd, getLinkLayer());

			setLinkLabelLayer(new AnnotationLayer());
			getLinkLabelLayer().setName("Link Labels");
			insertBeforeCompass(wwd, getLinkLabelLayer());

			markerLayer = new MarkerLayer();
			markerLayer.setOverrideMarkerElevation(false);
			markerLayer.setMarkers(getMarkers());
			markerLayer.setKeepSeparated(false);
			markerLayer.setName("Markers");
			insertBeforeCompass(wwd, markerLayer);

			// Create renderable layers for user defined links
			setUserDefinedLinkLayer(new RenderableLayer());
			getUserDefinedLinkLayer().setName("User Defined Network Links");
			insertBeforeCompass(getWwd(), getUserDefinedLinkLayer());

			setUserDefinedLinkLabelLayer(new AnnotationLayer());
			getUserDefinedLinkLabelLayer().setName("User Defined Link Labels");
			insertBeforeCompass(getWwd(), getUserDefinedLinkLabelLayer());

			userDefinedMarkerLayer = new MarkerLayer();
			userDefinedMarkerLayer.setOverrideMarkerElevation(false);
			userDefinedMarkerLayer.setMarkers(getUserDefinedMarkers());
			userDefinedMarkerLayer.setKeepSeparated(false);
			userDefinedMarkerLayer.setName("User Defined Markers");
			insertBeforeCompass(getWwd(), userDefinedMarkerLayer);

			setNodeLabelLayer(new AnnotationLayer());
			getNodeLabelLayer().setName("Node Labels");
			insertBeforeCompass(wwd, getNodeLabelLayer());

			// Create renderable layer for node icons
			setNodeIconLayer(new IconLayer());
			// We might override elevation on a node by node basis...
			getNodeIconLayer().setAlwaysUseAbsoluteElevation(true);
			getNodeIconLayer().setRegionCulling(false);
			getNodeIconLayer().setName("Node Icons");
			insertBeforeCompass(wwd, getNodeIconLayer());

			// We disable view clipping, as view tracking works best when
			// an icon's screen rectangle is known even when the icon is outside
			// the view frustrum. When set to "true" the view jumps up and down.
			getNodeIconLayer().setViewClippingEnabled(false);
			
			// Create renderable layer for node models
			setNodeModelLayer(new Model3DLayer());
			getNodeModelLayer().setName("Node Models");
			getNodeModelLayer().setMaintainConstantSize(true);
			getNodeModelLayer().setSize(1);
			insertBeforeCompass(wwd, getNodeModelLayer());

			// Create renderable layer for node kml models
			setNodeKmlModelLayer(new SdtKmlLayer());
			getNodeKmlModelLayer().setName("Node Kml Models");
			insertBeforeCompass(wwd, getNodeKmlModelLayer());

			// Create renderable layer for symbols.
			setSymbolLayer(new SdtSymbolLayer());
			getSymbolLayer().setName("Node Symbols");
			insertBeforeCompass(wwd, getSymbolLayer());

			// Create renderable layer for regions.
			regionLayer = new SdtRegionLayer();
			regionLayer.setName("Regions");
			// We need to turn picking off so that wwj picking functions
			// still work, e.g. left click moves the globe to that location
			regionLayer.setPickEnabled(false);
			insertBeforeCompass(wwd, regionLayer);

			// Create renderable layer for image "tiles".
			setTileLayer(new RenderableLayer());
			getTileLayer().setName("Tiles");
			// We need to turn picking off so that wwj picking functions
			// still work, e.g. left click moves the globe to that location
			getTileLayer().setPickEnabled(false);
			insertBeforeCompass(wwd, getTileLayer());

			// Create a renderable layer to manage kml rendering
			kmlPanelLayer = new RenderableLayer();
			kmlPanelLayer.setName("Kml Panel");
			insertBeforeCompass(wwd, kmlPanelLayer);

			// Create a renderable layer to manage kml node rendering
			kmlLayer = new RenderableLayer();
			kmlLayer.setName("Kml");
			insertBeforeCompass(wwd, kmlLayer);

		} // end createDefaultSdtLayers


		private void buildMenuBar()
		{
			menuBar = new JMenuBar();
			// build file menu
			fileMenu = new JMenu("File");
			openItem = new JMenuItem("Open file...");
			appendItem = new JMenuItem("Append file...");
			resetItem = new JMenuItem("Reset");
			hardResetItem = new JMenuItem("Hard Reset");
			resetPerspectiveItem = new JMenuItem("Reset Perspective");
			clearSpriteTableItem = new JMenuItem("Delete sprite table");

			configFileMenu = new JMenu("Configuration file options");

			screenItem = new JMenuItem("Save a screenshot ");
			listenUdpItem = new JMenuItem("Listen to UDP port...");
			listenTcpItem = new JMenuItem("Listen to TCP port...");
			exitItem = new JMenuItem("Exit");
			openItem.addActionListener(this);
			appendItem.addActionListener(this);
			resetItem.addActionListener(this);
			hardResetItem.addActionListener(this);
			resetPerspectiveItem.addActionListener(this);
			clearSpriteTableItem.addActionListener(this);
			screenItem.addActionListener(this);
			listenUdpItem.addActionListener(this);
			listenTcpItem.addActionListener(this);
			exitItem.addActionListener(this);

			offlineModeItem = new JCheckBoxMenuItem("WWJ offline mode");
			offlineModeItem.setSelected(false);
			offlineModeItem.addActionListener(this);

			loadCacheItem = new JMenuItem("Set WWJ cache ...");
			loadCacheItem.addActionListener(this);

			// Build kml menu
			kmlMenu = new JMenu("KML");
			kmlMenu.add(loadKMLFileItem = new JMenuItem("Load KML file ..."));
			kmlMenu.add(loadKMLUrlItem = new JMenuItem("Load KML URL ..."));

			loadKMLFileItem.addActionListener(this);
			loadKMLUrlItem.addActionListener(this);

			kmlMenu.addSeparator();

			fileMenu.add(openItem);
			fileMenu.add(appendItem);
			fileMenu.add(resetItem);
			fileMenu.add(hardResetItem);
			fileMenu.add(resetPerspectiveItem);
			fileMenu.add(clearSpriteTableItem);
			fileMenu.add(configFileMenu);
			fileMenu.addSeparator();
			fileMenu.add(kmlMenu);
			fileMenu.add(screenItem);
			fileMenu.addSeparator();
			fileMenu.add(listenUdpItem);
			fileMenu.add(listenTcpItem);
			fileMenu.addSeparator();
			fileMenu.add(offlineModeItem);
			fileMenu.add(loadCacheItem);
			fileMenu.addSeparator();
			fileMenu.add(exitItem);
			menuBar.add(fileMenu);

			loadConfigItem = new JMenuItem("Load configuration file...");
			loadConfigItem.addActionListener(this);
			configFileMenu.add(loadConfigItem);
			clearConfigItem = new JMenuItem("Clear configuration");
			configFileMenu.add(clearConfigItem);
			clearConfigItem.addActionListener(this);

			// build view menu
			viewMenu = new JMenu("View");
			menuBar.add(viewMenu);
			layerMenu = new JMenu("Layer controls");
			viewMenu.add(layerMenu);
			removeUDLayersItem = new JMenuItem("Remove user defined layers");
			sdtLayersItem = new JMenuItem("Reset Worldwind layers");
			sdtLayersItem.addActionListener(this);
			removeUDLayersItem.addActionListener(this);
			layerMenu.add(sdtLayersItem);
			layerMenu.add(removeUDLayersItem);

			// build bookmark menu
			bookmarkMenu = new JMenu("Bookmarks");
			viewMenu.add(bookmarkMenu);
			bookmarkMenu.add(bookmarkItem = new JMenuItem("Bookmark this view"));
			bookmarkMenu.add(loadBookmarkItem = new JMenuItem("Load bookmark"));
			bookmarkMenu.add(loadDefaultBookmarksItem = new JMenuItem("Load default bookmarks"));
			bookmarkMenu.addSeparator();
			bookmarkItem.addActionListener(this);
			loadBookmarkItem.addActionListener(this);
			loadDefaultBookmarksItem.addActionListener(this);

			// build globe menu
			globeMenu = new JMenu("Globe");
			viewMenu.add(globeMenu);
			viewMenu.addSeparator();
			globeItem = new JCheckBoxMenuItem("Round");
			globeItem.setSelected(true);

			globeItem.addActionListener(this);
			globeMenu.add(globeItem);
			flatEarthMenu = new JMenu("Flat Earth");
			flatEarthMenu.setSelected(false);
			globeMenu.add(flatEarthMenu);
			// Can't add combo boxes to menus??
			// projectionCombo = new JComboBox(new String[] {"Mercator", "Lat-Lon", "Modified Sinusoidal",
			// "Sinusoidal"});

			latLonItem = new JCheckBoxMenuItem("Lat/Lon");
			latLonItem.addActionListener(this);
			latLonItem.setSelected(false);
			mercatorItem = new JCheckBoxMenuItem("Mercator");
			mercatorItem.addActionListener(this);
			mercatorItem.setSelected(false);
			sinusoidalItem = new JCheckBoxMenuItem("Sinusoidal");
			sinusoidalItem.addActionListener(this);
			sinusoidalItem.setSelected(false);
			modSinusoidalItem = new JCheckBoxMenuItem("ModSinusoidal");
			modSinusoidalItem.addActionListener(this);
			modSinusoidalItem.setSelected(false);
			flatEarthMenu.add(latLonItem);
			flatEarthMenu.add(mercatorItem);
			flatEarthMenu.add(sinusoidalItem);
			flatEarthMenu.add(modSinusoidalItem);

			elevationItem = new JCheckBoxMenuItem("Elevation model");
			elevationItem.setSelected(true);
			viewMenu.add(elevationItem);
			elevationItem.addActionListener(this);

			stereoItem = new JCheckBoxMenuItem("Stereo mode");
			stereoItem.setSelected(false);
			viewMenu.add(stereoItem);
			stereoItem.addActionListener(this);

			backgroundColorItem = new JMenuItem("Background color");
			viewMenu.add(backgroundColorItem);
			backgroundColorItem.addActionListener(this);

			// build toggle multiple links menu
			collapseLinksItem = new JCheckBoxMenuItem("Collapse multiple links");
			collapseLinksItem.setSelected(false);
			viewMenu.add(collapseLinksItem);
			collapseLinksItem.addActionListener(this);

			symbolOffsetItem = new JCheckBoxMenuItem("Apply symbol offset");
			symbolOffsetItem.setSelected(true);
			viewMenu.add(symbolOffsetItem);
			symbolOffsetItem.addActionListener(this);

			debugItem = new JCheckBoxMenuItem("Log debug output");
			debugItem.setSelected(false);
			logDebugOutput = false;
			viewMenu.add(debugItem);
			debugItem.addActionListener(this);

			viewMenu.addSeparator();
			showLayersItem = new JCheckBoxMenuItem("Show layer panel");
			showLayersItem.addActionListener(this);
			showLayersItem.setSelected(true);
			;
			viewMenu.add(showLayersItem);

			showSdtPanelItem = new JCheckBoxMenuItem("Show sdt panel");
			showSdtPanelItem.setSelected(true);
			viewMenu.add(showSdtPanelItem);
			showSdtPanelItem.addActionListener(this);
			;

			showStatusItem = new JCheckBoxMenuItem("Show status panel");
			showStatusItem.setSelected(true);
			viewMenu.add(showStatusItem);
			showStatusItem.addActionListener(this);

			showSdtViewControlsItem = new JCheckBoxMenuItem("Show sdt view controls");
			showSdtViewControlsItem.setSelected(true);
			viewMenu.add(showSdtViewControlsItem);
			showSdtViewControlsItem.addActionListener(this);

			multiFrameItem = new JCheckBoxMenuItem("Show 2nd Frame");
			multiFrameItem.setSelected(false);
			viewMenu.add(multiFrameItem);
			multiFrameItem.addActionListener(this);

			showScenarioPlaybackItem = new JCheckBoxMenuItem("Show Scenario Playback panel");
			showScenarioPlaybackItem.setSelected(true);
			viewMenu.add(showScenarioPlaybackItem);
			showScenarioPlaybackItem.addActionListener(this);

			showWmsItem = new JCheckBoxMenuItem("Show WMS Frame");
			showWmsItem.setSelected(false);
			viewMenu.add(showWmsItem);
			showWmsItem.addActionListener(this);

			this.setJMenuBar(menuBar);

			// ---- "About [sdt-3D]" ----
			if (!Configuration.isMacOS())
			{
				JMenu aboutMenu = new JMenu("Help");
				JMenuItem about = new JMenuItem();
				about.setText("About");
				about.setMnemonic('A');
				aboutMenu.add(about);
				about.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent event)
						{
							showAbout();
						}
					});

				menuBar.add(aboutMenu);
			}
			else
			{
				try
				{
					OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAbout", (Class[]) null));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		} // end buildMenuBar


		public void showAbout()
		{
			// default title and icon
			java.net.URL imageURL = getClass().getResource("/images/sdtLogo.jpg");

			ImageIcon image = null;
			if (imageURL != null)
			{
				image = new ImageIcon(imageURL);
				image = new ImageIcon(image.getImage().getScaledInstance(logoPixelSize / 2, logoPixelSize / 2, 0));
			}
			String versionTxt = "  sdt3d Version " + version + "\nWorldwind Version  " + wwjVersion;
			JOptionPane.showMessageDialog(this, versionTxt, "sdt-3d", JOptionPane.INFORMATION_MESSAGE, image);
		}


		private void startProtoPipe()
		{
			// Start thread that opens ProtoPipe and listens for commands
			pipeThread = new PipeThread(this);
			pipeThread.start();
			System.out.println(System.getProperty("java.library.path"));

		} // end startProtoPipe

		/**
		 * 			 Create select listener for tool tips and drag events
		 */
		private void createSelectListener()
		{
			try
			{
				// Create select listener - ljt make items draggable?
				getWwd().addSelectListener(new SelectListener()
					{
						private WWIcon lastToolTipIcon = null;

						private SdtPolyline lastToolTipPolyline = null;


						@Override
						public void selected(SelectEvent event)
						{
							// Have hover selections show a picked icon's tool tip.
							if (event.getEventAction().equals(SelectEvent.HOVER))
							{
								// If a tool tip is already showing, undisplay it.
								if (lastToolTipIcon != null)
								{
									lastToolTipIcon.setShowToolTip(false);
									this.lastToolTipIcon = null;
									((Component) getWwd()).repaint();

								}

								if (lastToolTipPolyline != null)
								{
									lastToolTipPolyline.setShowToolTip(false);
									this.lastToolTipPolyline = null;
									((Component) getWwd()).repaint();

								}
								// If there's a selection, (check that we're not dragging
								// if we implement dragging), and
								// the selection is an icon or sdtpolyline, show tool tip.
								if (event.hasObjects())
								{
									if (event.getTopObject() instanceof WWIcon)
									{
										this.lastToolTipIcon = (WWIcon) event
												.getTopObject();
										lastToolTipIcon.setShowToolTip(true);
										((Component) getWwd()).repaint();

									}

									if (event.getTopObject() instanceof SdtPolyline)
									{
										this.lastToolTipPolyline = (SdtPolyline) event
												.getTopObject();
										lastToolTipPolyline.setShowToolTip(true);
										((Component) getWwd()).repaint();
									}
								}
							}
							// Have rollover events highlight the rolled-over
							// object.
							else if (event.getEventAction().equals(
								SelectEvent.ROLLOVER))
							{
								AppFrame.this.highlight(event.getTopObject());
							}
							// Have drag events drag the selected object.
							else if (event.getEventAction().equals(
								SelectEvent.DRAG_END)
								|| event.getEventAction().equals(
									SelectEvent.DRAG))
							{
								// Delegate dragging computations to a dragger.
								// this.dragger.selected(event);

								// We missed any roll-over events while dragging, so
								// highlight any under the cursor now,
								// or de-highlight the dragged shape if it's no
								// longer under the cursor.
								if (event.getEventAction().equals(
									SelectEvent.DRAG_END))
								{
									PickedObjectList pol = getWwd()
											.getObjectsAtCurrentPosition();
									if (pol != null)
									{
										AppFrame.this.highlight(pol.getTopObject());
										((Component) getWwd()).repaint();
									}
								}
							}
						}
					});

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		} // end createSelectListener

		
		public void modelPropertyChange(String propertyName, Object oldValue, Object newValue)
		{
			if (propChangeSupport != null)
			{
				this.propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
			}
		}
		
		private void stopScenarioThread()
		{    	
			if (scenarioThread != null)
			{
    				scenarioThread.stopThread();
    				
			}
		}
		
		
		private void startScenarioThread(Long scenarioPlaybackStartTime)
		{
			if (scenarioThread != null)
			{
				scenarioThread.stopThread();
			}
			scenarioThread = new ScenarioThread(this, scenarioController, int2Cmd, scenarioPlaybackStartTime);
			scenarioThread.start();
		}
		
		


		private boolean validateColor(String c)
		{
			// To support old style links we check to see if we have
			// a valid color, otherwise the attribute is a linkId
			Color red = Color.red;
			Color color = getColor(c);
			// Rgb colors are valid
			// This is a hack - linkId's with two quotes will fail!! ljt fix
			if (c.split(":").length == 2)
				return true;
			if (color.equals(red) && !c.equalsIgnoreCase("red"))
				return false;
			return true;

		} // end validateColor


		private static Color getColor(String c)
		{
			Color color = Color.RED;
			// Is it a valid hex color?
			if (c.startsWith("0x") || c.startsWith("0X"))
			{
				c = c.trim();
				String x[] = c.split("0x");
				if (x.length < 2)
				{
					System.out.println("sdt3d::validateColor 0x required!");
					return color;
				}
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
				if (!(rgbVal.length == 3))
				{
					System.out.println("getColor() Invalid color rgb values");
					return color;
				}
				if ((Integer.valueOf(rgbVal[0]) < 0 || Integer.valueOf(rgbVal[0]) > 255
					|| Integer.valueOf(rgbVal[1]) < 0 || Integer.valueOf(rgbVal[1]) > 255
					|| Integer.valueOf(rgbVal[2]) < 0 || Integer.valueOf(rgbVal[2]) > 255))
				{
					System.out.println("getColor() Invalid color rgb values");
					return color;
				}
				color = new Color(Integer.valueOf(rgbVal[0]), Integer.valueOf(rgbVal[1]), Integer.valueOf(rgbVal[2]));
				return color;
			}

			if (c.equalsIgnoreCase("white"))
				color = Color.white;
			else if (c.equalsIgnoreCase("yellow"))
				color = Color.yellow;
			else if (c.equalsIgnoreCase("green"))
				color = Color.green;
			else if (c.equalsIgnoreCase("blue"))
				color = Color.blue;
			else if (c.equalsIgnoreCase("cyan"))
				color = Color.cyan;
			else if (c.equalsIgnoreCase("red"))
				color = Color.red;
			else if (c.equalsIgnoreCase("pink"))
				color = Color.pink;
			else if (c.equalsIgnoreCase("orange"))
				color = Color.orange;
			else if (c.equalsIgnoreCase("magenta"))
				color = Color.magenta;
			else if (c.equalsIgnoreCase("purple"))
				color = Color.magenta;
			else if (c.equalsIgnoreCase("gray"))
				color = Color.gray;
			else if (c.equalsIgnoreCase("black"))
				color = Color.black;
			else
				color = Color.RED;
			return color;
		} // end sdt3d.getColor()

		/**
		 * Little helper class to parse commands.
		 * 
		 * @author ljt
		 *
		 */
		public class CmdParser
		{

			String pending_cmd = null;

			boolean seeking_cmd = true;

			String current_cmd = null;

			private SdtSprite currentSprite = null;

			private SdtNode currentNode = null;

			private SdtRegion currentRegion = null;

			private SdtSymbol currentSymbol = null;

			private List<SdtLink> currentLinkSet = new ArrayList<SdtLink>();

			private SdtTile currentTile = null;

			private String currentView = null;

			private String currentGeoTiff = null;

			private SdtSpriteKml currentKml = null;

			boolean pipeCmd = false;

			StringWriter buffer = new StringWriter();

			PrintWriter out = new PrintWriter(buffer);


			void SetPipeCmd(boolean theBool)
			{
				pipeCmd = true;
			}


			public CmdType GetCmdType(String cmd)
			{

				if (0 == cmd.length())
					return CmdType.CMD_INVALID;
				boolean matched = false;
				CmdType type = CmdType.CMD_INVALID;
				String[] nextCmd = CMD_LIST;
				int i = 0;
				while (i < CMD_LIST.length - 1)
				{
					String validCmd = nextCmd[i].substring(1, nextCmd[i].length());
					if (validCmd.equalsIgnoreCase(cmd))
					{
						if (matched)
						{
							// ambiguous command (cmd should match only once)
							return CmdType.CMD_INVALID;
						}
						else
						{
							matched = true;
							if (nextCmd[i].startsWith("+"))
								type = CmdType.CMD_ARG;
							else
								type = CmdType.CMD_NOARG;
						}
					}
					i++;
				} // end while
				return type;
			} // end GetCmdType


			/**
			 * Called by the thread sychronized sdt3d::OnInput loop, this
			 * method is responsible for waiting for command arguments
			 * if necessary and invoking sdt3d.processCommand()
			 * 
			 * @param str
			 * @return
			 */
			public boolean OnCommand(String str, boolean scenarioCmd)
			{	
				
				//System.out.println("OnCommand str>" + str);
				str = str.trim();

				if (null == pending_cmd)
					pending_cmd = str;

				if (seeking_cmd)
				{
					switch (GetCmdType(pending_cmd))
					{
						case CMD_ARG:
							current_cmd = pending_cmd;
							seeking_cmd = false;
							break;
						case CMD_NOARG:
							processCmd(pending_cmd, null, scenarioCmd); // ljt error checking?
							pending_cmd = null;
							seeking_cmd = true;
							break;
						default:
							seeking_cmd = true;
							pending_cmd = null;
							return false;
					} // end switch
				}
				else // Not seeking command
				{
					processCmd(current_cmd, str, scenarioCmd);
					seeking_cmd = true;
					pending_cmd = null;
				} // done seeking cmd

				return true;
			} // end OnCommand
		}; // end class CmdParser


		private StatusPanel getStatusPanel()
		{
			return this.statusPanel;
		}


		private ScenarioPlaybackPanel getScenarioPlaybackPanel()
		{
			return this.scenarioPlaybackPanel;
		}


		private JFrame getWmsFrame()
		{
			return this.wmsFrame;
		}


		private JPanel getSdtViewControlsPanel()
		{
			return this.sdtViewControlsPanel;
		}


		private void highlight(Object o)
		{
			// Manage highlighting of icons.

			if (this.lastPickedIcon == o)
				return; // same thing selected

			// Turn off highlight if on.
			if (this.lastPickedIcon != null)
			{
				this.lastPickedIcon.setHighlighted(false);
				this.lastPickedIcon = null;
			}

			// Turn on highlight if object selected.
			if (o != null && o instanceof WWIcon)
			{
				this.lastPickedIcon = (WWIcon) o;
				this.lastPickedIcon.setHighlighted(true);
			}
		} // end highlight


		/*
		 * Reset sdt perspective to default
		 * 
		 */
		private void resetPerspective()
		{

			// unfollow any nodes
			setFollow("all,off");
			// turn off the focus on any nodes
			setFocus("off");
			// Set up a view controller to keep the nodes in view.
			this.viewController = new ViewController(getWwd());
			this.viewController.setIcons(getNodeIconLayer().getIcons());
			this.viewController.setModels(getNodeModelLayer().getModels());
			this.viewController.setKmlModels(getNodeKmlModelLayer().getRenderables());

			// Reset system modes
			this.setOfflineMode("off");
			this.setElevationData("on");
			this.setStereo("off");
			this.setBackgroundColor("black");
			this.setCollapseLinks("off");
			this.getSdtLayerPanel().update(getWwd(), "wwj");
			this.getSdtLayerPanel().update(getWwd(), "all");

			getWwd().setView(new BasicOrbitView());

			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

		} // resetPerspective


		/*
		 * Reset sdt state & stop any file processing
		 * 
		 */
		/**
		 * Reset sdt state.
		 * 
		 * In addition to a soft reset, a hardReset will stop any file processing,
		 * logging, stop listening to any sockets, and reset system modes (offline mode,
		 * flat earth etc). For now the sprite table is NOT cleared.
		 * 
		 * A "soft" reset will only clear visual system state like user defined
		 * layers and will remove all system renderables. Any user configuration and
		 * settings files are reloaded.
		 * 
		 * @param hardReset
		 */
		private void resetSystemState(boolean hardReset)
		{
			if (fileThread != null && openFile != null)
			{
				fileThread.stopThread();
				fileThread.stopRead();
				fileThread.clear();
				try
				{
					Thread.currentThread();
					Thread.sleep(1000); // sleep for 1000 ms
				}
				catch (InterruptedException ie)
				{
					// If this thread was interrupted by another thread
				}
			}
			
			if (hardReset)
			{

				// ljt todo: here?
				startScenarioThread((long) 0);
				scenarioController.reset();
				
				// shutdown any socket threads, recreate menu item
				if (udpSocketThread != null && !udpSocketThread.stopped())
				{
					udpSocketThread.stopThread();
					udpSocketThread = null;
					toggleUdpOn();
				}
				if (tcpSocketThread != null && !tcpSocketThread.stopped())
				{
					tcpSocketThread.stopThread();
					tcpSocketThread = null;
					toggleTcpOn();
				}
								
				// Stop scenario recording and reset state
				startStopScenarioRecording("off");
				
				// Reset system modes
				this.setOfflineMode("off");
				this.setElevationData("on");
				this.setStereo("off");
				this.setBackgroundColor("black");
				this.setCollapseLinks("off");

				// unfollow any nodes
				setFollow("all,off");
				// turn off the focus on any nodes
				setFocus("off");

				// Set up a view controller to keep the nodes in view.
				this.viewController = new ViewController(getWwd());
				this.viewController.setIcons(getNodeIconLayer().getIcons());
				this.viewController.setModels(getNodeModelLayer().getModels());
				this.viewController.setKmlModels(getNodeKmlModelLayer().getRenderables());

				getWwd().setView(new BasicOrbitView());

			} // end hard reset

			
			logDebugOutput = false;
			debugItem.setSelected(false);
			try
			{
				if (fileWriter != null)
				{
					fileWriter.close();
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (logDebugFile != null)
			{
				logDebugFile.close();
				logDebugFile = null;

			}
			removeUDLayers();

			// clear does not delete the sprite table
			// reset will subsequently call "delete" all
			// but file loads and configuration loads do not

			if (hardReset)
			{
				clear("all");

				loadUserPreferencesFile();

				getSdtLayerPanel().update(getWwd(), "wwj");
				getSdtLayerPanel().update(getWwd(), "all");

				// loadUserConfigFile(currentConfigFile);
				// loadInputFile(currentConfigFile);

			}
			else
			{
				// clear("all") stops reading input file
				// so remove all rednerables directly

				removeNodes();
				removeRegions();
				removeTiles();
				removeLinkLabels();
				removeKml();
				clearAllRenderables();
				this.elevationBuilder.clear();
				setStatus("");
				loadInputFile(currentConfigFile, false);
			}


			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

		} // resetSystemState


		private void toggleUdpOff()
		{
			fileMenu.remove(listenUdpItem);
			listenUdpItem = new JMenuItem("UDP Off " + socketPort);
			listenUdpItem.addActionListener(this);
			fileMenu.add(listenUdpItem, 8);
		}


		private void toggleTcpOff()
		{
			fileMenu.remove(listenTcpItem);
			listenTcpItem = new JMenuItem("TCP Off " + socketPort);
			listenTcpItem.addActionListener(this);
			fileMenu.add(listenTcpItem, 9);
		}


		private void toggleUdpOn()
		{
			fileMenu.remove(listenUdpItem);
			listenUdpItem = new JMenuItem("Listen to UDP port... ");
			listenUdpItem.addActionListener(this);
			fileMenu.add(listenUdpItem, 8);
		}


		private void toggleTcpOn()
		{
			fileMenu.remove(listenTcpItem);
			listenTcpItem = new JMenuItem("Listen to TCP port... ");
			listenTcpItem.addActionListener(this);
			fileMenu.add(listenTcpItem, 9);
		}


		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent event)
		{

			if (event.getSource() == loadConfigItem)
			{
				// Stop file read, do a hard system reset
				resetSystemState(true);

				// Delete sprite table
				delete("sprite,all");

				// We've reset everything so we can load user preferences file immediately
				loadUserPreferencesFile();

				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					openFile = fc.getSelectedFile();
					String fileName = openFile.getAbsolutePath();
					// Make the current config file the last one selected
					currentConfigFile = fileName;

					fileThread = new FileThread(this, fileName, false);
					fileThread.start();
				}
				else
				{
					System.out.println("Load config command cancelled by user.");
				}
			}
			else if (event.getSource() == clearConfigItem)
			{
				// Do a hard system reset
				resetSystemState(true);
				delete("sprite,all");
				currentConfigFile = null;
				loadUserPreferencesFile();
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}

			else if (event.getSource() == openItem)
			{

				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					openFile = fc.getSelectedFile();
					String fileName = openFile.getAbsolutePath();
					// Do a hard system reset
					resetSystemState(false);

					loadInputFile(fileName, true);

				}
				else
				{
					System.out.println("Open command cancelled by user.");
				}
			}
			else if (event.getSource() == loadCacheItem)
			{
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					cacheDir = fc.getSelectedFile();
					String fileName = cacheDir.getAbsolutePath();
					FileStore wwjCache = WorldWind.getDataFileStore();
					wwjCache.addLocation(0, fileName, true);
				}
			}
			else if (event.getSource() == loadKMLFileItem)
			{
				File kmlFile = null;
				fc.setCurrentDirectory(openFile);
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					kmlFile = fc.getSelectedFile();
					String fileName = kmlFile.getAbsolutePath();

					setKml(fileName);
					setKmlFile(fileName);

				}

			}
			else if (event.getSource() == loadKMLUrlItem)
			{
				// http://earthquake.usgs.gov/earthquakes/feed/earthquakes.kml
				String inputValue = JOptionPane.showInputDialog("URL");
				if (inputValue == null || inputValue.isEmpty())
					return; // check cancel button?
				inputValue.trim();
				setKml(inputValue);
				setKmlFile(inputValue);
			}
			else if (event.getSource() == appendItem)
			{

				// append the file so that it is processed sequentially
				fc.setCurrentDirectory(openFile);

				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File appendFile = fc.getSelectedFile();
					String fileName = appendFile.getAbsolutePath();
					// Pretend we're a pipe command so the file is appended to the
					// file list - probably should add append file option
					loadInputFile(fileName, true);
				}
				else
				{
					System.out.println("Append command cancelled by user.");
				}
			}
			else if (event.getSource() == resetItem)
			{
				resetSystemState(false);
			}
			else if (event.getSource() == hardResetItem)
			{
				resetSystemState(true);
			}
			else if (event.getSource() == resetPerspectiveItem)
			{
				resetPerspective();

			}
			else if (event.getSource() == clearSpriteTableItem)

			{
				delete("sprite,all");
			}
			else if (event.getSource() == screenItem)
			{
				WorldWindowGLCanvas canvas = (WorldWindowGLCanvas) getWwd();

				int framewidth = canvas.getSize().width; // get the canvas'
				// dimensions
				int frameheight = canvas.getSize().height;
				canvas.getContext().makeCurrent();
				BufferedImage image = Screenshot.readToBufferedImage(
					framewidth, frameheight);

				int returnVal = fc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();
					try
					{
						javax.imageio.ImageIO.write(image, "PNG", file);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					System.out.println("Screenshot command cancelled by user.");
				}
			}
			else if (event.getSource() == listenUdpItem)
			{

				if (udpSocketThread == null || udpSocketThread.stopped())
				{
					String inputValue = JOptionPane.showInputDialog("[<addr>/]<port>");
					if (inputValue == null || inputValue.isEmpty())
						return; // check cancel button?
					String udpInputValue = "udp," + inputValue;
					setListen(udpInputValue);
					if (!udpSocketThread.stopped())
					{
						JOptionPane.showMessageDialog(getWwjPanel(), "Listening to " + inputValue);
					}
					else
					{
						JOptionPane.showMessageDialog(getWwjPanel(), "Unable to listen to " + inputValue);
					}

				}
				else
				{
					// stop the thread
					udpSocketThread.stopThread();
				}
				if (udpSocketThread.stopped())
					toggleUdpOn();
				else
					toggleUdpOff();
			}
			else if (event.getSource() == listenTcpItem)
			{

				if (tcpSocketThread == null || tcpSocketThread.stopped())
				{
					String inputValue = JOptionPane.showInputDialog("<port>");
					if (inputValue == null || inputValue.isEmpty())
						return; // check cancel button?
					String tcpInputValue = "tcp," + inputValue;
					setListen(tcpInputValue);
					if (tcpSocketThread.stopped())
					{
						JOptionPane.showMessageDialog(getWwjPanel(), "Unable to listen to TCP port " + inputValue);
					}

				}
				else
				{
					// stop the thread
					tcpSocketThread.stopThread();
				}
				// recreate the menu item we do this here as we might not
				// have been able to listen to the socket
				if (tcpSocketThread.stopped())
					toggleTcpOn();
				else
					toggleTcpOff();
				return;

			}
			else if (event.getSource() == offlineModeItem)
			{

				if (offlineModeItem.isSelected())
					this.setOfflineMode("on");
				else
					this.setOfflineMode("false");

			}
			else if (event.getSource() == loadDefaultBookmarksItem)
			{

				File configDir = new File(configDirName);

				File[] files = configDir.listFiles();
				for (int fileInList = 0; fileInList < files.length; fileInList++)
				{
					// System.out.println(files[fileInList].toString());
					String fileName = files[fileInList].getAbsolutePath();
					if (!fileName.endsWith(".xml"))
						continue;

					String[] viewName = files[fileInList].getName().split(".xml");

					currentView = viewName[0];
					if (!setViewXml(fileName, false))
					{
						System.out.println("Xml file " + fileName + " not a valid view");
					}

				}
			}
			else if (event.getSource() == loadBookmarkItem)
			{

				File bookmarkFile = null;
				File configDir = new File(configDirName);
				fc.setCurrentDirectory(configDir);
				// fc.setCurrentDirectory(openFile);
				String[] viewName = null;
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					bookmarkFile = fc.getSelectedFile();

					String fileName = bookmarkFile.getAbsolutePath();
					if (!fileName.endsWith(".xml"))
						return;
					viewName = bookmarkFile.getName().split(".xml");

					currentView = viewName[0];
					if (!setViewXml(fileName, true))
					{
						System.out.println("View xml not found.");
						return;
					}

				}
				else
				{
					System.out.println("Open command cancelled by user.");
					return;
				}
				String theView = viewTable.get(viewName[0]);
				if (theView != null)
					((OrbitView) getWwd().getView()).restoreState(theView);
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			else if (event.getSource() == bookmarkItem)
			{

				Object[] options = { "Cancel", "Save to disk", "Save in current session only" };
				Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
				ImageIcon transparantIcon = new ImageIcon(image, "");
				int ret = JOptionPane.showOptionDialog(null, null, "Bookmark this view",
					JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
					transparantIcon, options, options[2]);
				if (ret == 3)
					return;
				viewState = ((OrbitView) getWwd().getView()).getRestorableState();

				// Save view to disk
				if (ret == 1)
				{
					File configDir = new File(configDirName);
					fc.setCurrentDirectory(configDir);

					int returnVal = fc.showSaveDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						String path = fc.getSelectedFile().getAbsolutePath();
						if (!path.endsWith(".xml"))
							path = path + ".xml";

						String[] viewName = fc.getSelectedFile().getName().split(".xml");
						currentView = viewName[0];

						// Save to disk
						try
						{
							BufferedWriter out = new BufferedWriter(new FileWriter(path));
							out.write(viewState);
							out.close();
						}
						catch (IOException e)
						{
							System.out.println("Error saving view state to file " + path);
						}
					}
				}
				// Save view for current session only
				else if (ret == 2)
				{
					currentView = JOptionPane.showInputDialog(null, "View name : ", "", 1);
					if (currentView == null)
						return;
				}
				// add to bookmarks menu
				JMenuItem newItem = new JMenuItem(currentView);
				newItem.addActionListener(this);
				bookmarkMenu.add(newItem);
				viewTable.put(currentView, viewState);

			}
			else if (viewTable.get(event.getActionCommand()) != null)
			{
				String theView = viewTable.get(event.getActionCommand());
				if (theView != null)
					((OrbitView) getWwd().getView()).restoreState(theView);

				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			else if (kmlTable.get(event.getActionCommand()) != null)
			{
				SdtSpriteKml theKml = kmlTable.get(event.getActionCommand());
				if (theKml.getLookAt() != null)
				{
					sdtKmlViewController.goTo(theKml.getLookAt());
				}

				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			else if (event.getSource() == exitItem)
			{
				System.exit(0);
			}
			else if (event.getSource() == sdtLayersItem)
			{
				// this.getSdtLayerPanel().setVisible(true);
				this.getSdtLayerPanel().update(getWwd(), "wwj");
			}
			else if (event.getSource() == showLayersItem)
			{
				if (showLayersItem.isSelected())
					this.getSdtLayerPanel().setVisible(true);
				else
					this.getSdtLayerPanel().setVisible(false);
			}
			else if (event.getSource() == removeUDLayersItem)
			{
				this.removeUDLayers();
				this.getSdtLayerPanel().update(getWwd(), "all");
			}
			else if (event.getSource() == showStatusItem)
			{
				if (showStatusItem.isSelected())
				{
					this.getStatusPanel().setVisible(true);
				}
				else
				{
					this.getStatusPanel().setVisible(false);
				}
			}
			else if (event.getSource() == showScenarioPlaybackItem)
			{
				if (showScenarioPlaybackItem.isSelected())
				{
					this.getScenarioPlaybackPanel().setVisible(true);
				}
				else
				{
					this.getScenarioPlaybackPanel().setVisible(false);
				}
			}
			else if (event.getSource() == showWmsItem)
			{
				if (showWmsItem.isSelected())
				{
					if (this.getWmsFrame() == null)
						buildWmsLayerFrame();
					this.getWmsFrame().setVisible(true);
				}
				else
				{
					if (this.getWmsFrame() != null)
						this.getWmsFrame().setVisible(false);
				}
			}
			else if (event.getSource() == showSdtPanelItem)
			{
				if (showSdtPanelItem.isSelected())
				{
					westPanel.setVisible(true);
					horizontalSplitPane.setLeftComponent(westPanel);
				}
				else
					westPanel.setVisible(false);
			}
			else if (event.getSource() == showSdtViewControlsItem)
			{
				if (showSdtViewControlsItem.isSelected())
					this.getSdtViewControlsPanel().setVisible(true);
				else
					this.getSdtViewControlsPanel().setVisible(false);

			}
			else if (event.getSource() == debugItem)
			{
				if (debugItem.isSelected())
				{
					logDebugOutput = true;
				}
				else
				{
					logDebugOutput = false;
					debugItem.setSelected(false);
					try
					{
						if (fileWriter != null)
						{
							fileWriter.close();
						}
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (logDebugFile != null)
					{
						logDebugFile.close();
						logDebugFile = null;
					}

				}

			}
			else if (event.getSource() == elevationItem)
			{
				if (elevationItem.isSelected())
				{
					this.setElevationData("on");
				}
				else
				{
					this.setElevationData("off");
				}
			}
			else if (event.getSource() == stereoItem)
			{

				if (stereoItem.isSelected())
					this.setStereo("on");
				else
					this.setStereo("off");

			}
			else if (event.getSource() == globeItem)
			{
				if (globeItem.isSelected())
				{
					setFlatEarth("off");
					mercatorItem.setSelected(false);
					sinusoidalItem.setSelected(false);
					modSinusoidalItem.setSelected(false);
					latLonItem.setSelected(false);
				}
				else
				{
					setFlatEarth("latLon");
					globeItem.setSelected(false);
					mercatorItem.setSelected(false);
					sinusoidalItem.setSelected(false);
					modSinusoidalItem.setSelected(false);
					latLonItem.setSelected(true);
					setFlatEarth("latLon");
					updateProjection();
				}
			}
			else if (event.getSource() == mercatorItem)
			{
				globeItem.setSelected(false);
				mercatorItem.setSelected(true);
				sinusoidalItem.setSelected(false);
				modSinusoidalItem.setSelected(false);
				latLonItem.setSelected(false);
				setFlatEarth("mercator");
				updateProjection();
			}
			else if (event.getSource() == sinusoidalItem)
			{
				globeItem.setSelected(false);
				mercatorItem.setSelected(false);
				sinusoidalItem.setSelected(true);
				modSinusoidalItem.setSelected(false);
				latLonItem.setSelected(false);
				setFlatEarth("sinusoidal");
				updateProjection();

			}
			else if (event.getSource() == modSinusoidalItem)
			{
				globeItem.setSelected(false);
				mercatorItem.setSelected(false);
				sinusoidalItem.setSelected(false);
				modSinusoidalItem.setSelected(true);
				latLonItem.setSelected(false);
				setFlatEarth("modSinusoidal");
				updateProjection();

			}
			else if (event.getSource() == latLonItem)
			{
				globeItem.setSelected(false);
				mercatorItem.setSelected(false);
				sinusoidalItem.setSelected(false);
				modSinusoidalItem.setSelected(false);
				latLonItem.setSelected(true);
				setFlatEarth("latLon");
				updateProjection();

			}
			else if (event.getSource() == backgroundColorItem)
			{
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				String s = (String) JOptionPane.showInputDialog(
					frame,
					"Enter color value (e.g. name, 0xFFFFFF, 255:255:0)",
					"Background Color",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null, "black");
				if ((s != null) && (s.length() > 0))
				{
					setBackgroundColor(s);
				}
			}
			else if (event.getSource() == collapseLinksItem)
			{
				// TBD: do we want to redraw the links immediately
				// or wait for a position change?
				if (collapseLinksItem.isSelected())
				{
					collapseLinks = true;
					refreshLinks();
					getWwd().redraw();
					if (sharedFrame != null)
						sharedFrame.wwjPanel.wwd.redraw();

				}
				else
				{
					collapseLinks = false;
					refreshLinks();
					getWwd().redraw();
					if (sharedFrame != null)
						sharedFrame.wwjPanel.wwd.redraw();

				}

			}
			else if (event.getSource() == symbolOffsetItem)
			{
				if (symbolOffsetItem.isSelected())
					symbolOffset = true;
				else
					symbolOffset = false;
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			else if (event.getSource() == multiFrameItem)
			{
				if (multiFrameItem.isSelected())
					multiFrame = true;
				else
					multiFrame = false;
				// Create multiframe if it doesn't exist and toggle visibility
				if (this.multiFrame)
				{
					if (this.sharedFrame == null)
					{
						// We need to remove the view control layer and select listener
						// before sharing the globe - we readd it later.
						this.removeViewControlLayer(this);
						Model modelB = new BasicModel(new Earth(), new LayerList(this.wwjPanel.getWwd().getModel().getLayers()));
						sharedFrame = new SdtApplication.AppFrame((WorldWindowGLCanvas) this.getWwd(), modelB, true, false, false);
						WWUtil.alignComponent(null, sharedFrame, AVKey.LEFT_OF_CENTER);
						this.addViewControlLayer(sharedFrame);
						this.addViewControlLayer(this);

						// TODO: TBD frameB.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					}
					sharedFrame.setVisible(this.multiFrame);
				}
				else if (!this.multiFrame)
				{
					if (this.sharedFrame != null)
						sharedFrame.setVisible(this.multiFrame);
				}
			}
		}


		private void removeUDLayers()
		{
			// Remove all layers associated with all nodes
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtNode theNode = i.next().getValue();
				theNode.returnRenderables(this);
				theNode.removeNodeFromAllLayers(this);
			}
			for (Iterator<Entry<String, SdtRegion>> i = regionTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtRegion theRegion = i.next().getValue();
				theRegion.removeSymbolFromLayer();
			}

			for (Iterator<Entry<String, SdtTile>> i = tileTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtTile theTile = i.next().getValue();
				theTile.removeLayer();

				SurfaceImage theImage = null;
				theImage = theTile.getSurfaceImage();

				if (null != theImage)
				{
					// LJT check if selected instead of removing each time
					getTileLayer().removeRenderable(theImage);
					getTileLayer().addRenderable(theImage);
				}
			}

			// Finally remove any lingering renderables
			// At present only links are in separate user defined layers.
			// Eventually all the object types should be handled this way.
			userDefinedLinkLabelLayer.removeAllAnnotations();
			userDefinedLinkLayer.removeAllRenderables();
			userDefinedMarkerLayer.setMarkers(new ArrayList<Marker>());

			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

		} // removeUDLayers


		private String loadState(String val)
		{
			StringBuilder contents = new StringBuilder();
			try
			{
				// use buffering, reading one line at a atime
				// FileReader always assumed default encoding is OK!
				BufferedReader input = new BufferedReader(new FileReader(val));
				try
				{
					String line = null;

					while ((line = input.readLine()) != null)
					{
						contents.append(line);
						contents.append(System.getProperty("line.separator"));
					}
				}
				finally
				{
					input.close();
				}
			}
			catch (IOException e)
			{
				System.out.println("Error reading " + val + " state from file ");
				return null;
			}

			return contents.toString();

		}


		private void refreshLinks()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtNode theNode = i.next().getValue();
				theNode.updateLinkPositions(getWwd().getSceneController().getDrawContext());
				theNode.recreateLinkLabels(this);
			}
		}


		public String getPipeName()
		{
			return pipe_name;
		}


		private boolean setPipeName(String val)
		{
			if (0 == val.length())
				return false; // no <spriteName> TODO: error handling
			pipe_name = val;

			// is this correct??
			if (pipeThread.isAlive())
				pipeThread.stopThread();

			// Start thread that opens ProtoPipe and listens for commands
			pipeThread = new PipeThread(this);
			pipeThread.start();

			return true;
		}


		private boolean setFlyTo(String val)
		{
			if (!this.enableSdtViewControls)
				return false;
			if (0 == val.length())
				return false;

			// For now, we just pan/zoom to the "center" of
			// the given "bgbounds" coordinates
			String[] coord = val.split(new String(","));
			if (coord.length < 2)
			{
				// TODO display bad coords error
				return false;
			}
			Float f = new Float(coord[1]);
			double lat = f.doubleValue();
			f = new Float(coord[0]);
			double lon = f.doubleValue();
			Float alt = (coord.length > 2) ? new Float(coord[2]) : new Float(0);
			// optional heading pitch zoom
			if (coord.length > 3 && !coord[3].equalsIgnoreCase("x"))
				setHeading(coord[3]);
			if (coord.length > 4 && !coord[4].equalsIgnoreCase("x"))
				setPitch(coord[4]);
			if (coord.length > 5 && !coord[5].equalsIgnoreCase("x"))
				setZoom(coord[5]);
			Position targetPos = Position.fromDegrees(lat,
				lon, alt);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();

			if (view != null)
			{
				view.goTo(new Position(targetPos, alt), alt);
			}
			return true;
		}


		private boolean setZoom(String val)
		{
			if (!this.enableSdtViewControls)
				return false;
			if (0 == val.length())
				return false;
			double n = new Double(val);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			view.setZoom(n);
			return true;
		}


		private boolean setHeading(String val)
		{
			if (!this.enableSdtViewControls)
				return false;
			if (0 == val.length())
				return false;
			double n = new Double(val);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			view.setHeading(Angle.fromDegrees(n));
			return true;
		}


		private boolean setPitch(String val)
		{
			if (!this.enableSdtViewControls)
				return false;
			if (0 == val.length())
				return false;
			double n = new Double(val);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			view.setPitch(Angle.fromDegrees(n));
			return true;
		}


		private boolean setBackgroundBounds(String val)
		{
			if (0 == val.length())
				return false;

			// For now, we just pan/zoom to the "center" of
			// the given "bgbounds" coordinates
			String[] coord = val.split(new String(","));
			if (4 != coord.length)
			{
				// TODO display bad coords error
				return false;
			}
			Float f = new Float(coord[1]);
			double upperLat = f.doubleValue();
			f = new Float(coord[0]);
			double upperLon = f.doubleValue();
			f = new Float(coord[3]);
			double lowerLat = f.doubleValue();
			f = new Float(coord[2]);
			double lowerLon = f.doubleValue();

			Position upperPos = Position.fromDegrees(upperLat,
				upperLon, 0.0);
			Position lowerPos = Position.fromDegrees(lowerLat,
				lowerLon, 0.0);
			Position midPos = Position.interpolate(0.5, upperPos,
				lowerPos);

			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			Globe globe = wwd.getModel().getGlobe();

			Vec4 upperCoord = globe.computePointFromPosition(upperPos);
			Vec4 lowerCoord = globe.computePointFromPosition(lowerPos);
			double viewHeight = 2 * upperCoord.distanceTo3(lowerCoord);
			viewHeight += globe.getElevation(midPos.getLatitude(),
				midPos.getLongitude());

			// For now, we use the distance between the two points as
			// the "altitude"
			// for our view to display the bgbounds "area of interest"
			// (TBD) Confirm and/or tweak this
			// view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
			// view, globe, midPos, view.getHeading(), view.getPitch(),
			// viewHeight));
			view.setCenterPosition(midPos);
			view.setZoom(viewHeight);

			((OrbitView) getWwd().getView()).setPitch(Angle.fromDegrees(0));
			((OrbitView) getWwd().getView()).setHeading(Angle.fromDegrees(0));

			return true;
		}


		private boolean setScale(String scale)
		{
			if (0 == scale.length() || currentSprite == null)
				return false; // TODO: error handling

			currentSprite.setScale(Float.valueOf(scale));

			return true;
		}


		private boolean setSprite(String spriteName)
		{
			if (0 == spriteName.length())
				return false; // no <spriteName> TODO: error handling
			currentSprite = spriteTable.get(spriteName);
			if (null == currentSprite)
			{
				// It's a new one
				currentSprite = new SdtSprite(spriteName);
				// If first entry to our table, squirrel away
				// default sprite.
				if (spriteTable.size() == 1 && !spriteName.equalsIgnoreCase("default"))
					defaultSprite = spriteName;
				spriteTable.put(spriteName, currentSprite);
			}
			return true;
		} // end SetSprite


		static public String findFile(String val)
		{
			// Rewrite all this using java path helper functions at some point
			// for now just get it DONE!
			if (val.length() == 0)
				return null;
			// See if the file was fully qualified
			val.trim();
			File f1 = new File(val);
			if (f1.exists())
				return val;

			// if a "path" prefix was specified, try to find it there
			String imageFilePath = null;
			if (null != imagePathPrefix)
			{

				String[] cmdTokens = imagePathPrefix.split(new String(";"));

				// On linux systems we allow ":" as the delimiter
				if (imagePathPrefix.contains(":") && !System.getProperty("os.name").startsWith("Windows"))
					cmdTokens = imagePathPrefix.split(new String(":"));

				for (int i = 0; i < cmdTokens.length && cmdTokens.length != 0; i++)
				{
					imageFilePath = new String(cmdTokens[i]);
					if (imageFilePath.startsWith("\""))
						imageFilePath = imageFilePath.substring(1, imageFilePath.length());
					if (imageFilePath.endsWith("\""))
						imageFilePath = imageFilePath.substring(0, (imageFilePath.length() - 1));

					if (!imageFilePath.endsWith("/"))
						imageFilePath = imageFilePath + "/";

					imageFilePath = new String(imageFilePath + val);
					imageFilePath.trim();

					f1 = new File(imageFilePath);
					if (!f1.exists())
					{
						// is it a relative path? (We would have found this earlier
						// in the logic on a linux box. For some reason windows relative
						// paths are not handled by java? Is this a 0.7 thing?
						if (imageFilePath.startsWith(".."))
						{
							imageFilePath = imageFilePath.substring(2, imageFilePath.length());
							imageFilePath = fc.getCurrentDirectory().getParent().concat(imageFilePath);

						}
						else
							imageFilePath = fc.getCurrentDirectory() + "\\" + imageFilePath;

						f1 = new File(imageFilePath);
						if (!f1.exists())
							continue;
					}
					return imageFilePath;
				}
			}

			// Try infer the name from the path of the current file if open
			if (openFile != null)
			{

				String openPathPrefix = openFile.getAbsolutePath().replace(
					openFile.getName(), "");
				// Chop off "windows" file attrs
				String[] cmdTokens = openPathPrefix.split(new String(":"));
				if (cmdTokens.length == 1)
				{
					imageFilePath = new String(cmdTokens[0]) + val;
					imageFilePath = imageFilePath.replaceAll("\\\\", "/");
				}
				else
				{
					imageFilePath = new String(cmdTokens[1]) + val;
					imageFilePath = imageFilePath.replaceAll("\\\\", "/");
				}
				imageFilePath.trim();
				f1 = new File(imageFilePath);

			}
			if (!f1.exists())
			{
				// finally check in the current dir
				imageFilePath = fc.getCurrentDirectory() + File.separator + val;
				imageFilePath.trim();
				f1 = new File(imageFilePath);
				if (!f1.exists())
				{
					// finally check in the current dir - hack for mac
					imageFilePath = fc.getCurrentDirectory() + "/" + val;
					imageFilePath.trim();
					f1 = new File(imageFilePath);

					if (!f1.exists())
					{
						// Finally look in the user's home directory
						imageFilePath = System.getProperty("user.home");
						imageFilePath = imageFilePath + "/" + val;
						imageFilePath.trim();
						f1 = new File(imageFilePath);
						if (!f1.exists())
						{
							System.out.println("imageFile " + imageFilePath + " does not exist.");
							return null;
						}
					}
				}
			}
			return imageFilePath;
		}


		private boolean loadTile(String val)
		{
			if ((0 == val.length()) || currentTile == null)
				return false;

			// For now, we just pan/zoom to the "center" of
			// the given "bgbounds" coordinates
			String[] coord = val.split(new String(","));
			if (4 != coord.length)
			{
				// TODO display bad coords error
				System.out.println("Bad tile coordinates");
				return false;
			}
			Float f = new Float(coord[1]);
			double upperLat = f.doubleValue();
			f = new Float(coord[0]);
			double upperLon = f.doubleValue();
			f = new Float(coord[3]);
			double lowerLat = f.doubleValue();
			f = new Float(coord[2]);
			double lowerLon = f.doubleValue();

			SurfaceImage theImage = currentTile.getSurfaceImage();

			if (theImage == null)
				return false;

			// Shouldn't be null, we create it in setImage
			getTileLayer().removeRenderable(theImage);

			theImage = new SurfaceImage(theImage.getImageSource(), Sector.fromDegrees(upperLat, lowerLat, upperLon, lowerLon));
			currentTile.addSurfaceImage(theImage);
			if (theImage != null)
				getTileLayer().addRenderable(theImage);
			return true;
		}


		private boolean setTile(String val)
		{
			if (0 == val.length())
				return false;

			currentTile = tileTable.get(val);

			if (currentTile == null)
			{
				currentTile = new SdtTile(val);
				tileTable.put(val, currentTile);
			}
			return true;
		}


		private boolean setTileImage(String val)
		{
			if (0 == val.length() || null == currentTile)
				return false; // no <imageFile>

			SurfaceImage theImage = currentTile.getSurfaceImage();
			if (null != theImage)
			{
				getTileLayer().removeRenderable(theImage);
				tileTable.remove(theImage);
			}

			if (val.equalsIgnoreCase("NONE"))
			{
				return true;
			}

			String fileName = findFile(val);
			if (fileName == null)
				return false;

			theImage = new SurfaceImage(fileName, Sector.EMPTY_SECTOR);
			currentTile.addSurfaceImage(theImage);

			return true;
		}


		private boolean setImage(String val)
		{
			if (currentSprite == null)
				return false;
			
			if (0 == val.length() || null == currentSprite)
				return false; // no <imageFile>

			String fileName = findFile(val);
			if (fileName == null)
				return false;

			try
			{
				// We don't know the sprite type until we try load it.
				// The load function will return either an icon sprite (SdtSprite)
				// or a model subclass (WWModel3D or SdtKml)
				SdtSprite newSprite = currentSprite.Load(fileName);
				if (newSprite == null)
					return false;

				// Replace the currentSprite with the newSprite
				try
				{
					spriteTable.remove(currentSprite.getName());
					spriteTable.put(newSprite.getName(), newSprite);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				currentSprite = newSprite;
				return true;

			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return false;
		}


		private boolean setNode(String nodeName)
		{
			if (0 == nodeName.length())
				return false; // no <nodeName>

			currentNode = nodeTable.get(nodeName);
			if (null == currentNode)
			{
				// It's a new one
				currentNode = new SdtNode(nodeName);

				// By default set the node sprite to the defaultSprite - which will be
				// the first sprite assigned by the user, if any. If none have been assigned
				// the transparant "default" sprite will be assigned.
				if (!spriteTable.isEmpty() && defaultSprite != null)
				{
					SdtSprite theSprite = spriteTable.get(defaultSprite);

					if (theSprite != null && theSprite.getType() != SdtSprite.Type.INVALID)
					{
						currentNode.setSprite(theSprite);
					}

				}
				else
				{
					// else assign dummy icon sprite for nodes that won't get assigned
					// sprites by the user. In this way we have something to follow
					setType("default");
				}
				currentNode.drawRenderables(this);

				currentNode.setNodeUpdate(true);

				nodeTable.put(nodeName, currentNode);
			}
			// we are following all nodes, add the node to the follow list
			if (followAll)
				setFollow(nodeName);

			return true;
		} // end SetNode


		private boolean setType(String type)
		{
			if (0 == type.length() || null == currentNode)
				return false; // no <Type>

			SdtSprite theSprite = spriteTable.get(type);

			if (theSprite != null && theSprite.getType() == SdtSprite.Type.INVALID)
				return false;
			if ((type.equalsIgnoreCase("NONE") && theSprite == null))
			{
				theSprite = new SdtSprite(type);
				theSprite.setType(SdtSprite.Type.NONE);
			}
			if (theSprite == null)
			{
				System.out.println("sdt3d.setType() The sprite being assigned does not exist. Using default sprite");
				theSprite = spriteTable.get("default");
				if (theSprite == null)
				{
					System.out.println("sdt3d.setType() Error the default sprite does not exist");
					return false;
				}
			}
			// There are several things to do here:
			// 1) Does the node have a position?
			// a) if so, add the sprite to its layer
			// b) else, just keep track
			// 2) Does the node already have a sprite?
			// a) if so, same or different?
			// b) if different, we need to replace the model or icon
			boolean newSprite = true;
			if (currentNode.isDisplayed())
			{
				// a) remove old model or icon from its layer if we are
				// changing it
				if (currentNode.hasSprite() && currentNode.getSprite().getType() != SdtSprite.Type.INVALID)
				{ // are we changing it?
					if (!currentNode.getSprite().getName().equalsIgnoreCase(theSprite.getName()))
					{
						switch (currentNode.getSprite().getType())
						{
							case MODEL:
								if (currentNode.getModel() != null)
									getNodeModelLayer().removeModel(currentNode.getModel());
								break;
							case ICON:
								if (currentNode.getIcon() != null)
									getNodeIconLayer().removeIcon(currentNode.getIcon());
								break;
							case KML:
								if (currentNode.getKmlController() != null)
									getNodeKmlModelLayer().removeRenderable(currentNode.getKmlController());
								break;
							case NONE:
								break;
							case INVALID:
								System.out.println("sprite "
									+ currentNode.getSprite().getName()
									+ " is INVALID!");
								break;
						}
						currentNode.setSprite(theSprite);
					}
					else // end node has a different sprite
						newSprite = false;

				}
				else
					// node has no sprite, assign it
					currentNode.setSprite(theSprite);

				// only add it to the layer if it's a changed or new
				if (newSprite)
				{
					// We may need to recompute symbol dimensions
					if (currentNode.hasSymbol())
						currentNode.getSymbol().setInitialized(false);
					switch (theSprite.getType())
					{
						case MODEL:
							if (currentNode.getModel() != null)
								getNodeModelLayer().addModel(currentNode.getModel());
							break;
						case ICON:
							if (currentNode.getIcon() != null)
								getNodeIconLayer().addIcon(currentNode.getIcon());
							break;
						case KML:
							if (currentNode.getKmlController() != null)
								getNodeKmlModelLayer().addRenderable(currentNode.getKmlController());
							break;
						case NONE:
							break;
						case INVALID:
							System.out.println("sprite "
								+ currentNode.getSprite().getName()
								+ " is INVALID!");
							break;
					}
				}
			}
			else
			{
				// current node isn't displayed, but assign the sprite so we have it
				// when it is.
				currentNode.setSprite(theSprite);
			}
			return true;
		} // end SetType


		private boolean setRegionPosition(String val)
		{
			if ((0 == val.length()) || currentRegion == null)
				return false;

			regionLayer.removeRenderables(currentRegion);

			currentRegion.setInitialized(false);

			// Get the LatLon of the symbol
			String[] coord = val.split(new String(","));

			if (coord.length < 2)
			{
				// TODO display bad coords error
				return false;
			}
			Float f = new Float(coord[1]);
			double lat = f.doubleValue();
			f = new Float(coord[0]);
			double lon = f.doubleValue();

			Float alt = (coord.length > 2) ? new Float(coord[2]) : new Float(0);

			Position p = Position.fromDegrees(lat, lon, alt);

			if (coord.length > 3 && coord[3].equalsIgnoreCase("c"))
			{
				p = getCartesianPosition(lon + "," + lat + "," + alt);
			}

			currentRegion.setPosition(p);
			return true;
		}


		private Position getCartesianPosition(String val)
		{
			// Parse comma-delimited "lat,lon,alt" coordinates
			String[] coord = val.split(",");
			if (coord.length < 3)
			{
				// TODO warn about bad coords -- shouldn't get here actually
				return null;
			}

			Float y = 0.0f;
			Float x = 0.0f;
			Float alt = 0.0f;
			if (!coord[1].equalsIgnoreCase("x"))
				x = new Float(coord[1]);
			if (!coord[0].equalsIgnoreCase("x"))
				y = new Float(coord[0]);
			if (coord.length > 2 && !coord[2].equalsIgnoreCase("x"))
			// leave node at last known altitude if not set
			{
				alt = new Float(coord[2]);
			}
			String origLatStr = originLocation.getLatitude().toDecimalDegreesString(6);
			origLatStr = origLatStr.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", "");
			double origLat = Double.parseDouble(origLatStr);

			String origLonStr = originLocation.getLongitude().toDecimalDegreesString(6);
			origLonStr = origLonStr.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", "");
			double origLon = Double.parseDouble(origLonStr);

			// average radius of the earth from center to pole
			double latsPerDeg = 111226.0;
			double latsRel = y / latsPerDeg;
			double lat = origLat + latsRel;

			// normalize the latitude... but dont account for pole crossings yet...
			while (lat < -180)
				lat += 360;
			while (lat > 180)
				lat -= 360;

			// Radius of the earth from center to the equator...
			double lonsPerDeg = Math.cos(Math.PI * lat / 180) * 111320;
			double lonRel = x / lonsPerDeg;
			double lon = origLon + lonRel;

			// handle pole crossings...
			if (lat > 90)
			{
				lat = 180 - lat;
				lon += 180;
			}
			else if (lat < -90)
			{
				lat = -180 - lat;
				lon += 180;
			}

			// normalize the longitude
			while (lon < 180)
				lon += 360;
			while (lon > 180)
				lon -= 360;

			Position newPos = Position.fromDegrees(lat, lon, alt);

			return newPos;
		}


		private boolean setEnableSdtViewControls(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setEnableSdtViewControls() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				// When we turn focus off we reenable the view controller
				if (focusNode == null)
					this.viewController.setEnabled(true);
				this.enableSdtViewControls = true;
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.viewController.setEnabled(false);
				this.enableSdtViewControls = false;
				return true;
			}
			System.out.println("setEnableSdtViewControls() error invalid value\n");
			return false;

		} // setEnableSdtViewControls


		private boolean setShowLayerPanel(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setShowLayerPanel() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				this.showLayersItem.setSelected(true);
				this.sdtLayerPanel.setVisible(true);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.showLayersItem.setSelected(false);
				this.sdtLayerPanel.setVisible(false);
				return true;
			}
			System.out.println("setShowLayerPanel() error invalid value\n");
			return false;

		} // setEnableSdtViewControls


		private boolean setShowSdtPanel(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setSdtPanel() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				this.showSdtPanelItem.setSelected(true);
				this.westPanel.setVisible(true);
				horizontalSplitPane.setLeftComponent(westPanel);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.showSdtPanelItem.setSelected(false);
				this.westPanel.setVisible(false);
				return true;
			}
			System.out.println("setShowLayerPanel() error invalid value\n");
			return false;

		} // setSdtPanel


		private boolean setShowStatusPanel(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setShowStatusPanel() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				this.showStatusItem.setSelected(true);
				this.statusPanel.setVisible(true);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.showStatusItem.setSelected(false);
				this.statusPanel.setVisible(false);
				return true;
			}
			System.out.println("setShowStatusPanel() error invalid value\n");
			return false;

		} // setShowStatusPanel


		private boolean setShowScenarioPlaybackPanel(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setShowScenarioPlaybackPanel() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				this.showScenarioPlaybackItem.setSelected(true);
				this.scenarioPlaybackPanel.setVisible(true);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.showScenarioPlaybackItem.setSelected(false);
				this.scenarioPlaybackPanel.setVisible(false);
				return true;
			}
			System.out.println("setShowScenarioPlaybackPanel() error invalid value\n");
			return false;

		} // setShowScenarioPlaybackPanel


		private boolean setShowWmsFrame(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setShowWmsFrame() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				if (this.wmsFrame == null)
				{
					buildWmsLayerFrame();
				}
				this.showWmsItem.setSelected(true);
				this.wmsFrame.setVisible(true);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.showWmsItem.setSelected(false);
				if (this.wmsFrame != null)
					this.wmsFrame.setVisible(false);
				return true;
			}
			System.out.println("setShowWmsFrame() error invalid value\n");
			return false;

		} // setShowWmsPanel


		private boolean setShowSdtControlPanel(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setShowSdtControlPanel() error invalid value\n");
				return false;
			}
			if (val.equalsIgnoreCase("on"))
			{
				this.showSdtViewControlsItem.setSelected(true);
				this.sdtViewControlsPanel.setVisible(true);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				this.showSdtViewControlsItem.setSelected(false);
				this.sdtViewControlsPanel.setVisible(false);
				return true;
			}
			System.out.println("setSdtViewControlsPanel() error invalid value\n");
			return false;

		} // setShowSdtViewControls


		private boolean setLogDebugOutput(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setLogDebugOutput() error invalid value\n");
				return false;
			}
			String[] file = val.split(",");

			if (file[0].equalsIgnoreCase("on"))
			{
				logDebugOutput = true;
				debugItem.setSelected(true);

				// if no filename, log to sdtout
				if (file.length > 1)
				{
					try
					{
						// TODO: error handling
						if (file.length > 0)
						{
							String filename = file[1];
							int index = 1;
							while (true)
							{
								File theFile = new File(filename);

								if (theFile.exists() && theFile.isFile())
								{
									filename = file[1] + "." + index;
									index++;
								}
								else
								{
									break;
								}
							}
							fileWriter = new FileWriter(filename);
							logDebugFile = new PrintWriter(fileWriter);
						}
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				logDebugOutput = false;
				debugItem.setSelected(false);
				try
				{
					fileWriter.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (logDebugFile != null)
				{
					logDebugFile.close();
					logDebugFile = null;
				}
				return true;
			}
			System.out.println("setLogDebugOutput() error invalid value\n");
			return false;

		} // setLogDebugOutput


		private boolean startStopScenarioRecording(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setRecordScenario() error invalid value\n");
				return false;
			}
			String[] file = val.split(",");

			if (file[0].equalsIgnoreCase("on"))
			{
				recordScenario = true;
				scenarioController.initController();
				
			}
			else
			{
				if (val.equalsIgnoreCase("off"))
				{
					if (scenarioThread != null) 
					{ 
						scenarioThread.stopRecording();
						stopScenarioThread();
					}
					scenarioController.reset();
					scenarioThread = null;						
					recordScenario = false;
					playbackScenario = false;
					playbackStopped = false; // consolidate these booleans
					playbackOnly = false;

				}
				else
				{
					System.out.println("setRecordScenario() error invalid value\n");
					return false;
				}
			}

			return true;

		} // setRecordScenario

		
		private boolean setLoadCache(String val)
		{
			if (0 == val.length())
			{
				System.out.println("setLoadCache() error invalid value\n");
				return false;
			}

			String fileName = findFile(val);
			if (fileName == null)
				return false;

			if (!new File(fileName).isDirectory())
			{
				System.out.println("setLoadCache() error invalid cache directory\n");
			}

			FileStore wwjCache = WorldWind.getDataFileStore();
			wwjCache.addLocation(0, fileName, true);

			return false;

		} // setLoadCache


		private boolean setOrientation(String val)
		{
			if ((0 == val.length()) || currentNode == null)
				return false; // no <params>
			// If no sprite, orientation not valid but don't need to
			// trigger warning
			if (!currentNode.hasSprite())
				return true;

			currentNode.setNodeUpdate(true);

			// Parse comma-delimited "pitch,yaw,roll"
			String[] coord = val.split(",");
			Double pitch = 999.0;
			Double yaw = 999.0;
			Double roll = 999.0;
			if (!coord[0].equalsIgnoreCase("x"))
				currentNode.setPitch(new Double(coord[0]));

			if (coord.length > 1 && !coord[1].equalsIgnoreCase("x"))
			{
				if (coord[1].endsWith("a"))
				{
					coord[1] = coord[1].replace("a", "");
					currentNode.setYaw(new Double(coord[1]));
					currentNode.getSprite().setAbsoluteYaw(true);

				}
				else if (coord[1].endsWith("r"))
				{
					coord[1] = coord[1].replace("r", "");
					currentNode.getSprite().setAbsoluteYaw(false);
					currentNode.setYaw(new Double(coord[1]));
				}

			}
			if (coord.length > 2 && !coord[2].equalsIgnoreCase("x"))
			{
				currentNode.setRoll(new Double(coord[2]));
			}

			// view.setFieldOfView(Angle.fromDegrees(this.fovSlider.getValue()));
			// LJT TEST TBD: Here? node render function?
			// if (0 == 1 && this.enableSdtViewControls &&
			// currentNode.hasFocus())
			if (this.enableSdtViewControls && currentNode.hasFocus())
			{
				// As changing the view is on the event queue, we need to
				// store the currentNode's position
				focusPosition = currentNode.getPosition();
				// ljt testing
				if (pitch != 999.0)
					focusPitch = pitch;
				if (yaw != 999.0)
					focusYaw = yaw;
				if (roll != 999.0)
					focusRoll = roll;

				SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							Position eyePosition = getWwd().getView().getCurrentEyePosition();
							getWwd().getView().setOrientation(eyePosition, focusPosition);
							// getWwd().getView().setEyePosition(currentNode.getPosition());
							View view = getWwd().getView();
							if (focusPitch != 999.0)
								view.setPitch(Angle.fromDegrees(focusPitch));
							// if (focusYaw != 999.0)
							// view.setHeading(Angle.fromDegrees(focusYaw));
							if (focusRoll != 999.0)
								view.setRoll(Angle.fromDegrees(focusRoll));
						}
					});
			}

			return true;
		}


		private boolean setPosition(String val)
		{
			if ((0 == val.length()) || currentNode == null)
				return false; // no <coordinates>

			// Parse comma-delimited "lat,lon,alt" coordinates
			String[] coord = val.split(",");
			if (coord.length < 2)
			{
				// TODO warn about bad coords
				return false;
			}

			currentNode.setNodeUpdate(true);

			boolean firstPosition = !currentNode.hasPosition();
			Double lon = 0.0;
			Double lat = 0.0;
			if (!coord[1].equalsIgnoreCase("x"))
				lat = new Double(coord[1]);
			else if (!firstPosition)
				if (currentNode.isCartesian())
					lat = new Double(currentNode.getCartesianLat());
				else
					lat = new Double(currentNode.getPosition().getLatitude().degrees);

			if (!coord[0].equalsIgnoreCase("x"))
				lon = new Double(coord[0]);
			else if (!firstPosition)
				if (currentNode.isCartesian())
					lon = new Double(currentNode.getCartesianLon());
				else
					lon = new Double(currentNode.getPosition().getLongitude().degrees);

			if ((lat == -9999.0 || lon == -9999.0))
			{
				if (firstPosition)
					return true;
			}

			Double alt = currentNode.getAltitude();
			if (coord.length > 2 && !coord[2].equalsIgnoreCase("x"))
				// leave node at last known altitude if not set
				alt = new Double(coord[2]);

			if (coord.length > 3 && !coord[3].equalsIgnoreCase("x"))
			{
				if (coord[3].equalsIgnoreCase("c"))
					currentNode.setCartesian(true);
				else if (coord[3].equalsIgnoreCase("g"))
					currentNode.setCartesian(false);
			}
			if (coord.length > 4 && !coord[4].equalsIgnoreCase("x"))
			{
				if (coord[4].equalsIgnoreCase("agl"))
					currentNode.setUseAbsoluteElevation(false);
				else if (coord[4].equalsIgnoreCase("msl"))
					currentNode.setUseAbsoluteElevation(true);
			}

			// We save the altitude separately as we may need it depending on what
			// we are rendering
			currentNode.setAltitude(alt);
			currentNode.setFollowTerrain(alt == 0);
			Position p = Position.fromDegrees(lat, lon, alt);
			if (currentNode.isDisplayed())
			{
				if (currentNode.isCartesian())
				{
					currentNode.setCartesianLat(new Float(lat));
					currentNode.setCartesianLon(new Float(lon));
					currentNode.setPosition(getCartesianPosition(lon + "," + lat + "," + alt));
				}
				else
					currentNode.setPosition(p);
			}
			else
			{
				// We are displaying this node for the first time, add it to its layer
				if (currentNode.isCartesian())
				{
					currentNode.setCartesianLat(new Float(lat));
					currentNode.setCartesianLon(new Float(lon));
					currentNode.setPosition(getCartesianPosition(lon + "," + lat + "," + alt));
				}
				else
					currentNode.setPosition(p);

				if (currentNode.hasSprite() && currentNode.nodeInVisibleLayer())
				{
					switch (currentNode.getSprite().getType())
					{
						case MODEL:
							if (currentNode.getModel() != null)
								getNodeModelLayer().addModel(currentNode.getModel());
							break;
						case ICON:
							if (currentNode.getIcon() != null)
								getNodeIconLayer().addIcon(currentNode.getIcon());
							break;
						case KML:
							if (currentNode.getKmlController() != null)
								getNodeKmlModelLayer().addRenderable(currentNode.getKmlController());
							break;
						case NONE:
							break;
						case INVALID:
							System.out.println("sprite "
								+ currentNode.getSprite().getName()
								+ " is INVALID!");
							break;
					}
				} // end currentNode.hasSprite()

				if (currentNode.nodeInVisibleLayer())
				{
					GlobeAnnotation label = currentNode.getLabel();
					if (null != label)
					{
						getNodeLabelLayer().addAnnotation(label);
					}
				}
			} // end else (firstDisplay)
			if (firstPosition && currentNode.nodeInVisibleLayer())
			{

				if (currentNode.hasTrail())
				{
					getTrailLayer().removeRenderable(currentNode.getPath());
					getTrailLayer().addRenderable(currentNode.getPath());
				}
				Map<String, List<SdtLink>> linkTable = currentNode.getLinkTable();
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
			// TODO: consolidate all of this
			if (currentNode.hasTrail() && currentNode.nodeInVisibleLayer())
			{
				// Can we use fire property change or something like that??
				getTrailLayer().removeRenderable(currentNode.getPath());
				getTrailLayer().addRenderable(currentNode.getPath());
			}

			// Update the view in case we're keeping any nodes in the frustrum
			if (viewController.isEnabled())
				viewController.sceneChanged();

			// ljt TBD: Here? node render function?
			if (this.enableSdtViewControls &&
				currentNode.hasFocus())
			{
				// As changing the view is on the event queue, we need to
				// store the currentNode's position
				focusPosition = currentNode.getPosition();
				SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							View view = getWwd().getView();
							Angle fov = view.getFieldOfView();
							Angle pitch = view.getPitch();
							Angle heading = view.getHeading();
							Double zoom = ((OrbitView) view).getZoom();
							Position eyePosition = view.getCurrentEyePosition();
							view.setOrientation(eyePosition, focusPosition);
							view.setPitch(pitch);
							view.setHeading(heading);
							view.setFieldOfView(fov);
							((OrbitView) view).setZoom(zoom);

						}
					});
			}

			return true;
		} // end SetPosition


		// Deletes all objects assigned to a layer from the sdt
		// object lists. They will not subsequently be available
		// in any other layer.
		private boolean clearLayer(String val)
		{

			String objectName = val;
			if (!objectName.startsWith("User Defined"))
				objectName = "User Defined::" + objectName;

			SdtCheckboxNode theLayer = (SdtCheckboxNode) sdtLayerPanel.findLeaf(objectName);

			if (theLayer == null)
			{
				System.out.println("sdt3d::clear layer,<" + objectName + "> not found");
				return false;
			}
			if (!theLayer.isLeaf())
			{
				System.out.println("sdt3d::clear layer,<" + objectName + "> is not a leaf, cannot be cleared");
				return false;
			}
			theLayer.setSelected(true);
			theLayer.clearLayer(this);
			this.getSdtLayerPanel().update(getWwd(), objectName);

			return true;
		}


		// Clear deletes all objects of the specified type or all objects
		// The SPRITE table is not cleared - delete all DOES clear the sprite table
		// The redundancy in clear/delete should be cleaned up
		private boolean clear(String val)
		{ // check to see if val is valid, valueOf crashes if it isn't
			boolean valid = false;

			// Special case to clear a layer name
			String[] layer = val.split(",");
			if (layer.length > 1)
			{
				if (layer[0].equalsIgnoreCase("layer"))
					return clearLayer(layer[1]);

				else
					System.out.println("clear() Invalid object type: " + val);
				return false;
			}

			for (ObjectList olv : ObjectList.values())
			{
				if (olv.toString().equalsIgnoreCase(val))
					valid = true;
			}
			if (!valid)
			{
				System.out.println("Invalid object type: " + val);
				return false;
			}

			ObjectList ol = ObjectList.valueOf(val.toUpperCase());

			switch (ol)
			{
				case ALL:
					// Removing nodes removes associated links, labels, sprites, and symbols
					removeNodes();
					removeRegions();
					removeTiles();
					removeLinkLabels();
					removeKml();
					clearAllRenderables();
					this.elevationBuilder.clear();
					setStatus("");
					if (fileThread != null)
					{
						fileThread.stopThread();
						fileThread = null;
					}
					break;
				case NODES:
					removeNodes();
					break;
				case LABELS:
					removeLabels();
					break;
				case LINKS:
					removeLinks();
					removeLinkLabels();
					break;
				case TRAILS:
					removeTrails();
					break;
				case LINKLABELS:
					removeLinkLabels();
					break;
				case SYMBOLS:
					removeSymbols();
					break;
				case SPRITES:
					removeSprites();
					break;
				case REGIONS:
					removeRegions();
					break;
				case TILES:
					removeTiles();
					break;
				case ELEVATIONOVERLAY:
					this.elevationBuilder.clear();
					break;
				case KML:
					removeKml();
					break;
				case INVALID:
					System.out.println("Invalid object type for clear command");
					break;
				default:
					System.out.println("Invalid object type for clear command");
					break;
			}

			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

			return true;
		} // clear


		private void removeNode(SdtNode current_node)
		{
			// Remove the node and all its components from all layers
			// including user defined layer assignment
			if (current_node == null)
				return;

			// Remove the node & all its associated renderables
			// from the wwj layers
			current_node.removeRenderables(this);

			// Now remove the node & associated components
			// including from any user-defined layers
			current_node.removeLinksFromLayers(this);

			if (current_node.hasLabel())
			{
				current_node.deleteLabel();
			}

			if (current_node.getSprite() != null)
			{
				current_node.deleteSprite();
			}
			if (current_node.hasSymbol())
			{
				// ljt test but we do this above in remove renderables...
				getSymbolLayer().removeSymbol(current_node.getSymbol());
				current_node.deleteSymbol();
			}
			if (current_node.hasTrail())
			{
				getTrailLayer().removeRenderable(current_node.getPath());
				current_node.deleteTrail();
			}
			// remove node and all its' associated elements & renderables
			getNodeLayer().removeRenderable(current_node);

			// We need to pretend the node is drawn so removeNode toggle function doesn't
			// redraw it.
			current_node.setDrawn(true);
			current_node.removeNodeFromAllLayers(this);

		} // end removeNode


		private void removeNodes()
		{
			// Remove all nodes
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtNode theNode = i.next().getValue();
				removeNode(theNode);
				i.remove();
			}
		}


		private void removeLinkLabels()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				Map<String, List<SdtLink>> linkTable = current_node.getLinkTable();
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
							GlobeAnnotation theLabel = theLink.getLabel();
							if (theLabel != null)
							{
								theLink.getLinkLabelLayer().removeAnnotation(theLabel);
								theLink.removeLabel();
							}
						}
					}
					// it.remove();
				}
			}
		}


		private void removeLinks()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				// We don't use finalize because we want to delete any
				// associated markers & linkLabels (we need access to the
				// rendering layers)
				// current_node.finalize(linkLayer);

				Map<String, List<SdtLink>> linkTable = current_node.getLinkTable();
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
					it.remove();
				}
			}
		}


		private void removeLabels()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				// Remove all labels
				// Remove associated sprite,label,symbol,&links
				if (current_node.hasLabel())
				{
					getNodeLabelLayer().removeAnnotation(current_node.getLabel());
					current_node.deleteLabel();
				}
			}
		}


		private void removeSymbols()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				if (current_node.hasSymbol())
				{
					// ljt use clear all?
					getSymbolLayer().removeSymbol(current_node.getSymbol());
					current_node.deleteSymbol();
				}
			}
		}


		private void removeTrails()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				if (current_node.hasTrail())
				{
					// ljt use clear all?
					getTrailLayer().removeRenderable(current_node.getPath());
					current_node.deleteTrail();
				}
			}
		}


		private void removeSprites()
		{
			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				if (current_node.hasSprite())
				{
					if (current_node.hasSprite() && current_node.getSprite().getName().equalsIgnoreCase("default"))
						continue;
					
					switch (current_node.getSprite().getType())
					{
						case MODEL:
							if (current_node.getModel() != null)
								getNodeModelLayer().removeModel(current_node.getModel());
							break;
						case ICON:
							if (current_node.getIcon() != null) // ljt rework
								getNodeIconLayer().removeIcon(current_node.getIcon());
							break;
						case KML:
							if (current_node.getSprite() instanceof SdtSpriteKml &&
								((SdtSpriteKml) current_node.getSprite()).getKmlController() != null)
								getNodeKmlModelLayer().removeRenderable(((SdtSpriteKml) current_node.getSprite()).getKmlController());
							break;
						case NONE:
							break;
						case INVALID:
							System.out.println("sprite "
								+ current_node.getSprite().getName()
								+ " is INVALID!");
							break;
					}
				}
				if (current_node.getSprite() != null)
				{
					current_node.deleteSprite();
				}
			}
		}


		private void removeRegions()
		{
			// remove all regions
			for (Iterator<Entry<String, SdtRegion>> i = regionTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtRegion theRegion = i.next().getValue();
				regionLayer.removeRenderables(theRegion);
				regionLayer.removeRegion(theRegion);
				i.remove();
			}
		}


		private void removeTiles()
		{
			// remove all images
			getTileLayer().removeAllRenderables();

		}


		private void removeKml()
		{
			// remove all kml
			for (Iterator<Entry<String, SdtSpriteKml>> i = kmlTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtSpriteKml theKml = i.next().getValue();
				if (theKml.getKmlMenuItem() != null)
					kmlMenu.remove(theKml.getKmlMenuItem());
				getKmlLayer().removeRenderable(theKml.getKmlController());
				i.remove();
			}
			// Clear the panel tree
			this.kmlPanelLayerTree.getModel().removeAllLayers();
		}


		private void clearAllRenderables()
		{
			// Occasionally we have dangling renderables, clear them
			// all in a final step just in case.
			nodeLayer.removeAllRenderables();
			trailLayer.removeAllRenderables();
			nodeIconLayer.removeAllIcons();
			nodeLabelLayer.removeAllAnnotations();
			linkLabelLayer.removeAllAnnotations();
			userDefinedLinkLabelLayer.removeAllAnnotations();
			linkLayer.removeAllRenderables();
			userDefinedLinkLayer.removeAllRenderables();
			regionLayer.removeAllRenderables();
			symbolLayer.removeAllRenderables();
			tileLayer.removeAllRenderables();
			markerLayer.setMarkers(new ArrayList<Marker>());
			userDefinedMarkerLayer.setMarkers(new ArrayList<Marker>());
			kmlLayer.removeAllRenderables();
		}


		// Delete deletes a specific object identified by name or all objects of
		// the specified type (redundant with clear - should be cleaned up)
		// Note that delete all deletes ALL object types while clear does not.
		boolean delete(String nodeName)
		{
			String[] attr = nodeName.split(",");
			String objectName = null;
			if (attr.length == 1 && !attr[0].equalsIgnoreCase("all"))
			{
				// use deprecated delete node command
				attr[0] = new String("node");
				objectName = new String(nodeName);
			}
			else
			{
				if (attr.length > 1)
					objectName = new String(attr[1]);
				else
				{
					// else its a delete all command
					attr[0] = new String("all");
					objectName = new String("all");
				}
			}
			if (objectName.length() == 0)
				return false; // no object name

			if (attr[0].equalsIgnoreCase("all"))
			{
				// Note that delete all deletes sprites whereas
				// clear all does not
				objectName = "all";
				delete("node,all");
				delete("label,all");
				delete("link,all");
				delete("region,all");
				delete("sprite,all");
				delete("tile,all");
				delete("symbol,all");
				delete("layer,all");
				delete("trail,all");
				delete("kml,all");
				// Clear all geoTiff's
				this.elevationBuilder.clear();
				this.getSdtLayerPanel().update(getWwd(), "wwj");

				imagePathPrefix = null;
			}

			else if (attr[0].equalsIgnoreCase("node"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeNodes();
					nodeTable.clear();
					return true;
				}

				SdtNode theNode = nodeTable.get(objectName);
				if (theNode != null)
				{
					removeNode(theNode);
					nodeTable.remove(theNode.getName());
				}
			}
			else if (attr[0].equalsIgnoreCase("region"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeRegions();
					regionTable.clear();
					return true;
				}

				SdtRegion theRegion = regionTable.get(objectName);
				if (theRegion != null)
				{
					regionLayer.removeRenderables(theRegion);
					regionLayer.removeRegion(theRegion);
					regionTable.remove(theRegion.getName());
				}
			}
			else if (attr[0].equalsIgnoreCase("link"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeLinks();
					removeLinkLabels();
					return true;
				}

				// rebuild linkName
				String[] linkName = nodeName.split("link,");
				setUnlink(linkName[1]);
			}
			else if (attr[0].equalsIgnoreCase("tile"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeTiles();
					tileTable.clear();
					return true;
				}

				SurfaceImage theImage = null;
				theImage = tileTable.get(objectName).getSurfaceImage();

				if (null != theImage)
				{
					getTileLayer().removeRenderable(theImage);
					tileTable.remove(objectName);
				}
			}
			else if (attr[0].equalsIgnoreCase("sprite"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeSprites();
					spriteTable.clear();
					// Readd default sprite
					java.net.URL dummyIcon = getClass().getResource("/images/dummyIcon.png");
					setSprite("default");
					try
					{
						currentSprite.LoadURL(dummyIcon);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				}

				SdtSprite theSprite = null;
				theSprite = spriteTable.get(objectName);

				if (null != theSprite)
				{
					spriteTable.remove(theSprite.getName());
				}
			}
			else if (attr[0].equalsIgnoreCase("symbol"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeSymbols();
					return true;
				}
				System.out.println("INFO: Named symbol deletion not supported.");
			}
			else if (attr[0].equalsIgnoreCase("trail"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeTrails();
					return true;
				}
				System.out.println("INFO: Named trail deletion not supported.");
			}

			else if (attr[0].equalsIgnoreCase("label"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeLabels();
					return true;
				}
				System.out.println("INFO: Named label deletion not supported.");
			}
			else if (attr[0].equalsIgnoreCase("elevationOverlay"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					this.elevationBuilder.clear();
				}
				else
				{
					this.elevationBuilder.removeModel(objectName);
				}
			}
			else if (attr[0].equalsIgnoreCase("geoTiff"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					this.elevationBuilder.clear();
				}
				else
				{
					this.elevationBuilder.removeModel(objectName);
				}
			}
			else if (attr[0].equalsIgnoreCase("kml"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeKml();
					kmlTable.clear();
					return true;
				}

				SdtSpriteKml theKml = null;
				theKml = kmlTable.get(objectName);

				if (null != theKml)
				{
					this.kmlPanelLayerTree.getModel().getRoot().removeChild(theKml.getLayerNode());
					kmlMenu.remove(theKml.getKmlMenuItem());
					kmlLayer.removeRenderable(theKml.getKmlController());
					kmlTable.remove(objectName);

				}

			}
			else if (attr[0].equalsIgnoreCase("layer"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					removeUDLayers();
					this.getSdtLayerPanel().update(getWwd(), "all");
					return true;
				}
				if (!objectName.startsWith("User Defined"))
					objectName = "User Defined::" + objectName;

				SdtCheckboxNode theLayer = (SdtCheckboxNode) sdtLayerPanel.findLeaf(objectName);

				if (theLayer == null)
				{
					System.out.println("sdt3d::delete layer " + objectName + " not found");
					return false;
				}
				if (!theLayer.isLeaf())
				{
					System.out.println("sdt3d::delete layer " + objectName + " is not a leaf, cannot be deleted");
					return false;
				}
				theLayer.setSelected(true);
				theLayer.deleteAll();
				this.getSdtLayerPanel().update(getWwd(), objectName);
			}
			return true;
		}


		private boolean setSize(String val)
		{
			if ((0 == val.length()))
				return false; // no size

			if (null == currentSprite)
			{
				// TODO warn about no "node" specified
				return false;
			}
			String[] dim = val.split(",");
			// <dimensions> is in format "width,height"
			if (dim.length < 2)
			{
				// TODO warn about bad width,height
				return false;
			}
			Integer width = new Integer(dim[0]);
			Integer height = new Integer(dim[1]);

			currentSprite.setSize(width.intValue(), height.intValue());
			// If we are setting a size specifically, we are not
			// a real size model - e.g. the model will stay in sight
			// as the view changes
			currentSprite.setRealSize(false);
			return true;
		}


		private boolean setLength(String val)
		{
			if ((0 == val.length()))
				return false;

			if (null == currentSprite)
			{
				return false;
			}
			Float length = new Float(val);
			if (currentSprite.getType() == SdtSprite.Type.ICON)
			{
				System.out.println("sdt3d.setLength() Length not applicable for icons, use the size attribute.");
				return false;
			}
			currentSprite.setFixedLength(length.doubleValue());

			return true;
		}


		private boolean setLight(String val)
		{
			if ((0 == val.length()))
				return false; // need on/off

			if (null == currentSprite)
			{
				// TODO warn about no "node" specified
				return false;
			}
			if (!(currentSprite instanceof SdtSpriteModel))
				return false;
			String status = val;
			if (status.equalsIgnoreCase("on"))
			{
				((SdtSpriteModel) currentSprite).setUseLighting(true);
			}
			else if (status.equalsIgnoreCase("off"))
			{
				((SdtSpriteModel) currentSprite).setUseLighting(false);
			}
			else
			{
				// TODO warn about bad status
				return false;
			}
			return true;
		}


		public static boolean followAll()
		{
			return followAll;
		}


		private boolean setFollow(String val)
		{
			if (0 == val.length())
				return false;

			if (val.equalsIgnoreCase("on"))
			{
				viewController.setEnabled(true);
				return true;
			}
			if (val.equalsIgnoreCase("off"))
			{
				viewController.setEnabled(false);
				return true;
			}
			String[] attrs = val.split(",");
			int index = 0;
			// Check type in case we eventually want to follow things other than nodes..
			if (attrs[0].equalsIgnoreCase("node"))
			{
				if (attrs.length < 2)
					return false;
				index = 1;
			}
			if (attrs[0 + index].equalsIgnoreCase("all"))
			{

				// if we are focused on a node, disallow follow
				// we can add support for both if there is interest
				// currently focus takes precedence
				if (followAll && focusNode != null)
				{
					System.out.println("setFollow() focus is on " + focusNode + " follow currently disabled");
					return false;
				}

				if ((attrs.length + index > 1) && attrs[1 + index].equalsIgnoreCase("off"))
				{
					viewController.setEnabled(false);
					followAll = false;
				}
				else
				{
					viewController.setEnabled(true);
					followAll = true;
				}

				for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
				{
					// Only one call to i.next() allowed!
					SdtNode theNode = i.next().getValue();
					theNode.setFeedbackEnabled(followAll);
				}
			}
			if (attrs.length > 1 && attrs[1 + index].equalsIgnoreCase("false") && focusNode != null)
			{
				System.out.println("setFollow() focus is on " + focusNode + " follow currently disabled");
				return false;
			}
			SdtNode theNode = nodeTable.get(attrs[0 + index]);
			if (theNode == null)
				return false;
			if ((attrs.length + index) > 1 && attrs[1 + index].equalsIgnoreCase("off"))
				theNode.setFeedbackEnabled(false);
			else
				theNode.setFeedbackEnabled(true);
			return true;
		} // setFollow


		private boolean setFocus(String val)
		{
			if (0 == val.length())
				return false;

			SdtNode theFocusNode = null;
			// turn off any current focus
			if (focusNode != null)
			{
				theFocusNode = nodeTable.get(focusNode);
				if (theFocusNode != null)
					theFocusNode.setFocus(false);
				focusNode = null;
			}

			if (val.equalsIgnoreCase("off"))
			{
				// we already disabled focus, reenable view controller in
				// case we were following anynodes
				viewController.setEnabled(true);
				return true;
			}

			theFocusNode = nodeTable.get(val);
			if (null == theFocusNode)
			{
				System.out.println("setFocus() node " + val + " not found");
				return false;
			}

			// Disable following, we could support both
			// if there is interest TBD
			viewController.setEnabled(false);
			theFocusNode.setFocus(true);
			focusNode = val;

			return true;
		} // setFocus


		boolean setLabel(String val)
		{
			if ((0 == val.length()))
				return false; // no label
			if (null == currentNode)
			{
				// TODO warn about no "node" specified
				return false;
			}

			currentNode.setNodeUpdate(true);

			String[] attrs = val.split(",");
			if (attrs.length > 1)
			{
				// Remove any quotes
				String str = attrs[1];
				str = str.replaceAll("\"", "");
				str = str.replaceAll("\'", "");

				currentNode.setLabelText(str);
			}

			if (attrs[0].equalsIgnoreCase("off"))
			{
				if (currentNode.hasLabel())
				{
					getNodeLabelLayer().removeAnnotation(currentNode.getLabel());
				}
				// Remove label sets showlabel to false (and therefore we
				// don't keep recreating it!)
				currentNode.deleteLabel();

			}
			else
			{
				currentNode.showLabel();
				if (!attrs[0].equalsIgnoreCase("on"))
					currentNode.setLabelColor(getColor(attrs[0]));

				if (!currentNode.hasLabel() && currentNode.hasPosition())
				{
					GlobeAnnotation label = currentNode.getLabel();
					if (null != label)
					{
						getNodeLabelLayer().addAnnotation(label);

					}
				}
			}
			return true;
		} // end label


		boolean setTrail(String val)
		{

			if ((0 == val.length()))
				return false; // no trail

			if (null == currentNode)
			{
				// TODO warn about no "node" specified
				return false;
			}
			String[] attrs = val.split(",");

			if (attrs[0].equalsIgnoreCase("off"))
			{
				if (currentNode.hasTrail())
				{
					currentNode.setTrail(false);
					getTrailLayer().removeRenderable(currentNode.getPath());
				}

			}
			else
			{
				currentNode.setTrail(true);
				if (!attrs[0].equalsIgnoreCase("on"))
					currentNode.setTrailColor(getColor(attrs[0]));
			}

			if (attrs.length > 1 && !attrs[1].equalsIgnoreCase("x"))
			{
				currentNode.setTrailLength(new Integer(attrs[1]));
			}
			if (attrs.length > 2 && !attrs[2].equalsIgnoreCase("x"))
			{
				// Changing renderable type
				getTrailLayer().removeRenderable(currentNode.getPath());
				if (attrs[2].equalsIgnoreCase("dirPath"))
				{
					currentNode.setDirected(true);
				}
				else if (attrs[2].equalsIgnoreCase("path"))
				{
					currentNode.setDirected(false);
				}
				else
				{
					System.out.println("sdt3d.setTrail() invalid path command [dirPath|path]");
					return false;
				}
				getTrailLayer().addRenderable(currentNode.getPath());
			}
			if (attrs.length > 3 && !attrs[3].equalsIgnoreCase("x"))
			{
				currentNode.setTrailPosScale(new Integer(attrs[3]));
			}
			if (attrs.length > 4 && !attrs[4].equalsIgnoreCase("x"))
			{
				currentNode.setTrailOutlineWidth(new Integer(attrs[4]));
			}
			if (attrs.length > 5 && !attrs[5].equalsIgnoreCase("x"))
			{
				if (attrs[5].equalsIgnoreCase("y"))
					currentNode.showTrailPosition(true);
				else
					currentNode.showTrailPosition(false);
			}
			return true;
		} // end setTrail


		boolean setSymbol(String val)
		{

			if ((0 == val.length()) || null == currentNode)
				return false; // wait for symbol type

			// TODO: Error handling
			String[] attrs = val.split(",");
			int x = attrs.length;

			if (attrs.length < 1)
				return false; // wait for symbol type

			// See if it's a valid symbol type
			String symbolType = attrs[0];
			if (SdtSymbol.getType(symbolType) == SdtSymbol.Type.INVALID)
			{
				System.out.println("Invalid symbol type: " + symbolType);
				return false;
			}

			if (currentNode.hasSymbol() && currentNode.getSymbol() != null)
			{
				getSymbolLayer().removeSymbol(currentNode.getSymbol());

				if (symbolType.equalsIgnoreCase("none"))
				{
					// Only remove the layer assignement if we are removing the symbol
					// otherwise it remains in the existing layer
					currentNode.getSymbol().removeSymbolFromLayer();
					currentNode.setSymbol(null);
					return true;
				}
				if (symbolType.equalsIgnoreCase("CONE") && !(currentNode.getSymbol() instanceof SdtCone))
				{
					currentNode.getSymbol().removeSymbolFromLayer();
					currentNode.setSymbol(null);

					// Remove airspace symbol and create cone
					currentSymbol = new SdtCone(symbolType, currentNode);
				}
				else
				{
					// If the existing symbol is an airspace - need to recreate it
					if (currentNode.getSymbol() instanceof SdtCone && !symbolType.equalsIgnoreCase("CONE"))
					{
						currentNode.getSymbol().removeSymbolFromLayer();
						currentNode.setSymbol(null);

						currentSymbol = new SdtSymbol(symbolType, currentNode);
					}
					else
						currentSymbol = currentNode.getSymbol();
				}

				currentSymbol.setType(symbolType); // we might be changing it
			}
			else
			{
				// Else we don't have anything to remove or create
				if (symbolType.equalsIgnoreCase("none"))
					return true;
				// ljt check this
				if (symbolType.equalsIgnoreCase("CONE"))
					currentSymbol = new SdtCone(symbolType, currentNode);
				else
					currentSymbol = new SdtSymbol(symbolType, currentNode);
			}
			currentSymbol.setInitialized(false);

			// symbol
			// <symbolName>[,<color>,[<thickness>[,<width>[,<height>[,<opacity>[,<scale>[,<lAzimuth>[,<rAzimuth>]]]]]]
			for (int ind = 1; ind < x; ind++)
			{
				String useDefault = String.valueOf(attrs[ind]);
				if (useDefault.equalsIgnoreCase("X"))
					continue;
				switch (ind)
				{
					case 1:
					{
						currentSymbol.setColor(getColor(attrs[ind]));
						break;
					}
					case 2:
					{
						currentSymbol.setOutlineWidth(Integer.valueOf(attrs[ind]));
						break;
					}
					case 3:
					{
						String width = attrs[ind];
						if (width.endsWith("s"))
						{
							currentSymbol.setScalable(true);
							width = width.replace("s", "");
						}
						currentSymbol.setWidth(Double.valueOf(width));
						currentSymbol.setIconHugging(false);

						break;
					}
					case 4:
					{
						String height = attrs[ind];
						if (height.endsWith("s"))
						{
							currentSymbol.setScalable(true);
							height = height.replace("s", "");
						}
						currentSymbol.setHeight(Double.valueOf(height));
						currentSymbol.setIconHugging(false);

						break;
					}
					case 5:
					{
						currentSymbol.setOpacity(Double.valueOf(attrs[ind]));
						break;
					}
					case 6:
					{
						currentSymbol.setScale(Float.valueOf(attrs[ind]));
						break;
					}
					case 7:
					{
						String absolutePositioning = attrs[ind];
						if (absolutePositioning.endsWith("a"))
						{
							currentSymbol.setAbsolutePositioning(true);
							absolutePositioning = absolutePositioning.replace("a", "");
						}
						if (absolutePositioning.endsWith("r"))
						{
							currentSymbol.setAbsolutePositioning(false);
							absolutePositioning = absolutePositioning.replace("r", "");
						}

						double lAzimuth = Double.valueOf(absolutePositioning);
						if (lAzimuth < 0 || lAzimuth > 360)
						{
							System.out.println("Error: Symbol lAzimuth out of range (0-360)" + lAzimuth);
							lAzimuth = 0;
						}
						currentSymbol.setLAzimuth(lAzimuth);
						break;
					}
					case 8:
					{
						// LJT ADD Error checking 0-360
						currentSymbol.setRAzimuth(Double.valueOf(attrs[ind]));
						break;
					}
				}
			}

			// ljt store all symbol state!! for recreating later
			currentNode.setSymbolType(symbolType);
			currentNode.setSymbol(currentSymbol);
			currentSymbol.initialize(getWwd().getSceneController().getDrawContext());

			// if layers are empty or visible, add the new symbol (clean this up!!)
			if (currentNode.getSymbol().getLayerList().isEmpty()
				&&
				currentNode.getLayerList().isEmpty())
			{
				getSymbolLayer().addSymbol(currentSymbol);

			}
			else if (!currentNode.getSymbol().getLayerList().isEmpty() && currentNode.getSymbol().symbolInVisibleLayer() ||
				!currentNode.getLayerList().isEmpty() && currentNode.nodeInVisibleLayer())
			{
				getSymbolLayer().addSymbol(currentSymbol);
			}

			return true;
		} // end SetSymbol


		private boolean setLine(String val)
		{
			if (currentLinkSet.isEmpty())
				return false;
			if (0 == val.length())
				return false;

			String[] attr = val.split(",");

			Iterator<SdtLink> itr = currentLinkSet.iterator();
			while (itr != null && itr.hasNext())
			{
				SdtLink theLink = itr.next();

				theLink.getSrcNode().setLinkUpdate(true);

				if (!attr[0].equalsIgnoreCase("x"))
				{
					theLink.setColor(getColor(attr[0]));
				}
				if (attr.length > 1 && !attr[1].equalsIgnoreCase("x"))
				{
					theLink.setWidth(Double.valueOf(attr[1]));
				}
				if (attr.length > 2 && !attr[2].equalsIgnoreCase("x"))
				{
					theLink.setOpacity(Double.valueOf(attr[2]));
				}
				if (attr.length > 3 && !attr[3].equalsIgnoreCase("x"))
				{
					theLink.setStippleFactor(Integer.valueOf(attr[3]));
				}
				// TODO error handling
				if (attr.length > 4 && !attr[4].equalsIgnoreCase("x"))
				{
					if (attr[4].startsWith("0x") || attr[4].startsWith("0X"))
					{
						attr[4] = attr[4].trim();
						String x[] = attr[4].split("0x");
						if (x.length < 2)
						{

							x = attr[4].split("0X");
							if (x.length < 2)
							{
								System.out.println("sdt3d::setLine() hex value required. ");
								return false;
							}
						}
						boolean isHex = x[1].matches("[0-9A-Fa-f]+");
						if (x[1].length() != 4 || !isHex)
						{
							System.out.println("sdt3d::setLine() invalid hex value");
							return false;
						}
						int stipplePattern = Integer.parseInt(x[1], 16);
						theLink.setStipplePattern(Integer.valueOf(Integer.valueOf(stipplePattern)).shortValue());
					}
					else
					{
						theLink.setStipplePattern(Integer.valueOf(attr[4]).shortValue());
					}
				}

			}
			return true;
		}


		private boolean setLinkLabel(String val)
		{
			if (currentLinkSet.isEmpty())
				return false;
			if ((0 == val.length()))
				return false; // no label

			Iterator<SdtLink> itr = currentLinkSet.iterator();
			while (itr != null && itr.hasNext())
			{
				SdtLink theLink = itr.next();

				theLink.getSrcNode().setLinkUpdate(true);

				String[] attrs = val.split(",");

				if (attrs[0].equalsIgnoreCase("off"))
				{
					if (theLink.hasLabel())
					{
						theLink.getLinkLabelLayer().removeAnnotation(theLink.getLabel());
						theLink.removeLabel();
					}
					if (attrs.length > 1)
						theLink.setLabelText(attrs[1]);
				}
				else
				{
					theLink.setShowLabel();
					if (attrs.length > 1)
						theLink.setLabelText(attrs[1]);
					if (!attrs[0].equalsIgnoreCase("on"))
						theLink.setLabelColor(getColor(attrs[0]));
					if (!theLink.hasLabel()
						&& theLink.hasPosition()
						&& !theLink.isHidden()
						&& theLink.linkInVisibleLayer())
					{
						GlobeAnnotation label = theLink.getLabel();
						if (null != label)
						{
							theLink.getLinkLabelLayer().addAnnotation(label);
						}
					}
				}
			} // end linkSet while
			return true;
		} // end label


		// Merge delete directional&birectional links into one subroutine
		private void deleteDirectionalLinks(SdtNode node1, SdtNode node2, String linkId)
		{
			List<SdtLink> links = node1.getLinksTo(node2);
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink> itr = links.iterator();

				while (itr != null && itr.hasNext())
				{
					SdtLink link = itr.next();

					if (((link.getLinkID() == null && linkId == null) ||
						(linkId != null && link.getLinkID() != null && link.getLinkID().equals(linkId)))
						&&
						link.isDirectional())
					{
						link.removeRenderables();
					}
				}

				node1.removeDirectionalLinkTo(node2, linkId, true);
				node2.removeDirectionalLinkTo(node1, linkId, true);

			}

		}


		private void deleteBiDirectionalLinks(SdtNode node1, SdtNode node2, String linkId)
		{
			List<SdtLink> links = node1.getLinksTo(node2);
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink> itr = links.iterator();

				while (itr != null && itr.hasNext())
				{

					SdtLink link = itr.next();

					if (((link.getLinkID() == null && linkId == null) ||
						(linkId != null && link.getLinkID() != null && link.getLinkID().equals(linkId)))
						&&
						!link.isDirectional())
					{
						link.removeRenderables();
					}
				}
				node1.removeDirectionalLinkTo(node2, linkId, false);
				node2.removeDirectionalLinkTo(node1, linkId, false);
			}

		}


		private boolean getMultiLinkSet(String val)
		{
			String[] attr = null;
			boolean allNode1 = false;
			boolean allNode2 = false;
			String linkId = null;
			boolean directional = false;
			boolean allLinks = false;
			boolean allDirections = false;

			// TODO: support ipv6
			attr = val.split(",");

			if (attr.length < 2)
				return false;
			SdtNode node1 = nodeTable.get(attr[0]);
			if (null == node1 && !attr[0].equalsIgnoreCase("all"))
				return false;
			if (attr[0].equalsIgnoreCase("all"))
				allNode1 = true;
			SdtNode node2 = nodeTable.get(attr[1]);
			if (null == node2 && !attr[1].equalsIgnoreCase("all"))
				return false;
			if (attr[0].equalsIgnoreCase("all"))
				allNode2 = true;

			// if attr[2] is not a valid sdt3d color assume it's a linkID
			// and get the remaining attributes
			if (attr.length > 2 && !validateColor(attr[2]))
			{
				linkId = attr[2];
				if (linkId.equalsIgnoreCase("all"))
				{
					allLinks = true;
					linkId = null;
				}
				if (linkId != null && linkId.equals(""))
					linkId = null;

				if (attr.length > 3)
				{
					if (attr[3].equalsIgnoreCase("dir"))
						directional = true;
					if (attr[3].equalsIgnoreCase("all"))
						allDirections = true;
				}
			}

			for (Iterator<Entry<String, SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();

				Map<String, List<SdtLink>> linkTable = current_node.getLinkTable();
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
							// Does the link match our link id?
							if ((theLink.getLinkID() == null && linkId == null) ||
								((linkId != null && theLink.getLinkID() != null) && theLink.getLinkID().equals(linkId))
								|| allLinks)
							{

								// get all links
								if (allNode1 && allNode2)
								{
									currentLinkSet.add(theLink);
								}
								else
								{
									if (allNode1)
									{
										// get all links going to node2
										if (theLink.getDstNode().getName().equalsIgnoreCase(node2.getName()))
											currentLinkSet.add(theLink);
									}
									else
									{
										// get all links going to node1
										if (theLink.getSrcNode().getName().equalsIgnoreCase(node1.getName()))
											currentLinkSet.add(theLink);
									}
								}
							}
						}
					}
				}
			}
			if (allDirections)
				// we have the set of all links in all directions.
				// Wait for subsequent line attribute commands
				return true;

			List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
			Iterator<SdtLink> itr = currentLinkSet.iterator();
			while (itr != null && itr.hasNext())
			{
				SdtLink tmpLink = itr.next();
				if (directional && tmpLink.isDirectional())
					tmpLinkList.add(tmpLink);
				if (!directional && !tmpLink.isDirectional())
					tmpLinkList.add(tmpLink);
			}
			// We have the set of all links in the specified
			// direction. Wait for subsequent line attribute commands.
			currentLinkSet = tmpLinkList;
			return true;
		} // end getMultilinkSet


		private boolean setLink(String val)
		{
			// Here we build a currentLinkSet of all links selected in the
			// link command (e.g. link x,y,all) for subsequent attribute
			// changes
			if (0 == val.length())
				return false;

			// TODO: account for ipv6 ip addresses (contain :'s)
			SdtNode node1 = null;
			SdtNode node2 = null;
			String linkId = null;
			boolean directional = false;
			boolean allLinks = false;
			boolean allDirections = false;
			String[] attr = null;
			// TODO: support ipv6
			attr = val.split(",");

			if (attr.length < 2)
			{
				if (currentLinkSet != null)
				{
					currentLinkSet.clear();
				}
				return false;
			}
			node1 = nodeTable.get(attr[0]);
			if (null == node1 && !attr[0].equalsIgnoreCase("all"))
			{
				if (currentLinkSet != null)
				{
					currentLinkSet.clear();
				}
				return false;
			}
			node2 = nodeTable.get(attr[1]);
			if (null == node2 && !attr[1].equalsIgnoreCase("all"))
			{
				if (currentLinkSet != null)
				{
					currentLinkSet.clear();
				}
				return false;
			}

			// if attr[2] is not a valid sdt3d color assume it's a linkID
			// and get the remaining attributes
			if (attr.length > 2 && !validateColor(attr[2]))
			{
				linkId = attr[2];
				if (linkId.equalsIgnoreCase("all"))
				{
					allLinks = true;
					linkId = null;
				}
				if (linkId != null && linkId.equals(""))
					linkId = null;

				if (attr.length > 3)
				{
					if (attr[3].equalsIgnoreCase("dir"))
						directional = true;
					if (attr[3].equalsIgnoreCase("all"))
						allDirections = true;
				}
			}
			if (currentLinkSet != null)
			{
				currentLinkSet.clear();
			}

			// TBD: Only set an update for one end of the link
			node1.setLinkUpdate(true);

			if (attr[0].equalsIgnoreCase("all") || attr[1].equalsIgnoreCase("all"))
			{
				return getMultiLinkSet(val);
			}

			if (allLinks)
			{ // get all links between the two nodes
				if (node1.getLinksTo(node2) != null)
					currentLinkSet.addAll(node1.getLinksTo(node2));
				if (node2.getLinksTo(node1) != null)
					currentLinkSet.addAll(node2.getLinksTo(node1));

				if (allDirections)
					// we have the set of all links in all directions.
					// Wait for subsequent line attribute commands
					return true;

				List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
				Iterator<SdtLink> itr = currentLinkSet.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink tmpLink = itr.next();
					if (directional && tmpLink.isDirectional())
						tmpLinkList.add(tmpLink);
					if (!directional && !tmpLink.isDirectional())
						tmpLinkList.add(tmpLink);
				}
				// We have the set of all links in the specified
				// direction. Wait for subsequent line attribute commands.
				currentLinkSet = tmpLinkList;
				return true;
			}
			else
			{
				// else we want all links associated with the linkId in
				// either direction
				if (allDirections)
				{
					if (node1.getLinksTo(node2) != null)
						currentLinkSet.addAll(node1.getLinksTo(node2));
					if (node2.getLinksTo(node1) != null)
						currentLinkSet.addAll(node2.getLinksTo(node1));

					List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
					Iterator<SdtLink> itr = currentLinkSet.iterator();
					while (itr != null && itr.hasNext())
					{
						SdtLink tmpLink = itr.next();
						if ((tmpLink.getLinkID() == null && linkId == null) ||
							((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
							tmpLinkList.add(tmpLink);
					}
					currentLinkSet = tmpLinkList;
					return true;
				}
			}

			// Otherwise if it's a bidirectional link, delete any directional
			// links associated with the link id (merge into common subroutine!)
			if (!directional)
			{
				deleteDirectionalLinks(node1, node2, linkId);
				deleteDirectionalLinks(node2, node1, linkId);
			}
			else
			{
				// else delete any bidirectional links for the linkId
				deleteBiDirectionalLinks(node1, node2, linkId);// ljt tbd: add directionality to hash table?
			}

			// Do we need to create a directional/bidirectional link?
			List<SdtLink> links = node1.getLinksTo(node2);

			SdtLink link = null;
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink> itr = links.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink tmpLink = itr.next();
					if (((tmpLink.getLinkID() == null && linkId == null) ||
						((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
						&&
						((directional && tmpLink.isDirectional()) || (!directional && !tmpLink.isDirectional())))
					{
						if (!directional ||
							directional && tmpLink.getDstNode().getName().equals(node2.getName()))
						{
							link = tmpLink;
							break;
						}
					}
				}
			}
			if (null == link)
			{
				// Until we rework all this passing the app around - send theApp
				// to the link so we can check whether network links are enabled
				// when we calculate the number of links that are displayed.
				link = new SdtLink(this, node1, node2, linkId);
				link.setDirectional(directional);
				link.drawRenderables(false);
			}
			currentLinkSet.add(link);

			// If attribute 3 is a valid color update "line"
			// color/thickness for legacy link commands
			if (attr.length > 2 && validateColor(attr[2]))
			{
				if (attr.length > 2)
					link.setColor(getColor(attr[2]));
				if (attr.length > 3)
				{
					Double thickness = new Double(attr[3]);
					// if (thickness < 1)
					// ljt thickness = 1;
					link.setWidth(thickness);
				}
			}

			return true;
		}


		private boolean unlinkList(List<SdtLink> deleteLinkList)
		{
			Iterator<SdtLink> itr = deleteLinkList.iterator();
			SdtNode node1 = null;
			SdtNode node2 = null;
			String linkId = null;
			while (itr != null && itr.hasNext())
			{
				try
				{
					SdtLink link = itr.next();
					// We need to remove link renderables here so we can pass a ref to the app
					link.removeRenderables();
					node1 = link.getSrcNode();
					node2 = link.getDstNode();
					linkId = link.getLinkID();
					node1.removeLinkTo(node2, linkId);
					node2.removeLinkTo(node1, linkId);
				}
				catch (NullPointerException e)
				{
					// Tried to unlink an already unlinked pair
				}
			}
			// Clear currentLinkSet so any subsequent link attributes don't
			// create phantom links
			currentLinkSet.clear();
			return true;
		}


		// TODO: make the set selection a common subroutine ljt!!
		private boolean setUnlink(String val)
		{
			if (0 == val.length())
				return false;
			SdtNode node1 = null;
			SdtNode node2 = null;
			String linkId = null;
			boolean directional = false;
			boolean allLinks = false;
			boolean allDirections = false;
			String[] attr = null;
			// TODO: support ipv6
			attr = val.split(",");

			if (attr.length < 2)
				return false;
			node1 = nodeTable.get(attr[0]);
			if (null == node1)
				return false;
			node2 = nodeTable.get(attr[1]);
			if (null == node2)
				return false;

			// if attr[2] is not a valid sdt3d color assume it's a linkID
			// and get the remaining attributes
			if (attr.length > 2 && !validateColor(attr[2]))
			{
				linkId = attr[2];
				if (linkId.equalsIgnoreCase("all"))
				{
					allLinks = true;
					linkId = null;
				}
				if (linkId != null && linkId.equals(""))
					linkId = null;
				if (attr.length > 3)
				{
					if (attr[3].equalsIgnoreCase("dir"))
						directional = true;
					if (attr[3].equalsIgnoreCase("all"))
						allDirections = true;
				}
			}
			List<SdtLink> deleteLinkList = new ArrayList<SdtLink>();
			if (allLinks)
			{ // get all links between the two nodes
				if (node1.getLinksTo(node2) != null)
					deleteLinkList.addAll(node1.getLinksTo(node2));
				if (node2.getLinksTo(node1) != null)
					deleteLinkList.addAll(node2.getLinksTo(node1));

				if (allDirections)
					// we have the set of all links in all directions.
					return unlinkList(deleteLinkList);

				List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
				Iterator<SdtLink> itr = deleteLinkList.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink tmpLink = itr.next();
					if (directional && tmpLink.isDirectional())
						tmpLinkList.add(tmpLink);
					if (!directional && !tmpLink.isDirectional())
						tmpLinkList.add(tmpLink);
				}
				// We have the set of all links in the specified
				// direction.
				return unlinkList(tmpLinkList);
			}
			else
			{
				// else we want all links associated with the LinkId in
				// either direction
				if (allDirections)
				{
					if (node1.getLinksTo(node2) != null)
						deleteLinkList.addAll(node1.getLinksTo(node2));
					if (node2.getLinksTo(node1) != null)
						deleteLinkList.addAll(node2.getLinksTo(node1));

					List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
					Iterator<SdtLink> itr = deleteLinkList.iterator();
					while (itr != null && itr.hasNext())
					{
						SdtLink tmpLink = itr.next();
						if ((tmpLink.getLinkID() == null && linkId == null) ||
							((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
							tmpLinkList.add(tmpLink);
					}
					return unlinkList(tmpLinkList);
				}
			}
			// Do we need to delete a directional/bidirectional link w/the linkId?
			List<SdtLink> links = node1.getLinksTo(node2);

			SdtLink link = null;
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink> itr = links.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink tmpLink = itr.next();
					if (((tmpLink.getLinkID() == null && linkId == null) ||
						((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
						&&
						((directional && tmpLink.isDirectional()) || (!directional && !tmpLink.isDirectional())))
					{
						if (!directional ||
							directional && tmpLink.getDstNode().getName().equals(node2.getName()))
						{
							link = tmpLink;
							break;
						}
					}
				}
			}
			if (links != null)
			{
				deleteLinkList.add(link);
				return unlinkList(deleteLinkList);
			}
			// no links !
			return false;
		} // end unlink


		private boolean setPath(String val)
		{
			if (0 == val.length())
				return false; // wait for dir

			imagePathPrefix = val;
			if (!imagePathPrefix.endsWith("/"))
				imagePathPrefix = imagePathPrefix + "/";

			return true;
		}


		private boolean setLookAt(String val)
		{
			if (!this.enableSdtViewControls)
				return false;
			/** Minimum time for animation, in milliseconds. */
			// long MIN_LENGTH_MILLIS = 4000;
			long MIN_LENGTH_MILLIS = 1000;
			/** Maximum time for animation, in milliseconds. */
			long MAX_LENGTH_MILLIS = 16000;

			if (0 == val.length())
				return false;

			String[] coord = val.split(new String(","));
			if (coord.length < 3)
			{
				// TODO display bad origin error
				System.out.println("setLookAt() error.  Invalid lookAt coordinates %s\n" + val);
				return false;
			}
			// lat,lon,altitude,heading,pitch,zoom
			// LJT ADD x's!!
			Float f = new Float(coord[1]);
			double latitude = f.doubleValue();
			f = new Float(coord[0]);
			double longitude = f.doubleValue();
			f = new Float(coord[2]);
			double altitude = f.doubleValue();

			double heading = 0.0; // Default to NORTH, valid range 0 - 360
			double pitch = 0.0; // Default to 0 (straight down), valid range 0 - 90
			double range = altitude; // Distance in meters for point lat,lon,alt

			if (coord.length > 3 && !coord[3].equalsIgnoreCase("x"))
			{
				f = new Float(coord[3]);
				if (f < 0 || f > 360)
				{
					System.out.println("setLookAt() error.  Invalid heading %f (0-360)\n" + coord[3]);
				}
				else
					heading = f.doubleValue();
			}
			if (coord.length > 4 && !coord[4].equalsIgnoreCase("x"))
			{
				f = new Float(coord[4]);
				if (f < 0 || f > 90)
				{
					System.out.println("setLookAt() error.  Invalid pitch %f (0-90)\n" + coord[4]);
				}
				else
					pitch = f.doubleValue();
			}
			if (coord.length > 5 && !coord[5].equalsIgnoreCase("x"))
			{
				f = new Float(coord[5]);
				range = f.doubleValue();
			}

			// TODO: ljt <gx:horizFov> and <gx:altitudeMode> <gx:FlyTo> attributes not yet supported
			// <altitudeMode>clampToGround</altitudeMode>
			// <!--kml:altitudeModeEnum:clampToGround, relativeToGround, absolute -->
			// <!-- or, gx:altitudeMode can be substituted: clampToSeaFloor, relativeToSeaFloor -->

			BasicOrbitView theView = (BasicOrbitView) getWwd().getView();
			theView.setViewOutOfFocus(true); // ljt?
			theView.stopAnimations();
			boolean animate = false;
			// Setting eye position seems to result in smoother "animation"
			if (animate)
			{
				theView.stopAnimations();
				// theView.stopMovementOnCenter(); unless this is better with setting zoom?
				Position endCenterPosition = Position.fromDegrees(latitude, longitude, altitude);

				long timeToMove = AnimationSupport.getScaledTimeMillisecs(
					theView.getEyePosition(), endCenterPosition,
					MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
				((BasicOrbitView) getWwd().getView()).addPanToAnimator(theView.getCenterPosition(), endCenterPosition,
					theView.getHeading(), Angle.fromDegrees(heading),
					theView.getPitch(), Angle.fromDegrees(pitch),
					theView.getZoom(), range, timeToMove, true); // true = endSurfaceOnCenter
			}
			else
			{
				if (altitude == 0)
					altitude = range;
				double initPitch = theView.getPitch().degrees;
				theView.setEyePosition(Position.fromDegrees(latitude, longitude, altitude));
				theView.setHeading(Angle.fromDegrees(heading));
				while (initPitch < pitch)
				{
					initPitch++;
					theView.setPitch(Angle.fromDegrees(initPitch));
					getWwd().redraw();
					if (sharedFrame != null)
						sharedFrame.wwjPanel.wwd.redraw();

				}
				theView.setPitch(Angle.fromDegrees(pitch));
				theView.setZoom(range);
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			return true;
		} // setLookAt


		private boolean setStatus(String val)
		{

			if ((0 == val.length()))
			{
				this.getStatusPanel().setText("");
			}
			else
			{
				// Remove any quotes that didn't get stripped out
				String str = val;
				str = str.replaceAll("\"", "");
				str = str.replaceAll("\'", "");
				this.getStatusPanel().setText(str);
			}
			return true;
		} // end SetStatus


		private boolean setRegion(String regionName)
		{
			if (0 == regionName.length())
				return false; // no region name

			currentRegion = regionTable.get(regionName);

			if (currentRegion == null)
			{
				currentRegion = new SdtRegion(regionName);
				regionTable.put(regionName, currentRegion);
				regionLayer.addRegion(currentRegion);
			}
			return true;
		}


		private boolean setShape(String val)
		{
			if ((0 == val.length()) || currentRegion == null)
				return false;

			// TODO: parsing needs error handling!
			String[] attrs = val.split(",");
			int x = attrs.length;

			if (attrs.length < 1)
				return false; // no region type

			if (SdtRegion.getType(attrs[0]) == SdtRegion.Type.INVALID)
			{
				System.out.println("Invalid region type " + attrs[0]);
				return false;
			}
			currentRegion.setType(attrs[0]);
			regionLayer.removeRenderables(currentRegion);

			if (SdtRegion.getType(attrs[0]) == SdtRegion.Type.NONE)
			{
				// In case we are removing it
				return false;
			}
			// symbol
			// <regionType>[,<color>,[<thickness>[,<x_radius>[,<y_radius>[,<opacity>[,<lAzimuth>[,<rAzimuth>]]]]]]
			for (int ind = 1; ind < x; ind++)
			{
				String useDefault = String.valueOf(attrs[ind]);
				if (useDefault.equalsIgnoreCase("X"))
					continue;

				switch (ind)
				{
					case 1:
					{
						currentRegion.setColor(getColor(attrs[ind]));
						break;
					}
					case 2:
					{
						currentRegion.setOutlineWidth(Integer.valueOf(attrs[ind]));
						break;
					}
					case 3:
					{
						currentRegion.setWidth(Double.valueOf(attrs[ind]));
						break;
					}
					case 4:
					{
						currentRegion.setHeight(Double.valueOf(attrs[ind]));
						break;
					}
					case 5:
					{
						currentRegion.setOpacity(Double.valueOf(attrs[ind]));
						break;
					}
					// cylinder only
					case 6:
					{
						currentRegion.setLAzimuth(Double.valueOf(attrs[ind]));
						break;
					}
					// cylinder only
					case 7:
					{
						currentRegion.setRAzimuth(Double.valueOf(attrs[ind]));
						break;
					}
					default:
						break;
				}

			}

			currentRegion.setInitialized(false);

			return true;
		}


		private boolean setAppTitle(String title)
		{
			if (0 == title.length())
				return false;

			this.setTitle(title);
			return true;
		}


		private boolean setDefaultAltitudeType(String val)
		{
			if (0 == val.length())
				return false;

			if (val.equalsIgnoreCase("agl"))
				useAbsoluteElevation = false;
			if (val.equalsIgnoreCase("msl"))
				useAbsoluteElevation = true;

			return true;
		}


		private boolean setPopup(String val)
		{
			// TODO: fix to only change text
			if (0 == val.length())
				return false;

			JTextArea text = new JTextArea(val);
			text.setWrapStyleWord(true);
			text.setLineWrap(true);
			JScrollPane scroll = new JScrollPane(text);
			scroll.setPreferredSize(new Dimension(300, 350));
			scroll.setBackground(getBackground());
			scroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), new TitledBorder("Info")));
			JPanel panel = new JPanel(new BorderLayout());
			smallLogoPanel.setPreferredSize(new Dimension(300, 64));
			smallLogoPanel.revalidate();
			panel.add(smallLogoPanel, BorderLayout.NORTH);
			panel.add(scroll, BorderLayout.SOUTH);
			popupFrame.setContentPane(panel);
			popupFrame.setVisible(true);
			popupFrame.revalidate();
			popupFrame.pack();
			return true;
		}


		private boolean setPopdown()
		{
			popupFrame.setVisible(false);
			return true;
		}


		private boolean setView(String val)
		{
			if (0 == val.length())
				return false;
			String theView = viewTable.get(val);

			if (theView != null)
				((OrbitView) getWwd().getView()).restoreState(theView);

			currentView = val; // ljt
			return true;
		}


		private boolean setViewXml(String val, boolean loadView)
		{
			if (0 == val.length() || currentView == null)
				return false;

			// First look for file via normal lookup rules in case it is fully qualified
			String fileName = findFile(val);

			// else look for file in config directory
			if (fileName == null)
				fileName = findFile(configDirName + System.getProperty("file.separator") + val);

			if (fileName == null)
			{
				System.out.println("View xml " + val + " not found.");
				return false;
			}
			String viewState = loadState(fileName);
			if (viewState == null)
			{
				System.out.println("Loading view state for " + val + " failed.");
				currentView = null;
				return false;
			}
			// If we're just loading default bookmarks we don't want to set the view
			if (loadView)
				((OrbitView) getWwd().getView()).restoreState(viewState);

			String theView = viewTable.get(currentView);
			if (theView == null)
			{
				JMenuItem newItem;
				bookmarkMenu.add(newItem = new JMenuItem(currentView));
				newItem.addActionListener(this);
			}
			viewTable.put(currentView, viewState);
			currentView = null;
			return true;
		}


		private boolean setBackgroundColor(String theColor)
		{

			Color bgColor = getColor(theColor);

			// Override red default background
			if (bgColor == Color.red && !theColor.equalsIgnoreCase("red"))
				bgColor = new Color(0, 0, 0, 0);

			((SdtBasicSceneController) this.getWwd().getSceneController()).setClearColor(bgColor);
			return true;
		}


		public boolean isFlatGlobe()
		{
			return getWwd().getModel().getGlobe() instanceof FlatGlobe;
		}


		// Update flat globe projection
		private void updateProjection()
		{
			if (!isFlatGlobe())
				return;

			// Update flat globe projection
			this.flatGlobe.setProjection(this.getProjection());
			this.getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

		} // updateProjection


		private String getProjection()
		{
			// String item = (String) projectionCombo.getSelectedItem();
			if (projection.equalsIgnoreCase("Mercator"))
				return FlatGlobe.PROJECTION_MERCATOR;
			else if (projection.equalsIgnoreCase("Sinusoidal"))
				return FlatGlobe.PROJECTION_SINUSOIDAL;
			else if (projection.equalsIgnoreCase("ModSinusoidal"))
				return FlatGlobe.PROJECTION_MODIFIED_SINUSOIDAL;
			// Default to lat-lon
			return FlatGlobe.PROJECTION_LAT_LON;
		}


		private boolean setFlatEarth(String val)
		{

			boolean flat = false;
			if (val.equalsIgnoreCase("on"))
				flat = true;
			if (val.equalsIgnoreCase("Mercator") || val.equalsIgnoreCase("Sinusoidal")
				|| val.equalsIgnoreCase("modSinusoidal") || val.equalsIgnoreCase("latLon"))
			{
				flat = true;
				projection = val;
			}

			if (isFlatGlobe() == flat)
				return true;

			if (!flat)
			{
				// Switch to round globe
				getWwd().getModel().setGlobe(roundGlobe);
				// Switch to orbit view and update with current position
				FlatOrbitView flatOrbitView = (FlatOrbitView) getWwd().getView();
				BasicOrbitView orbitView = new BasicOrbitView();
				orbitView.setCenterPosition(flatOrbitView.getCenterPosition());
				orbitView.setZoom(flatOrbitView.getZoom());
				orbitView.setHeading(flatOrbitView.getHeading());
				orbitView.setPitch(flatOrbitView.getPitch());
				getWwd().setView(orbitView);
				setElevationData("on");
				// Change sky layer
				LayerList layers = getWwd().getModel().getLayers();
				for (int i = 0; i < layers.size(); i++)
				{
					if (layers.get(i) instanceof SkyColorLayer)
						layers.set(i, new SkyGradientLayer());
				}
			}
			else
			{
				// Switch to flat globe
				getWwd().getModel().setGlobe(flatGlobe);
				// let's assume we don't want elevation data for flat earth
				getWwd().getModel().getGlobe().setElevationModel(new ZeroElevationModel());
				flatGlobe.setProjection(this.getProjection());
				// Switch to flat view and update with current position
				BasicOrbitView orbitView = (BasicOrbitView) getWwd().getView();
				FlatOrbitView flatOrbitView = new FlatOrbitView();
				flatOrbitView.setCenterPosition(orbitView.getCenterPosition());
				flatOrbitView.setZoom(orbitView.getZoom());
				flatOrbitView.setHeading(orbitView.getHeading());
				flatOrbitView.setPitch(orbitView.getPitch());
				getWwd().setView(flatOrbitView);
				// Change sky layer
				LayerList layers = getWwd().getModel().getLayers();
				for (int i = 0; i < layers.size(); i++)
				{
					if (layers.get(i) instanceof SkyGradientLayer)
						layers.set(i, new SkyColorLayer());

				}
			}

			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

			return true;
		} // setFlatEarth


		private boolean setElevationData(String val)
		{
			if (val.equalsIgnoreCase("off"))
			{
				getWwd().getModel().getGlobe().setElevationModel(new ZeroElevationModel());
				elevationItem.setSelected(false);
			}
			if (val.equalsIgnoreCase("on"))
			{
				this.getWwd().getModel().getGlobe().setElevationModel(noDepthModel);
				elevationItem.setSelected(true);
			}
			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

			return true;
		} // setElevationData


		private boolean setStereo(String val)
		{

			if (val.equalsIgnoreCase("on"))
			{
				System.setProperty("gov.nasa.worldwind.stereo.mode", "redblue");

				((SdtBasicSceneController) this.getWwd().getSceneController()).setStereo(true);
				stereoItem.setSelected(true);
			}
			else
			{
				((SdtBasicSceneController) this.getWwd().getSceneController()).setStereo(false);
				stereoItem.setSelected(false);
				;
			}
			getWwd().redraw();
			if (sharedFrame != null)
				sharedFrame.wwjPanel.wwd.redraw();

			return true;
		} // setStereo


		private boolean setCollapseLinks(String val)
		{

			if (val.equalsIgnoreCase("on"))
			{
				collapseLinksItem.setSelected(true);
				collapseLinks = true;
				refreshLinks();
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			else
			{
				collapseLinksItem.setSelected(false);
				collapseLinks = false;
				refreshLinks();
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

			}
			return true;
		} // setCollapseLinks


		private boolean setOfflineMode(String val)
		{

			if (val.equalsIgnoreCase("on"))
			{
				WorldWind.setOfflineMode(true);
				offlineModeItem.setSelected(true);
			}
			else
			{
				WorldWind.setOfflineMode(false);
				offlineModeItem.setSelected(false);
			}
			return true;
		} // setOfflineMode


		private boolean setOrigin(String val)
		{
			if (0 == val.length())
				return false;

			String[] coord = val.split(new String(","));
			if (3 != coord.length)
			{
				// TODO display bad origin error
				return false;
			}
			Float f = new Float(coord[1]);
			double lat = f.doubleValue();
			f = new Float(coord[0]);
			double lon = f.doubleValue();
			f = new Float(coord[2]);
			double alt = f.doubleValue();

			originLocation = Position.fromDegrees(lat, lon, alt);

			return true;
		}


		private boolean setLayer(String val)
		{
			if (0 == val.length())
				return false;

			// TODO: parsing needs error handling!
			String[] attrs = val.split(",");

			String layerName = attrs[0];
			boolean selected = true;

			if (attrs.length > 1 && attrs[1].equalsIgnoreCase("on"))
				selected = true;
			if (attrs.length > 1 && attrs[1].equalsIgnoreCase("off"))
				selected = false;

			if (!sdtLayerPanel.contains(layerName))
				sdtLayerPanel.addLayer(layerName, selected);
			else
			{
				sdtLayerPanel.toggleLayer(val, selected); // rewrite toggleLayer to pass selected
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();
			}
			return true;

		}


		private boolean setNodeUDLayer(String val)
		{
			if (0 == val.length() || currentNode == null)
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				currentNode.removeNodeFromLayer();
				currentNode.removeRenderables(this);
				currentNode.drawRenderables(this);
				return true;
			}

			if (!sdtLayerPanel.contains(val))
				sdtLayerPanel.addLayer(val, false);

			if (!assignLayerToNode(val))
				return false;

			return true;
		}


		private boolean setLinkUDLayer(String val)
		{
			if (0 == val.length() || currentLinkSet == null)
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				Iterator<SdtLink> itr = currentLinkSet.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink theLink = itr.next();
					theLink.removeRenderables();
					theLink.removeLinkFromLayers();
					theLink.drawRenderables(false);
				}
				return true;
			}

			if (!sdtLayerPanel.contains(val))
				sdtLayerPanel.addLayer(val, false);

			if (!assignLayerToLinkSet(val))
				return false;

			return true;
		}


		private boolean setSymbolUDLayer(String val)
		{
			if (currentSymbol == null && currentNode != null)
				currentSymbol = currentNode.getSymbol();

			if (0 == val.length() || currentSymbol == null)
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				// Remove the airspace if it existed we don't have
				// an easy way to figure this out as yet.
				getSymbolLayer().removeSymbol(currentSymbol);
				currentSymbol.removeSymbolFromLayer();
				currentSymbol.setInitialized(false);
				getSymbolLayer().addSymbol(currentSymbol);
				return true;
			}

			if (!sdtLayerPanel.contains(val))
				sdtLayerPanel.addLayer(val, false);

			if (!assignLayerToSymbol(val))
				return false;

			return true;
		}


		private boolean setLabelUDLayer(String val)
		{
			if (0 == val.length() || currentNode == null)
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				currentNode.removeLabelFromLayer();
				// in case its already drawn - we don't have an easy way to
				// check this
				if (currentNode.getLabel() != null)
				{
					getNodeLabelLayer().removeAnnotation(currentNode.getLabel());
					getNodeLabelLayer().addAnnotation(currentNode.getLabel());

				}
				return true;
			}

			if (!sdtLayerPanel.contains(val))
				sdtLayerPanel.addLayer(val, false);

			if (!assignLayerToLabel(val))
				return false;
			return true;
		}


		private boolean setRegionUDLayer(String val)
		{
			if (0 == val.length() || null == currentRegion)
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				getRegionLayer().removeRenderables(currentRegion);

				currentRegion.removeSymbolFromLayer();
				// currentRegion.setInitialized(false);
				// ljt old getRegionLayer().addRegion(currentRegion);
				return true;
			}

			if (!sdtLayerPanel.contains(val))
				sdtLayerPanel.addLayer(val, false);

			if (!assignLayerToRegion(val))
				return false;

			return true;
		}


		private boolean setTileUDLayer(String val)
		{
			if (0 == val.length() || null == currentTile)
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				currentTile.removeLayer();
				return true;

			}
			if (!sdtLayerPanel.contains(val))
				sdtLayerPanel.addLayer(val, false);

			if (!assignLayerToTile(val))
				return false;

			return true;
		}


		private boolean assignLayerToNode(String val)
		{
			if ((0 == val.length()) || null == currentNode)
				return false; // wait for symbol type

			// TODO ljt centralize appending user defined...
			if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
				val = "User Defined::" + val;

			// Are any of the node's attributes assigned to a layer?
			if (currentNode.attributeAlreadyAssigned())
			{
				System.out.println("sdt3d::assignLayerToNode() A node attribute is already assigned to a layer");
				return false;
			}

			// Node already in layer
			if (currentNode.nodeAlreadyAssigned() && currentNode.getLayerList().containsKey(val))
				return false;

			// We must be moving it to a new layer, remove it from the old..
			if (currentNode.nodeAlreadyAssigned() && !currentNode.getLayerList().containsKey(val))
			{
				currentNode.removeNodeFromLayer();
				currentNode.removeRenderables(this);
			}

			SdtCheckboxNode theCheckbox = (SdtCheckboxNode) sdtLayerPanel.findLeaf(val);
			if (theCheckbox == null || !theCheckbox.isLeaf())
			{
				System.out.println("sdt3d.assignLayerToObject() Objects may only be assigned to leafs.");
				return false;
			}

			currentNode.addLayer(val, theCheckbox);
			theCheckbox.addNode(currentNode, this);

			return true;
		}


		private boolean assignLayerToLinkSet(String val)
		{
			if ((0 == val.length()) || null == currentLinkSet || currentLinkSet.isEmpty())
				return false;

			// TODO ljt centralize appending user defined...
			if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
				val = "User Defined::" + val;

			SdtCheckboxNode theCheckbox = (SdtCheckboxNode) sdtLayerPanel.findLeaf(val);
			if (theCheckbox == null || !theCheckbox.isLeaf())
			{
				System.out.println("sdt3d.assignLayerToObject() Objects may only be assigned to leafs.");
				return false;
			}

			Iterator<SdtLink> itr = currentLinkSet.iterator();
			while (itr != null && itr.hasNext())
			{
				SdtLink theLink = itr.next();

				// Node already in layer?
				if (theLink.linkInUserDefinedLayer() && theLink.getLayerList().containsKey(val))
					return true;
				// We must be moving it to a new layer, remove it from old...
				// or if not in a user defined layer from the network links
				// layer
				if ((!theLink.linkInUserDefinedLayer() ||
					(theLink.linkInUserDefinedLayer()
						&& !theLink.getLayerList().containsKey(val))))
				{
					// Important to remove renderables before clearing link layer list or
					// link will not be removed from user defined layers (it uses
					// link layer assignment to figure out what layer to remove from)!
					theLink.removeRenderables();

					theLink.removeLinkFromLayers();
				}
				theLink.addLayer(val, theCheckbox);
				theCheckbox.addLink(theLink, this);
			}
			return true;
		}


		private boolean assignLayerToSymbol(String val)
		{
			if ((0 == val.length()) || null == currentSymbol)
				return false; // wait for symbol type

			// TODO ljt centralize appending user defined...
			if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
				val = "User Defined::" + val;

			SdtCheckboxNode theCheckbox = (SdtCheckboxNode) sdtLayerPanel.findLeaf(val);
			if (theCheckbox == null || !theCheckbox.isLeaf())
			{
				System.out.println("sdt3d.assignLayerToObject() Objects may only be assigned to leafs.");
				return false;
			}

			if (currentSymbol.getSdtNode().nodeAlreadyAssigned() || currentSymbol.getSdtNode().labelAlreadyAssigned())
			{
				System.out.println("sdt3d::assignLayerToSymbol() node already assigned to layer");
				return false;
			}

			// Node already in layer?
			if (currentSymbol.getSdtNode().symbolAlreadyAssigned() && currentSymbol.getLayerList().containsKey(val))
				return false;

			// We must be moving it to a new layer, remove from old
			if (currentSymbol.getSdtNode().symbolAlreadyAssigned() && !currentSymbol.getLayerList().containsKey(val))
			{
				currentSymbol.removeSymbolFromLayer();
				currentSymbol.setInitialized(false);
				getSymbolLayer().removeSymbol(currentSymbol);
			}
			// ljt testing - did the symbol exist before we added it to a layer?
			getSymbolLayer().removeSymbol(currentSymbol);
			currentSymbol.addLayer(val, theCheckbox);
			theCheckbox.addSymbol(currentSymbol, this);
			return true;
		}


		private boolean assignLayerToRegion(String val)
		{
			if ((0 == val.length()) || null == currentRegion)
				return false; // wait for symbol type

			// TODO ljt centralize appending user defined...
			if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
				val = "User Defined::" + val;

			SdtCheckboxNode theCheckbox = (SdtCheckboxNode) sdtLayerPanel.findLeaf(val);
			if (theCheckbox == null || !theCheckbox.isLeaf())
			{
				System.out.println("sdt3d.assignLayerToObject() Objects may only be assigned to leafs.");
				return false;
			}
			// Region already in layer?
			if (currentRegion.alreadyAssigned() && currentRegion.getLayerList().containsKey(val))
				return false;

			// We must be moving it to a new layer, remove from the old
			if (currentRegion.alreadyAssigned() && !currentRegion.getLayerList().containsKey(val))
			{
				currentRegion.removeSymbolFromLayer();
				currentRegion.setInitialized(false);
				getRegionLayer().removeRenderables(currentRegion);
			}

			currentRegion.addLayer(val, theCheckbox);
			theCheckbox.addRegion(currentRegion, this);
			return true;
		}


		private boolean assignLayerToTile(String val)
		{
			if ((0 == val.length()) || null == currentTile)
				return false; // wait for symbol type

			// TODO ljt centralize appending user defined...
			if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
				val = "User Defined::" + val;

			// Tile already in layer?
			if (currentTile.alreadyAssigned() && currentTile.getLayerList().containsKey(val))
				return false;

			// We must be moving to a new layer, remove from old...
			if (currentTile.alreadyAssigned() && !currentTile.getLayerList().containsKey(val))
			{
				currentTile.removeLayer();
				getTileLayer().removeRenderable(currentTile.getSurfaceImage());
			}

			SdtCheckboxNode theCheckbox = (SdtCheckboxNode) sdtLayerPanel.findLeaf(val);
			if (theCheckbox == null || !theCheckbox.isLeaf())
			{
				System.out.println("sdt3d.assignLayerToObject() Objects may only be assigned to leafs.");
				return false;

			}
			currentTile.addLayer(val, theCheckbox);
			theCheckbox.addTile(currentTile, this);
			return true;
		}


		private boolean assignLayerToLabel(String val)
		{
			if ((0 == val.length()) || null == currentNode)
				return false; // wait for symbol type

			// TODO ljt centralize appending user defined...
			if (!val.startsWith("User Defined") && !val.startsWith("All Layers"))
				val = "User Defined::" + val;

			SdtCheckboxNode theCheckbox = (SdtCheckboxNode) sdtLayerPanel.findLeaf(val);
			if (theCheckbox == null || !theCheckbox.isLeaf())
			{
				System.out.println("sdt3d.assignLayerToObject() Objects may only be assigned to leafs.");
				return false;
			}

			// Node assigned?
			if (!currentNode.getLayerList().isEmpty())
			{
				System.out.println("sdt3d.assignLayerToObject() Node already assigned to a layer.");
				return false;
			}

			// Label already in layer?
			if (currentNode.getLayerList() != null
				&& (currentNode.getLabelLayerList().containsKey(val)))
				return false;

			// We must be moving it to a new layer, remove from old.. LJT why are we checking node?
			if (currentNode.getLayerList() != null
				&& (!currentNode.getLabelLayerList().containsKey(val)))
			{
				getNodeLabelLayer().removeAnnotation(currentNode.getLabel());
				currentNode.removeLabelFromLayer();
			}

			currentNode.addLabelLayer(val, theCheckbox);
			theCheckbox.addLabel(currentNode, this);

			return true;
		}


		private boolean setListen(String val)
		{
			if (0 == val.length())
				return false;

			if (val.equalsIgnoreCase("off"))
			{
				// Turn off all sockets
				if (udpSocketThread != null)
				{
					udpSocketThread.stopThread();
					udpSocketThread = null;
					toggleUdpOn();
				}
				if (tcpSocketThread != null)
				{
					tcpSocketThread.stopThread();
					tcpSocketThread = null;
					toggleTcpOn();
				}
				return true;
			}
			String[] attrs = val.split(",");
			// Are we turning off a specific protocol?
			if (attrs.length > 1)
				if (attrs[0].equalsIgnoreCase("udp") && attrs[1].equalsIgnoreCase("off"))
				{
					if (udpSocketThread != null)
					{
						udpSocketThread.stopThread();
						udpSocketThread = null;
						toggleUdpOn();
						return true;
					}

				}
				else
				{
					if (attrs[0].equalsIgnoreCase("tcp") && attrs[1].equalsIgnoreCase("off"))
					{

						if (tcpSocketThread != null)
						{
							System.out.println("Calling shutdown");
							tcpSocketThread.stopThread();
							tcpSocketThread = null;
							toggleTcpOn();
							return true;
						}
					}

				}

			String protocol = "udp";
			// do we just have a [addr/]port assignment?
			if (attrs.length == 1)
			{ // ljt add these checks elsewhere
				String[] addr = attrs[0].split("/");
				if (addr.length > 1)
				{
					socketMulticastAddr = addr[0];
					try
					{
						socketPort = new Integer(addr[1]);
					}
					catch (NumberFormatException e)
					{
						System.out.println("Invalid port assignment");
						return false;
					}
				}
				else
				{ // just the port
					try
					{
						socketPort = new Integer(attrs[0]);
					}
					catch (NumberFormatException e)
					{
						System.out.println("Invalid port assignment");
						return false;
					}
				}
			}
			else
			{

				if (attrs[0].equalsIgnoreCase("udp") || attrs[0].equalsIgnoreCase("tcp"))
				{
					// Were we given an address?
					String[] addr = attrs[1].split("/");
					if (addr.length > 1)
					{

						socketMulticastAddr = addr[0];
						try
						{
							socketPort = new Integer(addr[1]);
						}
						catch (NumberFormatException e)
						{
							System.out.println("Invalid port assignment");
							return false;

						}
					}
					else // or just a port
					{
						try
						{
							socketPort = new Integer(attrs[1]);
						}
						catch (NumberFormatException e)
						{
							System.out.println("Invalid port assignment");
							return false;

						}

					}
				}
				else
				{
					System.out.println("Invalid protocol");
					return false;
				}

			}

			// Stop any existing threads in the protocol family
			// (udp is the default)
			if (!attrs[0].equalsIgnoreCase("tcp"))
			{
				if (udpSocketThread != null)
				{
					udpSocketThread.stopThread();
					udpSocketThread = null;
					toggleUdpOn();
				}
			}
			else
			// else we're tcp
			{
				if (tcpSocketThread != null)
				{
					tcpSocketThread.stopThread();
					tcpSocketThread = null;
					toggleTcpOn();
				}

			}
			// We default to udp
			if (attrs[0].equalsIgnoreCase("tcp"))
				protocol = "tcp";
			// ljt modularize this at some point!!!
			if (protocol == "udp")
			{
				udpSocketThread = new UdpSocketThread(this, socketPort, socketMulticastAddr);
				udpSocketThread.start();
				if (udpSocketThread.stopped())
					toggleUdpOn();
				else
					toggleUdpOff();

			}
			else
			{
				tcpSocketThread = new TcpSocketThread(this, socketPort);
				tcpSocketThread.start();
				if (tcpSocketThread.stopped())
					toggleTcpOn();
				else
					toggleTcpOff();
			}
			return true;

		}


		public static boolean useAbsoluteElevation()
		{
			return useAbsoluteElevation;
		}


		private boolean loadInputFile(String val, boolean forceAppend)
		{
			// The pipeCmd flag tells us that we are processing an input file
			// received over the command pipe - in this case we don't want to
			// process any more pipe commands. Sort of messy..
			if (val == null || val.length() == 0)
				return false;

			String fileName = findFile(val);
			if (fileName == null)
				return false;

			File f1 = new File(fileName);
			if (!f1.exists())
				return false;

			fileName = f1.getAbsolutePath();
			// If we're not currently reading anything start the thread
			// immediately
			if (fileThread == null)
			{
				fileThread = new FileThread(this, fileName, pipeCmd);
				fileThread.start(); // calls the run method for us
			}
			else if (!fileThread.isRunning())
			{
				fileThread = new FileThread(this, fileName, pipeCmd);
				fileThread.start();
			}
			else
			{
				// Process input statements from scripts immediately,
				// but pipe input statements serially

				// We also append files that are added via the
				// "append file menu option"
				if (pipeCmd || forceAppend)
				{
					// System.out.println("LJT Appending input file");
					fileThread.addLast(fileName, true);
				}
				else
				{
					// start immediately
					fileThread.pushFile(fileName);
				}
			}
			return true;
		}


		private boolean setGeoTiff(final String val)
		{
			if (val == null || val.isEmpty())
			{
				return false;
			}
			else
			{
				this.currentGeoTiff = val;
				return true;
			}
		}


		private boolean setGeoTiffFile(final String val)
		{
			if (val == null || val.isEmpty())
			{
				return false;
			}
			else
			{
				String fileName = findFile(val);
				if (fileName == null)
				{
					return false;
				}
				else
				{
					return this.elevationBuilder.addModel(this.currentGeoTiff, fileName);
				}
			}
		}


		private boolean setKml(final String val)
		{
			if (val == null || val.isEmpty())
				return false;

			currentKml = kmlTable.get(val);

			if (currentKml == null)
			{
				currentKml = new SdtSpriteKml(val);
				// Strip off the directory name for display purposes...
				kmlTable.put(val, currentKml);
			}

			return true;

		}


		private boolean setKmlFile(final String val)
		{
			if (0 == val.length() || null == currentKml)
				return false; // no <imageFile>

			if (currentKml.hasController())
			{
				KMLController theKmlController = currentKml.getKmlController();

				// Remove the kml from the display
				getKmlLayer().removeRenderable(theKmlController);

				SdtSpriteKml theKml = null;
				theKml = kmlTable.get(currentKml.getName());

				// Remove from menu and panel
				this.kmlPanelLayerTree.getModel().getRoot().removeChild(theKml.getLayerNode());
				kmlMenu.remove(theKml.getKmlMenuItem());
				kmlLayer.removeRenderable(theKml.getKmlController());
				// currentKml.setKmlController(null);
			}

			if (val.equalsIgnoreCase("NONE"))
			{
				// We won't be readding it, remove from table
				kmlTable.remove(currentKml.getName());
				return true;
			}

			String fileName = findFile(val);
			if (fileName == null)
				return false;

			File f1 = new File(fileName);
			if (!f1.exists())
				return false;

			if (currentKml.setKmlFile(fileName) != true)
				return false;

			kmlLayer.addRenderable(currentKml.getKmlController());

			// Create menu items for ease of viewing
			kmlMenu.add(currentKml.getKmlMenuItem());
			currentKml.getKmlMenuItem().addActionListener(this);

			// Adds a new layer tree node for the KMLRoot to the on-screen layer tree panel, and makes the new node
			// visible
			// in the tree. This also expands any tree paths that represent open KML containers or open KML network
			// links.
			KMLLayerTreeNode layerNode = new KMLLayerTreeNode(kmlLayer, currentKml.getKmlRoot());
			currentKml.setLayerNode(layerNode);
			this.kmlPanelLayerTree.getModel().addLayer(layerNode);
			this.kmlPanelLayerTree.makeVisible(layerNode.getPath());
			layerNode.expandOpenContainers(this.kmlPanelLayerTree);

			// Listens to refresh property change events from KML network link nodes. Upon receiving such an event this
			// expands any tree paths that represent open KML containers. When a KML network link refreshes, its tree
			// node replaces its children with new nodes created form the refreshed content, then sends a refresh
			// property change event through the layer tree. By expanding open containers after a network link refresh,
			// we ensure that the network link tree view appearance is consistent with the KML specification.
			layerNode.addPropertyChangeListener(AVKey.RETRIEVAL_STATE_SUCCESSFUL, new PropertyChangeListener()
				{
					@Override
					public void propertyChange(PropertyChangeEvent event)
					{
						if (event.getSource() instanceof KMLNetworkLinkTreeNode)
							((KMLNetworkLinkTreeNode) event.getSource()).expandOpenContainers(kmlPanelLayerTree);
					}
				});

			return true;
		}

		public enum Time {
			;
			private static long lastTime;


			public synchronized static long increasingTimeMillis()
			{
				long now = System.currentTimeMillis();
				if (now > lastTime)
					return lastTime = now;
				return ++lastTime;
			}
		}

		private void logDebugOutput(String pendingCmd, String val)
		{
			// Don't want to log playback commands but shouldn't get here
			// in any event
			if (logDebugOutput && !playbackScenario)
			{
				if (logDebugFile != null)
				{
					logDebugFile.print(pendingCmd + " \"" + val + "\" \n");

					long wait = currentTime - lastTime;
					if (wait > 100)
					{
						logDebugFile.print("wait " + wait + " \n");
						// TODO: Do we want to flush each time?
						logDebugFile.flush();
						lastTime = currentTime;
					}
				}
				else
				{
					System.out.println(pendingCmd + " " + val + " \n");
				}
			}
		}
		
		
		
		private void createPropertyChangeListener()
		{
		    propChangeSupport.addPropertyChangeListener(new PropertyChangeListener()
		        {
		            public void propertyChange(PropertyChangeEvent event)
		            {
		            		stopScenarioThread();
		            		if (event.getPropertyName().equals(ScenarioController.RECORDING_STARTED))
		            		{
		            			System.out.println("RECORDING_STARTED sdt3d\n");
		            			recordScenario = true;
		            			playbackOnly = false;
		            			
		            		}
		            		if (event.getPropertyName().equals(ScenarioController.RECORDING_STOPPED))
		            		{
		            			// The user stopped recording, keep the data model available
		            			// and don't reset controller
		            			
		            			System.out.println("RECORDING_STOPPED sdt3d\n");
		            			
		    					if (scenarioThread != null) 
		    					{ 
		    						scenarioThread.stopRecording();
		    						stopScenarioThread();
		    					}
		    					scenarioController.stopRecording();
		    					scenarioThread = null;
		    					recordScenario = false;
		    					playbackOnly = true;
		            		}

		            		/*
		            		if (event.getPropertyName().equals(ScenarioController.SAVE_STATE))
		            		{
		            			scenarioController.saveState();
		            		}
		            		*/
		            		if (event.getPropertyName().equals(ScenarioController.LOAD_STATE))
		            		{
		            			System.out.println("LOAD_STATE sdt3d (resetting system state)\n");

		            			resetSystemState(false);
		            		}
		            		
		                if (event.getPropertyName().equals(ScenarioController.START_SCENARIO_PLAYBACK))
		                {
		                		System.out.println("START_SCENARIO_PLAYBACK sdt3d\n");
		                		scenarioController.appendBufferModel();
	                			
		                		// oldValue: sliderStartTime, newValue: scenarioStartTime
		                		startScenarioThread((Long) event.getNewValue());
	                			playbackScenario = true;	  
	                			playbackStopped = false;
						}
		                if (event.getPropertyName().equals(ScenarioController.SKIP_FORWARD))
		                {
	                		System.out.println("SKIP_FORWARD sdt3d\n");

		                	if (scenarioThread != null)
		                	{
		                		playbackScenario = true;
		                		playbackStopped = true;		                		

		                		scenarioThread.setScenarioStartTime((Long) event.getNewValue());
		                		
		                		//startScenarioThread((Long) event.getNewValue());
	                			playbackScenario = true;	  
	                			playbackStopped = false;

		                	}
		                }
		                if (event.getPropertyName().equals(ScenarioController.STOP_SCENARIO_PLAYBACK))
		                {	
		                		System.out.println("STOP_SCENARIO_PLAYBACK sdt3d\n");
		                		playbackScenario = true;
		                		playbackStopped = true;		                		
		                }
		                if (event.getPropertyName().equals(ScenarioController.RESUME_LIVE_PLAY))
		                	{
		                		System.out.println("RESUME_LIVE_PLAY sdt3d\n");
		                		// TODO: Need to playback what is in the buffer prior to resuming play
		                		scenarioController.appendBufferModel();
		                		playbackScenario = false;
		                		playbackStopped = false;
		                }
		            }
		        });
		}
		
		void processCmd(String pendingCmd, String val,boolean scenarioCmd)
		{									
			/*
			 * If we are recording the scenario either update our model
			 * if we are not playing it back, or the buffered command
			 * model if we are.
			 */
			if (recordScenario && !playbackOnly)
			{
				if (!playbackScenario && !scenarioCmd) 
				{
					//System.out.println("Updating model..");
					scenarioController.updateModel(cmd2Int.get(pendingCmd), val);
				}
				else
				{
					if (!scenarioCmd)
					{
						scenarioController.updateBufferModel(cmd2Int.get(pendingCmd),val);
					}
				}	
			}
			
			if ((playbackScenario && !scenarioCmd)
					||
					playbackStopped)
			{
				
				return;
			}
		
			/*
			 * If we are no longer scenario recording/playback, don't play back
			 * any scenario commands (fix this text)
			 * 
			 * or if we have stopped recording and are just playing back the scenario
			 * 
			 * clean all these booleans up
			 */
			if (!recordScenario && scenarioCmd && !playbackOnly)
			{
				return;
			}
			
			if (playbackOnly && !scenarioCmd)
			{
				return;
			}
			
			
			if (this.doCmd(pendingCmd, val))
			{
				logDebugOutput(pendingCmd, val);
			}
			else
			{
				System.out.println("sdt3d::doCmd() cmd> " + pendingCmd + " val>" + val + " failed ");
			}
		
		}


		boolean doCmd(String pendingCmd, String val)
		{			
			//System.out.println("sdt3d:doCmd() cmd>" + pendingCmd + " val>" + val);
			if (pendingCmd.equalsIgnoreCase("bgbounds"))
				return setBackgroundBounds(val);
			else if (pendingCmd.equalsIgnoreCase("flyto"))
				return setFlyTo(val);
			else if (pendingCmd.equalsIgnoreCase("zoom"))
				return setZoom(val);
			else if (pendingCmd.equalsIgnoreCase("heading"))
				return setHeading(val);
			else if (pendingCmd.equalsIgnoreCase("pitch"))
				return setPitch(val);
			else if (pendingCmd.equalsIgnoreCase("tileImage"))
				return setTileImage(val);
			else if (pendingCmd.equalsIgnoreCase("tile"))
				return setTile(val);
			else if (pendingCmd.equalsIgnoreCase("sector"))
				return loadTile(val);
			else if (pendingCmd.equalsIgnoreCase("instance"))
				return setPipeName(val);
			else if (pendingCmd.equalsIgnoreCase("bgimage"))
				return false;
			else if (pendingCmd.equalsIgnoreCase("sprite"))
				return setSprite(val);
			else if (pendingCmd.equalsIgnoreCase("scale"))
				return setScale(val);
			else if (pendingCmd.equalsIgnoreCase("image"))
			{
				// sprite file not found
				if (!setImage(val))
				{
					// Invalid image assigned, reset our state for the sprite
					// so we can reassign it to the same name if need be.
					spriteTable.remove(currentSprite.getName());
					currentSprite = null;
					return false;
				}
				return true;
			}
			else if (pendingCmd.equalsIgnoreCase("node"))
				return setNode(val);
			else if (pendingCmd.equalsIgnoreCase("type"))
				return setType(val);
			else if (pendingCmd.equalsIgnoreCase("position") || pendingCmd.equalsIgnoreCase("pos"))
				return setPosition(val);
			else if (pendingCmd.equalsIgnoreCase("focus"))
				return setFocus(val);
			else if (pendingCmd.equalsIgnoreCase("follow"))
				return setFollow(val);
			else if (pendingCmd.equalsIgnoreCase("center"))
				return setRegionPosition(val);
			else if (pendingCmd.equalsIgnoreCase("clear"))
				return clear(val);
			else if (pendingCmd.equalsIgnoreCase("delete"))
				return delete(val);
			else if (pendingCmd.equalsIgnoreCase("size"))
				return setSize(val);
			else if (pendingCmd.equalsIgnoreCase("length"))
				return setLength(val);
			else if (pendingCmd.equalsIgnoreCase("light"))
				return setLight(val);
			else if (pendingCmd.equalsIgnoreCase("label"))
				return setLabel(val);
			else if (pendingCmd.equalsIgnoreCase("trail"))
				return setTrail(val);
			else if (pendingCmd.equalsIgnoreCase("symbol"))
				return setSymbol(val);
			else if (pendingCmd.equalsIgnoreCase("shape"))
				return setShape(val);
			else if (pendingCmd.equalsIgnoreCase("link"))
			{
				// Spurious link commands should not be generated as
				// performance may be impacted due to refreshing links...

				return (setLink(val));
			}
			else if (pendingCmd.equalsIgnoreCase("linklabel"))
				return setLinkLabel(val);
			else if (pendingCmd.equalsIgnoreCase("unlink"))
				return setUnlink(val);
			else if (pendingCmd.equalsIgnoreCase("line"))
				return setLine(val);
			else if (pendingCmd.equalsIgnoreCase("wait"))
			{
				return true; // wait is implemented in FileThread only
			}
			else if (pendingCmd.equalsIgnoreCase("path"))
				return setPath(val);
			else if (pendingCmd.equalsIgnoreCase("status"))
				return setStatus(val);
			else if (pendingCmd.equalsIgnoreCase("region"))
				return setRegion(val);
			else if (pendingCmd.equalsIgnoreCase("input"))
				// Files loaded "in line" in scripts should be processed
				// immediately. Note that when an input command is recvd
				// via the input pipe, the file will be appended - pipeCmd
				// flag controls this.
				return loadInputFile(val, false);
			else if (pendingCmd.equalsIgnoreCase("title"))
				return setAppTitle(val);
			else if (pendingCmd.equalsIgnoreCase("defaultAltitudeType"))
				return setDefaultAltitudeType(val);
			else if (pendingCmd.equalsIgnoreCase("listen"))
				return setListen(val);
			else if (pendingCmd.equalsIgnoreCase("popup"))
				return setPopup(val);
			else if (pendingCmd.equalsIgnoreCase("popdown"))
				return setPopdown();
			else if (pendingCmd.equalsIgnoreCase("layer"))
				return setLayer(val);
			else if (pendingCmd.equalsIgnoreCase("nodeLayer"))
				return setNodeUDLayer(val);
			else if (pendingCmd.equalsIgnoreCase("linkLayer"))
				return setLinkUDLayer(val);
			else if (pendingCmd.equalsIgnoreCase("symbolLayer"))
				return setSymbolUDLayer(val);
			else if (pendingCmd.equalsIgnoreCase("labelLayer"))
				return setLabelUDLayer(val);
			else if (pendingCmd.equalsIgnoreCase("regionLayer"))
				return setRegionUDLayer(val);
			else if (pendingCmd.equalsIgnoreCase("tileLayer"))
				return setTileUDLayer(val);
			else if (pendingCmd.equalsIgnoreCase("view"))
				return setView(val);
			else if (pendingCmd.equalsIgnoreCase("viewXml"))
				return setViewXml(val, true);
			else if (pendingCmd.equalsIgnoreCase("backgroundColor"))
				return setBackgroundColor(val);
			else if (pendingCmd.equalsIgnoreCase("flatEarth"))
				return setFlatEarth(val);
			else if (pendingCmd.equalsIgnoreCase("elevationData"))
				return setElevationData(val);
			else if (pendingCmd.equalsIgnoreCase("stereo"))
				return setStereo(val);
			else if (pendingCmd.equalsIgnoreCase("offlineMode"))
				return setOfflineMode(val);
			else if (pendingCmd.equalsIgnoreCase("collapseLinks"))
				return setCollapseLinks(val);
			else if (pendingCmd.equalsIgnoreCase("elevationOverlay"))
				return setGeoTiff(val);
			else if (pendingCmd.equalsIgnoreCase("file"))
				return setGeoTiffFile(val);
			else if (pendingCmd.equalsIgnoreCase("geoTiff"))
				return setGeoTiff(val);
			else if (pendingCmd.equalsIgnoreCase("geoTiffFile"))
				return setGeoTiffFile(val);
			else if (pendingCmd.equalsIgnoreCase("kml"))
				return setKml(val);
			else if (pendingCmd.equalsIgnoreCase("kmlFile"))
				return setKmlFile(val);
			else if (pendingCmd.equalsIgnoreCase("origin"))
				return setOrigin(val);
			else if (pendingCmd.equalsIgnoreCase("reset"))
			{
				resetSystemState(false);
				return true;
			}
			else if (pendingCmd.equalsIgnoreCase("hardReset"))
			{
				resetSystemState(true);
				return true;
			}
			else if (pendingCmd.equalsIgnoreCase("resetPerspective"))
			{
				resetPerspective();
				return true;
			}
			else if (pendingCmd.equalsIgnoreCase("lookat"))
			{
				return setLookAt(val);

			}
			else if (pendingCmd.equalsIgnoreCase("userConfigFile"))
			{
				return loadUserConfigFile(val);
			}
			else if (pendingCmd.equalsIgnoreCase("symbolOffset"))
			{
				if (val.equalsIgnoreCase("on"))
					symbolOffset = true;
				else
					symbolOffset = false;

				symbolOffsetItem.setSelected(symbolOffset);
				getWwd().redraw();
				if (sharedFrame != null)
					sharedFrame.wwjPanel.wwd.redraw();

				return true;
			}
			else if (pendingCmd.equalsIgnoreCase("multiFrame"))
			{
				if (val.equalsIgnoreCase("on"))
					multiFrame = true;
				else
					multiFrame = false;
				multiFrameItem.setSelected(multiFrame);
				;
				if (sharedFrame == null)
				{
					// Create multiframe if it doesn't exist and toggle visibility
					if (this.multiFrame)
					{
						if (this.sharedFrame == null)
						{
							// We need to remove the view control layer and select listener
							// before sharing the globe - we readd it later.
							this.removeViewControlLayer(this);
							Model modelB = new BasicModel(new Earth(), new LayerList(this.wwjPanel.getWwd().getModel().getLayers()));
							sharedFrame = new SdtApplication.AppFrame((WorldWindowGLCanvas) this.getWwd(), modelB, true, false, false);
							WWUtil.alignComponent(null, sharedFrame, AVKey.LEFT_OF_CENTER);
							this.addViewControlLayer(sharedFrame);
							this.addViewControlLayer(this);

							// TODO: TBD frameB.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						}
						sharedFrame.setVisible(this.multiFrame);
					}

				}
				return true;
			}
			else if (pendingCmd.equalsIgnoreCase("orientation"))
			{
				return setOrientation(val);
			}
			else if (pendingCmd.equalsIgnoreCase("enableSdtViewControls"))
			{
				return setEnableSdtViewControls(val);
			}
			else if (pendingCmd.equalsIgnoreCase("logDebugOutput"))
			{
				return setLogDebugOutput(val);
			}
			else if (pendingCmd.equalsIgnoreCase("recordScenario"))
			{
				return true;
				//return setRecordScenario(val);
			}
			else if (pendingCmd.equalsIgnoreCase("loadCache"))
			{
				return setLoadCache(val);
			}
			else if (pendingCmd.equalsIgnoreCase("showLayerPanel"))
			{
				return setShowLayerPanel(val);
			}
			else if (pendingCmd.equalsIgnoreCase("showSdtPanel"))
			{
				return setShowSdtPanel(val);
			}
			else if (pendingCmd.equalsIgnoreCase("showStatusPanel"))
			{
				return setShowStatusPanel(val);
			}
			else if (pendingCmd.equalsIgnoreCase("showScenarioPlaybackPanel"))
			{
				return setShowScenarioPlaybackPanel(val);
			}
			else if (pendingCmd.equalsIgnoreCase("showWmsFrame"))
			{
				return setShowWmsFrame(val);
			}
			else if (pendingCmd.equalsIgnoreCase("showSdtControlPanel"))
			{
				return setShowSdtControlPanel(val);
			}
			else
				return false;
		} // end ProcessCmd


		/**
		 * The sdt3d::onInput() function is the main command processing "loop" called
		 * by the various input threads:
		 * 
		 * SocketThread::UdpSocketThread
		 * SocketThread::TcpSocketThread
		 * SocketThread::PipeThread
		 * SocketThread::FileThread
		 * 
		 * The run method of each input thread calls SocketThread::ParseString to
		 * extract commands and sleep for any wait commands. SocketThread::ParseString
		 * calls sdt3d::onInput asynchronously on the AWT event dispatching thread.
		 * 
		 * Each thread owns a CmdParser which maintains state for each sdt3d
		 * "object" such that object attributes interleaved among threads
		 * are applied to the last sdt3d object referenced in a particular
		 * thread. sdt3d::OnInput then calls the threads CmdParser::OnCommand
		 * method.
		 * 
		 * onInput is also responsible for restarting the pollTimer
		 * every POLL_INTERVAL. The timer handles redrawing the worldwind
		 * display.
		 * 
		 * The worldwind redraw function is therefore only called once
		 * for each poll interval regardless of how many sdt3d commands
		 * have been received by the various input threads.
		 */
		public synchronized boolean onInput(String str, CmdParser parser, boolean scenarioCmd)
		{
			currentNode = parser.currentNode;
			currentSprite = parser.currentSprite;
			currentRegion = parser.currentRegion;
			currentSymbol = parser.currentSymbol;
			currentLinkSet = parser.currentLinkSet;
			currentTile = parser.currentTile;
			currentView = parser.currentView;
			currentGeoTiff = parser.currentGeoTiff;
			currentKml = parser.currentKml;
			// We handle input file processing differently for piped input
			pipeCmd = parser.pipeCmd;

			str.trim();
			// Remove quoted string
			if (str.startsWith("\""))
			{
				str = str.substring(1, str.length());
				str = str.substring(0, str.lastIndexOf("\""));
			}
			String cmd = str.concat(" ");
			parser.OnCommand(cmd, scenarioCmd);

			// So we don't clobbering file/pipe state when interleaving
			// the two command sets
			parser.currentNode = currentNode;
			parser.currentSprite = currentSprite;
			parser.currentRegion = currentRegion;
			parser.currentSymbol = currentSymbol;
			parser.currentLinkSet = currentLinkSet;
			parser.currentTile = currentTile;
			parser.currentView = currentView;
			parser.currentGeoTiff = currentGeoTiff;
			parser.currentKml = currentKml;
			// Start the timer that will update the display
			if (pollTimer != null && !pollTimer.isRunning())
			{
				pollTimer.start();
			}
			return true;
		}


		public SdtLayerPanel getSdtLayerPanel()
		{
			// TODO Auto-generated method stub
			return sdtLayerPanel;
		}


		public void setMarkers(ArrayList<Marker> markers)
		{
			this.markers = markers;
		}


		public List<Marker> getMarkers()
		{
			return markers;
		}


		public List<Marker> getUserDefinedMarkers()
		{
			return userDefinedMarkers;
		}


		public void setUserDefinedMakers(ArrayList<Marker> markers)
		{
			this.userDefinedMarkers = markers;
		}


		public void setLinkLabelLayer(AnnotationLayer linkLabelLayer)
		{
			this.linkLabelLayer = linkLabelLayer;
		}


		public AnnotationLayer getLinkLabelLayer()
		{
			return linkLabelLayer;
		}


		public void setUserDefinedLinkLabelLayer(AnnotationLayer linkLabelLayer)
		{
			this.userDefinedLinkLabelLayer = linkLabelLayer;
		}


		public AnnotationLayer getUserDefinedLinkLabelLayer()
		{
			return userDefinedLinkLabelLayer;
		}


		public void setNodeLabelLayer(AnnotationLayer nodeLabelLayer)
		{
			this.nodeLabelLayer = nodeLabelLayer;
		}


		public AnnotationLayer getNodeLabelLayer()
		{
			return nodeLabelLayer;
		}


		public void setLinkLayer(RenderableLayer linkLayer)
		{
			this.linkLayer = linkLayer;
		}


		public RenderableLayer getLinkLayer()
		{
			return linkLayer;
		}


		public void setUserDefinedLinkLayer(RenderableLayer linkLayer)
		{
			this.userDefinedLinkLayer = linkLayer;
		}


		public RenderableLayer getUserDefinedLinkLayer()
		{
			return userDefinedLinkLayer;
		}


		public void setNodeModelLayer(Model3DLayer nodeModelLayer)
		{
			this.nodeModelLayer = nodeModelLayer;
		}


		public Model3DLayer getNodeModelLayer()
		{
			return this.nodeModelLayer;
		}


		public void setNodeKmlModelLayer(SdtKmlLayer nodeKmlModelLayer)
		{
			this.nodeKmlModelLayer = nodeKmlModelLayer;
		}


		public SdtKmlLayer getNodeKmlModelLayer()
		{
			return this.nodeKmlModelLayer;
		}


		public void setNodeIconLayer(IconLayer nodeIconLayer)
		{
			this.nodeIconLayer = nodeIconLayer;
		}


		public IconLayer getNodeIconLayer()
		{
			return nodeIconLayer;
		}


		public void setSymbolLayer(SdtSymbolLayer symbolLayer)
		{
			this.symbolLayer = symbolLayer;
		}


		public SdtSymbolLayer getSymbolLayer()
		{
			return this.symbolLayer;
		}


		public void setNodeLayer(RenderableLayer nodeLayer)
		{
			this.nodeLayer = nodeLayer;
		}


		public RenderableLayer getNodeLayer()
		{
			return nodeLayer;
		}


		public void setTrailLayer(RenderableLayer trailLayer)
		{
			this.trailLayer = trailLayer;
		}


		public RenderableLayer getTrailLayer()
		{
			return trailLayer;
		}


		public MarkerLayer getMarkerLayer()
		{
			// TODO Auto-generated method stub
			return markerLayer;
		}


		public MarkerLayer getUserDefinedMarkerLayer()
		{
			// TODO Auto-generated method stub
			return userDefinedMarkerLayer;
		}


		public SdtRegionLayer getRegionLayer()
		{
			// TODO Auto-generated method stub
			return regionLayer;
		}


		public void setTileLayer(RenderableLayer imageLayer)
		{
			this.tileLayer = imageLayer;
		}


		public RenderableLayer getTileLayer()
		{
			return tileLayer;
		}


		// This layer stores loaded kml files (not movable node kml models)
		public RenderableLayer getKmlLayer()
		{
			return kmlLayer;
		}


		public void setPlaybackScenario(boolean b) 
		{
			playbackScenario = false;
		}

	} // end class AppFrame

	// **************************************************************//
	// ******************** View Controller ***********************//
	// **************************************************************//

	public static class ViewController
	{
		protected static final double SMOOTHING_FACTOR = 0.98;

		protected boolean enabled = true;

		protected WorldWindow wwd;

		protected ViewAnimator animator;

		protected Iterable<? extends WWIcon> iconIterable;

		protected Iterable<SdtSpriteModel> modelIterable;

		protected Iterable<? extends Renderable> kmlModelIterable;

		protected Iterable<? extends ExtentHolder> extentHolderIterable;


		public ViewController(WorldWindow wwd)
		{
			this.wwd = wwd;
		}


		public boolean isEnabled()
		{
			return this.enabled;
		}


		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;

			if (this.animator != null)
			{
				this.animator.stop();
				this.animator = null;
			}
		}


		private Iterable<? extends WWIcon> getIcons()
		{
			return this.iconIterable;
		}


		private void setIcons(Iterable<? extends WWIcon> icons)
		{
			this.iconIterable = icons;
		}


		private Iterable<? extends SdtSpriteModel> getModels()
		{
			return this.modelIterable;
		}


		private void setModels(Iterable<SdtSpriteModel> iterable)
		{
			this.modelIterable = iterable;
		}


		private Iterable<? extends Renderable> getKmlModels()
		{
			return this.kmlModelIterable;
		}


		private void setKmlModels(Iterable<Renderable> iterable)
		{
			this.kmlModelIterable = iterable;
		}


		public Iterable<? extends ExtentHolder> getExtentHolders()
		{
			return this.extentHolderIterable;
		}


		public void setExtentHolders(Iterable<? extends ExtentHolder> extentHolders)
		{
			this.extentHolderIterable = extentHolders;
		}


		public boolean isSceneContained(View view)
		{
			ExtentVisibilitySupport vs = new ExtentVisibilitySupport();
			this.addExtents(vs);

			return vs.areExtentsContained(view);
		}


		public Vec4[] computeViewLookAtForScene(View view)
		{
			Globe globe = wwd.getModel().getGlobe();
			double ve = wwd.getSceneController().getVerticalExaggeration();

			gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport vs = new gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport();
			this.addExtents(vs);

			return vs.computeViewLookAtContainingExtents(globe, ve, view);
		}


		public Position computePositionFromPoint(Vec4 point)
		{
			return wwd.getModel().getGlobe().computePositionFromPoint(point);
		}


		public void gotoScene()
		{
			Vec4[] lookAtPoints = this.computeViewLookAtForScene(wwd.getView());
			if (lookAtPoints == null || lookAtPoints.length != 3)
				return;

			Position centerPos = wwd.getModel().getGlobe().computePositionFromPoint(lookAtPoints[1]);
			double zoom = lookAtPoints[0].distanceTo3(lookAtPoints[1]);

			wwd.getView().stopAnimations();
			wwd.getView().goTo(centerPos, zoom);

			// long timeInMilliseconds = 3000L; // Time in milliseconds you want the animation to take.
			// View view = ...; // How you get the view depends on the context.
			// OrbitViewInputHandler ovih = (OrbitViewInputHandler) this.wwd.getView().getViewInputHandler();
			// ovih.addPanToAnimator(centerPos, this.wwd.getView().getHeading(), this.wwd.getView().getPitch(), 0.0,
			// timeInMilliseconds, true);

		}


		public void sceneChanged()
		{
			OrbitView view = (OrbitView) wwd.getView();

			if (!this.isEnabled())
				return;

			// LJT This is a hack - did something change in latest wwj that broke
			// our view controller? isSceneContained is now throwing an exception
			// debug this.

			if (view != null)
				if (view.getViewport() != null)
					if (view.getViewport().getWidth() <= 0d)
						return;

			if (this.isSceneContained(view))
				return;

			if (this.animator == null || !this.animator.hasNext())
			{
				this.animator = new ViewAnimator(SMOOTHING_FACTOR, view, this);
				this.animator.start();
				view.stopAnimations();
				view.addAnimator(this.animator);
				view.firePropertyChange(AVKey.VIEW, null, view);
			}
		}


		protected void addExtents(ExtentVisibilitySupport vs)
		{
			// Compute screen extents for objects to track which
			// have feedback information from their Renderer.
			ArrayList<ExtentHolder> extentHolders = new ArrayList<ExtentHolder>();
			ArrayList<ExtentVisibilitySupport.ScreenExtent> screenExtents = new ArrayList<ExtentVisibilitySupport.ScreenExtent>();

			// TODO: create common handler for all objects to track

			Iterable<? extends WWIcon> icons = this.getIcons();
			if (icons != null)
			{

				for (WWIcon o : icons)
				{
					if (o instanceof ExtentHolder)
					{
						extentHolders.add((ExtentHolder) o);

					}
					else if (o instanceof AVList)
					{
						AVList avl = o;
						Object b = avl.getValue(AVKey.FEEDBACK_ENABLED);
						if (b == null || !Boolean.TRUE.equals(b))
							continue;

						if (avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT) != null)
						{
							screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
								(Vec4) avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
								(Rectangle) avl.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
						}
					}
				}
			}
			// Compute screen extents for WWModels which have feedback information from their IconRenderer.
			Iterable<? extends SdtSpriteModel> models = this.getModels();
			if (models != null)
			{
				for (SdtSpriteModel o : models)
				{
					// We handle models differently as they are not AVList instances
					// they should be...
					if (o == null || o.getValue(AVKey.FEEDBACK_ENABLED) == null ||
						!o.getValue(AVKey.FEEDBACK_ENABLED).equals(Boolean.TRUE))
						continue;

					if (o instanceof ExtentHolder)
					{
						extentHolders.add((ExtentHolder) o);

					}
					else
					{
						if (o.getValue(AVKey.FEEDBACK_REFERENCE_POINT) != null)
						{
							screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
								(Vec4) o.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
								(Rectangle) o.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
						}
					}

				}
			}
			Iterable<? extends Renderable> kmlModels = this.getKmlModels();
			if (kmlModels != null)
			{
				for (Renderable o : kmlModels)
				{
					if (o instanceof ExtentHolder)
					{
						extentHolders.add((ExtentHolder) o);

					}
					else if (o instanceof AVList)
					{
						AVList avl = (AVList) o;
						Object b = avl.getValue(AVKey.FEEDBACK_ENABLED);
						if (b == null || !Boolean.TRUE.equals(b))
							continue;

						if (avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT) != null)
						{
							screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
								(Vec4) avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
								(Rectangle) avl.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
						}
					}

				}

			}
			if (!extentHolders.isEmpty())
			{
				Globe globe = wwd.getModel().getGlobe();
				double ve = wwd.getSceneController().getVerticalExaggeration();
				vs.setExtents(ExtentVisibilitySupport.extentsFromExtentHolders(extentHolders, globe, ve));
			}
			if (!screenExtents.isEmpty())
			{
				vs.setScreenExtents(screenExtents);
			}

		}
	} // end ViewController

	// **************************************************************//
	// ******************** View Animator *************************//
	// **************************************************************//

	public static class ViewAnimator extends BasicAnimator
	{
		protected static final double LOCATION_EPSILON = 1.0e-9;

		protected static final double ALTITUDE_EPSILON = 0.1;

		protected OrbitView view;

		protected ViewController viewController;

		protected boolean haveTargets;

		protected Position centerPosition;

		protected double zoom;


		public ViewAnimator(final double smoothing, OrbitView view, ViewController viewController)
		{
			super(new Interpolator()
				{
					@Override
					public double nextInterpolant()
					{
						return 1d - smoothing;
					}
				});

			this.view = view;
			this.viewController = viewController;
		}


		@Override
		public void stop()
		{
			super.stop();
			this.haveTargets = false;
		}


		@Override
		protected void setImpl(double interpolant)
		{
			this.updateTargetValues();

			if (!this.haveTargets)
			{
				this.stop();
				return;
			}

			if (this.valuesMeetCriteria(this.centerPosition, this.zoom))
			{
				this.view.setCenterPosition(this.centerPosition);
				this.view.setZoom(this.zoom);
				this.stop();
			}
			else
			{
				Position newCenterPos = Position.interpolateGreatCircle(interpolant, this.view.getCenterPosition(),
					this.centerPosition);
				double newZoom = WWMath.mix(interpolant, this.view.getZoom(), this.zoom);
				this.view.setCenterPosition(newCenterPos);
				this.view.setZoom(newZoom);
			}

			this.view.firePropertyChange(AVKey.VIEW, null, this);
		}


		protected void updateTargetValues()
		{
			if (this.viewController.isSceneContained(this.view))
				return;

			Vec4[] lookAtPoints = this.viewController.computeViewLookAtForScene(this.view);
			if (lookAtPoints == null || lookAtPoints.length != 3)
				return;

			this.centerPosition = this.viewController.computePositionFromPoint(lookAtPoints[1]);
			this.zoom = lookAtPoints[0].distanceTo3(lookAtPoints[1]);
			if (this.zoom < view.getZoom())
				this.zoom = view.getZoom();
			this.haveTargets = true;
		}


		protected boolean valuesMeetCriteria(Position centerPos, double zoom)
		{
			Angle cd = LatLon.greatCircleDistance(this.view.getCenterPosition(), centerPos);
			double ed = Math.abs(this.view.getCenterPosition().getElevation() - centerPos.getElevation());
			double zd = Math.abs(this.view.getZoom() - zoom);

			return cd.degrees < LOCATION_EPSILON
				&& ed < ALTITUDE_EPSILON
				&& zd < ALTITUDE_EPSILON;
		}
	} // end ViewAnimator


	public static void main(String[] args)
	{
		// Override the scene controller so that we can control the
		// rendering process.
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, SdtStereoOpticsScreenController.class.getName());
		// <Property name="gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize" value="100000000"/>
		if (Configuration.isMacOS())
		{
			System.setProperty("com.apple.mrj.application.apple.menu.about.version", "2.0");
		}
		sdt3d.start("sdt-3D", AppFrame.class, args);
	}

}
