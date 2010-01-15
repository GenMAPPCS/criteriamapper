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

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
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
	private CriteriaBuilderDialog cbDialog;

	private JPanel tablePanel;
	// private JPanel criteriaControlPanel;
	// private JPanel tableButtons;
	private JTable table;

	// private int listCount = 0;
	// private int globalRowCount = 0;

	// private ArrayList<String> allCurrentLabels = new ArrayList<String>();

	// String mapTo = "Node Color";
	// boolean setFlag = false;

	// private String CBlabel;
	// private String CBcriteria;
	// private String CBvalue;
	// private String CBmapTo;

	protected String setName = "New...";
	protected String mapTo[] = { "Node Color", "Node Size", "Node Shape" };
	public String mapToPick = mapTo[0];

	private CriteriaCalculator calculate = new CriteriaCalculator();

	public CriteriaTablePanel() {
		dataModel = new BooleanTableModel();
		table = new JTable(dataModel);
		mapper = new ColorMapper();
		attManager = new AttributeManager();
		colorEditor = new ColorEditor();
		// calculator = new BooleanCalculator();
		cbDialog = new CriteriaBuilderDialog(this);
		initializeTable();
	}

	public JPanel getTablePanel() {

		return this.tablePanel;
	}

	/*
	 * Initializes the table, table buttons, and adds it all to a JPanel which
	 * can be returned by the method above to the construction of the larger GUI
	 * in CriteriaMapperDialog.
	 */
	public void initializeTable() {

		tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
		table = new JTable(dataModel);

		/*
		 * This is the code that causes to color editor to only pop up on a
		 * double click of the color cell in the table.
		 */

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				int row = target.getSelectedRow();
				int column = target.getSelectedColumn();

				// fill Builder fields from selected row cells
				// cbDialog.labelField.setText((String) getCell(row, 1));
				// cbDialog.criteriaField.setText((String) getCell(row, 2));

				if (e.getClickCount() == 1) {
					if (column == 0) {
						Object show = dataModel.getValueAt(row, column);
						show = show + "";
						// if(show.equals("true")){
						applyCriteria();
					}

				}

				if (e.getClickCount() == 2) {
					System.out.println("Check 1:"
							+ dataModel.isCellEditable(row, column));

					if (column == 2) {
						// TODO: pop up slim version of criteria builder
						System.out.println("text: "
								+ dataModel.getValueAt(row, column));
						cbDialog.criteriaField.setText((String) dataModel
								.getValueAt(row, column));
					}

					if (column == 4) {
						System.out.println("Check 2");
						JColorChooser colorChooser = new JColorChooser();
						JButton button = new JButton();
						button.setActionCommand("edit");
						// button.addActionListener(this);
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
						// initializeTable();
					}
				}
			}
		});

		// table.setFillsViewportHeight(true);

		// Disable auto resizing
		// table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		TableColumn showCol = table.getColumnModel().getColumn(0);
		// TableColumn buildCol = table.getColumnModel().getColumn(2);
		TableColumn mapToCol = table.getColumnModel().getColumn(3);

		showCol.setMaxWidth(40);
		// buildCol.setMaxWidth(25);

		JCheckBox showBox = new JCheckBox();
		showCol.setCellEditor(new DefaultCellEditor(showBox));

		// buildCol.setCellRenderer(new builderIconRenderer(true));

		JComboBox mapToBox = new JComboBox(mapTo);
		mapToCol.setCellEditor(new DefaultCellEditor(mapToBox));

		table.setPreferredScrollableViewportSize(new Dimension(315, 80));

		// Create the scroll pane and add the table to it.

		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel.addListSelectionListener(this);
		table.setSelectionModel(listSelectionModel);
		SelectionListener listener = new SelectionListener();
		table.getSelectionModel().addListSelectionListener(listener);
		table.getColumnModel().getSelectionModel().addListSelectionListener(
				listener);
		table.getModel().addTableModelListener(new MyTableModelListener());

		// Set up renderer and editor for the Favorite Color column.

		table.getColumn("Value").setCellRenderer(new ColorRenderer(true));
		// table.getColumn("").setCellRenderer(new ArrowRenderer(true));

		// table.setEditingColumn(1);

		table.setDefaultEditor(Color.class, colorEditor);
		table.getColumn("Value").setCellEditor(colorEditor);

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
		arrowPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		JPanel horizontalPanel = new JPanel();
		horizontalPanel.setLayout(new BoxLayout(horizontalPanel,
				BoxLayout.X_AXIS));

		horizontalPanel.add(scrollPane);
		horizontalPanel.add(arrowPanel);

		tablePanel.add(horizontalPanel);

		// scrollPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.
		// LOWERED));

		cbDialog.initialize();
		cbDialog.criteriaField.setAction(new UpdateAnchorAction());

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

		if (command.equals("CBdone")) {
			// dataModel.setValueAt(cbDialog.labelField.getText(), row, 1);
			dataModel.setValueAt(cbDialog.criteriaField.getText(), table
					.getSelectedRow(), 2);
			// dataModel.setValueAt(cbDialog.mapToBox.getSelectedItem(), row,
			// 3);
			// dataModel.setValueAt(cbDialog.currentColor, row, 4);
			// applyCriteria();
		}

		// if (command.equals("CBadd")) {
		// System.out.println("HEY");
		// populateList(cbDialog.criteriaField.getText(), cbDialog.labelField
		// .getText(), cbDialog.currentColor);
		// // populateList("Criteria", "label", Color.WHITE);
		//
		// }

		if (command.equals("deleteCriteria")) {

			int[] rows = table.getSelectedRows();
			// data[row][0] = "";

			if (rows.length == 1) {
				try {
					attManager.removeColorAttribute(getCell(rows[0], 1) + "");
				} catch (Exception failedDelete) {
					System.out.println(failedDelete.getMessage());
				}

				dataModel.removeRow(rows[0]);
				// dataModel.rowCount--;
				// dataModel.fireTableRowsDeleted(row[0], row[0]);
				// dataModel.createNewDataObject(dataModel.rowCount, 3);

				// if (rows[0] != 0) {
				// while (getCell(rows[0] - 1, 0) == "") {
				// rows[0]--;
				// }
				// }
				// listCount = row[0];

			} else if (rows.length == 0) {
				// TODO: fix this!
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						"Please select a row first.");
			} else {
				// TODO: fix this!
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
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

	public class UpdateAnchorAction extends AbstractAction {
		int row = table.getSelectedRow();

		UpdateAnchorAction() {
			super("Set Anchor");
		}

		// Update the value in the anchor cell whenever the text field changes
		public void actionPerformed(ActionEvent evt) {
			JTextField textField = (JTextField) evt.getSource();
			// Get anchor cell location
			dataModel.setValueAt(textField.getText(), this.row, 2);
		}
	}

	public class SelectionListener implements ListSelectionListener {
		// Update the text field whenever the anchor cell changes
		public void valueChanged(ListSelectionEvent e) {
			int rowIndex = table.getSelectedRow();
			int vColIndex = table.getSelectedColumn();
			if (vColIndex == 2) {
				System.out.println("r=" + rowIndex + " c=" + vColIndex);
				// Get the value and set the text field
				cbDialog.criteriaField.setText((String) table.getValueAt(
						rowIndex, vColIndex));
			}
		}
	}

	public class MyTableModelListener implements TableModelListener {
		// It is necessary to keep the table since it is not possible
		// to determine the table from the event's source
		MyTableModelListener() {
		}

		// Update the text field whenever the value in the anchor cell changes
		public void tableChanged(TableModelEvent e) {
			// Get anchor cell location
			int rAnchor = table.getSelectedRow();
			int vcAnchor = table.getSelectedColumn();
			// This method is defined in
			// Converting a Column Index Between the View and Model in a JTable
			// Component
			// int mcAnchor = vcAnchor; // toModel(table, vcAnchor);
			// // Get affected rows and columns
			// int firstRow = e.getFirstRow();
			// int lastRow = e.getLastRow();
			// int mColIndex = e.getColumn();
			// if (firstRow != TableModelEvent.HEADER_ROW
			// && rAnchor >= firstRow
			// && rAnchor <= lastRow
			// && (mColIndex == TableModelEvent.ALL_COLUMNS || mColIndex ==
			// mcAnchor)) {
			// Set the text field with the new value
			if (vcAnchor == 2) {
				cbDialog.criteriaField.setText((String) table.getValueAt(
						rAnchor, vcAnchor));
			}
		}

	}

	// Converts a visible column index to a column index in the model.
	// Returns -1 if the index does not exist.
	public int toModel(JTable table, int vColIndex) {
		if (vColIndex >= table.getColumnCount()) {
			return -1;
		}
		return table.getColumnModel().getColumn(vColIndex).getModelIndex();
	}

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
		ArrayList<String> stemp = new ArrayList<String>();
		for (int i = 0; i < indices.length; i++) {

			temp[i] = getCell(indices[i], 1) + "";

		}
		// return stemp.toArray(String[] tem);
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

					System.out.println("LENGTHHH: " + temp.length);
					if (temp.length == 1) {
						System.out.println("Selected Index: " + i);
						String colorString = getCell(i, 4) + "";

						if (getCell(i, 0).equals("")) {
							return;
						}
						Color c = stringToColor(colorString);
						mapper.createDiscreteMapping(getCell(i, 1)
								+ "_discrete", (String) getCell(i, 1), c,
								mapToPick);
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
		ArrayList<Color> colors = new ArrayList<Color>();
		String compositeLabel = "";
		String[] nameLabels = new String[getDataLength()];
		if (setName.equals("")) {
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
					"Must have a set name.");
			return;
		}
		for (int i = 0; i < getDataLength(); i++) {
			String label = setName + "_" + getCell(i, 1);
			String current = (String) getCell(i, 2);
			String showBoolean = "" + getCell(i, 0);
			if (current != null && !current.equals("") && !label.equals("")) { // &&
				// showBoolean
				// .
				// equals
				// (
				// "true"
				// )

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
				labels.add(label);

				Color c = stringToColor(getCell(i, 4) + "");
				colors.add(c);

			}
		}

		String[] labelsA = new String[labels.size()];
		for (int h = 0; h < labels.size(); h++) {
			labelsA[h] = labels.get(h);
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

		// System.out.println("compositeLabel: "+compositeLabel);
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
		if (col == 5) {
			// java.awt.Color[r=0,g=0,b=255]
			Color temp = stringToColor(value);
			dataModel.setValueAt(temp, row, col);
			// System.out.println("set values");
			return;
		}
		dataModel.setValueAt(value, row, col);
	}

	public void clearTable() {
		dataModel.rowCount = 1;

		for (int i = 0; i < dataModel.rowCount; i++) {
			for (int j = 0; j < dataModel.colCount; j++) {
				if (j == 0) {
					dataModel.setValueAt(false, i, j);
				} else {
					if (j == 4) {
						dataModel.setValueAt(Color.WHITE, i, j);
					} else {
						dataModel.setValueAt("", i, j);
					}
				}
			}
		}
	}

	public int getDataLength() {
		return dataModel.getRowCount();
	}

	public void deleteRow(int rowNumber) {
		setCell(rowNumber, 1, "");
		setCell(rowNumber, 2, "");
		setCell(rowNumber, 3, "");

	}

	public void addEditableRow() {
		dataModel.addRow();
		// table.setModel(dataModel);

		// setCell(editableRowCount-1, 1, labelstr);
		// setCell(editableRowCount-1, 2, critstr);
		// if(editableRowCount > 4){

		// populateList(critstr, labelstr, Color.white); // }
	}

	public void populateList(String criteria, String label, Color currentColor) {

		// for (int i = 0; i < dataModel.getRowCount(); i++) {
		// // System.out.println("i: "+i);
		// if (getCell(i, 1) == null || getCell(i, 1).equals((""))) {
		//
		// // System.out.println(i);
		// listCount = i;
		// break;
		// }
		//
		// }

		System.out.println(" Global Row Count: " + dataModel.rowCount
				+ " Criteria: " + criteria + " Label: " + label);

		// if (listCount < globalRowCount) {
		// }

		dataModel.setValueAt(label, dataModel.rowCount - 1, 1);
		dataModel.setValueAt(criteria, dataModel.rowCount - 1, 2);
		dataModel.setValueAt("Node Color", dataModel.rowCount - 1, 3);
		dataModel.setValueAt(currentColor, dataModel.rowCount - 1, 4);

		dataModel.fireTableDataChanged();

		// listCount++;
		// initializeTable();

	}

	public void moveRowUp(int rowNumber) {
		if (rowNumber != 0) {

			Object showTemp = dataModel.data[rowNumber][0];
			Object labelTemp = dataModel.data[rowNumber][1];
			Object criteriaTemp = dataModel.data[rowNumber][2];
			Object mapToTemp = dataModel.data[rowNumber][3];
			Object colorTemp = dataModel.data[rowNumber][4];

			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][0], rowNumber, 0);
			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][1], rowNumber, 1);
			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][2], rowNumber, 2);
			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][3], rowNumber, 3);
			dataModel
					.setValueAt(dataModel.data[rowNumber - 1][4], rowNumber, 4);

			dataModel.setValueAt(showTemp, rowNumber - 1, 0);
			dataModel.setValueAt(labelTemp, rowNumber - 1, 1);
			dataModel.setValueAt(criteriaTemp, rowNumber - 1, 2);
			dataModel.setValueAt(mapToTemp, rowNumber - 1, 3);
			dataModel.setValueAt(colorTemp, rowNumber - 1, 4);

			applyCriteria();
		}
	}

	public void moveRowDown(int rowNumber) {
		if (rowNumber != dataModel.rowCount) {

			Object showTemp = dataModel.data[rowNumber][0];
			Object labelTemp = dataModel.data[rowNumber][1];
			Object criteriaTemp = dataModel.data[rowNumber][2];
			Object mapToTemp = dataModel.data[rowNumber][3];
			Object colorTemp = dataModel.data[rowNumber][4];

			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][0], rowNumber, 0);
			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][1], rowNumber, 1);
			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][2], rowNumber, 2);
			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][3], rowNumber, 3);
			dataModel
					.setValueAt(dataModel.data[rowNumber + 1][4], rowNumber, 4);

			dataModel.setValueAt(showTemp, rowNumber + 1, 0);
			dataModel.setValueAt(labelTemp, rowNumber + 1, 1);
			dataModel.setValueAt(criteriaTemp, rowNumber + 1, 2);
			dataModel.setValueAt(mapToTemp, rowNumber + 1, 3);
			dataModel.setValueAt(colorTemp, rowNumber + 1, 4);

			applyCriteria();
			// initializeTable();
		}
	}

	class BooleanTableModel extends AbstractTableModel {
		// initialize variables
		int labelCount = 1;
		int rowCount = 1;
		int colCount = 5;
		boolean empty = true;

		String[] columnNames = { "Show", "Label", "Criteria", "Map To", "Value" };
		Object[][] data = new Object[rowCount][colCount];

		public void addRow() {
			if (!empty) { // preserve data
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
			}
			// add new row
			data[rowCount - 1][0] = false;
			data[rowCount - 1][1] = "Label " + labelCount;
			data[rowCount - 1][2] = "";
			data[rowCount - 1][3] = mapTo[0];
			data[rowCount - 1][4] = Color.WHITE;
			// System.out.println("Added row: "+rowCount);
			empty = false;
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
				fireTableCellUpdated(row, col);
			}
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

	}
}

// class builderIconRenderer extends JLabel implements TableCellRenderer {
// Border unselectedBorder = null;
// Border selectedBorder = null;
// boolean isBordered = true;
//
// public builderIconRenderer(boolean isBordered) {
// this.isBordered = isBordered;
// setOpaque(true);
// }
//
// public Component getTableCellRendererComponent(JTable table, Object color,
// boolean isSelected, boolean hasFocus, int row, int column) {
//
// // java.net.URL builderIconURL = CriteriaTablePanel.class
// // .getResource("img/stock_form-properties.png");
// //
// // ImageIcon builderIcon = new ImageIcon();
// // if (builderIconURL != null) {
// // builderIcon = new ImageIcon(builderIconURL);
// // }
// // this.setIcon(builderIcon);
//
// if (isBordered) {
// if (isSelected) {
// if (selectedBorder == null) {
// selectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
// 5, table.getSelectionBackground());
// }
// setBorder(selectedBorder);
// } else {
// if (unselectedBorder == null) {
// unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
// 5, table.getBackground());
// }
// setBorder(unselectedBorder);
// }
// }
//
// return this;
// }
// }

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

		// setToolTipText("RGB value: " + newColor.getRed() + ", "
		// + newColor.getGreen() + ", "
		// + newColor.getBlue());
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
		dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
				colorChooser, this, // OK button handler
				null); // no CANCEL button handler
	}

	/**
	 * Handles events from the editor button and from the dialog's OK button.
	 */
	public void actionPerformed(ActionEvent e) {
		System.out.println("made dialog");
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
		currentColor = (Color) value;
		return button;
	}

}
