package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.LorentzienFilter;

public class LorentzienAction extends GeneralAction {
	
	public static void performAction(InteractiveSurface interactions) {
		int nbPoints=Integer.valueOf(interactions.getUserDialog().getParameter("nbPoints")).intValue();
		double peakWidth=Double.valueOf(interactions.getUserDialog().getParameter("peakWidth")).doubleValue();
	
		if (interactions.getActiveEntity() instanceof Spectra)
		{
			Spectra spectra=(Spectra)interactions.getActiveEntity();
			addLorentzienSpectrum(spectra, nbPoints, peakWidth);
			interactions.repaint();
		}
		interactions.getUserDialog().setText("Done");
		interactions.checkButtonsStatus();
	}

	private static void addLorentzienSpectrum(Spectra spectra, int nbPoints, double peakWidth) {
		addLorentzienSpectrum(spectra, nbPoints, peakWidth,0);
		
	}

	private static void addLorentzienSpectrum(Spectra spectra, int nbPoints,double peakWidth, int i) {
		SpectraData spectraData=spectra.getSpectraData();
		LorentzienFilter filter = new LorentzienFilter(nbPoints, peakWidth);
		spectraData.applyFilter(filter);
		spectra.checkSizeAndPosition();
	}

}
