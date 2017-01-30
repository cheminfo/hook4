package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;

public class ChangeHydrogenContractionState extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -107899938515616922L;

	String infoMessage = "Change hydrogen contraction state";
	int buttonType = ImageButton.CHECKBUTTON;

	public ChangeHydrogenContractionState() {
		super();
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public ChangeHydrogenContractionState(Image inImage) {
		super(inImage);
		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	public ChangeHydrogenContractionState(Image inImage, String infoMessage,
			InteractiveSurface interactions) {
		super(inImage);

		this.setInfoMessage(infoMessage);
		this.setInteractiveSurface(interactions);
		interactions.addButton(this);

		this.setButtonType(ImageButton.RADIOBUTTON);
		this.setGroupNb(1);
	}

	@Override
	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof ActMoleculeDisplay)
			this.activate();
		else
			this.deactivate();
	}

	@Override
	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	@Override
	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);
		if (interactions.getOverEntity() instanceof ActAtomEntity
				&& ev.getID() == MouseEvent.MOUSE_RELEASED) {
			ActAtomEntity atomEntity = (ActAtomEntity) interactions.getOverEntity();
			if (atomEntity.getParentEntity() instanceof ActMoleculeDisplay) {
				ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) atomEntity.getParentEntity();
				int atomID = atomEntity.getAtomID();
				molDisplay.switchExpansionState(atomID);
			}
		}
	}

}
