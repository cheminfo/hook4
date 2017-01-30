package org.cheminfo.hook.nemo;

public class PeakData {
	private double chemicalShift;
	private double nHydrogens;
	private int atomId;
	private SmartPeakLabel peakLabel;
	
	public PeakData() {
	}

	public double getChemicalShift() {
		return chemicalShift;
	}

	public void setChemicalShift(double chemicalShift) {
		this.chemicalShift = chemicalShift;
	}

	public double getNHydrogens() {
		return nHydrogens;
	}

	public void setNHydrogens(double hydrogens) {
		nHydrogens = hydrogens;
	}

	public int getAtomId() {
		return atomId;
	}

	public void setAtomId(int atomId) {
		this.atomId = atomId;
	}

	public SmartPeakLabel getPeakLabel() {
		return peakLabel;
	}

	public void setPeakLabel(SmartPeakLabel peakLabel) {
		this.peakLabel = peakLabel;
	}
	
	
}
