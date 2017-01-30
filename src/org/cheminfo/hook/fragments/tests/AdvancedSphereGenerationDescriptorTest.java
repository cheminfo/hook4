package org.cheminfo.hook.fragments.tests;

import org.cheminfo.hook.graph.HOSE.AdvancedSphereDescriptorGenerator;
import org.cheminfo.hook.graph.HOSE.HoseStereoInfo;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.epfl.StereoInfo;

public class AdvancedSphereGenerationDescriptorTest {
	
	private static StereoMolecule getRacemic() {
		StereoMolecule molecule = new StereoMolecule();
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(9);
		molecule.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule.addBond(2, 4, Molecule.cBondTypeSingle);
		Canonizer canonizer = new Canonizer(molecule);
		String idcode = canonizer.getIDCode();
		String coordinates = canonizer.getEncodedCoordinates();
		IDCodeParser parser = new IDCodeParser();
		return parser.getCompactMolecule(idcode,coordinates);
	}
	
	private static StereoMolecule getRForm() {
		StereoMolecule molecule = new StereoMolecule();
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(9);
		molecule.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule.addBond(2, 4, Molecule.cBondTypeUp);
		Canonizer canonizer = new Canonizer(molecule);
		String idcode = canonizer.getIDCode();
		String coordinates = canonizer.getEncodedCoordinates();
		IDCodeParser parser = new IDCodeParser();
		return parser.getCompactMolecule(idcode,coordinates);
	}
	
	private static StereoMolecule getSForm() {
		StereoMolecule molecule = new StereoMolecule();
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(6);
		molecule.addAtom(9);
		molecule.addBond(0, 1, Molecule.cBondTypeSingle);
		molecule.addBond(1, 2, Molecule.cBondTypeSingle);
		molecule.addBond(2, 3, Molecule.cBondTypeSingle);
		molecule.addBond(2, 4, Molecule.cBondTypeDown);
		Canonizer canonizer = new Canonizer(molecule);
		String idcode = canonizer.getIDCode();
		String coordinates = canonizer.getEncodedCoordinates();
		IDCodeParser parser = new IDCodeParser();
		return parser.getCompactMolecule(idcode,coordinates);
	}
	
	public static void main(String[] argv) {
		StereoMolecule currentMolecule = null;
		AdvancedSphereDescriptorGenerator generator = new AdvancedSphereDescriptorGenerator();
		// do racemic
		currentMolecule = getRacemic();
		generator.setMolecule(currentMolecule);
		for (int iAtom = 0; iAtom < currentMolecule.getAllAtoms(); iAtom++) {
			System.out.println("iAtom="+iAtom);
			String code = generator.getSphereIdCode(iAtom, 4);
			System.out.println("\t"+code);
		}
		// do R
		currentMolecule = getRForm();
		generator.setMolecule(currentMolecule);
		for (int iAtom = 0; iAtom < currentMolecule.getAllAtoms(); iAtom++) {
			System.out.println("iAtom="+iAtom);
			String code = generator.getSphereIdCode(iAtom, 4);
			System.out.println("\t"+code);
			if (generator.getHoseStereoInfo(iAtom) == HoseStereoInfo.STEREO_TYPE_EZ) {
				System.out.println("\tpro-E/Z");
				System.out.println("\t"+AdvancedSphereDescriptorGenerator.adaptCodeToStereoChemistry(code,StereoInfo.PRO_E));
			} else if (generator.getHoseStereoInfo(iAtom) == HoseStereoInfo.STEREO_TYPE_RS) {
				System.out.println("\tpro-R/S");
				System.out.println("\t"+AdvancedSphereDescriptorGenerator.adaptCodeToStereoChemistry(code,StereoInfo.PRO_R));
			}
		}
		
		
		

	}

	
	
}
