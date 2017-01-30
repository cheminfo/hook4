package org.cheminfo.hook.framework;

import java.awt.Image;

public class SendToBackActionButton extends DefaultActionButton
{
	
	String infoMessage;
	int buttonType=ImageButton.CLASSIC;
	private Class activeClass=null; 

	public SendToBackActionButton(Image inImage, String infoMessage, InteractiveSurface interactions, Class activeClass)
	{
		super(inImage);
		this.activeClass=activeClass;
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);		
		this.setButtonType(ImageButton.CLASSIC);
		this.setGroupNb(0);
	}

	protected void performInstantAction()
	{
		interactions.takeUndoSnapshot();
		BasicEntity entity = (BasicEntity)interactions.getActiveEntity();
		
		if (entity != null)
		{
			BasicEntity parent = (BasicEntity)entity.getParentEntity();
			
			if (parent != null)
			{
				parent.remove(entity);
				parent.addEntity(entity, 0);
				interactions.repaint();
			}
		}
	}
	
	protected void checkButtonStatus()
	{
		if ((interactions!=null) && (interactions.getActiveEntity() !=null) && (interactions.getActiveEntity().getClass()==activeClass))
			this.activate();
		else this.deactivate();
	}		
}
