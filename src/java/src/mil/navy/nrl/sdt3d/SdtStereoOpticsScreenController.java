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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;

public class SdtStereoOpticsScreenController extends SdtBasicSceneController
{

	// We need the need to override the StereoOpticsScreenController functions
	// so that our own draw method is called.

	@Override
	protected void draw(DrawContext dc)
	{
		// Capture the capabilities actually in use.
		if (this.capabilities == null)
		{
			this.capabilities = dc.getGLContext().getGLDrawable().getChosenGLCapabilities();
			this.hardwareStereo = this.capabilities.getStereo();
			this.inStereo = this.isHardwareStereo() ? true : this.isInStereo();
		}

		// If stereo isn't to be applied, just draw and return.
		if (!isInStereo())
		{
			super.draw(dc);
			return;
		}

		// Check if pitch is in correct range (50 - 90 degrees) for current stereo implementation to
		// work correctly (temporary hack)
		View dcView = dc.getView();
		Boolean pitchInRange = (dcView.getPitch().compareTo(Angle.fromDegrees(50)) > 0
			&& dcView.getPitch().compareTo(Angle.POS90) < 0);

		if (AVKey.STEREO_MODE_DEVICE.equals(this.stereoMode) && this.isHardwareStereo() && pitchInRange)
			this.doDrawToStereoDevice(dc);
		else if (AVKey.STEREO_MODE_RED_BLUE.equals(this.stereoMode) && pitchInRange)
			this.doDrawStereoRedBlue(dc);
		else // AVKey.STEREO_MODE_NONE
			this.doDrawStereoNone(dc);
	}


	/**
	 * Implement no stereo ("Mono") while using a stereo device.
	 * <p/>
	 * Note that this method draws the image twice, once to each of the left and right eye buffers, even when stereo is
	 * not in effect. This is to prevent the stereo device from drawing blurred scenes.
	 *
	 * @param dc the current draw context.
	 */
	@Override
	protected void doDrawStereoNone(DrawContext dc)
	{
		// If running on a stereo device but want to draw a normal image, both buffers must be filled or the
		// display will be blurry.

		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		gl.glDrawBuffer(GL2.GL_BACK_LEFT);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		super.draw(dc);

		gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		super.draw(dc);
	}


	/**
	 * Implement stereo using the red-blue anaglyph technique.
	 *
	 * @param dc the current draw context.
	 */
	@Override
	protected void doDrawStereoRedBlue(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
		View dcView = dc.getView();

		// Draw the left eye
		if (this.isSwapEyes())
		{
			if (this.isHardwareStereo())
				gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
			gl.glColorMask(false, true, true, true); // right eye in green/blue
		}
		else
		{
			if (this.isHardwareStereo())
				gl.glDrawBuffer(GL2.GL_BACK_LEFT);
			gl.glColorMask(true, false, false, true); // left eye in red only
		}

		if (this.isHardwareStereo())
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		super.draw(dc);

		// Move the view to the right eye
		Angle viewHeading = dcView.getHeading();
		dcView.setHeading(dcView.getHeading().subtract(this.getFocusAngle()));
		dcView.apply(dc);

		// Draw the right eye frame green and blue only
		try
		{
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
			if (this.isSwapEyes())
			{
				if (this.isHardwareStereo())
					gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
				gl.glColorMask(true, false, false, true); // right eye in red only
			}
			else
			{
				if (this.isHardwareStereo())
					gl.glDrawBuffer(GL2.GL_BACK_LEFT);
				gl.glColorMask(false, true, true, true); // right eye in green/blue
			}

			if (this.isHardwareStereo())
				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			super.draw(dc);
		}
		finally
		{
			// Restore the original view heading
			dcView.setHeading(viewHeading);
			dcView.apply(dc);
			gl.glColorMask(true, true, true, true);
		}
	}


	/**
	 * Implement stereo using the stereo-enabled graphics device. The mode has an effect only if the display device
	 * implements stereo.
	 *
	 * @param dc the current draw context.
	 */
	@Override
	protected void doDrawToStereoDevice(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
		View dcView = dc.getView();

		// Draw the left eye
		if (this.isSwapEyes())
			gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
		else
			gl.glDrawBuffer(GL2.GL_BACK_LEFT);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		super.draw(dc);

		// Move the view to the right eye
		Angle viewHeading = dcView.getHeading();
		dcView.setHeading(dcView.getHeading().subtract(this.getFocusAngle()));
		dcView.apply(dc);

		// Draw the right eye
		try
		{
			if (this.isSwapEyes())
				gl.glDrawBuffer(GL2.GL_BACK_LEFT);
			else
				gl.glDrawBuffer(GL2.GL_BACK_RIGHT);

			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
			super.draw(dc);
		}
		finally
		{
			// Restore the original view heading
			dcView.setHeading(viewHeading);
			dcView.apply(dc);
		}
	}
}
