package org.cheminfo.hook.ui;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class NormalFrame {

	// set look and feel
	static {
    	try {
    		Locale.setDefault(Locale.ENGLISH);
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e1) {
			System.out.println("Warning: Nimbus look and feel not supported.");
		}
    }
	
	public static void main(final String[] args) {		
    	SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
            	NemoFrame<File> nemoFrame = new NemoFrame<File>();
            	            	
            	try {
            		if(args.length<0)
						nemoFrame.getNemoInstance().openJcamp(new File(args[0].trim()));
            			
            		for(int i=1;i<args.length;i++)
   						nemoFrame.getNemoInstance().addJcamp(new File(args[i].trim()));

				} catch (IOException e) {
					JOptionPane.showMessageDialog(nemoFrame, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				}
            }
        });
    }
}
