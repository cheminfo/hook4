package org.cheminfo.hook.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class AddPolygonActionButton extends DefaultActionButton
{
	private int nbSides;
	private DemoPolygon tempPoly;
	
	public AddPolygonActionButton(int nbSides, String infoMessage, InteractiveSurface interactions)
	{
		super();
		
		this.nbSides=nbSides;
		this.infoMessage=infoMessage;
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
			if (interactions.getActiveDisplay() instanceof DemoDisplay)
			{
				switch (ev.getID())
				{
				case MouseEvent.MOUSE_PRESSED:
					DemoDisplay tempDisplay=(DemoDisplay)interactions.getActiveDisplay();
					tempPoly=new DemoPolygon(this.nbSides, 20);
					tempPoly.setLocation(interactions.getContactPoint());
					
					tempDisplay.addEntity(tempPoly);
					tempPoly.refreshSensitiveArea();
					interactions.repaint();
					break;
					
				case MouseEvent.MOUSE_DRAGGED:
					double dist=Math.sqrt(Math.pow(interactions.getReleasePoint().x-interactions.getContactPoint().x, 2)+Math.pow(interactions.getReleasePoint().y-interactions.getContactPoint().y,2));
					if (dist > 20)
					{
						tempPoly.setParams(dist, 0);
						tempPoly.refreshSensitiveArea();
						interactions.repaint();
					}
					
				default:
					break;
						
				}
			}
		}
	}

	
	public void paint(Graphics g)
	{
		if(theImage==null) {
			theImage=this.createImage(22,22);
			Graphics tempGraph=theImage.getGraphics();
			
			int verticesX[], verticesY[];
			
			verticesX=new int[this.nbSides];
			verticesY=new int[this.nbSides];
			
			
			AffineTransform temptrans=new AffineTransform();
			Point2D.Double tempPoint=new Point2D.Double(0, -8);
			Point2D.Double tempPoint2=new Point2D.Double();
			
			for (int v=0; v < nbSides; v++)
			{
				temptrans.setToRotation(v*2*Math.PI/nbSides);
				temptrans.transform(tempPoint, tempPoint2);
				
				verticesX[v]=(int)tempPoint2.x+11;
				verticesY[v]=(int)tempPoint2.y+11;
			}
			
			tempGraph.setColor(Color.lightGray);
			tempGraph.fillRect(0,0, 22, 22);
			
			tempGraph.setColor(Color.darkGray);
			tempGraph.fillPolygon(verticesX, verticesY, this.nbSides);
		}
		
		super.paint(g);
	}

	protected void checkButtonStatus()
	{
		if (interactions.getActiveDisplay() instanceof DemoDisplay)
			this.activate();
		else this.deactivate();
	}

}