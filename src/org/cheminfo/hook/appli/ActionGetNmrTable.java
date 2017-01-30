package org.cheminfo.hook.appli;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;


class ActionGetNmrTable extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2442001899758059960L;
	AllInformation allInformation;
	
	public ActionGetNmrTable(AllInformation allInformation) {
		super("NMR assignment");

		this.allInformation=allInformation;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		putValue(SHORT_DESCRIPTION, "Get NMR spectrum assignment.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
	}
	
	public void actionPerformed (ActionEvent e) {
		JEditorPane taskOutput=new JEditorPane("text/plain",allInformation.getNemo().getJSON());
		// JEditorPane taskOutput=new JEditorPane("text/plain","TODO: create the nmr table from the peak list");
		JDialog dialog=new JDialog();
		int width=800;
		int height=400;
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width-width)/2;
		int y = (screen.height-height)/2;
		dialog.setBounds(x,y,width,height);
		dialog.setModal(true);
		dialog.setSize(width, height);
		taskOutput.setMargin(new Insets(5,5,5,5));
		dialog.getContentPane().add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		dialog.setVisible(true);
	}
}
