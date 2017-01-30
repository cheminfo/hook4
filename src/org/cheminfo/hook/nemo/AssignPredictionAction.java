package org.cheminfo.hook.nemo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.GeneralAction;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.nmr.ExperimentType;
import org.cheminfo.hook.optimizer.HungarianAlgorithm;

public class AssignPredictionAction extends GeneralAction {
	public static final String[] methods = { "Conservative", "Best Match" };

	private static InteractiveSurface interactions = null;
	private static final boolean isDebug = false;

	public static void performAction(InteractiveSurface interactions) {
		if (isDebug)
			System.out.println("assigning");
		AssignPredictionAction.interactions = interactions;
		SpectraDisplay spectraDisplay = (SpectraDisplay) interactions.getActiveDisplay();
		Spectra mainSpectrum = spectraDisplay.getFirstSpectra();
		boolean is2D = false;
		if (mainSpectrum == null) {
			if (isDebug)
				System.out.println("no mainSpectrum");
			return;
		} else {
			if (mainSpectrum.isDrawnAs2D())
				is2D = true;
		}
		String method = interactions.getUserDialog().getParameter("method");
		if (isDebug)
			System.out.println("requested method=" + method);
		if (method.equals(methods[1])) {
			if (isDebug)
				System.out.println("assigning using " + methods[1]);
			if (is2D)
				assign2DBestMatch(mainSpectrum);
			else
				assign1DBestMatch();
		} else if (method.equals(methods[0])) {
			if (isDebug)
				System.out.println("assigning using " + methods[0]);
			if (is2D)
				assign2DConservative(mainSpectrum);
			else
				assign1DConservative();
		} else {
			interactions.getUserDialog().setText("");
		}
		interactions.repaint();
	}

	private static void assign1DConservative() {
		SpectraDisplay spectraDisplay;
		if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
			spectraDisplay = (SpectraDisplay) interactions.getActiveDisplay();
		} else {
			interactions.getUserDialog().setText("No SpectraDisplay found");
			return;
		}
		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);
		if (molDisplay == null) {
			interactions.getUserDialog().setText("No molDisplay found");
			return;
		}
		int nbSpectra = spectraDisplay.getNbSpectra();
		if (nbSpectra != 2) {
			interactions.getUserDialog().setText("No enough spectra found");
			return;
		}
		Vector<Spectra> spectra = new Vector<Spectra>(2);
		for (int ent = 0; ent < spectraDisplay.getEntitiesCount(); ent++)
			if (spectraDisplay.getEntity(ent) instanceof Spectra)
				spectra.add((Spectra) spectraDisplay.getEntity(ent));
		Spectra measuredSpectrum = spectra.get(0);
		Spectra predictedSpectrum = spectra.get(1);
		Vector<PeakData> measuredPeaks = getMeasuredPeakData(measuredSpectrum);
		Vector<PeakData> predictedPeaks = getPredictedPeakData(predictedSpectrum);
		int nMeasured = measuredPeaks.size();
		int nPredicted = measuredPeaks.size();
		boolean[][] overlap = new boolean[nMeasured][nPredicted];
		int[] measuredUnique = new int[nMeasured];
		Arrays.fill(measuredUnique, 0);
		int[] predictedUnique = new int[nPredicted];
		Arrays.fill(predictedUnique, 0);
		for (int iMeasured = 0; iMeasured < nMeasured; iMeasured++) {
			for (int iPredicted = 0; iPredicted < nPredicted; iPredicted++) {
				boolean match = splOverlap(measuredPeaks.get(iMeasured)
						.getPeakLabel(), predictedPeaks.get(iPredicted)
						.getPeakLabel());
				overlap[iMeasured][iPredicted] = match;
				if (match) {
					measuredUnique[iMeasured]++;
					predictedUnique[iPredicted]++;
				}
			}
		}
		//
		for (int iMeasured = 0; iMeasured < nMeasured; iMeasured++) {
			for (int iPredicted = 0; iPredicted < nPredicted; iPredicted++) {
				if (measuredUnique[iMeasured] == 1
						&& predictedUnique[iPredicted] == 1
						&& overlap[iMeasured][iPredicted]) {
					SmartPeakLabel predicted = predictedPeaks.get(iPredicted)
							.getPeakLabel();
					SmartPeakLabel measured = measuredPeaks.get(iMeasured)
							.getPeakLabel();
					Vector linkedEntities = interactions
							.getLinkedDestEntities(predicted);
					for (int iLink = 0; iLink < linkedEntities.size(); iLink++) {
						if (linkedEntities.get(iLink) instanceof ActAtomEntity)
							interactions.createLink(measured,
									(ActAtomEntity) linkedEntities.get(iLink));
					}
				}
			}
		}
		interactions.getUserDialog().setText("");
	}

	private static boolean splOverlap(SmartPeakLabel spl1, SmartPeakLabel spl2) {
		double a1 = Math.min(spl1.getNmrSignal1D().getStartX(), spl1.getNmrSignal1D().getEndX());
		double b1 = Math.max(spl1.getNmrSignal1D().getStartX(), spl1.getNmrSignal1D().getEndX());
		double a2 = Math.min(spl2.getNmrSignal1D().getStartX(), spl2.getNmrSignal1D().getEndX());
		double b2 = Math.max(spl2.getNmrSignal1D().getStartX(), spl2.getNmrSignal1D().getEndX());
		if ((a2 >= a1 && a2 <= b1) || (b2 >= a1 && b2 <= b1)) {
			return true;
		} else {
			if ((a1 >= a2 && a1 <= b2) || (b1 >= a2 && b1 <= b2)) {
				return true;
			} else {
				return false;
			}
		}

	}

	private static void assign1DBestMatch() {
		SpectraDisplay spectraDisplay;
		if (interactions.getActiveDisplay() instanceof SpectraDisplay)
			spectraDisplay = (SpectraDisplay) interactions.getActiveDisplay();
		else
			return;
		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);
		if (molDisplay == null)
			return;
		int nbSpectra = spectraDisplay.getNbSpectra();
		if (nbSpectra != 2)
			return;
		Vector<Spectra> spectra = new Vector<Spectra>(2);
		for (int ent = 0; ent < spectraDisplay.getEntitiesCount(); ent++)
			if (spectraDisplay.getEntity(ent) instanceof Spectra)
				spectra.add((Spectra) spectraDisplay.getEntity(ent));
		Spectra measuredSpectrum = spectra.get(0);
		Spectra predictedSpectrum = spectra.get(1);
		Vector<PeakData> measuredPeaks = getMeasuredPeakData(measuredSpectrum);
		Vector<PeakData> predictedPeaks = getPredictedPeakData(predictedSpectrum);
		int nMeasured = measuredPeaks.size();
		int nPredicted = predictedPeaks.size();

		// this.normalize
		// calculate weight matrix
		double[][] distances;
		if (nPredicted > nMeasured) {
			distances = new double[nMeasured][nPredicted];
			for (int iPredicted = 0; iPredicted < nPredicted; iPredicted++) {
				PeakData predicted = predictedPeaks.get(iPredicted);
				for (int iMeasured = 0; iMeasured < nMeasured; iMeasured++) {
					PeakData measured = measuredPeaks.get(iMeasured);
					double difference = measured.getChemicalShift()
							- predicted.getChemicalShift();
					distances[iMeasured][iPredicted] = difference * difference;
				}
			}
		} else {
			distances = new double[nPredicted][nMeasured];
			for (int iPredicted = 0; iPredicted < nPredicted; iPredicted++) {
				PeakData predicted = predictedPeaks.get(iPredicted);
				for (int iMeasured = 0; iMeasured < nMeasured; iMeasured++) {
					PeakData measured = measuredPeaks.get(iMeasured);
					double difference = measured.getChemicalShift()
							- predicted.getChemicalShift();
					distances[iPredicted][iMeasured] = difference * difference;
				}
			}
		}
		//
		TreeMap<Integer, ActAtomEntity> atomEntities = molDisplay
				.getAtomEntityMap();

		int[][] assignment = HungarianAlgorithm.hgAlgorithm(distances, "min");
		int nAssignemnts = assignment.length;
		for (int iAssignment = 0; iAssignment < nAssignemnts; iAssignment++) {
			int iPredicted, iMeasured;
			if (nPredicted > nMeasured) {
				iPredicted = assignment[iAssignment][1];
				iMeasured = assignment[iAssignment][0];
			} else {
				iPredicted = assignment[iAssignment][0];
				iMeasured = assignment[iAssignment][1];
			}
			PeakData predicted = predictedPeaks.get(iPredicted);
			PeakData measured = measuredPeaks.get(iMeasured);
			Vector linkedEntities = interactions.getLinkedEntities(predicted
					.getPeakLabel());
			for (int iLink = 0; iLink < linkedEntities.size(); iLink++) {
				if (linkedEntities.get(iLink) instanceof ActAtomEntity) {
					interactions.createLink(measured.getPeakLabel(),
							(BasicEntity) linkedEntities.get(iLink));
				}
			}
		}
		interactions.getUserDialog().setText("");
	}

	private static void assign2DBestMatch(Spectra mainSpectrum) {
		Vector<PeakLabel> peakLabels = new Vector<PeakLabel>();
		Vector<PredictionLabel> predictionLabels = new Vector<PredictionLabel>();
		int nEntities = mainSpectrum.getEntitiesCount();
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			BasicEntity currentEntity = mainSpectrum.getEntity(iEntity);
			if (currentEntity instanceof PeakLabel) {
				peakLabels.add((PeakLabel) currentEntity);
			} else if (currentEntity instanceof PredictionLabel) {
				predictionLabels.add((PredictionLabel) currentEntity);
			}
		}
		int nPredictionLabels = predictionLabels.size();
		int nPeakLabels = peakLabels.size();
		if (nPeakLabels == 0) {
			interactions.getUserDialog().setText(
					"No peak labels have been found!");
			return;
		}
		if (nPredictionLabels == 0) {
			interactions.getUserDialog().setText(
					"No prediction labels have been found!");
			return;
		}
		// calculate weight matrix
		double[][] distances = null;
		if (nPredictionLabels > nPeakLabels) {
			distances = new double[nPeakLabels][nPredictionLabels];
			for (int iPredictionLabel = 0; iPredictionLabel < nPredictionLabels; iPredictionLabel++) {
				PredictionLabel predictionLabel = predictionLabels
						.get(iPredictionLabel);
				for (int iPeakLabel = 0; iPeakLabel < nPeakLabels; iPeakLabel++) {
					PeakLabel peakLabel = peakLabels.get(iPeakLabel);
					double deltaX = peakLabel.getXPos()
							- predictionLabel.getXPos();
					double deltaY = peakLabel.getYPos()
							- predictionLabel.getYPos();
					distances[iPeakLabel][iPredictionLabel] = deltaX * deltaX
							+ deltaY * deltaY;
				}
			}
		} else {
			distances = new double[nPredictionLabels][nPeakLabels];
			for (int iPredictionLabel = 0; iPredictionLabel < nPredictionLabels; iPredictionLabel++) {
				PredictionLabel predictionLabel = predictionLabels
						.get(iPredictionLabel);
				for (int iPeakLabel = 0; iPeakLabel < nPeakLabels; iPeakLabel++) {
					PeakLabel peakLabel = peakLabels.get(iPeakLabel);
					double deltaX = peakLabel.getXPos()
							- predictionLabel.getXPos();
					double deltaY = peakLabel.getYPos()
							- predictionLabel.getYPos();
					distances[iPredictionLabel][iPeakLabel] = deltaX * deltaX
							+ deltaY * deltaY;
				}
			}
		}
		//
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(distances, "min");

		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);
		if (molDisplay == null)
			return;
		TreeMap<Integer, ActAtomEntity> atomEntities = molDisplay
				.getAtomEntityMap();

		int nAssignemnts = assignment.length;
		for (int iAssignment = 0; iAssignment < nAssignemnts; iAssignment++) {
			int iPredicted, iMeasured;
			if (nPredictionLabels > nPeakLabels) {
				iPredicted = assignment[iAssignment][1];
				iMeasured = assignment[iAssignment][0];
			} else {
				iPredicted = assignment[iAssignment][0];
				iMeasured = assignment[iAssignment][1];
			}
			PredictionLabel predicted = predictionLabels.get(iPredicted);
			PeakLabel measured = peakLabels.get(iMeasured);
			AssignPredictionAction.createLinksWithTraces(interactions,
					predicted, measured, atomEntities);
		}
		interactions.getUserDialog().setText("");
	}

	@SuppressWarnings("unchecked")
	private static void assign2DConservative(Spectra mainSpectrum) {
		Vector<PeakLabel> peakLabels = new Vector<PeakLabel>();
		Vector<PredictionLabel> predictionLabels = new Vector<PredictionLabel>();
		int nEntities = mainSpectrum.getEntitiesCount();
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			BasicEntity currentEntity = mainSpectrum.getEntity(iEntity);
			if (currentEntity instanceof PeakLabel) {
				peakLabels.add((PeakLabel) currentEntity);
			} else if (currentEntity instanceof PredictionLabel) {
				predictionLabels.add((PredictionLabel) currentEntity);
			}
		}

		int nPredictionLabels = predictionLabels.size();
		int nPeakLabels = peakLabels.size();
		if (nPeakLabels == 0) {
			interactions.getUserDialog().setText(
					"No peak labels have been found!");
			return;
		}
		if (nPredictionLabels == 0) {
			interactions.getUserDialog().setText(
					"No prediction labels have been found!");
			return;
		}
		// 
		//
		// get all non overlapping prediction
		for (int iUpper = predictionLabels.size() - 1; iUpper >= 0; iUpper--) {
			PredictionLabel upperLabel = predictionLabels.get(iUpper);
			if (upperLabel != null) {
				double xMinU = upperLabel.getXMin();
				double xMaxU = upperLabel.getXMax();
				double yMinU = upperLabel.getYMin();
				double yMaxU = upperLabel.getYMax();
				for (int iLower = iUpper - 1; iLower >= 0; iLower--) {
					PredictionLabel lowerLabel = predictionLabels.get(iLower);
					if (lowerLabel != null) {
						double xMinL = lowerLabel.getXMin();
						double xMaxL = lowerLabel.getXMax();
						double yMinL = lowerLabel.getYMin();
						double yMaxL = lowerLabel.getYMax();
						if ((xMinL >= xMinU && xMinL <= xMaxU && yMinL >= yMinU && yMinL <= yMaxU)
								|| (xMinL >= xMinU && xMinL <= xMaxU
										&& yMaxL >= yMinU && yMaxL <= yMaxU)
								|| (xMaxL >= xMinU && xMaxL <= xMaxU
										&& yMinL >= yMinU && yMinL <= yMaxU)
								|| (xMaxL >= xMinU && xMaxL <= xMaxU
										&& yMaxL >= yMinU && yMaxL <= yMaxU)) {
							predictionLabels.set(iUpper, null);
							predictionLabels.set(iLower, null);
							break;
						}
					}
				}
			}
		}
		Vector<PredictionLabel> assignedPredictions = new Vector<PredictionLabel>();
		Vector<PeakLabel> assignedPeaks = new Vector<PeakLabel>();
		for (int iPrediction = 0; iPrediction < predictionLabels.size(); iPrediction++) {
			PredictionLabel predictionLabel = predictionLabels.get(iPrediction);
			if (predictionLabel != null) {
				double xMin = predictionLabel.getXMin();
				double xMax = predictionLabel.getXMax();
				double yMin = predictionLabel.getYMin();
				double yMax = predictionLabel.getYMax();
				PeakLabel assignedPeakLabel = null;
				for (int iMeasured = 0; iMeasured < peakLabels.size(); iMeasured++) {
					PeakLabel currentLabel = peakLabels.get(iMeasured);
					double x = currentLabel.getXPosUnits();
					double y = currentLabel.getYPosUnits();
					if (x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
						if (assignedPeakLabel == null) {
							assignedPeakLabel = currentLabel;
						} else {
							assignedPeakLabel = null;
							break;
						}
					}
				}
				if (predictionLabel != null && assignedPeakLabel != null) {
					if (isDebug)
						System.out.println("prediction=["
								+ predictionLabel.getXPos() + ","
								+ predictionLabel.getYPos() + "]peakLabel=["
								+ assignedPeakLabel.getXPos() + ","
								+ assignedPeakLabel.getYPos() + "]");
					assignedPredictions.add(predictionLabel);
					assignedPeaks.add(assignedPeakLabel);
				}
			}
		}
		if (isDebug)
			System.out.println("found assignments "
					+ assignedPredictions.size());
		if (assignedPredictions.size() != assignedPeaks.size()) {
			interactions.getUserDialog().setText(
					"Error in conservative assignment algorithm");
			return;
		}
		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);
		if (molDisplay == null) {
			return;
		}
		TreeMap<Integer, ActAtomEntity> atomEntities = molDisplay.getAtomEntityMap();

		for (int iAssignment = 0; iAssignment < assignedPredictions.size(); iAssignment++) {
			PredictionLabel predicted = assignedPredictions.get(iAssignment);
			PeakLabel measured = assignedPeaks.get(iAssignment);
			AssignPredictionAction.createLinksWithTraces(interactions,
					predicted, measured, atomEntities);
		}
		interactions.getUserDialog().setText("");
	}

	private static Vector<PeakData> getMeasuredPeakData(Spectra spectrum) {
		Vector<PeakData> peaks = new Vector<PeakData>();
		int nEntities = spectrum.getEntitiesCount();
		double integral = 0.0;
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			if (spectrum.getEntity(iEntity) instanceof SmartPeakLabel) {
				SmartPeakLabel peakLabel = (SmartPeakLabel) spectrum
						.getEntity(iEntity);
				PeakData data = new PeakData();
				data.setPeakLabel(peakLabel);
				data.setChemicalShift(spectrum.pixelsToUnits(peakLabel
						.getLocation().x)

				);
				Vector linkedEntities = interactions
						.getLinkedDestEntities(peakLabel);
				for (int iLinkedEntity = 0; iLinkedEntity < linkedEntities
						.size(); iLinkedEntity++) {
					if (linkedEntities.get(iLinkedEntity) instanceof Integral)
						data.setNHydrogens(((Integral) linkedEntities
								.get(iLinkedEntity)).getRelArea());
				}
				integral += data.getNHydrogens();
				peaks.add(data);
			}
		}
		System.out.println("Integral=" + integral);
		return peaks;
	}

	private static Vector<PeakData> getPredictedPeakData(Spectra spectrum) {
		Vector<PeakData> peaks = new Vector<PeakData>();
		int nEntities = spectrum.getEntitiesCount();
		for (int iEntity = 0; iEntity < nEntities; iEntity++) {
			if (spectrum.getEntity(iEntity) instanceof SmartPeakLabel) {
				SmartPeakLabel peakLabel = (SmartPeakLabel) spectrum
						.getEntity(iEntity);
				PeakData data = new PeakData();
				data.setPeakLabel(peakLabel);
				data.setChemicalShift(spectrum.pixelsToUnits(peakLabel
						.getLocation().x)

				);
				Vector linkedEntities = interactions
						.getLinkedDestEntities(peakLabel);
				for (int iLinkedEntity = 0; iLinkedEntity < linkedEntities
						.size(); iLinkedEntity++) {
					Object entity = linkedEntities.get(iLinkedEntity);
					if (entity instanceof Integral)
						data.setNHydrogens(((Integral) entity).getRelArea());
					if (entity instanceof ActAtomEntity)
						data.setAtomId(((ActAtomEntity) entity).getAtomID());
				}
				peaks.add(data);
			}
		}
		return peaks;
	}

	@SuppressWarnings("unchecked")
	private static void createLinksWithTraces(InteractiveSurface interactions,
			PredictionLabel predLabel, PeakLabel peakLabel,
			TreeMap<Integer, ActAtomEntity> atomEntities) {
		TreeSet<ActAtomEntity> xAtoms = new TreeSet<ActAtomEntity>();
		TreeSet<ActAtomEntity> yAtoms = new TreeSet<ActAtomEntity>();
		Iterator<Integer> iterator = predLabel.getAtomIDIterator();
		while (iterator.hasNext()) {
			int atomID = iterator.next();
			ActAtomEntity atomEntity = atomEntities.get(atomID);
			if (atomEntity != null) {
				if (predLabel.isXAtom(atomID))
					xAtoms.add(atomEntity);
				else
					yAtoms.add(atomEntity);
			}
		}

		//
		TreeSet<SmartPeakLabel> peakLabels = new TreeSet<SmartPeakLabel>();
		boolean isHomonuclear = ((Spectra) peakLabel.getParentEntity())
				.isHomonuclear();
		Vector linkedEntities = interactions.getLinkedDestEntities(peakLabel);
		for (int i = 0; i < linkedEntities.size(); i++) {
			if (linkedEntities.get(i) instanceof SmartPeakLabel) {
				SmartPeakLabel spl = (SmartPeakLabel) linkedEntities.get(i);
				if (isHomonuclear) {
					double splPos = spl.getNmrSignal1D().getShift();
					double xPos = peakLabel.getXPos();
					double yPos = peakLabel.getYPos();
					if (Math.abs(xPos - splPos) < Math.abs(yPos - splPos)) {
						for (ActAtomEntity atomEntity : xAtoms)
							interactions.createLink(spl, atomEntity);
					} else {
						for (ActAtomEntity atomEntity : yAtoms)
							interactions.createLink(spl, atomEntity);
					}
				} else {
					if (isSmartPeakInHorSpectrum(spl)) {
						for (ActAtomEntity atomEntity : xAtoms)
							interactions.createLink(spl, atomEntity);
					} else {
						for (ActAtomEntity atomEntity : yAtoms)
							interactions.createLink(spl, atomEntity);
						if (yAtoms.size() == 0
								&& ((Spectra) peakLabel.getParentEntity())
										.getExperimentType() == ExperimentType.HSQC) {
							for (ActAtomEntity atomEntity : xAtoms)
								interactions.createLink(spl, atomEntity);
						}
					}
				}

			}
		}

	}

	private static boolean isSmartPeakInHorSpectrum(SmartPeakLabel spl) {
		Spectra parentSpectra = ((Spectra) spl.getParentEntity());
		SpectraDisplay parentDisplay = (SpectraDisplay) parentSpectra
				.getParentEntity();
		return parentDisplay.getHorRefSpectrum() == parentSpectra;
	}

}
