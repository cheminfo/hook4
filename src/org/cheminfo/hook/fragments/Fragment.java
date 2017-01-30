package org.cheminfo.hook.fragments;

import java.util.Arrays;

import com.actelion.research.chem.Molecule;

/**
 * This class represent a bare bone fragment implemntation to be used together
 * with the fragment canonizer.
 * 
 * @author Marco Engeler
 * 
 */
public class Fragment {
	// constraints
	public static final int MAX_ATOMS = 1024;
	public static final int MAX_BONDS = 1024;

	// bond constants
	public static final int BOND_TYPE_SINGLE = 1;
	public static final int BOND_TYPE_DOUBLE = 2;
	public static final int BOND_TYPE_TRIPLE = 3;
	public static final int BOND_TYPE_AROMATIC = 4;
	public static final int BOND_TYPE_UP = 5;
	public static final int BOND_TYPE_DOWN = 6;
	public static final int BOND_TYPE_DOUBLE_E = 7;
	public static final int BOND_TYPE_DOUBLE_Z = 8;
	// atoms
	public static final int ATOM_PARITY_NONE = 0;
	public static final int ATOM_PARITY_R = 1;
	public static final int ATOM_PARITY_S = 2;

	// fragment properties
	private int nAtoms;
	private int nBonds;
	private int[] atomicNumber;
	private boolean[] isAromaticAtom;
	private int[] atomCharge;
	private int[] attachedHydrogens;
	private int[] bondAtoms;
	private int[] bondType;
	private int[] nBonds4Atom;
	private int[] stereoParity;
	private int[][] connAtoms;
	private int[] nConnAtoms;
	private boolean explicitHydrogens;
	private boolean noStereo;

	// maps atomic numbers to different types of symbols
	private String[] atomMap;

	public Fragment() {
		// atom properties
		nAtoms = 0;
		this.atomicNumber = new int[MAX_ATOMS];
		this.isAromaticAtom = new boolean[MAX_ATOMS];
		this.atomCharge = new int[MAX_ATOMS];
		this.attachedHydrogens = new int[MAX_ATOMS];
		this.nBonds4Atom = new int[MAX_ATOMS];
		this.stereoParity = new int[MAX_ATOMS];
		Arrays.fill(this.nBonds4Atom, 0);
		//
		this.nConnAtoms = new int[MAX_ATOMS];
		Arrays.fill(this.nConnAtoms, 0);
		this.connAtoms = new int[MAX_ATOMS][MAX_ATOMS];
		// bond properties
		nBonds = 0;
		this.bondAtoms = new int[MAX_BONDS * 2];
		this.bondType = new int[MAX_BONDS];
		this.explicitHydrogens = false;
		this.noStereo = false;
		//
		this.atomMap = AtomMaps.periodicTable;
	}

	public int addAtom(int atomicNumber, int charge, int nAttachedHydrogens,
			boolean isAromatic, int stereoParity) {
		this.atomicNumber[nAtoms] = atomicNumber;
		this.atomCharge[nAtoms] = charge;
		this.attachedHydrogens[nAtoms] = nAttachedHydrogens;
		this.isAromaticAtom[nAtoms] = isAromatic;
		if (this.noStereo) {
			this.stereoParity[nAtoms] = Fragment.ATOM_PARITY_NONE;
		} else {
			switch (stereoParity) {
			case Molecule.cAtomCIPParityRorM:
				this.stereoParity[nAtoms] = Fragment.ATOM_PARITY_R;
				break;
			case Molecule.cAtomCIPParitySorP:
				this.stereoParity[nAtoms] = Fragment.ATOM_PARITY_S;
				break;
			default:
				this.stereoParity[nAtoms] = Fragment.ATOM_PARITY_NONE;
				break;
			}
		}
		return nAtoms++;
	}

	public void reset() {
		this.nAtoms = 0;
		this.nBonds = 0;
		Arrays.fill(this.nBonds4Atom, 0);
	}

	public int getAtomicNo(int iAtom) {
		return this.atomicNumber[iAtom];
	}

	public boolean isAromaticAtom(int iAtom) {
		return this.isAromaticAtom[iAtom];
	}

	public int getNConnAtom(int iAtom) {
		return this.nConnAtoms[iAtom];
	}
	
	public int addBond(int iAtom1, int iAtom2, int bondType) {
		this.bondAtoms[nBonds * 2] = iAtom1;
		this.bondAtoms[nBonds * 2 + 1] = iAtom2;
		this.bondType[nBonds] = bondType;
		this.connAtoms[iAtom1][this.nConnAtoms[iAtom1]++] = iAtom2;
		this.connAtoms[iAtom2][this.nConnAtoms[iAtom2]++] = iAtom1;
		// use no stereo information
		if (this.noStereo) {
			switch (bondType) {
			// use a normal double bond
			case Fragment.BOND_TYPE_DOUBLE_E:
			case Fragment.BOND_TYPE_DOUBLE_Z:
				this.bondType[nBonds] = Fragment.BOND_TYPE_DOUBLE;
				break;
			// use single bonds
			case Fragment.BOND_TYPE_UP:
			case Fragment.BOND_TYPE_DOWN:
				this.bondType[nBonds] = Fragment.BOND_TYPE_SINGLE;
				break;
			}
		}
		this.nBonds4Atom[iAtom1]++;
		this.nBonds4Atom[iAtom2]++;
		return nBonds++;
	}

	public int getBondAtom(int iBond, int iAtom) {
		return this.bondAtoms[iBond * 2 + iAtom];
	}

	public int getBondType(int iBond) {
		return this.bondType[iBond];
	}

	public int getNAtoms() {
		return nAtoms;
	}

	public int getNBonds() {
		return nBonds;
	}

	public String[] getAtomMap() {
		return atomMap;
	}

	public void setAtomMap(String[] atomMap) {
		this.atomMap = atomMap;
	}

	public void getAdjecencyMatrix(int[] adjecencyMatrix) {
		int iAtom1, iAtom2;
		Arrays.fill(adjecencyMatrix, 0, this.nAtoms * this.nAtoms, 0);
		for (int iBond = 0; iBond < this.nBonds; iBond++) {
			iAtom1 = this.bondAtoms[iBond * 2];
			iAtom2 = this.bondAtoms[iBond * 2 + 1];
			adjecencyMatrix[iAtom1 * this.nAtoms + iAtom2] = 1;
			adjecencyMatrix[iAtom2 * this.nAtoms + iAtom1] = 1;
		}
	}

	public long getAtomicInvariant(int iAtom) {
		long result = 0;
		result = (result * 1000 + this.atomicNumber[iAtom]);
		result = (result * 100 + this.nBonds4Atom[iAtom]);
		if (this.atomCharge[iAtom] < 0) {
			result = result * 1000 + (100 + (-this.atomCharge[iAtom]));
		} else {
			result = result * 1000 + (this.atomCharge[iAtom]);
		}
		if (this.explicitHydrogens) {
			result = result * 100 + this.attachedHydrogens[iAtom];
		}
		// aromatic
		result *= 10;
		if (this.isAromaticAtom[iAtom])
			result += 1;
		// stereo chemistry
		if (!this.noStereo) {
			result *= 10;
			//
			switch (this.stereoParity[iAtom]) {
			case Fragment.ATOM_PARITY_R:
				result += 1;
				break;
			case Fragment.ATOM_PARITY_S:
				result += 2;
				break;
			}
		}
		return result;
	}

	public char getFastParsableAtomEntity(int iAtom) {
		// first 2 bits are used for hydrogen
		int hydrogens = 0;
		if (this.explicitHydrogens)
			hydrogens = (char) this.attachedHydrogens[iAtom] & 7;
		// use five bits for the charge
		int charge;
		if (this.atomCharge[iAtom] < 0)
			charge = (-1 * this.atomCharge[iAtom]) & (7) | 8;
		else
			charge = (this.atomCharge[iAtom]) & (7);
		charge = charge << 3;
		// another eight bits for the atomic number
		int atomicNo = this.atomicNumber[iAtom] << 8;
		// return the whole shebang
		return (char) (atomicNo | charge | hydrogens);
	}

	public char getFastParsableBondEntity(int iBond, int iNeighbour) {
		// use the first byte to encode the bond type
		// and the second to give the neighbours address
		return ((char) ((this.bondType[iBond] << 8) | iNeighbour | 32768));
	}

	public String getSmilesAtomEntity(int iAtom) {
		String result = Molecule.cAtomLabel[this.atomicNumber[iAtom]];
		if (this.isAromaticAtom[iAtom]) {
			result = result.toLowerCase();
		}
		if (this.explicitHydrogens && this.attachedHydrogens[iAtom] > 0) {
			result = "[" + result + "H" + this.attachedHydrogens[iAtom] + "]";
		}
		return result;
	}

	public String getSmilesBondEntity(int iBond) {
		switch (this.bondType[iBond]) {
		case Fragment.BOND_TYPE_DOUBLE:
			return "=";
		case Fragment.BOND_TYPE_TRIPLE:
			return "#";
		default:
			return "";
		}
	}

	public String getSimpleBondEntity(int iBondType) {
		switch (iBondType) {
		// these are all judged as single bonds
		case Fragment.BOND_TYPE_SINGLE:
		case Fragment.BOND_TYPE_UP:
		case Fragment.BOND_TYPE_DOWN:
			return "s";
		case Fragment.BOND_TYPE_DOUBLE:
			return "d";
		case Fragment.BOND_TYPE_TRIPLE:
			return "t";
		case Fragment.BOND_TYPE_AROMATIC:
			return "a";
		case Fragment.BOND_TYPE_DOUBLE_E:
			return "e";
		case Fragment.BOND_TYPE_DOUBLE_Z:
			return "z";
		default:
			return "";
		}
	}

	public String getSimpleAtomEntity(int iAtom) {
		String result;
		String label = null;
		if (this.isAromaticAtom[iAtom])
			label = this.atomMap[this.atomicNumber[iAtom]].toLowerCase();
		else
			label = this.atomMap[this.atomicNumber[iAtom]];

		if (!this.noStereo
				&& this.stereoParity[iAtom] != Fragment.ATOM_PARITY_NONE) {
			switch (this.stereoParity[iAtom]) {
			case Fragment.ATOM_PARITY_R:
				label += "(r)";
				break;
			case Fragment.ATOM_PARITY_S:
				label += "(s)";
				break;
			}

		}
		if (this.explicitHydrogens && this.attachedHydrogens[iAtom] > 0) {
			result = "[" + label + "H" + this.attachedHydrogens[iAtom] + "]";
		} else if (this.atomCharge[iAtom] != 0) {
			result = "[" + label + "," + this.atomCharge[iAtom] + "]";
		} else {
			result = "["+label+"]";
		}
		return result;

	}

	public boolean isExplicitHydrogens() {
		return explicitHydrogens;
	}

	public void setExplicitHydrogens(boolean explicitHydrogens) {
		this.explicitHydrogens = explicitHydrogens;
	}

	public boolean isNoStereo() {
		return noStereo;
	}

	public void setNoStereo(boolean noStereo) {
		this.noStereo = noStereo;
	}
	
	public int getNbStereoCenters() {
		int nStereo = 0;
		for (int i = 0; i < this.nAtoms; i++) {
			if (this.stereoParity[i] != Fragment.ATOM_PARITY_NONE)
				nStereo++;
		}
		return nStereo;
	}
	
	
}
