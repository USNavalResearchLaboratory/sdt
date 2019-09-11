package mil.navy.nrl.sdt3d;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class StatusPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel statusPanel;
	private JPanel statusPane;
	private JLabel textLabel;
	public ArrayList<JPanel> list;	
	
	public StatusPanel()
    {
		// Make a panel at a default size.
        super(new BorderLayout());
        list = new ArrayList<JPanel>();
        this.makePanel();
    }
    
	private void makePanel()
    {
    	this.textLabel = new JLabel();
		//Create Status Panel and Pane
        this.statusPanel = new JPanel(new GridLayout(0,1,0,0));
        this.statusPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), new TitledBorder("Status")));
        
        this.statusPane = new JPanel(new BorderLayout());
        this.statusPane.add(statusPanel);
        this.statusPane.setBorder(BorderFactory.createEmptyBorder(9,9,0,9));
        this.statusPanel.add(textLabel);
        this.add(statusPane);
    }
    
    private JPanel getPanel(String aName)
    {
    	for(int i =0;i<list.size();i++)
    	{
    		String title = ((TitledBorder)((CompoundBorder)list.get(i).getBorder()).getInsideBorder()).getTitle();
    		if(title.compareTo(aName) == 0)
    			return list.get(i);
    	}
    	return null;
    }
	
	public void setItem(String aItem, String contents)
    {
		JPanel aPanel;
		JLabel aLabel = new JLabel(contents);
		aPanel = getPanel(aItem);
		if (aPanel == null)
    	{
    		aPanel = new JPanel(new GridLayout(0,1,0,0));
			aPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0,0,0,0), new TitledBorder(aItem)));
    		aPanel.add(aLabel);
    		list.add(aPanel);
    	}
    	else
    	{
    		int index = list.indexOf(aPanel);
    		aPanel.removeAll();
    		aPanel.add(aLabel);
    		list.remove(index);
    		if(contents.compareTo("") != 0)
    			list.add(index,aPanel);
    	}
		update();
    }
	
	public void setText(String aString)
	{
		textLabel.setText(aString);
		update();
	}

    private void update()
    {
    	this.statusPanel.removeAll();
    	if(textLabel.getText().compareTo("") != 0)
    	this.statusPanel.add(textLabel);
    	for(int i =0;i<list.size();i++)
    	{
    		this.statusPanel.add(list.get(i));
    	}
    	this.statusPanel.revalidate();
    	this.statusPanel.repaint();
    }
	    
}
