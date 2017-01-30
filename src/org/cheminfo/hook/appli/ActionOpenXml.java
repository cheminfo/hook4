package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.cheminfo.hook.util.GeneralMethods;



class ActionOpenXml extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8517403863138336603L;
	AllInformation allInformation;
	
	public ActionOpenXml(AllInformation allInformation) {
		super("Open");
		this.allInformation=allInformation;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(SHORT_DESCRIPTION, "Open an xml file describing a Jcamp.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

	}
	
	public void actionPerformed (ActionEvent e) {


		if(allInformation.isChanged()) {
			// confirm quit
			JFrame f=new JFrame();
			int answer=JOptionPane.showConfirmDialog(f,"Do you want to save the current file ?");		
			f.setVisible(false);
			f.dispose();
			
			switch (answer) {
		    	case JOptionPane.CANCEL_OPTION: 
		    		return;
		    	case JOptionPane.NO_OPTION:
		    		break;
		    	case JOptionPane.YES_OPTION:
		    		File file=allInformation.getCurrentFile();
		    		//new MenuItemSaveXml(allInformation).saveXml(file);
		    		// WE NEED TO SAVE !!!
		    		break;	
		    	default :
		    }

		}

		JFileChooser fileChooser = new JFileChooser(allInformation.getDefaultPath());
		fileChooser.addChoosableFileFilter(new XmlFilter());
		fileChooser.showOpenDialog(null);
		if (fileChooser.getSelectedFile()!=null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			
			allInformation.setDefaultPath(file);
			if (file.getName().indexOf(".xml") != -1)
			{
/*				//first load the JCAMP file if existing
				File jFile=new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(".xml"))+".jdx");
				if (!this.loadJCAMPFile(jFile))
				{
					jFile=new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(".xml"))+".dx");
					System.out.println(jFile.getAbsolutePath());
					this.loadJCAMPFile(jFile);
				}
				// then we apply the xml information to it
*/				this.loadXMLFile(file);
			}	
			else if (file.getName().indexOf(".jdx") != -1 || file.getName().indexOf(".dx") != -1)
				this.loadJCAMPFile(file);
			
			allInformation.setCurrentFile(file);
			allInformation.setChanged(false);
		}
	}
	
	private class XmlFilter extends javax.swing.filechooser.FileFilter {
		
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
	        return "Xml file";
	    }
	}
	
	private void loadXMLFile (File file)
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
				allInformation.getNemo().setXML(newXML, helpers);

				
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

	private boolean loadJCAMPFile (File file)
	{
		String stringUrl="file:///"+file.getAbsolutePath();
		
//		Converter tempConverter=Converter.getConverter("Jcamp");
//		SpectraData	spectraData = new SpectraData();
		URL correctUrl=null;
		// we need the URL
		// we first try to convert it directly
		try {
			correctUrl=new URL(stringUrl);
		} catch (MalformedURLException e) {System.out.println("Not found : "+stringUrl);return false;}
		if (allInformation.getNemo().addJcamp(stringUrl)!=null) return true;
		return false;
	}

}
