package org.cheminfo.hook.framework;

import java.awt.Image;

public class PrimarySecondaryActionButton extends DefaultActionButton
{
	public PrimarySecondaryActionButton()
	{
		super();
		this.setButtonType(ImageButton.CHECKBUTTON);
		this.setGroupNb(0);
	}

	public PrimarySecondaryActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.CHECKBUTTON);
		this.setGroupNb(0);
	}

	public PrimarySecondaryActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
	{
		super(inImage);
		
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);
		
		this.setButtonType(ImageButton.CHECKBUTTON);
		this.setGroupNb(0);
		
	}

	protected void performInstantAction()
	{
		interactions.isColorPrimary(!interactions.isColorPrimary());
		interactions.checkButtonsStatus();
	}
	
	protected void checkButtonStatus()
	{
		if (interactions.isColorPrimary())
			this.setStatus(DefaultActionButton.UP);
		else
			this.setStatus(DefaultActionButton.DOWN);
	}
}