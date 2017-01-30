package org.cheminfo.hook.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.cheminfo.hook.appli.MiscUtilities;
import org.cheminfo.hook.util.GeneralMethods;
import org.cheminfo.hook.util.NemoPreferences;



class ActionOpenXml extends AbstractAction {
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;
	protected String label;
	
	public ActionOpenXml(NemoInstance nemoInstance) {
		this("Open View", nemoInstance);
	}
	
	public ActionOpenXml(String label, NemoInstance nemoInstance) {
		super(label);
		this.label = label;
		putValue(SHORT_DESCRIPTION, "Open an xml file describing a Jcamp.");
		this.nemoInstance = nemoInstance;
	}
	
	public void actionPerformed (ActionEvent e) {
//		if(nemoInstance.isDirty()) {
//			int answer=JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()),"Do you want to save " + nemoInstance.getRessource() + "?");		
//			
//			switch (answer) {
//		    	case JOptionPane.NO_OPTION:
//		    		break;
//		    	case JOptionPane.YES_OPTION:
//		    		nemoInstance.save();
//		    		break;	
//		    	default :
//		    		return;
//		    }
//		}
		
		JFileChooser fileChooser = new JFileChooser(NemoPreferences.getInstance().get(NemoPreferences.getInstance().MOST_RECENT_PATH));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new XmlFilter());
		
		if(fileChooser.showDialog(Frame.getFrames()[0], label)==JFileChooser.APPROVE_OPTION)
		{
			File file=fileChooser.getSelectedFile().getAbsoluteFile();

			loadXMLFile(file);
		}
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

	private void loadXMLFile(File file)
	{
		if (file!=null) {	
			try
			{	
						
				FileInputStream fin =  new FileInputStream(file);
				InputStreamReader iStreamReader = new InputStreamReader(fin);
				BufferedReader inputFile=new BufferedReader(iStreamReader);
				
				String currentLine=inputFile.readLine();
				String xmlString="";
				
				while (currentLine != null)
				{
					xmlString+=currentLine;					
					currentLine=inputFile.readLine();
				}


				Hashtable helpers=new Hashtable();

/*				String folderPath=this.allInformation.getDefaultPath();
				
				int lastSlashIndex=0;
				
				while (folderPath.indexOf("\\", lastSlashIndex+1) != -1)
				{
					lastSlashIndex=folderPath.indexOf("\\", lastSlashIndex+1);
				}
				
				folderPath=folderPath.substring(0, lastSlashIndex+1);
				
				
				System.out.println("folderPath: "+folderPath);
				helpers.put("folderPath", folderPath);
*/
				System.out.println("p: "+file.getParentFile().getAbsolutePath());
				String newXML=this.RelativeToAbsolute(xmlString, file.getParentFile().getAbsolutePath().replaceAll("\\"+File.separator, "/"));
				System.out.println(newXML);
				nemoInstance.getNemo().setXML(newXML, helpers, true);
				
				fin.close();
			} catch (java.io.FileNotFoundException fnfEx)	{System.out.println("File Not Found");}
			catch (java.io.IOException ioEx)	{System.out.println("IOException");}
			
		}
	}

	private String RelativeToAbsolute(String origXML, String filePath)
	{
		// on unix the file should look like: file:///document/xxx
		// on PC: file:/C:/document/xxx
		
		
		System.out.println("filePath "+filePath);
		String newXML=origXML;
		if (filePath.startsWith("/")) filePath="/"+filePath;
		filePath=GeneralMethods.encodeString(filePath);
		/* LP20090429 I don't understand the following code ...
		newXML=newXML.replaceAll("file://h", "file:///h");
		if (newXML.indexOf("file:///") != -1)
			return newXML;
		newXML=newXML.replaceAll("file:/", "file:/"+filePath+"/");
		
		return newXML;
		*/
		newXML=newXML.replaceAll("file:/[^\"]*(/[^\"]*)\"","file:/"+filePath+"$1\"");
		System.out.println("newXML "+newXML);
		
		return newXML;
	}

}
