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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**
 * 
 */
public class CriteriaBuilderDialog extends JPanel
		implements
			ActionListener,
			ListSelectionListener {

	public JTextField criteriaField;
	private JList attList;
	private JList opList;
	private CriteriaTablePanel ctPanel;
	private JPanel tablePanel;
	// List which holds all of the attributes we want to display in builder
	private ArrayList<String> attributeList = new ArrayList<String>();
	private String[] opArray = {"=", "<", ">", ">=", "<=", "AND", "OR", "NOT"};
	private String[] attributesArray;

	public CriteriaBuilderDialog(CriteriaTablePanel panel) {

		ctPanel = panel;

	}

	/**
	 * 
	 */
	public void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(getCriteriaChooserPanel());
		this.add(getListPanel());

		tablePanel = ctPanel.getTablePanel();
		tablePanel.add(this);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
	}

	/**
	 * @return
	 */
	private JPanel getListPanel() {
		JPanel bigPanel = new JPanel();

		BoxLayout bigBox = new BoxLayout(bigPanel, BoxLayout.Y_AXIS);
		bigPanel.setLayout(bigBox);

		Border refBorder = BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(refBorder,
				"");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		bigPanel.setBorder(titleBorder);

		// make label panel
		JPanel labelPanel = new JPanel();
		BoxLayout labelBox = new BoxLayout(labelPanel, BoxLayout.X_AXIS);
		labelPanel.setLayout(labelBox);

		JPanel attPanel = new JPanel(new BorderLayout(0, 2));
		JLabel attLabel = new JLabel("      Attributes");
		attPanel.add(attLabel, BorderLayout.LINE_START);
		JPanel opPanel = new JPanel(new BorderLayout(0, 2));
		JLabel opLabel = new JLabel("Operations           ");

		opPanel.add(opLabel, BorderLayout.LINE_END);

		labelPanel.add(attPanel);
		labelPanel.add(opPanel);

		JPanel listPanel = new JPanel();
		BoxLayout listBox = new BoxLayout(listPanel, BoxLayout.X_AXIS);
		listPanel.setLayout(listBox);

		JPanel attListPanel = new JPanel(new BorderLayout(0, 2));
		attributesArray = getAllAttributes();
		attList = new JList();
		attList.setModel(new javax.swing.AbstractListModel() {
			String[] strings = attributesArray;
			public String getName() {
				return "attList";
			}
			public int getSize() {
				// if(strings.length == 8){ return 9; }
				// else{
				return strings.length; // }
			}
			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		ListSelectionModel listSelectModel = attList.getSelectionModel();
		listSelectModel.addListSelectionListener(this);

		attList.setSelectionModel(listSelectModel);

		JScrollPane attpane = new JScrollPane();
		attpane
				.setToolTipText("Click on attribute names to copy them into the expression above");
		attpane.setViewportView(attList);
		attpane.setMinimumSize(new Dimension(125, 100));
		attpane.setPreferredSize(new Dimension(125, 100));
		attpane.setMaximumSize(new Dimension(125, Cytoscape.getDesktop()
				.getHeight()));
		attListPanel.add(attpane, BorderLayout.LINE_START);

		JPanel opListPanel = new JPanel(new BorderLayout(0, 2));
		opList = new JList(opArray);
		ListSelectionModel listSelectionModel = opList.getSelectionModel();
		getOperationSelection opSelection = new getOperationSelection();
		listSelectionModel.addListSelectionListener(opSelection);
		opList.setSelectionModel(listSelectionModel);

		JScrollPane oppane = new JScrollPane();
		oppane
				.setToolTipText("Click on operations to copy them into the expression above");
		oppane.setViewportView(opList);
		oppane.setMinimumSize(new Dimension(125, 100));
		oppane.setPreferredSize(new Dimension(125, 100));
		oppane.setMaximumSize(new Dimension(125, Cytoscape.getDesktop()
				.getHeight()));
		opListPanel.add(oppane, BorderLayout.LINE_START);

		JPanel buttonBox = new JPanel();

		listPanel.add(attListPanel);
		listPanel.add(opListPanel);

		bigPanel.add(labelPanel);
		bigPanel.add(listPanel);
		bigPanel.add(buttonBox);
		bigPanel.setMaximumSize(new Dimension(300, Cytoscape.getDesktop()
				.getHeight()));

		return bigPanel;
	}

	/**
	 * Creates the criteria and label text fields, along with the color chooser,
	 * add, and clear buttons.
	 */
	private JPanel getCriteriaChooserPanel() {
		JPanel fieldPanel = new JPanel();

		BoxLayout box = new BoxLayout(fieldPanel, BoxLayout.Y_AXIS);
		fieldPanel.setLayout(box);

		Border refBorder = BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(refBorder,
				"Expression Editor");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		fieldPanel.setBorder(titleBorder);

		JTextArea jt = new JTextArea(
				"Use this field to construct expressions using Attributes and Operations below.");
		jt.setFont(new Font("Arial", Font.PLAIN, 10));
		jt.setBackground(this.getBackground());
		fieldPanel.add(jt);

		criteriaField = new JTextField();

		criteriaField.setHorizontalAlignment(JTextField.LEFT);

		fieldPanel.add(criteriaField);
		fieldPanel.setMaximumSize(new Dimension(Cytoscape.getDesktop()
				.getWidth(), 80));

		return fieldPanel;
	}

	String criteriaBuild = "";
	int last = -1;

	/**
	 * Handles list selection on attributes. Further below their is a separate
	 * class, getOperationSelection which does the exact same thing as this
	 * method but on the other list. Since their were two lists, one for the
	 * attributes, and one for operations I found that their was no good way to
	 * tell apart ListSelectionEvents from two separate lists. I thus was forced
	 * to create the other class to handle the operation selection.
	 */
	public void valueChanged(ListSelectionEvent e) {

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if (lsm.isSelectionEmpty()) {
			// System.out.println(" <none>");
		} else {
			// Find out which indexes are selected.
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();

			for (int i = minIndex; i <= maxIndex; i++) {
				// if (lsm.isSelectedIndex(i) && last != i) {
				criteriaBuild = criteriaField.getText();
				if (attributesArray[i].contains(" ")) {
					criteriaBuild = criteriaBuild + " \"" + attributesArray[i]
							+ "\" ";
				} else {
					criteriaBuild = criteriaBuild + " " + attributesArray[i]
							+ " ";
				}
				criteriaField.setText(criteriaBuild);
				// System.out.println("Selected Index: "+i);
				// }
				last = i;
			}
		}
		attList.clearSelection();
		criteriaField.requestFocus();
		criteriaField.setCaretPosition(criteriaField.getText().length());
		criteriaField.setHorizontalAlignment(JTextField.LEFT);

	}

	/**
	 * Class which handles the list selection for the operation List. I had to
	 * create this class because I could not find a good way of distinguishing
	 * between ListSelectionEvents coming from different lists that are
	 * registered to the same listener.
	 */
	class getOperationSelection implements ListSelectionListener {

		public getOperationSelection() {

		}
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();

			if (lsm.isSelectionEmpty()) {
				// System.out.println(" <none>");
			} else {
				// Find out which indexes are selected.
				int minIndex = lsm.getMinSelectionIndex();
				int maxIndex = lsm.getMaxSelectionIndex();

				for (int i = minIndex; i <= maxIndex; i++) {

					// if (lsm.isSelectedIndex(i) && last != i) {
					criteriaBuild = criteriaField.getText();
					criteriaBuild = criteriaBuild + " " + opArray[i] + " ";
					criteriaField.setText(criteriaBuild);
					// System.out.println("Selected Index: "+i);

					// }
					last = i;
				}
			}
			opList.clearSelection();
			criteriaField.requestFocus();
			criteriaField.setCaretPosition(criteriaField.getText().length());
			criteriaField.setHorizontalAlignment(JTextField.LEFT);

		}
	}

	/**
	 * @return
	 */
	public String[] getAllAttributes() {

		getAttributesList(attributeList, Cytoscape.getNodeAttributes(), "__");

		String[] str = (String[]) attributeList
				.toArray(new String[attributeList.size()]);
		attributeList.clear();
		return str;

	}

	/**
	 * @param attributeList
	 * @param attributes
	 * @param prefix
	 */
	public void getAttributesList(ArrayList<String> attributeList,
			CyAttributes attributes, String prefix) {
		String[] names = attributes.getAttributeNames();
		ArrayList<String> numericAttributes = new ArrayList<String>();
		// ArrayList<String> stringAttributes = new ArrayList<String>();
		ArrayList<String> booleanAttributes = new ArrayList<String>();
		for (int i = 0; i < names.length; i++) {
			if (!attributes.getUserVisible(names[i])
					|| names[i].startsWith(prefix)) {
				continue;
			}
			if (attributes.getType(names[i]) == CyAttributes.TYPE_FLOATING
					|| attributes.getType(names[i]) == CyAttributes.TYPE_INTEGER) {
				numericAttributes.add(names[i]);
			} else if (attributes.getType(names[i]) == CyAttributes.TYPE_BOOLEAN) {
				booleanAttributes.add(names[i]);
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

	}

}
