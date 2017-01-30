package org.cheminfo.hook.nemo;

import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class ModifySmartPeakLabelActionButton extends DefaultActionButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6417656419425206044L;
	PeakLabel tempPeakLabel = null;
	SmartPeakLabel selectedSmartPeakLabel = null;

	DefaultActionButton previousAction = null;

	public ModifySmartPeakLabelActionButton(InteractiveSurface interactions, SmartPeakLabel smartPeakLabel) {
		this.interactions = interactions;
		this.previousAction = this.interactions.getCurrentAction();
		this.interactions.setCurrentAction(this);
		this.selectedSmartPeakLabel = smartPeakLabel;
		this.tempPeakLabel = new PeakLabel(smartPeakLabel.getNmrSignal1D().getShift());
		this.tempPeakLabel.setContracted(true);
		((Spectra) this.selectedSmartPeakLabel.getParentEntity()).addEntity(this.tempPeakLabel);
		this.tempPeakLabel.checkSizeAndPosition();
		this.tempPeakLabel.refreshSensitiveArea();

	}

	@Override
	protected void handleEvent(MouseEvent ev) {
		if (this.tempPeakLabel == null)
			return;
		super.handleEvent(ev);
		Spectra parentSpectrum = (Spectra) this.selectedSmartPeakLabel
				.getParentEntity();
		switch (ev.getID()) {
		case MouseEvent.MOUSE_ENTERED:
			this.tempPeakLabel.setXPos(parentSpectrum
					.pixelsToUnits(this.interactions.getContactPoint().x));
			parentSpectrum.addEntity(this.tempPeakLabel);
			this.tempPeakLabel.checkSizeAndPosition();
			this.tempPeakLabel.refreshSensitiveArea();
			break;
		case MouseEvent.MOUSE_EXITED:
			parentSpectrum.remove(this.tempPeakLabel);
			this.tempPeakLabel.checkSizeAndPosition();
			this.tempPeakLabel.refreshSensitiveArea();
			break;
		case MouseEvent.MOUSE_MOVED: {
			double contactPointUnits = parentSpectrum
					.pixelsToUnits(this.interactions.getContactPoint().x);
			if (contactPointUnits >= Math.min(this.selectedSmartPeakLabel.getNmrSignal1D()
					.getStartX(), this.selectedSmartPeakLabel.getNmrSignal1D().getEndX())
					&& contactPointUnits <= Math.max(
							this.selectedSmartPeakLabel.getNmrSignal1D().getStartX(),
							this.selectedSmartPeakLabel.getNmrSignal1D().getEndX())) {
				this.tempPeakLabel.setXPos(contactPointUnits);
			} 
		}
			this.tempPeakLabel.checkSizeAndPosition();
			this.tempPeakLabel.refreshSensitiveArea();
			break;
		case MouseEvent.MOUSE_CLICKED:
			if (this.selectedSmartPeakLabel != null) {
				double contactPointUnits = parentSpectrum
						.pixelsToUnits(this.interactions.getContactPoint().x);
				if (contactPointUnits >= Math.min(this.selectedSmartPeakLabel.getNmrSignal1D()
						.getStartX(), this.selectedSmartPeakLabel.getNmrSignal1D().getEndX())
						&& contactPointUnits <= Math.max(
								this.selectedSmartPeakLabel.getNmrSignal1D().getStartX(),
								this.selectedSmartPeakLabel.getNmrSignal1D().getEndX())) {

					if (ev.isControlDown())
						this.selectedSmartPeakLabel.removeSubPeak();
					else
						this.selectedSmartPeakLabel.addSubPeak();

					this.interactions.getUserDialog().setText(
							this.selectedSmartPeakLabel.getEditDialog());
				} else {
					this.cleanUp();
					interactions
							.setCurrentAction(interactions
									.getButtonByClassName("org.cheminfo.hook.nemo.SmartPickingActionButton"));
					this.interactions.getUserDialog().setText("");
				}
			}
			break;
		}
		interactions.repaint();
	}

	public DefaultActionButton getPreviousAction() {
		return this.previousAction;
	}

	public void cleanUp() {
		if (this.tempPeakLabel != null
				&& this.tempPeakLabel.getParentEntity() != null) {
			this.tempPeakLabel.getParentEntity().remove(this.tempPeakLabel);
			this.tempPeakLabel = null;
		}
	}
}
