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
import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.cheminfo.hook.nemo.SpectraTextEntity;


class ActionPrintReport extends AbstractAction
{
	AllInformation allInformation;

	public ActionPrintReport(AllInformation allInformation) {
		super("Print Report");

		this.allInformation=allInformation;
		putValue(SHORT_DESCRIPTION, "Print Report");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
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
				
				virtualDisplay= new SpectraDisplay();
				virtualDisplay.setInteractiveSurface(interactions);
				interactions.addEntity(virtualDisplay);

				Hashtable xmlProperties = new Hashtable();
				xmlProperties.put("embedJCamp", new Boolean(true));
				String spectraXmlTag=((SpectraDisplay)allInformation.getNemo().getMainDisplay()).getFirstSpectra().getXmlTag(xmlProperties);
				
				virtualDisplay.init();
				virtualDisplay.setInteractiveSurface(interactions);
				virtualDisplay.setSize(pageFormat.getWidth(), pageFormat.getHeight());
				
				Hashtable helpers=new Hashtable();
				Spectra tempSpectra1=new Spectra(spectraXmlTag, virtualDisplay.getWidth(), virtualDisplay.getHeight(), helpers);

				SpectraTextEntity filenameBox=new SpectraTextEntity(30);
				filenameBox.setFontSize(8);
				filenameBox.setText(tempSpectra1.getSpectraData().getParamString("$file", ""));
				filenameBox.setLocation(tempSpectra1.getWidth()/2, 0);
				tempSpectra1.addEntity(filenameBox);
				
				SpectraTextEntity parameterBox=new SpectraTextEntity(25, 15);
				parameterBox.setFontSize(8);
				String tempText = "";
				tempText+=tempSpectra1.getSpectraData().getTitle()+"\n";
				tempText+="\n";
				tempText+="Pulse Sequence: "+tempSpectra1.getSpectraData().getParamString("$pslabel", " no pulse info ")+"\n";
				tempText+="Solvent: "+tempSpectra1.getSpectraData().getParamString("$solvent", "no info")+"\n";
				double temperature=tempSpectra1.getSpectraData().getParamDouble("$temp", -99999);
				if (temperature != -99999)
					tempText+="Temperature: "+temperature+"\n";
				else
					tempText+="Ambient Temperature\n";
				tempText+="\n";
				tempText+="OBSERVE "+tempSpectra1.getSpectraData().getParamString(".OBSERVENUCLEUS", "")+", "+tempSpectra1.getSpectraData().getParamDouble("observeFrequency", 0)+" MHz\n";

				parameterBox.setText(tempText);
				parameterBox.setLocation(0,0);
				tempSpectra1.addEntity(parameterBox);

				Spectra tempSpectra2=new Spectra(spectraXmlTag, virtualDisplay.getWidth(), virtualDisplay.getHeight(), helpers);



				SpectraDisplay insertDisplay= new SpectraDisplay();

				virtualDisplay.checkInteractiveSurface();

				virtualDisplay.addSpectra(tempSpectra1);

				virtualDisplay.addEntity(insertDisplay);
				
				insertDisplay.init();
				insertDisplay.setInteractiveSurface(interactions);
				insertDisplay.setSecondaryColor(null);
				insertDisplay.setSize(virtualDisplay.getWidth()/2.5, virtualDisplay.getHeight()/3);
				insertDisplay.setLocation(virtualDisplay.getWidth()/3, virtualDisplay.getHeight()/20);
				insertDisplay.addSpectra(tempSpectra2);
//				insertDisplay.hasHScale(true);
				insertDisplay.checkInteractiveSurface();
			
				insertDisplay.setCurrentLimits(12, 7);
				virtualDisplay.setCurrentLimits(10, -0.5);
				

				virtualDisplay.checkInteractiveSurface();
	
				virtualDisplay.checkSizeAndPosition();
		
				interactions.clearActiveEntities();
				
				printJob.print();
			} catch (PrinterException ex) {System.out.println("Print Report, Exception "+ex+", cause "+ex.getCause());}
		}
	}
}


