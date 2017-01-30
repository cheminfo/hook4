package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class PeakPickingActionButton extends DefaultActionButton {

	PeakLabel tempPeakLabel=null;

	public PeakPickingActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON, new char[] {'p','P'});
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
		if (interactions.getActiveDisplay() != null	&& !(interactions.getActiveEntity() instanceof Spectra))
		{
			if (((SpectraDisplay) interactions.getActiveDisplay()).getFirstSpectra() != null)
			{
				interactions.clearActiveEntities();
				interactions.addActiveEntity(((SpectraDisplay) interactions.getActiveDisplay()).getFirstSpectra());
				interactions.repaint();
			}
		} else if (interactions.getActiveDisplay() == null)
		{
			if (interactions.getRootDisplay() != null && interactions.getRootDisplay() instanceof SpectraDisplay)
			{
				SpectraDisplay rootDisplay = (SpectraDisplay) interactions.getRootDisplay();
				if (rootDisplay.getFirstSpectra() != null)
					interactions.setActiveEntity(rootDisplay.getFirstSpectra());
			}
		}
	}

	protected void handleEvent(MouseEvent ev) {
		if (interactions.getCurrentAction() == this) {
			Spectra tempSpectra = null;
			// if (interactions.getOverEntity() instanceof Spectra)
			// tempSpectra=(Spectra)interactions.getOverEntity();
			/* else */
			if (interactions.getActiveEntity() instanceof Spectra)
				tempSpectra = (Spectra) interactions.getActiveEntity();

			if (interactions.getOverEntity() instanceof PeakLabel
					&& ev.getID() == MouseEvent.MOUSE_CLICKED) {
				PeakLabel tempLabel = (PeakLabel) interactions.getOverEntity();

				DecimalFormat newFormat = new DecimalFormat();
				newFormat.applyPattern("#0.00");
				String value, positionHz;

				interactions.clearActiveEntities();
				interactions.setActiveEntity(tempLabel);
				interactions.getUserDialog().setText("");
				setUserDialogText(tempLabel.getClickedMessage());
			} else if (tempSpectra != null) {
				SpectraDisplay parentDisplay = (SpectraDisplay) tempSpectra.getParentEntity();

				if (tempSpectra.isDrawnAs2D() == false) {
					switch (ev.getID()) {
					case MouseEvent.MOUSE_CLICKED:
						if (interactions.getOverEntity() instanceof Spectra) {
							// Clicking on a non selected Spectra induces a
							// selection
							if (this.interactions != null) {
								interactions.setActiveEntity(interactions.getOverEntity());
								parentDisplay.checkSizeAndPosition();
								interactions.repaint();
							}
						}
						/*
						 * if (interactions.getActiveEntities().size() != 1 ||
						 * !interactions.getActiveEntities().contains(tempSpectra)) {
						 * if (this.interactions != null) {
						 * interactions.setActiveEntity(tempSpectra);
						 * interactions.repaint(); } }
						 */break;

					case MouseEvent.MOUSE_PRESSED:
						if (this.tempPeakLabel != null)
							tempSpectra.remove(this.tempPeakLabel);

						parentDisplay.setCursorType(SpectraDisplay.RECT);
						break;

					case MouseEvent.MOUSE_RELEASED:
						interactions.takeUndoSnapshot();
						if ((Math.abs(interactions.getContactPoint().x
								- interactions.getReleasePoint().x) > 3)
								|| (Math.abs(interactions.getContactPoint().y
										- interactions.getReleasePoint().y) > 3)) {
							PeakPickingHelpers.findPeaksInRect(interactions
									.getContactPoint(), interactions
									.getReleasePoint(), tempSpectra);
						} else {
							if (!(interactions.getOverEntity() instanceof Spectra
									|| interactions.getOverEntity() instanceof PeakLabel)
									|| interactions.getOverEntity() == tempSpectra
								)
							{
								Point2D.Double invertedPoint = new Point2D.Double();
								try {
									AffineTransform inverseTransform = tempSpectra.getGlobalTransform().createInverse();
									inverseTransform.transform(interactions.getContactPoint(), invertedPoint);
								} catch (Exception e) {
									System.out.println("transform not invertable");
								}

								double searchCenter = tempSpectra.pixelsToUnits(invertedPoint.x);
								double range = parentDisplay.unitsPerPixelH() * 5;
								int tempArrayPoint = PeakPickingHelpers	.findPeakInRange(searchCenter, range, tempSpectra, false);
								double peakAt = tempSpectra.arrayPointToUnits(tempArrayPoint);
								tempSpectra.addEntity(new PeakLabel(peakAt));
							}
						}
						parentDisplay.setCursorType(SpectraDisplay.NONE);
						parentDisplay.checkSizeAndPosition();
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_DRAGGED:
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_MOVED:
						int tempArrayPoint;

						// if (interactions.getActiveSpectra() == tempSpectra)
						{

							if (this.tempPeakLabel != null && tempSpectra.containsEntity(tempPeakLabel)	)
								tempSpectra.remove(tempPeakLabel);

							// // this part brings the absolute coordinates (in
							// interactions' space) to the spectra space through
							// the inverse of the global transform
							Point2D invertedPoint = new Point2D.Double();
							try {
								AffineTransform inverseTransform = tempSpectra.getGlobalTransform().createInverse();
								inverseTransform.transform(interactions.getContactPoint(), invertedPoint);
							} catch (Exception e) {
								System.out.println("transform not invertable");
							}

							
							// The following lines are not correct for PEAK TABLE spectra ...
							double searchCenter = tempSpectra.pixelsToUnits(invertedPoint.getX());
							double range = parentDisplay.unitsPerPixelH() * 5;
							tempArrayPoint = PeakPickingHelpers.findPeakInRange(searchCenter, range, tempSpectra, false);
							// double
							// peakAt=tempSpectra.arrayPointToUnits(tempArrayPoint);

							this.tempPeakLabel = new PeakLabel(tempSpectra.arrayPointToUnits(tempArrayPoint));
							if (this.tempPeakLabel != null)
								tempSpectra.addEntity(this.tempPeakLabel);

							// tempPeakLabel.setContracted(true);

							interactions.getUserDialog().setMessageText(
									this.tempPeakLabel.getOverMessage());

							// multi dimensional Experiments (e.g. LC-MS,...)

							/*
							 * Vector
							 * linkedEntities=interactions.getLinkedDestEntities((SpectraDisplay)tempSpectra.getParent()); //
							 * SpectraDisplay linkedDisplay=null;
							 * 
							 * for (int ent=0; ent < linkedEntities.size();
							 * ent++) { if (linkedEntities.elementAt(ent)
							 * instanceof SpectraDisplay) {
							 * linkedDisplay=(SpectraDisplay)linkedEntities.elementAt(ent);
							 * break; } }
							 * 
							 * if (linkedDisplay != null) { Vector
							 * selectedEntities=interactions.getActiveEntities();
							 * PeakLabel selectedPeak=null;
							 * 
							 * for (int ent=0; ent < selectedEntities.size();
							 * ent++) { if (selectedEntities.elementAt(ent)
							 * instanceof PeakLabel &&
							 * ((PeakLabel)selectedEntities.elementAt(ent)).isSelected() ) {
							 * selectedPeak=(PeakLabel)selectedEntities.elementAt(ent);
							 * break; } }
							 * 
							 * double test; if (selectedPeak == null)
							 * test=interactions.getActiveSpectra().arrayPointToUnits(tempArrayPoint);
							 * else test=selectedPeak.getXPos();
							 * 
							 * GlobalCoord tempCoord = new
							 * GlobalCoord(tempSpectra.getSpectraData().getAbscissaSymbol()+"="+test);
							 * linkedDisplay.getFirstSpectra().setSpectraNb(tempSpectra.getSpectraData().getSubSpectraByGlobalCoord(tempCoord)); } //
							 * end of Multi Dimensional Experiments
							 */
							// parentDisplay.checkSizeAndPosition();
							tempPeakLabel.checkSizeAndPosition();
							interactions.repaint();
						}
						break;

					case MouseEvent.MOUSE_EXITED:
						if (tempSpectra != null
								&& tempSpectra
										.containsEntity(this.tempPeakLabel))
							tempSpectra.remove(this.tempPeakLabel);

						interactions.getUserDialog().setText("");
						interactions.repaint();
						// if
						// (tempSpectra.getSpectraData().getDataClass()==SpectraData.DATACLASS_XY)
						/*
						 * { if (tempSpectra.getComponentCount()>0) { if
						 * (tempSpectra.getComponent(tempSpectra.getComponentCount()-1) ==
						 * tempSpectra.getTempPeakLabel())
						 * tempSpectra.remove(tempSpectra.getTempPeakLabel()); } }
						 */break;

					default:
						break;
					}
				}
				/*
				 * else // 2D case {
				 * 
				 * switch (ev.getID()) { case MouseEvent.MOUSE_CLICKED: //
				 * Clicking on a non selected Spectra induces a selection if
				 * (interactions.getActiveEntities().size() != 1 ||
				 * !interactions.getActiveEntities().contains(tempSpectra)) {
				 * interactions.clearActiveEntities(); if (this.interactions !=
				 * null) { // if (interactions.getActiveDisplay() != null)
				 * interactions.getActiveDisplay().deselectTree();
				 * interactions.setActiveEntity(tempSpectra); }
				 * interactions.repaint(); } else { Spectra
				 * spectra=(Spectra)interactions.getActiveEntities().elementAt(0);
				 * SpectraDisplay
				 * spectraDisplay=(SpectraDisplay)spectra.getParentEntity();
				 * 
				 * PredictionPeakLabel pPeakLabel=new
				 * PredictionPeakLabel(spectraDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x)-1,
				 * spectraDisplay.absolutePixelsToUnitsH(interactions.getContactPoint().x)+1,
				 * spectraDisplay.absolutePixelsToUnitsV((int)interactions.getContactPoint().y)-1,
				 * spectraDisplay.absolutePixelsToUnitsV((int)interactions.getContactPoint().y)+1);
				 * spectra.addEntity(pPeakLabel);
				 * pPeakLabel.checkSizeAndPosition(); interactions.repaint(); }
				 * break;
				 * 
				 * 
				 * default: break; } }
				 */} else if (ev.getSource() instanceof PeakLabel) {
				PeakLabel tempLabel = (PeakLabel) ev.getSource();

				DecimalFormat newFormat = new DecimalFormat();
				newFormat.applyPattern("#0.00");
				String value, positionHz;

				switch (ev.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					interactions.clearActiveEntities();
					interactions.setActiveEntity(tempLabel);
					interactions.getUserDialog().setText("");
					setUserDialogText(tempLabel.getClickedMessage());
					// tempLabel.displayText();
					break;

				default:
					break;
				}
			}

		}
	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			if (((SpectraDisplay) interactions.getActiveDisplay()).is2D()) {
				if (interactions.getActiveEntity() instanceof Spectra) {
					if (((Spectra)interactions.getActiveEntity()).spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM) {
						this.activate();
					} else {
						this.deactivate();
					}
				} else {
					this.deactivate();
				}
			} else {
				this.activate();
			}
		else
			this.deactivate();
	}

	private Point steeperAscent(SpectraData tempSpectraData, int arrayPoint,
			int subSpectra) {
		boolean peakFound = false;
		Point currentPoint = new Point();
		Point nextPoint = new Point();

		currentPoint.setLocation(arrayPoint, subSpectra);
		nextPoint.setLocation(arrayPoint, subSpectra);
		tempSpectraData.setActiveElement(currentPoint.y);
		double currentMaximum = tempSpectraData.getY(currentPoint.x);

		do {
			for (int yCor = -1; yCor <= 1; yCor++) {
				tempSpectraData.setActiveElement(currentPoint.y + yCor);
				for (int xCor = -1; xCor <= 1; xCor++) {
					if (tempSpectraData.getY(currentPoint.x + xCor) > currentMaximum) {
						currentMaximum = tempSpectraData.getY(currentPoint.x
								+ xCor);
						nextPoint.setLocation(currentPoint.x + xCor,
								currentPoint.y + yCor);
					}
				}
			}
			if (currentPoint.x != nextPoint.x || currentPoint.y != nextPoint.y)
				currentPoint.setLocation(nextPoint.x, nextPoint.y);
			else {
				peakFound = true;
			}
		} while (!peakFound);

		// System.out.println("max: " + currentMaximum);
		return currentPoint;
	}
}
