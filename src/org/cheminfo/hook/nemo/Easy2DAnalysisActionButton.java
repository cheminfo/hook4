package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;

public class Easy2DAnalysisActionButton extends DefaultActionButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8529167664904930946L;

	SmartPeakLabel lastClickedLabel = null; // used for SML replacement

	public Easy2DAnalysisActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	}

	protected void performInstantAction() {
		super.performInstantAction();
		interactions.setCurrentAction(this);
		this.lastClickedLabel = null;

		if (interactions.getActiveDisplay() != null) {
			interactions.setActiveEntity(((SpectraDisplay) interactions.getActiveDisplay()).getFirstSpectra());
		}
	}

	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);

		if (interactions.getCurrentAction() == this) {
			if (interactions.getOverEntity() instanceof Spectra) {
				// Spectra tempSpectra=(Spectra)interactions.getOverEntity();
				SpectraDisplay parentDisplay = (SpectraDisplay) interactions
						.getActiveDisplay();
				Spectra tempSpectra = (Spectra) interactions.getOverEntity();

				Point2D.Double invertedCPoint = new Point2D.Double();
				Point2D.Double invertedRPoint = new Point2D.Double();
				try {
					AffineTransform inverseTransform = tempSpectra
							.getGlobalTransform().createInverse();
					inverseTransform.transform(interactions.getContactPoint(),
							invertedCPoint);
					inverseTransform.transform(interactions.getReleasePoint(),
							invertedRPoint);
				} catch (Exception e) {
					System.out.println("transform not invertable");
				}

				if (tempSpectra != null && tempSpectra.isDrawnAs2D()) {
					switch (ev.getID()) {
					case MouseEvent.MOUSE_MOVED:
						if (tempSpectra.isDrawnAs2D()) {
							parentDisplay
									.setCursorType(SpectraDisplay.CROSSHAIR);
						} else {
							parentDisplay.setCursorType(SpectraDisplay.NONE);
						}
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_EXITED:
						// tempSpectra.setCursorType(Spectra.NONE);
						break;

					case MouseEvent.MOUSE_ENTERED:
						// tempSpectra.setCursorType(Spectra.CROSS);
						break;

					case MouseEvent.MOUSE_PRESSED:
						if (tempSpectra.isDrawnAs2D())
							parentDisplay
									.setCursorType(SpectraDisplay.CROSSHAIR_RECT);
						break;

					case MouseEvent.MOUSE_DRAGGED:
						interactions.repaint();
						break;

					case MouseEvent.MOUSE_RELEASED:
						interactions.takeUndoSnapshot();
						parentDisplay.setCursorType(SpectraDisplay.NONE);
						if (tempSpectra.isDrawnAs2D()) {
							this.startAnalysis();
							interactions.getUserDialog().setText("");
							interactions.repaint();
						}
						break;
					default:
						break;
					}
				}
			} else if (interactions.getOverEntity() instanceof SmartPeakLabel) {
				SmartPeakLabel tempSmartPeakLabel = (SmartPeakLabel) interactions
						.getOverEntity();

				switch (ev.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					if (this.lastClickedLabel != null) {
						Vector linkedEnts = interactions
								.getLinkedEntities(this.lastClickedLabel);
						PeakLabel diagonalRefLabel = null;

						for (int ent = 0; ent < linkedEnts.size(); ent++) {
							if (linkedEnts.elementAt(ent) instanceof PeakLabel
									&& ((PeakLabel) linkedEnts.elementAt(ent))
											.getPeakType() == PeakLabel.DIAGONAL_PEAK) {
								diagonalRefLabel = (PeakLabel) linkedEnts
										.elementAt(ent);
								break;
							}
						}
						if (diagonalRefLabel != null) {
							interactions.createLink(diagonalRefLabel,
									tempSmartPeakLabel,
									InteractiveSurface.INTEGRITY_CHECK);
						}

						Spectra tempSpectra = (Spectra) tempSmartPeakLabel
								.getParentEntity();
						tempSmartPeakLabel.getParentEntity().remove(
								this.lastClickedLabel);
						tempSpectra.checkSizeAndPosition();
						interactions.repaint();
						this.lastClickedLabel = null;
					} else {
						this.lastClickedLabel = tempSmartPeakLabel;
					}
					break;
				}
			}
		}
	}

	private void startAnalysis() {
		Point2D.Double pointA = interactions.getReleasePoint();
		Point2D.Double pointB = interactions.getContactPoint();
		double xA = Math.min(pointA.x,pointB.x);
		double yA = Math.min(pointA.y,pointB.y);
		double xB = Math.max(pointA.x,pointB.x);
		double yB = Math.max(pointA.y,pointB.y);
		//
		SpectraDisplay activeDisplay = (SpectraDisplay)interactions.getActiveDisplay();
		Spectra horSpectrum = activeDisplay.getHorRefSpectrum();
		Spectra verSpectrum = activeDisplay.getVerRefSpectrum();
		int nbSpectra = activeDisplay.getNbSpectra();
		Spectra mainSpectrum = activeDisplay.getFirstSpectra();
		
	}

	
	
	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() != null
				&& interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay()).is2D()) {
			SpectraDisplay activeDisplay = (SpectraDisplay) interactions
					.getActiveDisplay();

			if ((activeDisplay != null)
					&& (activeDisplay.getFirstSpectra() != null)
					&& (activeDisplay.getFirstSpectra().getSpectraData()
							.getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM))
				this.activate();
			else
				this.deactivate();
		} else
			this.deactivate();
	}

}
