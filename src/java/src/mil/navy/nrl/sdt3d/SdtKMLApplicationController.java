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
