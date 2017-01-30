package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;



class ActionSetNmrString extends AbstractAction {

	private JEditorPane taskOutput = new JEditorPane("text/html","");
	private JRadioButton rdoAsc;
	
	protected NemoInstance nemoInstance;
	
	public ActionSetNmrString(NemoInstance nemoInstance) {
		super("Add by Description...");

		this.nemoInstance = nemoInstance;
		putValue(SHORT_DESCRIPTION, "Add NMR spectrum by description.");
	}
	
	public void actionPerformed (ActionEvent e) {
		String description = (String)JOptionPane.showInputDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), "Paste Description", "Create Spectrum", JOptionPane.PLAIN_MESSAGE, null, null, "");		

		if (description != null && description.compareTo("") != 0)
			nemoInstance.getNemo().addSimulatedSpectrum(description,1);	
	}
}
