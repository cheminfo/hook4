package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Vector;

import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.util.XMLCoDec;

public class ManualBaselineCorrectionActionButton extends DefaultActionButton {


	public ManualBaselineCorrectionActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	}

	protected void performInstantAction() {
		Spectra spectrum = null;
		if (interactions.getActiveEntity() instanceof Spectra)
			spectrum = (Spectra) interactions.getActiveEntity();
		else if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& ((SpectraDisplay) interactions.getActiveDisplay())
						.getFirstSpectra() != null) {
			spectrum = ((SpectraDisplay) interactions.getActiveDisplay())
					.getFirstSpectra();
		}

		if (spectrum != null) {
			interactions.setCurrentAction(this);
			XMLCoDec buttonCodecManual = new XMLCoDec();
			buttonCodecManual.addParameter("action","org.cheminfo.hook.nemo.ManualBaselineCorrectionAction");
			buttonCodecManual.addParameter("image", "validate.gif");

			interactions.getUserDialog().setText("<Text type=\"plain\">Done selecting: </Text>"+ "<Button "
							+ buttonCodecManual.encodeParameters() + "></Button>");
			interactions.repaint();
		}
	}

	protected void handleEvent(MouseEvent ev) {
		super.handleEvent(ev);
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay display = (SpectraDisplay) interactions
					.getActiveDisplay();
			if (!display.is2D()) {
				switch (ev.getID()) {
				case MouseEvent.MOUSE_ENTERED:
					display.setCursorType(SpectraDisplay.LINES);
					interactions.repaint();
					break;
				case MouseEvent.MOUSE_EXITED:
					display.setCursorType(SpectraDisplay.NONE);
					interactions.repaint();
					break;
				case MouseEvent.MOUSE_RELEASED:
					double x0scr = interactions.getContactPoint().x;
					double xnscr = interactions.getReleasePoint().x;
					if (Math.abs(x0scr - xnscr) > 1.0) {
						double x0 = Math.min(display.absolutePixelsToUnitsH(x0scr),display.absolutePixelsToUnitsH(xnscr));
						double xn = Math.max(display.absolutePixelsToUnitsH(x0scr),display.absolutePixelsToUnitsH(xnscr));
						Spectra spectrum = display.getLastSpectra();
						Vector<RangeSelectionLabel> obsoleteLabels = new Vector<RangeSelectionLabel>();
						for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
							if (spectrum.getEntity(ent) instanceof RangeSelectionLabel) {
								RangeSelectionLabel currentLabel = (RangeSelectionLabel)spectrum.getEntity(ent);
								double xmin = currentLabel.getXMin();
								double xmax = currentLabel.getXMax();
								if ((xmin >= x0 && xmin <= xn) || (xmax >= x0 && xmax <= xn)) {
									x0 = Math.min(xmin, x0);
									xn = Math.max(xmax, xn);
									obsoleteLabels.add(currentLabel);
								}
							}
						}
						RangeSelectionLabel label = new RangeSelectionLabel(x0,
								xn);
						spectrum.addEntity(label);
						for (int iLabel = 0; iLabel < obsoleteLabels.size(); iLabel++) {
							spectrum.remove(obsoleteLabels.elementAt(iLabel));
						}
					}
					break;
				}
			}
		}
		interactions.getActiveDisplay().checkSizeAndPosition();
		interactions.getActiveDisplay().refreshSensitiveArea();
		interactions.repaint();
	}

	protected void checkButtonStatus() {
		if (interactions.getActiveDisplay() != null
				&& interactions.getActiveDisplay() instanceof SpectraDisplay) {
			SpectraDisplay activeDisplay = (SpectraDisplay) interactions
					.getActiveDisplay();

			Spectra activeSpectra = null;
			if (interactions.getActiveEntity() instanceof Spectra)
				activeSpectra = (Spectra) interactions.getActiveEntity();
			else if (interactions.getActiveEntity() != null
					&& interactions.getActiveEntity().getParentEntity() != null
					&& interactions.getActiveEntity().getParentEntity() instanceof Spectra)
				activeSpectra = (Spectra) interactions.getActiveEntity()
						.getParentEntity();

			SpectraData spectraData = null;
			if (activeSpectra != null)
				spectraData = activeSpectra.getSpectraData();

			if (spectraData == null && (activeDisplay != null)
					&& (activeDisplay.getFirstSpectra() != null))
				spectraData = activeDisplay.getFirstSpectra().getSpectraData();
			if (spectraData == null) {
				this.deactivate();
			} else {
				switch (spectraData.getDataType()) {
				case SpectraData.TYPE_NMR_SPECTRUM:
					if (spectraData.getSimulationDescriptor().compareTo("") == 0)
						this.activate();
					else
						this.deactivate();
					break;
				case SpectraData.TYPE_2DNMR_SPECTRUM:
					this.activate();
					break;
				default:
					this.deactivate();
				}
			}
		} else {
			this.deactivate();
		}
	}

}
