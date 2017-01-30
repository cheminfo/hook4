package org.cheminfo.hook.graph.HOSE;

import java.io.File;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.fragments.Fragment;
import org.cheminfo.hook.fragments.FragmentCanonizer;

import com.actelion.research.chem.ExtendedMolecule;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

/**
 * This class is used to generate spherical fragments similar to hose codes
 * 
 * @deprecated
 */
public class SphereDescriptorGenerator {
	private ExtendedMolecule molecule = null;
	private FragmentCanonizer canonizer;
	private Fragment fragment;

	// helper arrays
	int[] sphereCounts = null;
	int[] atomStack = null;
	int[] atomMap = null;
	boolean[] discovered = null;
	
	public SphereDescriptorGenerator() {
		super();
		init();
	}

	public SphereDescriptorGenerator(ExtendedMolecule molecule) {
		super();
		init();
		this.setMolecule(molecule);
	}

	public void init() {
		canonizer = new FragmentCanonizer();
		fragment = new Fragment();
		canonizer.setFragment(fragment);
//		canonizer.setFirstAtomPrioritized(true);
	}

	public ExtendedMolecule getMolecule() {
		return molecule;
	}

	public void setMolecule(ExtendedMolecule molecule) {
		this.molecule = molecule;
		if (this.molecule != null)
			prepareGenerator();
	}

	private void prepareGenerator() {
		this.molecule.ensureHelperArrays(1);
		// update aromaticity
		
		// prepare arrays
		int nAtoms = this.molecule.getAllAtoms();
		this.atomStack = new int[nAtoms];
		this.sphereCounts = new int[nAtoms];
		this.atomMap = new int[nAtoms];
		this.discovered = new boolean[nAtoms];
	}
	public Vector<String> getAllSphereIdCodes(int nSpheres) {
		if (this.molecule == null)
			return null;
		Vector<String> idCodes = new Vector<String>();
		for (int iAtom = 0; iAtom < this.molecule.getAllAtoms(); iAtom++)
			idCodes.add(this.getSphereIdCode(iAtom, nSpheres));
		return idCodes;
	}

	public TreeSet<String> getAllSphereIdCodesByAtomicNo(int atomicNo,
			int nSpheres) {
		if (this.molecule == null)
			return null;
		TreeSet<String> idCodes = new TreeSet<String>();
		for (int iAtom = 0; iAtom < this.molecule.getAllAtoms(); iAtom++)
			if (this.molecule.getAtomicNo(iAtom) == atomicNo)
				idCodes.add(this.getSphereIdCode(iAtom, nSpheres));
		return idCodes;
	}

	public String getSphereIdCode(int iCentralAtom, int nSpheres) {
		int nAtoms = this.molecule.getAllAtoms();
		if (iCentralAtom >= nAtoms)
			return null;
		// BFS starting at iCentralAtom
		Arrays.fill(this.discovered, false);
		int basePointer = 0;
		int topPointer = 1;
		atomStack[basePointer] = iCentralAtom;
		sphereCounts[iCentralAtom] = 1;
		// do dfs search
		while (basePointer < topPointer) {
			int parentAtom = atomStack[basePointer++];
			for (int iNeighbour = 0; iNeighbour < this.molecule.getAllConnAtoms(parentAtom); iNeighbour++) {
				int iNeighbourAtom = this.molecule.getConnAtom(parentAtom,
						iNeighbour);
				if (!discovered[iNeighbourAtom]) {
					discovered[iNeighbourAtom] = true;
					if (sphereCounts[parentAtom] < nSpheres - 1) {
						atomStack[topPointer++] = iNeighbourAtom;
						sphereCounts[iNeighbourAtom] = sphereCounts[parentAtom] + 1;
					}
				}
			}
		}
		// generate fragment
		// add atoms
		int[] reverseIdMap = sphereCounts;
		Arrays.fill(reverseIdMap, -1);
		topPointer = 0;
		this.fragment.reset();
		atomMap[topPointer++] = iCentralAtom;
		reverseIdMap[iCentralAtom] = this.fragment.addAtom(
				this.molecule.getAtomicNo(iCentralAtom),
				this.molecule.getAtomCharge(iCentralAtom),
				this.molecule.getAllHydrogens(iCentralAtom),
				this.molecule.isAromaticAtom(iCentralAtom),
				this.molecule.getAtomCIPParity(iCentralAtom));
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			if (discovered[iAtom] && iAtom != iCentralAtom) {
				atomMap[topPointer++] = iAtom;
				reverseIdMap[iAtom] = this.fragment
						.addAtom(	this.molecule.getAtomicNo(iAtom),
									this.molecule.getAtomCharge(iAtom),
									this.molecule.getAllHydrogens(iAtom),
									this.molecule.isAromaticAtom(iAtom),
									this.molecule.getAtomCIPParity(iAtom));
			}
		}
		// add bonds
		for (int iBond = 0; iBond < this.molecule.getAllBonds(); iBond++) {
			if (discovered[this.molecule.getBondAtom(0, iBond)]
					&& discovered[this.molecule.getBondAtom(1, iBond)]) {
				int iAtom1 = reverseIdMap[this.molecule.getBondAtom(0, iBond)];
				int iAtom2 = reverseIdMap[this.molecule.getBondAtom(1, iBond)];
				int bondType = this.molecule.getBondType(iBond);
				if (this.molecule.isAromaticBond(iBond))
					bondType = 4;
				this.fragment.addBond(iAtom1, iAtom2, bondType);
			}
		}
		return this.canonizer.getIdString();
	}

	public static void main(String[] argv) {
		String filename = "/home/engeler/source/molFiles/hose.mol";
		StereoMolecule molecule = new StereoMolecule();
		MolfileParser parser = new MolfileParser();
		parser.parse(molecule, new File(filename));
		SphereDescriptorGenerator generator = new SphereDescriptorGenerator(
				molecule);
		int iOxygenAtom = 10;
		String idCode = generator.getSphereIdCode(iOxygenAtom, 4);
		// output
		System.out.println(idCode);

	}
}
