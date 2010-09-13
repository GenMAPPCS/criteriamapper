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

import java.util.Collection;
import java.util.Map;

import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandNamespace;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;

public class CriteriaCommandHandler extends AbstractCommandHandler {

	public final static String NAMESPACE = "criteriaMapper";

	// Commands and associated args
	public static final String OPEN = "open dialog";
	public static final String LIST_SETS = "list sets";

	public static final String LIST_CRITERIA = "list criteria";
	public static final String DELETE_SET = "delete set";
	public static final String CREATE_SET = "create set";
	public static final String ARG_SETNAME = "setname";

	
	public static final String APPLY_SET = "apply set";
	public static final String ARG_NETWORK = "network";

	public CriteriaMapperDialog settingsDialog;

	public CriteriaCommandHandler() {
		super(CyCommandManager.reserveNamespace(NAMESPACE));

		addDescription(OPEN, "Open set dialog");
		addArgument(OPEN);

		addDescription(LIST_SETS, "List existing criteria sets");
		addArgument(LIST_SETS);

		addDescription(LIST_CRITERIA, "List criteria for a given set");
		addArgument(LIST_CRITERIA, ARG_SETNAME);

		addDescription(DELETE_SET, "Delete a set");
		addArgument(DELETE_SET, ARG_SETNAME);

		addDescription(CREATE_SET, "Create a new set");
		addArgument(CREATE_SET, ARG_SETNAME);

		addDescription(APPLY_SET, "Apply set to a network");
		addArgument(APPLY_SET, ARG_SETNAME);
		addArgument(APPLY_SET, ARG_NETWORK);

	}

	public CyCommandResult execute(String command, Collection<Tunable> args)
			throws CyCommandException {
		return execute(command, createKVMap(args));
	}

	public CyCommandResult execute(String command, Map<String, Object> args)
			throws CyCommandException {

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

		} else if (command.equals(LIST_CRITERIA)) {
			String set = getArg(command, ARG_SETNAME, args);
			if (null == set)
				throw new CyCommandException(
						"List criteria requires a set name");

			// do the thing
		}
		return null;
	}

}
