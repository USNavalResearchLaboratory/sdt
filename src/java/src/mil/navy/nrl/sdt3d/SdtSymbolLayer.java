package mil.navy.nrl.sdt3d;

import java.util.Iterator;
import java.util.Vector;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;

public class SdtSymbolLayer extends RenderableLayer
{
	private Vector<SdtCone> list;

	private AirspaceLayer airspaceLayer;


	/** Creates a new instance of SymbolLayer */
	public SdtSymbolLayer()
	{
		list = new Vector<SdtCone>();
		airspaceLayer = new AirspaceLayer();
	}


	public void addSymbol(SdtSymbol symbol)
	{
		if (symbol instanceof SdtCone)
		{
			if (!list.contains(symbol))
				list.add((SdtCone) symbol);
		}
		else
			airspaceLayer.addAirspace(symbol.getAirspace());
	}


	public void removeSymbol(SdtSymbol symbol)
	{
		if (symbol instanceof SdtCone && list.contains(symbol))
			list.remove(symbol);
		else if (symbol.getAirspace() != null)
			airspaceLayer.removeAirspace(symbol.getAirspace());
	}


	public void removeRenderables(SdtSymbol symbol)
	{
		if (symbol instanceof SdtCone && list.contains(symbol))
			list.remove(symbol);
		else if (symbol.getAirspace() != null)
			airspaceLayer.removeAirspace(symbol.getAirspace());
	}


	void addRenderable(SdtSymbol symbol)
	{
		if (symbol.getAirspace() != null)
			airspaceLayer.addAirspace(symbol.getAirspace());
		else if (symbol instanceof SdtCone && !list.contains(symbol))
			list.add((SdtCone) symbol);
	}


	@Override
	protected void doRender(DrawContext dc)
	{

		Iterator<SdtCone> it = list.iterator();
		try
		{
			while (it.hasNext())
			{
				SdtSymbol symbol = it.next();
				((SdtCone) symbol).render(dc);
			}
		}
		// handle any exceptions
		catch (Exception e)
		{
			// handle
			e.printStackTrace();
		}

		airspaceLayer.render(dc);

	} // doRender

}