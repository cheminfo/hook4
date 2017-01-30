package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class InsertActionButton extends DefaultActionButton {



	public InsertActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
	}

	protected void handleEvent(MouseEvent ev) {
		if (interactions.getActiveEntity() instanceof Spectra) {
			Spectra tempSpectra = (Spectra) interactions.getActiveEntity();
			SpectraDisplay parentDisplay = (SpectraDisplay) tempSpectra
					.getParentEntity();

			switch (ev.getID()) {
			case MouseEvent.MOUSE_PRESSED:
				interactions.takeUndoSnapshot();
				parentDisplay.setCursorType(SpectraDisplay.RECT);
				break;

			case MouseEvent.MOUSE_DRAGGED:
				interactions.repaint();
				break;

			case MouseEvent.MOUSE_RELEASED:
				// activeDisplay=findParentDisplay(tempSpectra);
				if (Math.abs(interactions.getContactPoint().x
						- interactions.getReleasePoint().x) > 3
						&& Math.abs(interactions.getContactPoint().y
								- interactions.getReleasePoint().y) > 3) {
					double selectionOne, selectionTwo;
					selectionOne = parentDisplay
							.absolutePixelsToUnitsH(interactions
									.getContactPoint().x);
					selectionTwo = parentDisplay
							.absolutePixelsToUnitsH(interactions
									.getReleasePoint().x);
					parentDisplay.addInsertDisplay(tempSpectra, selectionOne,
							selectionTwo);
					parentDisplay.checkSizeAndPosition();
					interactions.repaint();
				}
				parentDisplay.setCursorType(SpectraDisplay.NONE);
				break;

			default:
				break;

			}
		}
		/*
		 * else if (ev.getSource() instanceof SpectraDisplay) { Spectra
		 * tempSpectra=interactions.getActiveSpectra(); SpectraDisplay
		 * tempDisplay=(SpectraDisplay)ev.getSource();
		 * 
		 * if (tempSpectra != null) { switch (ev.getID()) { case
		 * MouseEvent.MOUSE_PRESSED: tempSpectra.setCursorType(Spectra.RECT);
		 * break;
		 * 
		 * case MouseEvent.MOUSE_DRAGGED: tempSpectra.repaint(); break;
		 * 
		 * case MouseEvent.MOUSE_RELEASED: double selectionOne, selectionTwo;
		 * selectionOne=tempSpectra.pixelsToUnits(tempDisplay.getContactPoint().x-tempSpectra.getX());
		 * selectionTwo=tempSpectra.pixelsToUnits(tempDisplay.getReleasePoint().x-tempSpectra.getX());
		 * ((SpectraDisplay)interactions.getActiveDisplay()).addInsertDisplay(tempSpectra,
		 * selectionOne,selectionTwo);
		 * interactions.getActiveDisplay().repaint();
		 * tempSpectra.setCursorType(Spectra.NONE); break;
		 * 
		 * default: break;
		 *  } } }
		 */}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& !((SpectraDisplay) interactions.getActiveDisplay()).is2D())
			this.activate();
		else
			this.deactivate();
	}
}