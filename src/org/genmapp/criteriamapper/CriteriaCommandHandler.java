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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;

public class CriteriaCommandHandler extends AbstractCommandHandler {

	public final static String NAMESPACE = "criteriamapper";
	public final static String PROPERTY_SETS = "org.genmapp.criteriasets_1.0";
	public final static String PROPERTY_SET_PREFIX = "org.genmapp.criteriaset.";
	// public final static String NET_ATTR_APPLIED_SET =
	// "org.genmapp.criteriaset";

	// Commands and associated args
	public static final String OPEN_CRITERIA_MAPPER = "open dialog";
	public static final String LIST_SETS = "list sets";
	public static final String LIST_CRITERIA = "list criteria";
	public static final String DELETE_SET = "delete set";
	public static final String APPLY_SET = "apply set";

	public static final String ARG_SETNAME = "setname";
	public static final String ARG_NETWORK = "network";
	public static final String ARG_MAP_TO = "mapto";
	public static final String ARG_LABEL_LIST = "labellist";
	public static final String ARG_EXP_LIST = "expressionlist";
	public static final String ARG_COLOR_LIST = "colorlist";
	// public static final String ARG_CREATE_FLAG = "createflag";

	// EXTERNAL
	private final static String WORKSPACES = "workspaces";
	private final static String UPDATE_CRITERIASETS = "update criteriasets";

	public CriteriaMapperDialog settingsDialog;
	private CriteriaCalculator calculate = new CriteriaCalculator();
	private AttributeManager attManager = new AttributeManager();;
	private ColorMapper mapper = new ColorMapper();

	public CriteriaCommandHandler() {
		super(CyCommandManager.reserveNamespace(NAMESPACE));

		addDescription(OPEN_CRITERIA_MAPPER, "Open set dialog");
		addArgument(OPEN_CRITERIA_MAPPER, ARG_SETNAME);

		addDescription(LIST_SETS, "List existing criteria sets per network");
		addArgument(LIST_SETS);

		addDescription(LIST_CRITERIA,
				"List criteria for a given set and network");
		addArgument(LIST_CRITERIA, ARG_SETNAME);

		addDescription(DELETE_SET, "Delete a set from a given network");
		addArgument(DELETE_SET, ARG_SETNAME);

		addDescription(APPLY_SET,
				"Apply set to a network, creating the set if needed");
		addArgument(APPLY_SET, ARG_SETNAME);
		addArgument(APPLY_SET, ARG_NETWORK);
		addArgument(APPLY_SET, ARG_MAP_TO);
		addArgument(APPLY_SET, ARG_LABEL_LIST);
		addArgument(APPLY_SET, ARG_EXP_LIST);
		addArgument(APPLY_SET, ARG_COLOR_LIST);

	}

	public CyCommandResult execute(String command, Collection<Tunable> args)
			throws CyCommandException {
		return execute(command, createKVMap(args));
	}

	public CyCommandResult execute(String command, Map<String, Object> args)
			throws CyCommandException {

		CyCommandResult result = new CyCommandResult();

		if (command.equals(OPEN_CRITERIA_MAPPER)) {
			String setName;
			Object sn = getArg(command, ARG_SETNAME, args);
			if (sn instanceof String) {
				setName = (String) sn;
			} else {
				setName = null;
			}

			// Create the dialog
			if (null == settingsDialog) {
				settingsDialog = new CriteriaMapperDialog();
			} else if (!settingsDialog.isVisible()) {
				settingsDialog = new CriteriaMapperDialog();
			} else {
				settingsDialog.setVisible(true);
			}
			// Keep it on top and active?
			settingsDialog.setAlwaysOnTop(true);
			// settingsDialog.setModal(false);

			if (null != setName) {
				settingsDialog.setName = setName;
				settingsDialog.ctPanel.setName = setName;
				settingsDialog.ctPanel.clearTable();
				settingsDialog.loadSettings(setName);
				settingsDialog.nameBox.setSelectedItem(setName);

				settingsDialog.tablePanel.setVisible(true);
				settingsDialog.controlPanel.setVisible(true);
				settingsDialog.applySet.setEnabled(true);
				settingsDialog.saveSet.setEnabled(true);
				settingsDialog.deleteSet.setEnabled(true);
				settingsDialog.duplicateSet.setEnabled(true);
				settingsDialog.nameBox.setEditable(false);
				settingsDialog.pack();
			} else {
				// just pop it up
				settingsDialog.actionPerformed(null);
			}

		} else if (command.equals(LIST_SETS)) {
			List<String> sets = new ArrayList<String>();
			String setString = CytoscapeInit.getProperties().getProperty(
					PROPERTY_SETS);
			setString = setString.substring(1, setString.length() - 1);
			String[] setArray = setString.split("\\]\\[");
			sets = Arrays.asList(setArray);
			result.addResult(sets);
			for (String set : sets) {
				result.addMessage(set);
			}

		} else if (command.equals(LIST_CRITERIA)) {
			String setName;
			Object sn = getArg(command, ARG_SETNAME, args);
			if (sn instanceof String) {
				setName = (String) sn;
			} else {
				setName = null;
			}
			List<String> criteria = new ArrayList<String>();
			String criteriaStr = CytoscapeInit.getProperties().getProperty(
					PROPERTY_SET_PREFIX + setName);
			criteriaStr = criteriaStr.substring(1, criteriaStr.length() - 1);
			String[] criteriaArray = criteriaStr.split("\\]\\[");
			criteria = Arrays.asList(criteriaArray);
			result.addResult(criteria);
			for (String c : criteria) {
				result.addMessage(c);
			}

		} else if (command.equals(APPLY_SET)) {
			String setName;
			CyNetwork network;
			String mapTo;
			List<String> labels;
			List<String> expressions;
			String[] colorsA = new String[0];
			boolean createFlag;

			Object sn = getArg(command, ARG_SETNAME, args);
			if (sn instanceof String) {
				setName = (String) sn;
			} else
				throw new CyCommandException("unknown type for set name");

			Object mt = getArg(command, ARG_MAP_TO, args);
			if (mt instanceof String) {
				mapTo = (String) mt;
			} else {
				// default to "Node Color"
				mapTo = "Node Color";
			}
			Object n = getArg(command, ARG_NETWORK, args);
			if (n instanceof CyNetwork) {
				network = (CyNetwork) n;
			} else if (n instanceof String) {
				network = Cytoscape.getNetwork((String) n);
			} else {
				// default to current network
				network = Cytoscape.getCurrentNetwork();
			}
			Object ll = getArg(command, ARG_LABEL_LIST, args);
			if (ll instanceof List) {
				labels = (List<String>) ll;
			} else if (ll instanceof String) {
				labels = new ArrayList<String>();
				// remove brackets, if they are there
				if (((String) ll).startsWith("[")
						&& ((String) ll).endsWith("]"))
					ll = ((String) ll).substring(1, ((String) ll).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) ll).split(",");
				for (String item : list) {
					if (labels.contains(item))
						throw new CyCommandException("Must have unique labels");

					labels.add(item);
				}
			} else
				throw new CyCommandException("unknown type for labels");

			Object el = getArg(command, ARG_EXP_LIST, args);
			if (el instanceof List) {
				expressions = (List<String>) el;
			} else if (el instanceof String) {
				expressions = new ArrayList<String>();
				// remove brackets, if they are there
				if (((String) el).startsWith("[")
						&& ((String) el).endsWith("]"))
					el = ((String) el).substring(1, ((String) el).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) el).split(",");
				for (String item : list) {
					expressions.add(item);
				}
			} else
				throw new CyCommandException("unknown type for expressions");

			Object cl = getArg(command, ARG_COLOR_LIST, args);
			if (cl instanceof String) {
				// remove brackets, if they are there
				if (((String) cl).startsWith("[")
						&& ((String) cl).endsWith("]"))
					cl = ((String) cl).substring(1, ((String) cl).length() - 1);
				// parse at comma and/or space delimiters
				colorsA = ((String) cl).split("[\\s,]+");
			} else
				throw new CyCommandException("unknown type for color list");

			/*
			 * Proceed to create/apply criteria set
			 */

			// //First, check if it already exists
			// CyAttributes na = Cytoscape.getNetworkAttributes();
			// List<String> setList =
			// na.getListAttribute(network.getIdentifier(),
			// CriteriaCommandHandler.PROPERTY_SETS);
			// if (null == setList) {
			// setList = new ArrayList<String>();
			// }
			// if (!setName.equals("")) {
			// if (!setList.contains(setName)) {
			// // then, add set name
			// setList.add(setName);
			//					
			// // and construct criteria list
			// ArrayList<String> critList = new ArrayList<String>();
			// critList.add(mapTo);
			// for (int k = 0; k < labels.size(); k++) {
			// critList.add(expressions.get(k) + ":" + labels.get(k) + ":"
			// + CriteriaTablePanel.colorToString(colors.get(k)));
			// }
			// System.out.println("CREATE SETTINGS: " + critList);
			// na.setListAttribute(network.getIdentifier(),
			// CriteriaCommandHandler.PROPERTY_SET_PREFIX + setName,
			// critList);
			// }
			// position set name in front to indicate latest mapping
			// choice
			// java.util.Collections
			// .swap(setList, 0, setList.indexOf(setName));
			// na.setAttribute(network.getIdentifier(),
			// CriteriaCommandHandler.NET_ATTR_APPLIED_SET, setName);
			// }
			// Now, calculate and apply
			String compositeLabel = "";
			if (setName.equals(""))
				throw new CyCommandException("Must have a set name.");

			for (int i = 0; i < labels.size(); i++) {
				String label = setName + "_" + labels.get(i);
				String current = expressions.get(i);
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
						compositeLabel = setName + ":composite";
						// if (!label.equals("") && label != null) {
						// compositeLabel = compositeLabel + ":" + label;
						// }
					}
					// evaluates expression and sets node attribute
					calculate.evaluateLeftToRight(label);
				}
			}

			String[] labelsA = new String[labels.size()];
			for (int h = 0; h < labels.size(); h++) {
				labelsA[h] = setName + "_" + labels.get(h);
			}

			try {
				attManager.setCompositeAttribute(compositeLabel, labelsA, colorsA);
			} catch (Exception e) {
				System.out.println("COMPOSITE FAILED!! " + e.getMessage());
			}

			if (labels.size() == 1) {
				mapper.createDiscreteMapping(network, setName, labelsA[0],
						colorsA[0], mapTo);
			} else {
				mapper.createCompositeMapping(network, setName, compositeLabel,
						colorsA, mapTo);
			}
			result.addMessage("Success!");

		} else {
			result.addError("Command not supported: " + command);
		}

		return (result);

	}

	/**
	 * Tell Workspaces to update criteria set info. Called when loading, saving
	 * or deleting a set via the UI. Workspaces will update CyCriteriaset
	 * objects as well as panel display and selection accordingly.
	 * 
	 * @param setname
	 */
	public static void updateWorkspaces(String setname) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(ARG_SETNAME, setname);
		try {
			CyCommandManager.execute(WORKSPACES, UPDATE_CRITERIASETS, args);
		} catch (CyCommandException cce) {
			// TODO Auto-generated catch block
			cce.printStackTrace();
		} catch (RuntimeException cce) {
			// TODO Auto-generated catch block
			cce.printStackTrace();
		}
	}

}
