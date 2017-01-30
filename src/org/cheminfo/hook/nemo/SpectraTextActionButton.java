package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class SpectraTextActionButton extends DefaultActionButton {

	public SpectraTextActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'t','T'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	protected void handleEvent(MouseEvent ev) {
		// super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			Spectra tempSpectra = null;
			if (interactions.getActiveEntity() instanceof Spectra) {
				tempSpectra = (Spectra) interactions.getActiveEntity();
			}

			if (tempSpectra != null) {
				if (interactions.getActiveEntities().contains(tempSpectra)) {
					switch (ev.getID()) {
					case MouseEvent.MOUSE_CLICKED:
						SpectraTextEntity newEntity = new SpectraTextEntity(20,
								3);
						tempSpectra.addEntity(newEntity);
						newEntity.setLocation(interactions.getContactPoint().x
								- newEntity.getWidth() / 20, interactions
								.getContactPoint().y
								- newEntity.getHeight() / 2);

						// EntityResizer eResizer = new
						// EntityResizer(EntityResizer.SE_RESIZER);
						// newEntity.addEntity(eResizer);
						// eResizer.checkSizeAndPosition();
						// newField.requestFocus();

						interactions.clearActiveEntities();
						interactions.setActiveEntity(newEntity);
						interactions.repaint();
						break;

					default:
						break;
					}
				} else {
					switch (ev.getID()) {
					case MouseEvent.MOUSE_CLICKED:
						interactions.clearActiveEntities();
						interactions.setActiveEntity(tempSpectra);
						// tempSpectra.requestFocus();
						interactions.repaint();
						break;

					default:
						break;
					}
				}
			}
			/*
			 * else if (ev.getSource() instanceof TextFieldEntity) {
			 * TextFieldEntity tempText=(TextFieldEntity)ev.getSource();
			 * 
			 * switch (ev.getID()) { case MouseEvent.MOUSE_CLICKED:
			 * interactions.clearActiveEntities();
			 * interactions.setActiveEntity(tempText);
			 * tempText.setWriteReady(true); tempText.requestFocus();
			 * tempText.repaint(); break;
			 * 
			 * default: break; } }
			 *//*
				 * else { BasicEntity tempEntity=(BasicEntity)ev.getSource();
				 * 
				 * switch (ev.getID()) { case MouseEvent.MOUSE_CLICKED: //
				 * tempEntity.requestFocus(); // tempEntity.repaint(); break;
				 * 
				 * default: break; } }
				 */
		}
	}

}