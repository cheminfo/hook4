package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class ActionMassFitting extends AbstractAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AllInformation allInformation;
	public ActionMassFitting(AllInformation allInformation) {
		super("Mass prediction fitting");

		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Prediction fitting.");
	}
	
	public void actionPerformed (ActionEvent e)
	{
		//Load the experimental spectrum
		JFileChooser fileChooser = new JFileChooser(allInformation.getDefaultPath());
		fileChooser.addChoosableFileFilter(new JcampFilter());
		fileChooser.setDialogTitle("Load experimental spectrum");
		int result=fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			allInformation.setDefaultPath(file);
			loadFile(file);
		}
		//Load the predicted spectrum
		fileChooser.setDialogTitle("Load predicted spectrum");
		result=fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			allInformation.setDefaultPath(file);
			loadFile(file);
		}
		//allInformation.getNemo().fitMassPrediction();
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
			String stringUrl="file:///"+file.getAbsolutePath();
			
//			Converter tempConverter=Converter.getConverter("Jcamp");
//			SpectraData	spectraData = new SpectraData();
			URL correctUrl=null;
			// we need the URL
			// we first try to convert it directly
			try {
				correctUrl=new URL(stringUrl);
			} catch (MalformedURLException e) {System.out.println("Not found : "+stringUrl);return;}

			allInformation.getNemo().addJcamp(stringUrl);
		}
}