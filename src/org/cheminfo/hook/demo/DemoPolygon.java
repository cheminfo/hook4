package org.cheminfo.hook.demo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import org.cheminfo.hook.framework.BasicEntity;

public class DemoPolygon extends BasicEntity
{
	private int[] verticesX=null;
	private int[] verticesY=null;
	private int nbSides;
	private double radius, angle;
	
	public DemoPolygon(int nbSides, double radius)
	{
		this.setPrimaryColor(Color.green);
		this.setSecondaryColor(Color.blue);
		
		this.nbSides=nbSides;
		
		this.setParams(radius, 0);
	}

	public void setParams(double radius, double angle)
	{
		this.radius=radius;
		this.angle=angle;
		
		if (this.verticesX == null)
		{
			this.verticesX=new int[this.nbSides];
			this.verticesY=new int[this.nbSides];
		}
		
		AffineTransform temptrans=new AffineTransform();
		Point2D.Double tempPoint=new Point2D.Double(0, -radius);
		Point2D.Double tempPoint2=new Point2D.Double();
		
		for (int v=0; v < nbSides; v++)
		{
			temptrans.setToRotation(angle+v*2*Math.PI/nbSides);
			temptrans.transform(tempPoint, tempPoint2);
			
			this.verticesX[v]=(int)tempPoint2.x;
			this.verticesY[v]=(int)tempPoint2.y;
		}
		
	}
	
	public double getRadius()
	{
		return this.radius;
	}
	
	public double getAngle()
	{
		return this.angle;
	}
	
	public void refreshSensitiveArea()
	{
		if (this.verticesX != null)
			this.setSensitiveArea(new Area(new Polygon(this.verticesX, this.verticesY, this.nbSides)));
	}
	
	public void paint(Graphics2D g)
	{
		if (this.getPrimaryColor() != null)
		{
			g.setColor(this.getPrimaryColor());
			
			g.fill(new Polygon(this.verticesX, this.verticesY, this.nbSides));
		}
		
		if (this.getSecondaryColor() != null)
		{
			g.setColor(this.getSecondaryColor());
			g.draw(new Polygon(this.verticesX, this.verticesY, this.nbSides));
			
			if (this.isSelected())
			{
				for (int v=0; v < this.nbSides; v++)
				{
					g.fillRect(this.verticesX[v]-2, this.verticesY[v]-2, 5,5);
				}
			}
			else if (this.isMouseover() || this.isHighlighted())
			{
				for (int v=0; v < this.nbSides; v++)
				{
					g.fillRect(this.verticesX[v]-1, this.verticesY[v]-1, 3,3);
				}
			}
		}
	}
}