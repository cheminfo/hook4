package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.FourierTransformFilter;
import org.cheminfo.hook.nemo.filters.PostFourierTransformFilter;
import org.cheminfo.hook.nemo.filters.ZeroFillingFilter;

public class FourierTransformActionButton extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	public FourierTransformActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);

		// this.shortcutKey="P";
	}

	protected void performInstantAction() {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra) 
			spectrum = (Spectra)interactions.getActiveEntity();
		if (spectrum == null && interactions.getActiveDisplay() instanceof SpectraDisplay)
			spectrum = ((SpectraDisplay)interactions.getActiveDisplay()).getLastSpectra();
		if (spectrum != null) {
			SpectraData spectraData = spectrum.getSpectraData();
			FourierTransformFilter fftFilter = new FourierTransformFilter();
			if (fftFilter.isApplicable(spectraData)) {
				// Symmetrize by zero filling first, if the spectrum
				// is 2D
				int nbSubSpectra = spectraData.getNbSubSpectra();
				if (nbSubSpectra > 2) {
					int nbPoints = spectraData.getNbPoints();
					if (nbSubSpectra / 2 != nbPoints) {
						int diff = nbPoints - nbSubSpectra / 2; 
						ZeroFillingFilter zeroFillingFilter = new ZeroFillingFilter(0,diff);
						if (zeroFillingFilter.isApplicable(spectraData)) {
							spectraData.applyFilter(zeroFillingFilter);
						}
					}
				} else {
					// by default apply zero filling by doubling the length of the spectrum
					int nbPoints = spectraData.getNbPoints();
					int nbNewPoints = nbPoints * 2;
					double pow = Math.log(nbNewPoints) / Math.log(2);
					int neededPoints = (int) Math.round(Math.pow(2, Math.max(
							Math.round(pow), Math.ceil(pow))));
					if (neededPoints != nbPoints)
						spectraData.applyFilter(new ZeroFillingFilter(neededPoints- nbPoints));
				}
				spectraData.applyFilter(fftFilter);
				// FourierTransformAction.resetScales(spectrum);
				PostFourierTransformFilter posfftFilter = new PostFourierTransformFilter();
				if (posfftFilter.isApplicable(spectraData)) {
					System.out.println("Post fft "+posfftFilter.getPh1corr());
					spectraData.applyFilter(posfftFilter);
				}
			}
			// spectrum.refreshSensitiveArea();
			
			((SpectraDisplay)(interactions.getActiveDisplay())).checkAndRepaint();
			interactions.setActiveEntity(spectrum);
		}
		
	}

	protected void handleEvent(MouseEvent ev) {

	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay display = (SpectraDisplay) interactions
					.getActiveDisplay();
			Spectra spectrum = display.getLastSpectra();
			if (spectrum == null) {
				this.deactivate();
			} else {
				SpectraData spectraData = spectrum.getSpectraData();
				if (spectraData.isNmrFid())
					this.activate();
				else
					this.deactivate();
			}
		} else {
			this.deactivate();
		}
	}
}
