package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.data.ByteBufferRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.LocalElevationModel;
import gov.nasa.worldwind.util.gdal.GDALUtils;
import gov.nasa.worldwindx.examples.dataimport.DataInstallUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.gdal.gdal.Dataset;

public class SdtModelBuilder {
	private static final Logger LOGGER = Logger.getLogger(SdtModelBuilder.class.getName());

	private transient final CompoundElevationModel compElev;
	private transient final RenderableLayer imageLayer;

	private transient final ConcurrentMap<String, Object> models;

	public SdtModelBuilder(final CompoundElevationModel elev, final RenderableLayer imag) {
		this.compElev = elev;
		this.imageLayer = imag;
		this.models = new ConcurrentHashMap<String, Object>();
	}

	public boolean addModel(final String name, final String filepath) {
		final File file = new File(filepath);
		if(file.exists()) {
			final AVList params = new AVListImpl();
			if(DataInstallUtil.isDataRaster(file, params)) {
				System.out.println("Adding " + file);
				return addGeotiff(name, file, params);
			}
		}
		return false;
	}

	public void clear() {
		for(Entry<String, Object> entry: this.models.entrySet()) {
			removeModel(entry.getValue());
			this.models.remove(entry);
		}
	}

	public void removeModel(final String name) {
		final Object model = this.models.remove(name);
		removeModel(model);
	}

	private void removeModel(final Object model) {
		if(model != null) {
			if(model instanceof ElevationModel) {
				final ElevationModel elevation = (ElevationModel) model;
				synchronized(this.compElev) {
					this.compElev.removeElevationModel(elevation);
				}
			} else if(model instanceof Renderable) {
				final Renderable render = (Renderable) model;
				synchronized(this.imageLayer) {
					this.imageLayer.removeRenderable(render);
				}
			}
		}
	}

	private boolean addGeotiff(final String name, final File file, final AVList params) {
		removeModel(name);
		if(AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT))) {
			return addGeotiffElevMap(name, file, params);
		} else if(AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT))) {
			return addGeotiffImage(name, file, params);
		} else {
			return false;
		}
	}

	private boolean addGeotiffElevMap(final String name, final File file, final AVList params) {
		try {
			final LocalElevationModel elevationModel = new LocalElevationModel();
			final Sector sector = (Sector) params.getValue(AVKey.SECTOR);
			final int width = (Integer) params.getValue(AVKey.WIDTH);
			final int height = (Integer) params.getValue(AVKey.HEIGHT);
			final Dataset gdata = GDALUtils.open(file);
			final DataRaster raster = GDALUtils.composeDataRaster(gdata, params);
			final ByteBuffer elevations = ((ByteBufferRaster) raster).getByteBuffer();
			raster.dispose();
			elevationModel.addElevations(elevations, sector, width, height, params);
			synchronized(this.compElev) {
				this.compElev.addElevationModel(elevationModel);
			}
			this.models.put(name, elevationModel);
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
	}

	private boolean addGeotiffImage(final String name, final File file, final AVList params) {
		try {
			final Sector sector = (Sector) params.getValue(AVKey.SECTOR);
			final BufferedImage image = ImageIO.read(file);
			final SurfaceImage simage = new SurfaceImage(image, sector);
			image.flush();
			synchronized(this.imageLayer) {
				this.imageLayer.addRenderable(simage);
			}
			this.models.put(name, simage);
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
			return false;
		}
	}
}
