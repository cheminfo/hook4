package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.filters.ApodizationFilter;
import org.cheminfo.hook.util.XMLCoDec;

public class ApodizationActionButton extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
//	String infoMessage = "Apodization";
//	int buttonType = ImageButton.CHECKBUTTON;
//	PeakLabel tempPeakLabel=null;

	/*
	public ApodizationActionButton() {
		super();
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public ApodizationActionButton(Image inImage) {
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}
*/
	public ApodizationActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
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
		interactions.setCurrentAction(this);
		if (interactions.getActiveEntity() instanceof Spectra) {
			XMLCoDec choiceCodec = new XMLCoDec();
			choiceCodec.addParameter("name", "function");
			String[] availableFunctions = ApodizationFilter.getAvailableFunctions();
			int nbChoices = availableFunctions.length;
			choiceCodec.addParameter("nbChoices",new Integer(nbChoices));
			for (int iChoice = 0; iChoice < nbChoices; iChoice++) {
				choiceCodec.addParameter("choice"+iChoice, availableFunctions[iChoice]);
			}
			choiceCodec.addParameter("active", availableFunctions[0]);
			// button
			XMLCoDec buttonCodec = new XMLCoDec();
			buttonCodec.addParameter("action",
					"org.cheminfo.hook.nemo.ApodizationAction");
			buttonCodec.addParameter("image", "validate.gif");
			// LB input
			XMLCoDec lineBroadeningCodec = new XMLCoDec();
			lineBroadeningCodec.addParameter("name", "lineBroadening");
			lineBroadeningCodec.addParameter("size", new Integer(5));
			
			XMLCoDec zeroPaddingCodec = new XMLCoDec();
			zeroPaddingCodec.addParameter("name", "zeropadding");
			zeroPaddingCodec.addParameter("size", new Integer(5));

			String line = "<Text type=\"plain\">Apodization function & LB [Hz]: </Text>" + 
			"<Choice " + choiceCodec.encodeParameters()	+ "></Choice>" +
//			"<Text type=\"plain\">LB = </Text>" +
			"<Input " + lineBroadeningCodec.encodeParameters() + ">1.00</Input>"+
//			"<Text type=\"plain\"> Hz</Text>" + 
			"<Text type=\"plain\">Circular shift: </Text>" + 
			"<Input " + zeroPaddingCodec.encodeParameters() + ">+</Input>"+
			"<Button " + buttonCodec.encodeParameters() + "></Button>";
			interactions.getUserDialog().setText(line);
		}
	}

	protected void handleEvent(MouseEvent ev) {

	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay display = (SpectraDisplay) interactions
					.getActiveDisplay();
			Spectra spectrum = display.getFirstSpectra();
			if (spectrum == null) {
				this.deactivate();
				return;
			}
			SpectraData spectraData = spectrum.getSpectraData();
			if (spectraData == null) {
				this.deactivate();
				return;
			}
			if (spectraData.getDataType() == SpectraData.TYPE_NMR_FID)
				this.activate();
			else
				this.deactivate();
		} else {
			this.deactivate();
		}
	}

}
