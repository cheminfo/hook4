package com.actelion.research.epfl.tests;

import com.actelion.research.chem.*;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.chem.coords.CoordinateInventor;
import com.actelion.research.epfl.EPFLUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StereoMolecule molecule = new StereoMolecule();
		// atoms
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(9);
		// bonds
		molecule.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule.addBond(1, 4, Molecule.cBondTypeUp);
		CoordinateInventor inventor = new CoordinateInventor();
		inventor.invent(molecule);
		MolfileCreator creator = new MolfileCreator(molecule);
		String molFile = creator.getMolfile();
		//
		try {
			PrintWriter writer = new PrintWriter("/home/engeler/chiral.mol");
			writer.println(molFile);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Test.testDeuteriumSurvival();
		Test.testChirality();
		Test.testDValine();
	}

	private static void testDValine() {
		MolfileParser parser = new MolfileParser();
		StereoMolecule mol = new StereoMolecule();
		if (parser.parse(mol, new File(
				"/home/engeler/test_molecules/d_valine.mol"))) {
			Canonizer canonizer = new Canonizer(mol,
					Canonizer.CREATE_SYMMETRY_RANK);
			for (int iAtom = 0; iAtom < mol.getAllAtoms(); iAtom++) {
				System.out.println(iAtom + "atomicNo=" + mol.getAtomicNo(iAtom)
						+ "nH=" + mol.getAllHydrogens(iAtom) + "symRank="
						+ canonizer.getSymmetryRank(iAtom));
			}
			Test.replaceMethylGroups(mol);
		} else {
			System.out.println("");
		}
	}

	private static void replaceMethylGroups(StereoMolecule inputMolecule) {
		StereoMolecule mol1 = new StereoMolecule();
		StereoMolecule mol2 = new StereoMolecule();
		EPFLUtils.copyMolecule(inputMolecule, mol1, false);
		mol1.ensureHelperArrays(2);
		EPFLUtils.copyMolecule(inputMolecule, mol2, false);
		mol2.ensureHelperArrays(2);
		//
		int[] ids = new int[2];
		int stackPointer = 0;
		for (int i = 0; i < inputMolecule.getAllAtoms(); i++)
			if (inputMolecule.getAtomicNo(i) == 6
					&& inputMolecule.getAllHydrogens(i) == 3)
				ids[stackPointer++] = i;
		if (stackPointer != 2) {
			System.out.println("not enough methyl groups");
			System.exit(-1);
		}
		int iNew;
		int replacementAtom = 17;
		iNew = mol1.addAtom(replacementAtom);
		mol1.addBond(ids[0], iNew, Molecule.cBondTypeUp);
		 
		
		iNew = mol2.addAtom(replacementAtom);
		mol2.addBond(ids[1], iNew, Molecule.cBondTypeUp);

		Canonizer c1 = new Canonizer(mol1);
		Canonizer c2 = new Canonizer(mol2);
		String id1 = c1.getIDCode();
		String id2 = c2.getIDCode();
		if (id1.equals(id2)) {
			System.out.println("methyl groups are equal");
		} else {
			System.out.println("methyl groups are not equal");
		}
		Test.writeMolfile(mol1, "/home/engeler/isoleucine_1.mol");
		Test.writeMolfile(mol2, "/home/engeler/isoleucine_2.mol");
	}
	
	@SuppressWarnings("unused")
	private static void addBondUp(StereoMolecule mol, int iAtom) {
		int iNeighbour = mol.getConnAtom(iAtom, 0);
		for (int iBond = 0; iBond < mol.getAllBonds(); iBond++) {
			if ((mol.getBondAtom(0, iBond) == iAtom && mol
					.getBondAtom(1, iBond) == iNeighbour
			)
					|| (mol.getBondAtom(1, iBond) == iAtom && mol.getBondAtom(
							0, iBond) == iNeighbour
					)) {
				mol.setBondType(iBond, Molecule.cBondTypeUp);
			}
		}
	}

	private static void writeMolfile(StereoMolecule mol, String filename) {
		MolfileCreator creator = new MolfileCreator(mol);
		try {
			PrintWriter writer = new PrintWriter(filename);
			writer.println(creator.getMolfile());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * check whether deuterium atoms survive canonization
	 */
	private static void testDeuteriumSurvival() {
		StereoMolecule molecule = new StereoMolecule();
		// atoms
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		// bonds
		molecule.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule.addBond(2, 3, Molecule.cBondTypeSingle);
		HydrogenHandler.addImplicitHydrogens(molecule);
		for (int i = 0; i < molecule.getAllAtoms(); i++)
			if (molecule.getAtomicNo(i) == 1)
				molecule.setAtomicNo(i, 151);
		for (int i = 0; i < molecule.getAllAtoms(); i++)
			System.out.println(i + ": " + molecule.getAtomicNo(i) + " "
					+ molecule.getAtomMass(i));
		Canonizer canonizer = new Canonizer(molecule);
		String idString = canonizer.getIDCode();
		IDCodeParser parser = new IDCodeParser();
		StereoMolecule resultMolecule = parser.getCompactMolecule(idString);
		System.out.println("id:" + idString);
		System.out.println("nAtoms(beforeCan)=" + molecule.getAllAtoms());

		System.out.println("nAtoms(afterAfterCan)="
				+ resultMolecule.getAllAtoms());
		for (int i = 0; i < molecule.getAllAtoms(); i++)
			System.out.println(i + ": " + resultMolecule.getAtomicNo(i) + " "
					+ resultMolecule.getAtomMass(i));
	}

	public static void testChirality() {
		StereoMolecule molecule1 = new StereoMolecule();
		// atoms
		molecule1.addAtom(6);
		molecule1.addAtom(6);
		molecule1.addAtom(6);
		molecule1.addAtom(6);
		molecule1.addAtom(9);
		molecule1.addAtom(17);
		// bonds
		molecule1.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule1.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule1.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule1.addBond(1, 4, Molecule.cBondTypeUp);
		molecule1.addBond(2, 5, Molecule.cBondTypeUp);
		CoordinateInventor inventor = new CoordinateInventor();
		inventor.invent(molecule1);
		MolfileCreator creator = new MolfileCreator(molecule1);
		String molFile = creator.getMolfile();
		//
		try {
			PrintWriter writer = new PrintWriter("/home/engeler/chiral1.mol");
			writer.println(molFile);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Test.testDeuteriumSurvival();

		StereoMolecule molecule2 = new StereoMolecule();
		// atoms
		molecule2.addAtom(6);
		molecule2.addAtom(6);
		molecule2.addAtom(6);
		molecule2.addAtom(6);
		molecule2.addAtom(9);
		molecule2.addAtom(17);
		// bonds
		molecule2.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule2.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule2.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule2.addBond(1, 4, Molecule.cBondTypeUp);
		molecule2.addBond(2, 5, Molecule.cBondTypeDown);
		inventor = new CoordinateInventor();
		inventor.invent(molecule2);
		creator = new MolfileCreator(molecule2);
		molFile = creator.getMolfile();
		//
		try {
			PrintWriter writer = new PrintWriter("/home/engeler/chiral2.mol");
			writer.println(molFile);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Test.testDeuteriumSurvival();

	}
}
