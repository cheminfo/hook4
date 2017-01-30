package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

public class LinearAction extends GeneralAction
{
	
	public static void performAction(InteractiveSurface interactions)
	{
		if (interactions.getActiveEntity() instanceof Spectra)
		{
			Spectra spectra=(Spectra)interactions.getActiveEntity();
	
			if (spectra.getSpectraData().getNbSubSpectra() == 2)
			{
				spectra.setSpectraNb(0);
				spectra.getSpectraData().deleteSubSpectraData(1);
			}
			spectra.isSmooth(true);
	
			interactions.getUserDialog().setText("");
			interactions.repaint();
		}
	}
}