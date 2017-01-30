package org.cheminfo.hook.nemo.nmr.prediction;


public class NMRPredictionClient {
//	public static String serverUrl = "10.0.128.1";
//
//	private InteractiveSurface interactions = null;
//	private Nucleus nucleus = null;
//	private boolean twoDimensional = false;
//	private String oneDimPrediction = null;
//	private String requestedType = null;
//	private String errorString = null;
//	private boolean returnIncomplete = false;
//
//	public String getErrorString() {
//		return errorString;
//	}
//
//	public String getOneDimPrediction() {
//		return oneDimPrediction;
//	}
//
//	public NMRPredictionClient(InteractiveSurface interactions,
//			String strNucleus) {
//		this.interactions = interactions;
//		this.requestedType = strNucleus;
//		this.nucleus = Nucleus.determineNucleus(strNucleus);
//		if (nucleus == Nucleus.UNDEF)
//			this.twoDimensional = true;
//	}
//
//	public boolean predictNMRSpectrum() {
//		BasicEntity entity = interactions.getEntityByName("molDisplay");
//		if (entity != null) {
//			ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) entity;
//			StereoMolecule molecule = molDisplay.getStereoMolecule();
//			molecule.ensureHelperArrays(3);
//			if (!this.isTwoDimensional()) {
//				String queryString = NMRPredictionClient.getHoseCodes4Query(
//						molecule, this.nucleus);
//				String result = NMRPredictionClient
//						.queryPrediction(queryString);
//				if ((this.oneDimPrediction = this.rewritePrediction(molDisplay
//						.getStereoMolecule(), result)) == null) {
//					return false;
//				}
//			} else {
//				String queryString;
//				if (this.requestedType.equals("COSY")) {
//					// the spectrum is homonuclear
//					queryString = NMRPredictionClient.getHoseCodes4Query(
//							molecule, Nucleus.NUC_1H);
//				} else {
//					//
//					queryString = "";
//					queryString += NMRPredictionClient.getHoseCodes4Query(
//							molecule, Nucleus.NUC_1H);
//					queryString += NMRPredictionClient.getHoseCodes4Query(
//							molecule, Nucleus.NUC_13C);
//				}
//				String strPredictionData = NMRPredictionClient
//						.queryPrediction(queryString);
//				PredictionData predictionData = this
//						.parseTwoDimensionalData(strPredictionData);
//				if (predictionData == null) {
//					this.errorString = "no 2d prediction data";
//					return false;
//				}
//				if (interactions.getActiveDisplay() instanceof SpectraDisplay) {
//					SpectraDisplay spectraDisplay = (SpectraDisplay) interactions
//							.getActiveDisplay();
//					Spectra spectra = spectraDisplay.getFirstSpectra();
//					if (spectraDisplay.is2D()) {
//						spectra.addPrediction(predictionData, molDisplay);
//					}
//				} else {
//					this.errorString = "error accessing SpectraDisplay";
//					return false;
//				}
//			}
//			return true;
//		}
//		return false;
//	}
//
//	private PredictionData parseTwoDimensionalData(String strPredictionData) {
//		String[] lines = strPredictionData.split("\n");
//		int nLines = lines.length;
//		PredictionData predictionData = new PredictionData();
//		predictionData.setMetadata(this.getClass().getCanonicalName() + "\n"
//				+ strPredictionData);
//		for (int iLine = 0; iLine < nLines; iLine++) {
//			String[] tokens = lines[iLine].split("\t");
//			int atomId = Integer.parseInt(tokens[0].trim());
//			String strChemicalShift = null;
//			for (int iToken = tokens.length - 1; iToken > 0; iToken--) {
//				if (!tokens[iToken].matches(".*=null$")) {
//					strChemicalShift = tokens[iToken].trim();
//					break;
//				}
//			}
//			if (strChemicalShift == null) {
//				if (this.returnIncomplete) {
//					continue;
//				} else {
//					this.errorString = "No data: " + lines[iLine];
//					return null;
//				}
//			}
//			tokens = strChemicalShift.split("=");
//			PeakPrediction peakPrediction = new PeakPrediction();
//			Nucleus nucleus;
//			if (tokens[0].indexOf(",H)") != -1) {
//				nucleus = Nucleus.NUC_1H;
//			} else if (tokens[1].indexOf("[C") == 0) {
//				nucleus = Nucleus.NUC_13C;
//			} else {
//				this.errorString = "unknown nucleus";
//				System.out.println(this.errorString);
//				return null;
//			}
//			peakPrediction.setNucleus(nucleus);
//			peakPrediction.setAtomId(atomId);
//			peakPrediction.setPrimaryNeighbourId(atomId); // this is done so
//			// that hydrogens
//			// work
//			double chemicalShift = Double.parseDouble(tokens[2].trim());
//			peakPrediction.setChemicalShift(chemicalShift);
//			peakPrediction.setMinChemicalShift(chemicalShift);
//			peakPrediction.setMaxChemicalShift(chemicalShift);
//			predictionData.addPrediction(peakPrediction);
//		}
//		return predictionData;
//	}
//
//	private static String queryPrediction(String requestedCodes) {
//		String dbUrl = "jdbc:mysql://" + NMRPredictionClient.serverUrl
//				+ ":3306/NMRPrediction";
//		String dbUser = "NMRPrediction";
//		String dbPassword = "NMRPrediction";
//
//		Connection conn = null;
//		TreeSet<String> codes = new TreeSet<String>();
//		String[] lines = requestedCodes.split("\n");
//		int nLines = lines.length;
//		String[][] codeTable = new String[nLines][];
//		for (int i = 0; i < nLines; i++) {
//			String[] tokens = lines[i].split("\t");
//			codeTable[i] = tokens;
//			for (int j = 1; j < tokens.length; j++) {
//				codes.add(tokens[j]);
//			}
//		}
//		Iterator<String> iterator = codes.iterator();
//		String codeConstraint = "'" + iterator.next() + "'";
//		while (iterator.hasNext()) {
//			codeConstraint += ",'" + iterator.next() + "'";
//		}
//		String result = "";
//		TreeMap<String, Double> results = new TreeMap<String, Double>();
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
//			if (conn != null) {
//				PreparedStatement st = conn
//						.prepareStatement("SELECT hoseCodes.hoseCode AS code, AVG(chemicalShifts.chemicalShift) AS delta "
//								+ "FROM chemicalShifts INNER JOIN hoseCodes ON hoseCodes._hoseCodeID = chemicalShifts.hoseCodeID "
//								+ "WHERE hoseCodes.hoseCode IN ("
//								+ codeConstraint
//								+ ") " + "" + " GROUP BY hoseCodes.hoseCode"
//								
//						
//						);
//				if (st.execute()) {
//					ResultSet resultSet = st.getResultSet();
//
//					while (resultSet.next()) {
//						results.put(resultSet.getString("code"), resultSet
//								.getDouble("delta"));
//					}
//				} else {
//					SQLWarning warning = st.getWarnings();
//					System.out.println(warning.getLocalizedMessage());
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		for (int iLine = 0; iLine < nLines; iLine++) {
//			result += codeTable[iLine][0];
//			for (int iToken = 1; iToken < codeTable[iLine].length; iToken++) {
//				codeTable[iLine][iToken] += "="
//						+ results.get(codeTable[iLine][iToken]);
//				result += "\t" + codeTable[iLine][iToken];
//			}
//			result += "\n";
//		}
//		System.out.println(result);
//		return result;
//	}
//
//	private String rewritePrediction(StereoMolecule molecule, String result) {
//		// parse result
//		TreeMap<Integer, Double> predictions = new TreeMap<Integer, Double>();
//		TreeMap<String, LinkedList<Integer>> predictionCodes = new TreeMap<String, LinkedList<Integer>>();
//		TreeMap<String,Double> predictionValues = new TreeMap<String,Double>();
//		String[] idCodes = result.split("\n");
//		int nCodes = idCodes.length;
//		for (int iCode = 0; iCode < nCodes; iCode++) {
//			String[] tokens = idCodes[iCode].split("\t");
//			String selectedToken = null;
//			for (int iToken = tokens.length - 1; iToken > 0; iToken--) {
//				if (!tokens[iToken].matches(".*=null")) {
//					selectedToken = tokens[iToken];
//					break;
//				}
//			}
//			if (selectedToken == null) {
//				if (this.returnIncomplete) {
//					continue;
//				} else {
//					this.errorString = "No data: " + idCodes[iCode];
//					return null;
//				}
//			}
//			int id = Integer.parseInt(tokens[0].trim());
//			tokens = selectedToken.split("=");
//			String hoseCode = tokens[0]+"="+tokens[1];
//			double delta = Double
//			.parseDouble(tokens[2].trim()); 
//			if  (!predictionCodes.containsKey(hoseCode))
//				predictionCodes.put(hoseCode, new LinkedList<Integer>());
//			predictionCodes.get(hoseCode).add(id);
//			predictions.put(id, delta);
//			predictionValues.put(hoseCode, delta);
//		}
//		// 10 (2,H)=[CH1]a1a2[C][CH1]=10.961415869441359
//		// (3,H)=[CH1]a4a5[C]s5[CH1]a4[CH1]a5[CH1][C]=8.498552243555237
//		// (4,H)=[CH1]a2a6[C]a7[CH1]a3[CH1]a4[CH1]a5[CH1]a6[C]s7[C]a8[O]=8.516158306431565
//		// (5,H)=[CH1]a2a7[CH2]s8[CH1]a3[CH1]a4[CH1]a5[CH1]a7[CH1]a10a11[C]s9[C]a9a10[C]a11[N][O]=8.237594538382517
//		// (6,H)=[CH1]a1a7[CH1]a2[CH1]a3[CH1]a4[CH1]a7[CH1]a10a12[CH2]s8s11[C]s9[C]a9a10[C]a12[N][OH1][O]=8.40349896517031
//
//		DecimalFormat format = new DecimalFormat("##0.000");
//		String spectrumString = this.nucleus.toString() + " NMR (CDCl3): ? = ";
//		Set<Integer> keySet = predictions.keySet();
//		Iterator<Integer> iterator = keySet.iterator();
//		// add 1 to all atom references because the system works that way
//// contraction of atom ids, does not work
////		Set<String> hoseCodeSet = predictionCodes.keySet();
////		Iterator<String> iterator = hoseCodeSet.iterator();
////		while (iterator.hasNext()) {
////			String hoseCode = iterator.next();
////			LinkedList<Integer> idSet = predictionCodes.get(hoseCode);
////			int nAtoms = 0;
////			Iterator<Integer> idIterator = idSet.iterator();
////			String idString = "";
////			while (idIterator.hasNext()) {
////				Integer id = idIterator.next();
////				if (this.nucleus == Nucleus.NUC_1H) {
////					nAtoms += molecule.getAllHydrogens(id);
////				}
////				idString += ", " + (id+1);
////			}
////			if (this.nucleus == Nucleus.NUC_1H) {
////				spectrumString += format.format(predictionValues.get(hoseCode)) + "(" + nAtoms +" H" + idString + ") ,";
////			} else {
////				spectrumString += format.format(predictionValues.get(hoseCode)) + "(" + nAtoms +"" + idString + ") ,";
////			}
////		}
//		
//		
//		TreeMap<Double,String> signals = new TreeMap<Double,String>();
//		String newToken;
//		while (iterator.hasNext()) {
//			Integer id = iterator.next();
//			if (this.nucleus == Nucleus.NUC_1H) {
//				spectrumString += format.format(predictions.get(id)) + "("
//						+ molecule.getAllHydrogens(id) + " H, " + (id + 1)
//						+ "), ";
//			} else {
//				spectrumString += format.format(predictions.get(id)) + "(1,"
//						+ (id + 1) + "), ";
//			}
//		}
//		spectrumString = spectrumString.substring(0,
//				spectrumString.length() - 2);
//		System.out.println(spectrumString);
//		return spectrumString;
//	}
//
//	public boolean isTwoDimensional() {
//		return twoDimensional;
//	}
//
//	public static String getHoseCodes4Query(StereoMolecule molecule,
//			Nucleus queryNucleus) {
//		String queryCodes = "";
//		molecule.ensureHelperArrays(3);
//		AdvancedSphereDescriptorGenerator generator = new AdvancedSphereDescriptorGenerator(
//				molecule);
//		int atomicNo = queryNucleus.getAtomicNo();
//		int nMinSpheres = 2;
//		int nMaxSpheres = 6;
//		TreeMap<Integer, String> hoseCodes = new TreeMap<Integer, String>();
//		for (int iSpheres = nMinSpheres; iSpheres <= nMaxSpheres; iSpheres++) {
//			TreeMap<Integer, String> sphereCodes = generator
//					.getAllSphereIdCodesByAtomicNo(atomicNo, iSpheres);
//			Set<Integer> keySet = sphereCodes.keySet();
//			Iterator<Integer> iterator = keySet.iterator();
//			while (iterator.hasNext()) {
//				Integer key = iterator.next();
//				if (hoseCodes.containsKey(key)) {
//					String currentCode = hoseCodes.get(key);
//					currentCode += "\t" + sphereCodes.get(key);
//					hoseCodes.put(key, currentCode);
//				} else {
//					hoseCodes.put(key, sphereCodes.get(key));
//				}
//			}
//		}
//		Set<Integer> keySet = hoseCodes.keySet();
//		Iterator<Integer> iterator = keySet.iterator();
//		while (iterator.hasNext()) {
//			Integer key = iterator.next();
//			queryCodes += key.toString() + "\t" + hoseCodes.get(key) + "\n";
//		}
//		return queryCodes;
//	}
//
//	public static void saveAssignmentHoseCodes(String assignment) {
//		String dbUrl = "jdbc:mysql://" + NMRPredictionClient.serverUrl
//				+ ":3306/NMRPrediction";
//		String dbUser = "NMRPrediction";
//		String dbPassword = "NMRPrediction";
//
//		String[] lines = assignment.split("\n");
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//			Connection conn = DriverManager.getConnection(dbUrl, dbUser,
//					dbPassword);
//			if (conn != null) {
//				String actelionId = lines[0];
//				int moleculeID = NMRPredictionClient.getMoleculeID(conn, actelionId);
//				int peakOriginID = NMRPredictionClient.getOriginID(conn);
//				for (int iLine = 1; iLine < lines.length; iLine++) {
//					String[] tokens = lines[iLine].split("\t");
//					int atomID = Integer.parseInt(tokens[0].trim());
//					String chemicalShift = tokens[1].trim();
//					for (int iToken = 2; iToken < tokens.length; iToken++) {
//						String hoseCode = tokens[iToken].trim();
//						int hoseCodeID = NMRPredictionClient.getHoseCodeID(conn, hoseCode);
//						NMRPredictionClient.addChemicalShift(conn, moleculeID, peakOriginID,
//								atomID, chemicalShift, hoseCodeID);
//					}
//				}
//			}
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static int getHoseCodeID(Connection conn, String hoseCode) {
//		int result = -1;
//		try {
//			PreparedStatement st = conn
//					.prepareStatement("SELECT _hoseCodeID FROM hoseCodes WHERE hoseCode = '"
//							+ hoseCode + "'");
//			if (st.execute()) {
//				ResultSet rs = st.getResultSet();
//				if (rs.next()) {
//					result = rs.getInt(1);
//					st.close();
//				} else {
//					st.close();
//					st = conn
//							.prepareStatement("INSERT INTO hoseCodes (hoseCode) VALUES('"
//									+ hoseCode + "')");
//					if (st.execute()) {
//						st.close();
//						st = conn.prepareStatement("SELECT LAST_INSERT_ID()");
//						if (st.execute()) {
//							rs = st.getResultSet();
//							if (rs.next()) {
//								result = rs.getInt(1);
//							}
//						}
//					}
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return result;
//
//	}
//
//	private static void addChemicalShift(Connection conn, int moleculeID,
//			int peakOriginID, int atomID, String chemicalShift, int hoseCodeID) {
//		try {
//			PreparedStatement st = conn
//					.prepareStatement("INSERT INTO chemicalShifts (moleculeID,peakOriginID,atomID,hoseCodeID,chemicalShift) VALUES(?,?,?,?,?)");
//			st.setInt(1, moleculeID);
//			st.setInt(2, peakOriginID);
//			st.setInt(3, atomID);
//			st.setInt(4, hoseCodeID);
//			st.setString(5, chemicalShift);
//			if (st.execute())
//				st.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	private static int getMoleculeID(Connection conn, String actelionId) {
//		int result = -1;
//		try {
//
//			PreparedStatement st = conn
//					.prepareStatement("SELECT _moleculeID FROM molecules WHERE actelionID = ?");
//			st.setString(1, actelionId);
//			if (st.execute()) {
//				ResultSet rs = st.getResultSet();
//				if (rs.next()) {
//					result = rs.getInt(1);
//				}
//				st.close();
//				if (result == -1) {
//					
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	private static int getOriginID(Connection conn) {
//		final String origin = "nemo";
//		int result = -1;
//		try {
//			PreparedStatement st = conn
//					.prepareStatement("SELECT _peakOriginID FROM peakOrigins WHERE originDescription = '"
//							+ origin + "'");
//			if (st.execute()) {
//				ResultSet rs = st.getResultSet();
//				if (rs.next()) {
//					result = rs.getInt(1);
//					st.close();
//				} else {
//					st.close();
//					st = conn
//							.prepareStatement("INSERT INTO peakOrigins (originDescription) VALUES('"
//									+ origin + "')");
//					if (st.execute()) {
//						st.close();
//						st = conn.prepareStatement("SELECT LAST_INSERT_ID()");
//						if (st.execute()) {
//							rs = st.getResultSet();
//							if (rs.next()) {
//								result = rs.getInt(1);
//							}
//						}
//					}
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	public boolean returnsIncomplete() {
//		return returnIncomplete;
//	}
//
//	public void setReturnIncomplete(boolean returnIncomplete) {
//		this.returnIncomplete = returnIncomplete;
//	}
}
