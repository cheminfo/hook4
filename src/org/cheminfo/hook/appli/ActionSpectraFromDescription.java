package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.cheminfo.hook.framework.InteractiveSurface;

class ActionSpectraFromDescription extends AbstractAction
{
	AllInformation allInformation;
	
	public ActionSpectraFromDescription(AllInformation allInformation) {
		super("Create Spectra From Description");

		this.allInformation=allInformation;
	//	putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(SHORT_DESCRIPTION, "Create Spectra From Description.");
//		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
	}
	
	public void actionPerformed (ActionEvent e)
	{
		InteractiveSurface interactions=allInformation.getNemo().getInteractions();

		String description = (String)JOptionPane.showInputDialog(null, "Paste Description", "Create Spectrum", JOptionPane.PLAIN_MESSAGE, null, null, "");		

		if (description != null && description.compareTo("") != 0)
			allInformation.getNemo().addSimulatedSpectrum(description,1);
	}
}
