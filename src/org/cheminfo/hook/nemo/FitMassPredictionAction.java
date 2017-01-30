package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.framework.InteractiveSurface;

public class FitMassPredictionAction {
	public static void performAction(InteractiveSurface interactions)
	{
		Vector ff=interactions.getActiveEntities();
		for(int i=0;i<ff.size();i++){
			if (ff.get(i) instanceof Spectra)
			{
				Spectra spectra=(Spectra)interactions.getActiveEntity();
				System.out.println("i "+i);
				//addGaussianSpectrum(spectra, nbPoints, peakWidth, gshift);
				interactions.repaint();
			}
		}
		
	}
}
