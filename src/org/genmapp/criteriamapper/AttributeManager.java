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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;

public class AttributeManager {
	private static CyAttributes nodeAttributes;

	public AttributeManager() {
		nodeAttributes = Cytoscape.getNodeAttributes();
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

	}

	/**
	 * The values attribute refers to a list of double-colon separated Strings.
	 * The values separated by double-colons are criteria, label, and color,
	 * respectively. Any number of values, or double-colon separated strings,
	 * can be associated with a setName or 'Criteria Set Name' attribute.
	 * 
	 * @param setName
	 * @param mapTo
	 * @param criteriaLabelColor
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

		/*
		 * Clear all setname nodeAttributes to clean up old, deleted and renamed
		 * criteria. This method is followed by a fresh call to
		 * calcNodeAttributes() to restore a new set.
		 */
		for (String attrname : nodeAttributes.getAttributeNames()){
			if (attrname.startsWith(setName+"_"))
				nodeAttributes.deleteAttribute(attrname);
		}
	}

	/**
	 * @param label
	 * @param nodeID
	 * @param outcome
	 */
	public void setColorAttribute(String label, String nodeID, String outcome) {
		nodeAttributes.setUserEditable(label, false);
		nodeAttributes.setUserVisible(label, true);
		nodeAttributes.setAttribute(nodeID, label, outcome);
		nodeAttributes = Cytoscape.getNodeAttributes();
	}

	/**
	 * This method is responsible for assessing the hierarchy of criteria. It
	 * creates a composite node attribute and assigns the color (e.g.,
	 * "#ff0000") corresponding to the criteria (i.e., row number) that both
	 * true and highest ranking. If a node fails all criteria, then the value is
	 * set to "null".
	 * 
	 * @param labels
	 * @throws Exception
	 */
	public void setCompositeAttribute(String compositeLabel, String[] labels,
			String[] colors) throws Exception {
		if (labels.length <= 1) {
			// only a single criteria; skip composition
			return;
		}
		// update reference to node attributes
		nodeAttributes = Cytoscape.getNodeAttributes();

		List<Node> nodesList = Cytoscape.getCyNodesList();
		String compositeName = compositeLabel;

		// set attr to hidden
		nodeAttributes.setUserVisible(compositeName, true);
		nodeAttributes.setUserEditable(compositeName, false);

		for (int i = 0; i < nodesList.size(); i++) {
			Node node = nodesList.get(i);
			String nodeID = node.getIdentifier();

			// initialize default value to null
			nodeAttributes.setAttribute(nodeID, compositeName, "null");

			/*
			 * Loop through each label from top to bottom, giving priority based
			 * on position and value of "true".
			 */
			for (int j = 0; j < labels.length; j++) {
				if (labels[j].equals("")) {
					continue;
				}
				if (!(nodeAttributes.hasAttribute(nodeID, labels[j]))) {
					continue;
				}
				if (nodeAttributes.getStringAttribute(nodeID, labels[j])
						.equals("null")) {
					continue;
				}

				/*
				 * By this point the value is either "true" or "false". If true,
				 * then mark label row position and skip to next node!
				 */
				if (Boolean.valueOf(nodeAttributes.getStringAttribute(nodeID,
						labels[j]))) {

					nodeAttributes.setAttribute(nodeID, compositeName,
							colors[j]);
					break; // next node

				} else {
					/*
					 * If top position is false, then mark it; otherwise leave
					 * it to null default
					 */
					if (j == 0)
						nodeAttributes.setAttribute(nodeID, compositeName,
								"false");
				}

			}
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
			return Boolean.valueOf(nodeAttributes.getStringAttribute(nodeID,
					label));
		}
		return false;
	}

	public void removeColorAttribute(String label) throws Exception {
		if (!nodeAttributes.deleteAttribute(label)) {
			throw new Exception("Could not delete Attribute");
		}

	}

}
