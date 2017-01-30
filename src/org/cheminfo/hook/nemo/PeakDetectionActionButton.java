package org.cheminfo.hook.nemo;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.DefaultActionButton;
import org.cheminfo.hook.framework.ImageButton;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.math.peakdetection.PeakFinders;
import org.cheminfo.hook.math.peakdetection.PeakFinders2D;
import org.cheminfo.hook.math.util.MathUtils;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.filters.EnergyNormalizationFilter;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.PredictionData;
import org.cheminfo.hook.nemo.nmr.ProprietaryTools;
import org.cheminfo.hook.nemo.signal.NMRSignal1D;
import org.cheminfo.hook.nemo.signal.NMRSignal2D;


public class PeakDetectionActionButton extends DefaultActionButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1281644779206562455L;
	
	public static final boolean isDebug = false;
	// add an extra 0.1 ppm after the baseline rejoin
	public static final double extraRange = 0.02;



	public PeakDetectionActionButton(Image inImage, String infoMessage, InteractiveSurface interactions) {
		super(inImage, infoMessage, interactions, 1, ImageButton.RADIOBUTTON);
	
	}
	
	private void detectPeaks1D(Spectra spectrum) {
		//System.out.println("detectPeaks1D");
		SpectraData spectraData = spectrum.getSpectraData();
		int nbPoints = spectraData.getNbPoints();
		spectraData.setActiveElement(0);

		Nucleus nucleus = spectraData.getNucleus();
		System.out.println("Nuc "+nucleus);
		double nStddev = PeakFinders.getNMRPeakThreshold(nucleus);
		double baselineRejoin = PeakFinders
				.getNMRBaselineRejoinThreshold(nucleus);
		//System.out.println("baselineRejoin "+baselineRejoin+" nStddev "+nStddev);
		/*int maxExtraPoints = (int) Math.round(Math
				.abs(PeakDetectionActionButton.extraRange
						/ spectraData.getInterval()));*/
		
		//AsymmetricPeak[] peakArray = null;
		NMRSignal1D[] peakArray = null;
		int waveletScale = 30;
		int whittakerLambda = 10000;
		spectraData.setActiveElement(spectrum.getSpectraNb());
		peakArray = PeakFinders.simpleThresholdedDetectionBSRRejoin(
				spectraData, spectraData.getSubSpectraDataY(0), 0, nbPoints,
				waveletScale, whittakerLambda, nStddev, baselineRejoin);
		//System.out.println("Here "+ spectraData.getNucleus()+" "+peakArray.length);
		if (peakArray != null) {
			Arrays.sort(peakArray);
			if (nucleus == Nucleus.NUC_1H) {
				// add a few extra points if possible to have a more
				// appealing visual representation of the integrals
				
				double intStart = peakArray[0].getIntegralData().getTo()
						- PeakDetectionActionButton.extraRange;
				//System.out.println(peakArray[0].getShift()+" "+peakArray[1].getShift());
				if (intStart > spectraData.getLastX())
					peakArray[0].getIntegralData().setTo(intStart);
				else
					peakArray[0].getIntegralData().setTo(spectraData.getLastX());
				
				for (int iPeak = 0; iPeak < peakArray.length - 1; iPeak++) {

					double intEnd1 = peakArray[iPeak].getIntegralData().getFrom();
					double intStart2 = peakArray[iPeak + 1].getIntegralData().getTo();
					double delta = intStart2 - intEnd1;
					
					if (delta > 2 * PeakDetectionActionButton.extraRange) {
						peakArray[iPeak].getIntegralData().setFrom(intEnd1 + PeakDetectionActionButton.extraRange);
						peakArray[iPeak + 1].getIntegralData().setTo(intStart2 - PeakDetectionActionButton.extraRange);
					} else{// if (delta > 3) {
						delta /= 2;
						peakArray[iPeak].getIntegralData().setFrom(intEnd1 + delta);
						peakArray[iPeak + 1].getIntegralData().setTo(intStart2 - delta);
					}
				}
				
				double intEnd =  peakArray[peakArray.length - 1].getIntegralData().getFrom()
						+ PeakDetectionActionButton.extraRange;
				if (intEnd < spectraData.getFirstX())
					peakArray[peakArray.length - 1].getIntegralData().setFrom(intEnd);
				else
					peakArray[peakArray.length - 1].getIntegralData().setFrom(spectraData.getFirstX());
			}
			//
			interactions.takeUndoSnapshot();
			if (nucleus == Nucleus.NUC_1H) {
				SmartPickingHelpers.evaluatePeakSelect(interactions, peakArray);
			}
			for (int iPeak = 0; iPeak < peakArray.length; iPeak++) {
				if (peakArray[iPeak] != null) {
					if (SmartPickingHelpers.findOverlappingSmartPeakLabel(
							spectrum.arrayPointToUnits(peakArray[iPeak]
									.getCenter()), spectrum, interactions) == null)
						SmartPickingHelpers.addSmartPeakLabel(interactions,
								spectrum, peakArray[iPeak]);
				}
			}
			spectrum.checkSizeAndPosition();
			spectrum.checkAllIntegrals();
			if (spectrum.getNucleus() == Nucleus.NUC_1H) {
				double maxY = Double.NEGATIVE_INFINITY;
				Integral maxIntegral = null;
				for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
					if (spectrum.getEntity(ent) instanceof Integral) {
						Integral integral = (Integral) spectrum.getEntity(ent);
						if (maxY < integral.getRelArea()) {
							maxY = integral.getRelArea();
							maxIntegral = integral;
						}
					}
				}
				if (maxIntegral != null) {
					double lastY = maxIntegral.getMaxBufferedY();
					double firstY = maxIntegral.getMinBufferedY();
					double yPos = spectrum.getLocation().y;
					double spectrumHeight = spectrum.getHeight();
					double yMax = yPos + spectrumHeight;
					if (lastY > yMax) {
						double newY = yMax * 0.8;
						double integralFactor = Math.abs((newY - firstY)
								/ (lastY - firstY));
						spectrum.setIntegralsMultFactor(integralFactor);
						spectrum.checkAllIntegrals();
					}
				}
			}
			if (nucleus == Nucleus.NUC_1H) {
				// set integral to number of protons
				BasicEntity entity = interactions.getEntityByName("molDisplay");
				if (entity != null && entity instanceof ActMoleculeDisplay) {
					ProprietaryTools.adjustIntegralsToMF(spectrum, (ActMoleculeDisplay)entity);
				}
			}
		}
	}

	private void detectPeaks2D(Spectra spectrum) {
		if (isDebug)
			System.out.println("peak detection 2d");
		SpectraData spectraData = spectrum.getSpectraData();
		
		
		//Lets try our new filter
		
		EnergyNormalizationFilter filter = new EnergyNormalizationFilter();
		spectraData.applyFilter(filter);
		
		/*int nbSpectra = spectraData.getNbSubSpectra();
		int nRows = nbSpectra;
		int nbPoints = spectraData.getNbPoints();
		int nCols = nbPoints;
		double[] inputSpectrum = new double[nRows * nCols];
		for (int iRow = 0; iRow < nRows; iRow++) {
			spectraData.setActiveElement(iRow);
			for (int iCol = 0; iCol < nCols; iCol++) {
				inputSpectrum[iRow * nCols + iCol] = spectraData.getY(iCol);
			}
		}*/
		NMRSignal2D[] signals = PeakFinders2D.findPeaks2DLoG(spectraData);
		Vector<PeakLabel> newLabels = new Vector<PeakLabel>();

		if (signals != null) {
			PeakLabel label;
			for (int iPeak = 0; iPeak < signals.length; iPeak++) {	
				label = new PeakLabel(signals[iPeak].getShiftX(), signals[iPeak].getShiftY());
				//label = new PeakLabel(signals[iPeak].getPeak(0).getX(), signals[iPeak].getPeak(0).getY());
				spectrum.addEntity(label);
				newLabels.add(label);
			}
		}
		// link with the 1D spectra
		if (newLabels.size() > 0)
			this.detectPeaks2DPostProcess(spectrum, newLabels);
	}
	/*private void detectPeaks2D(Spectra spectrum) {
		if (isDebug)
			System.out.println("peak detection 2d");
		SpectraData spectraData = spectrum.getSpectraData();
		int nbSpectra = spectraData.getNbSubSpectra();
		int nRows = nbSpectra;
		int nbPoints = spectraData.getNbPoints();
		int nCols = nbPoints;
		double[] inputSpectrum = new double[nRows * nCols];
		for (int iRow = 0; iRow < nRows; iRow++) {
			spectraData.setActiveElement(iRow);
			for (int iCol = 0; iCol < nCols; iCol++) {
				inputSpectrum[iRow * nCols + iCol] = spectraData.getY(iCol);
			}
		}

		Peak2D[] peaks = null;
		String experiment = spectraData.getParamString(".PULSE SEQUENCE",
				"undef");
		double nStdDev = PeakFinders2D.getLoGnStdDevNMR(experiment);
		peaks = PeakFinders2D.findPeaks2DLoG(inputSpectrum, nRows, nCols,
				nStdDev);
		Vector<PeakLabel> newLabels = new Vector<PeakLabel>();

		if (peaks != null) {
			// if (isDebug)
			// System.out.println("found " + peaks.length + " peaks");
			// double x0 = spectraData.getFirstX();
			// double dx = spectraData.getInterval();
			double firstY = Double.parseDouble(spectraData.getParamString(
					"firstY", ""));
			double lastY = Double.parseDouble(spectraData.getParamString(
					"lastY", ""));
			double dy = (lastY - firstY) / (nbSpectra - 1);
			// if (isDebug)
			// System.out.println("found " + peaks.length + " peaks");
			PeakLabel label;
			double xValue, yValue;
			for (int iPeak = 0; iPeak < peaks.length; iPeak++) {
				xValue = spectrum.arrayPointToUnits(peaks[iPeak].getX());
				// yValue = spectrum.arrayPointToUnitsV(peaks[iPeak].getY());
				yValue = firstY + dy * (peaks[iPeak].getY());
				if (isDebug)
					System.out
							.println("xValue=[" + xValue + "],yValue=["
									+ yValue + "],zValue=["
									+ peaks[iPeak].getZ() + "]");
				label = new PeakLabel(xValue, yValue);
				spectrum.addEntity(label);
				newLabels.add(label);
			}
		}
		// link with the 1D spectra
		if (newLabels.size() > 0)
			this.detectPeaks2DPostProcess(spectrum, newLabels);
	}*/

	public void performInstantAction() {
		if (interactions.getActiveDisplay() instanceof SpectraDisplay
				&& !(interactions.getActiveEntity() instanceof Spectra)) {
			SpectraDisplay display = (SpectraDisplay) interactions.getActiveDisplay();
			if (display.is2D())
				interactions.setActiveEntity(display.getFirstSpectra());
		}

		if (interactions.getActiveEntity() instanceof Spectra) {
			Spectra spectrum = (Spectra) interactions.getActiveEntity();
			SpectraData spectraData = spectrum.getSpectraData();
			switch (spectraData.getDataType()) {
			case SpectraData.TYPE_NMR_SPECTRUM:
				detectPeaks1D(spectrum);
				break;
			case SpectraData.TYPE_2DNMR_SPECTRUM:
				detectPeaks2D(spectrum);
				break;
			}
			
			/*System.out.println("Refresh");
			spectrum.clearContourLines();
			spectrum.drawAs2D();
			spectrum.resetNeedsRepaint();*/
			
			spectrum.refreshSensitiveArea();
			spectrum.checkSizeAndPosition();
			interactions.setCurrentAction(interactions.getDefaultAction());
			interactions.checkButtonsStatus();
			interactions.repaint();
		}
	}

	protected void handleEvent(MouseEvent ev) {

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

			if (activeSpectra != null) {
				if (activeSpectra.spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM
						|| activeSpectra.spectraData.getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM)
					this.activate();
				else
					this.deactivate();
			} else {
				if (activeDisplay != null) {
					if (activeDisplay.getFirstSpectra() != null
							&& activeDisplay.getFirstSpectra().getSpectraData() != null) {
						SpectraData spectraData = (SpectraData) activeDisplay
								.getFirstSpectra().getSpectraData();
						if (spectraData.getDataType() == SpectraData.TYPE_NMR_SPECTRUM
								|| spectraData.getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM) {
							this.activate();
							return;
						}
					}
				}
				this.deactivate();
			}
		} else
			this.deactivate();
	}

	private void detectPeaks2DPostProcess(Spectra mainSpectrum,
			Vector<PeakLabel> newLabels) {
		if (!(mainSpectrum.getParentEntity() instanceof SpectraDisplay))
			return;
		SpectraDisplay display = (SpectraDisplay) mainSpectrum
				.getParentEntity();
		// horizontal
		Spectra horSpectrum = display.getHorRefSpectrum();
		if (horSpectrum == null) {
			horSpectrum = display.createTraceSpectrum(false);
			PredictionData predictionData = horSpectrum.getPredictionData();
			double deltaX = 2 * horSpectrum.getSpectraData().getInterval();
			for (int iLabel = 0; iLabel < newLabels.size(); iLabel++) {
				PeakLabel currentLabel = newLabels.get(iLabel);
				double xPos = currentLabel.getXPos();
				SmartPeakLabel spl = SmartPickingHelpers.putSmartPeakLabels(
						Math.max(xPos + deltaX, xPos - deltaX), Math.min(xPos
								+ deltaX, xPos - deltaX), horSpectrum,
						interactions, false);
				if (spl == null)
					spl = predictionData.addFakeSmartPeak(horSpectrum, xPos);
				interactions.createLink(currentLabel, spl);
			}
		} else {
			boolean hasSmartPeakLabels = false;
			for (int ent = 0; ent < horSpectrum.getEntitiesCount(); ent++) {
				if (horSpectrum.getEntity(ent) instanceof SmartPeakLabel) {
					hasSmartPeakLabels = true;
					break;
				}
			}
			if (!hasSmartPeakLabels) {
				this.detectPeaks1D(horSpectrum);
			}
			for (int iLabel = 0; iLabel < newLabels.size(); iLabel++) {
				PeakLabel currentLabel = newLabels.get(iLabel);
				double xPos = currentLabel.getXPos();
				SmartPeakLabel spl = SmartPickingHelpers
						.findOverlappingSmartPeakLabel(xPos, horSpectrum,
								interactions);
				if (spl != null)
					interactions.createLink(currentLabel, spl);
			}

		}
		// vertical
		Spectra verSpectrum = display.getVerRefSpectrum();
		if (mainSpectrum.isHomonuclear()) {
			if (horSpectrum == null)
				return;
			verSpectrum = horSpectrum;
		}
		if (verSpectrum == null) {
			verSpectrum = display.createTraceSpectrum(true);
			PredictionData predictionData = verSpectrum.getPredictionData();
			double deltaY = 2 * verSpectrum.getSpectraData().getInterval();
			for (int iLabel = 0; iLabel < newLabels.size(); iLabel++) {
				PeakLabel currentLabel = newLabels.get(iLabel);
				double yPos = currentLabel.getYPos();
				SmartPeakLabel spl = SmartPickingHelpers.putSmartPeakLabels(
						Math.max(yPos + deltaY, yPos - deltaY), Math.min(yPos
								+ deltaY, yPos - deltaY), verSpectrum,
						interactions, false);
				if (spl == null)
					spl = predictionData.addFakeSmartPeak(verSpectrum, yPos);
				interactions.createLink(currentLabel, spl);
			}
		} else {
			boolean hasSmartPeakLabels = false;
			for (int ent = 0; ent < verSpectrum.getEntitiesCount(); ent++) {
				if (verSpectrum.getEntity(ent) instanceof SmartPeakLabel) {
					hasSmartPeakLabels = true;
					break;
				}
			}
			if (!hasSmartPeakLabels) {
				this.detectPeaks1D(verSpectrum);
			}
			for (int iLabel = 0; iLabel < newLabels.size(); iLabel++) {
				PeakLabel currentLabel = newLabels.get(iLabel);
				double yPos = currentLabel.getYPos();
				SmartPeakLabel spl = SmartPickingHelpers
						.findOverlappingSmartPeakLabel(yPos, horSpectrum,
								interactions);
				if (spl != null)
					interactions.createLink(currentLabel, spl);
			}

		}

	}
}
