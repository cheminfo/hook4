package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;



class ActionSaveXml extends AbstractAction {
	
	AllInformation allInformation;

	public ActionSaveXml(AllInformation allInformation) {
		super("Save");
		this.allInformation=allInformation;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(SHORT_DESCRIPTION, "Save as xml format containing link to spectra");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		allInformation.setActionSaveXml(this);
	}
	
	public void actionPerformed (ActionEvent e) {
		
		File file=allInformation.getCurrentFile();
		this.saveXml(file);
	}
	
	void saveXml (File file) {
		try {
			
			if (file==null) {	
				JFileChooser fileSave=new JFileChooser (allInformation.getDefaultPath());
				fileSave.setFileFilter(new XmlFilter());
				fileSave.showSaveDialog(null);
				if (fileSave.getSelectedFile()!=null) {
					file=fileSave.getSelectedFile();
					file=new File(file.getPath().replaceFirst("(\\.xml$)|(^[^\\.]*$)","$2\\.xml"));
					allInformation.setCurrentFile(file);
					allInformation.setDefaultPath(file);
				}
			}
			
			
			if (file!=null) {				
				FileOutputStream fout =  new FileOutputStream(file);
				OutputStreamWriter oStreamWriter = new OutputStreamWriter(fout);
				BufferedWriter outputFile=new BufferedWriter(oStreamWriter);
				
//				System.out.println(allInformation.rootDisplay.getXMLTag(true));

				Hashtable xmlProperties = new Hashtable();
				xmlProperties.put("includeURL", new Boolean(true));
				String XMLTag=allInformation.rootDisplay.getXmlTag(xmlProperties);
				outputFile.write(XMLTag, 0, XMLTag.length());


				outputFile.close();
				fout.close();
				// The button if disabled will not be reenabled ... (at least not well)
	//			allInformation.setChanged(false);
			}
		} catch (IOException er) {}	
	}
	
	



	class XmlFilter extends javax.swing.filechooser.FileFilter {
		
	    //Accept all directories and all gif, jpg, tiff, or png files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	
	        String extension = MiscUtilities.getExtension(f);
	        if (extension != null) {
	            if (extension.equals("xml")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }
	
	    //The description of this filter
	    public String getDescription() {
	        return "Xml view files";
	    }
	}

	
}

