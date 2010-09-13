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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.data.CyAttributes;
import cytoscape.layout.Tunable;

public class CriteriaCommandHandler extends AbstractCommandHandler {

	public final static String NAMESPACE = "criteriamapper";

	// Commands and associated args
	public static final String OPEN = "open dialog";
	public static final String LIST_SETS = "list sets";

	public static final String LIST_CRITERIA = "list criteria";
	public static final String DELETE_SET = "delete set";
	public static final String CREATE_SET = "create set";
	public static final String ARG_SETNAME = "setname";
	public static final String ARG_NETWORK = "network";
	public static final String ARG_MAP_TO = "mapto";
	public static final String ARG_LABEL_LIST = "labellist";
	public static final String ARG_EXP_LIST = "expressionlist";
	public static final String ARG_COLOR_LIST = "colorlist";

	public static final String APPLY_SET = "apply set";
	public static final String ARG_CREATE_FLAG = "createflag";

	public CriteriaMapperDialog settingsDialog;
	private CriteriaCalculator calculate = new CriteriaCalculator();
	private AttributeManager attManager = new AttributeManager();;
	private ColorMapper mapper = new ColorMapper();

	public CriteriaCommandHandler() {
		super(CyCommandManager.reserveNamespace(NAMESPACE));

		addDescription(OPEN, "Open set dialog");
		addArgument(OPEN);

		addDescription(LIST_SETS, "List existing criteria sets per network");
		addArgument(LIST_SETS);
		addArgument(LIST_SETS, ARG_NETWORK);

		addDescription(LIST_CRITERIA,
				"List criteria for a given set and network");
		addArgument(LIST_CRITERIA, ARG_SETNAME);
		addArgument(LIST_CRITERIA, ARG_NETWORK);

		addDescription(DELETE_SET, "Delete a set from a given network");
		addArgument(DELETE_SET, ARG_SETNAME);
		addArgument(DELETE_SET, ARG_NETWORK);

		addDescription(CREATE_SET, "Create a new set for a given network");
		addArgument(CREATE_SET, ARG_SETNAME);
		addArgument(CREATE_SET, ARG_NETWORK);
		addArgument(CREATE_SET, ARG_MAP_TO);
		addArgument(CREATE_SET, ARG_LABEL_LIST);
		addArgument(CREATE_SET, ARG_EXP_LIST);
		addArgument(CREATE_SET, ARG_COLOR_LIST);

		addDescription(APPLY_SET,
				"Apply set to a network, creating the set if indicated. ");
		addArgument(APPLY_SET, ARG_SETNAME);
		addArgument(APPLY_SET, ARG_NETWORK);
		addArgument(APPLY_SET, ARG_CREATE_FLAG);

	}

	public CyCommandResult execute(String command, Collection<Tunable> args)
			throws CyCommandException {
		return execute(command, createKVMap(args));
	}

	public CyCommandResult execute(String command, Map<String, Object> args)
			throws CyCommandException {

		CyCommandResult result = new CyCommandResult();

		if (command.equals(OPEN)) {
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
			// Pop it up
			settingsDialog.actionPerformed(null);

		} else if (command.equals(LIST_SETS)) {
			String setName;
			CyNetwork network = null;
			Object sn = getArg(command, ARG_SETNAME, args);
			if (sn instanceof String) {
				setName = (String) sn;
			} else {
				setName = null;
			}
			Object n = getArg(command, ARG_NETWORK, args);
			if (n instanceof CyNetwork) {
				network = (CyNetwork) n;
			} else if (n instanceof String) {
				network = Cytoscape.getNetwork((String) n);
			} else {
				network = null;
			}
			List<String> sets = new ArrayList<String>();
			sets = (List<String>) Cytoscape.getNetworkAttributes()
					.getListAttribute(network.getIdentifier(),
							AttributeManager.NET_ATTR);
			result.addResult(sets);
			for (String set : sets) {
				result.addMessage(set);
			}

		} else if (command.equals(CREATE_SET)) {
			String setName;
			CyNetwork network;
			String mapTo;
			List<String> labels;
			List<String> expressions;
			List<Color> colors;

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
			if (cl instanceof List) {
				if (((List<Object>) cl).get(0) instanceof Color)
					colors = (List<Color>) cl;
				else
					throw new CyCommandException(
							"color list not of type Color?");
			} else if (cl instanceof String) {
				colors = new ArrayList<Color>();
				// remove brackets, if they are there
				if (((String) cl).startsWith("[")
						&& ((String) cl).endsWith("]"))
					cl = ((String) cl).substring(1, ((String) cl).length() - 1);
				// parse at comma delimiters
				String[] list = ((String) cl).split(",");
				for (String colorString : list) {
					colors.add(Color.decode(colorString));
				}
			} else
				throw new CyCommandException("unknown type for color list");

			// now do it
			CyAttributes na = Cytoscape.getNetworkAttributes();
			List<String> setList = na.getListAttribute(network.getIdentifier(),
					AttributeManager.NET_ATTR);
			if (null == setList) {
				setList = new ArrayList<String>();
			}
			if (setList.contains(setName)) {
				throw new CyCommandException(setName
						+ " already exists for this network");
			}
			setList.add(setName);
			na.setListAttribute(network.getIdentifier(),
					AttributeManager.NET_ATTR, setList);

			// construct criteria set list
			ArrayList<String> critList = new ArrayList<String>();
			critList.add(mapTo);
			for (int k = 0; k < labels.size(); k++) {
				critList.add(expressions.get(k) + ":" + labels.get(k) + ":"
						+ CriteriaTablePanel.colorToString(colors.get(k)));
			}
			System.out.println("CREATE SETTINGS: " + critList);
			na.setListAttribute(network.getIdentifier(), setName, critList);

			// TODO: calculate and apply
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
						if (!label.equals("") && label != null) {
							compositeLabel = compositeLabel + ":" + label;
						}
					}
					// evaluates expression and sets node attribute
					calculate.evaluateLeftToRight(label);

					colors.add(colors.get(i));
				}
			}

			String[] labelsA = new String[labels.size()];
			for (int h = 0; h < labels.size(); h++) {
				labelsA[h] = setName + "_" + labels.get(h);
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

			if (labels.size() == 1) {
				mapper.createDiscreteMapping(setName, labelsA[0], colorsA[0],
						mapTo);
			} else {
				mapper.createCompositeMapping(setName, compositeLabel, colorsA,
						mapTo);
			}

		} else {
			result.addError("Command not supported: " + command);
		}

		return (result);

	}

}
