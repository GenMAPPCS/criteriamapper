/*
 Copyright 2010 Alexander Pico
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 	
 	http://www.apache.org/licenses/LICENSE-2.0 
 	
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License. 
 */

package org.genmapp.criteriamapper;

/* 
 * Steve Federowicz 
 * Google Summer of Code
 * 
 * This is the driver class for the entire program.  Essentially it just adds the Boolean Mapper
 * to the plugins menu and sets up an action command to fire the dialog window in BooleanSettingsDialog.
 * 
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cytoscape.Cytoscape;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandHandler;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandNamespace;
import cytoscape.logger.CyLogger;
import cytoscape.plugin.CytoscapePlugin;

public class CriteriaMapper extends CytoscapePlugin {

	private JMenuItem item = new JMenuItem("Criteria Mapper");
	public CriteriaMapperDialog settingsDialog;
	protected CyCommandHandler cch;

	private CyLogger logger = CyLogger.getLogger(CriteriaMapper.class);
	
	public CriteriaMapper() {

		item.addActionListener(new CriteriaMapperCommandListener());
		JMenu pluginMenu = Cytoscape.getDesktop().getCyMenus().getMenuBar()
				.getMenu("Plugins");
		pluginMenu.add(item);

		// register cycommands
		try {
			// You must reserve your namespace first
			CyCommandNamespace ns = CyCommandManager
					.reserveNamespace("criteria mapper");
			// Now register handlers
			cch = new CriteriaCommandHandler(ns);
		} catch (RuntimeException e) {
			// Handle already registered exceptions
			System.out.println(e);
		}

	}



	class CriteriaMapperCommandListener implements ActionListener {
		// BooleanAlgorithm alg = null;

		public CriteriaMapperCommandListener() {
			// this.alg = algorithm;
		}

		public void actionPerformed(ActionEvent e) {
			// if (alg != null) {
			// Create the dialog
			try {
				cch.execute(CriteriaCommandHandler.OPEN, new HashMap());
			} catch (CyCommandException e1) {
				logger.warning(e1.getMessage());
			}
		}
	}
}
