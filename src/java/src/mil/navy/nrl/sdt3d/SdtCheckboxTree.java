/*********************************************************************
 *
 * AUTHORIZATION TO USE AND DISTRIBUTE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: 
 *
 * (1) source code distributions retain this paragraph in its entirety, 
 *  
 * (2) distributions including binary code include this paragraph in
 *     its entirety in the documentation or other materials provided 
 *     with the distribution.
 * 
 *      "This product includes software written and developed 
 *       by Code 5520 of the Naval Research Laboratory (NRL)." 
 *         
 *  The name of NRL, the name(s) of NRL  employee(s), or any entity
 *  of the United States Government may not be used to endorse or
 *  promote  products derived from this software, nor does the 
 *  inclusion of the NRL written and developed software  directly or
 *  indirectly suggest NRL or United States  Government endorsement
 *  of this product.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * For more information send email to sdt_info@nrl.navy.mil
 *
 *
 * WWJ code:
 * 
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 ********************************************************************/

package mil.navy.nrl.sdt3d;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

public class SdtCheckboxTree extends JTree
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2524029840989979592L;


	public SdtCheckboxTree(TreeNode root)
	{

		super(root);
		setCellRenderer(new SdtCheckboxCellRenderer(this));
		addMouseListener(new SdtCheckboxTreeMouseListener());
	}

	private class SdtCheckboxTreeMouseListener extends MouseAdapter
	{

		public SdtCheckboxTreeMouseListener()
		{
		}


		@Override
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
				if (rect.contains(e.getX(), e.getY()))
				{
					((SdtCheckboxCellRenderer) getCellRenderer()).mouseEventToggleCheckbox(getPathForRow(selRow), false);
				}
			}
		}
	}

}
