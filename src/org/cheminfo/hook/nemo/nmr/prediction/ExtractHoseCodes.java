package org.cheminfo.hook.nemo.nmr.prediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.nmr.HoseGeneratorInterface;

/**
 * 
 * 
 * @author engeler
 * 
 */
public class ExtractHoseCodes {
	public static void main(String[] argv) {
		if (argv.length != 2) {
			System.out.println("Usage: <campaign directory> <type>");
			System.exit(0);
		}
		File directory = new File(argv[0]);
		if (!directory.isDirectory()) {
			System.out.println("not a directory [" + argv[0] + "]");
			System.exit(0);
		}
		String type = argv[1];
		//
		File evalDir = new File(directory.getAbsolutePath() + "/eval");
		if (!evalDir.isDirectory()) {
			System.out.println("eval directory does not exist");
			System.exit(0);
		}
		File molFile = new File(directory.getAbsolutePath() + "/00000.mol");
		if (!molFile.exists()) {
			System.out.println("mol file does not exist["
					+ molFile.getAbsolutePath() + "]");
			System.exit(0);
		}
		File predictionFile = new File(directory.getAbsolutePath() + "/eval/"
				+ type + ".dat");
		if (!predictionFile.exists()) {
			System.out.println("prediction file does not exist["
					+ predictionFile.getAbsolutePath() + "]");
			System.exit(0);
		}
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();
		StringBuffer molFileBuffer = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(molFile));
			String line;
			while ((line = reader.readLine()) != null) {
				molFileBuffer.append(line);
				molFileBuffer.append('\n');
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		molDisplay.addMolfile(molFileBuffer.toString(), false);
		System.out.println(molDisplay.getMoleculeIDCode());
		System.out.println(molDisplay.getMoleculeIDCode());
		// read prediction
		TreeMap<Integer, Double> predictions = new TreeMap<Integer, Double>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					predictionFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\t");
				predictions.put(Integer.parseInt(tokens[0].trim()), Double
						.parseDouble(tokens[2].trim()));
				System.out.println(tokens[0]+"\t"+tokens[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HoseGeneratorInterface hgi = new HoseGeneratorInterface(molDisplay);
		Set<Integer> idSet = predictions.keySet();
		Iterator<Integer> iterator = idSet.iterator();
		while (iterator.hasNext()) {
			int id = iterator.next();
			System.out.print(id + "\t" + predictions.get(id));
			String[] codes = hgi.getHoseCodes4Atom(id);
			if (codes != null)
				for (int i = 0; i < codes.length; i++)
					System.out.print("\t" + codes[i]);
			System.out.println();
		}
	}
}
