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

import java.util.Collection;
import java.util.Map;

import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandNamespace;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;

public class CriteriaCommandHandler extends AbstractCommandHandler {

	//args
	public static final String SETNAME = "setname";
	
	//commands
	public static final String OPEN = "open";
	public static final String LIST_SETS = "list sets";
	public static final String LIST_CRITERIA = "list criteria";
	public static final String DELETE_SET = "delete set";
	public static final String SAVE_SET = "save set";
	
	public CriteriaMapperDialog settingsDialog;
	
	public CriteriaCommandHandler(CyCommandNamespace ns) {
		super(ns);
		
		addDescription(OPEN, "blah blah");
		addArgument(OPEN);
		
		addDescription(LIST_SETS, "blah blah");
		addArgument(LIST_SETS);
		
		addDescription(LIST_CRITERIA, "blah blah");
		addArgument(LIST_CRITERIA, SETNAME);
		
		addDescription(SAVE_SET, "blah blah");
		addArgument(SAVE_SET, SETNAME);
		
		
	}

	public CyCommandResult execute(String command, Map<String, Object> args)
			throws CyCommandException {
		
		if(command.equals(OPEN)){
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

		} else if(command.equals(LIST_CRITERIA)){
			String set = getArg(command, SETNAME, args);
			if(null == set)	
				throw new CyCommandException("List criteria requires a set name");
			 
			// do the thing
		}
		return null;
	}

	public CyCommandResult execute(String arg0, Collection<Tunable> arg1)
			throws CyCommandException {
		// TODO Auto-generated method stub
		return null;
	}

}
