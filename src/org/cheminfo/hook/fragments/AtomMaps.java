package org.cheminfo.hook.fragments;

import java.util.TreeMap;

public class AtomMaps {
	// taken from com.actelion.reasearch.chem.Molecule
	public static String[] periodicTable = { "?", "H", "He", "Li", "Be", "B",
			"C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl",
			"Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni",
			"Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y",
			"Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn",
			"Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm",
			"Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf",
			"Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi",
			"Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu",
			"Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "??", "??",
			"??", "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
			"??", "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
			"??", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12",
			"R13", "R14", "R15", "R16", "R1", "R2", "R3", "A", "A1", "A2",
			"A3", "??", "??", "D", "T", "X", "R", "H2", "H+", "Nnn", "HYD",
			"Pol", "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
			"??", "Ala", "Arg", "Asn", "Asp", "Cys", "Gln", "Glu", "Gly",
			"His", "Ile", "Leu", "Lys", "Met", "Phe", "Pro", "Ser", "Thr",
			"Trp", "Tyr", "Val" };

	public static void main(String[] argv) {
		for (int i = 0; i < periodicTable.length; i++)
			System.out.println(i + ": " + periodicTable[i]);
	}

	public static int remapHalogens(int atomicNumber) {
		switch (atomicNumber) {
		case 9: // fluorine
		case 17: // chlorine
		case 35: // bromine
		case 53: // iodine
		case 85: // astatine
			return 153;
		default:			return atomicNumber;
		}
	}

	public static TreeMap<String, Integer> getAtomicNoLookupTable(String[] table) {
		TreeMap<String, Integer> lookupTable = new TreeMap<String, Integer>();

		for (int iAtom = 0; iAtom < table.length; iAtom++) {
			if (!lookupTable.containsKey(table[iAtom])) {
				lookupTable.put(table[iAtom], new Integer(iAtom));
			}
		}
		return lookupTable;
	}

}
