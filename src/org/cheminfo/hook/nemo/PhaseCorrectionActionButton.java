package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.Vector;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionHWFilter;
import org.cheminfo.hook.nemo.filters.BaselineCorrectionWFilter;
import org.cheminfo.hook.nemo.filters.FourierTransformFilter;
import org.cheminfo.hook.nemo.filters.PhaseCorrectionFilter;
import org.cheminfo.hook.nemo.filters.PostFourierTransformFilter;
import org.cheminfo.hook.nemo.filters.SpectraFilter;

public class PhaseCorrectionActionButton extends DefaultActionButton {


	public PhaseCorrectionActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	}

	protected void performInstantAction() {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra) {
			spectrum = (Spectra) interactions.getActiveEntity();
		} else if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay())
						.getLastSpectra() != null) {
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getLastSpectra();
		}
		if (spectrum != null) {
			interactions.getUserDialog().setText("Phasing ...");
			interactions.repaint();
			AutomaticPhaseCorrectionAction.performAction(interactions);
			interactions.repaint();
		}
	}

	protected void handleKeyEvent(KeyEvent ev) {
	}

	protected void checkButtonStatus() {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra)
			spectrum = (Spectra) interactions.getActiveEntity();
		if (spectrum == null
				&& interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay display = (SpectraDisplay) interactions
					.getActiveDisplay();
			spectrum = display.getFirstSpectra();
		}
		if (spectrum == null) {
			this.deactivate();
		} else {
			SpectraData spectraData = spectrum.getSpectraData();
			if (spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM
					&& spectraData.getNbSubSpectra() > 1) {
				Vector<SpectraFilter> appliedFilters = spectraData
						.getAppliedFilters();
				if (appliedFilters.size() == 0) {
					this.activate();
				} else {
					SpectraFilter lastFilter = appliedFilters.lastElement();
					if (lastFilter instanceof FourierTransformFilter
							|| lastFilter instanceof PhaseCorrectionFilter
							|| lastFilter instanceof BaselineCorrectionHWFilter
							|| lastFilter instanceof BaselineCorrectionWFilter
							|| lastFilter instanceof PostFourierTransformFilter) {
						this.activate();
					} else {
						this.deactivate();
					}
				}
			} else {
				this.deactivate();
			}
		}
	}
}
