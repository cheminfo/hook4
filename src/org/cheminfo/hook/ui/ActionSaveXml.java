package org.cheminfo.hook.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.cheminfo.hook.appli.MiscUtilities;
import org.cheminfo.hook.util.NemoPreferences;



class ActionSaveXml extends AbstractAction {
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;
	protected String label;
	
	public ActionSaveXml(NemoInstance nemoInstance) {
		this("Save View", nemoInstance);
	}
	
	public ActionSaveXml(String label, NemoInstance nemoInstance) {
		super(label);
		this.label = label;
		putValue(SHORT_DESCRIPTION, "Save as xml format containing link to spectra");
		this.nemoInstance = nemoInstance;
	}

	
	public void actionPerformed (ActionEvent e) 
	{
		JFileChooser fileChooser = new JFileChooser(NemoPreferences.getInstance().get(NemoPreferences.getInstance().MOST_RECENT_PATH));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new XmlFilter());
		
		if(fileChooser.showDialog(Frame.getFrames()[0], label)==JFileChooser.APPROVE_OPTION)
		{
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			if (file!=null) {
				file = new File(file.getPath().replaceFirst("(\\.xml$)|(^[^\\.]*$)","$2\\.xml"));
				saveXml(file);
			}
		}

	}
	
	void saveXml (File file) {
		try {			
			if (file!=null) {				
				FileOutputStream fout =  new FileOutputStream(file);
				OutputStreamWriter oStreamWriter = new OutputStreamWriter(fout);
				BufferedWriter outputFile=new BufferedWriter(oStreamWriter);
				
//				System.out.println(allInformation.rootDisplay.getXMLTag(true));

				String XMLTag=nemoInstance.getNemo().getXMLEmbedded();
				outputFile.write(XMLTag, 0, XMLTag.length());

				outputFile.close();
				fout.close();
				// The button if disabled will not be reenabled ... (at least not well)
	//			allInformation.setChanged(false);
			}
		} catch (IOException er) {}	
	}
	
	

	private class XmlFilter extends javax.swing.filechooser.FileFilter {
		
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	
	        String extension = MiscUtilities.getExtension(f);
	        if (extension != null) {
	            if (extension.equalsIgnoreCase("xml")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }
	
	    //The description of this filter
	    public String getDescription() {
	        return "Xml view file";
	    }
	}

}

