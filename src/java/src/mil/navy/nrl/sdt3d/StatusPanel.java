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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class StatusPanel extends JPanel
{

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
		this.statusPanel = new JPanel(new GridLayout(0, 1, 0, 0));
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
