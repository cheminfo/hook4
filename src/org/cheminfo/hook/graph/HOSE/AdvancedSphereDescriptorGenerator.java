package org.cheminfo.hook.graph.HOSE;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.epfl.EPFLUtils;
import com.actelion.research.epfl.StereoInfo;
import org.cheminfo.hook.fragments.Fragment;
import org.cheminfo.hook.fragments.FragmentCanonizer;
import org.cheminfo.hook.math.util.MathUtils;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.Vector;

/**
 * This class is used to generate spherical fragments similar to hose codes
 * 
 * 
 */
public class AdvancedSphereDescriptorGenerator {
	private StereoMolecule molecule = null;
	private FragmentCanonizer canonizer;
	private Fragment fragment;

	public static final int nMinSpheres = 2;
	public static final int nMaxSpheres = 6;

	
	// private int

	// helper arrays
	private int[] sphereCounts = null;
	private int[] atomStack = null;
	private int[] atomMap = null;
	private int[] reverseIdMap = null;
	private HoseStereoInfo[] stereoInfo = null;
	private boolean[] discovered = null;

	public AdvancedSphereDescriptorGenerator() {
		super();
		init();
	}

	public AdvancedSphereDescriptorGenerator(StereoMolecule molecule) {
		super();
		init();
		this.setMolecule(molecule);
	}

	public void init() {
		canonizer = new FragmentCanonizer();
		fragment = new Fragment();
		fragment.setNoStereo(false);
		fragment.setExplicitHydrogens(true);
		canonizer.setFragment(fragment);
		// TODO enable this
		canonizer.setFirstAtomPrioritized(true);
	}

	public StereoMolecule getMolecule() {
		return molecule;
	}

	public void setMolecule(StereoMolecule molecule) {
		this.molecule = molecule;
		if (this.molecule != null) {
			this.testInputMolecule();
			this.prepareStereoInfo();
			this.prepareGenerator();
		}
	}

	private void prepareGenerator() {
		this.molecule.ensureHelperArrays(2);
		// update aromaticity

		// prepare arrays
		int nAtoms = this.molecule.getAllAtoms();
		this.atomStack = new int[nAtoms];
		this.sphereCounts = new int[nAtoms];
		this.atomMap = new int[nAtoms];
		this.discovered = new boolean[nAtoms];
		this.reverseIdMap = new int[nAtoms];
	}

	public Vector<String> getAllSphereIdCodes(int nSpheres) {
		if (this.molecule == null)
			return null;
		Vector<String> idCodes = new Vector<String>();
		for (int iAtom = 0; iAtom < this.molecule.getAllAtoms(); iAtom++)
			idCodes.add(this.getSphereIdCode(iAtom, nSpheres));
		return idCodes;
	}

	public TreeMap<Integer, String> getAllSphereIdCodesByAtomicNo(int atomicNo,
			int nSpheres) {
		if (this.molecule == null)
			return null;
		TreeMap<Integer, String> idCodes = new TreeMap<Integer, String>();
		for (int iAtom = 0; iAtom < this.molecule.getAllAtoms(); iAtom++) {
			if (this.molecule.getAtomicNo(iAtom) == atomicNo
					|| (atomicNo == 1 && this.molecule.getAllHydrogens(iAtom) > 0)) {
				String code = this.getSphereIdCode(iAtom, nSpheres);
				if (atomicNo == 1)
					code = AdvancedSphereDescriptorGenerator
							.adaptCodeToHydrogen(code);
				idCodes.put(new Integer(iAtom), code);
			}
		}
		return idCodes;
	}

	public static String adaptCodeToHydrogen(String inCode) {
		String outCode = inCode.replaceFirst("\\)", ",H)");
		return outCode;
	}

	/**
	 * This adapts the hose codes for the stereo chemistry
	 * 
	 * @param inCode
	 * @param stereoInfo
	 * @return
	 */
	public static String adaptCodeToStereoChemistry(String inCode,
			StereoInfo stereoInfo) {
		String targetCode = "";
		if (stereoInfo == StereoInfo.PRO_E) {
			targetCode = "pro-E";
		} else if (stereoInfo == StereoInfo.PRO_Z) {
			targetCode = "pro-Z";
		} else if (stereoInfo == StereoInfo.PRO_R) {
			targetCode = "pro-R";
		} else if (stereoInfo == StereoInfo.PRO_S) {
			targetCode = "pro-R";
			inCode = invertStereoCenters(inCode);
		}
		if (targetCode.equals(""))
			return inCode;
		String outCode = inCode.replaceFirst("\\)", "," + targetCode + ")");
		return outCode;
	}

	public static int countStereoCenters(String code) {
		int nStereo = 0;
		String tmpCode = code.replaceAll("\\(r\\)", "(s)");
		int fromIndex = 0;
		while ((fromIndex = tmpCode.indexOf("(s)", fromIndex)) != -1) {
			nStereo++;
			fromIndex++;
		}
		return nStereo;
	}

	public static String removeStereoCenters(String code) {
		return code.replaceAll("\\([rs]\\)", "");
	}

	private static String invertStereoCenters(String inCode) {
		return inCode.replaceAll("\\(r\\)", "(q)").replaceAll("\\(s\\)", "(r)")
				.replaceAll("\\(q\\)", "(s)");
	}

	public String[] getSphereCodes(int iAtom, int nMinSpheres, int nMaxSpheres) {
		int nSpheres = nMaxSpheres - nMinSpheres + 1;
		String[] list = new String[nSpheres];
		list[0] = this.getSphereIdCode(iAtom, nMinSpheres);
		int iSphere = 1;
		for (iSphere = 1; iSphere < nSpheres; iSphere++) {
			list[iSphere] = this.getSphereIdCode(iAtom, nMinSpheres + iSphere);
			if (list[iSphere - 1].equals(list[iSphere])) {
				break;
			}
		}
		int nEffectiveSpheres = iSphere == nSpheres ? iSphere : iSphere;

		String[] result = new String[nEffectiveSpheres];
		System.arraycopy(list, 0, result, 0, nEffectiveSpheres);
		return result;
	}

	public String getSphereIdCode(int iCentralAtom, int nSpheres) {
		int nAtoms = this.molecule.getAllAtoms();
		if (iCentralAtom >= nAtoms)
			return null;
		// BFS starting at iCentralAtom
		Arrays.fill(this.discovered, false);
		Arrays.fill(sphereCounts, 0);
		Arrays.fill(atomStack, 0);
		int basePointer = 0;
		int topPointer = 1;
		atomStack[basePointer] = iCentralAtom;
		discovered[iCentralAtom] = true;
		sphereCounts[iCentralAtom] = 1;
		// do dfs search
		while (basePointer < topPointer) {
			int parentAtom = atomStack[basePointer++];
			for (int iNeighbour = 0; iNeighbour < this.molecule
					.getAllConnAtoms(parentAtom); iNeighbour++) {
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
		int nMaxSpheres = MathUtils.findGlobalMaximum(sphereCounts, 0,
				sphereCounts.length) + 1;
		// generate fragment
		// add atoms
		Arrays.fill(reverseIdMap, -1);
		topPointer = 0;
		this.fragment.reset();
		atomMap[topPointer++] = iCentralAtom;
		reverseIdMap[iCentralAtom] = this.fragment.addAtom(this.molecule
				.getAtomicNo(iCentralAtom), this.molecule
				.getAtomCharge(iCentralAtom), this.molecule
				.getAllHydrogens(iCentralAtom), this.molecule
				.isAromaticAtom(iCentralAtom), this.molecule
				.getAtomCIPParity(iCentralAtom));
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			if (discovered[iAtom] && iAtom != iCentralAtom) {
				atomMap[topPointer++] = iAtom;
				reverseIdMap[iAtom] = this.fragment.addAtom(this.molecule
						.getAtomicNo(iAtom),
						this.molecule.getAtomCharge(iAtom), this.molecule
								.getAllHydrogens(iAtom), this.molecule
								.isAromaticAtom(iAtom), this.molecule
								.getAtomCIPParity(iAtom));
			}
		}
		// add bonds
		for (int iBond = 0; iBond < this.molecule.getAllBonds(); iBond++) {
			if (discovered[this.molecule.getBondAtom(0, iBond)]
					&& discovered[this.molecule.getBondAtom(1, iBond)]) {
				int iAtom1 = reverseIdMap[this.molecule.getBondAtom(0, iBond)];
				int iAtom2 = reverseIdMap[this.molecule.getBondAtom(1, iBond)];
				int bondType = this.molecule.getBondType(iBond);
				if (this.molecule.isAromaticBond(iBond)) {
					bondType = 4;
				} else if (bondType == Molecule.cBondTypeUp
						|| bondType == Molecule.cBondTypeDown) {
					bondType = 1;
				}

				this.fragment.addBond(iAtom1, iAtom2, bondType);
			}
		}

		String idCode = this.canonizer.getIdString();
		int[] rankTable = this.canonizer.getIdMapRank2Atom();
		int index = 0;
		while (rankTable[index] != 0)
			index++;
		// String codePointer = "(" + nSpheres + "," + index + ")=";
		String codePointer = "(" + nMaxSpheres + ")=";
		return codePointer + idCode;
	}

	public boolean hasDiastereotopicProtons(int iAtom) {
		if (this.stereoInfo == null)
			return false;
		return this.stereoInfo[iAtom] != HoseStereoInfo.STEREO_TYPE_NONE;
	}

	private void prepareStereoInfo() {
		StereoMolecule compactMolecule = new StereoMolecule();
		this.molecule.copyMolecule(compactMolecule);
		Canonizer canonizer = new Canonizer(compactMolecule);
		int[] graphIndexes = canonizer.getGraphIndexes().clone();
		String idcode = canonizer.getIDCode();
		String coordinates = canonizer.getEncodedCoordinates();
		IDCodeParser parser = new IDCodeParser();
		compactMolecule = parser.getCompactMolecule(idcode, coordinates);
		StereoMolecule enhancedMolecule = new StereoMolecule();
		compactMolecule.copyMolecule(enhancedMolecule);
		HydrogenHandler.addImplicitHydrogens(enhancedMolecule);
		StereoInfo[] enhancedStereoInfo = EPFLUtils
				.determineNonEquivalentProtons(enhancedMolecule,
						compactMolecule);
		//
		int nAtoms = this.molecule.getAllAtoms();
		this.stereoInfo = new HoseStereoInfo[nAtoms];
		Arrays.fill(this.stereoInfo, HoseStereoInfo.STEREO_TYPE_NONE);
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			if (enhancedStereoInfo[iAtom] == StereoInfo.HAS_DIFFERENT_PROTONS) {
				int iHydrogen = -1;
				for (int j = 0; j < enhancedMolecule.getAllConnAtoms(iAtom); j++) {
					if (enhancedMolecule.getAtomicNo(enhancedMolecule
							.getConnAtom(iAtom, j)) == 1) {
						iHydrogen = enhancedMolecule.getConnAtom(iAtom, j);
						break;
					}
				}
				if (iHydrogen != -1) {
					if (enhancedStereoInfo[iHydrogen] == StereoInfo.PRO_E
							|| enhancedStereoInfo[iHydrogen] == StereoInfo.PRO_Z) {
						this.stereoInfo[graphIndexes[iAtom]] = HoseStereoInfo.STEREO_TYPE_EZ;
					} else if (enhancedStereoInfo[iHydrogen] == StereoInfo.PRO_S
							|| enhancedStereoInfo[iHydrogen] == StereoInfo.PRO_R) {
						this.stereoInfo[graphIndexes[iAtom]] = HoseStereoInfo.STEREO_TYPE_RS;
					}
				}
			}
		}

	}

	private void testInputMolecule() {
		int iHydrogen = -1;
		for (int i = 0; i < this.molecule.getAllAtoms(); i++)
			if (this.molecule.getAtomicNo(i) == 1)
				iHydrogen = i;
		if (iHydrogen != -1)
			System.out
					.println("WARNING: this molecule contains explicit hydrogen atoms");
	}

	public HoseStereoInfo getHoseStereoInfo(int iAtom) {
		return this.stereoInfo[iAtom];
	}


}
