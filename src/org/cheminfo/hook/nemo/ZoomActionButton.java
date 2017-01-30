package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class ZoomActionButton extends DefaultActionButton {

	private Vector previousActiveEntities = null;
	private double firstSelLimit;

	public ZoomActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'z','Z'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			// we try to find the active SpectraDisplay
			SpectraDisplay spectraDisplay = null;
			if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
				spectraDisplay=(SpectraDisplay) interactions.getActiveDisplay();
			} else if (interactions.getOverEntity() instanceof SpectraDisplay)
				spectraDisplay = (SpectraDisplay) interactions.getOverEntity();
			else if (interactions.getOverEntity() instanceof Spectra)
				spectraDisplay = (SpectraDisplay) interactions.getOverEntity().getParentEntity();
			else if (interactions.getOverEntity() instanceof SpectraObject)
				spectraDisplay = (SpectraDisplay) interactions.getOverEntity().getParentEntity().getParentEntity();
			
			if (spectraDisplay!=null) {
				switch (ev.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
						double currentLeftLimit = spectraDisplay.getCurrentLeftLimit();
						double currentRightLimit = spectraDisplay.getCurrentRightLimit();
						double middle = (currentLeftLimit + currentRightLimit) / 2;
						double halfWidth = currentLeftLimit - middle;
						double newLeftLimit = middle + 2 * halfWidth;
						double newRightLimit = middle - 2 * halfWidth;
						double fulloutLeftLimit = spectraDisplay.getFulloutLeftLimit();
						double fulloutRightLimit = spectraDisplay.getFulloutRightLimit();
						if (!((newRightLimit > fulloutRightLimit && newRightLimit < fulloutLeftLimit) || (newRightLimit < fulloutRightLimit && newRightLimit > fulloutLeftLimit)))
							newRightLimit = fulloutRightLimit;
	
						if (!((newLeftLimit > fulloutRightLimit && newLeftLimit < fulloutLeftLimit) || (newLeftLimit < fulloutRightLimit && newLeftLimit > fulloutLeftLimit)))
							newLeftLimit = fulloutLeftLimit;
						spectraDisplay.setCurrentLimits(newLeftLimit, newRightLimit);
						spectraDisplay.checkSizeAndPosition();
					}
					break;
				case MouseEvent.MOUSE_PRESSED:
					this.previousActiveEntities = (Vector) interactions.getActiveEntities().clone();
					interactions.setActiveEntity(spectraDisplay);
					interactions.takeUndoSnapshot();
					firstSelLimit = spectraDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
					spectraDisplay.setSelectionLimits(firstSelLimit,firstSelLimit);
					spectraDisplay.setCursorType(BasicDisplay.LINES);
					break;
	
				case MouseEvent.MOUSE_RELEASED:

					double leftSelection, rightSelection;
					if (interactions.getReleasePoint().x < interactions
							.getContactPoint().x) {
						leftSelection = spectraDisplay.absolutePixelsToUnitsH(interactions.getReleasePoint().x);
						rightSelection = spectraDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
					} else {
						rightSelection = spectraDisplay.absolutePixelsToUnitsH(interactions.getReleasePoint().x);
						leftSelection = spectraDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x);
					}

					if (Math.abs(interactions.getReleasePoint().x - interactions.getContactPoint().x) > 1) {
						spectraDisplay.setCurrentLimits(leftSelection,rightSelection);
					} else {
						spectraDisplay.setSelectionLimits(9999999, 9999999);
					}
					spectraDisplay.setCursorType(BasicDisplay.NONE);
					spectraDisplay.checkSizeAndPosition();

					interactions.clearActiveEntities();
					for (int ent = 0; ent < this.previousActiveEntities.size(); ent++)
						interactions.addActiveEntity((BasicEntity) this.previousActiveEntities.elementAt(ent));

					interactions.repaint();
					
					break;
	
				case MouseEvent.MOUSE_DRAGGED:
					interactions.repaint();
					break;
	
				default:
					break;
				}
			}
		}
	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			if (((SpectraDisplay) interactions.getActiveDisplay()).is2D())
				this.deactivate();
			else
				this.activate();
		else
			this.deactivate();
	}

}
