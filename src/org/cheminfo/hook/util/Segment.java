package org.cheminfo.hook.util;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class Segment implements QuadTreeElement
{
	public float x1,y1,x2,y2;
	
	public Segment(float x1, float y1, float x2, float y2)
	{
		this.x1=x1;
		this.y1=y1;
		this.x2=x2;
		this.y2=y2;
	}
	
	public double getP1x()
	{
		return this.x1;
	}

	public double getP1y()
	{
		return this.y1;
	}
	
	public double getP2x()
	{
		return this.x2;
	}
	
	public double getP2y()
	{
		return this.y2;
	}
	
	public boolean isContained(float queryX1, float queryX2, float queryY1, float queryY2)
	{
		if ( ( this.x1 > Math.min(queryX1, queryX2) && this.x1 < Math.max(queryX1, queryX2)
				&& this.y1 > Math.min(queryY1, queryY2) && this.y1 < Math.max(queryY1, queryY2) )
			||	
				( this.x2 > Math.min(queryX1, queryX2) && this.x2 < Math.max(queryX1, queryX2)
				&& this.y2 > Math.min(queryY1, queryY2) && this.y2 < Math.max(queryY1, queryY2) )
			)
			return true;
			
		return false;
	}
	
	public void paint(Graphics2D g, float tileLeft, float tileRight, float tileTop, float tileBottom, float tileWidth, float tileHeight)
	{
		g.draw(new Line2D.Float( (tileLeft-this.x1)*tileWidth/(tileLeft-tileRight), (tileTop-this.y1)*tileHeight/(tileTop-tileBottom), (tileLeft-this.x2)*tileWidth/(tileLeft-tileRight), (tileTop-this.y2)*tileHeight/(tileTop-tileBottom)));
	}

}