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

import java.awt.Rectangle;
import java.util.List;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.ogc.kml.KMLAbstractContainer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.kml.impl.KMLModelPlacemarkImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class SdtKmlLayer extends RenderableLayer
{

	@Override
	protected void doRender(DrawContext dc)
	{
		// Not very efficient but we don't want to change
		// the renderable layer at this point...

		super.doRender(dc, this.getActiveRenderables());
		Iterable<Renderable> renderables = this.getActiveRenderables();
		for (Renderable r : renderables) // check if feedback
		{
			// The kmlController owns our root - we don't have access to the node
			// at this point so get it out of the controller. I'm sure this could
			// be more efficient! Not many accessors are available at present.
			ColladaRoot colladaRoot = getColladaRoot(((KMLController) r).getKmlRoot());

			// Record feedback data for the KML model if feedback is enabled
			if (colladaRoot != null)
			{
				Position pos = colladaRoot.getPosition();
				if (pos == null)
					continue;
				Vec4 modelPoint = null;
				if (pos.getElevation() < dc.getGlobe().getMaxElevation())
					modelPoint = dc.getSurfaceGeometry().getSurfacePoint(pos);
				if (modelPoint == null)
					modelPoint = dc.getGlobe().computePointFromPosition(pos);

				Vec4 screenPoint = dc.getView().project(modelPoint);
				Vec4 modelScale = colladaRoot.getModelScale();
				Rectangle rect = new Rectangle((int) (screenPoint.x), (int) (screenPoint.y),
					(int) (modelScale.x), (int) (modelScale.y));

				this.recordFeedback(dc, ((KMLController) r), modelPoint, rect);
			}
		}
	}


	// When we fix kml handling we'll have access to the collada roots
	// but for now...
	public ColladaRoot getColladaRoot(KMLRoot kmlRoot)
	{
		// Set collada root
		ColladaRoot colladaRoot = null;
		if (kmlRoot != null && kmlRoot.getFeature() != null)
		{
			KMLAbstractFeature kmlAbstractFeature = kmlRoot.getFeature();
			for (KMLAbstractFeature feature : ((KMLAbstractContainer) kmlAbstractFeature).getFeatures())
			{
				if (feature instanceof KMLPlacemark)
				{
					List<KMLRenderable> rs = ((KMLPlacemark) feature).getRenderables();
					if (rs != null)
					{
						for (KMLRenderable r : rs)
						{
							if (r instanceof KMLModelPlacemarkImpl)
							{
								if (((KMLModelPlacemarkImpl) r).getColladaRoot() != null)
								{
									colladaRoot = ((KMLModelPlacemarkImpl) r).getColladaRoot();
								}
							}
						}
					}
				}
			}
		} // end if feature != null
		return colladaRoot;
	}


	// These are duplicate functions from the icon renderer
	/**
	 * Returns true if the ModelRenderer should record feedback about how the specified kmlModel has been processed.
	 *
	 * @param dc the current DrawContext.
	 * @param model the KMLModel to record feedback information for.
	 *
	 * @return true to record feedback; false otherwise.
	 */
	protected boolean isFeedbackEnabled(DrawContext dc, KMLController kml)
	{
		if (dc.isPickingMode())
			return false;

		Boolean b = (Boolean) kml.getValue(AVKey.FEEDBACK_ENABLED);
		return (b != null && b);
	}


	/**
	 * If feedback is enabled for the specified model, this method records feedback about how the specified model has
	 * been processed.
	 *
	 * @param dc the current DrawContext.
	 * @param model the model which the feedback information refers to.
	 * @param modelPoint the model's reference point in model coordinates.
	 * @param screenRect the models's bounding rectangle in screen coordinates.
	 */
	protected void recordFeedback(DrawContext dc, KMLController model, Vec4 modelPoint, Rectangle screenRect)
	{
		if (!this.isFeedbackEnabled(dc, model))
			return;

		this.doRecordFeedback(dc, model, modelPoint, screenRect);
	}


	/**
	 * Records feedback about how the specified WWIcon has been processed.
	 *
	 * @param dc the current DrawContext.
	 * @param icon the icon which the feedback information refers to.
	 * @param modelPoint the icon's reference point in model coordinates.
	 * @param screenRect the icon's bounding rectangle in screen coordinates.
	 */
	@SuppressWarnings({ "UnusedDeclaration" })
	protected void doRecordFeedback(DrawContext dc, KMLController model, Vec4 modelPoint, Rectangle screenRect)
	{
		model.setValue(AVKey.FEEDBACK_REFERENCE_POINT, modelPoint);
		model.setValue(AVKey.FEEDBACK_SCREEN_BOUNDS, screenRect);
	}

}
