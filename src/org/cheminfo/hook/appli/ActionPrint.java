package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.SpectraDisplay;


class ActionPrint extends AbstractAction
{
	AllInformation allInformation;

	public ActionPrint(AllInformation allInformation) {
		super("Print");

		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Print view");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
	}

	public void actionPerformed (ActionEvent e)
	{
		SpectraDisplay virtualDisplay=null;
		InteractiveSurface interactions=new InteractiveSurface();
		
//		PrintSetupDialog psDialog= new PrintSetupDialog(allInformation);
		
		PrinterJob printJob = PrinterJob.getPrinterJob();
		
		printJob.setCopies(1);
		//printJob.setPrintable(interactions);
		printJob.setPageable(interactions);
		if (printJob.printDialog())
		{
			try
			{
				PageFormat pageFormat=interactions.getPageFormat(0);

				interactions.setSize((int)pageFormat.getImageableWidth()+20, (int)pageFormat.getImageableHeight()+40);
				
				interactions.isLegal=true;

				virtualDisplay= new SpectraDisplay();
				virtualDisplay.setInteractiveSurface(interactions);
				interactions.addEntity(virtualDisplay);
						
				Hashtable helpers=new Hashtable();
				virtualDisplay.init(allInformation.getNemo().getXML(), pageFormat.getImageableWidth()+40, pageFormat.getImageableHeight()+40, helpers);

				virtualDisplay.checkInteractiveSurface();
				virtualDisplay.checkSizeAndPosition();
				interactions.clearActiveEntities();
				
				printJob.print();
			} catch (PrinterException ex) {System.out.println("Exception "+ex+", cause "+ex.getCause());}
		}
	}
}


