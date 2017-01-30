package org.cheminfo.hook.appli;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextArea;

public class SpectraFromDescriptionPane extends JComponent
{
	private JTextArea textArea;
	
	public SpectraFromDescriptionPane()
	{
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.invalidate();
		this.repaint();
	}
	
	public JDialog createDialog()
	{	
		JDialog dialog=new JDialog();
		dialog.getContentPane().add(this);
		dialog.setModal(true);
		dialog.setResizable(false);
		
		this.textArea=new JTextArea(1, 20);
		this.add(textArea);
		this.add(new JButton("OK"));
		dialog.invalidate();
		dialog.repaint();
		
		return dialog;
	}
	
	public static boolean showDialog(String retDescription)
	{
		SpectraFromDescriptionPane pane=new SpectraFromDescriptionPane();
		JDialog dialog=pane.createDialog();
		
		dialog.pack();
		dialog.show();
		
		retDescription=pane.textArea.getText();
		return true;
	}
}