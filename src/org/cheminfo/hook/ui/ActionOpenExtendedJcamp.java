package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class ActionOpenExtendedJcamp extends ActionAddJcamp {
	private static final long serialVersionUID = 8517403863138336603L;
	
	public ActionOpenExtendedJcamp(NemoInstance nemoInstance) {
		super(nemoInstance, "Open");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(SHORT_DESCRIPTION, "Open a Jcamp File.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
	}
	
	@Override
	protected void doit() throws IOException {
		if(nemoInstance.isDirty()) {
			int answer=JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()),"Do you want to save " + nemoInstance.getRessource() + "?");		
			
			switch (answer) {
		    	case JOptionPane.NO_OPTION:
		    		break;
		    	case JOptionPane.YES_OPTION:
		    		nemoInstance.save();
		    		break;	
		    	default :
		    		return;
		    }
		}

		super.doit();
	}
	
	@Override
	protected void loadFile(File file) throws IOException {
		nemoInstance.openJcamp(file);
	}
}
