package org.cheminfo.hook.fragments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

public class BulkFragmentGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		actelionTest(10000);
		epflTest(10000);
	}
	
	
	public static void actelionTest(int nMaxMolecules) {
		System.out.println("actelion test");
		String url = "file:///home/engeler/source/molecules.tab";
		URL fileUrl;
		try {
			fileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		int nMolecules = 0;
		String[] idCodes = new String[0];
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					fileUrl.openStream()));
			String inputLine;
			int iLine = 0;
			LinkedList<String> ids = new LinkedList<String>();
			while ((inputLine = in.readLine()) != null && iLine < nMaxMolecules) {
				String[] tokens = inputLine.split("\t");
				ids.add(tokens[2]);
				iLine++;
			}
			in.close();
			nMolecules = ids.size();
			idCodes = ids.toArray(idCodes);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		long start = System.currentTimeMillis();
		IDCodeParser idParser = new IDCodeParser();
		FragmentGeneratorActelionId generator = new FragmentGeneratorActelionId();
		generator.setMinAtoms(3);
		generator.setMaxAtoms(6);
		TreeSet<String> fragmentBasis = new TreeSet<String>();
		TreeMap<String, Occurrence> fragments = null;
		int[] fragmentsPerMolecule = new int[nMolecules];
		StereoMolecule inputMolecule;
		double average = 0.0;
		double variance = 0.0;
		for (int iMolecule = 0; iMolecule < nMolecules; iMolecule++) {
			// generate
			inputMolecule = new StereoMolecule();
			idParser.parse(inputMolecule, idCodes[iMolecule]);
			// generate fragments
			generator.setMolecule(inputMolecule);
			fragments = generator.generate();
			fragmentsPerMolecule[iMolecule] = fragments.size();
			Set<String> keys = fragments.keySet();
			Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {
				fragmentBasis.add(iterator.next());
			}
			average += fragmentsPerMolecule[iMolecule];
			variance +=  fragmentsPerMolecule[iMolecule] * fragmentsPerMolecule[iMolecule];
		}
		average /= nMolecules;
		variance/= nMolecules;
		variance -= average*average;
		long end = System.currentTimeMillis();
		System.out.println("number of fragments: " + fragmentBasis.size());
		System.out.println("average: " + average);
		System.out.println("stddev: " + Math.sqrt(variance));
		System.out.println("seconds elapsed:" + (end-start)/1000.0);
	}
	
	public static void epflTest(int nMaxMolecules) {
		System.out.println("epfl test");
		String url = "file:///home/engeler/source/molecules.tab";
		URL fileUrl;
		try {
			fileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		int nMolecules = 0;
		String[] idCodes = new String[0];
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					fileUrl.openStream()));
			String inputLine;
			int iLine = 0;
			LinkedList<String> ids = new LinkedList<String>();
			while ((inputLine = in.readLine()) != null && iLine < nMaxMolecules) {
				String[] tokens = inputLine.split("\t");
				ids.add(tokens[2]);
				iLine++;
			}
			in.close();
			nMolecules = ids.size();
			idCodes = ids.toArray(idCodes);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		long start = System.currentTimeMillis();
		IDCodeParser idParser = new IDCodeParser();
		FragmentGenerator generator = new FragmentGenerator();
		generator.setMinAtoms(3);
		generator.setMaxAtoms(6);
		TreeSet<String> fragmentBasis = new TreeSet<String>();
		TreeMap<String, Occurrence> fragments = null;
		int[] fragmentsPerMolecule = new int[nMolecules];
		StereoMolecule inputMolecule;
		double average = 0.0;
		double variance = 0.0;
		for (int iMolecule = 0; iMolecule < nMolecules; iMolecule++) {
			// generate
			inputMolecule = new StereoMolecule();
			idParser.parse(inputMolecule, idCodes[iMolecule]);
			// generate fragments
			generator.setMolecule(inputMolecule);
			fragments = generator.generate();
			fragmentsPerMolecule[iMolecule] = fragments.size();
			Set<String> keys = fragments.keySet();
			Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {
				fragmentBasis.add(iterator.next());
			}
			average += fragmentsPerMolecule[iMolecule];
			variance +=  fragmentsPerMolecule[iMolecule] * fragmentsPerMolecule[iMolecule];
		}
		average /= nMolecules;
		variance/= nMolecules;
		variance -= average*average;
		long end = System.currentTimeMillis();
		System.out.println("number of fragments: " + fragmentBasis.size());
		System.out.println("average: " + average);
		System.out.println("stddev: " + Math.sqrt(variance));
		System.out.println("seconds elapsed:" + (end-start)/1000.0);
	}
}
