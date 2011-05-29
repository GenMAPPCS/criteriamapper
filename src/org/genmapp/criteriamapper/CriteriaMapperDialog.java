/*******************************************************************************
 * Copyright 2010 Alexander Pico
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.genmapp.criteriamapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.view.CytoscapeDesktop;

public class CriteriaMapperDialog extends JDialog
		implements
			ActionListener,
			FocusListener,
			ListSelectionListener,
			java.beans.PropertyChangeListener {

	// private BooleanCalculator calculator = null;
	// private BooleanScanner scan = null; // Not currently used
	private AttributeManager attributeManager;
	// private ColorMapper colorMapper;
	public CriteriaTablePanel ctPanel;
	// private CriteriaCalculator calculate = new CriteriaCalculator(); // Not
	// currently
	// used

	public String setName = null;

	public JButton applySet;
	public JButton saveSet;
	private JButton closeAll;
	public JButton deleteSet;
	public JButton duplicateSet;
	private JPanel mainPanel;
	public JPanel tablePanel;
	private JPanel setPanel;
	public JPanel controlPanel;

	public JComboBox nameBox;
	private String[] nameBoxArray;

	private boolean deletedFlag = false;
	private boolean dupFlag = false;

	String mapTo;

	public CriteriaMapperDialog() {
		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(
				this);
		// add as listener to CytoscapeDesktop
		Cytoscape.getDesktop().getSwingPropertyChangeSupport()
				.addPropertyChangeListener(this);

		// currentAlgorithm = algorithm;
		// colorMapper = new ColorMapper();
		attributeManager = new AttributeManager();
		// calculator = new BooleanCalculator();
		ctPanel = new CriteriaTablePanel();
		// scan = new BooleanScanner();
		initialize();
	}

	public CriteriaMapperDialog getcmDialog() {
		return this;
	}

	public void initialize() {
		mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		setPanel = getCriteriaSetPanel();
		setPanel.setMaximumSize(new Dimension(
				Cytoscape.getDesktop().getWidth(), 150));

		tablePanel = ctPanel.getTablePanel();

		controlPanel = getControlPanel();

		mainPanel.add(setPanel);
		mainPanel.add(tablePanel);
		mainPanel.add(controlPanel);

		controlPanel.setVisible(false);
		tablePanel.setVisible(false);

		setContentPane(mainPanel);
		CytoscapeDesktop d = Cytoscape.getDesktop();
		setLocation(d.getX() + d.getWidth() / 2 - 200, d.getY() + d.getHeight()
				/ 2 - 200);

		this.pack();
		this.setVisible(true);
	}

	public JPanel getControlPanel() {

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

		closeAll = new JButton("Close");
		closeAll.addActionListener(this);
		closeAll.setActionCommand("closeAll");

		// controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(closeAll);

		return controlPanel;
	}

	public JPanel getCriteriaSetPanel() {
		// JPanel setPanel = new JPanel(new BorderLayout(0, 2));

		nameBoxArray = attributeManager.getNamesAttributeForMenu();

		JPanel setPanel = new JPanel();
		BoxLayout box = new BoxLayout(setPanel, BoxLayout.Y_AXIS);
		setPanel.setLayout(box);

		String labelLocation = BorderLayout.LINE_START;
		String fieldLocation = BorderLayout.LINE_END;

		JPanel namePanel = new JPanel(new BorderLayout(0, 2));
		JLabel setLabel = new JLabel("Name");

		nameBox = new JComboBox(nameBoxArray);
		nameBox.setEditable((nameBoxArray.length == 1) ? true : false);
		nameBox.setPreferredSize(new Dimension(240, 20));
		nameBox.setActionCommand("nameBoxChanged");
		nameBox.addActionListener(this);

		namePanel.add(setLabel, labelLocation);
		namePanel.add(nameBox, fieldLocation);

		JPanel nPanel = new JPanel();
		nPanel.add(namePanel);

		JPanel sPanel = new JPanel(new BorderLayout(0, 2));
		JPanel setButtonsPanel = new JPanel();// new BorderLayout(0,2));

		applySet = new JButton("Apply");
		applySet.addActionListener(this);
		applySet.setActionCommand("applySet");
		applySet.setEnabled(false);

		saveSet = new JButton("Save");
		saveSet.addActionListener(this);
		saveSet.setActionCommand("saveSet");
		saveSet.setEnabled(false);

		deleteSet = new JButton("Delete");
		deleteSet.addActionListener(this);
		deleteSet.setActionCommand("deleteSet");
		deleteSet.setEnabled(false);

		duplicateSet = new JButton("Duplicate");
		duplicateSet.addActionListener(this);
		duplicateSet.setActionCommand("duplicateSet");
		duplicateSet.setEnabled(false);

		setButtonsPanel.add(applySet);
		setButtonsPanel.add(saveSet);
		setButtonsPanel.add(deleteSet);
		setButtonsPanel.add(duplicateSet);

		sPanel.add(setButtonsPanel, BorderLayout.CENTER);

		Border refBorder = BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(refBorder,
				"Criteria Set");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		setPanel.setBorder(titleBorder);

		setPanel.add(nPanel);
		setPanel.add(sPanel);

		return setPanel;
	}

	public void actionPerformed(ActionEvent e) {

		if (null == e) { // e.g., when called by CyCommands
			return;
		}

		String command = e.getActionCommand();

		if (command.equals("nameBoxChanged")) {
			// save current set before switching
			String oldName = setName;
			if (null != oldName && !deletedFlag) {
				saveSettings(oldName);
			}
			// switch to newly selected set
			setName = (String) nameBox.getSelectedItem();

			// check to see if new set name already exists
			boolean setExists = false;
			nameBoxArray = attributeManager.getNamesAttributeForMenu();
			for (int i = 0; i < nameBoxArray.length; i++) {
				if (nameBoxArray[i].equals(setName)) {
					setExists = true;
				}
			}

			// user has selected "New..."
			if (setName.equalsIgnoreCase("New...")) {
				tablePanel.setVisible(false);
				controlPanel.setVisible(false);
				applySet.setEnabled(false);
				saveSet.setEnabled(false);
				deleteSet.setEnabled(false);
				duplicateSet.setEnabled(false);
				nameBox.setEditable(true);
				pack();
				return;
			}
			// user is naming a duplicate a set
			if (dupFlag) {
				if (setExists) {
					JOptionPane
							.showMessageDialog(this,
									"Sorry, but that name already exists.\n Please type a new name.");
					return;
				} else {
					dupFlag = false;
					// add new name; keep old criteria
					nameBox.addItem(setName);
					ctPanel.setName = setName;
					ctPanel.clearTable();
					loadSettings(oldName);
					ctPanel.savedFlag = false;
					saveSettings(setName);
				}
			} else if (setExists) {
				// user has selected an existing set
				ctPanel.setName = setName;
				ctPanel.clearTable();
				loadSettings(setName);
			} else { // User has typed a new set name
				// Add new set name and open table
				nameBox.addItem(setName);
				ctPanel.setName = setName;
				ctPanel.clearTable();
				ctPanel.addEditableRow();
				saveSettings(setName);
			}
			tablePanel.setVisible(true);
			controlPanel.setVisible(true);
			applySet.setEnabled(true);
			saveSet.setEnabled(true);
			deleteSet.setEnabled(true);
			duplicateSet.setEnabled(true);
			nameBox.setEditable(false);
			pack();
		} else if (command.equals("applySet")) {
			ctPanel.calcNodeAttributes();
			ctPanel.applyCriteria(Cytoscape.getCurrentNetwork());
			// CriteriaCommandHandler.updateWorkspaces(setName);
		} else if (command.equals("saveSet")) {
			// User is saving Set while editing an existing Set
			setName = (String) nameBox.getSelectedItem();
			saveSettings(setName);
		} else if (command.equals("deleteSet")) {
			// ignore if in edit mode
			if (nameBox.isEditable()) {
				return;
			}
			// prompt user to confirm deletion
			setName = (String) nameBox.getSelectedItem();
			Object[] options = {"No", "Yes"};
			int n = JOptionPane.showOptionDialog(this,
					"Are you sure that you want to delete " + setName + "?",
					"", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 1) { // YES
				attributeManager.removeSetFromProps(setName);
				deletedFlag = true; // to avoid autosave via nameBoxChanged
				nameBox.removeItem(setName);
				ctPanel.clearTable();
				CriteriaCommandHandler.updateWorkspaces(setName);
			} else { // NO
				// do nothing...
			}
		} else if (command.equals("duplicateSet")) {
			dupFlag = true;
			tablePanel.setVisible(false);
			controlPanel.setVisible(false);
			applySet.setEnabled(false);
			saveSet.setEnabled(false);
			deleteSet.setEnabled(false);
			duplicateSet.setEnabled(false);
			nameBox.setEditable(true);
			pack();
			return;
		} else if (command.equals("closeAll")) {
			if (ctPanel.savedFlag) { // changes already saved
				this.setVisible(false);
				return;
			}
			setName = (String) nameBox.getSelectedItem();
			Object[] options = {"Cancel", "No", "Yes"};
			int n = JOptionPane.showOptionDialog(this,
					"Do you want to save this Set before closing?", "",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[2]);
			if (n == 2) { // YES
				saveSettings(setName);
				this.setVisible(false);
				return;
			} else if (n == 1) { // NO
				this.setVisible(false);
				return;
			} else { // CANCEL
				// do nothing...
			}
		}
	}

	public void saveSettings(String sn) {

		if (sn.equals("New...") || ctPanel.savedFlag) {
			System.out.println("Skipped Save!");
			return; // skip saving "New..." or already saved
		}

		// prepare parameters
		String[] criteriaLabels = new String[ctPanel.getDataLength()];

		for (int k = 0; k < criteriaLabels.length; k++) {
			String temp = ctPanel.getCell(k, ctPanel.EXP_COL) + ":"
					+ ctPanel.getCell(k, ctPanel.LABEL_COL) + ":"
					+ ctPanel.getCell(k, ctPanel.COLOR_COL);
			System.out.println("SAVE SETTINGS: " + sn + "  " + temp);
			if (!temp.equals(null))
				criteriaLabels[k] = temp;

		}

		// write to cytoprefs
		attributeManager.setNameAttribute(sn);
		attributeManager.setValuesAttribute(sn, ctPanel.mapToPick,
				criteriaLabels);

		// calculate and apply to networks
		ctPanel.calcNodeAttributes();
		ctPanel.applyCriteria(Cytoscape.getCurrentNetwork());

		// update workspaces
		CriteriaCommandHandler.updateWorkspaces(setName);
		ctPanel.savedFlag = true;
	}

	public void loadSettings(String setName) {
		String[] criteria = attributeManager.getValuesAttribute(setName);

		ctPanel.clearTable();
		if (criteria.length > 0) {
			mapTo = criteria[0];
		}
		// System.out.println("MAP TO: " + mapTo);
		ctPanel.mapToPick = mapTo;

		for (int i = 1; i < criteria.length; i++) {

			String[] temp = criteria[i].split(":");
			if (temp.length != 3) {
				break;
			}
			System.out.println("LOAD SETTINGS: " + setName + " " + temp[0]
					+ " :" + temp[1] + " :" + temp[2]);
			ctPanel.populateList(temp[0], temp[1], Color.decode(temp[2]));

		}
		ctPanel.calcNodeAttributes();
		// ctPanel.applyCriteria(Cytoscape.getCurrentNetwork());
		CriteriaCommandHandler.updateWorkspaces(setName);
		ctPanel.savedFlag = true;
	}

	public void valueChanged(ListSelectionEvent e) {

	}

	public void focusGained(FocusEvent e) {
		System.out.println(e.toString());
	}

	public void focusLost(FocusEvent e) {
		System.out.println(e.toString());
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals("ATTRIBUTES_CHANGED")) {
			//TODO: update attr list
		}
	}

}
