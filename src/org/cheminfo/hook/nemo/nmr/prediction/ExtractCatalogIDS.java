package org.cheminfo.hook.nemo.nmr.prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.TreeMap;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;

public class ExtractCatalogIDS {
	private static File abinitioDir = new File(
			"/home/engeler/source/NMRPrediction/molecules");

	public static void main(String[] argv) {
		TreeMap<String, String> abinitioMap = getAbinitioIDS();
		mapToDatabase(abinitioMap);
	}

	private static void mapToDatabase(TreeMap<String, String> abinitioMap) {
		String dbUrl = "jdbc:mysql://localhost:3306/maybridgeNMR";
		String dbUser = "maybridgeNMR";
		String dbPassword = "maybridgeNMR";
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			if (conn != null) {
				PreparedStatement st = conn
						.prepareStatement("SELECT catalogID,value FROM jcamp");
				if (st.execute()) {
					ResultSet resultSet = st.getResultSet();
					while (resultSet.next()) {
						String catalogID = resultSet.getString("catalogID");
						String molfile = resultSet.getString("value");
						String actelionID = molfileToId(molfile);
						if (abinitioMap.containsKey(actelionID)) {
							String moldir = abinitioMap.get(actelionID);
							System.out.println("FOUND: catalogID="+catalogID);
							try {
								PrintWriter printWriter = new PrintWriter(abinitioDir.getAbsolutePath()+"/"+moldir+"/catalogID");
								printWriter.print(catalogID);
								printWriter.close();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						} else {
							System.out.println("WARNING: no info for catalogID={"+catalogID+"}");
						}
					}
				} else {
					SQLWarning warning = st.getWarnings();
					System.out.println(warning.getLocalizedMessage());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static String molfileToId(String molfile) {
		MolfileParser parser = new MolfileParser();
		StereoMolecule molecule = new StereoMolecule();
		if (parser.parse(molecule, molfile)) {
			Canonizer canonizer = new Canonizer(molecule);
			return canonizer.getIDCode();
		} else {
			return null;
		}
	}

	private static TreeMap<String, String> getAbinitioIDS() {
		TreeMap<String, String> abinitioMap = new TreeMap<String, String>();
		String[] moldirs = abinitioDir.list();
		MolfileParser parser = new MolfileParser();
		for (int i = 0; i < moldirs.length; i++) {
			if (moldirs[i].matches("^\\d{5}$")) {
				File molFile = new File(abinitioDir.getAbsolutePath() + "/"
						+ moldirs[i] + "/00000.mol");
				System.out.println("mol="+moldirs[i]);
				if (molFile.exists()) {
					StereoMolecule molecule = new StereoMolecule();
					if (parser.parse(molecule, molFile)) {
						Canonizer canonizer = new Canonizer(molecule);
						abinitioMap.put(canonizer.getIDCode(), moldirs[i]);
					}
				} else {
					System.out.println("WARNING: file does not exist "
							+ molFile.getAbsolutePath());
				}
			}
		}
		return abinitioMap;
	}
}
