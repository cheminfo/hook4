package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;


class ActionSaveAsXml extends AbstractAction {
	
	AllInformation allInformation;
	
	public ActionSaveAsXml(AllInformation allInformation) {
		super("Save As...");
		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Save as xml format");
	}
	
	public void actionPerformed (ActionEvent e)
	{
		File file=allInformation.getCurrentFile();
		this.saveXml(file);
	}
	
	void saveXml (File file) {
		try {
			
//			if (file==null) {	
				JFileChooser fileSave=new JFileChooser (allInformation.getDefaultPath());
				fileSave.setFileFilter(new XmlFilter());
				fileSave.showSaveDialog(null);
				if (fileSave.getSelectedFile()!=null) {
					file=fileSave.getSelectedFile();
					file=new File(file.getPath().replaceFirst("(\\.xml$)|(^[^\\.]*$)","$2\\.xml"));
					if (file.exists())
						file.delete();
					allInformation.setCurrentFile(file);
					allInformation.setDefaultPath(file);
				}
//			}
			
			
			if (file!=null) {				
				FileOutputStream fout =  new FileOutputStream(file);
				OutputStreamWriter oStreamWriter = new OutputStreamWriter(fout);
				BufferedWriter outputFile=new BufferedWriter(oStreamWriter);
				

				String xmlString=allInformation.nemo.getXML();
				
//				this.AbsoluteToRelative(xmlString, file.getAbsolutePath().replace("\\", "/"));
				
				
				//LP The modification is done on load and not on save ...
				// String newXML=this.AbsoluteToRelative(xmlString, file.getAbsolutePath());
				outputFile.write(xmlString, 0, xmlString.length());

//				outputFile.write(allInformation.nemo.getXML(), 0, allInformation.nemo.getXML().length());

				outputFile.close();
				fout.close();
				allInformation.setChanged(false);
			}
		} catch (IOException er) {}	
	}

	private String AbsoluteToRelative(String origXML, String filePath)
	{
		String outString=origXML;

		String[] splitter1=filePath.split("/");
		for (int i=0; i < splitter1.length; i++)
			System.out.println(splitter1[i]);
		
		System.out.println("------------------");
		String[] splitter2;
		String oldFilename="";
		String tempString="";
		
		int nextFilename=origXML.indexOf("file:/");
		
		while (nextFilename >= 0)
		{
			oldFilename=origXML.substring(nextFilename+6, origXML.indexOf("\"", nextFilename));
			System.out.println("FILENAME: "+oldFilename);
			splitter2=oldFilename.split("/");
			
			for (int i=0; i < splitter2.length; i++)
				System.out.println(splitter2[i]);
			
			int counter=0; 
			
			while (counter < splitter1.length-1 && counter < splitter2.length-1)
			{
				if (splitter1[counter].compareTo(splitter2[counter]) != 0)
					break;
								
				counter++;
			}
			
			System.out.println("First Difference: "+splitter1[counter]+" -> "+splitter2[counter]);
			
			if (counter == splitter1.length-1)
			{
				tempString="file:/./";
			}
			else
			{
				tempString="file:/";
			
				for (int i=0; i < splitter1.length-1-counter; i++)
				{
					tempString+="../";
				}
			}
			
			for (int i=counter; i < splitter2.length-1; i++)
			{
				tempString+=splitter2[i]+"/";
			}
			
			
			tempString+=splitter2[splitter2.length-1];	// filename
			
			System.out.println("new path: "+tempString);
			outString=outString.replaceAll(origXML.substring(nextFilename, origXML.indexOf("\"", nextFilename)), tempString);

			nextFilename=origXML.indexOf("file:", nextFilename+1);
		}
		
		
		return outString;
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

