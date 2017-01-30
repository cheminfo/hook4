package org.cheminfo.hook.graphdraw2;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class Graph2DZoomActionButton extends DefaultActionButton
{
	String infoMessage="2D Zoom";
	
	int buttonType=ImageButton.RADIOBUTTON;
	private Vector previousActiveEntities=null;
	private double firstSelLimitH, firstSelLimitV;
	
	private double originalValue1, originalValue2;
	
	private BasicEntity targetEntity=null;
	
	public Graph2DZoomActionButton()
	{
		super();
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public Graph2DZoomActionButton(Image inImage)
	{
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public Graph2DZoomActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
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
			switch (ev.getID())
			{
				case MouseEvent.MOUSE_PRESSED:
					GraphDisplay2 activeDisplay=null;
					
					if (interactions.getOverEntity() instanceof GraphAxis)
					{
						targetEntity=interactions.getOverEntity();
						activeDisplay=(GraphDisplay2)targetEntity.getParentEntity();
						
						if( ((GraphAxis)targetEntity).getAxisOrientation() == GraphAxis.HORIZONTAL)
						{
							this.originalValue1=((GraphDisplay2)targetEntity.getParentEntity()).getLeftLimit();
							this.originalValue2=((GraphDisplay2)targetEntity.getParentEntity()).getRightLimit();
						}
						else
						{
							this.originalValue1=((GraphDisplay2)targetEntity.getParentEntity()).getBottomLimit();
							this.originalValue2=((GraphDisplay2)targetEntity.getParentEntity()).getTopLimit();
						}
					}
					else
					{
						if (interactions.getOverEntity() instanceof GraphDisplay2)
							activeDisplay=(GraphDisplay2)interactions.getOverEntity();
						else if (interactions.getOverEntity().getParentEntity() instanceof GraphDisplay2)
							activeDisplay=(GraphDisplay2)interactions.getOverEntity().getParentEntity();
						
						if (activeDisplay != null)
						{
							targetEntity=activeDisplay;
							this.previousActiveEntities=(Vector)interactions.getActiveEntities().clone();
							interactions.setActiveEntity(activeDisplay);
							interactions.takeUndoSnapshot();
							firstSelLimitH=activeDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
							firstSelLimitV=activeDisplay.absolutePixelsToUnitsV(interactions.getContactPoint().y);
							activeDisplay.setCursorType(BasicDisplay.RECT);
						}
					}
					break;
					
				case MouseEvent.MOUSE_RELEASED:
					if ( !(targetEntity instanceof GraphAxis) )
					{
						if (interactions.getActiveEntity() instanceof GraphDisplay2)
						{
							double leftSelection, rightSelection, topSelection, bottomSelection;
							
							activeDisplay=(GraphDisplay2)interactions.getActiveDisplay();
							
							if (interactions.getReleasePoint().x < interactions.getContactPoint().x)
							{
								leftSelection=activeDisplay.absolutePixelsToUnitsH(interactions.getReleasePoint().x);
								rightSelection=activeDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
							}
							else
							{
								rightSelection=activeDisplay.absolutePixelsToUnitsH(interactions.getReleasePoint().x);
								leftSelection=activeDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
							}							
	
							if (interactions.getReleasePoint().y < interactions.getContactPoint().y)
							{
								topSelection=activeDisplay.absolutePixelsToUnitsV(interactions.getReleasePoint().y);
								bottomSelection=activeDisplay.absolutePixelsToUnitsV(interactions.getContactPoint().y);
							}
							else
							{
								bottomSelection=activeDisplay.absolutePixelsToUnitsV(interactions.getReleasePoint().y);
								topSelection=activeDisplay.absolutePixelsToUnitsV(interactions.getContactPoint().y);
							}							
	
							if (Math.abs(interactions.getReleasePoint().x-interactions.getContactPoint().x) > 1 && Math.abs(interactions.getReleasePoint().y-interactions.getContactPoint().y) > 1)
							{
								activeDisplay.setCurrentLimits(leftSelection, rightSelection, topSelection, bottomSelection);
							}
	
							activeDisplay.setCursorType(BasicDisplay.NONE);
							activeDisplay.refreshSensitiveArea();
							activeDisplay.checkSizeAndPosition();
							
							interactions.clearActiveEntities();
							for (int ent=0; ent < this.previousActiveEntities.size(); ent++)
								interactions.addActiveEntity((BasicEntity)this.previousActiveEntities.elementAt(ent));
							
							interactions.repaint();
						}
					}
					break;
					
				case MouseEvent.MOUSE_DRAGGED:
					if (targetEntity instanceof GraphAxis)
					{
						double newValue1, newValue2;
						if ( ((GraphAxis)targetEntity).getAxisOrientation() == GraphAxis.HORIZONTAL)
						{

						}
						else
						{
							
						}
						
						interactions.repaint();
					}
					
					if (interactions.getActiveEntity() instanceof GraphDisplay2)
					{
						interactions.repaint();
					}
					break;
					
				default:
					break;
			}
		}
	}
	
	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() instanceof GraphDisplay2)// && interactions.getActiveSpectra().isDrawnAs2D() )
			this.activate();
		else
			this.deactivate();
	}
}
