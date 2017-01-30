package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class NextActionButton extends DefaultActionButton {

	public NextActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CLASSIC);
	}

	protected void performInstantAction() {
		super.performInstantAction();

		Spectra tempSpectra = null;

		if (interactions.getActiveEntity() instanceof Spectra)
			tempSpectra = (Spectra) interactions.getActiveEntity();

		if (tempSpectra == null)
			tempSpectra = ((SpectraDisplay) interactions.getActiveDisplay())
					.getFirstSpectra();
		if (tempSpectra != null) {
			int newSpectraNb = tempSpectra.getSpectraNb() + 1;
			if (newSpectraNb >= tempSpectra.getSpectraData().getNbSubSpectra())
				newSpectraNb = 0;
			tempSpectra.setSpectraNb(newSpectraNb);
			tempSpectra.checkSizeAndPosition();
			tempSpectra.checkVerticalLimits();
			interactions.repaint();
		}

	}

	protected void checkButtonStatus() {
		if (interactions.getActiveEntity() != null
				&& interactions.getActiveEntity() instanceof Spectra
				&& ((Spectra) interactions.getActiveEntity()).getSpectraData()
						.getNbSubSpectra() > 1
				&& ((Spectra) interactions.getActiveEntity()).getSpectraData()
						.getDataType() != SpectraData.TYPE_2DNMR_SPECTRUM) // ||
																			// ((BasicEntity)interactions.getEntitiesVector().elementAt(0)).getParent()
																			// instanceof
																			// Spectra)
																			// &&
																			// interactions.getActiveSpectra().getSpectraData().getNbSubSpectra()
																			// > 1
																			// &&
																			// interactions.getActiveSpectra().getSpectraData().getDataType()
																			// !=
																			// SpectraData.TYPE_2DNMR_SPECTRUM)
			this.activate();
		else
			this.deactivate();
	}
}