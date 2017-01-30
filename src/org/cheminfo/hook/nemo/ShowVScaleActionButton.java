package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;


public class ShowVScaleActionButton extends DefaultActionButton
{

	public ShowVScaleActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CHECKBUTTON);
	}

	protected void performInstantAction()
	{
		((SpectraDisplay)interactions.getActiveDisplay()).switchVScale();
		interactions.getActiveDisplay().checkSizeAndPosition();
		interactions.repaint();
	}
	

	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
		{
			this.activate();
			if (interactions.getActiveDisplay() != null)
			{
				SpectraDisplay tempDisplay=(SpectraDisplay)interactions.getActiveDisplay();
				
				if (tempDisplay.hasVScale()) this.setStatus(DefaultActionButton.DOWN);
				else this.setStatus(DefaultActionButton.UP);
			}
		}
		else this.deactivate();
	}
}

