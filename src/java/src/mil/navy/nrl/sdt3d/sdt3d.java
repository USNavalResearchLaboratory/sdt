/*
 /*
 * sdt3d.java
 *
 * Created on February 12, 2008, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * WWJ code:
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
*/
package mil.navy.nrl.sdt3d;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.BasicAnimator;
import gov.nasa.worldwind.animation.Interpolator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.ExtentHolder;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.util.ExtentVisibilitySupport;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.terrain.BathymetryFilterElevationModel;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.render.markers.Marker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.ArrayList;


import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

import com.sun.opengl.util.Screenshot;

/**
 * 
 * @author Brian Adamson
 */
public class sdt3d extends ApplicationTemplate {
	// Our AppFrame will maintain most of the state and handle commands, etc
	protected static class AppFrame extends ApplicationTemplate.AppFrame implements
			ActionListener {
		/**
		 * 
		 */
		private SdtLayerPanel sdtLayerPanel;
		private StatusPanel statusPanel;
		private JPanel westPanel, logoPanel;
		private static final int logoPixelSize = 120;
		enum ObjectList {ALL, NODES, LABELS, SPRITES, SYMBOLS, LINKS, LINKLABELS, REGIONS, TILES, INVALID};  

		enum CmdType {CMD_INVALID, CMD_ARG, CMD_NOARG};
		String CMD_LIST[] =
		{
		    "+bgbounds",
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
		    "+follow",
		    "+center",
		    "+path",
		    "+label",
		    "+symbol",
		    "+shape",
		    "+region",
		    "+delete",  // remove node and any links to it
		    "+clear",   // remove all nodes and links
		    "+link",
		    "+light",
		    "+linklabel",
		    "+unlink",
		    "+line",
		    "+wait",
		    "+input",
		    "+tile",
		    "+tileimage",
		    "+sector",
		    "+status",
		    "+defaultAltitudeType",
		    "+listen",
		    "-off",
		    "+popup",
		    "+view",
		    "+viewSource",
		    "+viewXml",
		 //   "+popup",
		  //  "+popdown",
		  //  "+content",
		 //   "-resize",  // resize popup window
		    "+title",
		    null
		};	

		private String pipe_name = "sdt";			
		private boolean pipeCmd = false;

		private SdtSprite currentSprite = null;
		private SdtNode currentNode = null;
		private SdtRegion currentRegion = null;
		private SdtSymbol currentSymbol = null;
		private String currentTile = null;
		private String currentView = null;

		private List<SdtLink> currentLinkSet = new ArrayList<SdtLink>();
        private ArrayList<Marker> markers = new ArrayList<Marker>();
        
		// private SdtLink currentLink = null;
		
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);

		private static final long serialVersionUID = 1L;
		private JMenuBar menuBar;
		private JMenu fileMenu, viewMenu, layerMenu, bookmarkMenu;
		private JMenuItem openItem, screenItem, listenItem, exitItem, sdtLayersItem,
				allLayersItem, offLayersItem, bookmarkItem;
		private JCheckBoxMenuItem statusItem;
		private JCheckBoxMenuItem collapseLinksItem;
		public static boolean collapseLinks = false;
		final JFileChooser fc = new JFileChooser();
		
		private PipeThread pipeThread = null;
        private SocketThread socketThread = null;
        private int socketPort = 0;
        private String socketMulticastAddr = "";
		private FileThread fileThread = null;
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
		// agl elevation is the default.
		private static boolean useAbsoluteElevation = false;
		private static boolean followAll = false;
		protected ViewController viewController;	
		private RenderableLayer nodeLayer = null;
		private Model3DLayer nodeModelLayer = null;
		private IconLayer nodeIconLayer = null;
		// These could be common but we want to be able to turn them on/off separately
		private AnnotationLayer nodeLabelLayer = null;
		private AnnotationLayer linkLabelLayer = null;
		private RenderableLayer linkLayer = null;
		private SdtRegionLayer regionLayer = null;
		private AirspaceLayer symbolLayer = null;
		private RenderableLayer imageLayer = null;
		private MarkerLayer markerLayer = null;

		private String defaultSprite = null;
	    Hashtable<String, SdtNode> nodeTable = new Hashtable<String, SdtNode>();
		Hashtable<String, SdtSprite> spriteTable = new Hashtable<String, SdtSprite>();
	    Hashtable<String, SdtRegion> regionTable = new Hashtable<String, SdtRegion>();
		Hashtable<String, String> viewTable = new Hashtable<String, String>();
		Hashtable<String, SurfaceImage> imageTable = new Hashtable<String, SurfaceImage>();
		private String viewState;

		private File openFile = null;
		private String imagePathPrefix = null;

		private Timer pollTimer = null;
		/**
		 * Little helper class to parse commands.
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
			private String currentTile = null;
		        private String currentView = null;
			boolean pipeCmd = false;
			StringWriter buffer = new StringWriter();
			PrintWriter out = new PrintWriter(buffer);

			void SetPipeCmd(boolean theBool) {pipeCmd = true;}
			public CmdType GetCmdType(String cmd)
			{

				if (0 == cmd.length()) return CmdType.CMD_INVALID;
				boolean matched = false;
				CmdType type = CmdType.CMD_INVALID;
				String[] nextCmd = CMD_LIST;
				int i = 0;
				while (i < CMD_LIST.length-1)
				{   
					String validCmd = nextCmd[i].substring(1,nextCmd[i].length());
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
			
			public void ParseInput(String str) 
			{
				if (str.startsWith("#"))
					return;
				
				String input_buffer = new String();
				StringReader reader = new StringReader(str);

				try {

					int c;
					boolean quoted = false;
					boolean escaped = false;
					while ((c = reader.read()) != -1)
					{
						//System.out.println("C:" + Character.toString((char)c) + ":" + c); 
						if (Character.toString((char)c).equals("\""))
						{
							quoted = quoted ? false : true;
							continue;
						}
						if (quoted && !escaped && Character.toString((char)c).equals("\\"))
						{
							escaped = true;
							continue;
						}
						if (escaped) 
						{
							//System.out.println("char" + Character.toString((char)c) + " val>" + c);
							if (Character.toString((char)c).equals("n"))
								out.print("<br>");
							if (Character.toString((char)c).equals("\\"))
								out.print("\\");
							escaped = false;
							continue;
						}				
						
						out.print((char)c);
						if (quoted)
							continue;

						if (Character.isWhitespace((char)c))
						{
							input_buffer = input_buffer + buffer.toString();
							input_buffer = input_buffer.substring(0,(input_buffer.length() -1));
							if (!input_buffer.isEmpty())	
								OnCommand(input_buffer);
							input_buffer = new String();
							buffer = new StringWriter();
							out = new PrintWriter(buffer);
						}
					}  //end while

				} catch (IOException e) {
					System.out.println("End of Stream:");
				}
			} // end ParseInput

			public boolean OnCommand(String str) 
			{
				//System.out.println("OnCommand() str:" + str);
				if (null == pending_cmd)
					pending_cmd = str;			
		
				if (seeking_cmd)
				{
					switch (GetCmdType(pending_cmd))  // make sure last entry in list works! ljt
					{		
						case CMD_ARG:
							current_cmd = pending_cmd;
							seeking_cmd = false;
							break;
						case CMD_NOARG:
							ProcessCmd(pending_cmd,null); // ljt error checking?
							pending_cmd = null;
							seeking_cmd = true;
							break;
						default:
							seeking_cmd = true;
							pending_cmd = null;							
							return false; 
					}  // end switch
				}
				else  // Not seeking command
				{	
					ProcessCmd(current_cmd, str);
					seeking_cmd = true;
					pending_cmd = null;
				}  // done seeking cmd
				
				if (!pollTimer.isRunning())
					pollTimer.start();	
				return true;
			} // end OnCommand			
		};  //end class CmdParser

		public boolean validateColor(String c)
		{
			Color red = Color.red;
			Color color = getColor(c);					
			if (color.equals(red) && !c.equalsIgnoreCase("red"))
					return false;
			return true;
				
		}
		public static Color getColor(String c) {
			Color color;
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
			else
				color = Color.RED;
			return color;
		} // end sdt3d.getColor()
		
		public AppFrame() {
			super(true, false, false);
			initialize();
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			menuBar();
			
			// Start thread that opens ProtoPipe and listens for commands
			pipeThread = new PipeThread(this);
			pipeThread.start();
			System.out.println(System.getProperty("java.library.path"));

			final int POLL_INTERVAL = 100;
			// ...
			pollTimer = new Timer(POLL_INTERVAL, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					// check if some condition has changed
					// ...
					boolean doUpdate = true;
					if (doUpdate)
						getWwd().redraw();
					pollTimer.stop(); // one-shot redraw
				}
			});
				
			try {

				// Eliminate bathymetry data
				ElevationModel currentElevationModel = this.getWwd().getModel().getGlobe().getElevationModel();
				BathymetryFilterElevationModel noDepthModel = new BathymetryFilterElevationModel(currentElevationModel);
				// have the globe use the no-bathymetry elevation model
				this.getWwd().getModel().getGlobe().setElevationModel(noDepthModel);
				//this.getWwd().getSceneController().setVerticalExaggeration(5d);
				
				// Create a renderable layer to manage node component rendering
				nodeLayer = new RenderableLayer();
				nodeLayer.setName("Node Layer");
				insertBeforeCompass(getWwd(), nodeLayer);
				
				// Create renderable layer for "links" (bottom layer)
				linkLayer = new RenderableLayer();
				linkLayer.setName("Network Links");
				insertBeforeCompass(getWwd(), linkLayer);

				linkLabelLayer = new AnnotationLayer();
				linkLabelLayer.setName("Link Labels");
			    insertBeforeCompass(getWwd(), linkLabelLayer);				
	    
		        markerLayer = new MarkerLayer();
		        markerLayer.setOverrideMarkerElevation(false);
			    markerLayer.setMarkers(markers);
		        markerLayer.setKeepSeparated(false);
		        insertBeforeCompass(getWwd(),markerLayer);
			    
				nodeLabelLayer = new AnnotationLayer();
				nodeLabelLayer.setName("Node Labels");
			    insertBeforeCompass(getWwd(), nodeLabelLayer);				

				// Create renderable layer for node icons
				nodeIconLayer = new IconLayer();
				//We might override elevation on a node by node basis...
				nodeIconLayer.setAlwaysUseAbsoluteElevation(true);
				nodeIconLayer.setRegionCulling(false);
				nodeIconLayer.setName("Node Icons");
				insertBeforeCompass(getWwd(), nodeIconLayer);
				
				// Create renderable layer for node models
				nodeModelLayer = new Model3DLayer();
				nodeModelLayer.setName("Node Models");
				nodeModelLayer.setMaintainConstantSize(true);
				nodeModelLayer.setSize(1);
				insertBeforeCompass(getWwd(), nodeModelLayer);

				// Create renderable layer for symbols.
				symbolLayer = new AirspaceLayer();
				symbolLayer.setName("Node Symbols");
				insertBeforeCompass(getWwd(), symbolLayer);

				// Create renderable layer for regions.
				regionLayer = new SdtRegionLayer(); 
				regionLayer.setName("Regions");
				// We need to turn picking off so that wwj picking functions
				// still work, e.g. left click moves the globe to that location
				regionLayer.setPickEnabled(false);
				insertBeforeCompass(getWwd(), regionLayer);
				
				// Create renderable layer for image "tiles".
				imageLayer = new RenderableLayer();
				imageLayer.setName("Images");
				// We need to turn picking off so that wwj picking functions
				// still work, e.g. left click moves the globe to that location
				imageLayer.setPickEnabled(false);
				insertBeforeCompass(getWwd(),imageLayer);

	           // Set up a view controller to keep the nodes in view.
	            this.viewController = new ViewController(getWwd());
	            this.viewController.setIcons(nodeIconLayer.getIcons());
	            this.viewController.setModels(nodeModelLayer.getModels());
	            
	            // We disable view clipping, as view tracking works best when
	            // an icon's screen rectangle is known even when the icon is outside
	            // the view frustrum.  When set to "true" the view jumps up and down.
	            nodeIconLayer.setViewClippingEnabled(false);
	            
				this.getSdtLayerPanel().update(getWwd(), "sdt");
		
				SetInputFile("sdtConfigFile.txt");
				// Create select listener - ljt make items draggable?
				getWwd().addSelectListener(new SelectListener() 
				{
					private WWIcon lastToolTipIcon = null;
					private SdtPolyline lastToolTipPolyline = null;
					public void selected(SelectEvent event) {
						// Have hover selections show a picked icon's tool tip.
					if (event.getEventAction().equals(SelectEvent.HOVER)) 
					{
						// If a tool tip is already showing, undisplay it.
						if (lastToolTipIcon != null) 
						{
							lastToolTipIcon.setShowToolTip(false);
							this.lastToolTipIcon = null;
							getWwd().repaint();
						}

						if (lastToolTipPolyline != null) 
						{
							lastToolTipPolyline.setShowToolTip(false);
							this.lastToolTipPolyline = null;
							getWwd().repaint();
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
								getWwd().repaint();
							}
								
							if (event.getTopObject() instanceof SdtPolyline)
							{
								this.lastToolTipPolyline = (SdtPolyline) event
								.getTopObject();
								lastToolTipPolyline.setShowToolTip(true);
								getWwd().repaint();
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
						//	this.dragger.selected(event);

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
									getWwd().repaint();
								}
							}
						}
					}
				});
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	       private void initialize()
	        {
	           //create the west panel
	            this.westPanel = new JPanel(new BorderLayout());
	            
	            // Create logo image	            
	            logoPanel = new JPanel(new BorderLayout());
	            java.net.URL imageURL = sdt3d.class.getResource("sdtLogo.jpg");
	            ImageIcon image = null;
	            if (imageURL != null) {
	               image = new ImageIcon(imageURL);
	 	           image = new ImageIcon(image.getImage().getScaledInstance(logoPixelSize, logoPixelSize, 0)); 

	            }
	            logoPanel.add(new JLabel(image));
	            
	            // Must put the layer grid in a container to prevent scroll panel from stretching their vertical spacing.
	            JPanel dummyPanel = new JPanel(new BorderLayout());
	            dummyPanel.add(this.logoPanel, BorderLayout.NORTH);
	            
	            this.statusPanel = new StatusPanel();
	            dummyPanel.add(statusPanel, BorderLayout.CENTER);
	            this.sdtLayerPanel = new SdtLayerPanel(getWwd(), null);
	            dummyPanel.add(this.sdtLayerPanel, BorderLayout.SOUTH);
	            	
	            this.westPanel.add(dummyPanel, BorderLayout.NORTH);
	            this.getContentPane().add(westPanel, BorderLayout.WEST);           
	           
	            this.pack();

	            // Center the application on the screen.
	            WWUtil.alignComponent(null, this, AVKey.CENTER);
	            this.setResizable(true);
	            
	        }
       public SdtLayerPanel getSdtLayerPanel()
       {
           return sdtLayerPanel;
       }		
       public StatusPanel getStatusPanel()
       {
       	return this.statusPanel;
       }
		WWIcon lastPickedIcon;
		
		private void highlight(Object o) {
			// Manage highlighting of icons.

			if (this.lastPickedIcon == o)
				return; // same thing selected

			// Turn off highlight if on.
			if (this.lastPickedIcon != null) {
				this.lastPickedIcon.setHighlighted(false);
				this.lastPickedIcon = null;
			}

			// Turn on highlight if object selected.
			if (o != null && o instanceof WWIcon) {
				this.lastPickedIcon = (WWIcon) o;
				this.lastPickedIcon.setHighlighted(true);
			}
		}

		private void menuBar() {
			menuBar = new JMenuBar();
			// build file menu
			fileMenu = new JMenu("File");
			openItem = new JMenuItem("Open File...");
			screenItem = new JMenuItem("Save a Screenshot ");
			listenItem = new JMenuItem("Listen to port...");
			exitItem = new JMenuItem("Exit");
			openItem.addActionListener(this);
			screenItem.addActionListener(this);
			listenItem.addActionListener(this);
			exitItem.addActionListener(this);
			fileMenu.add(openItem);
			fileMenu.add(screenItem);
			fileMenu.add(listenItem);
			fileMenu.addSeparator();
			fileMenu.add(exitItem);
			menuBar.add(fileMenu);

			
			// build view menu
			viewMenu = new JMenu("View");
			menuBar.add(viewMenu);
			layerMenu = new JMenu("Layer Controls");
			viewMenu.add(layerMenu);
			allLayersItem = new JMenuItem("Show all");
			sdtLayersItem = new JMenuItem("Sdt only");
			offLayersItem = new JMenuItem("None");
			sdtLayersItem.addActionListener(this);
			allLayersItem.addActionListener(this);
			offLayersItem.addActionListener(this);
			layerMenu.add(sdtLayersItem);
			layerMenu.add(allLayersItem);
			layerMenu.add(offLayersItem);

			// build bookmark menu
			bookmarkMenu = new JMenu("Bookmarks");
			viewMenu.add(bookmarkMenu);						
			bookmarkMenu.add(bookmarkItem = new JMenuItem("Bookmark this View"));
			bookmarkMenu.addSeparator();
			bookmarkItem.addActionListener(this);

			// build status menu
			statusItem = new JCheckBoxMenuItem("Status");
			statusItem.setSelected(true);
			viewMenu.add(statusItem);
			statusItem.addActionListener(this);
			
			// build toggle multiple links menu
			collapseLinksItem = new JCheckBoxMenuItem("Collapse Multiple Links");
			collapseLinksItem.setSelected(false);
			viewMenu.add(collapseLinksItem);
			collapseLinksItem.addActionListener(this);
			
			this.setJMenuBar(menuBar);
	
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == openItem) {
				int returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					openFile = fc.getSelectedFile();
					String fileName = openFile.getAbsolutePath();

					if (fileThread != null && openFile != null)
					{
						fileThread.stopThread();
						fileThread.stopRead();
						try{
							  Thread.currentThread();
							  Thread.sleep(1000);//sleep for 1000 ms
							}
							catch(InterruptedException ie){
							//If this thread was interrupted by nother thread 
						}	
						Clear("all");
						getWwd().redraw();						
					}
					fileThread = new FileThread(this,fileName,false);
					fileThread.start();
				} 
				else {
					System.out.println("Open command cancelled by user.");
				}
			} else if (event.getSource() == screenItem) {
				WorldWindowGLCanvas canvas = getWwd();
				int framewidth = canvas.getSize().width; // get the canvas'
				// dimensions
				int frameheight = canvas.getSize().height;
				canvas.getContext().makeCurrent();
				BufferedImage image = Screenshot.readToBufferedImage(
						framewidth, frameheight);

				int returnVal = fc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						javax.imageio.ImageIO.write(image, "PNG", file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("Screenshot command cancelled by user.");
				}
			} 
			else if (event.getSource() == listenItem) {
				
				if (socketThread == null)
				{
					String inputValue = JOptionPane.showInputDialog("Listen [udp,][<addr>/]<port>"); 
					if (inputValue == null || inputValue.isEmpty())
						return;  // check cancel button?

					if (SetListen(inputValue))
					{
						JOptionPane.showMessageDialog( getWwjPanel(), "Listening to " + inputValue);		
					} else
					{
		               JOptionPane.showMessageDialog( getWwjPanel(), "Unable to listen to " + inputValue);							
		               return;
					}
					// There must be a way to swap the menu items text rather than this..				
					fileMenu.remove(listenItem);
					listenItem = new JMenuItem("Off " + socketPort);
					listenItem.addActionListener(this);
					fileMenu.add(listenItem,2);
				} else
				{
					// stop the thread, recreate menu item
					socketThread.stopThread();
					socketThread = null;
					fileMenu.remove(listenItem);
					listenItem = new JMenuItem("Listen to port... ");
					listenItem.addActionListener(this);
					fileMenu.add(listenItem,2);

				}
				
			} else if (event.getSource() == bookmarkItem) {
				
				viewState = ((OrbitView) getWwd().getView()).getRestorableState();
				//System.out.println("ViewState: " + viewState);
		        String str = JOptionPane.showInputDialog(null, "Enter view name : ", 
		        		"", 1);
				
		        if (str != null)
		        {
		        	JMenuItem newItem;
		        	bookmarkMenu.add(newItem = new JMenuItem(str));
					newItem.addActionListener(this);
		        	viewTable.put(str,viewState);
		        	int value = JOptionPane.showConfirmDialog(null, "Save to disk?");
		       		        	
		        	if (JOptionPane.YES_OPTION == value) 
		        	{

		        		try {
		        		
		        			BufferedWriter out = new BufferedWriter(new FileWriter(str + ".xml"));
		        			out.write(viewState);
		        			out.close();
		        		} catch (IOException e) {
		        			System.out.println("Error saving view state to file " + str);
		        		}
		        	}

		        }

			} else if (viewTable.get(event.getActionCommand()) != null)	
			{
				String theView = viewTable.get(event.getActionCommand());
				if (theView != null)
				    ((OrbitView) getWwd().getView()).restoreState(theView);
				getWwd().redraw();
				getWwd().redraw();
 			
			} else if (event.getSource() == exitItem) {
				System.exit(0);
			} else if (event.getSource() == sdtLayersItem) {
				this.getSdtLayerPanel().setVisible(true);
				this.getSdtLayerPanel().update(getWwd(), "sdt");
			} else if (event.getSource() == allLayersItem) {
				this.getSdtLayerPanel().setVisible(true);
				this.getSdtLayerPanel().update(getWwd(), "all");
			} else if (event.getSource() == offLayersItem) {
				this.getSdtLayerPanel().setVisible(false);
			} else if (event.getSource() == statusItem) {
				if (statusItem.isSelected()  ) {
					this.getStatusPanel().setVisible(true);
				} else {
					this.getStatusPanel().setVisible(false);
				}				
			} else if (event.getSource() == collapseLinksItem) {
				// TBD: do we want to redraw the links immediately 
				// or wait for a position change?
				if (collapseLinksItem.isSelected() ) {
					collapseLinks = true;
					refreshLinks();
					getWwd().redraw();
				} else {
					collapseLinks = false;
					refreshLinks();
					getWwd().redraw();
				}
				
			}
		}
		private String loadState(String val)
		{
			StringBuilder contents = new StringBuilder();
			try {
				// use buffering, reading one line at a atime
				// FileReader always assumed default encoding is OK!
				BufferedReader input = new BufferedReader(new FileReader(val));
				try {
					String line = null; 

					while ((line = input.readLine()) != null)
					{
						contents.append(line);
						contents.append(System.getProperty("line.separator"));
					}
				}
				finally {
					input.close();
				}
			}
			catch (IOException e) {
				System.out.println("Error reading " + val + " state from file " ); 
				return null;
			}
			
			return contents.toString();
				
		}
		private void refreshLinks()
		{
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtNode theNode = i.next().getValue();
				theNode.updateLinks(getWwd().getSceneController().getDrawContext());
				theNode.recreateLinkLabels(linkLabelLayer);
			}
		}
		
		private void removeNode(SdtNode current_node)
		{
			if (null != current_node)
			{
				// we can't aren't using finalize because
				// we want to delete any associated markers & linkLabels
				// and need access to the rendering layers
				//	current_node.finalize(linkLayer);  
				Map<String, List<SdtLink>> linkTable = current_node.getLinkTable();				
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
							Polyline line = theLink.getLine();
				             if (theLink.getMarker() != null)
				                 markers.remove(theLink.getMarker());
				             if (theLink.getLabel() != null)
				            	 linkLabelLayer.removeAnnotation(theLink.getLabel());
							if (null != line)
								linkLayer.removeRenderable(line);							
						}					
					}							
					it.remove(); 
				}							
							
				// Remove associated sprite,label,symbol,&links
				if (current_node.hasLabel()) {
					nodeLabelLayer.removeAnnotation(current_node
							.getLabel());
					current_node.removeLabel();
				}						
				if (current_node.hasSprite()) {
					switch (current_node.getSprite().getType()) {
					case MODEL:
						if (current_node.getNodeModel() != null)  // ljt otherwise getModel creates one - rework
							nodeModelLayer.removeModel(current_node.getNodeModel());
						break;
					case ICON:
						if (current_node.getNodeIcon() != null) // ljt rework
							nodeIconLayer.removeIcon(current_node.getNodeIcon());
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
					current_node.removeSprite();
				}
				if (current_node.hasSymbol())
				{
					symbolLayer.removeAirspace(current_node.getSymbol().getAirspace());
					current_node.removeSymbol();							
				}
			}
			nodeLayer.removeRenderable(current_node);
							
		} // end removeNode

		public String GetPipeName() {return pipe_name;}

		public boolean SetPipeName(String val) 
		{
			if (0 == val.length())
				return false; // no <spriteName>  TODO: error handling 
			pipe_name = val;
			
			// is this correct??
			if (pipeThread.isAlive())
				pipeThread.stopThread();
						
			// Start thread that opens ProtoPipe and listens for commands
			pipeThread = new PipeThread(this);
			pipeThread.start();
					
			return true;
		}
		
		private boolean SetFlyTo(String val)
		{
			if (0 == val.length())
				return false;
			
			// For now, we just pan/zoom to the "center" of
			// the given "bgbounds" coordinates
			String[] coord = val.split(new String(","));
			if (coord.length < 2) {
				// TODO display bad coords error
				return false;
			}
			Float f = new Float(coord[1]);
			double lat = f.doubleValue();
			f = new Float(coord[0]);
			double lon = f.doubleValue();
			Float alt = (coord.length > 2) ? new Float(coord[2])
			: new Float(0);
			
			Position targetPos = Position.fromDegrees(lat,
					lon, alt);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();

            if(view != null)
            {
 
    			Globe globe = wwd.getModel().getGlobe();
     			double elev = globe.getElevation(targetPos.getLatitude(),
						targetPos.getLongitude());
	
             	view.goTo(new Position(targetPos, alt),alt);
            }
            return true;
		}			
		private boolean SetZoom(String val)
		{

			if (0 == val.length())
				return false;
			double n = new Double(val);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			view.setZoom(n);
			return true;
		}
		private boolean SetHeading(String val)
		{
			if (0 == val.length())
				return false;
			double n = new Double(val);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			view.setHeading(Angle.fromDegrees(n));
			return true;
		}
		private boolean SetPitch(String val)
		{
			if (0 == val.length())
				return false;
			double n = new Double(val);
			WorldWindow wwd = getWwd();
			OrbitView view = (OrbitView) wwd.getView();
			view.setPitch(Angle.fromDegrees(n));
			return true;
		}
		private boolean SetBackgroundBounds(String val)
		{
			// For now, we just pan/zoom to the "center" of
			// the given "bgbounds" coordinates
			String[] coord = val.split(new String(","));
			if (4 != coord.length) {
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
			return true;
		} 

		private boolean SetScale(String scale)
		{
			if (0 == scale.length() || currentSprite == null)
				return false; //TODO: error handling 
			currentSprite.setScale(Float.valueOf(scale));
			return true;
		}
		private boolean SetSprite(String spriteName)
		{
			if (0 == spriteName.length())
				return false; // no <spriteName>  TODO: error handling 
			currentSprite = (SdtSprite) spriteTable.get(spriteName);
			if (null == currentSprite) {
				// It's a new one
				currentSprite = new SdtSprite(spriteName);
				// If first entry to our table, squirrel away
				// default sprite.
				if (spriteTable.isEmpty())
					defaultSprite = spriteName;				
				spriteTable.put(spriteName, currentSprite);
			}
			return true;
		} // end SetSprite
		
		private String FindFile(String val)
		{
			// Rewrite all this using java path helper functions at some point
			// for now just get it DONE!
			
			// See if the file was fully qualified
			val.trim();
			File f1 = new File(val);  
			if (f1.exists())
				return val;
			
			// if a "path" prefix was specified, try to find it there
			String imageFilePath = null;
			if (null != imagePathPrefix) {

				String[] cmdTokens = imagePathPrefix.split(new String(";"));
				
				// On linux systems we allow ":" as the delimiter
				if (imagePathPrefix.contains(":") && !System.getProperty("os.name").startsWith("Windows"))
					cmdTokens = imagePathPrefix.split(new String(":"));

				for (int i = 0; i < cmdTokens.length && cmdTokens.length != 0; i++) 
				{	
					imageFilePath = new String(cmdTokens[i]);
					if (imageFilePath.startsWith("\""))
						imageFilePath = imageFilePath.substring(1,imageFilePath.length());
					if (imageFilePath.endsWith("\""))
						imageFilePath = imageFilePath.substring(0,(imageFilePath.length() -1));

					if (!imageFilePath.endsWith("/"))
						imageFilePath = imageFilePath + "/";

					imageFilePath = new String(imageFilePath + val);										
					imageFilePath.trim();

					f1 = new File(imageFilePath);
					if (!f1.exists()) 
					{
						// is it a relative path?  (We would have found this earlier
						// in the logic on a linux box. For some reason windows relative
						// paths are not handled by java?  Is this a 0.7 thing?
						if (imageFilePath.startsWith(".."))
						{
							imageFilePath = imageFilePath.substring(2, imageFilePath.length());
							imageFilePath = fc.getCurrentDirectory().getParent().concat(imageFilePath);

						} else
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
						imageFilePath = imageFilePath.replaceAll("\\\\","/");
					}
					else
					{
						imageFilePath = new String(cmdTokens[1]) + val;
						imageFilePath = imageFilePath.replaceAll("\\\\","/");						
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
					System.out.println("imageFile " + imageFilePath + " does not exist.");
					return null;
				}
			}
			return imageFilePath;
		}
		private boolean LoadTile(String val)
		{
			if ((0 == val.length()) || currentTile == null) return false;

			// For now, we just pan/zoom to the "center" of
			// the given "bgbounds" coordinates
			String[] coord = val.split(new String(","));
			if (4 != coord.length) {
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
			
	        SurfaceImage theImage = null;
			theImage = (SurfaceImage) imageTable.get(currentTile);

			if (null != theImage) 
			{
				// Shouldn't be null, we create it in setImage
				imageLayer.removeRenderable(theImage);  
				imageTable.remove(currentTile);
				
			} 
			theImage = new SurfaceImage(theImage.getImageSource(), Sector.fromDegrees(upperLat,lowerLat,upperLon,lowerLon));
			imageTable.put(currentTile, theImage);	
			imageLayer.addRenderable(theImage);
	        currentTile = null;
			return true;
		}
		private boolean SetTile(String val)
		{
		    if (0 == val.length()) return false;
			currentTile = val; // temporary until we get file image rework!
			return true;
		}
		private boolean SetTileImage(String val)
		{
			if (0 == val.length() || null == currentTile) return false; //no <imageFile>

			SurfaceImage theImage = (SurfaceImage) imageTable.get(currentTile);
			if (null != theImage)
			{
				imageLayer.removeRenderable(theImage);
				imageTable.remove(theImage);
			}

			if (val.equalsIgnoreCase("NONE"))
			{
				return true;
			}
			
			String fileName = FindFile(val);
			if (fileName == null) return false;

			theImage = new SurfaceImage(fileName,Sector.EMPTY_SECTOR);
			imageTable.put(currentTile,theImage);

			
			return true;
		}		
		private boolean SetImage(String val)
		{			
			if (0 == val.length() || null == currentSprite) return false; //no <imageFile>			
				
			String fileName = FindFile(val);			
			if (fileName == null) return false;
			
			if (currentSprite.Load(fileName)) 
				return true;
							
			return false;
		}			
		private boolean SetNode(String nodeName)
		{
			if (0 == nodeName.length())
				return false; // no <nodeName>

			currentNode = (SdtNode) nodeTable.get(nodeName);
			if (null == currentNode) {
				// It's a new one
				currentNode = new SdtNode(nodeName);
				// ljt remember to delete !
				nodeLayer.addRenderable(currentNode);
			    // By default set the sprite to the first entry in the
				// sprite icon list, if it exists.
				if (!spriteTable.isEmpty())
				{
					SdtSprite theSprite = (SdtSprite) spriteTable.get(defaultSprite);
					if (theSprite != null)
						currentNode.setSprite(theSprite);
				}
				nodeTable.put(nodeName, currentNode);
			}
			return true;
		} // end SetNode
		private boolean SetType(String type)
		{
			if (0 == type.length() || null == currentNode) return false; // no <Type>
			SdtSprite theSprite = (SdtSprite) spriteTable.get(type);

			if (theSprite != null && theSprite.getType() == SdtSprite.Type.INVALID) return false;  
			if ((type.equalsIgnoreCase("NONE") && theSprite == null))
			{
				theSprite = new SdtSprite(type);
				theSprite.setType(SdtSprite.Type.NONE);
			}
			if (theSprite == null) return false;
			// There are several things to do here:
			// 1) Does the node have a position?
			// a) if so, add the sprite to its layer
			// b) else, just keep track
			// 2) Does the node already have a sprite?
			// a) if so, same or different?
			// b) if different, we need to replace the model or icon
			boolean newSprite = true;
			if (currentNode.isDisplayed()) {
				// a) remove old model or icon from its layer if we are
				//    changing it
				if (currentNode.hasSprite()) 
				{	// are we changing it?
					if (!currentNode.getSprite().getName().equalsIgnoreCase(theSprite.getName()))
					{
						switch (currentNode.getSprite().getType()) {
						case MODEL:
							nodeModelLayer.removeModel(currentNode
								.getModel());
							break;
						case ICON:
							nodeIconLayer.removeIcon(currentNode.getIcon());
							break;						
						case INVALID:
							System.out.println("sprite "
									+ currentNode.getSprite().getName()
									+ " is INVALID!");
							break;
						} 
						currentNode.setSprite(theSprite);
					} else // end node has a different sprite
						newSprite = false;
					
				} else
					// node has no sprite, assign it 
					currentNode.setSprite(theSprite);


				// only add it to the layer if it's a changed or new
				if (newSprite)
				{
					switch (theSprite.getType()) {
					case MODEL:
						nodeModelLayer.addModel(currentNode.getModel());
						break;
					case ICON:
					{
						nodeIconLayer.addIcon(currentNode.getIcon());
						break;
					}
					case NONE:
						break;
					case INVALID:
						System.out.println("sprite "
									+ currentNode.getSprite().getName()
									+ " is INVALID!");
						break;
					}
				}	
			} else {
				// current node isn't displayed, but assign the sprite so we have it
				// when it is.
				currentNode.setSprite(theSprite);
			}
			return true;
		} // end SetType

		private boolean SetRegionPosition(String val)
		{
			if ((0 == val.length()) || currentRegion == null)
				return false;

			// Get the LatLon of the symbol
			String[] coord = val.split(new String(","));
			
			if (coord.length < 2) {
				// TODO display bad coords error
				return false;
			}
			Float f = new Float(coord[1]);
			double lat = f.doubleValue();
			f = new Float(coord[0]);
			double lon = f.doubleValue();

			Float alt = (coord.length > 2) ? new Float(coord[2])
			: new Float(0);
			
			Position p = Position.fromDegrees(lat, lon, alt);	
			currentRegion.setPosition(p);
			return true;
		}
		private boolean SetPosition(String val)
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

			boolean firstPosition = !currentNode.hasPosition();
			Double lon = 0.0;			
			Double lat = 0.0;
			if (!coord[1].equalsIgnoreCase("x"))
				lat = new Double(coord[1]);
			else 
				if (!firstPosition)
					lat = new Double(currentNode.getPosition().getLatitude().degrees);
			
			if (!coord[0].equalsIgnoreCase("x"))
				lon = new Double(coord[0]);
			else
				if (!firstPosition)
					lon = new Double(currentNode.getPosition().getLongitude().degrees);
			
			if ((lat == -9999.0 || lon == -9999.0))
			{
				if (firstPosition)
					return true;
			}
			
			Float alt = currentNode.getAltitude();
			if (coord.length > 2 && !coord[2].equalsIgnoreCase("x"))
				// leave node at last known altitude if not set
				alt = new Float(coord[2]);		 

			if (coord.length > 3 && !coord[3].equalsIgnoreCase("x"))
			{
				if (coord[3].equalsIgnoreCase("agl"))
					currentNode.setUseAbsoluteElevation(false);
				else
					if (coord[3].equalsIgnoreCase("msl"))
						currentNode.setUseAbsoluteElevation(true);
			}
			// We save the altitude separately as we may need it depending on what
			// we are rendering
			currentNode.setAltitude(alt);
			currentNode.setFollowTerrain(alt == 0);
			Position p = Position.fromDegrees(lat, lon, alt);	
			if (currentNode.isDisplayed())
			{
				currentNode.setPosition(p);
			} 
			else 
			{
				// We are displaying this node for the first time, add it to its layer
				currentNode.setPosition(p);

				if (currentNode.hasSprite()) 
				{
					switch (currentNode.getSprite().getType()) {
					case MODEL:
						nodeModelLayer.addModel(currentNode.getModel());
						break;
					case ICON:
						nodeIconLayer.addIcon(currentNode.getIcon());
						break;
					case NONE:
						break;
					case INVALID:
						System.out.println("sprite "
								+ currentNode.getSprite().getName()
								+ " is INVALID!");
						break;
					}
				}  // end currentNode.hasSprite()
					
				GlobeAnnotation label = currentNode.getLabel();
				if (null != label)
					nodeLabelLayer.addAnnotation(label);
				
			} // end else (firstDisplay)
			if (firstPosition) 
			{
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
							Polyline line = theLink.getLine();
							if (null != line)
								linkLayer.addRenderable(line);
							// Labels might get defined before we have any positions...
							// so let's force them to get created now that we have 
							// a position... messy!
							if (theLink.showLabel() && !theLink.hasLabel() && theLink.hasPosition())
							{
								GlobeAnnotation label = theLink.getLabel();
								linkLabelLayer.addAnnotation(label);
							}
						}
					}
				}	
			} 

			// Update the view in case we're keeping any nodes in the frustrum
			viewController.sceneChanged(); 

			return true;
		} // end SetPosition

		private boolean Clear(String val)
		{	// check to see if val is valid, valueOf crashes if it isn't
			boolean valid = false;
		    for (ObjectList olv : ObjectList.values())
		    {
		    	if (olv.toString().equalsIgnoreCase(val))
		    		valid = true;
		    }
		    if (!valid) {System.out.println("Invalid object type: " + val); return false;}
		    
			ObjectList ol = ObjectList.valueOf(val.toUpperCase());

			 switch (ol)
			 {
			 case ALL:
				 // Removing nodes removes associated links, labels, sprites, and symbols
				 RemoveNodes();
				 RemoveRegions();
				 RemoveTiles();
				 RemoveLinkLabels();
				 fileThread.stopThread();
				 fileThread = null;
				 break;
			 case NODES:
				 RemoveNodes();
				 break;
			 case LABELS:
				 RemoveLabels();
				 break;
			 case LINKS:
				 RemoveLinks();
				 RemoveLinkLabels();
				 break;				 
			 case LINKLABELS:
				 RemoveLinkLabels();
				 break;
			 case SYMBOLS:
				 RemoveSymbols();
				 break;
			 case SPRITES:
				 RemoveSprites();
				 break;
			 case REGIONS:
				 RemoveRegions();
				 break;
			 case TILES:
				 RemoveTiles();
				 break;
			 case INVALID:
				 System.out.println("Invalid object type for clear command");
				 break;
			 default:
				 System.out.println("Invalid object type for clear command");
				 break;
			 }
					
			((OrbitView) getWwd().getView()).setPitch(Angle.fromDegrees(0));
 			((OrbitView) getWwd().getView()).setHeading(Angle.fromDegrees(0));
			getWwd().redraw();		
			
			//fileThread.startThread();  // ljt
			return true;
		}
		private void RemoveNodes()
		{
			// Remove all nodes
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtNode theNode = i.next().getValue();
				removeNode(theNode);
				i.remove();
			}			
		}
		private void RemoveLinkLabels()
		{
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
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
						Iterator<SdtLink>itr = links.iterator();

						while (itr != null && itr.hasNext())
						{
							SdtLink theLink = itr.next();
							GlobeAnnotation theLabel = theLink.getLabel();
							if (theLabel != null)
							{
								linkLabelLayer.removeAnnotation(theLabel);
								theLink.removeLabel();
							}
						}					
					}							
					//it.remove(); 
				}
			}	
		}
		private void RemoveLinks()
		{
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				// We don't use finalize because we want to delete any
				// associated markers & linkLabels (we need access to the
				// rendering layers)
			    //	current_node.finalize(linkLayer);  

				Map<String, List<SdtLink>> linkTable = current_node.getLinkTable();
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
							if (theLink.getMarker() != null)
								markers.remove(theLink.getMarker());
							if (theLink.getLabel() != null)
								linkLabelLayer.removeAnnotation(theLink.getLabel());
							Polyline line = theLink.getLine();
							if (null != line)
								linkLayer.removeRenderable(line);							
						}					
					}							
					it.remove(); 
				}
			}
		}
		private void RemoveLabels()
		{
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				// Remove all labels
				// Remove associated sprite,label,symbol,&links
				if (current_node.hasLabel()) {
					nodeLabelLayer.removeAnnotation(current_node
							.getLabel());
					current_node.removeLabel();
				}							
			}
		}
		private void RemoveSymbols()
		{
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				if (current_node.hasSymbol())
				{
					// ljt use clear all?
					symbolLayer.removeAirspace(currentNode.getSymbol().getAirspace());
					current_node.removeSymbol();							
				}
			}
		}
		private void RemoveSprites()
		{
			for (Iterator<Entry<String,SdtNode>> i = nodeTable.entrySet().iterator(); i.hasNext();)
			{
				SdtNode current_node = i.next().getValue();
				if (current_node.hasSprite()) {
					switch (current_node.getSprite().getType()) {
					case MODEL:
						if (current_node.getNodeModel() != null)  // ljt otherwise getModel creates one - rework
							nodeModelLayer.removeModel(current_node.getNodeModel());
						break;
					case ICON:
						if (current_node.getNodeIcon() != null) // ljt rework
							nodeIconLayer.removeIcon(current_node.getNodeIcon());
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
					current_node.removeSprite();
				}
			}
		}
		private void RemoveRegions()
		{		
			// remove all regions
			for (Iterator<Entry<String,SdtRegion>> i = regionTable.entrySet().iterator(); i.hasNext();)
			{
				// Only one call to i.next() allowed!
				SdtRegion theRegion = i.next().getValue();
				regionLayer.removeRenderables(theRegion);
				regionLayer.removeRegion(theRegion);
				i.remove();
			}			
		}
		private void RemoveTiles()
		{
			// remove all images
			imageLayer.removeAllRenderables();
			
		}
		private boolean Delete(String nodeName)  
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
				return false; //no object name

			if (attr[0].equalsIgnoreCase("all"))
			{
				// Note that delete all deletes sprites whereas
				// clear all does not
				objectName = "all";
				Delete("node,all");
				Delete("label,all");
				Delete("link,all");
				Delete("region,all");
				Delete("sprite,all");
				Delete("tile,all");
				Delete("symbol,all");
			}
			
			if (attr[0].equalsIgnoreCase("node"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveNodes();
					return true;
				}
				
				SdtNode theNode = (SdtNode) nodeTable.get(objectName);
				if (theNode != null)
				{
					removeNode(theNode);				
					nodeTable.remove(theNode.getName());  	
				}
			}
			if (attr[0].equalsIgnoreCase("region"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveRegions();
					return true;
				}
				
				SdtRegion theRegion = (SdtRegion) regionTable.get(objectName);
				if (theRegion != null)
				{
					regionLayer.removeRenderables(theRegion);
					regionLayer.removeRegion(theRegion);					
					regionTable.remove(theRegion.getName());
				}
			}
			if (attr[0].equalsIgnoreCase("link"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveLinks();
					RemoveLinkLabels();
					return true;
				}
				
				// rebuild linkName
				String[] linkName = nodeName.split("link,");
				SetUnlink(linkName[1]);
			}
			if (attr[0].equalsIgnoreCase("tile"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveTiles();
					return true;
				}
				
		        SurfaceImage theImage = null;
				theImage = (SurfaceImage) imageTable.get(objectName);

				if (null != theImage) 
				{
					imageLayer.removeRenderable(theImage);  
					imageTable.remove(currentTile);					
				} 	
			}
			if (attr[0].equalsIgnoreCase("sprite"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveSprites();
					for (Iterator<Entry<String,SdtSprite>> i = spriteTable.entrySet().iterator(); i.hasNext();)
					{
						// Only one call to i.next() allowed!
						SdtSprite theSprite = i.next().getValue();
						//regionLayer.removeRenderables(theRegion);
						//regionLayer.removeRegion(theRegion);
						i.remove();
					}			
					return true;
				}
				
		        SdtSprite theSprite = null;
				theSprite = (SdtSprite) spriteTable.get(objectName);

				if (null != theSprite) 
				{
					spriteTable.remove(theSprite);
				} 	
			}
			if (attr[0].equalsIgnoreCase("symbol"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveSymbols();
					return true;
				}
				System.out.println("INFO: Named symbol deletion not yet supported.");
			}
			if (attr[0].equalsIgnoreCase("label"))
			{
				if (objectName.equalsIgnoreCase("all"))
				{
					RemoveLabels();
					return true;
				}
				System.out.println("INFO: Named label deletion not yet supported.");
			}
			return true;
		}		
		
		private boolean SetSize(String val)
		{
			if ((0 == val.length()))
				return false; // no size

			if (null == currentSprite) {
				// TODO warn about no "node" specified
				return false;
			}
			String[] dim = val.split(",");
			// <dimensions> is in format "width,height"
			if (dim.length < 2) {
				// TODO warn about bad width,height
				return false;
			}
			Integer width = new Integer(dim[0]);
			Integer height = new Integer(dim[1]);
			currentSprite.setIconSize(width.intValue(), height
					.intValue());
			return true;
		}
		private boolean SetLength(String val)
		{
			if ((0 == val.length()))
				return false;
			
			if (null == currentSprite) {
				return false;
			}
			Float length = new Float(val);
			currentSprite.setModelLength(length.doubleValue());
			
			return true;
		}
		private boolean SetLight(String val)
		{
			if ((0 == val.length()))
				return false;  // need on/off
			
			if (null == currentSprite) {
				// TODO warn about no "node" specified
				return false;
			}
			String status = val;
			if (status.equalsIgnoreCase("on")) {
				currentSprite.setModelUseLighting(true);
			} else if (status.equalsIgnoreCase("off")) {
				currentSprite.setModelUseLighting(false);
			} else {
				// TODO warn about bad status
				return false;
			}
			return true;
		}
		public static boolean FollowAll()
		{
			return followAll;
		}
		private boolean SetFollow(String val)
		{
			if (0 == val.length()) return false;
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
				if (attrs.length < 2) return false;
				index = 1;
			}
			if (attrs[0 + index].equalsIgnoreCase("all")) {
        		if ((attrs.length + index > 1) && attrs[1 + index].equalsIgnoreCase("off"))
        			followAll = false;
        		else
        			followAll = true;
		        for (WWIcon icon : nodeIconLayer.getIcons())
		        {
		        	if ((attrs.length + index) > 1)

		        	icon.setValue(AVKey.FEEDBACK_ENABLED, followAll);
	            }	 
		        for (WWModel3D model : nodeModelLayer.getModels())
		        {
		        	if ((attrs.length + index) > 1)
		        	model.setValue(AVKey.FEEDBACK_ENABLED, followAll);
		        }
				return true;
			}			
			SdtNode theNode = (SdtNode) nodeTable.get(attrs[0 + index]);
			if (theNode == null) return false;
			if ((attrs.length + index) > 1 && attrs[1 + index].equalsIgnoreCase("off"))
					theNode.setFeedbackEnabled(false);
			theNode.setFeedbackEnabled(true);
			
			return true;
		}
		private boolean SetLabel(String val)
		{
			if ((0 == val.length()))
				return false;  // no label
			if (null == currentNode) {
				// TODO warn about no "node" specified
				return false;
			}
			String[] attrs = val.split(",");
			if (attrs.length > 1) {
				currentNode.setLabelText(attrs[1]);
				}

			if (attrs[0].equalsIgnoreCase("off")) {
				if (currentNode.hasLabel()) {
					nodeLabelLayer.removeAnnotation(currentNode
							.getLabel());
				}
				// Remove label sets showlabel to false (and therefore we
				// don't keep recreating it!)
				currentNode.removeLabel();

			} else {
				currentNode.showLabel();
				if (!attrs[0].equalsIgnoreCase("on"))
					currentNode.setLabelColor(getColor(attrs[0]));

				if (!currentNode.hasLabel()	&& currentNode.hasPosition()) {
					GlobeAnnotation label = currentNode.getLabel();
					if (null != label)
					{
						nodeLabelLayer.addAnnotation(label);					
					}
				}
			}
			return true;
		} // end label
		

		private boolean SetSymbol(String val)
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
			if (SdtSymbol.getType(symbolType) == SdtSymbol.Type.INVALID)			{
				System.out.println("Invalid symbol type: " + symbolType);
				return false;
			}
			
			if (currentNode.hasSymbol() && currentNode.getSymbol().getAirspace() != null)
			{
				symbolLayer.removeAirspace(currentNode.getSymbol().getAirspace());
				if (symbolType.equalsIgnoreCase("none"))
				{
					currentNode.setSymbol(null);
					return true;
				}
				currentSymbol = currentNode.getSymbol();
				currentSymbol.setType(symbolType); // we might be changing it
			}	
			else
			{
				currentSymbol = new SdtSymbol(symbolType,currentNode);
			}
			currentSymbol.setInitialized(false);	
							
			// symbol <symbolName>[,<color>,[<thickness>[,<x_radius>[,<y_radius>[,opacity]]]]
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
					currentSymbol.setWidth(Double.valueOf(attrs[ind]));
					// We may be resetting a fixed symbol to icon hugging
					if (currentSymbol.getWidth() > 0)
						currentSymbol.isIconHugging(false);
					else
						currentSymbol.isIconHugging(true);
					break;
				}
				case 4:
				{
					currentSymbol.setHeight(Double.valueOf(attrs[ind]));
					// We may be resetting a fixed symbol to icon hugging
					// ljt I think this may be broken??
					
					if (currentSymbol.getHeight() > 0)
						currentSymbol.isIconHugging(false);
					else 
						currentSymbol.isIconHugging(true);
					break;
				}
				case 5:
				{
					currentSymbol.setOpacity(Double.valueOf(attrs[ind]));
					break;
				}
				}
			}	
			
			currentNode.setSymbol(currentSymbol);
			currentSymbol.initialize(getWwd().getSceneController().getDrawContext());
			symbolLayer.addAirspace(currentSymbol.getAirspace());
			return true;
		} // end SetSymbol

		private boolean SetLine(String val)
		{
			if (currentLinkSet.isEmpty()) return false; 
			if (0 == val.length()) return false;
			
			String[] attr = val.split(",");
		
			Iterator<SdtLink>itr = currentLinkSet.iterator();
			while (itr != null && itr.hasNext())
			{
				SdtLink theLink = itr.next();

				theLink.setColor(getColor(attr[0]));
				if (attr.length > 1) 
				{
					Integer thickness = new Integer(attr[1]);
					if (thickness < 1)
						thickness = 1;
					theLink.setWidth(thickness);
				}
			}			
			return true;
		}
		private boolean SetLinkLabel(String val)
		{
			if (currentLinkSet.isEmpty()) return false;
			if ((0 == val.length())) return false;  // no label
			
			Iterator<SdtLink>itr = currentLinkSet.iterator();
			while (itr != null && itr.hasNext())
			{
				SdtLink theLink = itr.next();
				String[] attrs = val.split(",");
				
				if (attrs[0].equalsIgnoreCase("off"))
				{
					if (theLink.hasLabel())
					{
						linkLabelLayer.removeAnnotation(theLink.getLabel());
						theLink.removeLabel();
					}
					if (attrs.length > 1)
						theLink.setLabelText(attrs[1]);
				} else
				{
					theLink.setShowLabel();
					if (attrs.length > 1)
						theLink.setLabelText(attrs[1]);
					if (!attrs[0].equalsIgnoreCase("on"))
						theLink.setLabelColor(getColor(attrs[0]));
					if (!theLink.hasLabel()
						&& theLink.hasPosition()
						&& !theLink.isHidden())
						{
							GlobeAnnotation label = theLink.getLabel();
							if (null != label)
							{
								linkLabelLayer.addAnnotation(label);
							}
						}
				}
			}  // end linkSet while
			return true;
		} // end label	
		
		// Merge delete directional&birectional links into one subroutine
		private void deleteDirectionalLinks(SdtNode node1,SdtNode node2,String linkId)
		{
			List<SdtLink> links = node1.getLinksTo(node2);
			SdtLink link = null;
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink>itr = links.iterator();

				while (itr != null && itr.hasNext())
				{					
					SdtLink tmpLink = itr.next();
				
					if (((tmpLink.getLinkID() == null && linkId == null) ||
						(linkId != null && tmpLink.getLinkID() != null && tmpLink.getLinkID().equals(linkId)))					
					&&
						tmpLink.isDirectional())						
					{						
						link = tmpLink;
						Polyline line = link.getLine();
						if (null != line)
						{
							linkLayer.removeRenderable(line);
						}
						GlobeAnnotation theLabel = link.getLabel();
						if (theLabel != null)
						{
							linkLabelLayer.removeAnnotation(theLabel);
							link.removeLabel();
						}
						if (link.getMarker() != null)
						{
							markers.remove(link.getMarker());
						}
					}					
				}

				node1.removeDirectionalLinkTo(node2,linkId,true);
				node2.removeDirectionalLinkTo(node1,linkId,true);

			}			
			
		}
		private void deleteBiDirectionalLinks(SdtNode node1,SdtNode node2,String linkId)
		{
			List<SdtLink> links = node1.getLinksTo(node2);
			SdtLink link = null;
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink>itr = links.iterator();

				while (itr != null && itr.hasNext())
				{					
					SdtLink tmpLink = itr.next();
				
					if (((tmpLink.getLinkID() == null && linkId == null) ||
						(linkId != null && tmpLink.getLinkID() != null && tmpLink.getLinkID().equals(linkId)))					
					&&
					!tmpLink.isDirectional())
					{	
						link = tmpLink;
						Polyline line = link.getLine();
						if (null != line)
						{
							linkLayer.removeRenderable(line);
						}
						GlobeAnnotation theLabel = link.getLabel();
						if (theLabel != null)
						{
							linkLabelLayer.removeAnnotation(theLabel);
							link.removeLabel();
						}
						if (link.getMarker() != null)
						{
							markers.remove(link.getMarker());
						}
					}
				}
				node1.removeDirectionalLinkTo(node2,linkId,false);
				node2.removeDirectionalLinkTo(node1,linkId,false);
			}			
			
		}
		private boolean SetLink(String val)
		{
			// Here we build a currentLinkSet of all links selected in the
			// link command (e.g. link x,y,all) for subsequent attribute
			// changes
			if (0 == val.length()) return false;			
			
			// TODO: account for ipv6 ip addresses (contain :'s)
			SdtNode node1 = null;
			SdtNode node2 = null;
			String linkId = null;
			boolean directional = false;
			boolean allLinks = false;
			boolean allDirections = false;
			String [] attr = null;
			// TODO: support ipv6 
			attr = val.split(",");

			if (attr.length < 2) return false;				
			node1 = nodeTable.get(attr[0]);
			if (null == node1) return false;				
			node2 = nodeTable.get(attr[1]);
			if (null == node2) return false;
			
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
				if(attr.length > 3)
				{
					if (attr[3].equalsIgnoreCase("dir")) directional = true;
					if (attr[3].equalsIgnoreCase("all")) allDirections = true;
				}						
			}
			currentLinkSet.clear();
			
			if (allLinks)
			{	// get all links between the two nodes
				if (node1.getLinksTo(node2) != null)
					currentLinkSet.addAll(node1.getLinksTo(node2));
				if (node2.getLinksTo(node1) != null)
					currentLinkSet.addAll(node2.getLinksTo(node1));
				
				if (allDirections)
					// we have the set of all links in all directions.
					// Wait for subsequent line attribute commands
					return true;  

				List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
				Iterator<SdtLink>itr = currentLinkSet.iterator();
				while (itr != null && itr.hasNext())
				{
					SdtLink tmpLink = itr.next();
					if (directional && tmpLink.isDirectional())
						tmpLinkList.add(tmpLink);	
					if (!directional && !tmpLink.isDirectional())
						tmpLinkList.add(tmpLink);
				}
				// We have the set of all links in the specified 
				// direction.  Wait for subsequent line attribute commands.
				currentLinkSet = tmpLinkList;
				return true;
			} else
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
					Iterator<SdtLink>itr = currentLinkSet.iterator();
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
				deleteDirectionalLinks(node1,node2,linkId);
				deleteDirectionalLinks(node2,node1,linkId);
			}
			else
			{
				// else delete any bidirectional links for the linkId
				deleteBiDirectionalLinks(node1,node2,linkId);// ljt tbd: add directionality to hash table?
			}
			
			// Do we need to create a directional/bidirectional link?
			List<SdtLink> links = node1.getLinksTo(node2);

			SdtLink link = null;
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink>itr = links.iterator();
				while (itr != null && itr.hasNext())
				{					
					SdtLink tmpLink = itr.next();
					if (((tmpLink.getLinkID() == null && linkId == null) ||
						 ((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
						 &&
						 ((directional && tmpLink.isDirectional()) || (!directional && !tmpLink.isDirectional())))
					{
						if (!directional ||
							directional  && tmpLink.getDstNode().getName().equals(node2.getName()))
						{
							link = tmpLink;
							break;
						}
					}
				}					
			}			
			Polyline line = null;			
			if (null == link) {
				link = new SdtLink(node1, node2, linkId);
				link.setDirectional(directional);
				line = link.getLine();
				if (null != line) 
				{
					linkLayer.addRenderable(line);
					if (link.getMarker() != null)
					{
						markers.add(link.getMarker());
						markerLayer.setMarkers(markers);
					}
				}
			}
			currentLinkSet.add(link);
			
			// If attribute 3 is a valid color update "line" 
			// color/thickness for legacy link commands
			if (attr.length > 2 && validateColor(attr[2]))
			{
				if (attr.length > 2)
					link.setColor(getColor(attr[2]));
				if (attr.length > 3) {
					Integer thickness = new Integer(attr[3]);
					if (thickness < 1)
						thickness = 1;					
					link.setWidth(thickness);
				}
			}
			return true;
		}
		private boolean UnlinkList(List<SdtLink> deleteLinkList)
		{
			Iterator<SdtLink>itr = deleteLinkList.iterator();
			SdtNode node1 = null;
			SdtNode node2 = null;
			String linkId = null;
			while (itr != null && itr.hasNext())
			{
            try {
               SdtLink link = itr.next();
               node1 = link.getSrcNode();
               node2 = link.getDstNode();
               linkId = link.getLinkID();
               Polyline line = link.getLine();
               if (null != line)
               {
                  linkLayer.removeRenderable(line);
               }
               GlobeAnnotation theLabel = link.getLabel();
               if (null != theLabel)
               {
                  linkLabelLayer.removeAnnotation(theLabel);
                  link.removeLabel();
               }
               if (link.getMarker() != null)
               {
                  markers.remove(link.getMarker());
               }
               node1.removeLinkTo(node2,linkId);
               node2.removeLinkTo(node1,linkId);
            } catch ( NullPointerException e) {
               // Tried to unlink an already unlinked pair
            }
			}
			
			return true;
		}
		// TODO: make the set selection a common subroutine ljt!!
		private boolean SetUnlink(String val)
		{
			if (0 == val.length()) return false;
			SdtNode node1 = null;
			SdtNode node2 = null;
			String linkId = null;
			boolean directional = false;
			boolean allLinks = false;
			boolean allDirections = false;
			String[] attr = null;
			// TODO: support ipv6
			attr = val.split(",");
			
			if (attr.length < 2) return false;
			node1 = nodeTable.get(attr[0]);
			if (null == node1) return false;		
			node2 = nodeTable.get(attr[1]);
			if (null == node2) return false;			

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
					if (attr[3].equalsIgnoreCase("dir")) directional = true;
					if (attr[3].equalsIgnoreCase("all")) allDirections = true;
				}
			}
			List<SdtLink> deleteLinkList = new ArrayList<SdtLink>();
			if (allLinks)
			{	// get all links between the two nodes
				if (node1.getLinksTo(node2) != null)
					deleteLinkList.addAll(node1.getLinksTo(node2));
				if (node2.getLinksTo(node1) != null)
					deleteLinkList.addAll(node2.getLinksTo(node1));
				
				if (allDirections)
					// we have the set of all links in all directions.
					// Unlink set
					return UnlinkList(deleteLinkList);
				
				List<SdtLink> tmpLinkList = new ArrayList<SdtLink>();
				Iterator<SdtLink>itr = deleteLinkList.iterator();
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
				return UnlinkList(tmpLinkList);
			} else
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
					Iterator<SdtLink>itr = deleteLinkList.iterator();
					while (itr != null && itr.hasNext())
					{
						SdtLink tmpLink = itr.next();
						if ((tmpLink.getLinkID() == null && linkId == null) ||
								 ((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
							tmpLinkList.add(tmpLink);	
					}
					return UnlinkList(tmpLinkList);										
				}
			}
			// Do we need to delete a directional/bidirectional link w/the linkId?
			List<SdtLink> links = node1.getLinksTo(node2);

			SdtLink link = null;
			if (links != null && !links.isEmpty())
			{
				Iterator<SdtLink>itr = links.iterator();
				while (itr != null && itr.hasNext())
				{					
					SdtLink tmpLink = itr.next();
					if (((tmpLink.getLinkID() == null && linkId == null) ||
						 ((linkId != null && tmpLink.getLinkID() != null) && tmpLink.getLinkID().equals(linkId)))
						 &&
						 ((directional && tmpLink.isDirectional()) || (!directional && !tmpLink.isDirectional())))
					{
						if (!directional ||
							directional  && tmpLink.getDstNode().getName().equals(node2.getName()))
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
				return UnlinkList(deleteLinkList);
			}
			// no links !
			return false;
		} // end unlink
	
		private boolean SetPath(String val)
		{
			if (0 == val.length())
				return false; // wait for dir
		
			imagePathPrefix = val;
			if (!imagePathPrefix.endsWith("/"))
				imagePathPrefix = imagePathPrefix + "/";

			return true;
		}

	private boolean SetStatus(String val)
	{
		if ((0 == val.length()))
		{
			this.getStatusPanel().setText("");
		} 
		else 
		{
			String[] attr = val.split(",", -2);
			if (attr[0].equalsIgnoreCase("none"))
			{
				this.getStatusPanel().setText("");
				//	ljt remove all status components not just one
				//this.getStatusPanel().getComponents().
		    	//this.getStatusPanel().removeAll();
			}
			if (attr.length == 2) 
			{
				this.getStatusPanel().setItem(attr[1], attr[0]);
			} 
			else 
			{
				this.getStatusPanel().setText(attr[0]);
			}
		}		
		return true;
	} // end SetStatus
		
	private boolean SetRegion(String regionName)
	{ 	
		if (0 == regionName.length())
			return false;  // no region name

		currentRegion = (SdtRegion) regionTable.get(regionName);

		if (currentRegion == null) 
		{
			currentRegion = new SdtRegion(regionName);
			regionTable.put(regionName, currentRegion);	
			regionLayer.addRegion(currentRegion);
		}
		return true;
	}


	private boolean SetShape(String val)
	{
		if ((0 == val.length()) || currentRegion == null)
			return false;
		
		// TODO: parsing needs error handling!
		String[] attrs = val.split(",");
		int x = attrs.length;
		
		if (attrs.length < 1) return false; // no region type
		
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
		
		for (int ind = 1; ind < x; ind++)
		{
			String useDefault = String.valueOf(attrs[ind]);
			if (useDefault.equalsIgnoreCase("X"))
				continue;
			
			if (ind == 1)
			{
				currentRegion.setColor(getColor(attrs[ind]));						
			}
			if (ind == 2)
			{
				currentRegion.setOutlineWidth(Integer.valueOf(attrs[ind]));
			}
			if (ind == 3)
			{
				currentRegion.setWidth(Double.valueOf(attrs[ind]));
			}
			if (ind == 4)
			{
				currentRegion.setHeight(Double.valueOf(attrs[ind]));
			}
			if (ind == 5)
			{
				currentRegion.setOpacity(Double.valueOf(attrs[ind]));

			}

		}

		currentRegion.setInitialized(false);
		regionLayer.addRegion(currentRegion);
		regionTable.put(currentRegion.getName(), currentRegion);	

		return true;
	}
	public boolean SetTitle(String title)
	{
		if (0 == title.length())
			return false;

		this.setTitle(title);        
		return true;
	}
	public boolean SetDefaultAltitudeType(String val)
	{
		if (0 == val.length())
			return false;

		if (val.equalsIgnoreCase("agl"))
			useAbsoluteElevation = false;
		if (val.equalsIgnoreCase("msl"))
			useAbsoluteElevation = true;
		
		return true;
	}

	public boolean SetPopup(String val)
	{
		if (0 == val.length()) return false;

		JTextArea text = new JTextArea(val);
		text.setWrapStyleWord(true);
		text.setLineWrap(true);		
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(200, 200));
        JOptionPane.showMessageDialog(null, scroll); // with JTextArea
		return true;
	}
	public boolean SetView(String val)
	{
		if (0 == val.length()) return false;		
		String theView = viewTable.get(val);
		
		if (theView != null)
			((OrbitView) getWwd().getView()).restoreState(theView);
		
		return true;
	}
	public boolean SetViewSource(String val)
	{
		if (0 == val.length()) return false;
		currentView = val;
	
		return true;
	}
	public boolean SetViewXml(String val)
	{
		if (0 == val.length() || currentView == null) return false;
		
		String fileName = FindFile(val);
		if (fileName == null) 
		{
			System.out.println("View xml " + val + " not found.");
			return false;		
		}
		String viewState = loadState(val);
		if (viewState == null)
		{
			System.out.println("Loading view state for " + val + " failed.");
			currentView = null;
			return false;
		}

    	JMenuItem newItem;
    	bookmarkMenu.add(newItem = new JMenuItem(currentView));
		newItem.addActionListener(this);
    	viewTable.put(currentView,viewState);		
		currentView = null;
		return true;
	}
	public boolean SetListen(String val)
	{
		if (0 == val.length())
			return false;

		if (val.equalsIgnoreCase("off"))
			if (socketThread != null)
			{
				socketThread.stopThread();
				socketThread = null;
				fileMenu.remove(listenItem);
				listenItem = new JMenuItem("Listen to port... ");
				listenItem.addActionListener(this);
				fileMenu.add(listenItem,2);	
				return true;
			}			
		String[] attrs = val.split(",");
		
		// do we just have a [addr/]port assignment?
		if (attrs.length == 1)
		{	// ljt add these checks elsewhere
			String[] addr = attrs[0].split("/");
			if (addr.length > 1)
			{
				socketMulticastAddr = addr[0];
				try {
				socketPort = new Integer(addr[1]);
				} catch (NumberFormatException e)
				{
					System.out.println("Invalid port assignment");
					return false;					
				}				
			} else
			{  // just the port
				try {
					socketPort = new Integer(attrs[0]);
				} catch (NumberFormatException e)
				{
					System.out.println("Invalid port assignment");
					return false;
				}
			}
		} else 
		{
			if (attrs[0].equalsIgnoreCase("udp"))
			{
				// Were we given an address?
				String[] addr = attrs[1].split("/");
				if (addr.length > 1)
				{
					socketMulticastAddr = addr[0];
					try {
					socketPort = new Integer(addr[1]);
					} catch (NumberFormatException e)
					{
						System.out.println("Invalid port assignment");
						return false;
						
					}
				}
				else // or just a port
				{
					try {
						socketPort = new Integer(attrs[1]);
					} catch (NumberFormatException e)
					{
						System.out.println("Invalid port assignment");
						return false;
				
					}
				
				}
			}
		}
		
		// Stop any existing threads
		if (socketThread != null)
		{
			socketThread.stopThread();
			socketThread = null;
			fileMenu.remove(listenItem);
			listenItem = new JMenuItem("Listen to port... ");
			listenItem.addActionListener(this);
			fileMenu.add(listenItem,2);					
		}	
		
		socketThread = new SocketThread(this,socketPort,socketMulticastAddr);
		socketThread.start();
		fileMenu.remove(listenItem);
		listenItem = new JMenuItem("Off " + socketPort);
		listenItem.addActionListener(this);
		fileMenu.add(listenItem,2);

		return true;
		
		
	}
	public static boolean UseAbsoluteElevation()
	{
		return useAbsoluteElevation;
	}
	public boolean SetInputFile(String val)
	{	
		String fileName = FindFile(val);
		if (fileName == null) return false;
		
		File f1 = new File(fileName);
		if (!f1.exists()) return false;
		
		fileName = f1.getAbsolutePath();
		if (fileThread == null)
		{
			fileThread = new FileThread(this, fileName,pipeCmd);
			fileThread.start();	 // calls the run method for us			
		} else
			if (!fileThread.isRunning())
			{
				fileThread = new FileThread(this,fileName,pipeCmd);
				fileThread.start();
			} else
			{
				// Process input statements from scripts immediately,
				// but pipe input statements serially
				if (pipeCmd)
				{
					fileThread.addLast(fileName);
				}
				else
					fileThread.pushFile(fileName);
			}	
		return true;
	}

		public boolean ProcessCmd(String pendingCmd, String val)
		{	
			//System.out.println("CMD>" + pendingCmd + " " + val);
			if (pendingCmd.equals("bgbounds")) 
				return SetBackgroundBounds(val);
			if (pendingCmd.equals("flyto"))
				return SetFlyTo(val);
			if (pendingCmd.equals("zoom"))
				return SetZoom(val);
			if (pendingCmd.equals("heading"))
				return SetHeading(val);
			if (pendingCmd.equals("pitch"))
				return SetPitch(val);
			if (pendingCmd.equals("tileImage"))
				return SetTileImage(val);
			if (pendingCmd.equals("tile"))
				return SetTile(val);
			if (pendingCmd.equals("sector"))
				return LoadTile(val);
			if (pendingCmd.equals("instance"))
				return SetPipeName(val);
			if (pendingCmd.equals("bgimage"))
				return false; 
			if (pendingCmd.equals("sprite"))
				return SetSprite(val);	
			if (pendingCmd.equals("scale"))
				return SetScale(val);
			if (pendingCmd.equals("image"))
				return SetImage(val);			
			if (pendingCmd.equals("node"))
				return SetNode(val);			
			if (pendingCmd.equals("type"))
				return SetType(val);			
			if (pendingCmd.equals("position") || pendingCmd.equals("pos"))
				return SetPosition(val);
			if (pendingCmd.equals("follow"))
				return SetFollow(val);
			if (pendingCmd.equals("center"))
				return SetRegionPosition(val);
			if (pendingCmd.equals("clear"))
				return Clear(val);	
			if (pendingCmd.equals("delete"))
				return Delete(val);
			if (pendingCmd.equals("size"))
				return SetSize(val);
			if (pendingCmd.equals("length"))
				return SetLength(val);
			if (pendingCmd.equals("light"))
				return SetLight(val);
			if (pendingCmd.equals("label"))
				return SetLabel(val);
			if (pendingCmd.equals("symbol"))
				return SetSymbol(val);
			if (pendingCmd.equals("shape"))
				return SetShape(val);
			if (pendingCmd.equals("link"))
			{
				// Spurious link commands should not be generated as 
				// performance may be impacted due to refreshing links...
				if (SetLink(val)) 
				{ 
					refreshLinks();
					return true;
				}
				return false;
			}
			if (pendingCmd.equals("linklabel"))
				return SetLinkLabel(val);
			if (pendingCmd.equals("unlink"))
				return SetUnlink(val);
			if (pendingCmd.equals("line"))
				return SetLine(val);
			if (pendingCmd.equals("wait"))
				return true; // wait is implemented in PipeThread & FileThread
			if (pendingCmd.equals("path"))
				return SetPath(val);
			if (pendingCmd.equals("status"))
				return SetStatus(val);
			if (pendingCmd.equals("region"))
				return SetRegion(val);
			if (pendingCmd.equals("input"))
				return SetInputFile(val);
			if (pendingCmd.equals("title"))
				return SetTitle(val);
			if (pendingCmd.equals("defaultAltitudeType"))
				return SetDefaultAltitudeType(val);
			if (pendingCmd.equals("listen"))
				return SetListen(val);
			if (pendingCmd.equals("popup"))
				return SetPopup(val);
			if (pendingCmd.equals("view"))
				return SetView(val);
			if (pendingCmd.equals("viewSource"))
				return SetViewSource(val);
			if (pendingCmd.equals("viewXml"))
				return SetViewXml(val);
			return false;
		}  // end ProcessCmd
		

		public boolean OnInput(String str,CmdParser parser) 
		{			
			// So we don't clobber file/pipe state when interleaving 
			// the two command sets.  Certainly there's a better way, but for now...
			currentNode = parser.currentNode;
			currentSprite = parser.currentSprite;
			currentRegion = parser.currentRegion;
			currentSymbol = parser.currentSymbol;
			currentTile = parser.currentTile;
			currentView = parser.currentView;
			// We handle input file processing differently for piped input
			pipeCmd = parser.pipeCmd;
			String cmd = str.concat(" ");
			parser.ParseInput(cmd);

			// So we don't clobbering file/pipe state when interleaving 
			// the two command sets
			parser.currentNode = currentNode;
			parser.currentSprite = currentSprite;
			parser.currentRegion = currentRegion;
			parser.currentSymbol = currentSymbol;
			parser.currentTile = currentTile;
			parser.currentView = currentView;
			// Start the timer that will update the display			
			if (!pollTimer.isRunning())
				pollTimer.start();	
			return true;
		}
	} // end class AppFrame
	
    //**************************************************************//
    //********************  View Controller  ***********************//
    //**************************************************************//

  public static class ViewController
    {
        protected static final double SMOOTHING_FACTOR = 0.99;

        protected boolean enabled = true;
        protected WorldWindow wwd;
        protected ViewAnimator animator;
        protected Iterable<? extends WWIcon > iconIterable;
        protected Iterable<? extends WWModel3D > modelIterable;
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

        public Iterable<? extends WWIcon> getIcons()
        {
            return this.iconIterable;
        }

        public Iterable<? extends WWModel3D> getModels()
        {
            return this.modelIterable;
        }

        public void setIcons(Iterable<? extends WWIcon> icons)
        {
            this.iconIterable = icons;
        }

        public void setModels(Iterable<? extends WWModel3D> models)
        {
        	this.modelIterable = models;
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

            ExtentVisibilitySupport vs = new ExtentVisibilitySupport();
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
            
          //  long timeInMilliseconds = 3000L; // Time in milliseconds you want the animation to take.
            //View view = ...; // How you get the view depends on the context.
          //  OrbitViewInputHandler ovih = (OrbitViewInputHandler) this.wwd.getView().getViewInputHandler();
          //  ovih.addPanToAnimator(centerPos, this.wwd.getView().getHeading(), this.wwd.getView().getPitch(), 0.0, timeInMilliseconds, true);           
            
            
        }

        public void sceneChanged()
        {
            OrbitView view = (OrbitView) wwd.getView();

            if (!this.isEnabled())
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
            // Compute screen extents for WWIcons which have feedback information from their IconRenderer.
            ArrayList<ExtentVisibilitySupport.ScreenExtent> screenExtents =
                new ArrayList<ExtentVisibilitySupport.ScreenExtent>();

            Iterable<? extends WWIcon> icons = this.getIcons();
            if (icons != null)
            {
 
                for (WWIcon icon : icons)
                {
                    if (icon == null || icon.getValue(AVKey.FEEDBACK_ENABLED) == null ||
                        !icon.getValue(AVKey.FEEDBACK_ENABLED).equals(Boolean.TRUE))
                    {
                         continue;
                    }

                    screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
                        (Vec4) icon.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
                        (Rectangle) icon.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
 
                }
            }
            // Compute screen extents for WWIcons which have feedback information from their IconRenderer.
            Iterable<? extends WWModel3D> models = this.getModels();
            if (models != null)
            {
                for (WWModel3D model : models)
                {
                    if (model == null || model.getValue(AVKey.FEEDBACK_ENABLED) == null ||
                        !model.getValue(AVKey.FEEDBACK_ENABLED).equals(Boolean.TRUE))
                    {
                         continue;
                    }

                    screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
                        (Vec4) model.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
                        (Rectangle) model.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
                 }

             }
            if (!screenExtents.isEmpty())
                vs.setScreenExtents(screenExtents);
           
            Iterable<? extends ExtentHolder> extentHolders = this.getExtentHolders();
            if (extentHolders != null)
            {
                Globe globe = wwd.getModel().getGlobe();
                double ve = wwd.getSceneController().getVerticalExaggeration();
                vs.setExtents(ExtentVisibilitySupport.extentsFromExtentHolders(extentHolders, globe, ve));
            }
        }
    } // end ViewController
	
	//**************************************************************//
    //********************  View Animator  *************************//
    //**************************************************************//

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
                public double nextInterpolant()
                {
                    return 1d - smoothing;
                }
            });

            this.view = view;
            this.viewController = viewController;
        }

        public void stop()
        {
            super.stop();
            this.haveTargets = false;
        }

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

	public static void main(String[] args) {
        // Override the scene controller so that we can control the
        // rendering process.  
        Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, SdtBasicSceneController.class.getName());
        // <Property name="gov.nasa.worldwind.avkey.AirspaceGeometryCacheSize" value="100000000"/>
		ApplicationTemplate.start("sdt-3D", AppFrame.class);
	}
}
