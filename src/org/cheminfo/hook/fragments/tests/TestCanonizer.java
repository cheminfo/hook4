package org.cheminfo.hook.fragments.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.cheminfo.hook.fragments.Fragment;
import org.cheminfo.hook.fragments.FragmentCanonizer;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

public class TestCanonizer {
	private static String dbUrl = "jdbc:mysql://localhost:3306/CanonizerTest";
	private static String dbUser = "cantest";
	private static String dbPassword = "cantest";

	private static Connection conn = null;

	File inputFile = null;

	Fragment fragment;
	FragmentCanonizer fragmentCanonizer;

	public TestCanonizer(File file) {
		this.inputFile = file;
		this.fragment = new Fragment();
		this.fragmentCanonizer = new FragmentCanonizer();
		this.fragmentCanonizer.setFragment(this.fragment);
	}

	public void runTest() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

			BufferedReader reader = new BufferedReader(new FileReader(
					this.inputFile));
			String inputLine;
			int iAdded = 0;
			while ((inputLine = reader.readLine()) != null) {
				String[] tokens = inputLine.split("\t");
				String actelionId = tokens[2];
				String smilesString = tokens[3];
				if (smilesString.indexOf('.') != -1)
					continue;
				String epflId = this.getEpflId(actelionId);
				PreparedStatement st = conn
						.prepareStatement("INSERT INTO molecules (actelionID,epflID) VALUES(?,?)");
				st.setString(1, tokens[2]);
				st.setString(2, epflId);
				st.execute();
				st.close();
				iAdded++;
				if (iAdded % 10 == 0)
					System.out.println("added=" + iAdded);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getEpflId(String actelionId) {
		IDCodeParser parser = new IDCodeParser();
		StereoMolecule molecule = new StereoMolecule();
		parser.parse(molecule, actelionId);

		int nAtoms = molecule.getAllAtoms();
		this.fragment.reset();
		for (int iAtom = 0; iAtom < nAtoms; iAtom++) {
			this.fragment.addAtom(molecule.getAtomicNo(iAtom), molecule
					.getAtomCharge(iAtom), molecule.getAllHydrogens(iAtom),
					molecule.isAromaticAtom(iAtom), molecule
							.getAtomCIPParity(iAtom));
		}
		int nBonds = molecule.getAllBonds();
		int iAtom1, iAtom2, bondType;
		for (int iBond = 0; iBond < nBonds; iBond++) {
			iAtom1 = molecule.getBondAtom(0, iBond);
			iAtom2 = molecule.getBondAtom(1, iBond);
			bondType = molecule.getBondType(iBond);
			if (molecule.isAromaticBond(iBond))
				bondType = 4;
			this.fragment.addBond(iAtom1, iAtom2, bondType);
		}
		return this.fragmentCanonizer.getIdString();
	}

	public static void main(String[] argv) {
		if (argv.length != 1) {
			System.err.println("Usage: <executable> /molecules.tab");
			System.exit(-1);
		}
		File inputFile = new File(argv[0]);
		if (!inputFile.equals(inputFile)) {
			System.err.println("File " + inputFile.getAbsolutePath()
					+ " does not exist");
			System.exit(-1);

		}
		TestCanonizer canonizerTester = new TestCanonizer(inputFile);
		canonizerTester.runTest();
	}
}
