package mil.navy.nrl.sdt3d;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

public class SdtCheckboxTree extends JTree {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2524029840989979592L;
	
	public SdtCheckboxTree(TreeNode root) {

		super(root);
		setCellRenderer(new SdtCheckboxCellRenderer(this));
		addMouseListener(new SdtCheckboxTreeMouseListener());
	}
	private class SdtCheckboxTreeMouseListener extends MouseAdapter 
	{

			public SdtCheckboxTreeMouseListener()
			{				
			}
	        public void mousePressed(MouseEvent e) 
	        {
	        	int selRow = getRowForLocation(e.getX(), e.getY());
	            
	            if (selRow != -1) 
	            {
	            	if (!isRowSelected(selRow))
	                {
	            		setSelectionRow(selRow);
	                }
	            	Rectangle rect = getRowBounds(selRow);
	                if(rect.contains(e.getX(),e.getY())) 
	                {
	                	((SdtCheckboxCellRenderer)getCellRenderer()).mouseEventToggleCheckbox(getPathForRow(selRow),false);
	                }
	            }
	        }
	}		

}
