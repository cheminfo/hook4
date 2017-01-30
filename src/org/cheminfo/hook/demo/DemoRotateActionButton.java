package org.cheminfo.hook.demo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class DemoRotateActionButton extends DefaultActionButton
{
	private double originalAngle;
	
	public DemoRotateActionButton(Image inImage, String infoMessage, InteractiveSurface interactions)
	{
		super(inImage);
		
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);
		
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public void performInstantAction()
	{
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}
	
	public void handleEvent(MouseEvent ev)
	{	
		if (interactions.getCurrentAction() == this)
		{

			if (interactions.getOverEntity() instanceof DemoPolygon)
			{
				switch (ev.getID())
				{
					case MouseEvent.MOUSE_PRESSED:
						interactions.setActiveEntity(interactions.getOverEntity());
						break;
					
					default:
						break;
				}
			}
			if (interactions.getActiveEntity() instanceof DemoPolygon)
			{
				DemoPolygon tempPoly=(DemoPolygon)interactions.getActiveEntity();

				switch (ev.getID())
				{
					case MouseEvent.MOUSE_PRESSED:
						this.originalAngle=tempPoly.getAngle();
						break;
					
					case MouseEvent.MOUSE_DRAGGED:
						Point2D.Double cP=interactions.getContactPoint();
						Point2D.Double rP=interactions.getReleasePoint();
						Point2D.Double o=tempPoly.getLocation();
						
						// dot product to angle
						double angle=Math.acos(((cP.x-o.x)*(rP.x-o.x)+(cP.y-o.y)*(rP.y-o.y))/(o.distance(cP)*o.distance(rP)));
						// cross product to sign (we only need the z coordinate
						double signDeterminant=(cP.x-o.x)*(rP.y-o.y)-(cP.y-o.y)*(rP.x-o.x);
						if (signDeterminant < 0) angle=-angle;
						
						tempPoly.setParams(tempPoly.getRadius(), originalAngle+angle);
						interactions.repaint();
						break;
						
					default:
						break;
				}
			}

		}
	}
	
}