package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.PhaseCorrectionFilter;
import org.cheminfo.hook.nemo.filters.SpectraFilter;
import org.cheminfo.hook.nemo.nmr.PhaseCorrectionByEntropy;

public class PhaseCorrectionAction extends GeneralAction {

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
			}

			String correctionMethod = interactions.getUserDialog()
					.getParameter("method");
			if (correctionMethod.equals("automatic")) {
				// // now lets run the phase correction
				
				PhaseCorrectionByEntropy phaseOptimizer = new PhaseCorrectionByEntropy();
				phaseOptimizer.setNmrSpectrum(spectraData);
				double[] phaseParameters = phaseOptimizer.getPhaseCorrection(0,
						0);
				double phi0 = phaseParameters[0];
				double phi1 = phaseParameters[1];
				filter.addCorrection(phi0, phi1);
				filter.apply(spectraData);
			}
			if (spectrum.getParentEntity() != null
					&& spectrum.getParentEntity() instanceof SpectraDisplay) {
				((SpectraDisplay)spectrum.getParentEntity()).fullSpectra();
			}
			spectrum.checkSizeAndPosition();
			spectrum.refreshSensitiveArea();
			interactions.repaint();
		} else {
		}
		interactions.getUserDialog().setText("");
	}

}
