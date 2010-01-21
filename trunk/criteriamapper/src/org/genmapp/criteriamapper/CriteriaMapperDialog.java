package org.genmapp.criteriamapper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CytoscapeDesktop;

public class CriteriaMapperDialog extends JDialog implements ActionListener,
		FocusListener, ListSelectionListener, java.beans.PropertyChangeListener {

	// private BooleanCalculator calculator = null;
	// private BooleanScanner scan = null; // Not currently used
	private AttributeManager attributeManager;
	// private ColorMapper colorMapper;
	private CriteriaTablePanel ctPanel;
	// private CriteriaCalculator calculate = new CriteriaCalculator(); // Not
	// currently
	// used

	private String setName = null;

	private JButton applySet;
	private JButton saveSet;
	private JButton closeAll;
	private JButton deleteSet;
	private JButton duplicateSet;
	private JPanel mainPanel;
	private JPanel tablePanel;
	private JPanel setPanel;
	private JPanel controlPanel;

	private boolean deletedFlag = false;

	String mapTo = "Node Color";

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

	public void initialize() {
		mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setMaximumSize(new Dimension(Cytoscape.getDesktop()
				.getWidth(), 150));

		setPanel = getCriteriaSetPanel();

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

	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("nameBoxChanged")) {
			// save current set before switching
			if (null != setName && !deletedFlag) {
				saveSettings(setName);
			}
			// switch to newly selected set
			setName = (String) nameBox.getSelectedItem();

			// check to see if new set name already exists
			boolean setExists = false;
			nameBoxArray = attributeManager.getNamesAttribute(Cytoscape
					.getCurrentNetwork());
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

			if (setExists) {
				// user has selected an existing set
				ctPanel.setName = setName;
				ctPanel.clearTable();
				loadSettings(setName);
			} else { // User has typed a new set name
				// Add new set name and open table
				// attributeManager.addNamesAttribute(Cytoscape
				// .getCurrentNetwork(), setName);
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
			// TODO: apply criteria set
			ctPanel.applyCriteria();
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
			 Object[] options = { "No", "Yes" };
			 int n = JOptionPane.showOptionDialog(this,
			 "Are you sure that you want to delete "+setName+"?",
			 "", JOptionPane.YES_NO_CANCEL_OPTION,
			 JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
			 if (n == 1) { // YES
			attributeManager.removeNamesAttribute(
					Cytoscape.getCurrentNetwork(), setName);
			deletedFlag = true; // to avoid autosave via nameBoxChanged
			nameBox.removeItem(setName);
			ctPanel.clearTable();
			 } else { // NO
			 // do nothing...
			 }
		} else if (command.equals("closeAll")) {
			if (ctPanel.savedFlag) { // saved all changes
				this.setVisible(false);
				return;
			}
			setName = (String) nameBox.getSelectedItem();
			Object[] options = { "Cancel", "No", "Yes" };
			int n = JOptionPane.showOptionDialog(this,
					"Do you want to save this Set before closing?", "",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[2]);
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

		if (sn.equals("New...")) {
			return; // skip saving "New..."
		}
		// System.out.println(nameBox.getSelectedItem());

		String mapTo = (String) mapToBox.getSelectedItem();

		attributeManager.addNamesAttribute(Cytoscape.getCurrentNetwork(), sn);

		String[] criteriaLabels = new String[ctPanel.getDataLength()];

		for (int k = 0; k < criteriaLabels.length; k++) {
			String temp = ctPanel.getCell(k, ctPanel.EXP_COL) + ":"
					+ ctPanel.getCell(k, ctPanel.LABEL_COL) + ":"
					+ ctPanel.getCell(k, ctPanel.VALUE_COL);
			System.out.println("SAVE SETTINGS: " + sn + "  " + temp);
			if (!temp.equals(null)) {
				criteriaLabels[k] = temp;
			}
			// attributeManager.setColorAttribute(label, color, nodeID);
			// System.out.println(criteriaLabels.length+"AAA"+temp);
		}
		attributeManager.setValuesAttribute(sn, mapTo, criteriaLabels);
		ctPanel.savedFlag = true;
	}

	public void loadSettings(String setName) {
		String[] criteria = attributeManager.getValuesAttribute(Cytoscape
				.getCurrentNetwork(), setName);

		ctPanel.clearTable();
		if (criteria.length > 0) {
			mapTo = criteria[0];
		}
		System.out.println("MAP TO: " + mapTo);
		ctPanel.mapToPick = mapTo;

		for (int i = 1; i < criteria.length; i++) {

			String[] temp = criteria[i].split(":");
			if (temp.length != 3) {
				break;
			}
			System.out.println("LOAD SETTINGS: " + setName + " " + temp[0]
					+ " :" + temp[1] + " :" + temp[2]);
			ctPanel.populateList(temp[0], temp[1], ctPanel
					.stringToColor(temp[2]));

		}
		ctPanel.applyCriteria();
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
			initialize();
			setVisible(true);
		}
	}

	private JComboBox nameBox;
	private JComboBox mapToBox;
	private String[] nameBoxArray;

	public JPanel getCriteriaSetPanel() {
		// JPanel setPanel = new JPanel(new BorderLayout(0, 2));

		nameBoxArray = attributeManager.getNamesAttribute(Cytoscape
				.getCurrentNetwork());

		JPanel setPanel = new JPanel();
		BoxLayout box = new BoxLayout(setPanel, BoxLayout.Y_AXIS);
		setPanel.setLayout(box);

		String labelLocation = BorderLayout.LINE_START;
		String fieldLocation = BorderLayout.LINE_END;

		JPanel namePanel = new JPanel(new BorderLayout(0, 2));
		JLabel setLabel = new JLabel("Name");
		// System.out.println(Cytoscape.getCurrentNetwork().getIdentifier());
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

		JPanel mapPanel = new JPanel(new BorderLayout(0, 2));
		JLabel mapLabel = new JLabel("Map To");
		mapToBox = new JComboBox(new String[] { "Node Color",
				"Node Border Color", "None" });
		mapToBox.setActionCommand("mapToBoxChanged");
		mapToBox.addActionListener(this);

		mapPanel.add(mapLabel, labelLocation);
		mapPanel.add(mapToBox, fieldLocation);

		setPanel.add(nPanel);
		// setPanel.add(mapPanel);
		setPanel.add(sPanel);

		return setPanel;
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

	public String[] getAllAttributes(ArrayList<String> attributeList) {
		// Create the list by combining node and edge attributes into a single
		// list

		getAttributesList(attributeList, Cytoscape.getNodeAttributes(), "");
		getAttributesList(attributeList, Cytoscape.getEdgeAttributes(), "");

		String[] str = (String[]) attributeList
				.toArray(new String[attributeList.size()]);
		attributeList.clear();
		return str;

	}

	public void getAttributesList(ArrayList<String> attributeList,
			CyAttributes attributes, String prefix) {
		String[] names = attributes.getAttributeNames();
		ArrayList<String> numericAttributes = new ArrayList<String>();
		// ArrayList<String> stringAttributes = new ArrayList<String>();
		ArrayList<String> booleanAttributes = new ArrayList<String>();
		ArrayList<String> internalAttributes = new ArrayList<String>();
		for (int i = 0; i < names.length; i++) {
			if (attributes.getType(names[i]) == CyAttributes.TYPE_FLOATING
					|| attributes.getType(names[i]) == CyAttributes.TYPE_INTEGER) {
				if (names[i].contains(" ")) {
					names[i].replace(" ", "-");
				}
				if (names[i].contains(":")) {
					internalAttributes.add(names[i]);
				} else {
					numericAttributes.add(names[i]);
				}
			}
			if (attributes.getType(names[i]) == CyAttributes.TYPE_BOOLEAN) {
				if (names[i].contains(" ")) {
					names[i].replace(" ", "-");
				}
				if (names[i].contains(":")) {
					internalAttributes.add(names[i]);
				} else {
					booleanAttributes.add(names[i]);
				}
			}
		}
		// attributeList.add("--Numeric Attributes--");
		for (int j = 0; j < numericAttributes.size(); j++) {
			attributeList.add(numericAttributes.get(j));
		}
		// attributeList.add("--Boolean Attributes--");
		for (int k = 0; k < booleanAttributes.size(); k++) {
			attributeList.add(booleanAttributes.get(k));
		}
		for (int i = 0; i < internalAttributes.size(); i++) {
			// attributeList.add(internalAttributes.get(i));
		}
	}
}
