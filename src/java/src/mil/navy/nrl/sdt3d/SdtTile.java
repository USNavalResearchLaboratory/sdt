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

import java.util.Enumeration;
import java.util.Hashtable;

import gov.nasa.worldwind.render.SurfaceImage;

public class SdtTile
{

	private String tileName;

	private Hashtable<String, SdtCheckboxNode> layerList = new Hashtable<String, SdtCheckboxNode>();

	private SurfaceImage theImage = null;


	public SdtTile(String name)
	{
		this.tileName = name;
	}


	public SurfaceImage getSurfaceImage()
	{
		return theImage;
	}


	public void addSurfaceImage(SurfaceImage theSurfaceImage)
	{
		theImage = theSurfaceImage;
	}


	public void addLayer(String val, SdtCheckboxNode theNode)
	{

		if (!layerList.containsKey(val))
		{
			layerList.put(val, theNode);
		}
	}


	public Hashtable<String, SdtCheckboxNode> getLayerList()
	{
		return layerList;
	}


	public void removeLayer()
	{
		// only in one layer for now
		removeFromCheckbox();
		layerList.clear();
	}


	public boolean alreadyAssigned()
	{
		return !layerList.isEmpty();
	}


	public void removeFromCheckbox()
	{
		// We now only have one layer per link... Could probably clean this up
		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theNode = e.nextElement();
				theNode.removeTile(this);
			}

		}
	}


	public boolean tileInVisibleLayer()
	{

		if (!layerList.isEmpty())
		{
			Enumeration<SdtCheckboxNode> e = layerList.elements();
			while (e.hasMoreElements())
			{
				SdtCheckboxNode theNode = e.nextElement();

				if (theNode.isSelected())
				{
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
