package org.cheminfo.hook.fragments;

import com.actelion.research.chem.*;
import com.actelion.research.chem.coords.CoordinateInventor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.TreeMap;

public class FragmentGeneratorActelionId {
	private int maxAtoms;
	private int minAtoms;
	ExtendedMolecule molecule;
	// utility variables for recursion
	private int nAtoms = 0;
	private boolean[] adjacencyMatrix = null;
	private boolean[] bitmask = null;
	private TreeMap<String, Occurrence> resultFragments = null;
	// utility variables for id generation
	StereoMolecule tempMolecule;
	private int[] idMap = null;
	private int[] bondLookupTable = null;
	private boolean[] discoveredAtoms = null;
	private int[] connectivityQueue = null;

	public FragmentGeneratorActelionId() {
		super();
	}

	public int getMaxAtoms() {
		return maxAtoms;
	}

	public void setMaxAtoms(int maxAtoms) {
		this.maxAtoms = maxAtoms;
	}

	public int getMinAtoms() {
		return minAtoms;
	}

	public void setMinAtoms(int minAtoms) {
		this.minAtoms = minAtoms;
	}

	public ExtendedMolecule getMolecule() {
		return molecule;
	}

	public void setMolecule(ExtendedMolecule molecule) {
		this.molecule = molecule;
	}

	private void prepareGenerator() {
		this.updateAromaticity();
		this.nAtoms = this.molecule.getAllAtoms();
		this.adjacencyMatrix = new boolean[nAtoms * nAtoms];
		Arrays.fill(this.adjacencyMatrix, false);
		this.bondLookupTable = new int[nAtoms * nAtoms];
		Arrays.fill(this.bondLookupTable, -1);
		int nBonds = this.molecule.getAllBonds();
		int iAtom1, iAtom2;
		for (int iBond = 0; iBond < nBonds; iBond++) {
			iAtom1 = this.molecule.getBondAtom(0, iBond);
			iAtom2 = this.molecule.getBondAtom(1, iBond);
			this.bondLookupTable[iAtom1 * nAtoms + iAtom2] = iBond;
			this.adjacencyMatrix[iAtom1 * nAtoms + iAtom2] = true;
			this.bondLookupTable[iAtom2 * nAtoms + iAtom1] = iBond;
			this.adjacencyMatrix[iAtom2 * nAtoms + iAtom1] = true;
		}
		//
		this.idMap = new int[this.maxAtoms];
		this.bitmask = new boolean[nAtoms];
		this.tempMolecule = new StereoMolecule();
		this.resultFragments = new TreeMap<String, Occurrence>();

		discoveredAtoms = new boolean[this.nAtoms];
		connectivityQueue = new int[this.maxAtoms];
	}

	private void updateAromaticity() {
	//	boolean[] isAromaticAtom = new boolean[this.molecule.getAllAtoms()];
		boolean[] isAromaticBond = new boolean[this.molecule.getAllBonds()];
	//	Arrays.fill(isAromaticAtom, false);
		Arrays.fill(isAromaticBond, false);
		AromaticityResolver resolver = new AromaticityResolver(this.molecule);
		resolver.locateDelocalizedDoubleBonds(isAromaticBond);
	}

	public TreeMap<String, Occurrence> generate() {
		this.prepareGenerator();
		for (int iBit = 0; iBit < this.nAtoms; iBit++) {
			bitmask[iBit] = true;
			this.recursiveBinaryCounting(1, iBit);
			bitmask[iBit] = false;
		}
		return this.resultFragments;
	}

	private void recursiveBinaryCounting(int cardinality, int rightmostBit) {
		if (cardinality > this.maxAtoms)
			return;
		for (int iBit = rightmostBit + 1; iBit < this.nAtoms; iBit++) {
			bitmask[iBit] = true;
			recursiveBinaryCounting(cardinality + 1, iBit);
			bitmask[iBit] = false;
		}
		// add fragment
		if (cardinality >= this.minAtoms && this.isConnected(cardinality))
			this.addFragment(cardinality);
	}

	private boolean isConnected(int fragmentAtoms) {
		int nDiscovered = 1;
		int basePointer = 0;
		int topPointer = 1;
		for (int i = 0; i < this.nAtoms; i++)
			if (this.bitmask[i])
				connectivityQueue[0] = i;
		//connectivityQueue[0] = idMap[0]; // start with atom 0!
		Arrays.fill(discoveredAtoms, false);
		discoveredAtoms[connectivityQueue[0]] = true;
		int currentAtom, nConnectedAtoms, iNeighbourAtom;
		while (basePointer != topPointer && nDiscovered < fragmentAtoms) {
			currentAtom = connectivityQueue[basePointer++];
			nConnectedAtoms = molecule.getAllConnAtoms(currentAtom);
			for (int iConnectedAtom = 0; iConnectedAtom < nConnectedAtoms; iConnectedAtom++) {
				iNeighbourAtom = molecule.getConnAtom(currentAtom, iConnectedAtom);
				if (this.bitmask[iNeighbourAtom] && !discoveredAtoms[iNeighbourAtom]) {
					nDiscovered++;
					discoveredAtoms[iNeighbourAtom] = true;
					connectivityQueue[topPointer++] = iNeighbourAtom;
				}
			}
		}
		if (nDiscovered != fragmentAtoms)
			return false;
		else
			return true;
	}
	
	private void addFragment(int fragmentAtoms) {
//		{
//			// test connectivity in a BFS manner
//			int nDiscovered = 1;
//			int basePointer = 0;
//			int topPointer = 1;
//			for (int i = 0; i < this.nAtoms; i++)
//				if (this.bitmask[i])
//					connectivityQueue[0] = i;
//			//connectivityQueue[0] = idMap[0]; // start with atom 0!
//			Arrays.fill(discoveredAtoms, false);
//			discoveredAtoms[connectivityQueue[0]] = true;
//			int currentAtom, nConnectedAtoms, iNeighbourAtom;
//			while (basePointer != topPointer && nDiscovered < fragmentAtoms) {
//				currentAtom = connectivityQueue[basePointer++];
//				nConnectedAtoms = molecule.getAllConnAtoms(currentAtom);
//				for (int iConnectedAtom = 0; iConnectedAtom < nConnectedAtoms; iConnectedAtom++) {
//					iNeighbourAtom = molecule.getConnAtom(currentAtom, iConnectedAtom);
//					if (this.bitmask[iNeighbourAtom] && !discoveredAtoms[iNeighbourAtom]) {
//						nDiscovered++;
//						discoveredAtoms[iNeighbourAtom] = true;
//						connectivityQueue[topPointer++] = iNeighbourAtom;
//					}
//				}
//			}
//			if (nDiscovered != fragmentAtoms)
//				return;
//		}
		// reset
		tempMolecule.setAllAtoms(0);
		tempMolecule.setAllBonds(0);
		tempMolecule.setFragment(true);
		// copy the atoms
		int stackPointer = 0;
		for (int iAtom = 0; iAtom < this.nAtoms; iAtom++) {
			if (bitmask[iAtom]) {
				this.molecule.copyAtom(tempMolecule, iAtom, 0,0);
			//	tempMolecule.addAtom(this.molecule, iAtom);
				idMap[stackPointer] = iAtom;
				stackPointer++;
			}
		}
//		{
//			// test connectivity in a BFS manner
//			int nDiscovered = 1;
//			int basePointer = 0;
//			int topPointer = 1;
//			connectivityQueue[0] = idMap[0]; // start with atom 0!
//			Arrays.fill(discoveredAtoms, false);
//			discoveredAtoms[connectivityQueue[0]] = true;
//			int currentAtom, nConnectedAtoms, iNeighbourAtom;
//			while (basePointer != topPointer && nDiscovered < fragmentAtoms) {
//				currentAtom = connectivityQueue[basePointer++];
//				nConnectedAtoms = molecule.getAllConnAtoms(currentAtom);
//				for (int iConnectedAtom = 0; iConnectedAtom < nConnectedAtoms; iConnectedAtom++) {
//					iNeighbourAtom = molecule.getConnAtom(currentAtom, iConnectedAtom);
//					if (this.bitmask[iNeighbourAtom] && !discoveredAtoms[iNeighbourAtom]) {
//						nDiscovered++;
//						discoveredAtoms[iNeighbourAtom] = true;
//						connectivityQueue[topPointer++] = iNeighbourAtom;
//					}
//				}
//			}
//			if (nDiscovered != fragmentAtoms)
//				return;
//		}
		int iOffset;
		int iOrigAtom1, iOrigAtom2, iBond, iAddr;
		for (int iAtom1 = 0; iAtom1 < fragmentAtoms; iAtom1++) {
			iOrigAtom1 = idMap[iAtom1];
			iOffset = iOrigAtom1 * nAtoms;
			for (int iAtom2 = 0; iAtom2 < iAtom1; iAtom2++) {
				iOrigAtom2 = idMap[iAtom2];
				iAddr = iOffset + iOrigAtom2;
				if (this.adjacencyMatrix[iAddr]) {
					iBond = this.bondLookupTable[iAddr];
					tempMolecule.addBond(iAtom1, iAtom2, this.molecule.getBondType(iBond));
				}
			}
		}

		//
		Canonizer canonizer = new Canonizer(tempMolecule);
		String idCode = canonizer.getIDCode();
		Occurrence occurrence;
		if ((occurrence = this.resultFragments.get(idCode)) == null)
			this.resultFragments.put(idCode, new Occurrence(1));
		else
			occurrence.increaseOccurrence();
	}

	
	
	public static void main(String[] argv) {
		String url = "file:///home/engeler/source/workspace/GraphTheory/molFiles/hose.mol";
		
		
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
		// 
		StereoMolecule molecule = new StereoMolecule();
		com.actelion.research.chem.MolfileParser mfParser = new com.actelion.research.chem.MolfileParser();
		mfParser.parse(molecule, molfile);
		{
			Depictor2D depictor = new Depictor2D(molecule);
			BufferedImage bufferedImage = new BufferedImage(300, 300,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, 300, 300);
			Rectangle2D.Double viewRect = new Rectangle2D.Double();
			viewRect.setRect(0, 0, 300, 300);
			depictor.simpleUpdateCoords(viewRect, Depictor.cModeInflateToMaxAVBL);
			depictor.paint(g2d);
			try {
				ImageIO.write(bufferedImage, "png", new File("original.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//
		FragmentGeneratorActelionId generator = new FragmentGeneratorActelionId();
		generator.setMolecule(molecule);
		int minAtoms = 3;
		int maxAtoms = 6;
		for (int nAtoms = minAtoms; nAtoms <= maxAtoms; nAtoms++) {
			System.out.println("nAtoms="+nAtoms);
			generator.setMinAtoms(nAtoms);
			generator.setMaxAtoms(nAtoms);
			TreeMap<String, Occurrence> fragments = generator.generate();
			System.out.println("Nfragments"+fragments.size());
		}
	}

	public static void idCode2Image(String idCode) {
		IDCodeParser parser = new IDCodeParser();
		StereoMolecule displayMolecule = parser.getCompactMolecule(idCode);
		CoordinateInventor inventor = new CoordinateInventor();
		inventor.invent(displayMolecule);
		String filename = "";
		try {
			filename = URLEncoder.encode(idCode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		Depictor2D depictor = new Depictor2D(displayMolecule);
		BufferedImage bufferedImage = new BufferedImage(300, 300,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, 300, 300);
		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, 300, 300);
		depictor.simpleUpdateCoords(viewRect, Depictor.cModeInflateToMaxAVBL);
		depictor.paint(g2d);
		try {
			ImageIO.write(bufferedImage, "png", new File(filename + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
