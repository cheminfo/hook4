package org.cheminfo.hook.nemo.nmr;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.graph.GraphAlgorithms;
import org.cheminfo.hook.math.functions.ScalarFunction;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.PredictionLabel;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.nemo.Spectra;
import org.cheminfo.hook.nemo.SpectraData;
import org.cheminfo.hook.nemo.SpectraDisplay;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.EPFLUtils;
import com.actelion.research.epfl.StereoInfo;

public class PredictionData {
	private static final boolean DEBUG=false;

	private String inputData = null;
	private Vector<PeakPrediction> predictions = null;
	private ActMoleculeDisplay moleculeDisplay = null;
	private SpectraDisplay spectraDisplay = null;
	private TreeMap<Nucleus, Double> standardErrors = null;
	private double simulationFrequency = NmrResurrector.DEFAULT_FREQUENCY;

	// needed for regenerating faked trace spectra
	private static final double fakePeakHeight = 10000;
	private boolean isProjectedTrace = false;
	private double firstX = Double.NaN;
	private double lastX = Double.NaN;
	private int nbPoints = -1;
	public static final String projectedTrace = "projected trace";

	private boolean ignoreSymmetry = false;
	private static final int nbPoints2D = 1024;
	private static final int nbPointsProjected = 32 * 1024;

	private TreeSet<Integer> fakePeaks = null;

	private Nucleus xNucleus = Nucleus.UNDEF;
	private Nucleus yNucleus = Nucleus.UNDEF;

	private int minDistance = -1;
	private int maxDistance = -1;

	private SpectraData spectraData=null;
	
	//  

	public PredictionData() {
		this.fakePeaks = new TreeSet<Integer>();
		this.predictions = new Vector<PeakPrediction>();
		this.standardErrors = Nucleus.getStandardErrors();
	}

	public int getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(int maxDistance) {
		this.maxDistance = maxDistance;
	}

	public Nucleus getXNucleus() {
		return xNucleus;
	}

	public void setXNucleus(Nucleus nucleus) {
		xNucleus = nucleus;
	}

	public Nucleus getYNucleus() {
		return yNucleus;
	}

	public void setYNucleus(Nucleus nucleus) {
		yNucleus = nucleus;
	}

	public ActMoleculeDisplay getMoleculeDisplay() {
		return moleculeDisplay;
	}

	public void setMoleculeDisplay(ActMoleculeDisplay molDisplay) {
		this.moleculeDisplay = molDisplay;
	}

	public void setInputData(String inputData) {
		this.inputData = inputData;
	}

	public String getInputData() {
		return this.inputData;
	}

	public void setSpectraDisplay(SpectraDisplay spectraDisplay) {
		this.spectraDisplay = spectraDisplay;
	}

	public void process() {
		if (this.inputData == null) return;
		this.parseData();
		if (this.spectraDisplay != null) {
			if (spectraDisplay.is2D()) {
				this.process2D();
			} else {
				this.process1D();
			}
		}
	}

	public String canonize(boolean suppressLabile) {
		String canonizedPredictions="";
		if (this.inputData == null) return null;
		this.parseData();
		if (suppressLabile) this.suppressLabile();
		if (this.maybeAbInitio()) this.joinSymmetricPredictions(true);
		for (PeakPrediction prediction : predictions) {
			canonizedPredictions+=prediction.getPredictionLine()+"\r\n";
		}
		return canonizedPredictions;
	}
	
	public Spectra simulate2DSpectrum() {
		if (this.inputData == null || this.moleculeDisplay == null)
			return null;
		if (this.xNucleus == Nucleus.UNDEF || this.yNucleus == Nucleus.UNDEF)
			return null;
		long startTime = System.currentTimeMillis();
		this.parseData();
		this.expandStereoCenters();
		Spectra spectra = this.create2DSpectrum();
		spectra.drawAs2D();
	//	spectra.generateContourLines(Spectra.DEFAULT_NB_CONTOURS);
		long endTime = System.currentTimeMillis();
		if (DEBUG) System.out.println("Seconds elapsed: " + ((endTime - startTime) / 1000.0));
		return spectra;
	}

	private Spectra create2DSpectrum() {
		SpectraData spectraData = this.createBlank2DSpectraData();
		Spectra spectra = new Spectra(spectraData);

		StereoMolecule enhancedMolecule = this.moleculeDisplay
				.getEnhancedMolecule();
		int nEnhancedAtoms = enhancedMolecule.getAllAtoms();
		int[] shortestPaths = GraphAlgorithms.FloydWarshall(EPFLUtils
				.getAdjecencyMatrix(enhancedMolecule), nEnhancedAtoms);

		// Prediction labels
		int keyOffset = (int) Math.ceil(Math.log10(nEnhancedAtoms));
		TreeMap<Integer, Integer> degenerateSymmetries = new TreeMap<Integer, Integer>();
		TreeMap<Integer, PredictionLabel> definedLabels = new TreeMap<Integer, PredictionLabel>();
		//
		this.generateSignals();
		//
		Vector<PeakPrediction> xPrediction = this.getPredictionsByNucleus(this.xNucleus);
		Vector<PeakPrediction> yPrediction = this.getPredictionsByNucleus(this.yNucleus);

		if (DEBUG) {
			System.out.println("#x=" + xPrediction.size() + "#y=" + yPrediction.size());
			System.out.println("minDistance=" + this.minDistance + "maxDistance=" + this.maxDistance);
		}
		//
		for (int iPredX = 0; iPredX < xPrediction.size(); iPredX++) {
			PeakPrediction predX = xPrediction.get(iPredX);
			if (predX.getAtoms() == 0 && this.xNucleus == this.yNucleus) {
				PredictionLabel predictionLabel = this.create2DPredictionLabel(
						predX, predX);
				spectra.addEntity(predictionLabel);
				predictionLabel.refreshSensitiveArea();
				predictionLabel.checkSizeAndPosition();
				continue;
			}
			for (int iPredY = 0; iPredY < yPrediction.size(); iPredY++) {
				PeakPrediction predY = yPrediction.get(iPredY);
				PredictionLabel currentLabel = null;
				for (int iAtomX : predX.getAtomIDs()) {
					int iEffectiveIDX = iAtomX;
					int symX = -1;
					if (enhancedMolecule.getAtomicNo(iAtomX) == 1) {
						int iHeavyAtom = predX.getHeavyAtomID(iAtomX);
						if (iHeavyAtom == -1)
							continue;
						if (this.moleculeDisplay.getStereoInfo(iHeavyAtom) == StereoInfo.HAS_DIFFERENT_PROTONS) {
							symX = this.moleculeDisplay.getSymRank(iAtomX);
						} else {
							symX = this.moleculeDisplay.getSymRank(iHeavyAtom);
							iEffectiveIDX = iHeavyAtom;
						}
					} else {
						symX = this.moleculeDisplay.getSymRank(iAtomX);
					}
					// create
					for (int iAtomY : predY.getAtomIDs()) {
						int iEffectiveIDY = iAtomY;
						int symY = -1;
						if (enhancedMolecule.getAtomicNo(iAtomY) == 1) {
							int iHeavyAtom = predY.getHeavyAtomID(iAtomY);
							if (iHeavyAtom == -1)
								continue;
							if (this.moleculeDisplay.getStereoInfo(iHeavyAtom) == StereoInfo.HAS_DIFFERENT_PROTONS) {
								symY = this.moleculeDisplay.getSymRank(iAtomY);
							} else {
								symY = this.moleculeDisplay
										.getSymRank(iHeavyAtom);
								iEffectiveIDY = iHeavyAtom;
							}
						} else {
							symY = this.moleculeDisplay.getSymRank(iAtomY);
						}
						int symKey = symY * keyOffset + symX;
						if (DEBUG) System.out.println("symKey=" + symKey);
						int distance = shortestPaths[iAtomX * nEnhancedAtoms
								+ iAtomY];
						if (DEBUG) {
							System.out.println("(" + iAtomX + "," + iAtomY
									+ ")=" + distance);
						}
						if (distance >= this.minDistance
								&& distance <= this.maxDistance
								|| (distance == 0 && this.xNucleus == this.yNucleus)) {
							if (DEBUG) {
								System.out.println("coupling (" + iAtomX + ","
										+ iAtomY + ")=");
							}
							PredictionLabel predictionLabel = null;
							if (currentLabel == null) {
								if (definedLabels.containsKey(symKey)
										&& !this.ignoreSymmetry) {
									// we already have predicition label with
									// the
									// defined
									// symmetry
									int degeneracy = 1;
									predictionLabel = definedLabels.get(symKey);
									if (DEBUG) System.out.println("symKey=" + symKey);
									if (predictionLabel != null
											&& this.isDifferentSimulation(
													predictionLabel, predX,
													predY)) {
										predictionLabel = null;
									}
									// if we have already stored degenerate
									// symmetries we
									// need
									// to scan them
									if (predictionLabel == null
											&& degenerateSymmetries
													.containsKey(symKey)) {
										degeneracy = degenerateSymmetries
												.get(symKey) + 1;
										int testKey;
										for (int iDeg = 1; iDeg < degeneracy; iDeg++) {
											testKey = -(symKey * 100 + degeneracy);
											predictionLabel = definedLabels
													.get(testKey);
											if (predictionLabel != null
													&& !this
															.isDifferentSimulation(
																	predictionLabel,
																	predX,
																	predY)) {
												symKey = testKey; // change
												// the
												// sym
												// Rank to the
												// degenerate rank
												// as we have found
												// something
												break;
											} else {
												predictionLabel = null;
											}
										}
									}
									if (predictionLabel == null) {
										degenerateSymmetries.put(symKey,
												degeneracy); // store
										// the
										// level
										// of
										// degenercy
										symKey = -(symKey * 100 + degeneracy);
										// create a peak label
										predictionLabel = this
												.create2DPredictionLabel(predX,
														predY);
										spectra.addEntity(predictionLabel);
										definedLabels.put(symKey,
												predictionLabel);
									}
								} else {
									predictionLabel = this
											.create2DPredictionLabel(predX,
													predY);
									spectra.addEntity(predictionLabel);
									definedLabels.put(symKey, predictionLabel);
								}
							} else {
								predictionLabel = currentLabel;
							}
							if (predictionLabel != null) {
								predictionLabel.addAtomID(iEffectiveIDX, true);
								predictionLabel.addAtomID(iEffectiveIDY, false);
								if (currentLabel == null) {
									currentLabel = predictionLabel;
								}
							}
							// this.generateSignal(spectraData, predX
							// .getAnalyticSignal(), predY
							// .getAnalyticSignal());
						}
					}
				}
			}
		}
		return spectra;
	}

	public Spectra resurrectOneDim() {
		this.parseData();
		if (this.isProjectedTrace) {
			return this.reconstructTrace();
		} else {
			return this.simulateSpectrum();
		}

	}

	private Spectra reconstructTrace() {
		if (DEBUG) System.out.println("resurrecting Trace");
		Spectra spectrum = this.createEmptySpectrum(this.xNucleus,
				PredictionData.nbPointsProjected, this.simulationFrequency, firstX, lastX);
		spectrum.setPredictionData(this);
		if (this.fakePeaks.size() > 0) {
			SpectraData spectraData = spectrum.getSpectraData();
			double[] yData = spectraData.getSubSpectraDataY(0);
			for (int iPos : this.fakePeaks) {
				yData[iPos] = PredictionData.fakePeakHeight;
				if (DEBUG) System.out.println("fakePeak=" + iPos);
			}
			// spectraData.prepareSpectraData();
			spectraData.updateDefaults();
		} else if (DEBUG) {
			System.out.println("no fake peaks");
		}
		spectrum.getSpectraData().resetMinMax();
		return spectrum;
	}

	/**
	 * 
	 * @return
	 */
	private boolean maybeAbInitio() {
		int maxIDS = 0;
		for (int i = 0; i < this.predictions.size(); i++) {
			int count = this.predictions.get(i).getAtoms();
			if (count > maxIDS)
				maxIDS = count;
		}
		if (maxIDS == 1)
			return true;
		else
			return false;
	}
	
	private void process1D() {
		if (DEBUG) System.out.println("Processing 1D");
		Spectra spectrum = this.spectraDisplay.getFirstSpectra();
		this.simulationFrequency = NmrResurrector.DEFAULT_FREQUENCY;
		if (spectrum != null) {
			SpectraData spectraData = spectrum.getSpectraData();
			this.simulationFrequency = spectraData.getParamDouble("$SFO1", this.simulationFrequency);
			this.xNucleus = spectraData.getNucleus();
		} else {
			// we need to determine the nucleus we want to take
			// because there is not yet a spectrum we will simply take the first peak
			if (predictions.size()>0) {
				this.xNucleus=predictions.get(0).getNucleus();
			}
		}
		if (this.maybeAbInitio()) this.joinSymmetricPredictions(true);
		int vPosition = this.spectraDisplay.getNbAddedSpectra() * (-20) - 10;
		
		Spectra simulatedSpectrum = this.simulateSpectrum();

		Color primaryColor = Color.BLUE;
		Color secondaryColor = simulatedSpectrum.getSecondaryColor();
		simulatedSpectrum.setPrimaryColor(primaryColor);
		this.spectraDisplay.addSpectra(simulatedSpectrum);
		simulatedSpectrum.setLocation(simulatedSpectrum.getLocation().x,vPosition);
		this.spectraDisplay.checkInteractiveSurface();
		if (this.moleculeDisplay != null) {
			NmrSimulator.createPeakAtomLinks(this.moleculeDisplay, simulatedSpectrum);
		} else
			System.out.println("molDisplay not found");
	}

	private Spectra simulateSpectrum() {
		/**
		 * @TODO Simulate the spectrum
		 */
		return null;
		/*InteractiveSurface interactiveSurface = null;
		if (this.spectraDisplay != null)
			interactiveSurface = this.spectraDisplay.getInteractiveSurface();
		NmrSimulator simulator = new NmrSimulator(interactiveSurface);
		Spectra spectra=simulator.simulateFromPredictionData(this, this.xNucleus);
		this.spectraData=spectra.getSpectraData();
		return spectra;*/
	}

	private void process2D() {
		if (DEBUG) System.out.println("Processing 2D");
		/* in 2D spectra we add prediction labels to the spectrum */
		Spectra spectrum = this.spectraDisplay.getFirstSpectra();
		if (spectrum == null)
			return;
		ExperimentType experimentType = spectrum.getExperimentType();
		SpectraData spectraData = spectrum.getSpectraData();
		double xa = spectraData.getFirstX();
		double xb = spectraData.getLastX();
		double ya = spectraData.getParamDouble("firstY", 0.0);
		double yb = spectraData.getParamDouble("lastY", 0.0);
		Vector<PredictionLabel> predictionLabels = null;
		// check for a structure
		if (this.minDistance == -1 || this.maxDistance == -1) {
			if (experimentType == ExperimentType.HMBC) {
				this.minDistance = 2;
				this.maxDistance = 4;
			} else if (experimentType == ExperimentType.HSQC) {
				this.minDistance = 1;
				this.maxDistance = 1;
			} else if (experimentType == ExperimentType.COSY) {
				this.minDistance = 3;
				this.maxDistance = 3;
			} else {
				return;
			}
		}
		if (this.moleculeDisplay != null) {
			this.expandStereoCenters();
			if (DEBUG) System.out.println("EXPERIMENT=" + experimentType.toString());
			this.xNucleus = spectrum.getNucleus(1);
			this.yNucleus = spectrum.getNucleus(2);
			predictionLabels = this.generatePrediction(xa, xb, ya, yb);
		} else if (spectrum.isHomonuclear()) {
			if (DEBUG) System.out.println("\tCOSY (diagonal)");
			predictionLabels = this.generateDiagonalPrediction(xa, xb, ya, yb);
		}
		// add labels to spectrum
		if (DEBUG) System.out.println("prediction labels " + predictionLabels.size());
		if (predictionLabels != null && predictionLabels.size() > 0)
			this.addLabelsToSpectrum(spectrum, predictionLabels);

	}

	private Vector<PredictionLabel> generatePrediction(double xa, double xb,
			double ya, double yb) {
		StereoMolecule enhancedMolecule = this.moleculeDisplay.getEnhancedMolecule();
		int nEnhancedAtoms = enhancedMolecule.getAllAtoms();
		int[] shortestPaths = GraphAlgorithms.FloydWarshall(EPFLUtils
				.getAdjecencyMatrix(enhancedMolecule), nEnhancedAtoms);

		if (DEBUG) {
			System.out.println("xNucleus=" + this.xNucleus.toString());
			System.out.println("yNucleus=" + this.yNucleus.toString());
		}
		// Prediction labels
		int keyOffset = (int) Math.ceil(Math.log10(nEnhancedAtoms));
		TreeMap<Integer, Integer> degenerateSymmetries = new TreeMap<Integer, Integer>();
		TreeMap<Integer, PredictionLabel> definedLabels = new TreeMap<Integer, PredictionLabel>();
		//
		this.generateSignals();
		//
		Vector<PeakPrediction> xPrediction = this
				.getPredictionsByNucleus(this.xNucleus);
		Vector<PeakPrediction> yPrediction = this
				.getPredictionsByNucleus(this.yNucleus);

		if (yPrediction.size() == 0) {
			if (DEBUG) System.out.println("fallback");
			return this.noHeteroFallback(xPrediction, xa, xb, ya, yb);
		}
		//
		Vector<PredictionLabel> labels = new Vector<PredictionLabel>();
		if (DEBUG) {
			System.out.println("#x=" + xPrediction.size() + "#y="
					+ yPrediction.size());
			System.out.println("minDistance=" + this.minDistance
					+ "maxDistance=" + this.maxDistance);
		}
		//
		for (int iPredX = 0; iPredX < xPrediction.size(); iPredX++) {
			PeakPrediction predX = xPrediction.get(iPredX);
			if (!(predX.getCenter() <= Math.max(xa, xb) && predX.getCenter() >= Math
					.min(xa, xb)))
				continue;
			if (predX.getAtoms() == 0 && this.xNucleus == this.yNucleus) {
				PredictionLabel predictionLabel = this.create2DPredictionLabel(
						predX, predX);
				labels.add(predictionLabel);
			}
			for (int iPredY = 0; iPredY < yPrediction.size(); iPredY++) {
				PeakPrediction predY = yPrediction.get(iPredY);
				if (!(predY.getCenter() <= Math.max(ya, yb) && predY
						.getCenter() >= Math.min(ya, yb)))
					continue;
				PredictionLabel currentLabel = null;
				for (int iAtomX : predX.getAtomIDs()) {
					int symX = -1;
					if (enhancedMolecule.getAtomicNo(iAtomX) == 1) {
						int iHeavyAtom = predX.getHeavyAtomID(iAtomX);
						if (iHeavyAtom == -1)
							continue;
						if (this.moleculeDisplay.getStereoInfo(iHeavyAtom) == StereoInfo.HAS_DIFFERENT_PROTONS) {
							symX = this.moleculeDisplay.getSymRank(iAtomX);
						} else {
							symX = this.moleculeDisplay.getSymRank(iHeavyAtom);
						}
					} else {
						symX = this.moleculeDisplay.getSymRank(iAtomX);
					}
					// 
					for (int iAtomY : predY.getAtomIDs()) {
						int symY = -1;
						if (enhancedMolecule.getAtomicNo(iAtomY) == 1) {
							int iHeavyAtom = predY.getHeavyAtomID(iAtomY);
							if (iHeavyAtom == -1)
								continue;
							if (this.moleculeDisplay.getStereoInfo(iHeavyAtom) == StereoInfo.HAS_DIFFERENT_PROTONS) {
								symY = this.moleculeDisplay.getSymRank(iAtomY);
							} else {
								symY = this.moleculeDisplay
										.getSymRank(iHeavyAtom);
							}
						} else {
							symY = this.moleculeDisplay.getSymRank(iAtomY);
						}
						int symKey = symY * keyOffset + symX;
						int distance = shortestPaths[iAtomX * nEnhancedAtoms
								+ iAtomY];
						if (DEBUG) {
							System.out.println("(" + iAtomX + "," + iAtomY
									+ ")=" + distance);
						}
						if (distance >= this.minDistance
								&& distance <= this.maxDistance
								|| (distance == 0 && this.xNucleus == this.yNucleus)) {
							PredictionLabel predictionLabel = null;
							if (currentLabel == null) {
								if (definedLabels.containsKey(symKey)
										&& !this.ignoreSymmetry) {
									// we already have predicition label with
									// the
									// defined
									// symmetry
									int degeneracy = 1;
									predictionLabel = definedLabels.get(symKey);
									// System.out.println("symKey=" + symKey);
									if (predictionLabel != null
											&& this.isDifferentPrediction(
													predictionLabel, predX,
													predY)) {
										predictionLabel = null;
									}
									// if we have already stored degenerate
									// symmetries we need to scan them
									if (predictionLabel == null
											&& degenerateSymmetries
													.containsKey(symKey)) {
										degeneracy = degenerateSymmetries
												.get(symKey) + 1;
										int testKey;
										for (int iDeg = 1; iDeg < degeneracy; iDeg++) {
											testKey = -(symKey * 100 + degeneracy);
											predictionLabel = definedLabels
													.get(testKey);
											if (predictionLabel != null
													&& !this
															.isDifferentPrediction(
																	predictionLabel,
																	predX,
																	predY)) {
												symKey = testKey; // change
												// the
												// sym
												// Rank to the
												// degenerate rank
												// as we have found
												// something
												break;
											} else {
												predictionLabel = null;
											}
										}
									}
									if (predictionLabel == null) {
										degenerateSymmetries.put(symKey,
												degeneracy); // store
										// the level of degenercy
										symKey = -(symKey * 100 + degeneracy);
										// create a peak label
										predictionLabel = this
												.create2DPredictionLabel(predX,
														predY);
										definedLabels.put(symKey,
												predictionLabel);
									}
								} else {
									predictionLabel = this
											.create2DPredictionLabel(predX,
													predY);
									definedLabels.put(symKey, predictionLabel);
								}
							} else {
								predictionLabel = currentLabel;
							}
							if (predictionLabel != null) {
								predictionLabel.addAtomID(iAtomX, true);
								predictionLabel.addAtomID(iAtomY, false);
								predictionLabel.addCoupling(iAtomX, iAtomY);
								if (currentLabel == null)
									currentLabel = predictionLabel;
							}
						}
					}
				}
			}
		}
		labels.addAll(definedLabels.values());
		return labels;
	}

	private void expandStereoCenters() {
		StereoMolecule compactMolecule = this.moleculeDisplay
				.getCompactMolecule();
		for (int iAtom = 0; iAtom < compactMolecule.getAllAtoms(); iAtom++)
			if (this.moleculeDisplay.getStereoInfo(iAtom) == StereoInfo.HAS_DIFFERENT_PROTONS
					&& !this.moleculeDisplay.isAtomExpanded(iAtom))
				this.moleculeDisplay.switchExpansionState(iAtom);
	}

	private void addLabelsToSpectrum(Spectra spectrum,
			Vector<PredictionLabel> predictionLabels) {
		// if (isDebug)
		// System.out.println("adding labels to spectrum");
		TreeMap<Integer, ActAtomEntity> atomEntityMap = new TreeMap<Integer, ActAtomEntity>();
		InteractiveSurface interactiveSurface = this.spectraDisplay
				.getInteractiveSurface();
		if (this.moleculeDisplay != null) {
			atomEntityMap = this.moleculeDisplay.getAtomEntityMap();
		}
		// find links for hydrogen atoms that are not expanded
		// add the stuff
		for (int i = 0; i < predictionLabels.size(); i++) {
			// if (isDebug)
			// System.out.println("prediction label: " + i);
			PredictionLabel currentLabel = predictionLabels.get(i);
			spectrum.addEntity(currentLabel);
			currentLabel.refreshSensitiveArea();
			currentLabel.checkSizeAndPosition();
			currentLabel.setHeterolink(false);
			Iterator<Integer> iterator = currentLabel.getAtomIDIterator();
			while (iterator.hasNext()) {
				Integer atomId = iterator.next();
				ActAtomEntity atomEntity = atomEntityMap.get(atomId);
				if (atomEntity != null) {
					interactiveSurface.createLink(currentLabel, atomEntity);
				}
			}
		}
		interactiveSurface.repaint();
	}

	public Vector<PeakPrediction> getPredictionsByNucleus(Nucleus nucleus) {
		Vector<PeakPrediction> peaks = new Vector<PeakPrediction>(
				this.predictions.size());
		for (int iPeak = 0; iPeak < this.predictions.size(); iPeak++)
			if (this.predictions.get(iPeak).getNucleus() == nucleus)
				peaks.add(this.predictions.get(iPeak));
		return peaks;
	}

	private void parseData() {
		boolean splitAssignment=true;
		if (this.moleculeDisplay == null) {
			splitAssignment=false;
		}
		
		Spectra spectrum = null;
		if (this.spectraDisplay != null)
			spectrum = this.spectraDisplay.getFirstSpectra();
		if (spectrum == null) {
			// xNucleus = spectrum.getNu
		} else {
			Nucleus xNucleus = spectrum.getNucleus(1);
			Nucleus yNucleus = spectrum.getNucleus(2);
			SpectraData spectraData = spectrum.getSpectraData();
			double ya = spectraData.getParamDouble("firstY", 0.0);
			double yb = spectraData.getParamDouble("lastY", 0.0);
			double xStandardError = Math.abs(spectraData.getInterval());
			double yStandardError = Math.abs((yb - ya) / (spectraData.getNbSubSpectra() + 1e-6));
			standardErrors.put(xNucleus, xStandardError);
			standardErrors.put(yNucleus, yStandardError);
		}
		String[] lines = this.inputData.split("[\r\n]+");
		for (int iLine = 0; iLine < lines.length; iLine++) {
			String currentLine = lines[iLine];
			if (currentLine.indexOf("atomID") != -1)
				continue;
			if (currentLine.indexOf("METADATA") != -1) {
				this.handleMetadata(currentLine);
				continue;
			}
			if (DEBUG) System.out.println("line: " + currentLine);
			String fields[] = currentLine.replaceAll("null","").split("\t");

			if (fields.length > 2) {
				
				boolean useDiastereoID=false;
				if (! fields[0].matches("^[0-9,]*$")) {
					useDiastereoID=true;
				}
				
				// we will give the possibility to split the assignment
				
				int maxValue=1;
				
				
				String ids[]=null;
				//if (splitAssignment) {
				if (fields[0].length() > 0) {
					ids = fields[0].split(",");
					maxValue=ids.length;
				}
				//}
				
				for (int i=0; i<maxValue; i++) {
				
					PeakPrediction currentPrediction = new PeakPrediction();
					this.predictions.add(currentPrediction);
	
					// TODO what should we do if ID already there ??????
					if (fields[0].length() > 0) {
						try {
							if (splitAssignment) {
								if (useDiastereoID) {
									currentPrediction.addDiastereoID(ids[i].trim());
								} else {
									currentPrediction.addAtomID(Integer.parseInt(ids[i].trim()));
								}
							} else {
								if (ids!=null) {
									for (int j = 0; j < ids.length; j++) {
										if (useDiastereoID) {
											currentPrediction.addDiastereoID(ids[j].trim());
										} else {
											currentPrediction.addAtomID(Integer.parseInt(ids[j].trim()));
										}
									}
								}
							}
						} catch (NumberFormatException e) {
							System.out.println("Number format exception (ID)");
						}
					}
	
					String nucString = fields[1];
					if ((nucString.equals("")) || (nucString.equals("null"))) {
						nucString = "1H";
					}
					Nucleus nuc = Nucleus.determineNucleus(nucString);
					currentPrediction.setNucleus(nuc);
					if (nuc == Nucleus.NUC_1H && this.moleculeDisplay != null)
						resolveHeavyAtomIDs(this.moleculeDisplay, currentPrediction);
					String delta1string = fields[2];
					double error = 0;
					double delta1 = Double.NEGATIVE_INFINITY;
					int position = delta1string.indexOf("+-");
	
					if (position > 0) { // there is an error
						try {
							delta1 = Double.parseDouble(delta1string.substring(0,
									position - 1).trim());
							error = Double.parseDouble(delta1string.substring(
									position + 2).trim());
						} catch (NumberFormatException e) {
							System.out.println("Number format exception (d1)");
						}
					} else {
						try {
							delta1 = Double.parseDouble(delta1string);
						} catch (NumberFormatException e) {
							System.out.println("Number format exception (d1)");
						}
					}
	
					if (delta1 != Double.NEGATIVE_INFINITY) {
						if (error == 0) {
							Double err = standardErrors.get(currentPrediction
									.getNucleus());
							if (err != null)
								error = err;
							else
								error = 0.001;
						}
					}
	
					currentPrediction.setChemicalShift(delta1);
					currentPrediction.setError(error);
	
					// second chemical shift ?
					if (fields.length > 3) {
						if (fields[3].length() > 0) {
							try {
								currentPrediction.setSecondChemicalShift(Double.parseDouble(fields[3].trim()));
							} catch (NumberFormatException e) {
								System.out.println("Number format exception (d2)");
							}
						}
					}
	
					// multiplicity
					if ((fields.length > 4) && (fields[4]!=null)) {
						currentPrediction.setPeakPattern(fields[4].trim());
					}
	
					// integration
					if ((fields.length > 5) && (fields[5]!=null)) {
						if (fields[5].length() > 0) {
							try {
								currentPrediction.setIntegral(Double.parseDouble(fields[5].trim()));
							} catch (NumberFormatException e) {
								currentPrediction.setIntegral(1);
								System.out.println("Number format exception of integral: "+fields[5].trim());
							}
						}
					}
	
					// finally we may have all the coupling constants with each
					// multiplicity
	
					// have we reached the end of it all?
					if (fields.length > 7) {
						for (int j = 6; j < (fields.length - 1); j = j + 2) {
							Coupling newCoupling = new Coupling();
							newCoupling.setMultiplicity(fields[j]);
							if (fields[j + 1].length() > 0) {
								try {
									newCoupling.setCouplingConstant(Double.parseDouble(fields[j + 1]));
									currentPrediction.addCoupling(newCoupling);
								} catch (NumberFormatException e) {
									System.out.println("Number format exception");
								}
							}
							if (DEBUG) System.out.println("Coupling constant: "+newCoupling.toString());
						}
					}
				}
				
			}
		}

		if (DEBUG) {
			for (PeakPrediction prediction : this.predictions) {
				System.out.println(prediction);
			}
		}

	}

	/**
	 * Creates diagonal peaks for COSY spectra.
	 * 
	 * @param xa
	 *            x-axis limit one
	 * @param xb
	 *            x-axis limit two
	 * @param ya
	 *            y-axis limit one
	 * @param yb
	 *            y-axis limit two
	 * @return
	 */
	private Vector<PredictionLabel> generateDiagonalPrediction(double xa,
			double xb, double ya, double yb) {
		Vector<PeakPrediction> protonPeaks = this
				.getPredictionsByNucleus(Nucleus.NUC_1H);
		Vector<PredictionLabel> predictionLabels = new Vector<PredictionLabel>(
				protonPeaks.size());
		for (int iPeak = 0; iPeak < protonPeaks.size(); iPeak++) {
			PeakPrediction peak = protonPeaks.get(iPeak);
			double xValue = peak.getChemicalShift();
			double xError = Math.abs(peak.getError());
			double xMin = xValue - xError;
			double xMax = xValue + xError;
			if (xValue >= Math.min(xa, xb) && xValue <= Math.max(xa, xb)
					&& xValue >= Math.min(ya, yb) && xValue <= Math.max(ya, yb)) {
				PredictionLabel label = new PredictionLabel(xValue, xValue,
						xMin, xMin, xMax, xMax);
				predictionLabels.add(label);
			}
		}
		return predictionLabels;
	}

	/**
	 * This method provides a fallback for heteronuclear 2D experiments when
	 * only a proton prediction is available. The generated peak labels extend
	 * over the whole range of the non hydrogen coordinate.
	 * 
	 * @param compactMolecule
	 *            the compact molecule from the relevant ActMoleculeDisplay
	 * @param protonPeaks
	 *            the proton prediction
	 * @param yNucleus
	 *            the type of the y nucleus
	 * @param xa
	 *            the x-axis lower limit
	 * @param xb
	 *            the x-axis upper limit
	 * @param ya
	 *            the y-axis lower limit
	 * @param yb
	 *            the y-axis upper limit
	 * @return
	 */
	private Vector<PredictionLabel> noHeteroFallback(
			Vector<PeakPrediction> protonPeaks, double xa, double xb,
			double ya, double yb) {
		Vector<PredictionLabel> predictionLabels = new Vector<PredictionLabel>(
				protonPeaks.size());
		TreeMap<Integer, PredictionLabel> symmetryMap = new TreeMap<Integer, PredictionLabel>();
		for (int iPeak = 0; iPeak < protonPeaks.size(); iPeak++) {
			PeakPrediction currentPrediction = protonPeaks.get(iPeak);
			int iHydrogenID = currentPrediction.getFirstAtomId();
			int iHeavyAtomID = currentPrediction.getHeavyAtomID(iHydrogenID);
			if (iHeavyAtomID == -1)
				continue;
			boolean isStereo = this.moleculeDisplay.getStereoInfo(iHeavyAtomID) == StereoInfo.HAS_DIFFERENT_PROTONS;
			int symmetryId = isStereo ? this.moleculeDisplay
					.getSymRank(iHydrogenID) : this.moleculeDisplay
					.getSymRank(iHeavyAtomID);
			PredictionLabel label = null;
			if (symmetryMap.containsValue(symmetryId)) {
				label = symmetryMap.get(symmetryId);
			} else {
				double hValue, hMin, hMax;
				if (currentPrediction.isMassive()) {
					hMin = Math.min(currentPrediction.getChemicalShift(),
							currentPrediction.getSecondChemicalShift());
					hMax = Math.max(currentPrediction.getChemicalShift(),
							currentPrediction.getSecondChemicalShift());
					hValue = (hMin + hMax);
				} else {
					hValue = currentPrediction.getChemicalShift();
					double error = Math.abs(currentPrediction.getError());
					hMin = hValue - error;
					hMax = hValue + error;
				}
				if (hValue >= Math.min(xa, xb) && hValue <= Math.max(xa, xb))
					label = new PredictionLabel(hValue, (ya + yb) / 2, hMin,
							Math.min(ya, yb), hMax, Math.max(ya, yb));
				if (label != null) {
					symmetryMap.put(symmetryId, label);
					predictionLabels.add(label);
				}
			}
			if (label != null)
				label.addAtomID(iHydrogenID, true);

		}
		return predictionLabels;
	}

	private PredictionLabel create2DPredictionLabel(PeakPrediction peakX,
			PeakPrediction peakY) {
		// define a new label
		double xValue = peakX.getCenter();
		double xMin = peakX.getLowerLimit();
		double xMax = peakX.getUpperLimit();
		double yValue = peakY.getCenter();
		double yMin = peakY.getLowerLimit();
		double yMax = peakY.getUpperLimit();
		return new PredictionLabel(xValue, yValue, xMin, yMin, xMax, yMax);
	}

	public double getSimulationFrequency() {
		return simulationFrequency;
	}

	public void setSimulationFrequency(double simulationFrequency) {
		this.simulationFrequency = simulationFrequency;
	}

	public String getXMLData() {
		StringBuffer xmlBuffer = new StringBuffer();
		xmlBuffer.append("METADATA:simulationFrequency=").append(this.simulationFrequency).append("\n");
		if (this.spectraData!=null) {
			xmlBuffer.append("METADATA:firstX=").append(spectraData.getFirstX()).append("\n");
			xmlBuffer.append("METADATA:lastX=").append(spectraData.getLastX()).append("\n");
		}
		
		if (this.fakePeaks.size() > 0) {
			xmlBuffer.append("METADATA:fakePeaks=");
			Iterator<Integer> iterator = this.fakePeaks.iterator();
			while (iterator.hasNext()) {
				int id = iterator.next().intValue();
				xmlBuffer.append(id);
				if (DEBUG)
					System.out.println("id=" + id);
				if (iterator.hasNext())
					xmlBuffer.append(',');
			}
			xmlBuffer.append("\n");
		}
		
		xmlBuffer.append("METADATA:xNucleus=").append(this.xNucleus.toString()).append("\n");
		xmlBuffer.append(this.inputData);
		return xmlBuffer.toString();
	}

	/*
	 * Fake 1D sepctra and
	 */
	private void handleMetadata(String inputLine) {
		int pos = inputLine.indexOf('=');
		if (inputLine.indexOf("simulationFrequency") != -1) {
			this.simulationFrequency = Double.parseDouble(inputLine.substring(
					pos + 1).trim());
			if (DEBUG)
				System.out.println("simulationFrequency=" + this.simulationFrequency);
		} else if (inputLine.indexOf("nbPoints") != -1) {
			this.nbPoints = Integer.parseInt(inputLine.substring(pos + 1).trim());
		} else if (inputLine.indexOf("firstX") != -1) {
			this.firstX = Double.parseDouble(inputLine.substring(pos + 1).trim());
		} else if (inputLine.indexOf("lastX") != -1) {
			this.lastX = Double.parseDouble(inputLine.substring(pos + 1).trim());
		} else if (inputLine.indexOf("xNucleus") != -1) {
			this.xNucleus = Nucleus.determineNucleus(inputLine.substring( pos + 1).trim());
		} else if (inputLine.indexOf("yNucleus") != -1) {
			this.yNucleus = Nucleus.determineNucleus(inputLine.substring( pos + 1).trim());
		} else if (inputLine.indexOf("type") != -1 && inputLine.indexOf(PredictionData.projectedTrace) != -1) {
			this.isProjectedTrace = true;
		} else if (inputLine.indexOf("fakePeaks") != -1) {
			String[] ids = inputLine.substring(pos + 1).trim().split(",");
			for (int i = 0; i < ids.length; i++) {
				this.fakePeaks.add(Integer.parseInt(ids[i]));
			}
		}
	}


	public boolean ignoreSymmetry() {
		return ignoreSymmetry;
	}

	public void setIgnoreSymmetry(boolean ignoreSymmetry) {
		this.ignoreSymmetry = ignoreSymmetry;
	}

	
	/**
	 * Suppress H that are not connected to a C
	 */
	public void suppressLabile() {
		if (this.moleculeDisplay == null) return;
		if (DEBUG) System.out.println("We will suppress Labile protons");
		Vector<PeakPrediction> newPredictions = new Vector<PeakPrediction>();
		for (PeakPrediction prediction : this.predictions) {
			// we need to check if it is a proton
			if (DEBUG) System.out.println("Analysing prediction: "+prediction);
			if (prediction.getNucleus()!=Nucleus.NUC_1H) {
				newPredictions.add(prediction);
			} else {
				// we find to which atom it is connected
				try {
					if (this.moleculeDisplay.isProtonOnCarbon(prediction.getFirstAtomId())) {
						newPredictions.add(prediction);
					}
				} catch (Exception e) {
					System.out.println("Could not get the number of the atom while suppressLabile");
					newPredictions.add(prediction);
				}
			}
		}
		this.predictions=newPredictions;
	}
	
	/**
	 * This routine performs a join of all peak Predictions base on symmetry.
	 * This is useful for prediction results that come from ab initio
	 * calculations where each protons has a different chemical shift!
	 * 
	 * The parameter complete missing adds missing atom IDS if they can be found
	 * This is also useful for ab initio calculations where point group symmetry
	 * was used to eliminate equivalent atoms.
	 * 
	 * @param completeMissing
	 */
	public void joinSymmetricPredictions(boolean completeMissing) {
		if (this.moleculeDisplay == null) return;
		StereoMolecule enhancedMolecule = this.moleculeDisplay.getEnhancedMolecule();
		int nAtoms = enhancedMolecule.getAllAtoms();
		int[] symRanks = new int[nAtoms];
		boolean[] processed = new boolean[nAtoms];
		Arrays.fill(processed, false);
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			symRanks[iAtom] = this.moleculeDisplay.getSymRank(iAtom);
			// protons are to be handled somewhat special
			if (enhancedMolecule.getAtomicNo(iAtom) == 1) {
				int iHeavyAtom = enhancedMolecule.getConnAtom(iAtom, 0);
				if (this.moleculeDisplay.getStereoInfo(iHeavyAtom) != StereoInfo.HAS_DIFFERENT_PROTONS) {
					symRanks[iAtom] = this.moleculeDisplay.getSymRank(iHeavyAtom);
				}
			}
		}
		LinkedList<PeakPrediction> processedPredictions = new LinkedList<PeakPrediction>();
		Vector<PeakPrediction> equivalentAtoms = new Vector<PeakPrediction>();
		while (this.predictions.size() > 0) {
			equivalentAtoms.clear();
			PeakPrediction currentPrediction = this.predictions.remove(this.predictions.size() - 1);
			equivalentAtoms.add(currentPrediction);
			if (currentPrediction.getAtoms() == 1) {
				Nucleus currentNucleus = currentPrediction.getNucleus();
				int iAtomID = currentPrediction.getFirstAtomId();
				int symRank = symRanks[iAtomID];
				for (int i = this.predictions.size() - 1; i >= 0; i--) {
					PeakPrediction prediction = this.predictions.get(i);
					if (prediction.getAtoms() == 1
							&& prediction.getNucleus() == currentNucleus
							&& symRanks[prediction.getFirstAtomId()] == symRank) {
						equivalentAtoms.add(prediction);
						this.predictions.remove(i);
					}
				}
				if (equivalentAtoms.size() > 1) {
					PeakPrediction newPrediction = new PeakPrediction();
					newPrediction.setSymRank(symRank);
					newPrediction.setNucleus(equivalentAtoms.get(0).getNucleus());
					double deltaAverage = 0.0;
					for (int i = 0; i < equivalentAtoms.size(); i++) {
						PeakPrediction prediction = equivalentAtoms.get(i);
						deltaAverage += prediction.getChemicalShift();
						for (int atomID : prediction.getAtomIDs()) {
							newPrediction.addAtomID(atomID);
							processed[atomID] = true;
						}
						// TODO: average coupling
						// currently we just take the first coupling constant
						if (i==0) {
							for (int iCoupling = 0; iCoupling < prediction.getCouplings(); iCoupling++) {
								newPrediction.addCoupling(prediction.getCoupling(iCoupling));
							}
						}
					}
					// chemical shift
					deltaAverage /= equivalentAtoms.size();
					newPrediction.setChemicalShift(deltaAverage);
					// integral
					newPrediction.setIntegral(equivalentAtoms.size());
					// set coupling pattern;
					String peakPattern = "";
					if (newPrediction.getCouplings() > 0) {
						for (int iCoupling = 0; iCoupling < newPrediction
								.getCouplings(); iCoupling++) {
							peakPattern += newPrediction.getCoupling(iCoupling)
									.getMultiplicity();
						}
					} else {
						peakPattern = "s";
					}
					newPrediction.setPeakPattern(peakPattern);
					processedPredictions.add(newPrediction);
				} else {
					processedPredictions.addAll(equivalentAtoms);
				}
			} else {
				processedPredictions.add(currentPrediction);
				for (int atomID : currentPrediction.getAtomIDs())
					processed[atomID] = true;
			}
		}
		// 
		this.predictions.addAll(processedPredictions);
		if (completeMissing) {
			for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
				if (!processed[iAtom]) {
					int symRank = symRanks[iAtom];
					int atomicNo = enhancedMolecule.getAtomicNo(iAtom);
					for (int iPrediction = 0; iPrediction < this.predictions
							.size(); iPrediction++) {
						PeakPrediction peakPrediction = this.predictions
								.get(iPrediction);
						if (peakPrediction.getNucleus().getAtomicNo() == atomicNo
								&& symRank == peakPrediction.getSymRank()) {
							processed[iAtom] = true;
							peakPrediction.addAtomID(iAtom);
						}
					}
				}
			}
		}
		for (int iPrediction = 0; iPrediction < this.predictions.size(); iPrediction++) {
			PeakPrediction currentPrediction = this.predictions.get(iPrediction); 
			resolveHeavyAtomIDs(this.moleculeDisplay, currentPrediction);
			currentPrediction.setIntegral(currentPrediction.getAtoms());
		}
	}

	/**
	 * Attempt to take the proton prediction and contract the ones that are
	 * connected to the same heteroatom
	 */
	public void contractProtonPredictions() {
		if (this.moleculeDisplay == null)
			return;
		Vector<PeakPrediction> nonHydrogen = new Vector<PeakPrediction>();
		Vector<PeakPrediction> hydrogenPredictions = new Vector<PeakPrediction>();
		for (int i = 0; i < this.predictions.size(); i++)
			if (this.predictions.get(i).getNucleus() == Nucleus.NUC_1H)
				hydrogenPredictions.add(this.predictions.get(i));
			else
				nonHydrogen.add(this.predictions.get(i));
		// nothing found
		if (hydrogenPredictions.size() == 0)
			return;
		for (int iUpper = hydrogenPredictions.size() - 1; iUpper >= 0; iUpper--) {
			PeakPrediction upperPrediction = hydrogenPredictions.get(iUpper);
			int upperHetero = upperPrediction.getHeavyAtomID(upperPrediction
					.getFirstAtomId());
			if (upperHetero == -1)
				continue;
			if (this.moleculeDisplay.getStereoInfo(upperHetero) == StereoInfo.HAS_DIFFERENT_PROTONS)
				continue;
			for (int iLower = iUpper - 1; iLower >= 0; iLower--) {
				PeakPrediction lowerPrediction = hydrogenPredictions
						.get(iLower);
				int lowerHetero = lowerPrediction
						.getHeavyAtomID(lowerPrediction.getFirstAtomId());
				if (upperHetero == lowerHetero) {
					lowerPrediction.join(upperPrediction);
					hydrogenPredictions.remove(iUpper);
					break;
				}
			}
		}
	}

	private boolean isDifferentPrediction(PredictionLabel label,
			PeakPrediction predX, PeakPrediction predY) {
		double xExisting = label.getXPos();
		double yExisting = label.getYPos();
		double x = predX.getCenter();
		double y = predY.getCenter();
		double rad = Math.sqrt(Math.pow(xExisting - x, 2)
				+ Math.pow(yExisting - y, 2));
		if (rad < 1E-3)
			return false;
		else
			return true;
	}

	private boolean isDifferentSimulation(PredictionLabel label,
			PeakPrediction predX, PeakPrediction predY) {
		double xExistingMin = label.getXMin();
		double xExistingMax = label.getXMax();
		double yExistingMin = label.getYMin();
		double yExistingMax = label.getYMax();
		double xMin = predX.getLowerLimit();
		double xMax = predX.getUpperLimit();
		double yMin = predY.getLowerLimit();
		double yMax = predY.getUpperLimit();

		double radMax = Math.sqrt(Math.pow(xExistingMax - xMax, 2)
				+ Math.pow(yExistingMax - yMax, 2));
		double radMin = Math.sqrt(Math.pow(xExistingMin - xMin, 2)
				+ Math.pow(yExistingMin - yMin, 2));
		if (radMax < 1E-3 && radMin < 1E-3)
			return false;
		else
			return true;
	}

	private double getMinAbscissa(Nucleus nucleus) {
		if (nucleus == Nucleus.NUC_1H) {
			return -4.0;
		} else if (nucleus == Nucleus.NUC_13C) {
			return -10.0;
		} else {
			return 0.0;
		}
	}

	private double getMaxAbscissa(Nucleus nucleus) {
		if (nucleus == Nucleus.NUC_1H) {
			return 16.0;
		} else if (nucleus == Nucleus.NUC_13C) {
			return 190.0;
		} else {
			return 0.0;
		}
	}

	private SpectraData createBlank2DSpectraData() {
		if (DEBUG)
			System.out.println("Creating blank 2D spectrum");
		SpectraData spectraData = new SpectraData();
		double firstX = this.getMinAbscissa(this.xNucleus);
		double lastX = this.getMaxAbscissa(this.yNucleus);
		spectraData
				.setParamDouble("firstY", this.getMaxAbscissa(this.yNucleus));
		spectraData.setParamDouble("lastY", this.getMinAbscissa(this.yNucleus));
		if (DEBUG) {
			System.out.println("firstX: " + firstX);
			System.out.println("lastX: " + lastX);
			System.out.println("firstY: " + this.getMaxAbscissa(yNucleus));
			System.out.println("lastY: " + this.getMinAbscissa(yNucleus));
		}
		for (int iSubSpectra = 0; iSubSpectra < PredictionData.nbPoints2D; iSubSpectra++) {
			spectraData.createNewSubSpectraData();
			spectraData.setDataType(SpectraData.TYPE_2DNMR_SPECTRUM);
			spectraData.setDataClass(SpectraData.DATACLASS_XY);
			spectraData.setFirstX(lastX);
			spectraData.setLastX(firstX);
			spectraData.setNbPoints(PredictionData.nbPoints2D);
			double[] yData = new double[PredictionData.nbPoints2D];
			Arrays.fill(yData, 500.0);
			spectraData.setSubSpectraDataY(iSubSpectra, yData);
		}
		return spectraData;

	}

	private void generateSignals() {
		// 0.00125 peak with at half height
		// final double gamma = 0.00125; // use a standard gamma for now
		for (PeakPrediction currentPrediction : this.predictions) {
			Nucleus nucleus = currentPrediction.getNucleus();
			if (nucleus == this.xNucleus || nucleus == this.yNucleus) {
				Multiplet multiplet = Multiplet.getInstance(currentPrediction,
						this.getMinAbscissa(nucleus), this
								.getMaxAbscissa(nucleus),
						this.simulationFrequency, PredictionData.nbPoints2D);

				// Lorentzian lorentzian = new Lorentzian();
				// lorentzian.setLocation(currentPrediction.getCenter());
				// lorentzian.setGamma(0.00125);
				// currentPrediction.setAnalyticSignal(lorentzian);
				currentPrediction.setAnalyticSignal(multiplet);
			}
		}
	}

	public void linkPredictionLabelsToAtoms(Spectra spectra) {
		if (this.moleculeDisplay == null)
			return;
		InteractiveSurface interactions = spectra.getInteractiveSurface();
		if (interactions == null)
			return;
		TreeMap<Integer, ActAtomEntity> atomEntities = this.moleculeDisplay
				.getAtomEntityMap();
		for (int ent = 0; ent < spectra.getEntitiesCount(); ent++) {
			if (spectra.getEntity(ent) instanceof PredictionLabel) {
				PredictionLabel label = (PredictionLabel) spectra
						.getEntity(ent);
				label.refreshSensitiveArea();
				label.checkSizeAndPosition();
				label.setHeterolink(false);
				Iterator<Integer> iterator = label.getAtomIDIterator();
				while (iterator.hasNext()) {
					Integer id = iterator.next();
					ActAtomEntity atomEntity = atomEntities.get(id);
					if (atomEntity != null) {
						interactions.createLink(label, atomEntity);
					}
				}
			}
		}
	}

	/**
	 * Creates virtual trace spectrum on the 2D
	 * @param twoDimNmrSpectrum
	 * @param isVertical
	 * @return
	 */
	
	public Spectra createTraceSpectrum(SpectraDisplay spectraDisplay, boolean isVertical) {
		Nucleus nucleus=Nucleus.UNDEF;
		double frequency=400;
		double firstX=Double.MIN_VALUE;
		double lastX=Double.MAX_VALUE;
		// we will go through all the 2D to find the external limits
		SpectraData spectraData;
		
		for (Spectra spectra : spectraDisplay.getAllSpectra()) {
			if (spectra.isDrawnAs2D()) {
				spectraData = spectra.getSpectraData();
				if (isVertical) {
					if (spectraData.getParamDouble("firstY", Double.MIN_VALUE) > firstX) firstX = spectraData.getParamDouble("firstY", Double.MIN_VALUE);
					if (spectraData.getParamDouble("lastY", Double.MAX_VALUE) < lastX) lastX = spectraData.getParamDouble("lastY", Double.MAX_VALUE);
					nucleus = spectraData.getNucleus(2);
					frequency = spectraData.getParamDouble("$SFO2", 400);
				} else {
					if (spectraData.getFirstX() > firstX) firstX = spectraData.getFirstX();
					if (spectraData.getLastX() < lastX) lastX = spectraData.getLastX();
					nucleus = spectraData.getNucleus(1);
					frequency = spectraData.getParamDouble("$SFO1", 400);
				}
			}
			// System.out.println("firstX: "+firstX+" - lastX: "+lastX);
		}
		
		
		
		/*
		SpectraData spectraData = twoDimNmrSpectrum.getSpectraData();
		if (isVertical) {
			firstX = spectraData.getParamDouble("firstY", 0);
			lastX = spectraData.getParamDouble("lastY", 0);
			nucleus = spectraData.getNucleus(2);
			frequency = spectraData.getParamDouble("$SFO2", 400);
		} else {
			firstX = spectraData.getFirstX();
			lastX = spectraData.getLastX();
			nucleus = spectraData.getNucleus(1);
			frequency = spectraData.getParamDouble("$SFO1", 400);
		}
		*/

		Spectra spectrum = createEmptySpectrum(nucleus, PredictionData.nbPointsProjected, frequency, firstX, lastX);
		
		
		spectrum.isVertical(isVertical);
		// add a prediction data object to allow reconstruction
		StringBuffer metadata = new StringBuffer();
		this.simulationFrequency = frequency;
		metadata.append("METADATA:type=").append(PredictionData.projectedTrace).append("\n");
		metadata.append("METADATA:firstX=").append(firstX).append("\n");
		metadata.append("METADATA:lastX=").append(lastX).append("\n");
		metadata.append("METADATA:nbPoints=").append(nbPoints).append("\n");
		metadata.append("METADATA:xNucleus=").append(nucleus.toString()).append("\n");
		this.setInputData(metadata.toString());
		spectrum.setPredictionData(this);
		return spectrum;
	}

	private Spectra createEmptySpectrum(Nucleus nucleus, int nbPoints, double frequency, double firstX, double lastX) {
		SpectraData spectraData = new SpectraData();
		spectraData.createNewSubSpectraData();
		spectraData.setActiveElement(0);
		spectraData.setDataType(SpectraData.TYPE_NMR_SPECTRUM);
		spectraData.setDataClass(SpectraData.DATACLASS_XY);
		spectraData.setXUnits("Hz");
		spectraData.setYUnits("ARBITRARY UNITS");
		spectraData.setNbPoints(nbPoints);
		spectraData.setFirstX(firstX);
		spectraData.setLastX(lastX);
		for (int i = 0; i < nbPoints; i++) {
			spectraData.addYPoint(0);
		}
		spectraData.setTitle(PredictionData.projectedTrace);
		spectraData.setSimulationDescriptor(PredictionData.projectedTrace);
		spectraData.putParam("observeFrequency", frequency);
		spectraData.putParam(".OBSERVE NUCLEUS", nucleus.toString());
		spectraData.prepareSpectraData();
		spectraData.updateDefaults();
		Spectra spectrum = new Spectra(spectraData);
		spectrum.setSpectraNb(0);
		return spectrum;
	}

	public SmartPeakLabel addFakeSmartPeak(Spectra spectrum, double position) {
		int iPos = spectrum.unitsToArrayPoint(position);
		spectrum.getSpectraData()
				.setYPoint(iPos, PredictionData.fakePeakHeight);
		this.fakePeaks.add(iPos);
		spectrum.getSpectraData().updateDefaults();
		double delta = spectrum.getNucleus().getDetectionBounds();
		SmartPeakLabel peakLabel = new SmartPeakLabel(position - delta,	position + delta);
		peakLabel.setPredicted(false);
		peakLabel.getNmrSignal1D().addPeak(position,spectrum.getSpectraData().getY(iPos));
		peakLabel.getNmrSignal1D().setNucleus(spectrum.getSpectraData().getNucleus());
		spectrum.addEntity(peakLabel);
		peakLabel.refreshSensitiveArea();
		peakLabel.checkSizeAndPosition();
		peakLabel.getNmrSignal1D().compute();
		spectrum.checkVerticalLimits();
		// spectrum.checkInteractiveSurface();
		spectrum.checkSizeAndPosition();
		return peakLabel;
	}

	public void removeFakeSmartPeak(Spectra spectrum, SmartPeakLabel peakLabel) {
		if (peakLabel.getNmrSignal1D().getNbPeaks() > 0) {
			int iPos = spectrum.unitsToArrayPoint(peakLabel.getNmrSignal1D().getPeak(0).getX());
			double[] yData = spectrum.getSpectraData().getSubSpectraDataY(0);
			yData[iPos] = 0;
			this.fakePeaks.remove(iPos);
		}
	}

	
	private void resolveHeavyAtomIDs(ActMoleculeDisplay molDisplay, PeakPrediction currentPrediction) {
		StereoMolecule molecule = molDisplay.getEnhancedMolecule();
		TreeMap<Integer, Integer> heavyAtomIDs=new TreeMap<Integer, Integer>();
		TreeSet<Integer> atomIDs=currentPrediction.getAtomIDs();
		for (Integer id : atomIDs) {
			int iHydrogen = id.intValue();
			if (molecule.getAtomicNo(iHydrogen) == 1
					&& molecule.getAllConnAtoms(iHydrogen) == 1) {
				heavyAtomIDs.put(id, molecule.getConnAtom(iHydrogen, 0));
			}
		}
		currentPrediction.setHeavyAtomIDs(heavyAtomIDs);
		
	}
}
