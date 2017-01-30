package org.cheminfo.hook.framework;

import java.awt.Image;

public class UndoActionButton extends DefaultActionButton
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6175743659814481163L;
	String infoMessage="About Nemo";
	int buttonType=ImageButton.CLASSIC;
	

	public UndoActionButton()
	{
		super();
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}

	public UndoActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}

	public UndoActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
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
		super.performInstantAction();
		
		
		interactions.undo();
	}
	
}


