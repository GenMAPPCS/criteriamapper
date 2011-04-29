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
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.ObjectMapping;

public class ColorMapper {
	CyNetwork network;
	CyNetworkView networkView;
	String vsName;
	String attributeLabel;
	Color color;
	List<Node> nodes = null;

	// VisualMappingManager manager;
	// CalculatorCatalog catalog;
	// VisualStyle vs;

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

		this.network = net;
		this.networkView = Cytoscape.getNetworkView(net.getIdentifier());

		VisualStyle vs = networkView.getVisualStyle();

		DiscreteMapping disMapping = new DiscreteMapping(new Color(0),
				compositeLabel, ObjectMapping.NODE_MAPPING);
		// DiscreteMapping disMapping = new DiscreteMapping(Color.class,
		// compositeLabel);
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
					// CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
					// b = nodeAttributes.getBooleanAttribute(id, labels[j]);
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
			nodeColorCalculator = new BasicCalculator("Single Node Color Calc",
					disMapping, VisualPropertyType.NODE_FILL_COLOR);
		}

		if (nodeColorCalculator != null) {
			nodeAppCalc.setCalculator(nodeColorCalculator);
		}
		vs.setNodeAppearanceCalculator(nodeAppCalc);

		// Set the visual style
		Cytoscape.getCurrentNetworkView().setVisualStyle(vs.getName());
		Cytoscape.getCurrentNetworkView().applyVizmapper(vs);

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

		network = net;
		networkView = Cytoscape.getNetworkView(net.getIdentifier());

		// must set current view to retrieve visual style via vmm
		Cytoscape.setCurrentNetworkView(networkView.getIdentifier());
		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
		VisualStyle vs = vmm.getNetworkView().getVisualStyle();
		networkView.setVisualStyle(vs.getName()); // stabilize setting
		// this is important for wikipathways imports, for some reason...

		DiscreteMapping disMapping = new DiscreteMapping(new Color(0), label,
				ObjectMapping.NODE_MAPPING);
		// DiscreteMapping disMapping = new DiscreteMapping(Boolean.class,
		// label);

		disMapping.putMapValue(Boolean.TRUE, currentColor);
		disMapping.putMapValue(Boolean.FALSE, Color.WHITE);

		NodeAppearanceCalculator nodeAppCalc = vs.getNodeAppearanceCalculator();

		Calculator nodeColorCalculator = null;

		if (mapTo.equals("Node Color")) {
			nodeColorCalculator = new BasicCalculator("Single Node Color Calc",
					disMapping, VisualPropertyType.NODE_FILL_COLOR);

		} else {
			nodeColorCalculator = null;
		}

		nodeAppCalc.setCalculator(nodeColorCalculator);

		vs.setNodeAppearanceCalculator(nodeAppCalc);

		// Set the visual style
		Cytoscape.getCurrentNetworkView().setVisualStyle(vs.getName());
		Cytoscape.getCurrentNetworkView().applyVizmapper(vs);

		networkView.redrawGraph(true, true);

		return vs;
	}

	/*
	 * This method creates a single discreteMapping of the border color taking
	 * the label of an evaluated criteria and the color it should be mapped to.
	 */
	public VisualStyle createDiscreteBorderMapping(CyNetwork net,
			String vsName, String label, Color currentColor) {

		this.network = net;
		networkView = Cytoscape.getNetworkView(net.getIdentifier());

		VisualStyle vs = networkView.getVisualStyle();

		DiscreteMapping disMapping = new DiscreteMapping(Color.WHITE, label,
				ObjectMapping.NODE_MAPPING);

		// DiscreteMapping disMapping = new DiscreteMapping(Color.class, label);

		// disMapping.setControllingAttributeName(label, network, false);
		// System.out.println("hey "+currentColor);

		disMapping.putMapValue(Boolean.TRUE, currentColor);
		disMapping.putMapValue(Boolean.FALSE, Color.WHITE);

		NodeAppearanceCalculator nodeAppCalc = vs.getNodeAppearanceCalculator();
		// EdgeAppearanceCalculator edgeAppCalc =
		// vs.getEdgeAppearanceCalculator();
		// GlobalAppearanceCalculator globalAppCalc =
		// vs.getGlobalAppearanceCalculator();

		Calculator nodeColorCalculator = new BasicCalculator(
				"Single Node Border Color Calc", disMapping,
				VisualPropertyType.NODE_BORDER_COLOR);

		nodeAppCalc.setCalculator(nodeColorCalculator);

		vs.setNodeAppearanceCalculator(nodeAppCalc);

		// Set the visual style
		Cytoscape.getCurrentNetworkView().setVisualStyle(vs.getName());
		Cytoscape.getCurrentNetworkView().applyVizmapper(vs);

		networkView.redrawGraph(true, true);

		return vs;
	}

	public VisualStyle createContinuousMapping(CyNetwork network, String label,
			Color currentColor, boolean update) {

		boolean edge = false;

		VisualStyle vs = networkView.getVisualStyle();

		// Get the right mapping, depending on whether we are mapping an edge or
		// a node
		byte mapping = ObjectMapping.NODE_MAPPING;
		VisualPropertyType vizType = VisualPropertyType.NODE_FILL_COLOR;
		if (edge) {
			mapping = ObjectMapping.EDGE_MAPPING;
			vizType = VisualPropertyType.EDGE_COLOR;
		}

		// Create the new continuous mapper
		ContinuousMapping colorMapping = new ContinuousMapping(currentColor,
				mapping);
		colorMapping.setControllingAttributeName(label, network, false);

		// ContinuousMapping colorMapping = new ContinuousMapping(Color.class,
		// label);

		colorMapping.setInterpolator(new LinearNumberToColorInterpolator());

		/*
		 * colorMapping.addPoint (minValue, new BoundaryRangeValues (downColor,
		 * downColor, downColor)); colorMapping.addPoint (minValue/100.0, new
		 * BoundaryRangeValues (downDeltaColor, downDeltaColor,
		 * downDeltaColor)); colorMapping.addPoint(0, new BoundaryRangeValues
		 * (zeroColor, zeroColor, zeroColor));
		 * colorMapping.addPoint(maxValue/100.0, new BoundaryRangeValues
		 * (upDeltaColor, upDeltaColor, upDeltaColor));
		 * colorMapping.addPoint(maxValue, new BoundaryRangeValues (upColor,
		 * upColor, upColor));
		 */

		Calculator colorCalculator = new BasicCalculator(
				"TreeView Color Calculator", colorMapping, vizType);

		// Apply it
		if (edge) {
			EdgeAppearanceCalculator edgeAppCalc = vs
					.getEdgeAppearanceCalculator();
			edgeAppCalc.setCalculator(colorCalculator);
			vs.setEdgeAppearanceCalculator(edgeAppCalc);
		} else {
			NodeAppearanceCalculator nodeAppCalc = vs
					.getNodeAppearanceCalculator();
			nodeAppCalc.setCalculator(colorCalculator);
			vs.setNodeAppearanceCalculator(nodeAppCalc);
		}

		// Set the visual style
		Cytoscape.getCurrentNetworkView().setVisualStyle(vs.getName());
		Cytoscape.getCurrentNetworkView().applyVizmapper(vs);

		return vs;
	}

}
