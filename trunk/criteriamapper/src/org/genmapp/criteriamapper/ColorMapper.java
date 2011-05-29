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
 * This class contains all of the methods responsible for coloring nodes in the graph.  It also contains methods for coloring node border color.
 * The methods in this class are called by BooleanSettingsDialog and CriteriaTablePanel.  The coloring methods are called by BooleanSettingsDialog 
 * user hits 'apply' and by CriteriaTablePanel any time a selection in the table is made.  
 * 
 */
import giny.model.Node;
import giny.view.NodeView;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;

public class ColorMapper {
	CyNetwork network;
	CyNetworkView networkView;
	String vsName;
	String attributeLabel;
	Color color;
	List<Node> nodes = null;

	public ColorMapper() {

	}

	/*
	 * This method creates the CompositeMapping, or mapping of multiple colors
	 * at once in the graph. It takes as arguments a compositeLabel or colon
	 * separated String and an array of Colors. Each of the values in the comma
	 * separated list is the name of an attribute that holds the values of the
	 * outcome of an individually evaluated criteria. Each of the comma
	 * separated values is then a label name in the order it appears in the
	 * table in the actual GUI. The Colors are in an array parallel to this and
	 * are subsequently mapped.
	 */
	public VisualStyle createCompositeMapping(CyNetwork net, String vsName,
			String compositeLabel, Color[] colors, String mapTo) {
		boolean newStyle = false;
		
		if (!compositeLabel.contains(":")) {
			return createDiscreteMapping(net, vsName, compositeLabel,
					colors[0], mapTo);
		}

		networkView = Cytoscape.getNetworkView(net.getIdentifier());

		// must set current view to retrieve visual style via vmm
		Cytoscape.setCurrentNetworkView(networkView.getIdentifier());

		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = vmm.getCalculatorCatalog();
		/*
		 * Create new style base on current visual style and append setName
		 */
		VisualStyle vs;
		VisualStyle vsOld = (VisualStyle) networkView.getVisualStyle();
		String[] baseName = vsOld.getName().split("__");
		String newVsName = baseName[0] + "__" + vsName;
		vs = catalog.getVisualStyle(newVsName);
		if (null == vs) {
			vs = new VisualStyle(vsOld, newVsName);
			catalog.addVisualStyle(vs);
		}

		DiscreteMapping disMapping = new DiscreteMapping(Color.class,
				compositeLabel);

		disMapping.putMapValue(new Integer(-1), Color.WHITE);

		List<Integer> rowList = new ArrayList<Integer>();
		boolean doCalc = false;

		// collect colors intended for Node Color into disMapping
		for (int i = 0; i < colors.length; i++) {
			disMapping.putMapValue(new Integer(i), colors[i]);
			doCalc = true;
		}

		NodeAppearanceCalculator nodeAppCalc = vs.getNodeAppearanceCalculator();

		Calculator nodeColorCalculator = null;

		if (rowList.size() > 0) {
			Map<String, List<Color>> nodeMap = new HashMap<String, List<Color>>();
			String[] labels = compositeLabel.split(":");
			for (Integer j : rowList) {
				List<Node> nodeList = network.nodesList();
				for (Node node : nodeList) {
					String id = node.getIdentifier();
					boolean b = AttributeManager.getColorAttribute(id,
							labels[j]);
					Color c = colors[j];
					if (!b)
						c = Color.white; // TODO: set to default node color
					List<Color> cl = nodeMap.get(id);
					if (null == cl)
						cl = new ArrayList<Color>();
					cl.add(c);
					nodeMap.put(id, cl);
				}
			}
		}

		if (doCalc) {
			nodeColorCalculator = new BasicCalculator(vs.getName()
					+ "-Node Color-Discrete Mapper", disMapping,
					VisualPropertyType.NODE_FILL_COLOR);
		}

		if (nodeColorCalculator != null) {
			nodeAppCalc.setCalculator(nodeColorCalculator);
		}
		vs.setNodeAppearanceCalculator(nodeAppCalc);

		// Set the visual style
		vmm.setVisualStyle(vs);
		vmm.applyAppearances();
		networkView.setVisualStyle(newVsName);

		networkView.redrawGraph(true, true);

		return vs;
	}

	public void clearNetwork(CyNetwork network) {

		CyNetworkView networkView = Cytoscape.getNetworkView(network
				.getIdentifier());
		List<Node> nodeList = network.nodesList();
		for (Node node : nodeList) {

			NodeView nodeView = networkView.getNodeView(node);
			Paint p = Color.WHITE;
			nodeView.setUnselectedPaint(p);

		}
		networkView.updateView();
	}

	/*
	 * This method creates a single discreteMapping taking the label of an
	 * evaluated criteria and the color it should be mapped to.
	 */
	public VisualStyle createDiscreteMapping(CyNetwork net, String vsName,
			String label, Color currentColor, String mapTo) {
		
		// must set current view to retrieve visual style via vmm
		Cytoscape.setCurrentNetwork(net.getIdentifier());
		networkView = Cytoscape.getNetworkView(net.getIdentifier());
		Cytoscape.setCurrentNetworkView(networkView.getIdentifier());

		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = vmm.getCalculatorCatalog();
		/*
		 * Create new style base on current visual style and append setName
		 */
		VisualStyle vs;
		VisualStyle vsOld = (VisualStyle) networkView.getVisualStyle();
		String[] baseName = vsOld.getName().split("__");
		String newVsName = baseName[0] + "__" + vsName;
		vs = catalog.getVisualStyle(newVsName);
		if (null == vs) {
			vs = new VisualStyle(vsOld, newVsName);
			catalog.addVisualStyle(vs);
		}

		DiscreteMapping disMapping = new DiscreteMapping(Color.class, label);

		disMapping.putMapValue(Boolean.TRUE, currentColor);
		disMapping.putMapValue(Boolean.FALSE, Color.WHITE);

		NodeAppearanceCalculator nodeAppCalc = vs.getNodeAppearanceCalculator();

		Calculator nodeColorCalculator = null;

		if (mapTo.equals("Node Color")) {
			nodeColorCalculator = new BasicCalculator(vs.getName()
					+ "-Node Color-Discrete Mapper", disMapping,
					VisualPropertyType.NODE_FILL_COLOR);

		} else {
			nodeColorCalculator = null;
		}

		nodeAppCalc.setCalculator(nodeColorCalculator);

		vs.setNodeAppearanceCalculator(nodeAppCalc);

		// Set the visual style
		vmm.setVisualStyle(vs);
		vmm.applyAppearances();
		networkView.setVisualStyle(newVsName);

		networkView.redrawGraph(true, true);

		return vs;
	}

	/*
	 * This method creates a single discreteMapping of the border color taking
	 * the label of an evaluated criteria and the color it should be mapped to.
	 */
	public VisualStyle createDiscreteBorderMapping(CyNetwork net,
			String vsName, String label, Color currentColor) {

		networkView = Cytoscape.getNetworkView(net.getIdentifier());

		// must set current view to retrieve visual style via vmm
		Cytoscape.setCurrentNetworkView(networkView.getIdentifier());

		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = vmm.getCalculatorCatalog();
		/*
		 * Create new style base on current visual style and append setName
		 */
		VisualStyle vs;
		VisualStyle vsOld = (VisualStyle) networkView.getVisualStyle();
		String[] baseName = vsOld.getName().split("__");
		String newVsName = baseName[0] + "__" + vsName;
		vs = catalog.getVisualStyle(newVsName);
		if (null == vs) {
			vs = new VisualStyle(vsOld, newVsName);
			catalog.addVisualStyle(vs);
		}

		DiscreteMapping disMapping = new DiscreteMapping(Color.class, label);

		disMapping.putMapValue(Boolean.TRUE, currentColor);
		disMapping.putMapValue(Boolean.FALSE, Color.WHITE);

		NodeAppearanceCalculator nodeAppCalc = vs.getNodeAppearanceCalculator();

		Calculator nodeColorCalculator = new BasicCalculator(vs.getName()
				+ "-Node Color-Discrete Mapper", disMapping,
				VisualPropertyType.NODE_BORDER_COLOR);

		nodeAppCalc.setCalculator(nodeColorCalculator);

		vs.setNodeAppearanceCalculator(nodeAppCalc);

		// Set the visual style
		vmm.setVisualStyle(vs);
		vmm.applyAppearances();
		networkView.setVisualStyle(newVsName);

		networkView.redrawGraph(true, true);

		return vs;
	}

}
