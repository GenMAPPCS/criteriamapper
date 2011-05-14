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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;

public class AttributeManager {
	private CyAttributes networkAttributes;
	private static CyAttributes nodeAttributes;
	// private SortedSet<String> criteriaSetNames = null;
	private List<String> criteriaSetNames = new ArrayList<String>();

	public AttributeManager() {
		networkAttributes = Cytoscape.getNetworkAttributes();
		nodeAttributes = Cytoscape.getNodeAttributes();
		// criteriaSetNames.add("");
		// criteriaSetNames.add("New...");
		getAllAttributes();
	}

	public String[] getNamesAttributeForMenu() {
		String[] a = {};
		List<String> full = new ArrayList<String>();
		full.add("New...");
		String setList = CytoscapeInit.getProperties().getProperty(
				CriteriaCommandHandler.PROPERTY_SETS);
		if (null != setList) {
			// trim leading and trailing brackets
			setList = setList.substring(1, setList.length() - 1);
			a = setList.split("\\]\\[");
			for (String s : a) {
				full.add(s);
			}
		}
		return full.toArray(a);
	}

	public void removeSetFromProps(String setName) {

		// remove from cytoprefs
		String setList = CytoscapeInit.getProperties().getProperty(
				CriteriaCommandHandler.PROPERTY_SETS);
		if (null != setList) {
			// trim leading and trailing brackets
			setList = setList.replace("[" + setName + "]", "");
			if (setList.length() > 1)
				CytoscapeInit.getProperties().setProperty(
						CriteriaCommandHandler.PROPERTY_SETS, setList);
			else
				CytoscapeInit.getProperties().remove(
						CriteriaCommandHandler.PROPERTY_SETS);
		}

		CytoscapeInit.getProperties().remove(
				CriteriaCommandHandler.PROPERTY_SET_PREFIX + setName);

		//THIS SHOULD ALL BE HANDLED BY VISUAL STYLES, RATHER THAN NET ATTRS
		// remove from network attributes
		// networkAttributes
		// .deleteAttribute(CriteriaCommandHandler.NET_ATTR_APPLIED_SET);
		// criteriaSetNames = (ArrayList<String>) networkAttributes
		// .getListAttribute(network.getIdentifier(),
		// CriteriaCommandHandler.PROPERTY_SETS);
		// criteriaSetNames.remove(setName);
		// if (criteriaSetNames.size() == 0) { // removed last set
		// networkAttributes
		// .deleteAttribute(CriteriaCommandHandler.PROPERTY_SETS);
		// } else {
		// networkAttributes.setListAttribute(network.getIdentifier(),
		// CriteriaCommandHandler.PROPERTY_SETS, criteriaSetNames);
		// }
		// networkAttributes
		// .deleteAttribute(CriteriaCommandHandler.PROPERTY_SET_PREFIX
		// + setName);

	}

	public void setNameAttribute(String sn) {
		// set cyto prefs
		String sets = CytoscapeInit.getProperties().getProperty(
				CriteriaCommandHandler.PROPERTY_SETS);
		if (null == sets)
			sets = new String();
		if (!sets.contains(sn))
			sets = sets + "[" + sn + "]";
		CytoscapeInit.getProperties().setProperty(
				CriteriaCommandHandler.PROPERTY_SETS, sets);

		// set network attr
		// networkAttributes.setAttribute(Cytoscape.getCurrentNetwork()
		// .getIdentifier(), CriteriaCommandHandler.NET_ATTR_APPLIED_SET,
		// sn);
		// Set<CyNetwork> allNetworks = Cytoscape.getNetworkSet();
		// for (CyNetwork network : allNetworks) {
		// List<String> temp = networkAttributes.getListAttribute(network
		// .getIdentifier(), CriteriaCommandHandler.PROPERTY_SETS);
		// if (null == temp)
		// temp = new ArrayList<String>();
		// if (!temp.contains(sn))
		// temp.add(sn);
		// networkAttributes.setListAttribute(network.getIdentifier(),
		// CriteriaCommandHandler.PROPERTY_SETS, temp);
		// }
	}

	/*
	 * The values attribute refers to a list of colon separated Strings. The
	 * values separated by the colons are the criteria, label, and color
	 * respectively. Any number of values, or colon separated strings can be
	 * associated with a setName or 'Criteria Set Name' attribute.
	 */
	public void setValuesAttribute(String setName, String mapTo,
			String[] criteriaLabelColor) {
		String str = new String();
		ArrayList<String> list = new ArrayList<String>();
		str = "[" + mapTo + "]";
		list.add(mapTo);
		for (int i = 0; i < criteriaLabelColor.length; i++) {
			str = str + "[" + criteriaLabelColor[i] + "]";
			list.add(criteriaLabelColor[i]);
		}

		// update cyto prefs
		CytoscapeInit.getProperties().setProperty(
				CriteriaCommandHandler.PROPERTY_SET_PREFIX + setName, str);

		// // then update networks and nodes
		// Set<CyNetwork> allNetworks = Cytoscape.getNetworkSet();
		// for (CyNetwork network : allNetworks) {
		// // otherwise, just do the network
		// networkAttributes = Cytoscape.getNetworkAttributes();
		// List<String> cset = networkAttributes.getListAttribute(network
		// .getIdentifier(), CriteriaCommandHandler.PROPERTY_SETS);
		// cset.add(setName);
		// networkAttributes.setListAttribute(network.getIdentifier(),
		// CriteriaCommandHandler.PROPERTY_SETS, cset);
		// networkAttributes.setListAttribute(network.getIdentifier(),
		// CriteriaCommandHandler.PROPERTY_SET_PREFIX + setName, list);
		// }

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

		List<Node> nodesList = Cytoscape.getCyNodesList();
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

	public String[] getValuesAttribute(String setName) {
		String[] a = {};

		String setParameters = CytoscapeInit.getProperties().getProperty(
				CriteriaCommandHandler.PROPERTY_SET_PREFIX + setName);
		// trim leading and trailing brackets
		setParameters = setParameters.substring(1, setParameters.length() - 1);
		a = setParameters.split("\\]\\[");
		return a;

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
