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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.lang3.tuple.Pair;


/**
 * Dialog to prompt for console debugging or file debugging
 * 
 * @author thompson
 * @since Jun 17, 2020
 */
public class SdtLogDebugDialog
{
	final private JFrame mainWindow;

	/**
	 * This is the amount of space that should be placed around the outside of
	 * any border
	 */
	public static final int PANEL_INSETS = 12;

	/**
	 * This is the amount of space that should be placed above all fields
	 */
	public static final int FIELD_TOP_INSET = 6;

	/**
	 * This is the amount of space that should be placed to the right of a label
	 */
	public static final int LABEL_RIGHT_INSET = 6;

	
	public static enum DebugType {
			OFF
			{
				@Override
				public String toString()
				{
					return "Off";
				}
			},
			CONSOLE
			{
				@Override
				public String toString()
				{
					return "Console";
				}
			},
			FILE
			{
				@Override
				public String toString()
				{
					return "File";
				}
			};
	}

	private DebugType debugType;

	private JRadioButton consoleButton;
	
	private JRadioButton fileButton;
	
	private JRadioButton debugOffButton;
	
	private ButtonGroup buttonGroup;
	
	private JButton browserButton;
	
	private JButton okButton;
		
	private JTextField fileNameField;

	private boolean okPressed = false;

	
	
	public SdtLogDebugDialog(
			JFrame mainWindow)
	{
		this.mainWindow = mainWindow;
	}


	public Optional<Pair<DebugType,String>> show()
	{
		JPanel messagePanel = new JPanel();

		fileNameField = new JTextField("/tmp/sdt3d.debug");
		fileNameField.setEnabled(false);
		
		okButton = new JButton("OK");
		okButton.setEnabled(true);
		okButton.setMnemonic('O');
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic('C');
		cancelButton.setEnabled(true);

		consoleButton = new JRadioButton("Log to console");
		consoleButton.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				radioButtonSelected(true);
			}
		});
		
		fileButton = new JRadioButton("Log to file");
		
		fileButton.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				radioButtonSelected(false);
			}
		});
		
		debugOffButton = new JRadioButton("Turn debug logging off");
		debugOffButton.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				radioButtonSelected(false);
			}
		});
		
		browserButton = new JButton("File browser");
		browserButton.setEnabled(false);
		browserButton.setMinimumSize(browserButton.getPreferredSize());

		buttonGroup = new ButtonGroup();
		buttonGroup.add(consoleButton);;
		buttonGroup.add(fileButton);
		buttonGroup.add(debugOffButton);
		debugOffButton.setSelected(true);

		
		Component contentPanel = layOut(
			messagePanel,
			debugOffButton,
			fileNameField,
			consoleButton,
			fileButton,
			browserButton,
			cancelButton);

		JDialog dialog = new JDialog(mainWindow, true);

		dialog.setTitle("Set Debug File");
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
		dialog.setMinimumSize(new Dimension(400, 100));
		dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
		dialog.pack();

		addListeners(
			dialog,
			messagePanel,
			cancelButton);

		dialog.setVisible(true);

		if (!okPressed)
		{
			return Optional.empty();
		}
		
		Pair<DebugType, String> pair = Pair.of(debugType, fileNameField.getText());

		return Optional.of(pair);
	}

	
	private void radioButtonSelected(boolean selected)
	{
		if (consoleButton.isSelected()) 
		{
			fileNameField.setEnabled(false);
			debugType = DebugType.CONSOLE;
			fileNameField.setEnabled(false);
		}
		
		if (fileButton.isSelected()) 
		{
			fileNameField.setEnabled(true);
			debugType = DebugType.FILE;
			browserButton.setEnabled(true);
		}
		if (debugOffButton.isSelected()) {
			fileNameField.setEnabled(false);;
			debugType = DebugType.OFF;
			fileNameField.setEnabled(false);
			
		}
		
	}
	

	private void addListeners(
			JDialog dialog,
			JPanel messagePanel,
			JButton cancelButton)
	{
		browserButton.addActionListener( e-> {
			
			if (fileButton.isSelected()) 
			{
				fileNameField.setEnabled(true);
				JFileChooser jFileChooser = new JFileChooser();
				jFileChooser.setSelectedFile(new File("/tmp/sdt3d.debug"));
				int returnVal = jFileChooser.showSaveDialog(mainWindow);
				
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File debugFile = jFileChooser.getSelectedFile();
					String fileName = debugFile.getAbsolutePath();
					fileNameField.setText(fileName);
					okButton.setEnabled(true);
					debugType = DebugType.FILE;
				}
			}
		});
		
		okButton.addActionListener( e-> {
			okPressed = true;
			dialog.dispose();
		});

		cancelButton.addActionListener(e -> {
			dialog.dispose();
		});
	}


	private JPanel createBottomPanel(JPanel messagePanel, JButton cancelButton)
	{
		messagePanel.setPreferredSize(new Dimension(100, cancelButton.getPreferredSize().height));
		cancelButton.setMinimumSize(cancelButton.getPreferredSize());
		okButton.setPreferredSize(cancelButton.getPreferredSize());
		okButton.setMinimumSize(cancelButton.getPreferredSize());

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c0 = new GridBagConstraints();
		c0.fill = GridBagConstraints.HORIZONTAL;
		c0.gridx = 0;
		c0.gridy = 0;
		c0.weightx = 1;
		panel.add(messagePanel, c0);

		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets.left = LABEL_RIGHT_INSET;
		c1.gridx = 1;
		c1.gridy = 0;
		panel.add(okButton, c1);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets.left = LABEL_RIGHT_INSET;
		c2.gridx = 2;
		c2.gridy = 0;
		panel.add(cancelButton, c2);

		return panel;
	}


	private Component layOut(
			JPanel messagePanel,
			JRadioButton debugOffButton,
			JTextField fileNameField,
			JRadioButton consoleButton,
			JRadioButton fileButton,
			JButton changeFileButton,
			JButton cancelButton)
	{
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(
			PANEL_INSETS,
			PANEL_INSETS,
			PANEL_INSETS,
			PANEL_INSETS));

		GridBagConstraints c00 = new GridBagConstraints();
		c00.anchor = GridBagConstraints.WEST;
		c00.gridx = 0;
		c00.gridy = 0;
		c00.insets.top = PANEL_INSETS;
		contentPanel.add(debugOffButton, c00);

		GridBagConstraints c01 = new GridBagConstraints();
		c01.fill = GridBagConstraints.HORIZONTAL;
		c01.gridwidth = 2;
		c01.gridx = 0;
		c01.gridy = 1;
		c01.insets.top = PANEL_INSETS;
		c01.weightx = 1;
		contentPanel.add(consoleButton, c01);
		
		GridBagConstraints c02 = new GridBagConstraints();
		c02.fill = GridBagConstraints.HORIZONTAL;
		c02.gridwidth = 2;
		c02.gridx = 0;
		c02.gridy = 2;
		c02.insets.top = PANEL_INSETS;
		c02.weightx = 1;
		contentPanel.add(fileButton, c02);

		GridBagConstraints c03 = new GridBagConstraints();
		c03.insets.left = LABEL_RIGHT_INSET;
		c03.fill = GridBagConstraints.EAST;
		c03.gridx = 0;
		c03.gridy = 3;
		contentPanel.add(changeFileButton, c03);

		GridBagConstraints c13 = new GridBagConstraints();
		c13.fill = GridBagConstraints.HORIZONTAL;
		c13.insets.left = LABEL_RIGHT_INSET;
		c13.gridx = 1;
		c13.gridy = 3;
		c13.weightx = 1;
		contentPanel.add(fileNameField,c13);
		
		GridBagConstraints c04 = new GridBagConstraints();
		c04.anchor = GridBagConstraints.LINE_END;
		c04.fill = GridBagConstraints.HORIZONTAL;
		c04.gridx = 0;
		c04.gridy = 4;
		c04.gridwidth = 2;
		c04.insets.top = FIELD_TOP_INSET;
		c04.weightx = 1;
		contentPanel.add(createBottomPanel(messagePanel, cancelButton), c04);

		JPanel emptyPanel = new JPanel();
		emptyPanel.setPreferredSize(new Dimension(0, 0));

		GridBagConstraints c05 = new GridBagConstraints();
		c05.fill = GridBagConstraints.BOTH;
		c05.gridx = 0;
		c05.gridy = 5;
		c05.gridwidth = 3;
		c05.weightx = 1;
		c05.weighty = 1;
		contentPanel.add(emptyPanel, c05);

		return contentPanel;
	}
}
