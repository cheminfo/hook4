package org.cheminfo.hook.graph.HOSE.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cheminfo.hook.graph.HOSE.AdvancedSphereDescriptorGenerator;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.StereoMolecule;

public class Chemexper2HoseCode {
	final static String inFile = "/home/engeler/source/mol.tab";
	final static String dbUrl = "jdbc:mysql://localhost:3306/hosedb";
	final static String dbUser = "NMRPrediction";
	final static String dbPassword = "NMRPrediction";

	final static int minSphereSize = 2;
	final static int maxSphereSize = 6;

	static Connection conn = null;

	static int nMoleculesDone;

	static int nHoseCodesDone;

	static String actelionID;
	static String lastActelionID;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			nMoleculesDone = 0;
			nHoseCodesDone = 0;
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			BufferedReader reader = new BufferedReader(new FileReader(inFile));
			String line;
			IDCodeParser parser = new IDCodeParser();
			AdvancedSphereDescriptorGenerator generator = new AdvancedSphereDescriptorGenerator();
			lastActelionID = "";
			while ((actelionID = reader.readLine()) != null) {
				if (lastActelionID.equals(actelionID))
					continue;
				else
					lastActelionID = actelionID;
				StereoMolecule molecule = new StereoMolecule();
				parser.parse(molecule, actelionID);
				generator.setMolecule(molecule);
				int moleculeID = Chemexper2HoseCode.getMoleculeID(conn,
						actelionID);
				if (moleculeID == -1)
					continue;
				for (int iSphereSize = minSphereSize; iSphereSize <= maxSphereSize; iSphereSize++) {
					TreeSet<String> hoseCodes = new TreeSet<String>();
					TreeMap<Integer, String> carbons = generator
							.getAllSphereIdCodesByAtomicNo(6, iSphereSize);
					hoseCodes.addAll(carbons.values());
					Iterator<String> iterator = hoseCodes.iterator();
					while (iterator.hasNext()) {
						String hoseCode = iterator.next();
						int hoseCodeID = Chemexper2HoseCode.getHoseCodeID(conn,
								hoseCode, iSphereSize);
						if (hoseCodeID == -1)
							continue;
						Chemexper2HoseCode.createAssociation(conn, moleculeID,
								hoseCodeID);
					}
				}
				nMoleculesDone++;
				if (nMoleculesDone % 10 == 0)
					System.out.println("molecules done: " + nMoleculesDone);

			}
			reader.close();
			conn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
	//
	private static int getMoleculeID(Connection conn, String actelionId) {
		int result = -1;
		PreparedStatement st = null;
		try {

			st = conn
					.prepareStatement("SELECT _moleculeID FROM molecules WHERE actelionID = ?");
			st.setString(1, actelionId);
			if (st.execute()) {
				ResultSet rs = st.getResultSet();
				if (rs.next()) {
					result = rs.getInt(1);
				}
				st.close();
				st = null;
				if (result == -1) {
					st = conn
							.prepareStatement("INSERT INTO molecules (actelionID) VALUES(?)");
					st.setString(1, actelionID);
					st.execute();
					st.close();
					st = null;
					st = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					if (st.execute()) {
						rs = st.getResultSet();
						if (rs.next()) {
							result = rs.getInt(1);
						}
					}
					st.close();
					st = null;
				} else {
					return -1;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (st != null)
				st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static int getHoseCodeID(Connection conn, String hoseCode,
			int nSpheres) {
		int result = -1;
		PreparedStatement st = null;
		try {
			st = conn
					.prepareStatement("SELECT _hoseCodeID FROM hoseCodes WHERE hoseCode = '"
							+ hoseCode + "'");
			if (st.execute()) {
				ResultSet rs = st.getResultSet();
				if (rs.next()) {
					result = rs.getInt(1);
					st.close();
					st = null;
				} else {
					st.close();
					st = conn
							.prepareStatement("INSERT INTO hoseCodes (hoseCode,nSpheres) VALUES('"
									+ hoseCode + "','" + nSpheres + "')");
					st.execute();
					nHoseCodesDone++;
					// if (nHoseCodesDone % 10 == 0)
					System.out.println("nHoseCodesDone: " + nHoseCodesDone);
					st.close();
					st = null;
					st = conn.prepareStatement("SELECT LAST_INSERT_ID()");
					if (st.execute()) {
						rs = st.getResultSet();
						if (rs.next()) {
							result = rs.getInt(1);
						}
					}
					st.close();
					st = null;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (st != null)
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return result;
	}

	private static void createAssociation(Connection conn, int moleculeID,
			int hoseCodeID) {
		PreparedStatement st = null;
		try {
			st = conn
					.prepareStatement("INSERT INTO hoseCodeAssociations (moleculeID,hoseCodeID) VALUES('"
							+ moleculeID + "','" + hoseCodeID + "')");
			st.execute();
			st.close();
			st = null;
		} catch (SQLException e) {
			if (e.getErrorCode() != 1062) {
				System.out.println("failed for moleculeID=[" + moleculeID
						+ "],hoseCodeID=[" + hoseCodeID + "]");
				e.printStackTrace();
			}
		}
		if (st != null)
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
}
