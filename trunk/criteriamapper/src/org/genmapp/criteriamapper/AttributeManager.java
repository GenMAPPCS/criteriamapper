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
 * This class contains all of the code for getting, setting, and removing attributes.  
 */

import giny.model.Node;

import java.util.ArrayList;
import java.util.List;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

public class AttributeManager {
	private CyAttributes networkAttributes;
	private static CyAttributes nodeAttributes;
	// private SortedSet<String> criteriaSetNames = null;
	private List<String> criteriaSetNames = new ArrayList<String>();
	public static final String NET_ATTR_SETS = "org.genmapp.criteriasets_1.0";
	public static final String NET_ATTR_SET_PREFIX = "org.genmapp.criteriaset.";

	public AttributeManager() {
		networkAttributes = Cytoscape.getNetworkAttributes();
		nodeAttributes = Cytoscape.getNodeAttributes();
		// criteriaSetNames.add("");
		// criteriaSetNames.add("New...");
		getAllAttributes();
	}

	/*
	 * The names attribute refers to a list of 'Criteria Set names'. A 'Criteria
	 * Set Name' is a name which identifies a session that can be saved when
	 * creating criteria using the criteria builder.
	 */
	public void addNamesAttribute(CyNetwork network, String setName) {
		networkAttributes = Cytoscape.getNetworkAttributes();
		if (networkAttributes.hasAttribute(network.getIdentifier(), NET_ATTR_SETS)) {
			criteriaSetNames = (ArrayList<String>) networkAttributes
					.getListAttribute(network.getIdentifier(), NET_ATTR_SETS);
		} else {
			criteriaSetNames.add(setName);
		}

		if (!(criteriaSetNames.contains(setName))) {
			criteriaSetNames.add(setName);
		}

		networkAttributes.setListAttribute(network.getIdentifier(), NET_ATTR_SETS,
				criteriaSetNames);
	}

	public String[] getNamesAttribute(CyNetwork network) {
		if (networkAttributes.hasAttribute(network.getIdentifier(), NET_ATTR_SETS)) {
			String[] a = {""};
			List<String> temp = (ArrayList<String>) networkAttributes
					.getListAttribute(network.getIdentifier(), NET_ATTR_SETS);
			List<String> full = new ArrayList<String>();
			full.add("New...");
			for (String s : temp) {
				full.add(s);
			}
			return full.toArray(a);
		} else {

			return new String[]{"New..."};
		}
	}

	public void removeNamesAttribute(CyNetwork network, String setName) {
		criteriaSetNames = (ArrayList<String>) networkAttributes
				.getListAttribute(network.getIdentifier(), NET_ATTR_SETS);
		criteriaSetNames.remove(setName);
		if (criteriaSetNames.size() == 0) { // removed last set
			networkAttributes.deleteAttribute(NET_ATTR_SETS);
		} else {
			networkAttributes.setListAttribute(network.getIdentifier(),
					NET_ATTR_SETS, criteriaSetNames);
		}
		networkAttributes.deleteAttribute(NET_ATTR_SET_PREFIX + setName);
	}

	public void setNamesAttribute(CyNetwork network, String[] setNames) {
		networkAttributes = Cytoscape.getNetworkAttributes();
		List<String> temp = new ArrayList<String>();
		for (int i = 0; i < setNames.length; i++) {
			temp.add(setNames[i]);
		}
		networkAttributes.setListAttribute(network.getIdentifier(), NET_ATTR_SETS,
				temp);
	}

	/*
	 * The values attribute refers to a list of colon separated Strings. The
	 * values separated by the colons are the criteria, label, and color
	 * respectively. Any number of values, or colon separated strings can be
	 * associated with a setName or 'Criteria Set Name' attribute.
	 */
	public void setValuesAttribute(String setName, String mapTo,
			String[] criteriaLabelColor) {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(mapTo);
		for (int i = 0; i < criteriaLabelColor.length; i++) {
			temp.add(criteriaLabelColor[i]);
		}
		networkAttributes = Cytoscape.getNetworkAttributes();
		networkAttributes.setListAttribute(Cytoscape.getCurrentNetwork()
				.getIdentifier(), NET_ATTR_SET_PREFIX + setName, temp);

	}

	public void setColorAttribute(String label, String nodeID, Boolean outcome) {
		nodeAttributes.setAttribute(nodeID, label, outcome);
		nodeAttributes = Cytoscape.getNodeAttributes();
	}

	// public int getCompositeAttribute(String nodeID, String compositeName){
	// nodeAttributes.getIntegerAttribute(nodeID, compositeName);

	// }

	/*
	 * This method is responsible for assessing the hierarchy of criteria. It
	 * creates a composite node attribute and assigns an integer corresponding
	 * the to criteria (i.e., row number) that both true and highest ranking. If
	 * a node fails all criteria, then the value is set to -1.
	 */
	public void setCompositeAttribute(String[] labels) throws Exception {
		if (labels.length <= 1) {
			// only a single criteria; skip composition
			return;
		}

		CyNetwork network = Cytoscape.getCurrentNetwork();
		List<Node> nodesList = network.nodesList();
		String compositeName = labels[0];
		for (int k = 1; k < labels.length; k++) {
			if (labels[k].equals("")) {
				continue;
			}
			compositeName = compositeName + ":" + labels[k];
		}
		for (int i = 0; i < nodesList.size(); i++) {
			Node node = nodesList.get(i);
			String nodeID = node.getIdentifier();

			// initialize value to all false (-1)
			nodeAttributes.setAttribute(nodeID, compositeName, -1);

			// loop through each label in order
			for (int j = 0; j < labels.length; j++) {
				if (labels[j].equals("")) {
					continue;
				}
				if (!(nodeAttributes.hasAttribute(nodeID, labels[j]))) {
					throw new Exception("ITERATION: " + j
							+ " Node Attribute for node " + nodeID + " at "
							+ labels[j] + " has not been calculated");
				}

				// if true, then mark label row position and skip to next node.
				if (nodeAttributes.getBooleanAttribute(nodeID, labels[j])) {

					nodeAttributes.setAttribute(nodeID, compositeName, j);
					break; // next node

				}
				// update reference to node attributes
				nodeAttributes = Cytoscape.getNodeAttributes();
			}
		}
	}

	public boolean isCompositeAttribute(String compositeName) {
		getAllAttributes();
		if (attributeList.contains(compositeName)) {
			return true;
		} else {
			return false;
		}
	}

	public void removeCompositeAttribute(String compositeName) {
		nodeAttributes.deleteAttribute(compositeName);
	}

	public String[] getValuesAttribute(CyNetwork network, String setName) {
		String[] a = {};
		List<String> temp = (List<String>) networkAttributes
				.getListAttribute(network.getIdentifier(), NET_ATTR_SET_PREFIX + setName);
		if (temp != null) {
			return temp.toArray(a);
		} else {
			return a;
		}
	}

	public static boolean getColorAttribute(String nodeID, String label) {
		if (nodeAttributes.hasAttribute(nodeID, label)) {
			return nodeAttributes.getBooleanAttribute(nodeID, label);
		}
		return false;
	}

	public void removeColorAttribute(String label) throws Exception {
		if (!nodeAttributes.deleteAttribute(label)) {
			throw new Exception("Could not delete Attribute");
		}

	}

	ArrayList<String> attributeList;

	public String[] getAllAttributes() {
		// Create the list by combining node and edge attributes into a single
		// list
		attributeList = new ArrayList<String>();
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

		for (int i = 0; i < names.length; i++) {
			if (attributes.getType(names[i]) == CyAttributes.TYPE_FLOATING
					|| attributes.getType(names[i]) == CyAttributes.TYPE_INTEGER
					|| attributes.getType(names[i]) == CyAttributes.TYPE_BOOLEAN) {

				/*
				 * for(int j = 0; j < names[i].length(); j++){ String temp =
				 * names[i].charAt(j) + ""; if(temp.matches(" ")){ names[i] =
				 * names[i].substring(0,j) + "-" + names[i].substring(j+2,
				 * names[i].length()); } }
				 */

				attributeList.add(names[i]);
			}
		}

	}
}
