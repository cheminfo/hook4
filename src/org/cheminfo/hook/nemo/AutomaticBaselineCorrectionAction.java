package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionHWFilter;

public class AutomaticBaselineCorrectionAction extends GeneralAction {
	private static final boolean isDebug = false;

	public static void performAction(InteractiveSurface interactions) {
		
		System.out.println("Perform action AutomaticBaselineCorrectionAction");
		
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra)
			spectrum = (Spectra) interactions.getActiveEntity();
		else if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay())
						.getFirstSpectra() != null) {
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getFirstSpectra();
		}
		if (spectrum != null) {
			SpectraData spectraData = spectrum.getSpectraData();
			switch (spectraData.getDataType()) {
			case SpectraData.TYPE_2DNMR_SPECTRUM:
				spectraData.applyFilter(new BaselineCorrectionHWFilter());
				break;
			case SpectraData.TYPE_NMR_SPECTRUM:
				spectraData.applyFilter(new BaselineCorrectionHWFilter());
				break;
			}
			SpectraDisplay display = (SpectraDisplay) spectrum.getParentEntity();
			display.setCursorType(SpectraDisplay.NONE);
			spectrum.checkAllIntegrals();
			spectrum.checkSizeAndPosition();
			spectrum.refreshSensitiveArea();
			interactions.checkButtonsStatus();
			interactions.setCurrentAction(null);
			interactions.getUserDialog().setText("");
			interactions.repaint();
		}
	}

}