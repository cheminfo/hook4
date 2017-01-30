package org.cheminfo.hook.fragments;

import com.actelion.research.chem.coords.CoordinateInventor;
import com.actelion.research.chem.StereoMolecule;

public class EPFLIDCodeParser {
	public static StereoMolecule parseIdCode(String idCode) {
		StereoMolecule molecule = new StereoMolecule();
		molecule.setFragment(true);

		byte[] byteArray = idCode.getBytes();
		int nAtoms = (int) byteArray[0];
		int stackPointer = 1;
		int charge, atomicNumber;
		int nBonds;
		// extract atoms
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			atomicNumber = byteArray[stackPointer++];
			molecule.addAtom(atomicNumber);
			charge = (byteArray[stackPointer++] >> 3);
			
			if (charge > 8) {
				molecule.setAtomCharge(iAtom, -1 * (charge & 7));
			} else {
				molecule.setAtomCharge(iAtom, charge);
			}
			nBonds = byteArray[stackPointer++];
			stackPointer += 2 * nBonds;
		}
		// extract bonds
		stackPointer = 1;
		int iNeighbourAtom, iBondType;
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			stackPointer+=2;
			nBonds = byteArray[stackPointer++];
			for (int iBond = 0; iBond < nBonds; iBond++) {
				iBondType = byteArray[stackPointer++];
				iNeighbourAtom = byteArray[stackPointer++];
				molecule.addBond(iAtom, iNeighbourAtom, iBondType);
			}
		}	
		// our id code has no coordinates whatsoever
		CoordinateInventor inventor = new CoordinateInventor();
		inventor.invent(molecule);
		return molecule;
	}

	public static int extractNumberOfAtoms(String idCode) {
		byte[] byteArray = idCode.getBytes();
		return (int) byteArray[0];
	}
	
}
