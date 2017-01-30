package org.cheminfo.hook.nemo.nmr;

import java.security.MessageDigest;
import java.util.TreeSet;

import org.cheminfo.hook.graph.HOSE.AdvancedSphereDescriptorGenerator;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.StereoInfo;
import com.twmacinta.util.MD5;

public class HoseGeneratorInterface {
	private ActMoleculeDisplay molDisplay = null;
	private AdvancedSphereDescriptorGenerator generator = null;
	private StereoMolecule compactMolecule = null;
	private StereoMolecule enhancedMolecule = null;

	private static final boolean obfuscateCodes = true;

	private MessageDigest md = null;


	public HoseGeneratorInterface(ActMoleculeDisplay molDisplay) {
		this.molDisplay = molDisplay;
		if (molDisplay != null) {
			this.compactMolecule = this.molDisplay.getCompactMolecule();
			this.enhancedMolecule = this.molDisplay.getEnhancedMolecule();
			this.generator = new AdvancedSphereDescriptorGenerator(
					this.compactMolecule);

		}

	}

	public void getAllHoseCodesByType(TreeSet<String> resultSet, Nucleus type) {
		if (type == Nucleus.NUC_1H) {
			for (int iAtom = 0; iAtom < this.enhancedMolecule.getAllAtoms(); iAtom++) {
				if (this.enhancedMolecule.getAtomicNo(iAtom) == 1) {
					String[] list = this.getHydrogenCodes(iAtom);
					for (int i = 0; i < list.length; i++) {
						resultSet.add(list[i]);
					}
				}
			}
		} else {
			int atomicNo = type.getAtomicNo();
			for (int iAtom = 0; iAtom < this.compactMolecule.getAllAtoms(); iAtom++) {
				if (this.compactMolecule.getAtomicNo(iAtom) == atomicNo) {
					String[] list = this.getHeavyAtomCodes(iAtom);
					for (int i = 0; i < list.length; i++) {
							resultSet.add(list[i]);
					}
				}
			}
		}
	}

	public String[] getHydrogenCodes(int iAtom) {
		int iHeavyAtom = this.enhancedMolecule.getConnAtom(iAtom, 0);
		String[] list = this.generator.getSphereCodes(iHeavyAtom,
				AdvancedSphereDescriptorGenerator.nMinSpheres,
				AdvancedSphereDescriptorGenerator.nMaxSpheres);
		for (int iToken = 0; iToken < list.length; iToken++) {
			// make a hydrogen code
			list[iToken] = AdvancedSphereDescriptorGenerator
					.adaptCodeToHydrogen(list[iToken]);
			int nStereo = AdvancedSphereDescriptorGenerator
					.countStereoCenters(list[iToken]);
			if (this.molDisplay.getStereoInfo(iHeavyAtom) == StereoInfo.HAS_DIFFERENT_PROTONS) {
				StereoInfo stereoInfo = this.molDisplay.getStereoInfo(iAtom);
				if (stereoInfo == StereoInfo.PRO_E
						|| stereoInfo == StereoInfo.PRO_Z) {
					// E/Z
					list[iToken] = AdvancedSphereDescriptorGenerator
							.adaptCodeToStereoChemistry(list[iToken],
									stereoInfo);
				} else {
					// R/S parity
					if (nStereo == 0) {
						list[iToken] = AdvancedSphereDescriptorGenerator
								.removeStereoCenters(list[iToken]);
					} else {
						list[iToken] = AdvancedSphereDescriptorGenerator
								.adaptCodeToStereoChemistry(list[iToken],
										stereoInfo);
					}
				}
			} else {
				if (nStereo == 1)
					list[iToken] = AdvancedSphereDescriptorGenerator
							.removeStereoCenters(list[iToken]);
			}
			if (HoseGeneratorInterface.obfuscateCodes)
				list[iToken] = this.obfsucatHoseCode(list[iToken]);
		}
		return list;
	}

	public String[] getHeavyAtomCodes(int iAtom) {
		String[] list = this.generator.getSphereCodes(iAtom,
				AdvancedSphereDescriptorGenerator.nMinSpheres,
				AdvancedSphereDescriptorGenerator.nMaxSpheres);
		for (int iToken = 0; iToken < list.length; iToken++) {
			if (AdvancedSphereDescriptorGenerator
					.countStereoCenters(list[iToken]) == 1) {
				list[iToken] = AdvancedSphereDescriptorGenerator
						.removeStereoCenters(list[iToken]);
			}
			if (HoseGeneratorInterface.obfuscateCodes)
				list[iToken] = this.obfsucatHoseCode(list[iToken]);
		}
		return list;
	}

	public String[] getHoseCodes4Atom(int iAtom) {
		if (iAtom >= this.compactMolecule.getAllAtoms()) {
			return this.getHydrogenCodes(iAtom);
		} else {
			return this.getHeavyAtomCodes(iAtom);
		}
	}

	public String getHoseCodes4Assignment(int iAtom) {
		String[] list = null;
		if (iAtom >= this.compactMolecule.getAllAtoms()) {
			list = this.getHydrogenCodes(iAtom);
		} else {
			list = this.getHeavyAtomCodes(iAtom);
		}
		String result = "";
		if (list != null)
			for (String s : list)
				result += "\t" + s;
		return result;
	}

	private String obfsucatHoseCode(String input) {		
		MD5 md5 = new MD5();
		md5.Update(input);
		return md5.asHex();
		// String result = null;
		// if (this.encoder == null)
		// this.encoder = new BASE64Encoder();
		// if (this.md == null) {
		// try {
		// this.md = MessageDigest.getInstance("MD5",new Provider());
		// } catch (NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// }
		//
		// }
		// if (this.md != null) {
		// this.md.reset();
		// byte[] inBytes = input.getBytes();
		// byte[] outBytes = this.md.digest(inBytes);
		// result = this.encoder.encode(outBytes);
		// } else {
		// System.out.println("failed to process hose code");
		// result = "";
		// }
		// return result;
	}

}
