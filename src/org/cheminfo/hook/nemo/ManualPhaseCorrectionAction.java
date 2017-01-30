package org.cheminfo.hook.nemo;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.FilterType;
import org.cheminfo.hook.nemo.filters.PhaseCorrectionFilter;
import org.cheminfo.hook.nemo.filters.SpectraFilter;

public class ManualPhaseCorrectionAction extends GeneralAction {

	public static void performAction(InteractiveSurface interactions) {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra)
			spectrum = (Spectra) interactions.getActiveEntity();
		if (spectrum == null
				&& (interactions.getActiveDisplay() instanceof SpectraDisplay && ((SpectraDisplay) interactions
						.getActiveDisplay()).getLastSpectra() != null))
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getLastSpectra();

		SpectraData spectraData = spectrum.getSpectraData();
		if (spectrum != null) {
			// check whether we can apply a phase correction first
			PhaseCorrectionFilter filter = null;
			SpectraFilter spectraFilter = spectraData.getAppliedFilterByFilterType(FilterType.PHASE_CORRECTION);

			if (spectraFilter instanceof PhaseCorrectionFilter)
				filter= (PhaseCorrectionFilter)spectraFilter;
			
			if (filter == null) {
				filter = new PhaseCorrectionFilter();
				if (!filter.isApplicable(spectraData)) {
					interactions.getUserDialog().setText("");
					return;
				}
				spectraData.applyFilter(filter);
			}
			DefaultActionButton button = interactions.getButtonByClassName("org.cheminfo.hook.nemo.ManualPhaseCorrectionActionButton");
			if (button != null) {
				ManualPhaseCorrectionActionButton actionButton = (ManualPhaseCorrectionActionButton)button;
				interactions.setCurrentAction(actionButton);
				try {
				actionButton.setManual();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			spectrum.checkSizeAndPosition();
			spectrum.refreshSensitiveArea();
			interactions.repaint();
		} else {
			interactions.getUserDialog().setText("");
		}
	}
}

