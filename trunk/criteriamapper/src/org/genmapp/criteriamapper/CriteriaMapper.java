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
import java.util.Collection;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cytoscape.Cytoscape;
import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandHandler;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandNamespace;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;
import cytoscape.plugin.CytoscapePlugin;



public class CriteriaMapper extends CytoscapePlugin {
	
	private JMenuItem item = new JMenuItem("Criteria Mapper");
	
	public CriteriaMapper() {
		
		
		item.addActionListener(new CriteriaMapperCommandListener());		
		JMenu pluginMenu = Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("Plugins");	
		pluginMenu.add(item);
		
		// register cycommands
		try {
			// You must reserve your namespace first
			CyCommandNamespace ns = CyCommandManager
					.reserveNamespace("criteria mapper");
			// Now register this handler as handling "open"
			CyCommandHandler oh = new OpenCommandHandler(ns);
		} catch (RuntimeException e) {
			// Handle already registered exceptions
			System.out.println(e);
		}
		
	}

	class OpenCommandHandler extends AbstractCommandHandler {
		protected OpenCommandHandler(CyCommandNamespace ns) {
			super(ns);
			addArgument("open");
		}

		public String getHandlerName() {
			return "open";
		}

		public CyCommandResult execute(String command, Map<String, Object> args)
				throws CyCommandException {
			// Create the dialog
			CriteriaMapperDialog  settingsDialog = new CriteriaMapperDialog();
			// Keep it on top and active?
			settingsDialog.setModal(false);
			// Pop it up
			settingsDialog.actionPerformed(null);
			return new CyCommandResult();
		}

		public CyCommandResult execute(String arg0, Collection<Tunable> arg1)
				throws CyCommandException {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
	
	class CriteriaMapperCommandListener implements ActionListener {
		//BooleanAlgorithm alg = null;

		public CriteriaMapperCommandListener() {
			//this.alg = algorithm;
		}

		public void actionPerformed(ActionEvent e) {
			//if (alg != null) {
				// Create the dialog
				CriteriaMapperDialog  settingsDialog = new CriteriaMapperDialog();
				// Keep it on top and active?
				settingsDialog.setModal(false);
				// Pop it up
				settingsDialog.actionPerformed(e);
			//} 
		}
	}
}

	


