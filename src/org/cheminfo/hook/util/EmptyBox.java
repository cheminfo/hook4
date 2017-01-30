package org.cheminfo.hook.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


/**
* EmptyBox is used to generate the borders of the display that
* give a 3D look to the interface.
*/
public class EmptyBox extends Component {
		
	private int fromLeft, toRight;
	
	/**
	* Creates a EmptyBox object.
	* @param formLeft - left point X coordinate for the depth line.
	* @param toRight - right point X coordinate for the depth line.
	*/
	public EmptyBox(int fromLeft,int toRight) {
		super();
		this.fromLeft=fromLeft;
		this.toRight=toRight;
	}
	
	/**
	* Paint the object. The horizontal line goes from fromLeft to toRight and 
	* is a t the bottom of the box. The vertical line is a the right of the box 
	* running through its entire length.
	* @param g - the Graphic context
	*/
	public void paint(Graphics g) 
	{
		int width=getSize().width;
		int height=getSize().height;
		g.setColor(Color.lightGray);
		g.fillRect(0,0,width,height);
		g.setColor(Color.darkGray);
		g.drawLine(fromLeft,height-1,toRight,height-1);
		g.drawLine(width-1,0,width-1,height-1);
	}

	public void update(Graphics g) {
		paint(g);
	}

}

