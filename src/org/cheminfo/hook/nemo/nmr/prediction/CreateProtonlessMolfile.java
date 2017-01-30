package org.cheminfo.hook.nemo.nmr.prediction;

import java.io.File;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

public class CreateProtonlessMolfile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			MolfileParser parser = new MolfileParser();
			StereoMolecule mol = new StereoMolecule();
			if (parser.parse(mol, new File(args[0]))) {
				Canonizer canonizer = new Canonizer(mol);
				String idcode = canonizer.getIDCode();
				String coords = canonizer.getEncodedCoordinates();
				IDCodeParser idparser = new IDCodeParser();
				StereoMolecule compactMolecule = idparser.getCompactMolecule(idcode, coords);
				MolfileCreator creator = new MolfileCreator(compactMolecule);
				System.out.print(creator.getMolfile());
			}
		}
	}

}
