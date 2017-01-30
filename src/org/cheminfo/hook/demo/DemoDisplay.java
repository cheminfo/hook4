package org.cheminfo.hook.demo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.cheminfo.hook.framework.BasicDisplay;


public class DemoDisplay extends BasicDisplay
{
/*	public DemoDisplay()
	{
		super();
		
		this.setPrimaryColor(Color.white);
		this.setSecondaryColor(Color.red);
	}
*/	
	public void init()
	{
		super.init();
		
		this.setPrimaryColor(Color.white);
		this.setSecondaryColor(Color.red);
	}
	
	public void paint(Graphics2D g)
	{
		if (this.getPrimaryColor() != null)
		{
			g.setColor(this.getPrimaryColor());
			g.fill(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
		}
		
		if (this.isSelected() && this.getSecondaryColor() != null)
		{
			g.setColor(this.getSecondaryColor());
			g.draw(new Rectangle2D.Double(0, 0, this.getWidth()-1, this.getHeight()-1));
		}
		
		super.paint(g);
		
	}
	
	
}