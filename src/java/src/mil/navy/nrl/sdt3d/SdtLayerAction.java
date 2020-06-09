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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

public class SdtLayerAction extends AbstractAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	WorldWindow wwd;

	private Layer layer;

	private boolean selected;


	public SdtLayerAction(Layer layer, WorldWindow wwd, boolean selected)
	{
		super(layer.getName());
		this.wwd = wwd;
		this.layer = layer;
		this.selected = selected;
		this.layer.setEnabled(this.selected);

	}


	public SdtLayerAction(String layerName, WorldWindow wwd, boolean selected)
	{
		super(layerName);
		this.wwd = wwd;
		this.selected = selected;

		// Load the layer here for now - testing
		LayerList layers = this.wwd.getModel().getLayers();
		layer = layers.getLayerByName(layerName);

		if (layer != null)
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

	}


	public void toggleLayer(Boolean selected)
	{
		// totally testing

		if (this.layer != null)
		{
			this.layer.setEnabled(selected);
		}

	}
}
