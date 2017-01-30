package org.cheminfo.hook.nemo;

import java.net.MalformedURLException;
import java.net.URL;

import org.cheminfo.hook.converter.Converter;

public class ProfileContourPlotting {
	public int nbLevels = 10;
	public int nIterations = 10;

	private SpectraData spectraData;
	private Spectra spectra;

	public ProfileContourPlotting(String stringUrl) {
		Converter tempConverter = Converter.getConverter("Jcamp");
		this.spectraData = new SpectraData();
		URL correctUrl = null;
		// we need the URL
		// we first try to convert it directly
		try {
			correctUrl = new URL(stringUrl);
		} catch (MalformedURLException e) {
			die("Not found : " + stringUrl);
		}
		if (tempConverter == null) {
			die("Converter -> null");
		}
		if (tempConverter.convert(correctUrl, spectraData)) {
			spectraData.setActiveElement(0);
		} else {
			die("Could Not Access File " + correctUrl);
		}
		System.out.println("Datatype: " + spectraData.getDataType());
		this.spectra = new Spectra(spectraData);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String stringUrl = "file://" + args[0];
		ProfileContourPlotting benchmark = new ProfileContourPlotting(stringUrl);
		long start, end;
		// nes
		start = System.currentTimeMillis();
		benchmark.benchmarkNew(stringUrl);
		end = System.currentTimeMillis();
		System.out.println("new: " + ((end-start)/1000.0) + " seconds");
		// old
		start = System.currentTimeMillis();
		benchmark.benchmarkOld(stringUrl);
		end = System.currentTimeMillis();
		System.out.println("old: " + ((end-start)/1000.0) + " seconds");
	}

	public void benchmarkNew(String stringUrl) {
		this.spectra.posContourLines.clear();
		this.spectra.negContourLines.clear();
		for (int iter = 0; iter < this.nIterations; iter++)
			this.spectra.generateContourLinesNew(this.nbLevels);
			
	}

	public void benchmarkOld(String stringUrl) {
		this.spectra.posContourLines.clear();
		this.spectra.negContourLines.clear();
		for (int iter = 0; iter < this.nIterations; iter++)
			this.spectra.generateContourLinesOld(this.nbLevels);
	}

	public static void die(String message) {
		System.err.println(message);
		System.exit(1);
	}

	public int getNbLevels() {
		return nbLevels;
	}

	public void setNbLevels(int nbLevels) {
		this.nbLevels = nbLevels;
	}

	public int getNIterations() {
		return nIterations;
	}

	public void setNIterations(int iterations) {
		nIterations = iterations;
	}

}
