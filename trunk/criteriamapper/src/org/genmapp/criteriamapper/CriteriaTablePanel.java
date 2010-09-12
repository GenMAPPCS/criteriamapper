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

/*
 * Steve Federowicz 
 * Google Summer of Code
 * 
 * This set of classes contains all of the code necessary to generate and make the table portion of the 
 * GUI interactive. 
 * 
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import cytoscape.Cytoscape;

public class CriteriaTablePanel implements ActionListener,
		ListSelectionListener {

	// external classes
	private AttributeManager attManager;

	// internal classes
	private BooleanTableModel dataModel;
	// private BooleanCalculator calculator;
	private ColorMapper mapper;
	private ColorEditor colorEditor;
	private CriteriaEditor criteriaEditor;
	private CriteriaBuilderDialog cbDialog;

	public final int LABEL_COL = 0;
	public final int EXP_COL = 1;
	public final int VALUE_COL = 2;

	private JPanel tablePanel;
	// private JPanel criteriaControlPanel;
	// private JPanel tableButtons;
	private JTable table;
	public boolean savedFlag = false;

	protected String setName; 
	public String mapToPick = "Node Color"; //fixed

	private CriteriaCalculator calculate = new CriteriaCalculator();

	public CriteriaTablePanel() {
		dataModel = new BooleanTableModel();
		mapper = new ColorMapper();
		attManager = new AttributeManager();
		colorEditor = new ColorEditor();
		criteriaEditor = new CriteriaEditor();
		// calculator = new BooleanCalculator();
		cbDialog = new CriteriaBuilderDialog(this);
		initializeTable();
	}

	public JPanel getTablePanel() {
		return this.tablePanel;
	}

	public JTable getTable() {
		return this.table;
	}

	/*
	 * Initializes the table, table buttons, and adds it all to a JPanel which
	 * can be returned by the method above to the construction of the larger GUI
	 * in CriteriaMapperDialog.
	 */
	public void initializeTable() {

		tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		table = new JTable(dataModel);
		Border refBorder = BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(refBorder,
				"Criteria");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		tablePanel.setBorder(titleBorder);
		table.getTableHeader().setReorderingAllowed(false);

		/*
		 * This is the code that causes to color editor to only pop up on a
		 * double click of the color cell in the table.
		 */

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				int row = target.getSelectedRow();
				int column = target.getSelectedColumn();

				if (e.getClickCount() == 1) {
					// update Expression Editor
					String currentValue = (String) target.getValueAt(row,
							EXP_COL);
					cbDialog.criteriaField.setText((String) currentValue);
					// if (column == 0) {
					// Object show = dataModel.getValueAt(row, column);
					// show = show + "";
					// applyCriteria();
					// } else
					if (column == EXP_COL) { // send focus to Expression Editor
						cbDialog.criteriaField.requestFocusInWindow();
						int pos = cbDialog.criteriaField.getText().length() - 1;
						if (pos < 0)
							pos = 0;
						cbDialog.criteriaField.setCaretPosition(pos);
					}
				}

				if (e.getClickCount() == 2) {
					if (column == VALUE_COL) {
						JColorChooser colorChooser = new JColorChooser();
						JButton button = new JButton();
						button.setActionCommand("edit");
						button.setBorderPainted(true);
						JDialog dialog = JColorChooser.createDialog(button,
								"Pick a Color", true, // modal
								colorChooser, null, // OK button handler
								null); // no CANCEL button handler
						dialog.add(button);
						dialog.setLocation(2, Cytoscape.getDesktop()
								.getHeight() - 385);
						dialog.setVisible(true);
						Color currentColor = colorChooser.getColor();
						setCell(row, column, currentColor + "");
						colorEditor.currentColor = currentColor;
					} else if (column == EXP_COL) { // send focus to Expression
						// Editor
						cbDialog.criteriaField.requestFocusInWindow();
						int pos = cbDialog.criteriaField.getText().length() - 1;
						if (pos < 0)
							pos = 0;
						cbDialog.criteriaField.setCaretPosition(pos);
					}
				}
			}
		});

		// table.setFillsViewportHeight(true);

		// Disable auto resizing
		// table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// TableColumn showCol = table.getColumnModel().getColumn(0);
		TableColumn labelTC = table.getColumnModel().getColumn(LABEL_COL);
		TableColumn expTC = table.getColumnModel().getColumn(EXP_COL);
		TableColumn valueTC = table.getColumnModel().getColumn(VALUE_COL);

		labelTC.setMinWidth(50);
		expTC.setMinWidth(180);
		valueTC.setMinWidth(25);

		// JCheckBox showBox = new JCheckBox();
		// showCol.setCellEditor(new DefaultCellEditor(showBox));
		//showBox.setToolTipText("Click to show/hide criteria as visual style");

		DefaultTableCellRenderer labelRenderer = new DefaultTableCellRenderer();
		labelRenderer.setToolTipText("Double-click to edit");
		labelTC.setCellRenderer(labelRenderer);

		expTC.setCellEditor(criteriaEditor);
		DefaultTableCellRenderer expRenderer = new DefaultTableCellRenderer();
		expRenderer.setToolTipText("Click to activate Expression Editor below");
		expTC.setCellRenderer(expRenderer);

		table.setPreferredScrollableViewportSize(new Dimension(315, 80));

		// Create the scroll pane and add the table to it.

		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel.addListSelectionListener(this);
		table.setSelectionModel(listSelectionModel);
		// SelectionListener listener = new SelectionListener();
		// table.getSelectionModel().addListSelectionListener(listener);
		// table.getColumnModel().getSelectionModel().addListSelectionListener(
		// listener);
		// table.getModel().addTableModelListener(new CriteriaCellListener());

		// table.setDefaultEditor(String.class, criteriaEditor);

		// Set up renderer and editor for the Favorite Color column.
		// table.setDefaultEditor(Color.class, colorEditor);
		valueTC.setCellEditor(colorEditor);
		valueTC.setCellRenderer(new ColorRenderer(true));

		table.setEnabled(true);

		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.

		// java.net.URL upArrowURL = CriteriaTablePanel.class
		// .getResource("img/upArrow2.gif");
		// java.net.URL downArrowURL = CriteriaTablePanel.class
		// .getResource("img/downArrow2.gif");
		// ImageIcon upIcon = new ImageIcon();
		// ImageIcon downIcon = new ImageIcon();
		// if (upArrowURL != null) {
		// upIcon = new ImageIcon(upArrowURL);
		// }
		// if (downArrowURL != null) {
		// downIcon = new ImageIcon(downArrowURL);
		// }

		JButton upArrow = new JButton("Move Row Up");
		upArrow.setActionCommand("moveUp");
		upArrow.addActionListener(this);

		JButton downArrow = new JButton("Move Row Down");
		downArrow.setActionCommand("moveDown");
		downArrow.addActionListener(this);

		JButton newRow = new JButton("New Row");
		newRow.setActionCommand("newCriteria");
		newRow.addActionListener(this);

		JButton deleteRow = new JButton("Delete Row");
		deleteRow.setActionCommand("deleteCriteria");
		deleteRow.addActionListener(this);

		JPanel arrowPanel = new JPanel();
		arrowPanel.setLayout(new BoxLayout(arrowPanel, BoxLayout.Y_AXIS));

		arrowPanel.add(upArrow);
		arrowPanel.add(downArrow);
		arrowPanel.add(newRow);
		arrowPanel.add(deleteRow);
		// arrowPanel.setBorder(BorderFactory
		// .createEtchedBorder(EtchedBorder.LOWERED));

		JPanel horizontalPanel = new JPanel();
		horizontalPanel.setLayout(new BoxLayout(horizontalPanel,
				BoxLayout.X_AXIS));

		horizontalPanel.add(scrollPane);
		horizontalPanel.add(arrowPanel);

		tablePanel.add(horizontalPanel);

		// scrollPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
		// LOWERED));

		cbDialog.initialize();
		cbDialog.criteriaField.getDocument().addDocumentListener(
				new ExpressionEditorListener());

	}

	/*
	 * Handles all of the action events in this case the move up, move down, and
	 * delete buttons for altering the table.
	 */
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("newCriteria")) {
			addEditableRow();
		}

		if (command.equals("deleteCriteria")) {

			int[] rows = table.getSelectedRows();
			// data[row][0] = "";

			if (rows.length == 1) {
				try {
					// TODO: is this right?
					attManager.removeColorAttribute(getCell(rows[0], 1) + "");
				} catch (Exception failedDelete) {
					System.out.println(failedDelete.getMessage());
				}
				// remove from table model
				dataModel.removeRow(rows[0]);
				// reset row selection
				int selectableRow = table.getRowCount() - 1;
				if (selectableRow >= 0) {
					table.setRowSelectionInterval(selectableRow, selectableRow);
					String currentValue = (String) table.getValueAt(
							selectableRow, EXP_COL);
					cbDialog.criteriaField.setText((String) currentValue);
				}
			} else if (rows.length == 0) {
				// TODO: fix this!
				JOptionPane.showMessageDialog(cbDialog,
						"Please select a row first");
			} else {
				// TODO: fix this!
				JOptionPane.showMessageDialog(cbDialog,
						"Sorry, I can only delete one row at a time :(");
			}

			// initializeTable();

		}

		if (command.equals("moveUp")) {
			int selected = table.getSelectedRow();

			if (selected != -1 && !(getCell(selected, 0) == "")
					&& !(selected < 1)) {

				moveRowUp(table.getSelectedRow());
				table.setRowSelectionInterval(selected - 1, selected - 1);
			}
		}
		if (command.equals("moveDown")) {
			int selected = table.getSelectedRow();
			if (selected != -1 && !(getCell(selected, 0) == "")
					&& selected >= 0 && selected < dataModel.getRowCount() - 1) {

				moveRowDown(table.getSelectedRow());
				table.setRowSelectionInterval(selected + 1, selected + 1);
			}
		}

	}

	public class ExpressionEditorListener implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
			// TODO Auto-generated method stub
		}

		public void insertUpdate(DocumentEvent e) {
			// TODO Auto-generated method stub
			dataModel.setValueAt(cbDialog.criteriaField.getText(), table
					.getSelectedRow(), EXP_COL);
		}

		public void removeUpdate(DocumentEvent e) {
			// TODO Auto-generated method stub
			dataModel.setValueAt(cbDialog.criteriaField.getText(), table
					.getSelectedRow(), EXP_COL);
		}

	}

	// public class UpdateTableCell extends AbstractAction {
	//
	// UpdateTableCell() {
	// super("Set Anchor");
	// }
	//
	// // Update the value in the anchor cell whenever the text field changes
	// public void actionPerformed(ActionEvent evt) {
	// System.out.println("typing...");
	// // Get anchor cell location
	// dataModel.setValueAt(cbDialog.criteriaField.getText(), table
	// .getSelectedRow(), 2);
	// }
	// }

	// public class SelectionListener implements ListSelectionListener {
	// // Update the text field whenever the anchor cell is selected
	// public void valueChanged(ListSelectionEvent e) {
	//
	// int row = table.getSelectedRow();
	// int col = table.getSelectedColumn();
	// System.out.println("r=" + row + " c=" + col);
	// if (col == 2 && row != -1) {
	//
	// // Get the value and set the text field
	// String currentValue = (String) table.getValueAt(row, col);
	// // if (currentValue.equals("")) {
	// // currentValue = "Edit here...";
	// // }
	// cbDialog.criteriaField.setText((String) currentValue);
	// cbDialog.criteriaField.requestFocusInWindow();
	//
	// //cbDialog.criteriaField.setCaretPosition(cbDialog.criteriaField
	// // .getText().length());
	// }
	// }
	// }

	// public class CriteriaCellListener implements TableModelListener {
	//
	// CriteriaCellListener() {
	// table.getModel().addTableModelListener(this);
	// }
	//
	// // Update the text field whenever the value in the anchor cell changes
	// public void tableChanged(TableModelEvent e) {
	// // Get anchor cell location
	// int row = table.getSelectedRow();
	// int col = table.getSelectedColumn();
	// System.out.println("Change! " + e.getSource());
	// if (col == 2) {
	// cbDialog.criteriaField.setText((String) table.getValueAt(row,
	// col));
	// }
	// }
	//
	// }

	// Converts a visible column index to a column index in the model.
	// Returns -1 if the index does not exist.
	// public int toModel(JTable table, int vColIndex) {
	// if (vColIndex >= table.getColumnCount()) {
	// return -1;
	// }
	// return table.getColumnModel().getColumn(vColIndex).getModelIndex();
	// }

	public Color[] getColorArray(int[] indices) {
		Color[] temp = new Color[indices.length];
		for (int i = 0; i < indices.length; i++) {
			String colorString = getCell(indices[i], 2) + "";
			temp[i] = stringToColor(colorString);
		}
		return temp;
	}

	public String[] getLabelArray(int[] indices) {
		String[] temp = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			temp[i] = getCell(indices[i], 1) + "";
		}
		return temp;
	}

	public String getCompositeLabel(String[] labels) {
		String compositeLabel = labels[0];
		for (int i = 1; i < labels.length; i++) {
			if (labels[i] == null || labels[i].equals("")) {
				return compositeLabel + "";
			}
			compositeLabel = compositeLabel + ":" + labels[i];
		}
		return compositeLabel + "";
	}

	/*
	 * This method handles all of the list and table selection events. If you
	 * select one row of the table it calls createDiscreteMapping from
	 * ColorMapper.java. If you select more than one row it calls
	 * setCompositeAttribute from AttributeManager.java to set a composite
	 * attribute. It then calls createCompositeMapping from ColorMapper.java and
	 * uses the composite attribute created to map all of the highlighted rows
	 * to colors. The composite attribute is stored as a boolean attribute for
	 * cytoscape and accessed by a colon separated string of the labels in the
	 * order of their selection.
	 */
	public void valueChanged(ListSelectionEvent e) {

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		int firstIndex = e.getFirstIndex();
		int lastIndex = e.getLastIndex();
		boolean isAdjusting = e.getValueIsAdjusting();

		if (lsm.isSelectionEmpty() || true) {
			// System.out.println(" <none>");
		} else {

			// Find out which indexes are selected.
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			int last = -1;

			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {

					int[] temp = table.getSelectedRows();

					System.out.println("LENGTH: " + temp.length);
					if (temp.length == 1) {
						System.out.println("Selected Index: " + i);
						String colorString = getCell(i, VALUE_COL) + "";

						// if (getCell(i, 0).equals("")) {
						// return;
						// }
						Color c = stringToColor(colorString);
						mapper.createDiscreteMapping(getCell(i, LABEL_COL)
								+ "_discrete", (String) getCell(i, LABEL_COL),
								c, mapToPick);
					} else {
						String[] labels = getLabelArray(temp);
						Color[] colors = getColorArray(temp);

						String compositeLabel = getCompositeLabel(labels);
						System.out
								.println("COMPOSITE LABEL: " + compositeLabel);
						if (labels.length == 1) {
							mapper.createDiscreteMapping(labels[0]
									+ "_discrete", labels[0], colors[0],
									mapToPick);
							break;
						}
						if (labels.length == 2
								&& (labels[0].equals("") || labels[1]
										.equals(""))) {

						}
						if (attManager.isCompositeAttribute(compositeLabel)) {
							attManager.removeCompositeAttribute(compositeLabel);
						}
						try {
							attManager.setCompositeAttribute(labels);
						} catch (Exception setAttFailure) {
							//System.out.println("NO"+setAttFailure.getMessage()
							// +"WAY");
						}
						mapper.createCompositeMapping(compositeLabel
								+ "_discrete", compositeLabel, colors,
								mapToPick);
					}

				}

			}

		}

	}

	public void applyCriteria() {

		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<String> maptos = new ArrayList<String>();
		ArrayList<Color> colors = new ArrayList<Color>();
		String compositeLabel = "";
		String[] nameLabels = new String[getDataLength()];
		if (setName.equals("")) {
			JOptionPane.showMessageDialog(cbDialog,
					"Must have a set name.");
			return;
		}
		for (int i = 0; i < getDataLength(); i++) {
			String label = setName + "_" + getCell(i, LABEL_COL);
			String current = (String) getCell(i, EXP_COL);
			// String showBoolean = "" + getCell(i, 0);
			if (current != null && !current.equals("") && !label.equals("")) {
				try {
					calculate.parse(current);
				} catch (Exception p) {
					System.out.println(p.getMessage());
					break;
				}

				calculate.printTokens();

				if (i == 0) {
					compositeLabel = label;
				} else {
					if (!label.equals("") && label != null) {
						compositeLabel = compositeLabel + ":" + label;
					}
				}
				// System.out.println("compositeLabel: "+compositeLabel);
				calculate.evaluateLeftToRight(label);
				if (labels.contains(label)) {
					JOptionPane.showMessageDialog(cbDialog,
							"Must have unique labels");
					return;
				} else {
					labels.add(label);
				}
				Color c = stringToColor(getCell(i, VALUE_COL) + "");
				colors.add(c);
			}
		}

		String[] labelsA = new String[labels.size()];
		for (int h = 0; h < labels.size(); h++) {
			labelsA[h] = labels.get(h);
		}

		String[] maptosA = new String[maptos.size()];
		for (int h = 0; h < maptos.size(); h++) {
			maptosA[h] = maptos.get(h);
		}

		try {
			attManager.setCompositeAttribute(labelsA);
		} catch (Exception e) {
			System.out.println("COMPOSITE FAILED!! " + e.getMessage());
		}

		Color[] colorsA = new Color[labels.size()];
		for (int g = 0; g < labels.size(); g++) {
			colorsA[g] = colors.get(g);
		}

		if (labels.size() == 0) {
			mapper.clearNetwork();
			return;
		}
		if (labels.size() == 1) {
			mapper.createDiscreteMapping(labelsA[0] + "_discrete", labelsA[0],
					colorsA[0], mapToPick);
		} else {
			mapper.createCompositeMapping(compositeLabel + "_discrete",
					compositeLabel, colorsA, mapToPick);
		}

	}

	public Color stringToColor(String value) {
		Pattern p = Pattern
				.compile("java.awt.Color\\[r=(\\d+),g=(\\d+),b=(\\d+)\\]");
		Matcher m = p.matcher(value);
		if (m.matches()) {
			// System.out.println(m.group(1)+" "+m.group(2)+" "+m.group(3));
			Color temp = new Color(Integer.parseInt(m.group(1)), Integer
					.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
			return temp;
		}
		return Color.white;
	}

	// Wrapper methods for the inner class BooleanTableModel
	public Object getCell(int row, int col) {
		return dataModel.getValueAt(row, col);
	}

	public void setCell(int row, int col, String value) {
		if (col == VALUE_COL) {
			// java.awt.Color[r=0,g=0,b=255]
			Color temp = stringToColor(value);
			dataModel.setValueAt(temp, row, col);
			// System.out.println("set values");
			return;
		}
		dataModel.setValueAt(value, row, col);
	}

	public void clearTable() {
		// applyCriteria();
		dataModel.labelCount = 0;
		dataModel.rowCount = 0;
		// for (int i = 0; i < dataModel.rowCount; i++) {
		// for (int j = 0; j < dataModel.colCount; j++) {
		// if (j == VALUE_COL) {
		// dataModel.setValueAt(Color.WHITE, i, j);
		// } else {
		// dataModel.setValueAt("", i, j);
		// }
		// }
		//
		// }
	}

	public int getDataLength() {
		return dataModel.getRowCount();
	}

	public void deleteRow(int rowNumber) {
		setCell(rowNumber, LABEL_COL, "");
		setCell(rowNumber, EXP_COL, "");
	}

	public void addEditableRow() {
		dataModel.addRow();
		int selectableRow = table.getRowCount() - 1;
		table.setRowSelectionInterval(selectableRow, selectableRow);
		String currentValue = (String) table.getValueAt(selectableRow, EXP_COL);
		cbDialog.criteriaField.setText((String) currentValue);
	}

	public void populateList(String criteria, String label, Color currentColor) {
		dataModel.addRow();
		System.out.println("  Row Count: " + dataModel.rowCount + " Criteria: "
				+ criteria + " Label: " + label);

		dataModel.setValueAt(label, dataModel.rowCount - 1, LABEL_COL);
		dataModel.setValueAt(criteria, dataModel.rowCount - 1, EXP_COL);
		dataModel.setValueAt(currentColor, dataModel.rowCount - 1, VALUE_COL);

		dataModel.fireTableDataChanged();

		int selectableRow = table.getRowCount() - 1;
		table.setRowSelectionInterval(selectableRow, selectableRow);
		String currentValue = (String) table.getValueAt(selectableRow, EXP_COL);
		cbDialog.criteriaField.setText((String) currentValue);
		savedFlag = true;
	}

	public void moveRowUp(int rowNumber) {
		if (rowNumber != 0) {

			Object temp0 = dataModel.data[rowNumber][0];
			Object temp1 = dataModel.data[rowNumber][1];
			Object temp2 = dataModel.data[rowNumber][2];

			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][0], rowNumber, 0);
			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][1], rowNumber, 1);
			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][2], rowNumber, 2);

			dataModel.setValueAt(temp0, rowNumber - 1, 0);
			dataModel.setValueAt(temp1, rowNumber - 1, 1);
			dataModel.setValueAt(temp2, rowNumber - 1, 2);

			applyCriteria();
		}
	}

	public void moveRowDown(int rowNumber) {
		if (rowNumber != dataModel.rowCount) {

			Object temp0 = dataModel.data[rowNumber][0];
			Object temp1 = dataModel.data[rowNumber][1];
			Object temp2 = dataModel.data[rowNumber][2];

			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][0], rowNumber, 0);
			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][1], rowNumber, 1);
			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][2], rowNumber, 2);

			dataModel.setValueAt(temp0, rowNumber + 1, 0);
			dataModel.setValueAt(temp1, rowNumber + 1, 1);
			dataModel.setValueAt(temp2, rowNumber + 1, 2);

			applyCriteria();
			// initializeTable();
		}
	}

	class BooleanTableModel extends AbstractTableModel {
		// initialize variables
		int labelCount = 1;
		int rowCount = 1;
		int colCount = 3;
		// boolean empty = true;

		String[] columnNames = { "Label", "Expression", "Value" };
		Object[][] data = new Object[rowCount][colCount];

		public void addRow() {
			// if (!empty) { // preserve data
			rowCount++;
			labelCount++;
			Object[][] temp = data;
			data = new Object[rowCount][colCount];

			for (int i = 0; i < rowCount; i++) {
				for (int j = 0; j < colCount; j++) {
					if (i < temp.length) {
						data[i][j] = temp[i][j];
					}
				}
			}
			// }
			// add new row
			data[rowCount - 1][LABEL_COL] = "Label " + labelCount;
			data[rowCount - 1][EXP_COL] = "";
			data[rowCount - 1][VALUE_COL] = Color.WHITE;
			// System.out.println("Added row: "+rowCount);
			// empty = false;
			savedFlag = false;
			fireTableDataChanged();

		}

		public void removeRow(int row) {
			rowCount--;
			if (rowCount == 0) {
				labelCount = 0; // reset label only if all rows are removed
			}
			int newi = 0;
			Object[][] temp = data;
			data = new Object[rowCount][colCount];

			for (int i = 0; i <= rowCount; i++) {
				if (i == row) { // skip removed row
					continue;
				}
				for (int j = 0; j < colCount; j++) {
					data[newi][j] = temp[i][j];
				}
				newi++;
			}
			savedFlag = false;
			fireTableDataChanged();
		}

		public boolean isCellEditable(int row, int col) {
			if (row < rowCount && row >= 0) {
				return true;
			} else {
				return false;
			}
		}

		public String getColumnName(int i) {
			return columnNames[i];
		}

		public int getColumnCount() {
			return colCount;
		}

		public int getRowCount() {
			return rowCount;
		}

		public Object getValueAt(int row, int col) {
			if (row == -1 || col == -1) {
				return "";
			}
			if (data[row][col] != null) {
				return data[row][col];
			} else {
				if (col == 0) {
					return false;
				}
				return "";
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (row >= 0 && col >= 0) {
				data[row][col] = value;
				savedFlag = false;
				fireTableCellUpdated(row, col);
			}
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

	}
}

/*
 * ColorRenderer and ColorEditor are taken almost verbatim from the Java Trail's
 * sun tutorial on using tables at
 * http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
 */

class ColorRenderer extends JLabel implements TableCellRenderer {
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;

	public ColorRenderer(boolean isBordered) {
		this.isBordered = isBordered;
		setOpaque(true); // MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table, Object color,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setToolTipText("Double-click to pick a color");
		Color newColor = stringToColor(color + "");
		setBackground(newColor);
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
							5, table.getSelectionBackground());
				}
				setBorder(selectedBorder);
			} else {
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
							5, table.getBackground());
				}
				setBorder(unselectedBorder);
			}
		}

		return this;
	}

	public Color stringToColor(String value) {
		Pattern p = Pattern
				.compile("java.awt.Color\\[r=(\\d+),g=(\\d+),b=(\\d+)\\]");
		Matcher m = p.matcher(value);
		if (m.matches()) {
			// System.out.println(m.group(1)+" "+m.group(2)+" "+m.group(3));
			Color temp = new Color(Integer.parseInt(m.group(1)), Integer
					.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
			return temp;
		}
		return Color.white;
	}
}

class ColorEditor extends AbstractCellEditor implements TableCellEditor,
		ActionListener {
	Color currentColor;
	JButton button;
	JColorChooser colorChooser;
	JDialog dialog;

	public ColorEditor() {
		// Set up the editor (from the table's point of view),
		// which is a button.
		// This button brings up the color chooser dialog,
		// which is the editor from the user's point of view.
		button = new JButton();
		button.setActionCommand("edit");
		button.addActionListener(this);
		button.setBorderPainted(false);
		// System.out.println("made editor");
		// Set up the dialog that the button brings up.
		colorChooser = new JColorChooser();
		// dialog = JColorChooser.createDialog(button, "Pick a Color", true, //
		// modal
		// colorChooser, this, // OK button handler
		// null); // no CANCEL button handler
	}

	/**
	 * Handles events from the editor button and from the dialog's OK button.
	 */
	public void actionPerformed(ActionEvent e) {
		// System.out.println("color editor");
		if (e.getActionCommand().equals("edit")) {

			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			// dialog.setVisible(true);

			// Make the renderer reappear.
			fireEditingStopped();

		} else { // User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
		}
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return currentColor;
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (value.getClass().equals(String.class)) {
			value = this.stringToColor((String) value);
		}
		currentColor = (Color) value;
		return button;
	}

	public Color stringToColor(String value) {
		Pattern p = Pattern
				.compile("java.awt.Color\\[r=(\\d+),g=(\\d+),b=(\\d+)\\]");
		Matcher m = p.matcher(value);
		if (m.matches()) {
			// System.out.println(m.group(1)+" "+m.group(2)+" "+m.group(3));
			Color temp = new Color(Integer.parseInt(m.group(1)), Integer
					.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
			return temp;
		}
		return Color.white;
	}

}

class CriteriaEditor extends AbstractCellEditor implements TableCellEditor,
		ActionListener {
	JButton button;

	String criteria;
	private CriteriaBuilderDialog cbDialog;

	public CriteriaEditor() {
		// Set up the editor (from the table's point of view),
		// which is a button.
		// This button brings up the color chooser dialog,
		// which is the editor from the user's point of view.
		button = new JButton();
		button.setActionCommand("build");
		button.addActionListener(this);
		button.setBorderPainted(false);
		// System.out.println("made editor");

	}

	/**
	 * Handles events from the editor button and from the dialog's OK button.
	 */
	public void actionPerformed(ActionEvent e) {
		// System.out.println("expression editor");
		if (e.getActionCommand().equals("build")) {

			// Make the renderer reappear.
			fireEditingStopped();

		}
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return criteria;
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		criteria = (String) value;
		return button;
	}
}
