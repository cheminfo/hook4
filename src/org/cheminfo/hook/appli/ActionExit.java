package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

class ActionExit extends AbstractAction {
	
	AllInformation allInformation;
	
	public ActionExit(AllInformation allInformation) {
		super("Exit");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
		putValue(SHORT_DESCRIPTION, "Exit the program, save if necessary");
		this.allInformation=allInformation;
		allInformation.setActionExit(this);
	}
	
	public void actionPerformed (ActionEvent e) {
		
		if(allInformation.isChanged()) {
			// confirm quit
			JFrame f=new JFrame();
			int answer=JOptionPane.showConfirmDialog(f,"Do you want to save the file before quit ?");		
			f.setVisible(false);
			f.dispose();
			
			switch (answer) {
		    	case JOptionPane.CANCEL_OPTION: 
		    		return;
		    	case JOptionPane.NO_OPTION:
		    		System.exit(0);
		    	case JOptionPane.YES_OPTION:
		    		File file=allInformation.getCurrentFile();
		    		//new MenuItemSaveXml(allInformation).saveXml(file);
		    		// WE NEED TO SAVE !!!
		    		//new MenuItemSaveXml(allInformation).saveXmlc(file);
		    		break;	
		    	default :
		    }

	
		}
		
		System.exit(0);
	}
	
}
