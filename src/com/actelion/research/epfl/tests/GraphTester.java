package com.actelion.research.epfl.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.cheminfo.hook.graph.GraphAlgorithms;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.EPFLUtils;

public class GraphTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StereoMolecule stereoMolecule = getStereoMolecule("/home/engeler/source/jcamp/UCB-references/budesonide/budesonide.mol");
		analyzeStereoMolecule(stereoMolecule);
	}

	public static void analyzeStereoMolecule(StereoMolecule stereoMolecule) {
		int nAtoms = stereoMolecule.getAllAtoms();
		System.out.println("number of atoms: " + nAtoms);
		
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			System.out.println("iAtom="+iAtom);
			System.out.println("atom number: " + stereoMolecule.getAtomicNo(iAtom));
		}
		int nBonds = stereoMolecule.getAllBonds();
		System.out.println("number of bonds: " + nBonds);
		int[] adjecencyMatrix = EPFLUtils.getAdjecencyMatrix(stereoMolecule);
		int iAtom1, iAtom2;
		for (int iBond = 0; iBond < nBonds; iBond++) {
			iAtom1 = stereoMolecule.getBondAtom(0, iBond);
			iAtom2 = stereoMolecule.getBondAtom(1, iBond);
			System.out.println(iAtom1 + " -> " + iAtom2);
		}
		int[] result = GraphAlgorithms.FloydWarshall(adjecencyMatrix, nAtoms);
		for (iAtom1 = 0; iAtom1 < nAtoms; iAtom1++) {
			System.out.print("[");
			for (iAtom2 = 0; iAtom2 < nAtoms; iAtom2++) {
				if (result[iAtom1 * nAtoms + iAtom2] != Integer.MAX_VALUE) {
					//System.out.println(iAtom1 + " -> " + iAtom2 + " = " + result[iAtom1 * nAtoms + iAtom2]);
					System.out.print(" " + result[iAtom1 * nAtoms + iAtom2] + " ");
				} else {
					System.out.print(" n/a ");
				}
			}
			System.out.println("]");
		}
	}

	public static StereoMolecule getStereoMolecule(String filename) {
		StereoMolecule stereoMolecule = new StereoMolecule();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String molfile = "";
			String line;
			while ((line = in.readLine()) != null) {
				molfile = molfile + line + "\n";
			}
			System.out.println(molfile);
			in.close();
			com.actelion.research.chem.MolfileParser mfParser = new com.actelion.research.chem.MolfileParser();
			mfParser.parse(stereoMolecule, molfile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			stereoMolecule = null;
		} catch (IOException e) {
			e.printStackTrace();
			stereoMolecule = null;
		}
		return stereoMolecule;
	}

}
