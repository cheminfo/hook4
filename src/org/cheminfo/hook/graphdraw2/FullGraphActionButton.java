package org.cheminfo.hook.graphdraw2;

import java.awt.Image;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;


public class FullGraphActionButton extends DefaultActionButton
{
	
	String infoMessage="Full Graph";
	int buttonType=ImageButton.CLASSIC;

	public FullGraphActionButton()
	{
		super();
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}
		
	public FullGraphActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}

	public FullGraphActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
	{
		super(inImage);
		
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);
		
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}

	protected void performInstantAction()
	{
		interactions.takeUndoSnapshot();
		((GraphDisplay2)interactions.getActiveDisplay()).fullout();
		((GraphDisplay2)interactions.getActiveDisplay()).checkSizeAndPosition();
		((GraphDisplay2)interactions.getActiveDisplay()).refreshSensitiveArea();
		interactions.repaint();
	}
	
	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() instanceof GraphDisplay2)
			this.activate();
		else this.deactivate();
	}		
}
