package org.cheminfo.hook.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
* An EntityResizer is an active Entity that is used to change the size of its parent
* Entity using the mouse. It has the apparence of a small square that shoud be visible only 
* when the parent is selected.
*/
public class EntityResizer extends BasicEntity
{
//	Resizer Types:
	public final static int	NE_RESIZER=0;
	public final static int	NW_RESIZER=1;
	public final static int	SE_RESIZER=2;
	public final static int	SW_RESIZER=3;
		
	Dimension	originalDimensions = new Dimension();
	
	final static int RESIZER_SIZE=10;	// changing size does not work ...

	private int	resizerType;
	private String overMessage="Resize";
	private static boolean DEBUG = false;
	/**
	* Creates an EntityResizer object of the type defined by resizerType.
	* @param resizerType - an integer representing the type of resizer (NE_RESIZER, NW_RESIZER, SE_RESIZER, SW_RESIZER).
	*/
	public EntityResizer(int resizerType)
	{
		super();
		
		if(DEBUG) System.out.println("Creating entity resizer");
		
		this.resizerType=resizerType;
		this.setSize(RESIZER_SIZE,RESIZER_SIZE);
		this.setPrimaryColor(Color.blue);
		
		this.setMovementType(BasicEntity.FIXED);
		
		this.refreshSensitiveArea();

	}


	public void checkSizeAndPosition()
	{
		BasicEntity parentEntity=this.getParentEntity();
		
		switch (this.resizerType)
		{
			case EntityResizer.SE_RESIZER:
				this.setLocation(parentEntity.getWidth()-RESIZER_SIZE, parentEntity.getHeight()-RESIZER_SIZE);
				break;
			
			default:
				break;
		}
	}
	
	public void setMouseover(boolean flag)
	{
		super.setMouseover(flag);
		this.getParentEntity().setMouseover(flag);
	}
	
	public String getOverMessage()
	{
		return overMessage;
	}
	
	public void setOverMessage(String overMessage) {
		this.overMessage=overMessage;
	}
	
	/**
	* Returns an integer representing the type of resizer.
	* @return an integer (NE_RESIZER, NW_RESIZER, SE_RESIZER, SW_RESIZER).
	*/
	protected int getResizerType()
	{
		return this.resizerType;
	}
	
	
	public void paint(Graphics2D g)
	{
		if (((BasicEntity)this.getParentEntity()).isSelected() || this.isSelected())
		{
			g.setColor(this.getPrimaryColor());
			g.setColor(Color.BLACK);
			g.fillRect(0,0,(int)this.getWidth(), (int)this.getHeight());
		}
	}
}