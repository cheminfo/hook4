package org.cheminfo.hook.graphdraw2;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class GraphSlideActionButton extends DefaultActionButton
{
	char [] shortcutKey = {'m','M'};
	String infoMessage="Move Spectra Horizontally";
	int buttonType=ImageButton.RADIOBUTTON;
	private double originalLeftLimit, originalRightLimit, originalTopLimit, originalBottomLimit;
	
	public GraphSlideActionButton()
	{
		super();
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}
	
	public GraphSlideActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public GraphSlideActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
	{
		super(inImage);
		
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);
		
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
		
	}

	protected void performInstantAction()
	{
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	protected void handleEvent(MouseEvent ev)	
	{
		super.handleEvent(ev);


		if (interactions.getCurrentAction() == this)
		{
			if (interactions.getActiveDisplay() instanceof GraphDisplay2)
			{
				GraphDisplay2 activeDisplay=(GraphDisplay2)interactions.getActiveDisplay();

				switch (ev.getID())
				{
					case MouseEvent.MOUSE_PRESSED:
						this.originalLeftLimit=activeDisplay.getLeftLimit();
						this.originalRightLimit=activeDisplay.getRightLimit();
						this.originalTopLimit=activeDisplay.getTopLimit();
						this.originalBottomLimit=activeDisplay.getBottomLimit();
						
						interactions.takeUndoSnapshot();
						break;
						
					case MouseEvent.MOUSE_DRAGGED:
						double	slideUnitsX=(interactions.getReleasePoint().x-interactions.getContactPoint().x)*activeDisplay.unitsPerPixelH();
						double	slideUnitsY=(interactions.getReleasePoint().y-interactions.getContactPoint().y)*activeDisplay.unitsPerPixelV();
						activeDisplay.setCurrentLimits(originalLeftLimit+slideUnitsX, originalRightLimit+slideUnitsX, originalTopLimit+slideUnitsY, originalBottomLimit+slideUnitsY);
						activeDisplay.checkSizeAndPosition();
						interactions.repaint();
						
					default:
						break;
				}
			}
		}
	}

	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() instanceof GraphDisplay2)
			this.activate();
		else this.deactivate();
	}

}