package org.cheminfo.hook.framework;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Hashtable;

public class BasicDisplay extends BasicEntity
{
	public final static int NONE			=		0;
	public final static int LINES			=		1;
	public final static int RECT			=		2;
	public final static int FILLED_RECT		=		3;
	public final static int CROSSHAIR		=		4;
	public final static int CROSSHAIR_RECT	=		5;
	
	private int currentCursorType;
	
	public BasicDisplay()
	{
		super();
		
		this.setMovementType(BasicEntity.FIXED);
		this.setPrimaryColor(Color.red);
		this.setSecondaryColor(Color.white);
	}
	
	public void init()
	{
		if (this.getParentEntity() != null)
			this.init(this.getParentEntity().getWidth() ,this.getParentEntity().getHeight());
		else
			this.init(this.getInteractiveSurface().getWidth(), this.getInteractiveSurface().getHeight());
	}
	
	public void init(double width, double height)
	{
		this.setSize(width, height);
		this.refreshSensitiveArea();
	}

	public void init(String xmlTag, Hashtable helpers)
	{
	}
	
	public void setCursorType(int cursorType)
	{
		this.currentCursorType=cursorType;
	}
	
	public int getCursorType()
	{
		return this.currentCursorType;
	}
	
	public void paint(Graphics2D g)
	{
		super.paint(g);
	}
}