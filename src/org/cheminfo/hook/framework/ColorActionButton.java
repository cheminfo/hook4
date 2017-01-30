package org.cheminfo.hook.framework;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;

public class ColorActionButton extends DefaultActionButton
{
	
	int buttonType=ImageButton.RADIOBUTTON;
	Color buttonColor;
	boolean transparent=false;
	

	public ColorActionButton(Color color, String infoMessage, InteractiveSurface interactions)
	{
		super();
		if (color == null)
		{
		//	this.buttonColor=MyTransparency.createTransparentColor(Color.white);
//			this.buttonColor=Color.white;
//			this.transparent=true;
		}
		else
			this.buttonColor=color;
		
		this.infoMessage=infoMessage;
		this.setInteractiveSurface(interactions);
		
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(2);
		
		interactions.addButton(this);
	}

	public ColorActionButton(Image inImage, Color color, String infoMessage, InteractiveSurface interactions)
	{
		this(color, infoMessage, interactions);
		
		this.theImage=inImage;
	}
	
	/*
	public ColorActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(2);
	}
	*/

	protected void performInstantAction()
	{
		super.performInstantAction();
		for (int entity=0; entity < interactions.getActiveEntities().size(); entity++)
		{
			if (interactions.isColorPrimary())
			{
				interactions.takeUndoSnapshot();
				((BasicEntity)interactions.getActiveEntities().elementAt(entity)).setPrimaryColor(this.buttonColor);
			}
			else
				((BasicEntity)interactions.getActiveEntities().elementAt(entity)).setSecondaryColor(this.buttonColor);
		}
			
		interactions.repaint();
	}
	

	protected void handleEvent(MouseEvent ev)
	{
		if (ev.getSource() instanceof BasicEntity)
		{
			BasicEntity tempEntity=(BasicEntity)ev.getSource();
			int state=DefaultActionButton.UP;
			
			switch (ev.getID())
			{
/*				case MouseEvent.MOUSE_CLICKED:
					for (int entity=0; entity < interactions.getEntitiesVector().size(); entity++)
					{
						if (interactions.isColorPrimary())
						{
							if (((BasicEntity)interactions.getEntitiesVector().elementAt(entity)).getPrimaryColor() == this.buttonColor)
								state=DefaultActionButton.DOWN;
						}
						else
						{
							if (((BasicEntity)interactions.getEntitiesVector().elementAt(entity)).getSecondaryColor() == this.buttonColor)
								state=DefaultActionButton.DOWN;
						}
					}
					this.setStatus(state);
					break;
*/
/*				case BasicEntity.MOUSE_DOUBLECLICKED:
					for (int entity=0; entity < interactions.getEntitiesVector().size(); entity++)
					{
						if (((BasicEntity)interactions.getEntitiesVector().elementAt(entity)).getPrimaryColor() == this.buttonColor)
							state=DefaultActionButton.DOWN;
					}
					this.setStatus(state);
					break;
*/					
				default:
					break;
			}
					
		}
	}

	protected void checkButtonStatus()
	{
		int state=DefaultActionButton.UP;;
		
		for (int entity=0; entity < interactions.getActiveEntities().size(); entity++)
		{
			if (interactions.isColorPrimary())
			{
				if (((BasicEntity)interactions.getActiveEntities().elementAt(entity)).getPrimaryColor() == this.buttonColor)
					state=DefaultActionButton.DOWN;
			}
			else
			{
				if (((BasicEntity)interactions.getActiveEntities().elementAt(entity)).getSecondaryColor() == this.buttonColor)
					state=DefaultActionButton.DOWN;
			}
		}
		this.setStatus(state);
	}

	protected void setButtonColor(Color newColor)
	{
		this.buttonColor=newColor;
	}
	
	protected Color getButtonColor()
	{
		return this.buttonColor;
	}
	
	
	public void paint(Graphics g) {
		if(theImage==null) {
			theImage=this.createImage(22,22);
			Graphics tempGraph=theImage.getGraphics();
			if (this.transparent)
			{
				tempGraph.setColor(Color.white);
				tempGraph.fillRect(0,0,21,21);
				tempGraph.setColor(Color.red);
				tempGraph.drawRect(0,0,21,21);
				tempGraph.drawLine(0,21,21,0);
			}
			else
			{
				tempGraph.setColor(buttonColor);	
				tempGraph.fillRect(0,0,21,21);
			}
		}
		
		super.paint(g);
	}
}