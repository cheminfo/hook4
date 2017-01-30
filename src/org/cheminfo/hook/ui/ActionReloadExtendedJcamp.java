package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class ActionReloadExtendedJcamp extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;

	protected NemoInstance nemoInstance;
	
	public ActionReloadExtendedJcamp(NemoInstance nemoInstance) {
		super("Reload");
		this.nemoInstance = nemoInstance;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
		putValue(SHORT_DESCRIPTION, "Reload Jcamp File from Disk");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
	}
	
	public void actionPerformed (ActionEvent e) {
		try {
			if(nemoInstance.isDirty()) {
				int answer=JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()),"Do you really want to revert " + nemoInstance.getRessource() + "?","Revert",JOptionPane.YES_NO_OPTION);		
				
				switch (answer) {
			    	case JOptionPane.YES_OPTION:
			    		break;	
			    	default :
			    		return;
			    }
			}
						
			nemoInstance.reloadJcamp();
		} 
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
}
