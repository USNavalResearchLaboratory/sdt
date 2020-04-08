package builder.mil.nrl.atest.worldwind.jogl;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;

//import mil.nrl.atest.math.GeoOrient;
//import mil.nrl.atest.worldwind.DrawContextWrapper;
//import mil.nrl.atest.worldwind.WorldWindGeoObject;
import builder.mil.nrl.spg.logging.LoggerExt;
//import mil.nrl.spg.scenario.ScenarioElement;
//import mil.nrl.spg.scenario.platform.Platform;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.SphereAirspace;
import gov.nasa.worldwind.util.Logging;
//import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.geometry.Model;
import gov.nasa.worldwind.render.Renderable;

/**
 * @author R.Wathelet, most of the code is from RodgersGB Model3DLayer class see https://joglutils.dev.java.net/
 *         modified by eterna2 modified by R.Wathelet adding the Adjustable
 */
public class Model3D
 implements Renderable //implements WorldWindGeoObject
{
	private final Logger log = LoggerExt.getLogger();

	private final Object modelLock = new Object();

	private Supplier<Model> modelSupplier;

	private Model model;

	private double yaw = 0.0;

	private double roll = 0.0;

	private double pitch = 0.0;

	private Globe globe;

	private double size = 1;
	
	private Position position;

	// ljt
	//private GeoOrient orient;

	private Object userObject;

	private boolean isSelected;

	private SphereAirspace selectionSphere;


	public Model3D(Model model) //, Object userObject, GeoOrient orient)
	{
		this.model = model;
		//this(() -> model); //, userObject, orient);
	}


	/*public Model3D(Supplier<Model> modelSupplier) //, Object userObject, GeoOrient orient)
	{
		//this.orient = orient;
		this.modelSupplier = modelSupplier;
		//this.userObject = userObject;
		this.isSelected = false;
		//this.selectionSphere = new SelectionSphereAirspace();
	}*/


	private Model getModelFromSupplier()
	{
		synchronized (modelLock)
		{
			if (model == null)
			{
				// ljt add model supplier?
				// modelSupplier.get() has the opportunity to create the model
				// in a thread
				model = modelSupplier.get();
				if (model != null)
				{
					model.setUseLighting(false);
					model.setUseTexture(true);
				}
			}
			return model;
		}
	}


	@Override
	public void render(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		// ljt
		//Model m = getModelFromSupplier();
		if (model == null)
		{
			return;
		}
		try
		{
			beginDraw(dc);
			// ljt
			/*if (dc.isPickingMode())
			{
				//m.setRenderPicker(true);
			}
			else
			{
				m.setRenderPicker(false);
			}*/
			draw(dc);

		}
		catch (Exception e)
		{
			log.log(Level.WARNING, "", e);
		}
		finally
		{
			endDraw(dc);

		}
	}

	// from DrawContextWrapper
	// ljt fix all this compute point code
	public Vec4 computePoint(DrawContext dc, Position p, boolean followTerrain)
	{
		//Position p = o.toPosition();
		
		// ljt get this from sdtNode
		boolean isAgl = true;
		if (followTerrain)
		{
			return computePoint(dc, p, isAgl); //p.getAltitude().getAGL());
		}
		// ljt return all this agl followterrain code
		return computePoint(dc, p); //, null);
	}
	
	// From DrawContextWrapper
	public Vec4 computePoint(DrawContext dc, Position p) //, boolean altAGL)
	{
		// ljt
		//if (altAGL != null)
		//{
		//	return dc.computeTerrainPoint(p.getLatitude(), p.getLongitude(), altAGL.toMeters());
		//}
		return dc.computePointFromPosition(p, WorldWind.ABSOLUTE);
	}

	
	public void draw(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		this.globe = dc.getGlobe();

		Position position = Position.fromDegrees(38.824029, -77.019117,200);
//orient.toPosition();
		boolean followTerrain = true; // ljt
		Vec4 loc = dc.computePointFromPosition(position, WorldWind.RELATIVE_TO_GROUND); //ABSOLUTE);

		//Vec4 loc = computePoint(dc, position, followTerrain); 
				//new DrawContextWrapper(dc).computePoint(orient, getPlatform());
		Position locPos = globe.computePositionFromPoint(loc);

		//selectionSphere.setLocation(locPos);
		//selectionSphere.setAltitude(locPos.getAltitude());

		double localSize = this.computeSize(dc, loc);
		localSize = 100; //589126;
		// All models are normalized, which means they are scaled by 1/radius
		// Thus theoretically, their radius should be 1, meaning we can directly set selection
		// sphere radius to 1 * localSize. However, the radius calculations in Bounds
		// might be incorrect, so doubling the radius seems necessary to fully encompass some models
		//selectionSphere.setRadius(localSize * 2);

		if (dc.getView().getFrustumInModelCoordinates().contains(loc))
		{
			dc.getView().pushReferenceCenter(dc, loc);
			gl.glRotated(position.getLongitude().degrees, 0, 1, 0);
			gl.glRotated(-position.getLatitude().degrees, 1, 0, 0);
			gl.glRotated(yaw, 0, 0, 1);
			gl.glRotated(pitch, 1, 0, 0);
			gl.glRotated(roll, 0, 1, 0);
			gl.glScaled(localSize, localSize, localSize);
			DisplayListRenderer.getInstance().render(dc,this.getModel());
			dc.getView().popReferenceCenter(dc);
		}
	}


	// puts opengl in the correct state for this layer
	protected void beginDraw(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glPushMatrix();
	}


	// resets opengl state
	protected void endDraw(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glPopMatrix();
	}


	private double computeSize(DrawContext dc, Vec4 loc)
	{
		if (loc == null)
		{
			System.err.println("Null location when computing size of model");
			return 1;
		}
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double newSize = this.size * dc.getView().computePixelSizeAtDistance(d);
		if (newSize < 2)
		{
			newSize = 2;
		}
		return newSize;
	}


	public void setOrient(Position pos) //GeoOrient orient)
	{
		this.position = pos;
		// ljt
		//this.orient = orient;
		//setYaw(-1 * orient.getCourseAngle().getDegrees());
		//setPitch(orient.getPitchAngle().getDegrees());
		//setRoll(orient.getRollAngle().getDegrees());
	}


	public double getSize()
	{
		return size;
	}


	public void setSize(double size)
	{
		this.size = size;
	}


	public Model getModel()
	{
		// ljt
		//synchronized (modelLock)
		//{
			return model;
		//}
	}


	public void setModelSupplier(Supplier<Model> newModelSupplier)
	{
		synchronized (modelLock)
		{
			modelSupplier = newModelSupplier;
			model = null;
		}
	}


	public double getYaw()
	{
		return yaw;
	}


	public void setYaw(double val)
	{
		this.yaw = val;
	}


	public double getRoll()
	{
		return roll;
	}


	public void setRoll(double val)
	{
		this.roll = val;
	}


	public double getPitch()
	{
		return pitch;
	}


	public void setPitch(double val)
	{
		this.pitch = val;
	}


	public Globe getGlobe()
	{
		return globe;
	}

/* ljt
	@Override
	public Object getUserObject()
	{
		return userObject;
	}


	@Override
	public boolean isSelectable()
	{
		return true;
	}


	@Override
	public boolean isSelected()
	{
		return isSelected;
	}


	@Override
	public void setSelected(boolean selected)
	{
		this.isSelected = selected;
	}


	@Override
	public void shutdown()
	{
		// TODO Auto-generated method stub

	}


	@Override
	public GeoOrient getGeoPosition()
	{
		return orient;
	}


	@Override
	public SphereAirspace getSelectionSphere()
	{
		return this.selectionSphere;
	}
	*/
}
