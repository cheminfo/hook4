package org.cheminfo.hook.graph.HOSE;

import com.actelion.research.chem.AromaticityResolver;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.EPFLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * 
 * This class generates something similar to a hose code. The encoding differs
 * however from W. Bremser, Anal Chimica Acta, 103 (1978) 355-365. Note
 * Bremser's paper is also inconsistent with the list given in his book
 * "Chemical shift ranges in carbon-13 NMR spectroscopy".
 * 
 */
/** @deprecated */
public class HoseCodeGenerator {
	private static final boolean isDebug = false;

	StereoMolecule molecule = null;

	public HoseCodeGenerator(StereoMolecule molecule) {
		super();
		this.molecule = molecule;
		if (this.molecule != null)
			this.updateAromaticity();
	}

	public HoseCodeGenerator() {
		super();
	}

	public StereoMolecule getMolecule() {
		return molecule;
	}

	public void setMolecule(StereoMolecule molecule) {
		this.molecule = molecule;
		if (this.molecule != null)
			this.updateAromaticity();

	}

	private void updateAromaticity() {
	//	boolean[] isAromaticAtom = new boolean[this.molecule.getAllAtoms()];
		boolean[] isAromaticBond = new boolean[this.molecule.getAllBonds()];
	//	Arrays.fill(isAromaticAtom, false);
		Arrays.fill(isAromaticBond, false);
		AromaticityResolver resolver = new AromaticityResolver(this.molecule);
		resolver.locateDelocalizedDoubleBonds(isAromaticBond);
	}

	public String getHoseCode(int atomId, int maxSpheres) {
		if (this.molecule == null)
			return null;
		this.molecule.isAromaticAtom(atomId);
		String hoseCode = "";
		int nAtoms = this.molecule.getAllAtoms();
		if (nAtoms <= atomId)
			return null;
		int nBonds = this.molecule.getAllBonds();
		int[] matrix = EPFLUtils.getGeneralizedDistanceMatrix(molecule);
		// debug stuff
		if (HoseCodeGenerator.isDebug) {
			System.out.println("nAtoms=" + nAtoms);
			System.out.println("nBonds=" + nBonds);
			System.out.println("Generalized distance matrix");
			for (int i = 0; i < nAtoms; i++) {
				System.out.print("[");
				for (int j = 0; j < nAtoms; j++) {
					System.out.print(matrix[i * nAtoms + j] + " ");
				}
				System.out.println("]");
			}
		}

		// walk through the molecule DFS
		boolean[] discovered = new boolean[nAtoms];
		Arrays.fill(discovered, false);
		LinkedList<HOSEAtomNode> atomQueue = new LinkedList<HOSEAtomNode>();
		HOSEAtomNode rootNode = new HOSEAtomNode();
		rootNode.setAtomId(atomId);
		rootNode.setAtomicNumber(this.molecule.getAtomicNo(atomId));
		rootNode.setCharge(this.molecule.getAtomCharge(atomId));
		rootNode.setSphere(0);
		discovered[atomId] = true;
		atomQueue.add(rootNode);
		HOSEAtomNode parentAtom;
		int nextLevel;
		int currentId, parentId;
		while (!atomQueue.isEmpty()) {
			parentAtom = atomQueue.removeFirst();
			currentId = parentAtom.getAtomId();
			parentId = parentAtom.getParentID();
			nextLevel = parentAtom.getSphere() + 1;
			for (int iNeighbour = 0; iNeighbour < this.molecule
					.getConnAtoms(currentId); iNeighbour++) {
				int nextId = this.molecule.getConnAtom(currentId, iNeighbour);
				if (nextId == parentId) // skip the parent node
					continue;
				if (discovered[nextId]) {
					// we have a ring closure
					HOSEAtomNode nextAtom = new HOSEAtomNode();
					nextAtom.setParentID(currentId);
					nextAtom.setRingClosure(true);
					parentAtom.addNode(nextAtom);
				} else {
					discovered[nextId] = true;
					HOSEAtomNode nextAtom = new HOSEAtomNode();
					nextAtom.setParentID(currentId);
					nextAtom.setAtomId(nextId);
					nextAtom.setAtomicNumber(this.molecule.getAtomicNo(nextId));
					nextAtom.setRingClosure(false);
					nextAtom.setSphere(nextLevel);
					nextAtom.setCharge(this.molecule.getAtomCharge(nextId));
					nextAtom.setBondType(matrix[currentId * nAtoms + nextId]);
					parentAtom.addNode(nextAtom);
					if (nextLevel < maxSpheres)
						atomQueue.add(nextAtom);
				}
			}
		}

		if (isDebug) {
			System.out.println(rootNode.toString());
		}
		hoseCode = rootNode.toString();
		return hoseCode;
	}

	public String[] getHoseCodesByAtomicNo(int atomicNo, int maxSpheres) {
		if (this.molecule == null)
			return null;
		LinkedList<String> hoseCodeList = new LinkedList<String>();
		int nAtoms = molecule.getAllAtoms();
		for (int iAtom = 0; iAtom < nAtoms; iAtom++)
			if (molecule.getAtomicNo(iAtom) == atomicNo)
				hoseCodeList.add(this.getHoseCode(iAtom, maxSpheres));
		String[] hoseCodes = null;
		if (hoseCodeList.size() > 0) {
			int nCodes = hoseCodeList.size();
			hoseCodes = new String[nCodes];
			for (int i = 0; i < nCodes; i++) {
				hoseCodes[i] = hoseCodeList.removeFirst();
			}
		}
		return hoseCodes;
	}

	public String[] getAllHoseCodes(int maxSpheres) {
		if (this.molecule == null)
			return null;
		int nAtoms = molecule.getAllAtoms();
		String[] hoseCodes = new String[nAtoms];
		for (int iAtom = 0; iAtom < nAtoms; iAtom++)
			hoseCodes[iAtom] = this.getHoseCode(iAtom, maxSpheres);
		return hoseCodes;
	}

	public static void main(String[] argv) {
		String url = "file:///home/engeler/source/workspace/GraphTheory/molFiles/hose2.mol";
		URL fileUrl;
		try {
			fileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		String molfile = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					fileUrl.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				molfile = molfile + inputLine + "\n";
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (HoseCodeGenerator.isDebug) {
			System.out.println("molfile");
			System.out.println(molfile);
		}
		// 
		StereoMolecule molecule = new StereoMolecule();
		com.actelion.research.chem.MolfileParser mfParser = new com.actelion.research.chem.MolfileParser();
		mfParser.parse(molecule, molfile);
		//
		if (isDebug) {
			int nAtoms = molecule.getAllAtoms();
			System.out.println("nAtoms=" + nAtoms);
			for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
				System.out.println("Atom(" + iAtom + ")="
						+ molecule.getAtomicNo(iAtom));
			}
			int nBonds = molecule.getAllBonds();
			System.out.println("nBonds=" + nBonds);
			for (int iBond = 0; iBond < nBonds; iBond++) {
				System.out.print("Bond(" + iBond + "): ("
						+ molecule.getBondAtom(0, iBond) + ","
						+ molecule.getBondAtom(1, iBond) + ")->"
						+ molecule.getBondType(iBond));
				if (molecule.isAromaticBond(iBond))
					System.out.print("*");
				System.out.println();
			}
		}
		HoseCodeGenerator generator = new HoseCodeGenerator(molecule);
		// find first carbon
		System.out.println(generator.getHoseCode(0, 4));
		String[] codes = generator.getAllHoseCodes(2);
		for (int iCode = 0; iCode < codes.length; iCode++)
			System.out.println("code:" + codes[iCode]);

	}

}
