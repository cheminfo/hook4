package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionWFilter;

public class ManualBaselineCorrectionAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {
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
			SpectraDisplay display = (SpectraDisplay) interactions
					.getActiveDisplay();
			spectrum = display.getLastSpectra();
			SpectraData spectraData = spectrum.getSpectraData();
			BaselineCorrectionWFilter filter = new BaselineCorrectionWFilter();
			Vector<BasicEntity> obsoleteLabels = new Vector<BasicEntity>();
			for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
				if (spectrum.getEntity(ent) instanceof RangeSelectionLabel) {
					RangeSelectionLabel label = (RangeSelectionLabel) spectrum
							.getEntity(ent);
					obsoleteLabels.add(label);
					int a = spectrum.unitsToArrayPoint(label.getXMin());
					int b = spectrum.unitsToArrayPoint(label.getXMax());
					int ia = Math.min(a, b);
					int ib = Math.max(a, b);
					filter.addRange(ia, ib);
				}
			}
			if (obsoleteLabels.size() < 2) {
				interactions.getUserDialog().setText(
						"Insufficient range labels");
			} else {
				spectraData.applyFilter(filter);
				for (int ent = 0; ent < obsoleteLabels.size(); ent++)
					spectrum.remove(obsoleteLabels.elementAt(ent));
				display.setCursorType(SpectraDisplay.NONE);
				interactions.setCurrentAction(null);
				interactions.getUserDialog().setText("");
				interactions.checkButtonsStatus();
			}
			spectrum.checkAllIntegrals();
			spectrum.refreshSensitiveArea();
			spectrum.checkSizeAndPosition();
		}

	}
}
