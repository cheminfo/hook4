package org.cheminfo.hook.framework;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.nemo.SmartPeakLabel;

public class LinkActionButton extends DefaultActionButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5273338690752441321L;
	BasicEntity memEntity = null;
	int buttonType = ImageButton.RADIOBUTTON;
	private LinkCreationHandler linkCreationHandler = null;
	private static final boolean isDebug = false;

	public LinkActionButton() {
		super();
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public LinkActionButton(Image inImage) {
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public LinkActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'r','R'});
	}
	
	/*
	public LinkActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}
	*/

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
		memEntity = null;
	}

	protected void handleEvent(MouseEvent ev) {
		if (interactions.getCurrentAction() == this) {
			switch (ev.getID()) {
			case MouseEvent.MOUSE_CLICKED:
				if (ev.isShiftDown()){
					interactions.removeAllLinks(interactions.getOverEntity(), this.linkCreationHandler);
				}
				break;

			case MouseEvent.MOUSE_PRESSED:
				// if (ev.isShiftDown() == false) {
				this.memEntity = interactions.getOverEntity();
				interactions.setRubberbandMode(true);
				// }
				break;

			case MouseEvent.MOUSE_RELEASED:
				// if (ev.isShiftDown() == false) {
				interactions.takeUndoSnapshot();
				/*
				 * if (memEntity == null) memEntity=(BasicEntity)ev.getSource();
				 * else { interactions.createLink((BasicEntity)ev.getSource(),
				 * memEntity); memEntity=null; }
				 */
				if (interactions.getOverEntity() != memEntity) {
					if (ev.isShiftDown()) {
						memEntity.setHighlighted(false);
						if (this.linkCreationHandler == null) {
						interactions.removeLink(interactions.getOverEntity(),
								memEntity);
						} else {
							this.linkCreationHandler.handleLinkDeletion(interactions, interactions.getOverEntity(),
									memEntity);
						}
					} else {
						if (this.linkCreationHandler != null) {
							this.linkCreationHandler.handleLinkCreation(
									interactions, interactions.getOverEntity(),
									memEntity);
						} else {
							interactions.createLink(interactions
									.getOverEntity(), memEntity);

						}
					}
				}
				interactions.setRubberbandMode(false);
				interactions.repaint();
				// }
				break;

			case MouseEvent.MOUSE_DRAGGED:
				interactions.repaint();
				break;

			default:
				break;
			}
		}
	}

	public void setLinkCreationHandler(LinkCreationHandler linkCreationHandler) {
		this.linkCreationHandler = linkCreationHandler;
	}

	@Override
	protected void checkButtonStatus() {
		super.checkButtonStatus();

	}

}
