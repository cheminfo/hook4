package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class ActionSaveExtendedJcamp extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	NemoInstance<?> nemoInstance;

	public ActionSaveExtendedJcamp(NemoInstance<?> nemoInstance) {
		super("Save");
		this.nemoInstance = nemoInstance;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(SHORT_DESCRIPTION, "Save Jcamp File");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
	}
	
	public void actionPerformed (ActionEvent e) {
		try {
			nemoInstance.save();
		} 
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
}

