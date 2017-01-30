package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraData;
import org.cheminfo.hook.nemo.SpectraDisplay;
import org.cheminfo.hook.nemo.nmr.simulation.SpectraSimulator;


class ActionSimulateSpectra extends AbstractAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AllInformation allInformation;
	
	public ActionSimulateSpectra(AllInformation allInformation) {
		super("Simulate Spectrum");

		this.allInformation=allInformation;
		//	putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(SHORT_DESCRIPTION, "Simulate Spectrum.");
		//	putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
	}
	
	public void actionPerformed (ActionEvent e)
	{

		allInformation.getNemo().getInteractions().getUserDialog().setText("Simulating...");
		
		String table = "1	1H	2			1	2	d	5\r\n2	1H	2.02			1	3	d	3\r\n3	1H	4			1	1	d	15";
		/*SpectraSimulator simulator = new SpectraSimulator(table,400e6,0,10,1,"PPM",3,8,8*1024);
		SpectraData spectraData = simulator.getSpectraData();		
		Spectra spectra = new Spectra(spectraData);
		//PeakPeaking simulSmartPeaks=new PeakPeaking(table,500000000,1,5, "PPM",1, 2);
		//Vector<String> atomID = simulSmartPeaks.getAtomID();
		//Vector<double[]> peakList = simulSmartPeaks.getSpinPeakList();
		//Use all of the DATA_PEAK to make an assignment
        //SmartPickingHelpers.addSmartPeakLabels(allInformation.getNemo().getInteractions(), spectra, peakList, atomID);
       // SmartPickingHelpers.mergeIdenticalPeakLabels(spectra);
		/*double resolution=500e6;
		SmartPeakCalculator simulSmartPeaks=new SmartPeakCalculator();
		Vector<String> atomID = simulSmartPeaks.getAtomID();
		Vector<DoubleArrayList> peakList = simulSmartPeaks.getSpinPeakList();
        //Use all of the DATA_PEAK to make an assignment
        
        SmartPeakLabel smartPeakLabel;
        double startX, stopX;
        DoubleArrayList xPeaks;
        int index=0;
        for(index=0;index<peakList.size();index++){
        	xPeaks=peakList.get(index);
        	//Find the statX and the stopX for this peak label
        	startX=Double.MAX_VALUE;
        	stopX=Double.MIN_VALUE;
        	for(int j=0;j<xPeaks.size();j++){
        		if(startX>xPeaks.get(j))
        			startX=xPeaks.get(j);
        		if(stopX<xPeaks.get(j))
        			stopX=xPeaks.get(j);
        	}
        	smartPeakLabel = new SmartPeakLabel(stopX,startX);
        	smartPeakLabel.setObserveFrequency(resolution/1e6);
        	for(int j=0;j<xPeaks.size();j++){
        		smartPeakLabel.addPeak(xPeaks.get(j));
        		smartPeakLabel.setIntensity(j,1);
        	}
        	try{
        		smartPeakLabel.setReferenceAtomID(Integer.parseInt(atomID.get(index)));
        	}catch(NumberFormatException ex){
        		smartPeakLabel.setReferenceAtomID(index);
        	}
        	smartPeakLabel.setPublicationAssignment(atomID.get(index));
        	spectra.addEntity(smartPeakLabel);
        	smartPeakLabel.compute();
    		smartPeakLabel.setInteractiveSurface(allInformation.getNemo().getInteractions());
        }*/
		
        
//		SpectraDisplay mainDisplay = (SpectraDisplay)allInformation.getNemo().getMainDisplay();
//		mainDisplay.addSpectra(spectra);
//		allInformation.getNemo().getInteractions().checkButtonsStatus();
		
	}
}
