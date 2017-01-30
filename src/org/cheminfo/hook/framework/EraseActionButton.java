package org.cheminfo.hook.framework;

import java.awt.Image;
import java.awt.event.MouseEvent;

public class EraseActionButton extends DefaultActionButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6322850519266664642L;
	String infoMessage = "Erase";


	public EraseActionButton(Image inImage) {
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
		this.shortcutKey = new char[] {'d','D'};
	}

	
	public EraseActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'d','D'});
		
		/*
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
		this.shortcutKey = new char[] {'d','D'};
		*/
	}
	

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			if (interactions.getOverEntity() instanceof BasicEntity) {
				BasicEntity tempEntity = (BasicEntity) interactions.getOverEntity();

				switch (ev.getID()) {
				case MouseEvent.MOUSE_RELEASED:
					interactions.takeUndoSnapshot();
					if (tempEntity.isSelected()) {
						BasicEntity parentEntity = tempEntity.getParentEntity();
						if (parentEntity != null) {
							for (int ent = parentEntity.getEntitiesCount() - 1; ent >= 0; ent--) {
								BasicEntity basicEntity = parentEntity.getEntity(ent);
								if (basicEntity.isSelected())
									basicEntity.delete(ev.isShiftDown());
							}
						} else {
							tempEntity.delete(ev.isShiftDown());
						}
					} else {
						tempEntity.delete(ev.isShiftDown());
					}
					this.interactions.repaint();
					break;

				default:
					break;
				}
			}
		}
	}

	protected int getGroupNb() {
		return this.groupNb;
	}

}