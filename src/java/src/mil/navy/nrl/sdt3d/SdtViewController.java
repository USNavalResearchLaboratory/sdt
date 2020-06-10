package mil.navy.nrl.sdt3d;

import java.awt.Rectangle;
import java.util.ArrayList;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.ExtentHolder;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport;

// **************************************************************//
// ******************** View Controller ***********************//
// **************************************************************//

public class SdtViewController
{
	protected static final double SMOOTHING_FACTOR = 0.98;

	protected boolean enabled = true;

	protected WorldWindow wwd;

	protected SdtViewAnimator animator;

	protected Iterable<? extends WWIcon> iconIterable;

	protected Iterable<SdtSprite> modelIterable;

	protected Iterable<? extends ExtentHolder> extentHolderIterable;


	public SdtViewController(WorldWindow wwd)
	{
		this.wwd = wwd;
	}


	public boolean isEnabled()
	{
		return this.enabled;
	}


	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;

		if (this.animator != null)
		{
			this.animator.stop();
			this.animator = null;
		}
	}


	private Iterable<? extends WWIcon> getIcons()
	{
		return this.iconIterable;
	}


	void setIcons(Iterable<? extends WWIcon> icons)
	{
		this.iconIterable = icons;
	}


	private Iterable<? extends SdtSprite> getModels()
	{
		return this.modelIterable;
	}


	void setModels(Iterable<SdtSprite> iterable)
	{
		this.modelIterable = iterable;
	}


	public Iterable<? extends ExtentHolder> getExtentHolders()
	{
		return this.extentHolderIterable;
	}


	public void setExtentHolders(Iterable<? extends ExtentHolder> extentHolders)
	{
		this.extentHolderIterable = extentHolders;
	}


	public boolean isSceneContained(View view)
	{
		ExtentVisibilitySupport vs = new ExtentVisibilitySupport();
		this.addExtents(vs);

		return vs.areExtentsContained(view);
	}


	public Vec4[] computeViewLookAtForScene(View view)
	{
		Globe globe = wwd.getModel().getGlobe();
		double ve = wwd.getSceneController().getVerticalExaggeration();

		gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport vs = new gov.nasa.worldwindx.examples.util.ExtentVisibilitySupport();
		this.addExtents(vs);

		return vs.computeViewLookAtContainingExtents(globe, ve, view);
	}


	public Position computePositionFromPoint(Vec4 point)
	{
		return wwd.getModel().getGlobe().computePositionFromPoint(point);
	}


	public void gotoScene()
	{
		Vec4[] lookAtPoints = this.computeViewLookAtForScene(wwd.getView());
		if (lookAtPoints == null || lookAtPoints.length != 3)
			return;

		Position centerPos = wwd.getModel().getGlobe().computePositionFromPoint(lookAtPoints[1]);
		double zoom = lookAtPoints[0].distanceTo3(lookAtPoints[1]);

		wwd.getView().stopAnimations();
		wwd.getView().goTo(centerPos, zoom);

		// long timeInMilliseconds = 3000L; // Time in milliseconds you want the animation to take.
		// View view = ...; // How you get the view depends on the context.
		// OrbitViewInputHandler ovih = (OrbitViewInputHandler) this.wwd.getView().getViewInputHandler();
		// ovih.addPanToAnimator(centerPos, this.wwd.getView().getHeading(), this.wwd.getView().getPitch(), 0.0,
		// timeInMilliseconds, true);

	}


	public void sceneChanged()
	{
		OrbitView view = (OrbitView) wwd.getView();

		if (!this.isEnabled())
			return;

		// TODO: was bug introduced in wwj 2.1?
		if (view != null)
			if (view.getViewport() != null)
				if (view.getViewport().getWidth() <= 0d)
					return;

		if (this.isSceneContained(view))
			return;

		if (this.animator == null || !this.animator.hasNext())
		{
			this.animator = new SdtViewAnimator(SMOOTHING_FACTOR, view, this);
			this.animator.start();
			view.stopAnimations();
			view.addAnimator(this.animator);
			view.firePropertyChange(AVKey.VIEW, null, view);
		}
	}


	protected void addExtents(ExtentVisibilitySupport vs)
	{
		// Compute screen extents for objects to track which
		// have feedback information from their Renderer.
		ArrayList<ExtentHolder> extentHolders = new ArrayList<ExtentHolder>();
		ArrayList<ExtentVisibilitySupport.ScreenExtent> screenExtents = new ArrayList<ExtentVisibilitySupport.ScreenExtent>();

		Iterable<? extends WWIcon> icons = this.getIcons();
		if (icons != null)
		{

			for (WWIcon o : icons)
			{
				if (o instanceof ExtentHolder)
				{
					extentHolders.add((ExtentHolder) o);

				}
				else if (o instanceof AVList)
				{
					AVList avl = o;
					Object b = avl.getValue(AVKey.FEEDBACK_ENABLED);
					if (b == null || !Boolean.TRUE.equals(b))
						continue;

					if (avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT) != null)
					{
						screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
							(Vec4) avl.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
							(Rectangle) avl.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
					}
				}
			}
		}
		
		// Compute screen extents for WWModels which have feedback 
		// information.
		Iterable<? extends SdtSprite> models = this.getModels();
		if (models != null)
		{
			for (SdtSprite o : models)
			{
				// We handle models differently as they are not AVList instances
				// they should be...
				if (o == null || o.getValue(AVKey.FEEDBACK_ENABLED) == null ||
					!o.getValue(AVKey.FEEDBACK_ENABLED).equals(Boolean.TRUE))
					continue;

				if (o instanceof ExtentHolder)
				{
					extentHolders.add((ExtentHolder) o);

				}
				else
				{
					if (o.getValue(AVKey.FEEDBACK_REFERENCE_POINT) != null)
					{
						screenExtents.add(new ExtentVisibilitySupport.ScreenExtent(
							(Vec4) o.getValue(AVKey.FEEDBACK_REFERENCE_POINT),
							(Rectangle) o.getValue(AVKey.FEEDBACK_SCREEN_BOUNDS)));
					}
				}

			}
		}
		
		if (!extentHolders.isEmpty())
		{
			Globe globe = wwd.getModel().getGlobe();
			double ve = wwd.getSceneController().getVerticalExaggeration();
			vs.setExtents(ExtentVisibilitySupport.extentsFromExtentHolders(extentHolders, globe, ve));
		}
		if (!screenExtents.isEmpty())
		{
			vs.setScreenExtents(screenExtents);
		}

	}
} // end ViewController
