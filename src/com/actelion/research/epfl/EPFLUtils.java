package com.actelion.research.epfl;

import com.actelion.research.chem.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class EPFLUtils {
	private static final boolean DEBUG=false;

	public static int[] getAdjecencyMatrix(ExtendedMolecule molecule) {
		int nAtoms = molecule.getAllAtoms();
		int[] adjecencyMatrix = new int[nAtoms * nAtoms];
		Arrays.fill(adjecencyMatrix, Integer.MAX_VALUE);
		int nBonds = molecule.getAllBonds();
		int iAtom1, iAtom2;
		for (int iBond = 0; iBond < nBonds; iBond++) {
			iAtom1 = molecule.getBondAtom(0, iBond);
			iAtom2 = molecule.getBondAtom(1, iBond);
			adjecencyMatrix[iAtom1 * nAtoms + iAtom2] = 1;
			adjecencyMatrix[iAtom2 * nAtoms + iAtom1] = 1;
		}
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			adjecencyMatrix[iAtom * (nAtoms + 1)] = 0;
		}
		return adjecencyMatrix;
	}

	public static int[] getGeneralizedDistanceMatrix(ExtendedMolecule molecule) {
		int nAtoms = molecule.getAllAtoms();
		int[] distanceMatrix = new int[nAtoms * nAtoms];
		Arrays.fill(distanceMatrix, -1);
		int nBonds = molecule.getAllBonds();
		for (int iBond = 0; iBond < nBonds; iBond++) {
			int iAtom1 = molecule.getBondAtom(0, iBond);
			int iAtom2 = molecule.getBondAtom(1, iBond);
			int bondType = molecule.getBondType(iBond);
			if (molecule.isAromaticBond(iBond)) {
				bondType = 4;
			} else if (bondType == Molecule.cBondTypeDown
					|| bondType == Molecule.cBondTypeUp) {
				bondType = 1;
			}
			distanceMatrix[iAtom1 * nAtoms + iAtom2] = bondType;
			distanceMatrix[iAtom2 * nAtoms + iAtom1] = bondType;
		}
		return distanceMatrix;
	}

	public static StereoMolecule getCanonicalForm(StereoMolecule molecule) {
		Canonizer canonizer = new Canonizer(molecule);
		String idCode = canonizer.getIDCode();
		
		if(DEBUG) System.out.println("EPFLUtils: idCode: "+idCode);
		
		String encodedCoordinates = canonizer.getEncodedCoordinates(true);
		IDCodeParser parser = new IDCodeParser();
		StereoMolecule canonizedMolecule = parser.getCompactMolecule(idCode, encodedCoordinates);
		return canonizedMolecule;
	}


	public static String getMolFile(StereoMolecule molecule) {
		MolfileCreator creator = new MolfileCreator(molecule);
		return creator.getMolfile();
	}

	public static int[] getProtonSymmRanksBySubstitution(StereoMolecule molecule) {
		int nAtoms = molecule.getAllAtoms();
		// substitute by element maxAtomicNo+1
		int targetAtomicNo = EPFLUtils.getReplacementAtom(molecule);
		for (int atm = 0; atm < nAtoms; atm++)
			if (molecule.getAtomicNo(atm) == 1)
				molecule.setAtomicNo(atm, targetAtomicNo);
		Canonizer canonizer = new Canonizer(molecule,
				Canonizer.CREATE_SYMMETRY_RANK);
		int[] symRanks = new int[nAtoms];
		for (int atm = 0; atm < nAtoms; atm++) {
			symRanks[atm] = canonizer.getSymmetryRank(atm);
			if (molecule.getAtomicNo(atm) == targetAtomicNo)
				molecule.setAtomicNo(atm, 1);
		}
		return symRanks;
	}

	public static StereoInfo[] determineNonEquivalentProtons(
			StereoMolecule enhancedMolecule, StereoMolecule compactMolecule) {

		int nAtoms = compactMolecule.getAllAtoms();
		compactMolecule.ensureHelperArrays(2);
		for (int j = 0; j < compactMolecule.getAllAtoms(); j++) {
			compactMolecule.setAtomX(j, enhancedMolecule.getAtomX(j));
			compactMolecule.setAtomY(j, enhancedMolecule.getAtomY(j));
			compactMolecule.setAtomZ(j, enhancedMolecule.getAtomZ(j));
		}
		StereoInfo[] stereoInfo = new StereoInfo[enhancedMolecule.getAllAtoms()];
		Arrays.fill(stereoInfo, StereoInfo.UNDEF);
		int replacementAtom = EPFLUtils.getReplacementAtom(compactMolecule);
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			switch (compactMolecule.getAllHydrogens(iAtom)) {
			case 1:
				if (compactMolecule.getAtomicNo(iAtom) == 7)
					EPFLUtils.testAmideProtons(enhancedMolecule, iAtom, stereoInfo);
				break;
			case 2:
				switch (compactMolecule.getAtomicNo(iAtom)) {
				case 7:
					EPFLUtils.testAmideProtons(enhancedMolecule, iAtom, stereoInfo);
					break;
				default:
					EPFLUtils.testDiasterotopic(enhancedMolecule, compactMolecule, iAtom, replacementAtom,
									stereoInfo);
					break;
				}
				break;
			}
		}
		return stereoInfo;
	}

	private static void testAmideProtons(StereoMolecule enhancedMolecule,
			int iAtom, StereoInfo[] stereoInfo) {
		if (EPFLUtils.DEBUG)
			System.out.println("testAmideProtons");
		if (enhancedMolecule.getAllConnAtoms(iAtom) != 3)
			return;
		int[] substituents = new int[3];
		int[] atomicNos = new int[3];
		int nSubstituents = 0;
		int iCarbonylOxygen = -1;
		for (int i = 0; i < enhancedMolecule.getAllConnAtoms(iAtom); i++) {
			int iConn = enhancedMolecule.getConnAtom(iAtom, i);
			int atomicNo = enhancedMolecule.getAtomicNo(iConn);
			if (EPFLUtils.DEBUG)
				System.out.println(atomicNo);
			if (atomicNo == 6
					&& iCarbonylOxygen == -1
					&& (iCarbonylOxygen = EPFLUtils.getCarbonylOxygen(
							enhancedMolecule, iConn)) != -1) {
			} else {
				substituents[nSubstituents] = iConn;
				atomicNos[nSubstituents] = atomicNo;
				nSubstituents++;
			}
		}
		if (iCarbonylOxygen == -1 || nSubstituents != 2)
			return;
		//
		double d1 = EPFLUtils.getDistance(enhancedMolecule, iCarbonylOxygen,
				substituents[0]);
		double d2 = EPFLUtils.getDistance(enhancedMolecule, iCarbonylOxygen,
				substituents[1]);
		if (d1 < d2) {
			// d1 - pro Z
			if (atomicNos[0] == 1)
				stereoInfo[substituents[0]] = StereoInfo.PRO_Z;
			// d2 - pro E
			if (atomicNos[1] == 1)
				stereoInfo[substituents[1]] = StereoInfo.PRO_E;
		} else {
			// d1 - pro E
			if (atomicNos[0] == 1)
				stereoInfo[substituents[0]] = StereoInfo.PRO_E;
			// d2 - pro Z
			if (atomicNos[1] == 1)
				stereoInfo[substituents[1]] = StereoInfo.PRO_Z;
		}

		return;
	}

	/***************************************************************************
	 * Actually returns the square of the distance. sqrt is a monotonous
	 * function of and just wastes time!
	 * 
	 * @param mol
	 * @param iAtomA
	 * @param iAtomB
	 * @return
	 */
	public static double getDistance(StereoMolecule mol, int iAtomA, int iAtomB) {
		double xA = mol.getAtomX(iAtomA);
		double yA = mol.getAtomY(iAtomA);
		double zA = mol.getAtomZ(iAtomA);
		double xB = mol.getAtomX(iAtomB);
		double yB = mol.getAtomY(iAtomB);
		double zB = mol.getAtomZ(iAtomB);
		return (xA - xB) * (xA - xB) + (yA - yB) * (yA - yB) + (zA - zB)
				* (zA - zB);
	}

	public static int getCarbonylOxygen(StereoMolecule mol, int iCarbon) {
		for (int i = 0; i < mol.getAllConnAtoms(iCarbon); i++) {
			int iBond = mol.getConnBond(iCarbon, i);
			if (mol.getBondType(iBond) == Molecule.cBondTypeDouble) {
				if (mol.getBondAtom(0, iBond) == iCarbon) {
					int iatm = mol.getBondAtom(1, iBond);
					if (mol.getAtomicNo(iatm) == 8)
						return iatm;
				} else if (mol.getBondAtom(1, iBond) == iCarbon) {
					int iatm = mol.getBondAtom(0, iBond);
					if (mol.getAtomicNo(iatm) == 8)
						return iatm;
				}
			}
		}
		return -1;
	}

	private static boolean testDiasterotopic(StereoMolecule enhancedMolecule,
			StereoMolecule compactMolecule, int iAtom, int replacementAtom,
			StereoInfo[] stereoInfo) {
		int nConnectedAtoms = enhancedMolecule.getAllConnAtoms(iAtom);
		if (EPFLUtils.DEBUG)
			System.out.println("nConnectedAtoms" + nConnectedAtoms);
		int[] hydrogens = new int[2];
		int i = 0;
		//
		for (int iConn = 0; iConn < nConnectedAtoms; iConn++)
			if (enhancedMolecule.getAtomicNo(enhancedMolecule.getConnAtom(iAtom, iConn)) == 1)
				hydrogens[i++] = enhancedMolecule.getConnAtom(iAtom, iConn);
		// molecule 1
		StereoMolecule mol1 = new StereoMolecule();
		EPFLUtils.copyMolecule(compactMolecule, mol1, false);
		int iNew1 = mol1.addAtom(enhancedMolecule.getAtomX(hydrogens[0]),
				enhancedMolecule.getAtomY(hydrogens[0]), enhancedMolecule.getAtomZ(hydrogens[0]));
		mol1.setAtomicNo(iNew1, replacementAtom);
		if (nConnectedAtoms == 4)
			mol1.addBond(iAtom, iNew1, Molecule.cBondTypeUp);
		else
			mol1.addBond(iAtom, iNew1, Molecule.cBondTypeSingle);
		Canonizer c1 = new Canonizer(mol1);
		String id1 = c1.getIDCode();
		if (EPFLUtils.DEBUG) {
			System.out.println("mol1");
			EPFLUtils.getNumberOfStereoCenters((new IDCodeParser()).getCompactMolecule(id1, c1.getEncodedCoordinates()));
		}
		// molecule 2
		StereoMolecule mol2 = new StereoMolecule();
		EPFLUtils.copyMolecule(compactMolecule, mol2, false);
		int iNew2 = mol2.addAtom(enhancedMolecule.getAtomX(hydrogens[1]),
				enhancedMolecule.getAtomY(hydrogens[1]), enhancedMolecule.getAtomZ(hydrogens[1]));
		mol2.setAtomicNo(iNew2, replacementAtom);
		if (nConnectedAtoms == 4)
			mol2.addBond(iAtom, iNew2, Molecule.cBondTypeDown);
		else
			mol2.addBond(iAtom, iNew2, Molecule.cBondTypeSingle);
		Canonizer c2 = new Canonizer(mol2);
		String id2 = c2.getIDCode();
		int[] graphIndexes2 = c1.getGraphIndexes();
		int iTargetID2 = graphIndexes2[iAtom];
		if (EPFLUtils.DEBUG)
			System.out.println("mol2");
		if (EPFLUtils.DEBUG)
			System.out.println("id1 = " + id1 + " id2 = " + id2);
		boolean different = false;
		if (id1.equals(id2)) {
			if (EPFLUtils.DEBUG) System.out.println("not different");
			different = false;
		} else {
			// protons are not equivalent
			if (EPFLUtils.DEBUG)
				System.out.println("different");
			different = true;
			// for some reason stereo information is not updated in the original
			// molecule
			mol2 = (new IDCodeParser()).getCompactMolecule(id2, c2
					.getEncodedCoordinates());
			int nStereoCenters = EPFLUtils.getNumberOfStereoCenters(mol2);
			if (stereoInfo != null) {
				switch (enhancedMolecule.getAllConnAtoms(iAtom)) {
				case 4: // normal stereo
				{
					if (nStereoCenters > 1) {
						stereoInfo[iAtom] = StereoInfo.HAS_DIFFERENT_PROTONS;
						if (mol2.getAtomCIPParity(iTargetID2) == Molecule.cAtomCIPParityRorM) {
							if (EPFLUtils.DEBUG)
								System.out.println("first hydrogen pro r");
							stereoInfo[hydrogens[0]] = StereoInfo.PRO_R;
							stereoInfo[hydrogens[1]] = StereoInfo.PRO_S;
						} else if (mol2.getAtomCIPParity(iTargetID2) == Molecule.cAtomCIPParitySorP) {
							if (EPFLUtils.DEBUG)
								System.out.println("first hydrogen pro s");
							stereoInfo[hydrogens[0]] = StereoInfo.PRO_S;
							stereoInfo[hydrogens[1]] = StereoInfo.PRO_R;
						}
					}
				}
					break;
				case 3: // double bond
				{
					stereoInfo[iAtom] = StereoInfo.HAS_DIFFERENT_PROTONS;
					int iNeighbour = -1;
					for (int j = 0; j < mol2.getAllConnAtoms(iTargetID2); j++) {
						if (mol2.getAtomicNo(mol2.getConnAtom(iTargetID2, j)) != replacementAtom) {
							iNeighbour = mol2.getConnAtom(iTargetID2, j);
							break;
						}
					}
					if (iNeighbour != -1) {
						for (int iBond = 0; iBond < mol2.getAllBonds(); iBond++) {

							if ((mol2.getBondAtom(0, iBond) == iTargetID2 && mol2
									.getBondAtom(1, iBond) == iNeighbour

							)
									|| (mol2.getBondAtom(1, iBond) == iTargetID2 && mol2
											.getBondAtom(0, iBond) == iNeighbour)) {
								int bondParity = mol2.getBondParity(iBond);
								if (bondParity == Molecule.cBondCIPParityEorP) {
									stereoInfo[hydrogens[1]] = StereoInfo.PRO_E;
									stereoInfo[hydrogens[0]] = StereoInfo.PRO_Z;
									break;
								} else if (bondParity == Molecule.cBondCIPParityZorM) {
									stereoInfo[hydrogens[1]] = StereoInfo.PRO_Z;
									stereoInfo[hydrogens[0]] = StereoInfo.PRO_E;
									break;
								}
							}

						}
					}
				}
					break;
				}
			}
		}
		return different;
	}

	/**
	 * Determines the maximum atomic Number not assigned
	 * 
	 * @param molecule
	 * @return
	 */
	public static int getReplacementAtom(StereoMolecule molecule) {
		int nAtoms = molecule.getAllAtoms();
		int maxAtomicNo = 0;
		for (int atm = 0; atm < nAtoms; atm++) {
			int atomicNo = molecule.getAtomicNo(atm);
			if (atomicNo > maxAtomicNo)
				maxAtomicNo = atomicNo;
		}
		return maxAtomicNo + 1;
	}

	public static void copyMolecule(StereoMolecule src, StereoMolecule dest, boolean noCoords) {
		src.copyMolecule(dest);
		/*
		dest.setChirality(src.getChirality());
		for (int iAtom = 0; iAtom < src.getAllAtoms(); iAtom++) {
			dest.addAtom(src.getAtomicNo(iAtom));
			dest.setAtomMass(iAtom, src.getAtomMass(iAtom));
			dest.setAtomCharge(iAtom, src.getAtomCharge(iAtom));
			if (!noCoords) {
				dest.setAtomX(iAtom, src.getAtomX(iAtom));
				dest.setAtomY(iAtom, src.getAtomY(iAtom));
				dest.setAtomZ(iAtom, src.getAtomZ(iAtom));
			}
	//		dest.setAtomStereoCenter(iAtom, src.isAtomStereoCenter(iAtom));
			dest.setAtomCIPParity(iAtom, src.getAtomCIPParity(iAtom));
			dest.setAtomESR(iAtom, src.getAtomESRType(iAtom), src.getAtomESRGroup(iAtom));
		}
		for (int iBond = 0; iBond < src.getAllBonds(); iBond++) {
			int ia0 = src.getBondAtom(0, iBond);
			int ia1 = src.getBondAtom(1, iBond);
			int bondType = src.getBondType(iBond);
			dest.addBond(ia0, ia1, bondType);
			dest.setBondCIPParity(iBond, src.getBondCIPParity(iBond));
		}
		*/
	}

	public static int getNumberOfStereoCenters(StereoMolecule molecule) {
		int nStereo = 0;
		for (int iAtom = 0; iAtom < molecule.getAllAtoms(); iAtom++) {
			switch (molecule.getAtomCIPParity(iAtom)) {
			case Molecule.cAtomCIPParityRorM:
				if (DEBUG)
					System.out.println(iAtom + " with R parity");
				nStereo++;
				break;
			case Molecule.cAtomCIPParitySorP:
				if (DEBUG)
					System.out.println(iAtom + " with S parity");
				nStereo++;
				break;
			case Molecule.cAtomCIPParityProblem:
				System.out.println("WARNING: parity problem");
				break;

			}
		}
		return nStereo;
	}

	public static void createMoleculeImageFile(StereoMolecule fragmentMolecule,
			String file, int width, int height) {
		Depictor2D depictor = new Depictor2D(fragmentMolecule);
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		Rectangle2D.Double viewRect = new Rectangle2D.Double();
		viewRect.setRect(0, 0, width, height);
		depictor.simpleUpdateCoords(viewRect, Depictor.cModeInflateToMaxAVBL);
		depictor.paint(g2d);
		try {
			ImageIO.write(bufferedImage, "png", new File(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
