package org.cheminfo.hook.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class ActionWiki extends AbstractAction {
	private static final long serialVersionUID = 8517403863138336603L;
	
	protected NemoInstance nemoInstance;

	public ActionWiki(NemoInstance nemoInstance) {
		super("Wiki");
		
		this.nemoInstance = nemoInstance;
		
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		putValue(SHORT_DESCRIPTION, "Open the wiki in a web browser.");
	}

	public void actionPerformed (ActionEvent e) {
		try {
			Desktop.getDesktop().browse(new URI("http://www.cheminfo.org/wiki/Nemo"));
		} 
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
}
