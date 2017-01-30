package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;


class ActionImportJcamp extends AbstractAction {
	
	AllInformation allInformation;
	
	public ActionImportJcamp(AllInformation allInformation) {
		super("Import jcamp file");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
		putValue(SHORT_DESCRIPTION, "Open and import a JDX file");
		this.allInformation=allInformation;
	}
	
	public void actionPerformed (ActionEvent e) {
		
		JFileChooser fileChooser = new JFileChooser(allInformation.getDefaultPath());
		fileChooser.addChoosableFileFilter(new JcampFilter());
		int result=fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			allInformation.setDefaultPath(file);
			loadFile(file);
		}

	}
	
	class JcampFilter extends javax.swing.filechooser.FileFilter {
	
    //Accept all directories and all gif, jpg, tiff, or png files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	
	        String extension = MiscUtilities.getExtension(f);
	        if (extension != null) {
	            if (extension.equals("jdx") || extension.equals("dx")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }

	    //The description of this filter
	    public String getDescription() {
	        return "JCAMP files";
	    }
	}
	
	private void loadFile (File file)
	{
		String stringUrl="file://"+""+file.getAbsolutePath();
		
//		Converter tempConverter=Converter.getConverter("Jcamp");
//		SpectraData	spectraData = new SpectraData();
		URL correctUrl=null;
		// we need the URL
		// we first try to convert it directly
		try {
			correctUrl=new URL(stringUrl);
		} catch (MalformedURLException e) {System.out.println("Not found : "+stringUrl);return;}

		allInformation.getNemo().addJcamp(stringUrl);
	}
	
}
