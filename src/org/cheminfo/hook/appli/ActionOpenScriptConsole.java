package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ActionOpenScriptConsole extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2910076324196334403L;
	AllInformation allInformation = null;
	
	public ActionOpenScriptConsole(AllInformation allInformation) {
		super("Open script console");
		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Save as xml format");
	}

	public void actionPerformed(ActionEvent e) {
		ScriptWindow window = new ScriptWindow(this.allInformation);
		window.setVisible(true);
	}

}
