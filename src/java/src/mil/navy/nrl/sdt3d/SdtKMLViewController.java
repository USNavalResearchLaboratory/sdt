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
import java.awt.event.ActionListener;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.Timer;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.AnimationSupport;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLAbstractView;
import gov.nasa.worldwind.ogc.kml.KMLCamera;
import gov.nasa.worldwind.ogc.kml.KMLLookAt;
import gov.nasa.worldwind.ogc.kml.gx.GXAbstractTourPrimitive;
import gov.nasa.worldwind.ogc.kml.gx.GXFlyTo;
import gov.nasa.worldwind.ogc.kml.gx.GXPlaylist;
import gov.nasa.worldwind.ogc.kml.gx.GXTour;
import gov.nasa.worldwind.ogc.kml.gx.GXWait;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwindx.examples.kml.KMLOrbitViewController;

public class SdtKMLViewController extends KMLOrbitViewController
{

	protected double duration;

	private Timer timer;


	protected SdtKMLViewController(WorldWindow wwd)
	{
		super(wwd);

		// TODO Auto-generated constructor stub
	}


	/**
	 * Override to invoke GxTour
	 * 
	 * @param feature Feature to look at.
	 */
	@Override
	public void goTo(KMLAbstractFeature feature)
	{
		if (feature == null)
		{
			String message = Logging.getMessage("nullValue.FeatureIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (feature instanceof GXTour)
		{
			this.goToGxTour((GXTour) feature);
		}
		else
		{
			super.goTo(feature);
		}

	}


	public void goToGxTour(final GXTour tour)
	{
		GXPlaylist playList = tour.getPlaylist();
		final Deque<GXAbstractTourPrimitive> primitiveStack = new ArrayDeque<GXAbstractTourPrimitive>(playList.getTourPrimitives());

		this.timer = new Timer(100, new ActionListener()
			{

				View view = wwd.getView();


				@Override
				public void actionPerformed(ActionEvent event)
				{
					if (view.isAnimating())
					{
						timer.setDelay(5);
						return;
					}
					if (primitiveStack.isEmpty())
					{
						timer.stop();
						return;
					}

					GXAbstractTourPrimitive primitive = primitiveStack.pop();
					duration = 0;
					if (primitive instanceof GXFlyTo)
					{
						if (((GXFlyTo) primitive).hasField("duration"))
							duration = ((GXFlyTo) primitive).getDuration();
						final KMLAbstractView theView = ((GXFlyTo) primitive).getView();
						goTo(theView);
						
						boolean animate = false;
						if (!animate && duration > 0)
						{
							int wait = 0;
							wait = (int) Math.round(duration * 1000);
							if (wait > 0)
								timer.setDelay(wait);

							return;
						}
					}
					else if (primitive instanceof GXWait)
					{
						int wait = 0;
						if (((GXWait) primitive).hasField("duration"))
							duration = ((GXWait) primitive).getDuration();
						wait = (int) Math.round(duration * 1000);
						if (wait > 0)
							timer.setDelay(wait);
						return;
					}

					timer.restart();

				} // end actionPerformed

			}); // end timer ActionListenerr
		timer.start();
	} // end gotoFlyTo


	// We override KMLViewController's goto method
	@Override
	public void goTo(KMLAbstractView view)
	{
		if (view == null)
		{
			String message = Logging.getMessage("nullValue.ViewIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (view instanceof KMLLookAt)
			this.goTo((KMLLookAt) view);
		else if (view instanceof KMLCamera)
			this.goTo((KMLCamera) view);
		else
			Logging.logger().warning(Logging.getMessage("generic.UnrecognizedView", view));
	}


	/***
	 * Overriding KmlOrbitViewController's goTo method invoked by KMLViewController
	 * so that we can use the KML Duration attribute or just setEyePosition which
	 * on initial testing seems to result in smoother animation. (Smaller durations ~seem~ to
	 * results in less "in and out" when using panToAnimator)
	 */

	@Override
	protected void goTo(KMLLookAt lookAt)
	{
		double latitude = lookAt.getLatitude() != null ? lookAt.getLatitude() : 0.0;
		double longitude = lookAt.getLongitude() != null ? lookAt.getLongitude() : 0.0;
		double altitude = lookAt.getAltitude() != null ? lookAt.getAltitude() : 0.0;
		double heading = lookAt.getHeading() != null ? lookAt.getHeading() : 0.0;
		double tilt = lookAt.getTilt() != null ? lookAt.getTilt() : 0.0;
		double range = lookAt.getRange() != null ? lookAt.getRange() : 0.0;

		// Currently unused
		String altitudeMode = lookAt.getAltitudeMode();

		Position lookAtPosition = Position.fromDegrees(latitude, longitude, altitude);

		long timeToMove = AnimationSupport.getScaledTimeMillisecs(
			this.orbitView.getCenterPosition(), lookAtPosition,
			MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

		long newDuration = 0;
		if (duration > 0)
			newDuration = Math.round(duration * 1000);

		// If the KML provided us a duration use that
		if (newDuration > 0)
			timeToMove = newDuration;

		boolean animate = true;
		BasicOrbitView theView = (BasicOrbitView) wwd.getView();

		if (animate)
		{
			theView.setViewOutOfFocus(true); 
			Position endCenterPosition = Position.fromDegrees(latitude, longitude, altitude);
			theView.stopMovementOnCenter();

			((BasicOrbitView) wwd.getView()).addPanToAnimator(theView.getCenterPosition(), endCenterPosition,
				theView.getHeading(), Angle.fromDegrees(heading),
				theView.getPitch(), Angle.fromDegrees(tilt),
				theView.getZoom(), range, timeToMove, true); // true = endSurfaceOnCenter

		}
		else
		{
			if (altitude == 0)
				altitude = range;

			theView.setEyePosition(Position.fromDegrees(latitude, longitude, altitude));
			theView.setHeading(Angle.fromDegrees(heading));
			theView.setPitch(Angle.fromDegrees(tilt));
			wwd.redraw();
		}

	}


	/**
	 * {@inheritDoc}
	 * Overriding KmlOrbitViewController's goTo method invoked by KMLViewController
	 * so that we can use the KML Duration attribute or just call setEyePosition.
	 * Initial tests indicate setting eye position results in smoother animation.
	 * (Smaller durations ~seem~ to results in less "in and out" when using
	 * panToAnimator)
	 */
	@Override
	protected void goTo(KMLCamera camera)
	{
		double latitude = camera.getLatitude() != null ? camera.getLatitude() : 0.0;
		double longitude = camera.getLongitude() != null ? camera.getLongitude() : 0.0;
		double altitude = camera.getAltitude() != null ? camera.getAltitude() : 0.0;
		double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
		double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0; // pitch
		double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;

		// Roll in WWJ is opposite to KML, so change the sign of roll.
		roll = -roll;

		String altitudeMode = camera.getAltitudeMode();
		Position cameraPosition = Position.fromDegrees(latitude, longitude, altitude);

		BasicOrbitView theView = (BasicOrbitView) wwd.getView();
		theView.setViewOutOfFocus(true); 
		Position endCenterPosition = Position.fromDegrees(latitude, longitude, altitude);

		long timeToMove = AnimationSupport.getScaledTimeMillisecs(
			theView.getEyePosition(), endCenterPosition,
			MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

		long newDuration = 0;
		if (duration > 0)
			newDuration = Math.round(duration * 1000);

		// If the KML provided us a duration use that instead of the default
		if (newDuration > 0)
			timeToMove = newDuration;

		boolean animate = false;
		if (animate)
		{
			theView.stopMovementOnCenter(); // ?? Versus stopAnimations?

			double range = altitude;
			((BasicOrbitView) wwd.getView()).addPanToAnimator(theView.getCenterPosition(), endCenterPosition,
				theView.getHeading(), Angle.fromDegrees(heading),
				theView.getPitch(), Angle.fromDegrees(tilt),
				theView.getZoom(), range, timeToMove, true); // true = endSurfaceOnCenter
		}
		else
		{
			theView.setCenterPosition(Position.fromDegrees(latitude, longitude, altitude));
			theView.setHeading(Angle.fromDegrees(heading));
			theView.setPitch(Angle.fromDegrees(tilt));
			wwd.redraw();
		}

	}
}
