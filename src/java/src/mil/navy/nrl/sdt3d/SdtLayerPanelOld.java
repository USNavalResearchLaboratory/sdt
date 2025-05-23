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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;

public class SdtLayerPanelOld extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel layersPanel;

	private JScrollPane layerPane;

	private JPanel westPanel;


	public SdtLayerPanelOld(WorldWindow wwd)
	{
		// Make a panel at a default size.
		super(new BorderLayout());
		this.makePanel(wwd, new Dimension(200, 400));
	}


	public SdtLayerPanelOld(WorldWindow wwd, Dimension size)
	{
		// Make a panel at a specified size.
		super(new BorderLayout());
		this.makePanel(wwd, size);
	}


	private void makePanel(WorldWindow wwd, Dimension size)
	{

		// Make and fillSdt the panel holding the layer titles.
		this.layersPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		this.layersPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), new TitledBorder("Layers")));
		this.fillSdt(wwd);

		// Must put the layer grid in a container to prevent scroll panel from stretching their vertical spacing.
		JPanel dummyPanel = new JPanel(new BorderLayout());
		dummyPanel.add(this.layersPanel, BorderLayout.NORTH);

		// Put the name panel in a scroll bar.
		this.layerPane = new JScrollPane(dummyPanel);
		this.layerPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		if (size != null)
			this.layerPane.setPreferredSize(size);

		// Add the scroll bar and name panel to a titled panel that will resize with the main window.
		westPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		westPanel.setBorder(BorderFactory.createEmptyBorder(0, 9, 9, 9));
		// new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("")));
		westPanel.add(layerPane);
		this.add(westPanel, BorderLayout.CENTER);

		// Layer status update commented out because it causes the panel to change size
		// which in turn forces the WW GL canvas to resize and 'flash'.
		// Timer statusTimer = new Timer(500, new ActionListener()
		// {
		// public void actionPerformed(ActionEvent actionEvent)
		// {
		// updateStatus();
		// }
		// });
		// statusTimer.start();
	}

	private Font defaultFont;

	private Font atMaxFont;


	private void updateStatus()
	{
		for (Component layerItem : this.layersPanel.getComponents())
		{
			if (!(layerItem instanceof JCheckBox))
				continue;

			LayerAction action = (LayerAction) ((JCheckBox) layerItem).getAction();
			if (!(action.layer.isMultiResolution()))
				continue;

			if ((action.layer).isAtMaxResolution())
				layerItem.setFont(this.atMaxFont);
			else
				layerItem.setFont(this.defaultFont);
		}
	}


	private void fillSdt(WorldWindow wwd)
	{
		// fillAll the layers panel with the titles of all layers in the world window's current model.
		for (Layer layer : wwd.getModel().getLayers())
		{
			if (layer.getName().contains("World Map"))
				layer.setEnabled(false);
			if (layer.getName().contains("Network Links") ||
				layer.getName().contains("Link Labels") ||
				layer.getName().contains("Node Icons") ||
				layer.getName().contains("Node Models") ||
				layer.getName().contains("Node Labels") ||
				layer.getName().contains("Node Symbols") ||
				layer.getName().contains("Images") ||
				layer.getName().contains("Regions"))
			{
				if (layer.getName().equals("Link Labels"))
					layer.setEnabled(false);
				LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
				JCheckBox jcb = new JCheckBox(action);
				jcb.setSelected(action.selected);
				this.layersPanel.add(jcb);

				if (defaultFont == null)
				{
					this.defaultFont = jcb.getFont();
					this.atMaxFont = this.defaultFont.deriveFont(Font.ITALIC);
				}
			}

		}
	}


	private void fillAll(WorldWindow wwd)
	{
		// fillAll the layers panel with the titles of all layers in the world window's current model.
		for (Layer layer : wwd.getModel().getLayers())
		{
			LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
			JCheckBox jcb = new JCheckBox(action);
			jcb.setSelected(action.selected);
			this.layersPanel.add(jcb);

			if (defaultFont == null)
			{
				this.defaultFont = jcb.getFont();
				this.atMaxFont = this.defaultFont.deriveFont(Font.ITALIC);
			}

		}
	}


	public void update(WorldWindow wwd, String selection)
	{
		// Replace all the layer names in the layers panel with the names of the current layers.
		this.layersPanel.removeAll();
		if (selection.equals("all"))
			this.fillAll(wwd);
		else if (selection.equals("sdt"))
			this.fillSdt(wwd);
		this.westPanel.revalidate();
		this.westPanel.repaint();
	}


	@Override
	public void setToolTipText(String string)
	{
		this.layerPane.setToolTipText(string);
	}

	private static class LayerAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		WorldWindow wwd;

		private Layer layer;

		private boolean selected;


		public LayerAction(Layer layer, WorldWindow wwd, boolean selected)
		{
			super(layer.getName());
			this.wwd = wwd;
			this.layer = layer;
			this.selected = selected;
			this.layer.setEnabled(this.selected);
		}


		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			// Simply enable or disable the layer based on its toggle button.
			if (((JCheckBox) actionEvent.getSource()).isSelected())
				this.layer.setEnabled(true);
			else
				this.layer.setEnabled(false);

			wwd.redraw();
		}
	}
}
