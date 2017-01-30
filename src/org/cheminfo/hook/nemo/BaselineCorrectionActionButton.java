package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class BaselineCorrectionActionButton extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	String infoMessage = "Baseline correction";
//	int buttonType = ImageButton.CHECKBUTTON;
//	PeakLabel tempPeakLabel;


	public BaselineCorrectionActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
		
/*
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
*/
//		this.tempPeakLabel = null;
		// this.shortcutKey="P";
	}

	protected void performInstantAction() {
		
		System.out.println("Perform action BaselineCorrectionActionButton");
		
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
			AutomaticBaselineCorrectionAction.performAction(interactions);
			interactions.repaint();
		}
	}



	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() != null && interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();

			Spectra activeSpectra = null;
			if (interactions.getActiveEntity() instanceof Spectra)
				activeSpectra = (Spectra) interactions.getActiveEntity();
			else if (interactions.getActiveEntity() != null
					&& interactions.getActiveEntity().getParentEntity() != null
					&& interactions.getActiveEntity().getParentEntity() instanceof Spectra)
				activeSpectra = (Spectra) interactions.getActiveEntity()
						.getParentEntity();

			SpectraData spectraData = null;
			if (activeSpectra != null)
				spectraData = activeSpectra.getSpectraData();

			if (spectraData == null && (activeDisplay != null)
					&& (activeDisplay.getFirstSpectra() != null))
				spectraData = activeDisplay.getFirstSpectra().getSpectraData();
			if (spectraData == null) {
				this.deactivate();
			} else {
				switch (spectraData.getDataType()) {
				case SpectraData.TYPE_NMR_SPECTRUM:
					if (spectraData.getSimulationDescriptor().compareTo("") == 0)
						this.activate();
					else
						this.deactivate();
					break;
				case SpectraData.TYPE_2DNMR_SPECTRUM:
					this.activate();
					break;
				default:
					this.deactivate();
				}
			}
		} else {
			this.deactivate();
		}
	}

}
