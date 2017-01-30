package com.actelion.research.epfl.tests;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;

public class CanonizerTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StereoMolecule molecule = new StereoMolecule();
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(1);
		molecule.addAtom(9);
		molecule.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule.addBond(1, 4, Molecule.cBondTypeSingle);
		molecule.addBond(2, 5, Molecule.cBondTypeUp);
		System.out.println("in atoms "+molecule.getAllAtoms());
		Canonizer canonizer = new Canonizer(molecule);
		String idcode = canonizer.getIDCode();
		IDCodeParser parser = new IDCodeParser();
		int[] graphIndexes = canonizer.getGraphIndexes();
		for (int i = 0; i < graphIndexes.length; i++) {
			System.out.println("graphIndexes["+i+"]="+graphIndexes[i]);
		}
		StereoMolecule resultMolecule = parser.getCompactMolecule(idcode);
		System.out.println("result atoms "+resultMolecule.getAllAtoms());
	}

}
