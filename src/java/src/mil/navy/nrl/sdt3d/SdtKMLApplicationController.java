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

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.gx.GXTour;
import gov.nasa.worldwindx.examples.kml.KMLApplicationController;

public class SdtKMLApplicationController extends KMLApplicationController
{

	public SdtKMLApplicationController(WorldWindow wwd)
	{
		super(wwd);
		// TODO Auto-generated constructor stub
	}


	public void setViewer(boolean copyValues)
	{
		View view = (View) WorldWind.createComponent("gov.nasa.worldwind.view.firstperson.BasicFlyView");
		view.getViewInputHandler();

		if (copyValues)
		{
			View viewToCopy = wwd.getView();

			try
			{
				view.copyViewState(viewToCopy);
				wwd.setView(view);
			}
			catch (IllegalArgumentException iae)
			{
				System.out.println("Canno switch to new view from this position/orientation");
			}
		}
		else
		{
			wwd.setView(view);
		}
	}


	/**
	 * Smoothly moves the <code>WorldWindow</code>'s <code>View</code> to the specified
	 * <code>KMLAbstractFeature</code>.
	 *
	 * @param feature the <code>KMLAbstractFeature</code> to move to.
	 */
	@Override
	protected void moveTo(KMLAbstractFeature feature)
	{
		if (feature instanceof GXTour)
		{
			SdtKMLViewController viewController = new SdtKMLViewController(this.wwd);
			viewController.goTo(feature);
			return;
		}

		super.moveTo(feature);

	}
}
