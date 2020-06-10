package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.animation.BasicAnimator;
import gov.nasa.worldwind.animation.Interpolator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.view.orbit.OrbitView;

// **************************************************************//
// ******************** View Animator *************************//
// **************************************************************//

public class SdtViewAnimator extends BasicAnimator
{
	protected static final double LOCATION_EPSILON = 1.0e-9;

	protected static final double ALTITUDE_EPSILON = 0.1;

	protected OrbitView view;

	protected SdtViewController viewController;

	protected boolean haveTargets;

	protected Position centerPosition;

	protected double zoom;


	public SdtViewAnimator(final double smoothing, OrbitView view, SdtViewController viewController)
	{
		super(new Interpolator()
			{
				@Override
				public double nextInterpolant()
				{
					return 1d - smoothing;
				}
			});

		this.view = view;
		this.viewController = viewController;
	}


	@Override
	public void stop()
	{
		super.stop();
		this.haveTargets = false;
	}


	@Override
	protected void setImpl(double interpolant)
	{
		this.updateTargetValues();

		if (!this.haveTargets)
		{
			this.stop();
			return;
		}

		if (this.valuesMeetCriteria(this.centerPosition, this.zoom))
		{
			this.view.setCenterPosition(this.centerPosition);
			this.view.setZoom(this.zoom);
			this.stop();
		}
		else
		{
			Position newCenterPos = Position.interpolateGreatCircle(interpolant, this.view.getCenterPosition(),
				this.centerPosition);
			double newZoom = WWMath.mix(interpolant, this.view.getZoom(), this.zoom);
			this.view.setCenterPosition(newCenterPos);
			this.view.setZoom(newZoom);
		}

		this.view.firePropertyChange(AVKey.VIEW, null, this);
	}


	protected void updateTargetValues()
	{
		if (this.viewController.isSceneContained(this.view))
			return;

		Vec4[] lookAtPoints = this.viewController.computeViewLookAtForScene(this.view);
		if (lookAtPoints == null || lookAtPoints.length != 3)
			return;

		this.centerPosition = this.viewController.computePositionFromPoint(lookAtPoints[1]);
		this.zoom = lookAtPoints[0].distanceTo3(lookAtPoints[1]);
		if (this.zoom < view.getZoom())
			this.zoom = view.getZoom();
		this.haveTargets = true;
	}


	protected boolean valuesMeetCriteria(Position centerPos, double zoom)
	{
		Angle cd = LatLon.greatCircleDistance(this.view.getCenterPosition(), centerPos);
		double ed = Math.abs(this.view.getCenterPosition().getElevation() - centerPos.getElevation());
		double zd = Math.abs(this.view.getZoom() - zoom);

		return cd.degrees < LOCATION_EPSILON
			&& ed < ALTITUDE_EPSILON
			&& zd < ALTITUDE_EPSILON;
	}
} // end ViewAnimator

