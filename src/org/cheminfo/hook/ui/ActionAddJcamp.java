package org.cheminfo.hook.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cheminfo.hook.appli.MiscUtilities;
import org.cheminfo.hook.util.NemoPreferences;

public class ActionAddJcamp extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;

	protected NemoInstance nemoInstance;
	protected String label;
	
	public ActionAddJcamp(NemoInstance nemoInstance) {
		this(nemoInstance, "Add");
	}
	
	public ActionAddJcamp(NemoInstance nemoInstance, String label) {
		super(label + "...");
		this.nemoInstance = nemoInstance;
		this.label = label;
		
		putValue(SHORT_DESCRIPTION, "Add a Jcamp File.");
	}

	public void actionPerformed (ActionEvent e) {
		try {
			doit();
		} 
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	protected void doit() throws IOException
	{
		JFileChooser fileChooser = new JFileChooser(NemoPreferences.getInstance().get(NemoPreferences.getInstance().MOST_RECENT_PATH));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new JdxFilter());
		
		if(fileChooser.showDialog(Frame.getFrames()[0], label)==JFileChooser.APPROVE_OPTION)
		{
			File file=fileChooser.getSelectedFile().getAbsoluteFile();

			loadFile(file);
		}
	}

	protected void loadFile(File file) throws IOException {
		nemoInstance.addJcamp(file);
	}

	private class JdxFilter extends javax.swing.filechooser.FileFilter {
		
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	
	        String extension = MiscUtilities.getExtension(f);
	        if (extension != null) {
	            if (extension.equalsIgnoreCase("jdx") || extension.equalsIgnoreCase("dx")) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	
	        return false;
	    }
	
	    //The description of this filter
	    public String getDescription() {
	        return "Jcamp file";
	    }
	}

}
