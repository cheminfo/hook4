package org.cheminfo.hook.nemo.nmr.prediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.Vector;

import org.cheminfo.hook.nemo.nmr.Coupling;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.PeakPrediction;

/**
 * 
 * This program rewrites the prediction that comes out of getNmrSpectrum.pl
 * in order to generate a prediction file that can be added
 * 
 * @author engeler
 *
 */
public class PredictionRewriter {
	
	
	private File directory = null;
	private String type = null;
	private IDRemapper remapper = null;
	private TreeMap<Integer,Integer> idMap = null;
	private Vector<PeakPrediction> predictions = null;
	
	public PredictionRewriter(File directory, String type) {
		this.directory = directory;
		this.type = type;
	}

	private void check() {
		if (!this.directory.exists()) {
			System.out.println("Directory does not exist "
					+ this.directory.getName());
			System.exit(-1);
		}
		if (!this.directory.isDirectory()) {
			System.out.println("Not a directory " + this.directory.getName());
			System.exit(-1);
		}
		File testFile = new File(this.directory.getAbsolutePath() + "/eval/" + this.type);
		if (!testFile.exists()) {
			System.out.println("No evaluation data for type " + this.type);
		}
	}

	private void remap() {
		this.remapper = new IDRemapper(new File(this.directory.getAbsolutePath()+ "/00000.mol"));
		this.remapper.remap();
		this.idMap = this.remapper.getIDMap();
	}

	public void rewrite() {
		this.check();
		this.remap();
		
		File evalDir = new File(this.directory.getAbsoluteFile()+"/eval");
		String[] files = evalDir.list();
		for (int iFile = 0; iFile < files.length; iFile++) {
			if (files[iFile].matches(this.type+".*\\.in")) {
				this.rewritePrediction(evalDir.getAbsolutePath()+"/"+files[iFile]);
				
			}
		}
	}

	public void rewritePrediction(String strFile) {
		File inputFile = new File(strFile);
		this.predictions = new Vector<PeakPrediction>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.trim().split("\t");
				PeakPrediction currentPrediction = new PeakPrediction();
				currentPrediction.addAtomID(this.idMap.get(Integer.parseInt(tokens[0].trim())));
				currentPrediction.setChemicalShift(Double.parseDouble(tokens[2].trim()));
				currentPrediction.setNucleus(Nucleus.determineNucleus(tokens[1].trim()));
				String pattern = "";
				if (tokens.length > 3) {
					int nCouplings = (tokens.length - 3) / 2;
					int ii = 3;
					for (int iCoupling = 0; iCoupling < nCouplings; iCoupling++) {
						pattern += "d";
						int id = this.idMap.get(Integer.parseInt(tokens[ii].trim()));
						double j = Double.parseDouble(tokens[ii+1].trim());
						Coupling currentCoupling = new Coupling();
						currentCoupling.setMultiplicity("d");
						currentCoupling.setCouplingConstant(j);
						currentPrediction.addCoupling(currentCoupling);
						ii+=2;
					}
				} else {
					pattern = "s";
				}
				currentPrediction.setPeakPattern(pattern);
				currentPrediction.setIntegral(1);
				this.predictions.add(currentPrediction);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.contractPredictions();
		this.write(strFile.replaceAll("\\.in$", ".dat"));
	}

	private void contractPredictions() {
		// If the protons have the same symmetry we should combine them ...
		
		/*
		public static String getSymmetryRankDia(String molfile) {
		this.remapper = new IDRemapper(new File(this.directory.getAbsolutePath()+ "/00000.mol"));
		
	//	PeakPrediction.average(peakPredictions)
		*/
		
	}
	
	private void write(String file) {
		try {
			PrintWriter printWriter = new PrintWriter(file);
			for (PeakPrediction p: this.predictions) {
				if (p.getNucleus() != Nucleus.UNDEF) {
					printWriter.println(p.getPredictionLine());
				}
			}
			printWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * id		nuc		d1	d2				pattern		#H	mult J
	 * 12      1H      2.27 +- 0.17            s       3
	 * 
	 * 
	 */
	
	public static void main(String[] argv) {
		if (argv.length != 2) {
			System.out.println("Usage: cmd <dir> <type>");
			System.exit(-1);
		}
		PredictionRewriter rewriter = new PredictionRewriter(new File(argv[0]), argv[1]);
		rewriter.rewrite();
	}
}
