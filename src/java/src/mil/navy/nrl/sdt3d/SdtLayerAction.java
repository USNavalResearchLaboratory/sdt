package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;


public class SdtLayerAction extends AbstractAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	WorldWindow wwd;
    private Layer layer;
    private boolean selected;
 
    public SdtLayerAction(Layer layer, WorldWindow wwd, boolean selected)
    {
        super(layer.getName());
        this.wwd = wwd;
        this.layer = layer;
        this.selected = selected;
        this.layer.setEnabled(this.selected);

    }
    public SdtLayerAction(String layerName, WorldWindow wwd, boolean selected)
    {
        super(layerName);
        this.wwd = wwd;
        this.selected = selected;
        
        // Load the layer here for now - testing
        LayerList layers = this.wwd.getModel().getLayers();
        layer = layers.getLayerByName(layerName);

        if (layer != null)	
        	this.layer.setEnabled(this.selected);
        
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        // Simply enable or disable the layer based on its toggle button.
        if (((JCheckBox) actionEvent.getSource()).isSelected())
            this.layer.setEnabled(true);
        else
            this.layer.setEnabled(false);
        // ljt need this?  isnt' the checkbox is also triggering redrawing?
       // ljt 032312 wwd.redraw();
    }
    public void toggleLayer(Boolean selected)
    {
    	// totally testing
    	
    	if (this.layer != null)	
    	{
    		this.layer.setEnabled(selected);
    	}

    }
}

