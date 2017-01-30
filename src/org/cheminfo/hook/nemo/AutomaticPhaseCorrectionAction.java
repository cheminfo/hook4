package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.scripting.spectradata.SD;

public class AutomaticPhaseCorrectionAction extends GeneralAction {
	private static final boolean isDebug = false;

	public static void performAction(InteractiveSurface interactions) {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra)
			spectrum = (Spectra) interactions.getActiveEntity();
		if (spectrum == null
				&& (interactions.getActiveDisplay() instanceof SpectraDisplay && ((SpectraDisplay) interactions
						.getActiveDisplay()).getLastSpectra() != null))
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getLastSpectra();

		if (spectrum != null) {
			SpectraData spectraData = spectrum.getSpectraData();

			long start = System.currentTimeMillis();

			//AutomaticPhaseCorrectionAction.autoCorrectPhase(spectraData);
			new SD().automaticPhase(spectraData);
			
			long end = System.currentTimeMillis();
			
			if (isDebug) {
				System.out.println("elapsed " + ((end - start) / 1000.0));
			}

			spectrum.checkSizeAndPosition();
			spectrum.refreshSensitiveArea();
			interactions.setCurrentAction(null);
			interactions.repaint();
		}
		interactions.getUserDialog().setText("");
	}
}
