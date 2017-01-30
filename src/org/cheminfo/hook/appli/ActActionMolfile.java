package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;

class ActActionMolfile extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 105877953102418338L;
	
	AllInformation allInformation;
	
	public ActActionMolfile(AllInformation allInformation) {
		super("Insert molfile");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
		putValue(SHORT_DESCRIPTION, "Open and insert a Molfile");
		this.allInformation=allInformation;
	}
	
	public void actionPerformed (ActionEvent e) {
		
		JFileChooser fileChooser = new JFileChooser(allInformation.getDefaultPath());
		fileChooser.addChoosableFileFilter(new JcampFilter());
		fileChooser.showOpenDialog(null);
		if (fileChooser.getSelectedFile()!=null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
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
	            if (extension.equals("mol")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }

	    //The description of this filter
	    public String getDescription() {
	        return "Molfiles";
	    }
	}
	
	private void loadFile (File file)
	{
		if (file!=null) {	
			try
			{	
						
				FileInputStream fin =  new FileInputStream(file);
				InputStreamReader iStreamReader = new InputStreamReader(fin);
				BufferedReader inputFile=new BufferedReader(iStreamReader);
				
				String currentLine=inputFile.readLine();
				String molString="";
				
				while (currentLine != null)
				{
					molString+=currentLine+"\n";
					
					currentLine=inputFile.readLine();
				}

//				allInformation.getNemo().addMolfileACT(molString);

				ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();

				molDisplay.setLocation(50, 50);
				
				molDisplay.setMovementType(BasicEntity.GLOBAL);
				// molDisplay.setErasable(true);
				molDisplay.setEntityName("molDisplay");
				
//				molDisplay.setPrimaryColor(MyTransparency.createTransparentColor(Color.white));
				
				allInformation.getNemo().getMainDisplay().addEntity(molDisplay,0);

				molDisplay.init(150,150);
				molDisplay.addMolfile(molString,false);
				
				fin.close();
			} catch (java.io.FileNotFoundException fnfEx)	{System.out.println("File Not Found");}
			catch (java.io.IOException ioEx)	{System.out.println("IOException");}
			
		}
	}	
}
