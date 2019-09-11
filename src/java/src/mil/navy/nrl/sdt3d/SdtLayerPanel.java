package mil.navy.nrl.sdt3d;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class SdtLayerPanel extends JPanel
{
	private JPanel layersPanel;
	private JScrollPane layerPane;
    private JPanel westPanel;
    

    public SdtLayerPanel(WorldWindow wwd)
    {
        // Make a panel at a default size.
        super(new BorderLayout());
        this.makePanel(wwd, new Dimension(200, 400));
    }

    public SdtLayerPanel(WorldWindow wwd, Dimension size)
    {
        // Make a panel at a specified size.
        super(new BorderLayout());
        this.makePanel(wwd, size);
    }

    private void makePanel(WorldWindow wwd, Dimension size)
    {
                
        // Make and fillSdt the panel holding the layer titles.
        this.layersPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        this.layersPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), new TitledBorder("Layers")));
        this.fillSdt(wwd);
        
        // Must put the layer grid in a container to prevent scroll panel from stretching their vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(this.layersPanel, BorderLayout.NORTH);

        // Put the name panel in a scroll bar.
        this.layerPane = new JScrollPane(dummyPanel);
        this.layerPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (size != null)
            this.layerPane.setPreferredSize(size);
        
        // Add the scroll bar and name panel to a titled panel that will resize with the main window.
        westPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        westPanel.setBorder(BorderFactory.createEmptyBorder(0,9,9,9));
            //new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("")));
        westPanel.add(layerPane);
        this.add(westPanel, BorderLayout.CENTER);  

        // Layer status update commented out because it causes the panel to change size 
        // which in turn forces the WW GL canvas to resize and 'flash'.
//        Timer statusTimer = new Timer(500, new ActionListener()
//        {
//            public void actionPerformed(ActionEvent actionEvent)
//            {
//                updateStatus();
//            }
//        });
//        statusTimer.start();
    }

    private Font defaultFont;
    private Font atMaxFont;

    private void updateStatus()
    {
        for (Component layerItem : this.layersPanel.getComponents())
        {
            if (!(layerItem instanceof JCheckBox))
                continue;

            LayerAction action = (LayerAction) ((JCheckBox) layerItem).getAction();
            if (!(action.layer.isMultiResolution()))
                continue;

            if ((action.layer).isAtMaxResolution())
                layerItem.setFont(this.atMaxFont);
            else
                layerItem.setFont(this.defaultFont);
        }
    }
    
    private void fillSdt(WorldWindow wwd)
    {
    	// fillAll the layers panel with the titles of all layers in the world window's current model.
    	for (Layer layer : wwd.getModel().getLayers())
    	{
    		if(layer.getName().contains("World Map"))
    			layer.setEnabled(false);
    		if(layer.getName().contains("Network Links") || 
   					layer.getName().contains("Link Labels") ||
    				layer.getName().contains("Node Icons") ||
    				layer.getName().contains("Node Models") ||
       				layer.getName().contains("Node Labels") ||
   					layer.getName().contains("Node Symbols") ||
   					layer.getName().contains("Images") ||
   					layer.getName().contains("Regions"))
   			    		{
    			if (layer.getName().equals("Link Labels"))
     				layer.setEnabled(false);
     			LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
    			JCheckBox jcb = new JCheckBox(action);
        		jcb.setSelected(action.selected);
				this.layersPanel.add(jcb);

    			if (defaultFont == null)
    			{
    				this.defaultFont = jcb.getFont();
    				this.atMaxFont = this.defaultFont.deriveFont(Font.ITALIC);
    			}
    		}

        }
    }
    
    private void fillAll(WorldWindow wwd)
    {
        // fillAll the layers panel with the titles of all layers in the world window's current model.
        for (Layer layer : wwd.getModel().getLayers())
        {
            LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
            JCheckBox jcb = new JCheckBox(action);
            jcb.setSelected(action.selected);
            this.layersPanel.add(jcb);

            if (defaultFont == null)
            {
                this.defaultFont = jcb.getFont();
                this.atMaxFont = this.defaultFont.deriveFont(Font.ITALIC);
            }

        }
    }

    public void update(WorldWindow wwd, String selection)
    {
        // Replace all the layer names in the layers panel with the names of the current layers.
        this.layersPanel.removeAll();
        if(selection.equals("all"))
        	this.fillAll(wwd);
        else if(selection.equals("sdt"))
        	this.fillSdt(wwd);
        this.westPanel.revalidate();
        this.westPanel.repaint();
    }

    @Override
    public void setToolTipText(String string)
    {
        this.layerPane.setToolTipText(string);
    }

    private static class LayerAction extends AbstractAction
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		WorldWindow wwd;
        private Layer layer;
        private boolean selected;

        public LayerAction(Layer layer, WorldWindow wwd, boolean selected)
        {
            super(layer.getName());
            this.wwd = wwd;
            this.layer = layer;
            this.selected = selected;
            this.layer.setEnabled(this.selected);
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // Simply enable or disable the layer based on its toggle button.
            if (((JCheckBox) actionEvent.getSource()).isSelected())
                this.layer.setEnabled(true);
            else
                this.layer.setEnabled(false);

            wwd.redraw();
        }
    }
}

