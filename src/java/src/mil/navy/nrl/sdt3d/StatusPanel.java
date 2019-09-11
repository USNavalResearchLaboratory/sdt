package mil.navy.nrl.sdt3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class StatusPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel statusPanel;
    protected JPanel westPanel;
	private JTextArea textArea;
    private Dimension size = new Dimension(200, 50);

	public StatusPanel()
    {
		// Make a panel at a default size.
        super(new BorderLayout());
        this.makePanel(); 
     }
    
	private void makePanel()
    {
        this.statusPanel = new JPanel(new GridLayout(0,1,0,0));
        this.statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); 
        this.statusPanel.setPreferredSize(size);
		this.textArea = new JTextArea();
		this.textArea.setWrapStyleWord(true);
		this.textArea.setLineWrap(true);
		this.textArea.setEditable(false);
		this.textArea.setBackground(this.statusPanel.getBackground());

        this.add(statusPanel, BorderLayout.CENTER);
        
        // Add the status panel to a titled panel that will resize with the main window.
        westPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        westPanel.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Status")));
        westPanel.setToolTipText("Status");
        westPanel.add(statusPanel);
        this.add(westPanel, BorderLayout.CENTER);
           
    }

	public void setText(String aString)
	{
		this.textArea.setText(aString);
		this.statusPanel.add(this.textArea);
	   	this.statusPanel.revalidate();		
		this.statusPanel.repaint();
	}
	    
}
