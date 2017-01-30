package org.cheminfo.hook.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.cheminfo.hook.util.arrays.ArraySearch;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

/**
 * 
 * This Class implements a SMILES type canonizer for the virtual screening
 * projects. This class is written such that memory/object allocations only
 * occur when an Instance is created.
 * 
 * IMPORTANT NOTE: this canonizer does not handle disconnected components
 * correctly!
 * 
 * 
 * 
 * 
 * @author Marco Engeler, EPFL 2007
 * 
 */
public class FragmentCanonizer {
	private static final boolean isDebug = false;

	private Fragment fragment;
	private boolean firstAtomPrioritized = false;

	// these are simply utility variables
	private int nAtoms;
	private int[] primeNumbers;
	private int[] auxiliaryMatrix;
	private int[] nNeighbours;
	private int[] neighbourLists;

	// id maps
	private int[] idMapRank2Atom; // maps a rank id to an atom id
	private int[] idMapAtom2Rank; // maps an atom id to a rank id
	private int[] idMapAtom2Class; // maps an atom to a class

	// rank arrrays
	private int[] atomRank; // atom ranks
	private int[] tmpRankArray;
	/* these ranks are only used initially */
	private long[] atomRanksL;
	private long[] atomRanksLsorted;

	// rank accounting
	private int nClasses; // number of symmetry groups, aka vertex partitions
	private int[] classBegin; // first index of a partition
	private int[] classEnd; // last index of a partition

	/**
	 * graph algorithm variables used for BFS & DFS
	 */
	private int[] vertexQueue;
	private int[] vertexMask;
	private int[] parentVertex;
	private int queueBasePointer;
	private int queueTopPointer;
	private String[] atomLabels;
	// ring structure handling
	private int[] ringLabel = new int[Fragment.MAX_ATOMS];
	private int nRings;

	private int bondStackPointer;
	private int[] bondTargetID = new int[Fragment.MAX_BONDS];
	private String[] bondTypeStack = new String[Fragment.MAX_BONDS];

	private byte[] idCodeStack;
	private ByteArrayOutputStream outputStream;

	public FragmentCanonizer() {
		fragment = null;
		// initialize all arrays
		this.auxiliaryMatrix = new int[Fragment.MAX_ATOMS * Fragment.MAX_ATOMS];
		this.primeNumbers = new int[Fragment.MAX_ATOMS];
		this.nNeighbours = new int[Fragment.MAX_ATOMS * Fragment.MAX_ATOMS];
		this.neighbourLists = new int[Fragment.MAX_ATOMS * Fragment.MAX_ATOMS];

		// rank arrays
		this.atomRank = new int[Fragment.MAX_ATOMS];
		this.tmpRankArray = new int[Fragment.MAX_ATOMS];
		this.atomRanksL = new long[Fragment.MAX_ATOMS];
		this.atomRanksLsorted = new long[Fragment.MAX_ATOMS];

		// id maps
		this.idMapRank2Atom = new int[Fragment.MAX_ATOMS]; // maps an a rank to
		// an atom
		this.idMapAtom2Rank = new int[Fragment.MAX_ATOMS]; // maps an atom to a
		// rank
		this.idMapAtom2Class = new int[Fragment.MAX_ATOMS];

		// rank accounting
		this.classBegin = new int[Fragment.MAX_ATOMS];
		this.classEnd = new int[Fragment.MAX_ATOMS];

		// BFS & DFS
		this.vertexQueue = new int[Fragment.MAX_ATOMS];
		this.vertexMask = new int[Fragment.MAX_ATOMS];
		this.parentVertex = new int[Fragment.MAX_ATOMS];

		// ring structure handling
		this.ringLabel = new int[Fragment.MAX_ATOMS];

		this.atomLabels = new String[Fragment.MAX_ATOMS];

		this.idCodeStack = new byte[1024];
		this.outputStream = new ByteArrayOutputStream(1024);
		//
		this.generatePrimes();
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public String getIdString() {
		if (fragment == null)
			return null;
		this.doCANON();
		return this.getHumanReadableId();
	}

	private String getFastParsableId() {
		// do some preparation work
		Arrays.fill(this.auxiliaryMatrix, 0);
		int iAtom1, iAtom2, iBondType;
		for (int iBond = 0; iBond < fragment.getNBonds(); iBond++) {
			iAtom1 = fragment.getBondAtom(iBond, 0);
			iAtom2 = fragment.getBondAtom(iBond, 1);
			iBondType = fragment.getBondType(iBond);
			this.auxiliaryMatrix[iAtom1 * this.nAtoms + iAtom2] = iBondType;
			this.auxiliaryMatrix[iAtom2 * this.nAtoms + iAtom1] = iBondType;
		}
		// start encoding
		Arrays.fill(this.idCodeStack, (byte) 0);
		int stackPointer = 0;
		this.idCodeStack[stackPointer++] = (byte) this.nAtoms;
		// construct id
		int atomEntity, nBonds;
		for (int iRank = 0; iRank < this.nAtoms; iRank++) {
			// add the net atom
			int iAtom = this.idMapRank2Atom[iRank];
			atomEntity = fragment.getFastParsableAtomEntity(iAtom);
			this.idCodeStack[stackPointer++] = (byte) ((atomEntity >> 8) & 255);
			this.idCodeStack[stackPointer++] = (byte) (atomEntity & 255);
			stackPointer++; // save a byte for the number of bonds
			nBonds = 0;
			for (int iNeighbour = 0; iNeighbour < this.nNeighbours[iAtom]; iNeighbour++) {
				int iNeighbourAtom = this.neighbourLists[iAtom * this.nAtoms
						+ iNeighbour];
				int iNeighbourRank = this.idMapAtom2Rank[iNeighbourAtom];
				if (iNeighbourRank > iRank) {
					nBonds++;
					this.idCodeStack[stackPointer++] = (byte) this.auxiliaryMatrix[iAtom
							* this.nAtoms + iNeighbourAtom];
					this.idCodeStack[stackPointer++] = (byte) iNeighbourRank;
				}
			}
			this.idCodeStack[stackPointer - (2 * nBonds + 1)] = (byte) nBonds;
			// the bonding information need to be sorted
			// let's do this as a bubble sort locally
			int iLower, iUpper;
			byte temp;
			int iBasePointer = stackPointer - (2 * nBonds);
			for (int iOuter = 1; iOuter < nBonds; iOuter++) {
				for (int iInner = 1; iInner <= iOuter; iInner++) {
					iUpper = iBasePointer + (2 * iInner + 1);
					iLower = iUpper - 2;
					if (this.idCodeStack[iLower] > this.idCodeStack[iUpper]) {
						// do a swap
						// swap bond partners
						temp = this.idCodeStack[iUpper];
						this.idCodeStack[iUpper] = this.idCodeStack[iLower];
						this.idCodeStack[iLower] = temp;
						// swap bond types
						iUpper--;
						iLower--;
						temp = this.idCodeStack[iUpper];
						this.idCodeStack[iUpper] = this.idCodeStack[iLower];
						this.idCodeStack[iLower] = temp;
					}
				}
			}
		}
		this.outputStream.reset();
		this.outputStream.write(this.idCodeStack, 0, stackPointer);
		String idCode = this.outputStream.toString();
		return idCode;
	}

	private String getHumanReadableId() {
		String result = ""; // get bond map
		Arrays.fill(this.auxiliaryMatrix, 0);
		int iAtom1, iAtom2, iBondType;
		for (int iBond = 0; iBond < fragment.getNBonds(); iBond++) {
			iAtom1 = fragment.getBondAtom(iBond, 0);
			iAtom2 = fragment.getBondAtom(iBond, 1);
			iBondType = fragment.getBondType(iBond);
			this.auxiliaryMatrix[iAtom1 * this.nAtoms + iAtom2] = iBondType;
			this.auxiliaryMatrix[iAtom2 * this.nAtoms + iAtom1] = iBondType;
		} // construct id
		for (int iRank = 0; iRank < this.nAtoms; iRank++) {
			int iAtom = this.idMapRank2Atom[iRank];
			result += fragment.getSimpleAtomEntity(iAtom);
			bondStackPointer = 0;
			for (int iNeighbour = 0; iNeighbour < this.nNeighbours[iAtom]; iNeighbour++) {
				int iNeighbourAtom = this.neighbourLists[iAtom * this.nAtoms
						+ iNeighbour];
				int iNeighbourRank = this.idMapAtom2Rank[iNeighbourAtom];
				if (iNeighbourRank > iRank) {
					//
					this.bondTargetID[bondStackPointer] = iNeighbourRank;
					this.bondTypeStack[bondStackPointer] = fragment
							.getSimpleBondEntity(this.auxiliaryMatrix[iAtom
									* this.nAtoms + iNeighbourAtom]);
					bondStackPointer++;
				}
			}
			result += this.getSortedBondString();
		}
		return result;
	}

	private String getSortedBondString() {
		String result = "";
		String tmpSymbol;
		int tmpID;
		for (int i1 = bondStackPointer - 1; i1 > 0; i1--) {
			for (int i2 = 0; i2 < i1; i2++) {
				if (this.bondTargetID[i2] > this.bondTargetID[i2 + 1]
						|| (this.bondTargetID[i2] == this.bondTargetID[i2 + 1] && this.bondTypeStack[i2]
								.compareTo(this.bondTypeStack[i2 + 1]) == 1)) {
					//
					tmpID = this.bondTargetID[i2];
					this.bondTargetID[i2] = this.bondTargetID[i2 + 1];
					this.bondTargetID[i2 + 1] = tmpID;
					//
					tmpSymbol = this.bondTypeStack[i2];
					this.bondTypeStack[i2] = this.bondTypeStack[i2 + 1];
					this.bondTypeStack[i2 + 1] = tmpSymbol;
				}
			}
		}
		for (int i = 0; i < bondStackPointer; i++)
			result += this.bondTypeStack[i] + this.bondTargetID[i];
		return result;
	}

	private void prepare() {
		this.nAtoms = fragment.getNAtoms();
		Arrays.fill(this.neighbourLists, -1);
		fragment.getAdjecencyMatrix(this.auxiliaryMatrix);
		for (int iAtom = 0; iAtom < this.nAtoms; iAtom++) {
			this.idMapRank2Atom[iAtom] = iAtom;
			// we need a long invariant to avoid overflow problems
			this.atomRanksL[iAtom] = fragment.getAtomicInvariant(iAtom);
			// this.atomRank[iAtom] = fragment.getAtomicInvariant(iAtom);
			this.nNeighbours[iAtom] = 0;
			for (int iNeighbour = 0; iNeighbour < this.nAtoms; iNeighbour++) {
				if (this.auxiliaryMatrix[iAtom * this.nAtoms + iNeighbour] == 1) {
					this.neighbourLists[iAtom * this.nAtoms
							+ this.nNeighbours[iAtom]] = iNeighbour;
					this.nNeighbours[iAtom]++;
				}
			}
		}
		// set symmetry classes
		this.nClasses = 1;
		this.classBegin[0] = 0;
		this.classEnd[0] = this.nAtoms;
		if (this.isFirstAtomPrioritized())
			this.atomRanksL[0] = 0;
		this.remapAtomRanksLongToInt();
	}

	private void remapAtomRanksLongToInt() {
		int iStackPointer = 1;
		this.atomRanksLsorted[0] = this.atomRanksL[0];
		for (int iAtom = 1; iAtom < this.nAtoms; iAtom++) {
			long rank = this.atomRanksL[iAtom];
			int index = ArraySearch.binarySearch(this.atomRanksLsorted, rank, 0, iStackPointer);
			if (this.atomRanksLsorted[index] != rank) {
				System.arraycopy(this.atomRanksLsorted, index, this.atomRanksLsorted, index+1, iStackPointer - index);
				this.atomRanksLsorted[index] = rank;
				iStackPointer++;
			}
			// 
		}
		// remap the ranks
		for (int iAtom = 0; iAtom < this.nAtoms; iAtom++) {
			long rank = this.atomRanksL[iAtom];
			int index = ArraySearch.binarySearch(this.atomRanksLsorted, rank, 0, iStackPointer);
			this.atomRank[iAtom] = index;
		}
		if (isDebug) {
			System.out.println("remapped ranks");
			for (int iAtom = 0; iAtom < this.nAtoms; iAtom++) {
				System.out.println("iAtom="+iAtom+"atomRankL="+this.atomRank[iAtom]+"atomRanksL="+this.atomRanksL[iAtom]);
			}
		}
	}

	/*
	 * CANONIZATION ROUTINES
	 */

	private void doCANON() {
		this.prepare();
		this.sortClasses();
		this.rank();
		int previousClasses;
		while (this.nClasses != this.nAtoms) {
			previousClasses = this.nClasses;
			this.multiplyWithNeighbours();
			this.sortClasses();
			this.rank();
			if (previousClasses == this.nClasses) {
				this.breakTie();
				this.sortClasses();
				this.rank();
			}
			// else: not invariant start the whole shebang again!
		}
	}

	// not used
	private String doGENES() {
		Arrays.fill(this.auxiliaryMatrix, 0);
		int iAtom1, iAtom2, iBondType;
		for (int iBond = 0; iBond < fragment.getNBonds(); iBond++) {
			iAtom1 = fragment.getBondAtom(iBond, 0);
			iAtom2 = fragment.getBondAtom(iBond, 1);
			iBondType = fragment.getBondType(iBond);
			this.auxiliaryMatrix[iAtom1 * this.nAtoms + iAtom2] = iBondType;
			this.auxiliaryMatrix[iAtom2 * this.nAtoms + iAtom1] = iBondType;
		}
		// reset to initial invariants
		Arrays.fill(this.vertexMask, 0, this.nAtoms, 0);
		Arrays.fill(this.parentVertex, 0, this.nAtoms, -1);
		Arrays.fill(this.ringLabel, 0, this.nAtoms, 0);
		this.nRings = 0;
		int iAtom = this.idMapRank2Atom[0]; // choose the atom with the lowest
		// id to start
		this.parentVertex[iAtom] = iAtom; // the first vertex is in some sense
		// its own parent
		this.vertexMask[iAtom] = 1;
		this.queueTopPointer = 0;
		String result = this.doBranch(iAtom);
		if (this.nRings > 0) {
			System.out.println();
		}
		return result;
	}

	@SuppressWarnings("unused")
	private void traverseMolecule(int iAtom) {
		this.atomLabels[iAtom] = fragment.getSmilesAtomEntity(iAtom);
		int iAddr = iAtom * this.nAtoms;
		int iNeighbourAtom = 0;
		for (int iNeighbour = 0; iNeighbour < this.nNeighbours[iAtom]; iNeighbour++) {
			iNeighbourAtom = this.neighbourLists[iAddr + iNeighbour];
			if (iNeighbourAtom != this.parentVertex[iAtom]) {
				if (this.vertexMask[iNeighbourAtom] != 1) {
					this.vertexMask[iNeighbourAtom] = 1;
					traverseMolecule(iNeighbourAtom);
				} else {
					// we have detected a ring!
					this.nRings++;
					this.ringLabel[iAtom] = 1;
					this.ringLabel[iNeighbourAtom] = 1;

				}
			}
		}

	}

	// this is missing the structure to handle rings as of yet
	private String doBranch(int iAtom) {
		String result = fragment.getSmilesAtomEntity(iAtom);
		int nNewNeighbours = 0;
		int iAddr = iAtom * this.nAtoms;
		int iNeighbourAtom;
		for (int iNeighbour = 0; iNeighbour < this.nNeighbours[iAtom]; iNeighbour++) {
			iNeighbourAtom = this.neighbourLists[iAddr + iNeighbour];
			if (iNeighbourAtom != this.parentVertex[iAtom]) {
				if (this.vertexMask[iNeighbourAtom] != 1) {
					nNewNeighbours++;
					this.parentVertex[iNeighbourAtom] = iAtom;
					this.vertexQueue[this.queueTopPointer++] = iNeighbourAtom;
					this.vertexMask[iNeighbourAtom] = 1;
				} else {
					// we have detected a ring!
					this.nRings++;
					this.ringLabel[iAtom] = this.nRings;
					this.ringLabel[iNeighbourAtom] = this.nRings;
				}
			}
		}
		// sort according the neighbours found
		if (nNewNeighbours > 1) {
			// use a bubble sort
			int lowerBound = this.queueTopPointer - nNewNeighbours;
			int upperBound = this.queueTopPointer;
			int iAtom1, iAtom2;
			for (int iOuter = upperBound; iOuter > lowerBound + 1; iOuter--) {
				for (int iInner = lowerBound + 1; iInner < iOuter; iInner++) {
					// compare iInner and iInner -1
					iAtom1 = this.vertexQueue[iInner - 1];
					iAtom2 = this.vertexQueue[iInner];
					// is the order already correct
					if (this.atomRank[this.idMapAtom2Rank[iAtom1]] > this.atomRank[this.idMapAtom2Rank[iAtom2]])
						continue;
					// swap
					this.vertexQueue[iInner - 1] = iAtom2;
					this.vertexQueue[iInner] = iAtom1;
				}
			}
		}
		//
		String bondEntity = "";
		while (nNewNeighbours != 0) {
			nNewNeighbours--;
			iNeighbourAtom = this.vertexQueue[--this.queueTopPointer];
			bondEntity = fragment
					.getSmilesBondEntity(this.auxiliaryMatrix[iNeighbourAtom
							* this.nAtoms + this.parentVertex[iNeighbourAtom]]);
			if (nNewNeighbours == 0) {
				result += bondEntity + this.doBranch(iNeighbourAtom);
			} else {
				// do a branching
				result += "(";
				result += bondEntity + this.doBranch(iNeighbourAtom);
				result += ")";
			}
		}
		return result;
	}

	private void multiplyWithNeighbours() {
		// System.arraycopy(this.atomRank, 0, this.tmpRankArray, 0,
		// this.atomRank.length);
		for (int iClass = 0; iClass <= this.nClasses; iClass++) {
			// if (this.classBegin[iClass] != this.classEnd[iClass] - 1) {
			for (int iClassElement = this.classBegin[iClass]; iClassElement < this.classEnd[iClass]; iClassElement++) {
				int iAtom = this.idMapRank2Atom[iClassElement];
				int rank = 1;
				for (int iNeighbour = 0; iNeighbour < this.nNeighbours[iAtom]; iNeighbour++) {
					int iNeighbourAtom = this.neighbourLists[iAtom
							* this.nAtoms + iNeighbour];
					rank *= this.atomRank[this.idMapAtom2Rank[iNeighbourAtom]];
				}
				this.tmpRankArray[iClassElement] = rank;
			}
			// }
		}
		// swap work and rank array
		int[] tmpArray = this.tmpRankArray;
		this.tmpRankArray = this.atomRank;
		this.atomRank = tmpArray;
	}

	private void rank() {
		int iRank = 0;
		for (int iClass = 0; iClass < this.nClasses; iClass++) {
			// handle first element of class
			int iClassElement = this.classBegin[iClass];
			this.tmpRankArray[iClassElement] = this.primeNumbers[iRank];
			// adjust the maps
			this.idMapAtom2Rank[this.idMapRank2Atom[iClassElement]] = iClassElement;
			this.idMapAtom2Class[this.idMapRank2Atom[iClassElement]] = iRank;
			// loop over remaing atoms in the class
			for (iClassElement += 1; iClassElement < this.classEnd[iClass]; iClassElement++) {
				if (this.atomRank[iClassElement - 1] != this.atomRank[iClassElement]) {
					iRank++;
				}
				// set rank now that we know whether thing's have changed or not
				this.tmpRankArray[iClassElement] = this.primeNumbers[iRank];
				// adjust the maps
				this.idMapAtom2Rank[this.idMapRank2Atom[iClassElement]] = iClassElement;
				this.idMapAtom2Class[this.idMapRank2Atom[iClassElement]] = iRank;
			}
			iRank++;
		}
		this.nClasses = iRank;
		// swap arrays
		int[] tmpArray = this.tmpRankArray;
		this.tmpRankArray = this.atomRank;
		this.atomRank = tmpArray;
		// renew class borders
		this.classBegin[0] = 0;
		int iClass = 0;
		for (int i = 1; i < this.nAtoms; i++) {
			if (this.atomRank[i - 1] != this.atomRank[i]) {
				this.classEnd[iClass] = i;
				iClass++;
				this.classBegin[iClass] = i;
			}
		}
		this.classEnd[this.nClasses - 1] = this.nAtoms;
	}

	private void sortClasses() {
		for (int iClass = 0; iClass < this.nClasses; iClass++) {
			if (this.classBegin[iClass] != this.classEnd[iClass] - 1)
				this.quicksort(this.classBegin[iClass], this.classEnd[iClass]);
		}
	}

	private void breakTie() {
		//
		Arrays.fill(vertexMask, 0, this.nAtoms, 0);
		// double all ranks first
		for (int i = 0; i < this.nAtoms; i++)
			this.atomRank[i] *= 2;
		this.queueBasePointer = 0;
		this.queueTopPointer = 1;
		this.vertexQueue[0] = this.idMapRank2Atom[0]; // start with the lowest
		// ranking atom
		this.vertexMask[this.idMapRank2Atom[0]] = 1; // this atom is also
		// discovered
		// of course
		int currentAtom, currentClass;
		while (this.queueBasePointer != this.queueTopPointer) {
			currentAtom = this.vertexQueue[this.queueBasePointer++];
			currentClass = this.idMapAtom2Class[currentAtom];
			if (this.classBegin[currentClass] != this.classEnd[currentClass] - 1) {
				this.atomRank[this.idMapAtom2Rank[currentAtom]]--;
				return;
			}
			for (int iNeighbour = 0; iNeighbour < this.nNeighbours[currentAtom]; iNeighbour++) {
				int iNeighbourAtom = this.neighbourLists[currentAtom
						* this.nAtoms + iNeighbour];
				if (this.vertexMask[iNeighbourAtom] == 0) {
					this.vertexMask[iNeighbourAtom] = 1;
					this.vertexQueue[this.queueTopPointer++] = iNeighbourAtom;
				}
			}
		}
	}

	/*
	 * PRIME NUMBER GENERATION
	 * 
	 * taken form hook project
	 */

	private void generatePrimes() {
		this.primeNumbers = new int[Fragment.MAX_ATOMS];
		this.primeNumbers[0] = 2;
		this.primeNumbers[1] = 3;
		int maxIndex = 1;
		int nextPrime = this.primeNumbers[maxIndex];
		while (maxIndex < this.primeNumbers.length - 1) {
			nextPrime += 2;
			if (this.testPrimality(nextPrime, maxIndex))
				this.primeNumbers[++maxIndex] = nextPrime;
		}
	}

	private boolean testPrimality(int value, int maIndex) {
		int sqrtValue = (int) Math.sqrt(value);
		boolean isPrimeNumber = true;
		for (int i = 0; i <= maIndex; i++) {
			if (this.primeNumbers[i] > sqrtValue)
				break;
			if (value % this.primeNumbers[i] == 0) {
				isPrimeNumber = false;
				break;
			}
		}
		return isPrimeNumber;
	}

	/*
	 * QUICKSORT
	 * 
	 * below follows a specific version of quick sort in order to sort The
	 * algorithm is "in place" as outlined in the QuickSort wikipedia article
	 * 
	 */
	private void quicksort(int lower, int upper) {
		if (upper > lower) {
			int pivot = (upper + lower) / 2;
			pivot = quicksortPartition(lower, upper, pivot);
			quicksort(lower, pivot);
			quicksort(pivot + 1, upper);
		}
	}

	private int quicksortPartition(int lower, int upper, int pivot) {
		int pivotValue = this.atomRank[pivot];
		quicksortSwap(pivot, upper - 1);
		int basePointer = lower;
		for (int i = lower; i < upper - 1; i++) {
			if (this.atomRank[i] < pivotValue) {
				this.quicksortSwap(basePointer, i);
				basePointer++;
			}
		}
		this.quicksortSwap(basePointer, upper - 1);
		return basePointer;
	}

	private void quicksortSwap(int i, int j) {
		int temp;
		// swap atom ranks
		temp = this.atomRank[i];
		this.atomRank[i] = this.atomRank[j];
		this.atomRank[j] = temp;
		// swap ordering
		temp = this.idMapRank2Atom[i];
		this.idMapRank2Atom[i] = this.idMapRank2Atom[j];
		this.idMapRank2Atom[j] = temp;
	}

	// taken from the hook project
	public static void main(String[] argv) {
		FragmentGenerator fragmentGenerator = new FragmentGenerator();
		StereoMolecule molecule = new StereoMolecule();
		MolfileParser parser = new MolfileParser();
		parser
				.parse(
						molecule,
						new File(
								"/home/engeler/source/workspace/GraphTheory/molFiles/test.mol"));
		fragmentGenerator.setMolecule(molecule);
		fragmentGenerator.setMinAtoms(4);
		fragmentGenerator.setMaxAtoms(4);
		TreeMap<String, Occurrence> fragments = fragmentGenerator.generate();
		Set<String> keys = fragments.keySet();
		Iterator<String> key = keys.iterator();
		while (key.hasNext()) {
			String idCode = key.next();
			System.out.println("idCode=" + idCode);

		}
	}

	/**
	 * If the first atom is prioritized it will be the first atom in the idCode.
	 * This will mess canonicalization up in general. We need this however for
	 * the HOSE code type spherical descriptor.
	 * 
	 * @return
	 */
	public boolean isFirstAtomPrioritized() {
		return firstAtomPrioritized;
	}

	public void setFirstAtomPrioritized(boolean firstAtomPrioritized) {
		this.firstAtomPrioritized = firstAtomPrioritized;
	}

	public int[] getIdMapRank2Atom() {
		return idMapRank2Atom;
	}

	public int[] getIdMapAtom2Class() {
		return idMapAtom2Class;
	}

	public int[] getIdMapAtom2Rank() {
		return idMapAtom2Rank;
	}

}
