package mil.navy.nrl.sdt3d;

import java.util.Iterator;
import java.util.Vector;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.util.Logging;

public class SdtSymbolLayer extends RenderableLayer
{
	private Vector<SdtSymbol> symbolList;

	private AirspaceLayer airspaceLayer;


	/** Creates a new instance of SymbolLayer */
	public SdtSymbolLayer()
	{
		symbolList = new Vector<SdtSymbol>();
		airspaceLayer = new AirspaceLayer();
	}


	public void addSymbol(SdtSymbol symbol)
	{
		if (!symbolList.contains(symbol))
		{
			symbolList.add(symbol);
		}
		
		if (!(symbol instanceof SdtCone))
		{
			airspaceLayer.addAirspace(symbol.getAirspace());
		}
	}


	public void removeSymbol(SdtSymbol symbol)
	{
		if (symbolList.contains(symbol))
		{
			symbolList.remove(symbol);
		}

		if (symbol.getAirspace() != null)
		{
			airspaceLayer.removeAirspace(symbol.getAirspace());
		}
	}


	/**
	 * We are overriding the SdtSymbolLayer's doRender method.
	 * (SdtSymbolLayer is a RenderableLayer) and then calling
	 * AirspaceLayer's doRender method to actually render
	 * the airspaces after we reset the various airspace attributes
	 * (asl, msl, iconhugging etc).
	 * 
	 * TODO: AirspaceLayer is now deprecated and needs to be replaced
	 * by a RenderableLayer.  Probably will simple to do if
	 * Airspaces if the RenderableLayer can now render Airspaces.
	 */
	@Override
	protected void doRender(DrawContext dc)
	{		
		// For now (pre rewrite) call symbol.updatePosition (so symbols are 
		// rendered according to zoom level during the airspace layer's render 
		// pass (and not just when the node position changes).
		
		// We completly rewrote rendering for SdtCones (to handle elevation
		// azimuth etc?) so call that render function directly here. 
		Iterator<SdtSymbol> it = symbolList.iterator();
		try
		{
			while (it.hasNext()) 
			{
				SdtSymbol symbol = it.next();
				if (symbol.getSymbolType() == SdtSymbol.Type.CONE)
				{
					((SdtCone) symbol).render(dc);
				}
				else
				{
					symbol.updatePosition(dc);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		airspaceLayer.render(dc);

	} // doRender

}