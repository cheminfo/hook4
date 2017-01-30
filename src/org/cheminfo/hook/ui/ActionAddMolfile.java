package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import org.cheminfo.hook.appli.MiscUtilities;
import org.cheminfo.hook.util.NemoPreferences;

public class ActionAddMolfile extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;

	protected NemoInstance nemoInstance;
	
	public ActionAddMolfile(NemoInstance nemoInstance) {
		super("Add Structure...");
		this.nemoInstance = nemoInstance;
		putValue(SHORT_DESCRIPTION, "Add a Structure from a Mol File.");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	}
		
	public void actionPerformed (ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser(NemoPreferences.getInstance().get(NemoPreferences.getInstance().MOST_RECENT_PATH));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new JcampFilter());
		if (fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile()!=null) {
			File file=fileChooser.getSelectedFile().getAbsoluteFile();
			loadFile(file);
		}
	}
	
	class JcampFilter extends javax.swing.filechooser.FileFilter {
	
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
				fin.close();

				nemoInstance.addMoleculeByMolfile(molString,50,50,150,150);
				
			} catch (java.io.FileNotFoundException fnfEx)	{System.out.println("File Not Found");}
			catch (java.io.IOException ioEx)	{System.out.println("IOException");}
			
		}
	}
}
