package org.cheminfo.hook.graph.HOSE.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

public class AtomStats {
	final static String inFile = "/home/engeler/source/mol.tab";
	// final static String dbUrl = "jdbc:mysql://localhost:3306/hosedb";
	// final static String dbUser = "NMRPrediction";
	// final static String dbPassword = "NMRPrediction";
	//
	// final static int minSphereSize = 2;
	// final static int maxSphereSize = 6;
	//	
	// static Connection conn = null;
	//
	static int nMoleculesDone;

	static int nHoseCodesDone;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long nCarbons = 0;
		long nHydrogens = 0;
		long nOverallAtoms = 0;
		try {
			nMoleculesDone = 0;
			nHoseCodesDone = 0;
			// Class.forName("com.mysql.jdbc.Driver");
			// conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			BufferedReader reader = new BufferedReader(new FileReader(inFile));
			String line;
			IDCodeParser parser = new IDCodeParser();
			// AdvancedSphereDescriptorGenerator generator = new
			// AdvancedSphereDescriptorGenerator();
			String lastActelionId = "";
			while ((line = reader.readLine()) != null) {
				String actelionId = line;
				if (lastActelionId.equals(actelionId)) {
					System.out.println("\tskip");
					continue;
				}
				lastActelionId = actelionId;
				StereoMolecule molecule = new StereoMolecule();
				parser.parse(molecule, actelionId);
				// generator.setMolecule(molecule);
				molecule.ensureHelperArrays(3);
				int nAtoms = molecule.getAllAtoms();
				nOverallAtoms += nAtoms;
				for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
					if (molecule.getAtomicNo(iAtom) == 6) {
						nCarbons++;
					}
					if (molecule.getAllHydrogens(iAtom) > 0)
						nHydrogens += molecule.getAllHydrogens(iAtom);
				}
				nMoleculesDone++;
				if (nMoleculesDone % 10 == 0)
					System.out.println("molecules done: " + nMoleculesDone);
			}
			reader.close();
			// conn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		System.out.println("molecules: " + nMoleculesDone);
		System.out.println("nHydrogens: " + nHydrogens);
		System.out.println("nCarbons: " + nCarbons);
		System.out.println("nOverallAtoms" + nOverallAtoms);
	}

	// private static void addHoseCode(String code) {
	// PreparedStatement st = null;
	// try {
	// st = conn.prepareStatement("INSERT INTO codes (code) VALUES(?)");
	// st.setString(1, code);
	// if (st.executeUpdate() == 1) {
	// nHoseCodesDone++;
	// if (nHoseCodesDone % 10 == 0)
	// System.out.println("\those codes done: " + nHoseCodesDone);
	// }
	// } catch (SQLException e) {
	// }
	// if (st != null) {
	// try {
	// st.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	// }

}
