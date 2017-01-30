package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class AssignPredictionActionButton extends DefaultActionButton {
	public static final boolean isDebug = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AssignPredictionActionButton(Image inImage, String infoMessage,InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
		/*
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);

		this.tempPeakLabel = null;
		// this.shortcutKey="P";
		 */
	}

	protected void performInstantAction() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			 XMLCoDec choiceCodec = new XMLCoDec();
			 choiceCodec.addParameter("name", "method");
			 choiceCodec.addParameter("nbChoices", AssignPredictionAction.methods.length);
			 choiceCodec.addParameter("choice0", AssignPredictionAction.methods[0]);
			 choiceCodec.addParameter("choice1", AssignPredictionAction.methods[1]);
			 choiceCodec.addParameter("active", AssignPredictionAction.methods[0]);
			String tempString = "";
			tempString += "<Text type=\"plain\">Method: </Text> " +
			 "<Choice " + choiceCodec.encodeParameters()+ "></Choice>";

			XMLCoDec buttonCodec1 = new XMLCoDec();
			buttonCodec1.addParameter("action",
					"org.cheminfo.hook.nemo.AssignPredictionAction");
			buttonCodec1.addParameter("image", "validate.gif");

			tempString += "<Button " + buttonCodec1.encodeParameters()
					+ "></Button>";
			interactions.getUserDialog().setText(tempString);
		}
	}



	protected void handleEvent(MouseEvent ev) {

	}

	protected void checkButtonStatus() {
		this.activate();
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay spectraDisplay = (SpectraDisplay) interactions.getActiveDisplay();
			Spectra firstSpectrum = spectraDisplay.getFirstSpectra();
			if (firstSpectrum != null) {
				if (firstSpectrum.isDrawnAs2D() && firstSpectrum.getPredictionData() == null) {
					this.activate();
				} else if (spectraDisplay.getFirstSpectra() != spectraDisplay.getLastSpectra()
						&& (spectraDisplay.getLastSpectra().getSpectraData()
								.getSimulationDescriptor().compareTo("") == 0 || spectraDisplay
								.getFirstSpectra().getSpectraData()
								.getSimulationDescriptor().compareTo("") == 0)) {
					this.activate();
				} else {
					this.deactivate();
				}
			} else {
				this.deactivate();
			}
		} else {
			this.deactivate();
		}
	}

}
