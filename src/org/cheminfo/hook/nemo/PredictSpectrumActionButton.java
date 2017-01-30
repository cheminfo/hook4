package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class PredictSpectrumActionButton extends DefaultActionButton {


	public PredictSpectrumActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);

		// this.shortcutKey="P";
	}

	protected void performInstantAction() {
		interactions.setCurrentAction(this);
		XMLCoDec choiceCodec = new XMLCoDec();
		choiceCodec.addParameter("name", "nucleus");
		choiceCodec.addParameter("nbChoices", 5);
		choiceCodec.addParameter("choice0","1H");
		choiceCodec.addParameter("choice1", "13C");
		choiceCodec.addParameter("choice2", "HMBC");
		choiceCodec.addParameter("choice3", "HSQC");
		choiceCodec.addParameter("choice4", "COSY");
		choiceCodec.addParameter("active", "1H");
		
		// button
		XMLCoDec buttonCodec = new XMLCoDec();
		buttonCodec.addParameter("action",
				"org.cheminfo.hook.nemo.PredictSpectrumAction");
		buttonCodec.addParameter("image", "validate.gif");
		interactions.getUserDialog().setText(
				"<Text type=\"plain\">Nucleus: </Text>"
						+ "<Choice " + choiceCodec.encodeParameters()
						+ "></Choice>" + "<Button "
						+ buttonCodec.encodeParameters() + "></Button>");
	}

	protected void handleEvent(MouseEvent ev) {

	}

	protected void checkButtonStatus() {
		if (interactions.getEntityByName("molDisplay") != null)
			this.activate();
		else
			this.deactivate();
	}

}
