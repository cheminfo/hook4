package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;


class ActionSetPrediction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8957711124239479106L;
	AllInformation allInformation;
	
	public ActionSetPrediction(AllInformation allInformation) {
		super("Set prediction");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
		putValue(SHORT_DESCRIPTION, "Open and import prediction file");
		this.allInformation=allInformation;
	}
	
	public void actionPerformed (ActionEvent e) {
		
		JFileChooser fileChooser = new JFileChooser(allInformation.getDefaultPath());
		fileChooser.addChoosableFileFilter(new PredictionFilter());
		int result=fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			loadFile(file);
		}

	}
	
	class PredictionFilter extends javax.swing.filechooser.FileFilter {
	
    //Accept all directories and all gif, jpg, tiff, or png files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	
	        String extension = MiscUtilities.getExtension(f);
	        if (extension != null) {
	            if (extension.equals("dat")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }

	    //The description of this filter
	    public String getDescription() {
	        return "Prediction files";
	    }
	}
	
	private void loadFile (File file)
	{
		String data="";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				data += line + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		allInformation.getNemo().setPrediction(data);
	}
	
}
