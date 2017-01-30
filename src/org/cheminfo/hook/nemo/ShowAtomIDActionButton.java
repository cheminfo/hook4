package org.cheminfo.hook.nemo;

import java.awt.Image;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;

public class ShowAtomIDActionButton extends DefaultActionButton {

	public ShowAtomIDActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 0, ImageButton.CLASSIC);
	}

	protected void performInstantAction() {
		ActMoleculeDisplay molDisplay = this.getMoleculeDisplay();
		if (molDisplay != null) {
			molDisplay.shiftDisplayIDLevel();
			interactions.repaint();
		}
	}

	protected void checkButtonStatus() {

		if (this.getMoleculeDisplay() == null)
			this.deactivate();
		else
			this.activate();

	}

	private ActMoleculeDisplay getMoleculeDisplay() {
		ActMoleculeDisplay molDisplay = null;
		BasicEntity entity = interactions.getEntityByName("molDisplay");
		if (entity instanceof ActMoleculeDisplay)
			molDisplay = (ActMoleculeDisplay) entity;
		if (molDisplay == null
				&& interactions.getActiveDisplay() instanceof ActMoleculeDisplay)
			molDisplay = (ActMoleculeDisplay) interactions.getActiveDisplay();
		if (molDisplay == null && interactions.getActiveDisplay() != null) {
			BasicDisplay activeDisplay = interactions.getActiveDisplay();
			for (int ent = 0; ent < activeDisplay.getEntitiesCount(); ent++) {
				if (activeDisplay.getEntity(ent) instanceof ActMoleculeDisplay) {
					molDisplay = (ActMoleculeDisplay) activeDisplay
							.getEntity(ent);
				}
			}
		}
		return molDisplay;
	}

}