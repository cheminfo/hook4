package org.cheminfo.hook.framework;


import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
* This class is ment to replace the standard java button and allows images to be
* put in the button instead of text. It also has a string (infoMessage) that can
* be used to store a 'mouse over message'.
*/
public class ImageButton extends Component implements MouseListener
{
	protected static int UP=0;
	protected static int DOWN=1;
	protected static int INACTIVE=2;
	
	public static int CLASSIC=0;
	public static int CHECKBUTTON=1;
	public static int RADIOBUTTON=2;
	
	final static int	MOUSE_RELEASED=2001;
	final static int	MOUSE_PRESSED=2002;
	final static int	MOUSE_CLICKED=2003;
	final static int	MOUSE_ENTERED=2004;
	final static int	MOUSE_EXITED=2005;

	boolean	mouseOver;									// used to keep track of presence of the mouse on the button
	boolean mousePressed;								// used to keep track of button down or up
	
	ActionListener	aListener=null;
	
	protected String infoMessage="";									// Text for mouse-over
	protected Image theImage;								// Image of the button
	private static Image buttonUpImage, buttonDownImage;	// Images of the button background (pressed / released)

	private int	buttonStatus;									// Is the button UP, DOWN or INACTIVE?
	private int buttonType;										// Type of Button (CLASSIC, CHECKBUTTON, RADIOBUTTON)	

	private int buttonSizeX=24;
	private int	buttonSizeY=24;
	
	
	
	/**
	* Creates an empty ImageButton object.
	*/
	protected ImageButton ()
	{
		super();
		this.setSize(buttonSizeX,buttonSizeY);
		
		buttonStatus=UP;
		buttonType=CLASSIC;
		
		this.addMouseListener(this);
	}

	/**
	* Creates an ImageButton object.
	* @param inImage - the image for this button.
	*/
	public ImageButton (Image inImage)
	{
		super();
		this.setSize(buttonSizeX,buttonSizeY);
		
		theImage=inImage;
		buttonStatus=UP;
		buttonType=CLASSIC;
		
		this.addMouseListener(this);
	}
	
	/**
	* Creates an ImageButton object.
	* @param inImage - the image for this button.
	* @param text - a String containing the info message.
	*/
	protected ImageButton (Image inImage, String text)
	{
		this(inImage);
		
		this.infoMessage=text;
		
	}	
	
	/**
	* Applies an ActionListener to the Entity.
	* @param listener - the ActionListener object to which all messages will be dispached
	*/
	protected void addActionListener(ActionListener listener)
	{
//		if ( !Arrays.asList(AWTEventMulticaster.getListeners(listener, ActionListener.class) ).contains(aListener) )
		aListener = AWTEventMulticaster.remove(aListener, listener);
		aListener = AWTEventMulticaster.add(aListener, listener);
	}
	
	/**
	* Removes the ActionListener to the Entity.
	* @param listener - the ActionListener object to which all messages were dispached
	*/
	protected void removeActionListener(ActionListener listener)
	{
		aListener = AWTEventMulticaster.remove(aListener, listener);
	}		
	
	/**
	* Set the status of the botton to the specified value.
	* @param newStatus - an integer representing the desired status for this button (UP, DOWN, INACTIVE)
	*/
	protected void setStatus(int newStatus)
	{
		buttonStatus=newStatus;
		repaint();
	}
	
	/**
	* Switches the status of the button. From UP to DOWN and from DOWN to UP.
	*/
	protected void switchStatus()
	{
		if (buttonStatus==UP) buttonStatus=DOWN;
		else if (buttonStatus==DOWN) buttonStatus=UP;
		
		repaint();
	}
	
	/**
	* Returns the status of this button.
	* @return an integer representing the status of the button (UP, DOWN INACTIVE)
	*/
	protected int getStatus()
	{
		return this.buttonStatus;
	}
	
	/**
	* Set the type of this button.
	* @param type - integer representing the desired type (CLASSIC, CHECKBUTTON; RADIOBUTTON)
	*/
	protected void setButtonType(int type)
	{
		buttonType=type;
	}
	
	protected int getButtonType()
	{
		return this.buttonType;
	}
	
	public void setButtonImage(Image newImage)
	{
		this.theImage=newImage;
	}
	
	/**
	* Set the background image for all button in the UP position.
	* @param inImage - the Image to be set as background.
	*/
	public static void setButtonUpImage(Image inImage)
	{
		buttonUpImage=inImage;
	}
	
	/**
	* Set the background image for all button in the DOWN position.
	* @param inImage - the Image to be set as background.
	*/
	public static void setButtonDownImage(Image inImage)
	{
		buttonDownImage=inImage;
	}
	
	/**
	* Set the info message.
	* @param infoString - String to be set as info message.
	*/
	public void setInfoMessage(String infoString)
	{
		infoMessage=infoString;
	}
	
	/**
	* Returns the info message for this button.
	* @return a string containing the info message.
	*/
	protected String getInfoMessage()
	{
		return infoMessage;
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(buttonSizeX,buttonSizeY);
	}
	
//--------------------------------------------------------------------
//	MouseListener functions
//--------------------------------------------------------------------

	public void mousePressed(MouseEvent e)
	{
		if (buttonStatus!=INACTIVE)
		{
			mousePressed=true;
			if(buttonType==CLASSIC)	setStatus(DOWN);
			if(buttonType==RADIOBUTTON)
			{
				if (aListener != null)	aListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
			}
			if(buttonType==CHECKBUTTON)	switchStatus();
		}
	}
	
	public void mouseReleased(MouseEvent e)
	{
		if (buttonStatus!=INACTIVE)
		{
			mousePressed=false;
			if (mouseOver)
			{
				if(buttonType==CLASSIC)	
				{
					if (aListener != null)	aListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
	
					setStatus(UP);
				}
				if(buttonType==CHECKBUTTON)
				{
					if (aListener != null)	aListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
	
				}
			}
		}
	}
	
	
	public void mouseClicked(MouseEvent e)
	{
	}
	
	
	public void mouseEntered(MouseEvent e)
	{
		if (buttonStatus!=INACTIVE)
		{
			mouseOver=true;
	
			if (aListener != null)	aListener.actionPerformed(new ActionEvent(this, ImageButton.MOUSE_ENTERED, null));
	
			if((buttonType==CLASSIC)&&(mousePressed))	setStatus(DOWN);
			if((buttonType==CHECKBUTTON)&&(mousePressed))	switchStatus();
		}
	}

	public void mouseExited(MouseEvent e)
	{
		if (buttonStatus!=INACTIVE)
		{
			mouseOver=false;
	
			if (aListener != null)	aListener.actionPerformed(new ActionEvent(this, ImageButton.MOUSE_EXITED, null));
			
			if((buttonType==CLASSIC)&&(mousePressed))	setStatus(UP);
			if((buttonType==CHECKBUTTON)&&(mousePressed))	switchStatus();
		}
	}	
//-----------------------------------------------------------------------
//	paint()
//-----------------------------------------------------------------------	
	/**
	* Paints this button
	*/
	public void paint(Graphics g)
	{
		Image tempImage=createImage(24,24);;
		Graphics tempGraphics=tempImage.getGraphics();
		
		if (buttonStatus!=INACTIVE)
		{
			if (buttonStatus==UP) tempGraphics.drawImage(buttonUpImage,0,0,this);
			else if (buttonStatus==DOWN) tempGraphics.drawImage(buttonDownImage,0,0,this);

			if (this.theImage != null)
				tempGraphics.drawImage(theImage,1,1,this);
		
			g.drawImage(tempImage,0,0,this);
		}
	}

	public Image getImage() {
		return theImage;
	}

	public void setImage(Image image) {
		this.theImage = image;
	}
	
}
		