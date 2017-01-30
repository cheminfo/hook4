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

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.PeakLabel;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.nemo.Spectra;

class ActionGetPeaks extends AbstractAction {

	AllInformation allInformation;

	public ActionGetPeaks(AllInformation allInformation) {
		super("Peak List");

		this.allInformation = allInformation;
		// putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(SHORT_DESCRIPTION, "Get peak list.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,
				ActionEvent.CTRL_MASK));

	}

	public void actionPerformed (ActionEvent e)
	{
		InteractiveSurface interactions=allInformation.getNemo().getInteractions();
		if (interactions.getActiveEntity() instanceof SmartPeakLabel)
		{
			SmartPeakLabel label=(SmartPeakLabel)interactions.getActiveEntity();
			
			JEditorPane taskOutput=new JEditorPane("text/plain",label.getPeakList());
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
		if (interactions.getActiveEntity() instanceof Spectra) {
			Spectra spectra = (Spectra)interactions.getActiveEntity();
			int nEntities = spectra.getEntitiesCount();
			for (int iEntity = 0; iEntity < nEntities; iEntity++) {
				if (spectra.getEntity(iEntity) instanceof PeakLabel) {
					PeakLabel peak = (PeakLabel)spectra.getEntity(iEntity);
					double xpos = peak.getXPosUnits();
					System.out.println("<cheminfo:peak position=\""+xpos+"\" />");
				}
			}
			

		}
	}
}
