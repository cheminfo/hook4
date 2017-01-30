package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ActionAbout extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;

	protected NemoInstance nemoInstance;
	
	public ActionAbout(NemoInstance nemoInstance) {
		super("About");
		this.nemoInstance = nemoInstance;
	}
	
	public void actionPerformed (ActionEvent e) {
		JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), Version.BANNER, "About Nemo", JOptionPane.INFORMATION_MESSAGE);
	}

}
