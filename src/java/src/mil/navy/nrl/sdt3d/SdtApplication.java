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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.util.StatisticsPanel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;
import gov.nasa.worldwindx.examples.FlatWorldPanel;
import gov.nasa.worldwindx.examples.LayerPanel;
import gov.nasa.worldwindx.examples.util.HighlightController;
import gov.nasa.worldwindx.examples.util.ToolTipController;

/**
 * Provides a base application framework for simple WorldWind examples. Examine other examples in this package to see
 * how it's used.
 *
 * @version $Id: ApplicationTemplate.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SdtApplication
{
	public static class AppPanel extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected WorldWindow wwd;

		protected StatusBar statusBar;

		protected ToolTipController toolTipController;

		protected HighlightController highlightController;


		public AppPanel(WorldWindowGLCanvas shareWith, Model model, Dimension canvasSize, boolean includeStatusBar)
		{
			super(new BorderLayout());

			this.wwd = shareWith != null ? new WorldWindowGLCanvas(shareWith) : new WorldWindowGLCanvas();
			((Component) this.wwd).setPreferredSize(canvasSize);

			// If we are being passed a model, share it in the panel
			if (model != null)
				this.wwd.setModel(model);
			else
				// Create the default model as described in the current worldwind properties.
				this.wwd.setModel((Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME));

			// Setup a select listener for the worldmap click-and-go feature
			this.wwd.addSelectListener(new ClickAndGoSelectListener(this.getWwd(), WorldMapLayer.class));

			this.add((Component) this.wwd, BorderLayout.CENTER);
			if (includeStatusBar)
			{
				this.statusBar = new StatusBar();
				this.add(statusBar, BorderLayout.PAGE_END);
				this.statusBar.setEventSource(wwd);
			}

			// Add controllers to manage highlighting and tool tips.
			this.toolTipController = new ToolTipController(this.getWwd(), AVKey.DISPLAY_NAME, null);
			this.highlightController = new HighlightController(this.getWwd(), SelectEvent.ROLLOVER);
		}


		public WorldWindow getWwd()
		{
			return wwd;
		}


		public StatusBar getStatusBar()
		{
			return statusBar;
		}
	}

	protected static class AppFrame extends JFrame
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private Dimension canvasSize = new Dimension(1000, 800);

		protected AppPanel wwjPanel;

		protected JPanel controlPanel;

		protected LayerPanel layerPanel;

		protected StatisticsPanel statsPanel;


		public AppFrame()
		{
			// Initialize a non shared frame by default
			this.initialize(null, null, true, true, false);
		}


		public AppFrame(Dimension size)
		{
			this.canvasSize = size;
			// Initialize a non shared frame by default
			this.initialize(null, null, true, true, false);
		}


		public AppFrame(WorldWindowGLCanvas shareWith, Model model, boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel)
		{
			this.initialize(shareWith, model, includeStatusBar, includeLayerPanel, includeStatsPanel);
		}


		protected void initialize(WorldWindowGLCanvas shareWith, Model model, boolean includeStatusBar, boolean includeLayerPanel,
				boolean includeStatsPanel)
		{
			// Create the WorldWindow.
			this.wwjPanel = this.createAppPanel(shareWith, model, canvasSize, includeStatusBar);
			this.wwjPanel.setPreferredSize(canvasSize);

			if (shareWith != null)
			{
				this.setTitle(System.getProperty("com.apple.mrj.application.apple.menu.about.name"));
			}
			// Put the pieces together.
			this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
			if (includeLayerPanel)
			{
				this.controlPanel = new JPanel(new BorderLayout(10, 10));
				this.layerPanel = new LayerPanel(this.wwjPanel.getWwd());
				this.controlPanel.add(this.layerPanel, BorderLayout.CENTER);
				this.controlPanel.add(new FlatWorldPanel(this.getWwd()), BorderLayout.NORTH);
				this.getContentPane().add(this.layerPanel, BorderLayout.WEST);
			}

			if (includeStatsPanel || System.getProperty("gov.nasa.worldwind.showStatistics") != null)
			{
				this.statsPanel = new StatisticsPanel(this.wwjPanel.getWwd(), new Dimension(250, canvasSize.height));
				this.getContentPane().add(this.statsPanel, BorderLayout.EAST);
			}

			// Register a rendering exception listener that's notified when exceptions occur during rendering.
			this.wwjPanel.getWwd().addRenderingExceptionListener(new RenderingExceptionListener()
				{
					@Override
					public void exceptionThrown(Throwable t)
					{
						if (t instanceof WWAbsentRequirementException)
						{
							String message = "Computer does not meet minimum graphics requirements.\n";
							message += "Please install up-to-date graphics driver and try again.\n";
							message += "Reason: " + t.getMessage() + "\n";
							message += "This program will end when you press OK.";

							JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program",
								JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
					}
				});

			// Search the layer list for layers that are also select listeners and register them with the World
			// Window. This enables interactive layers to be included without specific knowledge of them here.
			for (Layer layer : this.wwjPanel.getWwd().getModel().getLayers())
			{
				if (layer instanceof SelectListener)
				{
					this.getWwd().addSelectListener((SelectListener) layer);
				}
			}

			this.pack();

			// Center the application on the screen.
			WWUtil.alignComponent(null, this, AVKey.CENTER);
			this.setResizable(true);
		}


		protected AppPanel createAppPanel(WorldWindowGLCanvas shareWith, Model model, Dimension canvasSize, boolean includeStatusBar)
		{
			return new AppPanel(shareWith, model, canvasSize, includeStatusBar);
		}


		public Dimension getCanvasSize()
		{
			return canvasSize;
		}


		public AppPanel getWwjPanel()
		{
			return wwjPanel;
		}


		public WorldWindow getWwd()
		{
			return this.wwjPanel.getWwd();
		}


		public StatusBar getStatusBar()
		{
			return this.wwjPanel.getStatusBar();
		}


		/**
		 * @deprecated Use getControlPanel instead.
		 * @return This application's layer panel.
		 */
		@Deprecated
		public LayerPanel getLayerPanel()
		{
			return this.layerPanel;
		}


		public JPanel getControlPanel()
		{
			return this.controlPanel;
		}


		public StatisticsPanel getStatsPanel()
		{
			return statsPanel;
		}


		public void setToolTipController(ToolTipController controller)
		{
			if (this.wwjPanel.toolTipController != null)
				this.wwjPanel.toolTipController.dispose();

			this.wwjPanel.toolTipController = controller;
		}


		public void setHighlightController(HighlightController controller)
		{
			if (this.wwjPanel.highlightController != null)
				this.wwjPanel.highlightController.dispose();

			this.wwjPanel.highlightController = controller;
		}
	}


	public static void insertBeforeCompass(WorldWindow wwd, Layer layer)
	{
		// Insert the layer into the layer list just before the compass.
		int compassPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers)
		{
			if (l instanceof CompassLayer)
				compassPosition = layers.indexOf(l);
		}
		layers.add(compassPosition, layer);
	}


	public static void insertBeforePlacenames(WorldWindow wwd, Layer layer)
	{
		// Insert the layer into the layer list just before the placenames.
		int compassPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers)
		{
			if (l instanceof PlaceNameLayer)
				compassPosition = layers.indexOf(l);
		}
		layers.add(compassPosition, layer);
	}


	public static void insertAfterPlacenames(WorldWindow wwd, Layer layer)
	{
		// Insert the layer into the layer list just after the placenames.
		int compassPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers)
		{
			if (l instanceof PlaceNameLayer)
				compassPosition = layers.indexOf(l);
		}
		layers.add(compassPosition + 1, layer);
	}


	public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName)
	{
		// Insert the layer into the layer list just before the target layer.
		int targetPosition = 0;
		LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers)
		{
			if (l.getName().indexOf(targetName) != -1)
			{
				targetPosition = layers.indexOf(l);
				break;
			}
		}
		layers.add(targetPosition, layer);
	}

	static
	{
		System.setProperty("java.net.useSystemProxies", "true");
		if (Configuration.isMacOS())
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		}
		else if (Configuration.isWindowsOS())
		{
			System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
		}
	}


	public static AppFrame start(String appName, Class appFrameClass)
	{
		if (Configuration.isMacOS() && appName != null)
		{
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
		}

		try
		{
			final AppFrame frame = (AppFrame) appFrameClass.newInstance();
			frame.setTitle(appName);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			java.awt.EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						frame.setVisible(true);
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
}