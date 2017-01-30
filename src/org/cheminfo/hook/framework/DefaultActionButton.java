package org.cheminfo.hook.framework;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class DefaultActionButton extends ImageButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2553887105600333658L;
	int groupNb;
	protected InteractiveSurface interactions = null;
	
	// We may define many shortuts corresponding to the button
	protected char[] shortcutKey={};
	
	String actionClassName = "";

	public DefaultActionButton() {
		super();
	}

	public DefaultActionButton(Image inImage) {
		super(inImage);
	}

	
	public DefaultActionButton(Image inImage, String infoMessage, InteractiveSurface interactions, int newGroupNo, int buttonType) {
		this(inImage, infoMessage, interactions, newGroupNo, buttonType, null);
	}
	
	public DefaultActionButton(Image inImage, String infoMessage, InteractiveSurface interactions, int newGroupNo, int buttonType, char[] shortcutKeys) {
		super(inImage);
		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);
		this.setButtonType(buttonType);
		setGroupNb(newGroupNo);
		if (shortcutKeys!=null) {
			this.shortcutKey=shortcutKeys;
		}
	}
	
	
	
	
	protected void setGroupNb(int newGroupNb) {
		groupNb = newGroupNb;
	}

	protected int getGroupNb() {
		return groupNb;
	}

	protected void setActionClassName(String actionClassName) {
		this.actionClassName = actionClassName;
	}

	protected String getActionClassName() {
		return this.actionClassName;
	}

	protected void performInstantAction() {
		if (interactions != null && interactions.getUserDialog() != null)
			interactions.getUserDialog().setText(this.getInfoMessage());

		if (groupNb != 0) {
			interactions.releaseButtonGroup(groupNb);
			this.setStatus(DefaultActionButton.DOWN);
		}
	}

	/**
	 * When creating an ActionButton this method is overriden to provide the
	 * specific behaviour in response to specific events.
	 * 
	 * @param ev
	 */
	protected void handleEvent(MouseEvent ev) {
	}

	protected void handleWheelEvent(MouseWheelEvent ev) {
	}
	
	/** By default the keyevent is just to say the button has been clicked
	 * A button may still override this method in order to deal with more key events
	 */
	protected void handleKeyEvent(KeyEvent ev) {
		//System.out.println("Got key event: "+this.getClass().getName()+" - "+ev+" "+ev.getKeyChar()+" "+ev.getExtendedKeyCode());
		if (getStatus() != DefaultActionButton.INACTIVE) {
			for (char key : shortcutKey) {
				//System.out.println("sck "+key);
				if (ev.getKeyChar()==key) {
					//System.out.println("da key "+key);
					click();
				}
			}
		}
	}

	protected void checkButtonStatus() {
		if (interactions != null) {
			if (interactions.getCurrentAction() != this)
				this.setStatus(DefaultActionButton.UP);
			else
				this.setStatus(DefaultActionButton.DOWN);
		}
	}

	public void setInteractiveSurface(InteractiveSurface interactions) {
		this.interactions = interactions;
		this.addActionListener(interactions);
	}

	protected void setUserDialogText(String text) {
		this.interactions.getUserDialog().setText(text);
	}

	public void mouseEntered(MouseEvent e) {
		if (this.getStatus() != DefaultActionButton.INACTIVE) {
			super.mouseEntered(e);

			if (interactions != null && interactions.getUserDialog() != null)
				interactions.getUserDialog().setMessageText(this.getInfoMessage());
		}
	}

	public void mouseExited(MouseEvent e) {
		if (this.getStatus() != DefaultActionButton.INACTIVE) {
			super.mouseExited(e);

			if (interactions != null && interactions.getUserDialog() != null)
				interactions.getUserDialog().clearText();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (this.getStatus() != DefaultActionButton.INACTIVE) {
			super.mouseClicked(e);

		}
	}

	protected void deactivate() {
		if (getStatus() != DefaultActionButton.INACTIVE) {
			setStatus(DefaultActionButton.INACTIVE);
			if (this.getParent() != null) {
				getParent().doLayout();
				getParent().repaint();
			}
		}
	}

	protected void activate() {
		if (getStatus() == DefaultActionButton.INACTIVE) {
			setStatus(DefaultActionButton.UP);
			if (getParent()!=null) {
				getParent().doLayout();
				getParent().repaint();
			}
		}
	}

	public void click() {
		if (aListener != null)
			aListener.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED, null));
	}
	
	
	protected String getInfoMessage()
	{
		if (this.shortcutKey.length==0) {
			return infoMessage;
		} else {
			String toReturn="";
			for (char key : shortcutKey) {
				if (toReturn.length()>0) toReturn+=",";
				toReturn+=key;
			}
			return "["+toReturn+"] "+infoMessage;
		}
	}
	
	
}