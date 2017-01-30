package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.PhaseCorrectionFilter;
import org.cheminfo.hook.nemo.filters.SpectraFilter;

public class PhaseCorrectionAllDoneAction extends GeneralAction {
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
			// check whether we can apply a phase correction first
			SpectraData spectraData = spectrum.getSpectraData();
			Vector<SpectraFilter> filters = spectraData.getAppliedFilters();
			PhaseCorrectionFilter filter = null;
			for (int i = 0; i < filters.size(); i++) {
				if (filters.elementAt(i) instanceof PhaseCorrectionFilter) {
					filter = (PhaseCorrectionFilter) filters.elementAt(i);
					break;
				}
			}
			if (filter == null) {
				filter = new PhaseCorrectionFilter();
				if (!filter.isApplicable(spectraData)) {
					interactions.getUserDialog().setText("");
					return;
				}
				spectraData.applyFilter(filter);
			} else {
				if (isDebug) {
					System.out.println("phase0="+filter.getPhi0()+"phase1="+filter.getPhi1());
					System.out.println("[degrrees]:");					
					System.out.println("phase0="+(filter.getPhi0()/(2*Math.PI)*360)+"phase1="+(filter.getPhi1()/(2*Math.PI)*360));
				}
			}
			DefaultActionButton button = interactions
					.getButtonByClassName("org.cheminfo.hook.nemo.ManualPhaseCorrectionActionButton");
			if (button != null) {
				ManualPhaseCorrectionActionButton actionButton = (ManualPhaseCorrectionActionButton) button;
				interactions.setCurrentAction(actionButton);
				try {
					actionButton.cleanUp();
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
