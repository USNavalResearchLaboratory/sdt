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
import java.io.FileFilter;
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
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.tuple.Pair;



/**
 * Dialog to prompt for console debugging or file debugging
 * 
 * @author thompson
 * @since Jun 17, 2020
 */
public class ScenarioSettingsDialog
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
			
	private JButton browserButton;
	
	private JButton okButton;
		
	private JTextField fileNameField;

	private boolean okPressed = false;
	
	private String scenarioFileName;
	
	public ScenarioSettingsDialog(
			JFrame mainWindow)
	{
		this.mainWindow = mainWindow;
	}


	public Optional<String> show(String fileName)
	{
		JPanel messagePanel = new JPanel();

		scenarioFileName = fileName;
		
		fileNameField = new JTextField(scenarioFileName);
		fileNameField.setEnabled(true);
		
		okButton = new JButton("OK");
		okButton.setEnabled(true);
		okButton.setMnemonic('O');
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic('C');
		cancelButton.setEnabled(true);
				
		browserButton = new JButton("File Browser");
		browserButton.setEnabled(true);
		browserButton.setMinimumSize(browserButton.getPreferredSize());

		
		Component contentPanel = layOut(
			messagePanel,
			fileNameField,
			browserButton,
			cancelButton);

		JDialog dialog = new JDialog(mainWindow, true);

		dialog.setTitle("Scenario Name");
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
		
		return Optional.of(fileNameField.getText());
	}

	

	private void addListeners(
			JDialog dialog,
			JPanel messagePanel,
			JButton cancelButton)
	{
		browserButton.addActionListener( e-> {
			
				 
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setSelectedFile(new File(scenarioFileName));
						
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Scenario File", "cmdMap");
			jFileChooser.setFileFilter(filter);
		
			int returnVal = jFileChooser.showOpenDialog(mainWindow); //showSaveDialog(mainWindow);
					
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File debugFile = jFileChooser.getSelectedFile();
				String fileName = debugFile.getAbsolutePath();
				fileNameField.setText(fileName);
				okButton.setEnabled(true);
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
			JTextField fileNameField,
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
		//c00.fill = GridBagConstraints.HORIZONTAL;
		c00.insets.left = LABEL_RIGHT_INSET;
		c00.gridx = 0;
		c00.gridy = 0;
		contentPanel.add(changeFileButton, c00);

		GridBagConstraints c10 = new GridBagConstraints();
		//c01.fill = GridBagConstraints.HORIZONTAL;
		c10.insets.left = LABEL_RIGHT_INSET;
		c10.gridx = 1;
		c10.gridy = 0;
		c10.weightx = 1;
		contentPanel.add(fileNameField,c10);
		
		GridBagConstraints c02 = new GridBagConstraints();
		c02.anchor = GridBagConstraints.LINE_END;
		c02.fill = GridBagConstraints.HORIZONTAL;
		c02.gridx = 0;
		c02.gridy = 2;
		c02.gridwidth = 2;
		c02.insets.top = FIELD_TOP_INSET;
		c02.weightx = 1;
		contentPanel.add(createBottomPanel(messagePanel, cancelButton), c02);

		JPanel emptyPanel = new JPanel();
		emptyPanel.setPreferredSize(new Dimension(0, 0));

		GridBagConstraints c03 = new GridBagConstraints();
		c03.fill = GridBagConstraints.BOTH;
		c03.gridx = 0;
		c03.gridy = 5;
		c03.gridwidth = 3;
		c03.weightx = 1;
		c03.weighty = 1;
		contentPanel.add(emptyPanel, c03);

		return contentPanel;
	}
}