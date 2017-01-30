package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class ActionSaveRevertBareJcamp extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	NemoInstance<?> nemoInstance;

	public ActionSaveRevertBareJcamp(NemoInstance<?> nemoInstance) {
		super("Reset");
		this.nemoInstance = nemoInstance;
		putValue(SHORT_DESCRIPTION, "Clear View Data and Save Bare Jcamp File");
	}
	
	public void actionPerformed (ActionEvent e) {
		try {
			if (JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), "Do you really want to delete all view data and save the bare JCAMP file?\n\n (This permanently deletes all data like integrals, annotations, structures etc.!!!)", "Reset File to bare JCAMP File", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION) 
				nemoInstance.revertBareJdx();
		} 
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
}

