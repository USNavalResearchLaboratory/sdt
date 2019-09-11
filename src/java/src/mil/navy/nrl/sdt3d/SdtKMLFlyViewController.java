package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.AnimationSupport;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLCamera;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.firstperson.FlyToFlyViewAnimator;
import gov.nasa.worldwind.view.firstperson.FlyViewInputHandler;

public class SdtKMLFlyViewController extends SdtKMLViewController
{

	/** Minimum time for animation, in milliseconds. */
	protected final long MIN_LENGTH_MILLIS = 4000;

	/** Maximum time for animation, in milliseconds. */
	protected final long MAX_LENGTH_MILLIS = 16000;

	/** The view to animate. */
	protected BasicFlyView flyView;


	protected SdtKMLFlyViewController(WorldWindow wwd)
	{
		super(wwd);
		this.flyView = (BasicFlyView) wwd.getView();

		// TODO Auto-generated constructor stub
	}


	/** {@inheritDoc} */
	@Override
	protected void goTo(KMLCamera camera)
	{
		double latitude = camera.getLatitude() != null ? camera.getLatitude() : 0.0;
		double longitude = camera.getLongitude() != null ? camera.getLongitude() : 0.0;
		double altitude = camera.getAltitude() != null ? camera.getAltitude() : 0.0;
		double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
		double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0;
		double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;

		// Roll in WWJ is opposite to KML, so change the sign of roll.
		roll = -roll;

		String altitudeMode = camera.getAltitudeMode();

		Position cameraPosition = Position.fromDegrees(latitude, longitude, altitude);

		long timeToMove = AnimationSupport.getScaledTimeMillisecs(
			this.flyView.getEyePosition(), cameraPosition,
			MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

		Animator animator = FlyToFlyViewAnimator.createFlyToFlyViewAnimator(this.flyView,
			this.flyView.getEyePosition(), cameraPosition,
			this.flyView.getHeading(), Angle.fromDegrees(heading),
			this.flyView.getPitch(), Angle.fromDegrees(tilt),
			this.flyView.getRoll(), Angle.fromDegrees(roll),
			this.flyView.getEyePosition().getElevation(), cameraPosition.getElevation(),
			timeToMove, KMLUtil.convertAltitudeMode(altitudeMode, WorldWind.CLAMP_TO_GROUND));

		FlyViewInputHandler inputHandler = (FlyViewInputHandler) this.flyView.getViewInputHandler();
		inputHandler.stopAnimators();
		inputHandler.addAnimator(animator);
	}

}
