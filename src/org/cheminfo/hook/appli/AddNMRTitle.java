package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class AddNMRTitle extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6811349422194392380L;
	AllInformation allInformation;
	
	public AddNMRTitle(AllInformation allInformation) {
		super("Add NMR title");

		this.allInformation=allInformation;
	}

	
	
	public void actionPerformed(ActionEvent e) {
		this.allInformation.getNemo().addText(10, 10, 50, 50, "{TITLE}");
	}

}
