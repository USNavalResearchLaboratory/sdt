/*
 * Model3DLayer.java
 *
 * Created on February 12, 2008, 10:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;
import javax.media.opengl.GL;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.iModel3DRenderer;

/**
 *
 * @author RodgersGB
 */
public class Model3DLayer extends AbstractLayer {
    private Vector list;
    private boolean maintainConstantSize = false;
    private double size = 1;
    
    /** Creates a new instance of Model3DLayer */
    public Model3DLayer() {
        list = new Vector();
    }

    public void addModel(WWModel3D model) {
        list.add(model);
    }
    
    public void removeModel(WWModel3D model) {
        list.remove(model);
    }
    public Iterable<WWModel3D> getModels()
    {
        return java.util.Collections.unmodifiableCollection(this.list);
    }
  
    protected void doRender(DrawContext dc) {
        try {
            beginDraw(dc);
            Iterator<WWModel3D> it = list.iterator();
            int x = 1;
            while (it.hasNext())
            {
            	WWModel3D theModel = it.next();
                x++;
                draw(dc, theModel);
                // Record feedback data for this WWModel3D if feedback is enabled.
                if (theModel != null)
                {
                	
                    // Determine Cartesian position from the surface geometry if the icon is near the surface,
                    // otherwise draw it from the globe.
                    Position pos = theModel.getSdtNode().getPosition();
                    Vec4 modelPoint = null;
                    if (pos.getElevation() < dc.getGlobe().getMaxElevation())
                        modelPoint = dc.getSurfaceGeometry().getSurfacePoint(theModel.getSdtNode().getPosition());
                    if (modelPoint == null)
                        modelPoint = dc.getGlobe().computePointFromPosition(theModel.getSdtNode().getPosition());

                    //double eyeDistance = icon.isAlwaysOnTop() ? 0 : dc.getView().getEyePoint().distanceTo3(modelPoint);

           /*         if (this.isHorizonClippingEnabled() && eyeDistance > horizon)
                    {
                        // Record feedback data for this WWIcon if feedback is enabled.
                        this.recordFeedback(dc, icon, iconPoint, null);

                        continue; // don't render horizon-clipped icons
                    }
*/
                    // If enabled, eliminate icons outside the view volume. Primarily used to control icon visibility beyond
                    // the view volume's far clipping plane.
                   // if (this.isViewClippingEnabled() && !dc.getView().getFrustumInModelCoordinates().contains(modelPoint))
                    if (!dc.getView().getFrustumInModelCoordinates().contains(modelPoint))
                    {
                        // Record feedback data for this WWIcon if feedback is enabled.
                        this.recordFeedback(dc, theModel, modelPoint, null);

                        continue; // don't render frustum-clipped icons
                    }
               	
                	
                	
                    this.recordFeedback(dc, theModel, modelPoint, null);
                }
               
            }
        }
        // handle any exceptions
        catch (Exception e) {
            // handle
            e.printStackTrace();
        }
        // we must end drawing so that opengl
        // states do not leak through.
        finally {
            endDraw(dc);
        }
    }
    
    // draw this layer
    protected void draw(DrawContext dc, WWModel3D model) {

    	GL gl = dc.getGL();
        Position pos = model.getSdtNode().getPosition();
        Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
        double localSize = this.computeSize(dc, loc);
        localSize *= model.computeSizeScale(dc, loc);
        
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
            dc.getView().pushReferenceCenter(dc, loc);
            gl.glRotated(pos.getLongitude().degrees, 0,1,0);
            gl.glRotated(-pos.getLatitude().degrees, 1,0,0);
            gl.glScaled(localSize, localSize, localSize);
            
           // System.out.println("lsize = " + localSize + ", radius = " + model.getModel().getBounds().getRadius());
            
            gl.glRotated(model.getYaw(), 0,0,1);
            gl.glRotated(model.getPitch(), 1,0,0);
            //gl.glRotated(roll, 0,1,0);
            
            // Get an instance of the display list renderer
            iModel3DRenderer renderer = DisplayListRenderer.getInstance();
            renderer.render(gl, model.getModel());
            dc.getView().popReferenceCenter(dc);
        }
 
     }
 
    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
        GL gl = dc.getGL();
        
        Vec4 cameraPosition = dc.getView().getEyePoint();
        
        gl.glPushAttrib(
            GL.GL_TEXTURE_BIT |
            GL.GL_COLOR_BUFFER_BIT |
            GL.GL_DEPTH_BUFFER_BIT |
            GL.GL_HINT_BIT |
            GL.GL_POLYGON_BIT |
            GL.GL_ENABLE_BIT | 
            GL.GL_CURRENT_BIT | 
            GL.GL_LIGHTING_BIT | 
            GL.GL_TRANSFORM_BIT);

        //float[] lightPosition = {0F, 100000000f, 0f, 0f};
        float[] lightPosition =
            {(float) (cameraPosition.x + 1000), (float) (cameraPosition.y + 1000), (float) (cameraPosition.z + 1000), 1.0f};
        
        /** Ambient light array */
        float[] lightAmbient = {0.4f, 0.4f, 0.4f, 0.4f};
        /** Diffuse light array */
        float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        /** Specular light array */
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        
        float[] model_ambient = {0.5f, 0.5f, 0.5f, 1.0f};
        
        gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);

        gl.glDisable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_LIGHT1);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_NORMALIZE);
        
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
    }
    
    // resets opengl state
    protected void endDraw(DrawContext dc) {
        GL gl = dc.getGL();
        
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        gl.glPopMatrix();
        
        gl.glPopAttrib();
    }
    
    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.maintainConstantSize)
            return size;
        
        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double currentSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (currentSize < 2)
            currentSize = 2;
        
        return currentSize;
    }

    public boolean isConstantSize() {
        return maintainConstantSize;
    }

    public void setMaintainConstantSize(boolean maintainConstantSize) {
        this.maintainConstantSize = maintainConstantSize;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
    // Eventually we should extend WWIcon for WWModel3D...  These are duplicate functions from the renderer
    /**
     * Returns true if the ModelRenderer should record feedback about how the specified WWModel3D has been processed.
     *
     * @param dc   the current DrawContext.
     * @param model the WWModel3D to record feedback information for.
     *
     * @return true to record feedback; false otherwise.
     */
    protected boolean isFeedbackEnabled(DrawContext dc, WWModel3D model)
    {
        if (dc.isPickingMode())
            return false;

        Boolean b = (Boolean) model.getValue(AVKey.FEEDBACK_ENABLED);
        return (b != null && b);
    }

    /**
     * If feedback is enabled for the specified WWModel3D, this method records feedback about how the specified WWModel3D has
     * been processed.
     *
     * @param dc         the current DrawContext.
     * @param model      the model which the feedback information refers to.
     * @param modelPoint the model's reference point in model coordinates.
     * @param screenRect the models's bounding rectangle in screen coordinates.
     */
    protected void recordFeedback(DrawContext dc, WWModel3D model, Vec4 modelPoint, Rectangle screenRect)
    {
        if (!this.isFeedbackEnabled(dc, model))
            return;

        this.doRecordFeedback(dc, model, modelPoint, screenRect);
    }

    /**
     * Records feedback about how the specified WWIcon has been processed.
     *
     * @param dc         the current DrawContext.
     * @param icon       the icon which the feedback information refers to.
     * @param modelPoint the icon's reference point in model coordinates.
     * @param screenRect the icon's bounding rectangle in screen coordinates.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void doRecordFeedback(DrawContext dc, WWModel3D model, Vec4 modelPoint, Rectangle screenRect)
    {
        model.setValue(AVKey.FEEDBACK_REFERENCE_POINT, modelPoint);
        model.setValue(AVKey.FEEDBACK_SCREEN_BOUNDS, screenRect);
    }   
    
}
