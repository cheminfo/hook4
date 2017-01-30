package org.cheminfo.hook.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.cheminfo.hook.util.NemoPreferences;

public class ActionPreferences extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;

	protected NemoInstance nemoInstance;
	
	public ActionPreferences(NemoInstance nemoInstance) {
		super("Preferences");
		this.nemoInstance = nemoInstance;
	}
	
	public void actionPerformed (ActionEvent e) {
		Frame f = null;
		if(Frame.getFrames().length>0)
			f = Frame.getFrames()[0];
		new JPreferencesDialog(f, NemoPreferences.getInstance());
	}

}
