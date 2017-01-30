package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class DefaultSpectraActionButton extends DefaultActionButton {

	public DefaultSpectraActionButton(Image inImage, String infoMessage, InteractiveSurface interactions, int newGroupNo, int buttonType, char[] shortcutKeys) {
		super(inImage, infoMessage, interactions, newGroupNo, buttonType, shortcutKeys);
	}
	
	void activateIf1DNMR() {
		if (interactions.getActiveDisplay() != null	&& interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();
	
			Spectra activeSpectra = null;
			if (interactions.getActiveEntity() instanceof Spectra) {
				activeSpectra = (Spectra) interactions.getActiveEntity();
			} else if (interactions.getActiveEntity() != null && interactions.getActiveEntity().getParentEntity() != null
																&& interactions.getActiveEntity().getParentEntity() instanceof Spectra) {
				activeSpectra = (Spectra) interactions.getActiveEntity().getParentEntity();
			}
	
			if (activeSpectra != null) {
				if (activeSpectra.spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM)
					this.activate();
				else
					this.deactivate();
			} else {
				if ((activeDisplay != null)
						&& (activeDisplay.getFirstSpectra() != null)
						&& (activeDisplay.getFirstSpectra().getSpectraData().getDataType() == SpectraData.TYPE_NMR_SPECTRUM))
					this.activate();
				else
					this.deactivate();
			}
		} else {
			this.deactivate();
		}
	}
}
