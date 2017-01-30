package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.GaussianFilter;

public class GaussianAction extends GeneralAction {
	
	public static void performAction(InteractiveSurface interactions) {
		int nbPoints=Integer.valueOf(interactions.getUserDialog().getParameter("nbPoints")).intValue();
		double peakWidth=Double.valueOf(interactions.getUserDialog().getParameter("peakWidth")).doubleValue();
		//double gshift=Double.valueOf(interactions.getUserDialog().getParameter("globalShift")).doubleValue();
		// interactions.getUserDialog().setText("Applying the gaussian filter.");
		if (interactions.getActiveEntity() instanceof Spectra)
		{
			Spectra spectra=(Spectra)interactions.getActiveEntity();
			addGaussianSpectrum(spectra, nbPoints, peakWidth);
			interactions.repaint();
		}
		interactions.getUserDialog().setText("Done");
		interactions.checkButtonsStatus();
	}

	protected static void addGaussianSpectrum(Spectra spectra, int nbPoints, double peakWidth) {
		addGaussianSpectrum(spectra, nbPoints, peakWidth,0);
	}
	
	protected static void addGaussianSpectrum(Spectra spectra, int nbPoints, double peakWidth, double globalShift) {
		SpectraData spectraData=spectra.getSpectraData();
		GaussianFilter filter = new GaussianFilter(nbPoints, peakWidth);
		spectraData.applyFilter(filter);
		spectra.checkSizeAndPosition();
	}
	
}

