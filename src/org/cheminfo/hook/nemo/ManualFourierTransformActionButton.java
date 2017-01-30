package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class ManualFourierTransformActionButton extends DefaultActionButton {


	public ManualFourierTransformActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	}

	protected void performInstantAction() {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra) 
			spectrum = (Spectra)interactions.getActiveEntity();
		if (spectrum == null && interactions.getActiveDisplay() instanceof SpectraDisplay)
			spectrum = ((SpectraDisplay)interactions.getActiveEntity()).getLastSpectra();
		
		if (spectrum != null) {
			XMLCoDec zeroFillingChoice = new XMLCoDec();
			zeroFillingChoice.addParameter("name", "zeroFilling");
			int nChoices = 5;
			zeroFillingChoice.addParameter("nbChoices",new Integer(nChoices));
			for (int iChoice = 0; iChoice < nChoices; iChoice++) {
				zeroFillingChoice.addParameter("choice"+iChoice, (int)Math.pow(2, iChoice));
			}
			zeroFillingChoice.addParameter("active", (int)Math.pow(2, 0));
			
			// button
			XMLCoDec buttonCodec = new XMLCoDec();
			buttonCodec.addParameter("action", "org.cheminfo.hook.nemo.FourierTransformAction");
			buttonCodec.addParameter("image", "validate.gif");
			interactions.getUserDialog().setText(
					"<Text type=\"plain\">Zero filling: </Text>" + 
					"<Choice " + zeroFillingChoice.encodeParameters()	+ "></Choice>" +
					"<Button " + buttonCodec.encodeParameters() + "></Button>");
			interactions.repaint();
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
