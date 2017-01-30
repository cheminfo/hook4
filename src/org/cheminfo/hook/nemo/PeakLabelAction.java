package org.cheminfo.hook.nemo;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;

/**
 * This class describe the action to be executed when the user click on a peak
 * and validate the user dialog. The call is defined in the PeakLabel class and
 * the method getClickedMessage().
 */

public class PeakLabelAction extends GeneralAction {
	public static void performAction(InteractiveSurface interactions) {
		if (interactions != null) {
			interactions.takeUndoSnapshot();

			if (interactions.getActiveEntities().size() == 1 && interactions.getActiveEntity() instanceof PeakLabel)
			{
				PeakLabel tempLabel = (PeakLabel) interactions.getActiveEntity();
				Spectra tempSpectra = (Spectra) tempLabel.getParentEntity();
				SpectraDisplay activeDisplay = (SpectraDisplay) interactions.getActiveDisplay();

				switch (tempSpectra.getSpectraData().getDataType()) {
				case SpectraData.TYPE_2DNMR_SPECTRUM:
					PeakLabelAction.shiftSpectrum(interactions, activeDisplay,tempLabel);
					break;
				default:
					try {
						double newXPos = (double) new DecimalFormat().parse(interactions.getUserDialog().getParameter("peakXPos")).doubleValue();

						double difference = tempLabel.getXPos() - newXPos;

						tempSpectra.getSpectraData().setFirstX(
								tempSpectra.getSpectraData().getFirstX() - difference);
						tempSpectra.getSpectraData().setLastX(
								tempSpectra.getSpectraData().getLastX() - difference);

						PeakPickingHelpers.shiftSpectraObjects(tempSpectra,
								difference);

						tempLabel.setComment(interactions.getUserDialog().getParameter("comment"));

						PeakLabel currentLabel;
						for (int ent = 0; ent < tempSpectra.getEntitiesCount(); ent++) {
							if (tempSpectra.getEntity(ent) instanceof PeakLabel) {
								currentLabel = (PeakLabel) tempSpectra.getEntity(ent);
								currentLabel.setXPos(currentLabel.getXPos() - difference);
							}
						}
						// tempLabel.setXPos(newXPos);
						activeDisplay.setCurrentLimits(activeDisplay.getCurrentLeftLimit() - difference, activeDisplay.getCurrentRightLimit() - difference);
						activeDisplay.checkFulloutLimits();
						activeDisplay.checkSizeAndPosition();
						interactions.repaint();
						interactions.getUserDialog().setText("");
					} catch (ParseException ex) {
						interactions.getUserDialog().setText(
								"Number format exception");
					}
				
					break;
				}
			}
		}
	}

	public static void shiftSpectrum(InteractiveSurface interactions,
			SpectraDisplay spectraDisplay, PeakLabel tempLabel) {
		try {
			double newXPos = (double) new DecimalFormat().parse(
					interactions.getUserDialog().getParameter("peakXPos"))
					.doubleValue();
			double differenceX = tempLabel.getXPos() - newXPos;
			double newYPos = (double) new DecimalFormat().parse(
					interactions.getUserDialog().getParameter("peakYPos"))
					.doubleValue();
			double differenceY = tempLabel.getYPos() - newYPos;

			//
			Spectra mainSpectrum = spectraDisplay.getFirstSpectra();
			SpectraData mainSpectraData = mainSpectrum.getSpectraData();
			mainSpectraData
					.setFirstX(mainSpectraData.getFirstX() - differenceX);
			mainSpectraData.setLastX(mainSpectraData.getLastX() - differenceY);
			//
			mainSpectraData.setParamDouble("lastY", mainSpectraData
					.getParamDouble("lastY", 0.0)
					- differenceY);
			mainSpectraData.setParamDouble("firstY", mainSpectraData
					.getParamDouble("firstY", 0.0)
					- differenceY);

//			if (spectraDisplay.getHorRefSpectrum() != null)
//				shift1DSpectrum(spectraDisplay.getHorRefSpectrum(), differenceX);
//			if (!mainSpectrum.isHomonuclear())
//				if (spectraDisplay.getVerRefSpectrum() != null)
//					shift1DSpectrum(spectraDisplay.getVerRefSpectrum(),
//							differenceY);

//			mainSpectrum.clearContourLines();
//			mainSpectrum.generateContourLines(Spectra.DEFAULT_NB_CONTOURS);
			mainSpectrum.shiftContourLines(differenceX, differenceY);
			
			for (int ent = 0; ent < mainSpectrum.getEntitiesCount(); ent++) {
				if (mainSpectrum.getEntity(ent) instanceof PeakLabel) {
					PeakLabel label = (PeakLabel) mainSpectrum.getEntity(ent);
					label.setXPos(label.getXPos() - differenceX);
					label.setYPos(label.getYPos() - differenceY);
				}
			}

			tempLabel.setComment(interactions.getUserDialog().getParameter(
					"comment"));

			spectraDisplay.setCurrentLimits(spectraDisplay
					.getCurrentLeftLimit()
					- differenceX, spectraDisplay.getCurrentRightLimit()
					- differenceX, spectraDisplay.getCurrentTopLimit()
					- differenceY, spectraDisplay.getCurrentBottomLimit()
					- differenceY);
			spectraDisplay.checkFulloutLimits();
			spectraDisplay.checkSizeAndPosition();
			interactions.repaint();
			interactions.getUserDialog().setText("");
		} catch (ParseException ex) {
			interactions.getUserDialog().setText("Number format exception");
		}
	}

	public static void shift1DSpectrum(Spectra spectrum, double difference) {
		spectrum.getSpectraData().setFirstX(
				spectrum.getSpectraData().getFirstX() - difference);
		spectrum.getSpectraData().setLastX(
				spectrum.getSpectraData().getLastX() - difference);

		PeakPickingHelpers.shiftSpectraObjects(spectrum, difference);
		PeakLabel currentLabel;
		for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
			if (spectrum.getEntity(ent) instanceof PeakLabel) {
				currentLabel = (PeakLabel) spectrum.getEntity(ent);
				currentLabel.setXPos(currentLabel.getXPos() - difference);
			}
		}

	}
}