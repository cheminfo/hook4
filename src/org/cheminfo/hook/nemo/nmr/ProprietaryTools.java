package org.cheminfo.hook.nemo.nmr;


import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.contrib.DiastereotopicAtomID;
import com.actelion.research.chem.contrib.HydrogenHandler;
import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.math.peakdetection.AsymmetricPeak;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.*;
import org.cheminfo.hook.nemo.signal.NMRSignal1D;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.Map.Entry;


public class ProprietaryTools {

	final static boolean DEBUG=false;
	
	
	public static TreeSet<String> resolveDiaIDs(InteractiveSurface interactions,ActMoleculeDisplay molDisplay, SmartPeakLabel peakLabel,Nucleus nucleus) {
		TreeSet<Integer> atomIDs=resolveAtomIDs(interactions, molDisplay, peakLabel, nucleus, false);
		TreeSet<String> diaIDs=new TreeSet<String>();
		String tmp = "";
		for (Integer i : atomIDs) {
			tmp = molDisplay.getDiastereotopicID(i);
			diaIDs.add(tmp);
			peakLabel.getNmrSignal1D().addDiaID(tmp);
		}
		return diaIDs;
	}
	
	public static JSONArray getDiaIDs(ActMoleculeDisplay molDisplay){
		//JSONArray diaIDs = new JSONArray();
		TreeMap<String,JSONArray> diaIds = new TreeMap<String,JSONArray>();
		String[] ids = DiastereotopicAtomID.getAtomIds(molDisplay.getEnhancedMolecule());
		for(int i=0;i<ids.length;i++){
			if(diaIds.containsKey(ids[i])){
				diaIds.get(ids[i]).put(i);
			}
			else{
				JSONArray atoms = new JSONArray();
				atoms.put(i);
				diaIds.put(ids[i], atoms);
			}
		}
		JSONArray toReturn = new JSONArray();
		Iterator<String> elements = diaIds.navigableKeySet().descendingIterator();
		NavigableMap<String, JSONArray> map = diaIds.descendingMap();
		while(!map.isEmpty()){
			Entry<String, JSONArray> entry = map.pollLastEntry();
			JSONObject data = new JSONObject();
			try {
				data.put("id", entry.getKey());
				data.put("atoms", entry.getValue());
				data.put("nbEquivalent", entry.getValue().length());
			} catch (JSONException e) {
				
				e.printStackTrace();
			}
			toReturn.put(data);
		}
		
		return toReturn;
	}
	
	public static NMRSignal1D[] mergeSignalsByDiaID(ActMoleculeDisplay molDisplay, NMRSignal1D[] signals) {
		/*String[] ids = org.cheminfo.chem.DiastereotopicAtomID.getAtomIds(molDisplay.getEnhancedMolecule());
		for(int i=0;i<signals.length;i++){
			Vector<String > atomIds = signals[i].getAtomIDs();
			for(int j=0;j<atomIds.size();j++){
				int atomID = Integer.valueOf(atomIds.get(j));
				signals[i].addDiaID(ids[atomID]);
			}
			
		}*/
		int finalSize = signals.length;
		for(int i=signals.length-1;i>=0;i--){
			NMRSignal1D label1 = signals[i];
			for(int j=i-1;j>=0;j--){
				NMRSignal1D  label2 = signals[j];
				boolean sameDiaID = false;
				
				Iterator<String> iterator = label2.getdiaIDIterator();
				while(iterator.hasNext()){
					sameDiaID=label1.getDiaIDs().contains(iterator.next());
					if(sameDiaID)
						break;
				}
				/*System.out.println(label1.getDiaIDs());
				System.out.println(sameDiaID);
				System.out.println(label2.getDiaIDs());*/
				if(sameDiaID){
					//label1.getNmrSignal1D().setPublicationAssignment(label1.getNmrSignal1D().getPublicationAssignment()+","+label2.getNmrSignal1D().getPublicationAssignment());
					label2.joinWith(label1);
					signals[i]=null;
					finalSize--;
					break;
				}
				//System.out.println("JJ "+finalSize);
			}
		}
		//System.out.println("MM "+finalSize);
		NMRSignal1D[] toReturn = new NMRSignal1D[finalSize];
		int index = 0;
		for(int i=0;i<signals.length;i++){
			//System.out.println("i "+i);
			if(signals[i]!=null)
				toReturn[index++]=signals[i];
		}
		//System.out.println("Herer");
		return toReturn;
		
	}
	
	public static TreeSet<Integer> resolveAtomIDs(InteractiveSurface interactions,ActMoleculeDisplay molDisplay, SmartPeakLabel peakLabel,Nucleus nucleus, boolean ignoreSymmetry) {
		Vector linkedEntities = interactions.getLinkedDestEntities(peakLabel);
		TreeSet<Integer> atomIDs = new TreeSet<Integer>();
		StereoMolecule enhancedMolecule = molDisplay.getEnhancedMolecule();
		for (int i = 0; i < linkedEntities.size(); i++) {
			if (linkedEntities.get(i) instanceof ActAtomEntity) {
				ActAtomEntity atomEntity = (ActAtomEntity) linkedEntities
						.get(i);
				int atomID = atomEntity.getAtomID();
				int atomicNo = nucleus.getAtomicNo();
				if (nucleus == Nucleus.NUC_1H) {
					atomIDs.addAll(molDisplay.getProtonIds(atomID));
				} else {
					if (enhancedMolecule.getAtomicNo(atomID) == atomicNo)
						atomIDs.add(atomID);
				}
				if (!ignoreSymmetry) {
					Vector linkedAtomEntities = interactions
							.getLinkedEntities(atomEntity);
					for (int j = 0; j < linkedAtomEntities.size(); j++) {
						if (linkedAtomEntities.get(j) instanceof ActAtomEntity) {
							ActAtomEntity linkedAtomEntity = (ActAtomEntity) linkedAtomEntities
									.get(j);
							if (nucleus == Nucleus.NUC_1H) {
								atomIDs.addAll(molDisplay.getProtonIds(linkedAtomEntity.getAtomID()));
							} else {
								atomIDs.add(linkedAtomEntity.getAtomID());
							}
						}
					}
				}
			}
		}
		return atomIDs;
	}
	
	public static String resolveAtomIDsAsString(InteractiveSurface interactions,ActMoleculeDisplay molDisplay, SmartPeakLabel peakLabel,Nucleus nucleus, boolean ignoreSymmetry) {
		TreeSet<Integer> atomIDs=resolveAtomIDs(interactions, molDisplay, peakLabel, nucleus, ignoreSymmetry);
		// return a sorted string
		String strAtomIDs="";
		Iterator<Integer> iterator = atomIDs.iterator();
		while (iterator.hasNext()) {
			strAtomIDs += iterator.next().intValue();
			if (iterator.hasNext())
				strAtomIDs += ",";
		}
		return strAtomIDs;
	}
	
	public static void adjustIntegralsToMF(Spectra spectrum, ActMoleculeDisplay entity) {
		StereoMolecule molecule = entity.getCompactMolecule();
		if (molecule != null) {
			int nbHydrogens=HydrogenHandler.getNumberOfHydrogens(molecule);
			Vector<Integral> integrals = new Vector<Integral>(spectrum.getEntitiesCount());
			double totalArea = 0.0;
			for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
				if (spectrum.getEntity(ent) instanceof Integral) {
					Integral integral = (Integral) spectrum.getEntity(ent);
					totalArea += integral.getRelArea();
					integrals.add(integral);
				}
			}
			Integral refIntegral = integrals.get(0);
			double factor = nbHydrogens / totalArea;
			IntegrationHelpers.setNewRefIntegral(spectrum, refIntegral, refIntegral.getRelArea()* factor);
		}
	}
	
	public static String getQueryHoseCodes(ActMoleculeDisplay molDisplay, Spectra spectrum, String queryNucleus) {
		System.out.println("getQueryHoseCodes");
		if (molDisplay == null)
			return "";
		
		StereoMolecule compactMolecule = molDisplay.getCompactMolecule();
		StereoMolecule enhancedMolecule = molDisplay.getEnhancedMolecule();
		// determine the requested nuclei
		TreeSet<String> hoseCodes = new TreeSet<String>();
		TreeSet<Nucleus> requestedNuclei = new TreeSet<Nucleus>();
		String[] tokens = queryNucleus.split(",");
		for (String token : tokens) {
			requestedNuclei.add(Nucleus.determineNucleus(token));
		}
		if (requestedNuclei.size() == 0) {
			if (spectrum != null) {
				if (spectrum.isDrawnAs2D()) {
					requestedNuclei.add(spectrum.getNucleus(1));
					requestedNuclei.add(spectrum.getNucleus(2));
				}
			}
		}
		HoseGeneratorInterface genInterface = new HoseGeneratorInterface(
				molDisplay);
		for (Nucleus nucleus : requestedNuclei) {
			if (nucleus == Nucleus.UNDEF)
				continue;
			genInterface.getAllHoseCodesByType(hoseCodes, nucleus);
		}
		//
		String queryCodes = "";
		for (String queryCode : hoseCodes) {
			queryCodes += queryCode + "\n";
		}
		return queryCodes;
	}
	
	
	
	public static String getHoseCodes4Assignment(ActMoleculeDisplay molDisplay, SpectraDisplay spectraDisplay, InteractiveSurface interactiveSurface) {
		String assignmentString = "";
 
		if (molDisplay == null) return assignmentString;

		Vector<Spectra> spectras = new Vector<Spectra>();

		Spectra firstSpectrum = spectraDisplay.getFirstSpectra();
		if (firstSpectrum == null)
			return assignmentString;
		if (firstSpectrum.getSpectraData().getDataType() == SpectraData.TYPE_2DNMR_SPECTRUM) {
			if (spectraDisplay.getHorRefSpectrum() != null)
				spectras.add(spectraDisplay.getHorRefSpectrum());
			if (spectraDisplay.getVerRefSpectrum() != null)
				spectras.add(spectraDisplay.getVerRefSpectrum());
		} else if (firstSpectrum.getSpectraData().getDataType() == SpectraData.TYPE_NMR_SPECTRUM) {
			spectras.add(firstSpectrum);
		} else {
			return assignmentString;
		}
		HoseGeneratorInterface genInterface = new HoseGeneratorInterface(
				molDisplay);
		for (int iSpectrum = 0; iSpectrum < spectras.size(); iSpectrum++) {
			Spectra spectrum = spectras.get(iSpectrum);
			for (int ent = 0; ent < spectrum.getEntitiesCount(); ent++) {
				if (spectrum.getEntity(ent) instanceof SmartPeakLabel) {
					SmartPeakLabel peakLabel = (SmartPeakLabel) spectrum.getEntity(ent);
					Vector linkedEntities = interactiveSurface.getLinkedOutboundEntities(peakLabel);
					int nLinks = linkedEntities.size();
					TreeSet<ActAtomEntity> atomEntities = new TreeSet<ActAtomEntity>();
					for (int iLink = 0; iLink < nLinks; iLink++) {
						if (linkedEntities.get(iLink) instanceof ActAtomEntity) {
							ActAtomEntity atomEntity = (ActAtomEntity) linkedEntities.get(iLink);
							atomEntities.add(atomEntity);
							Vector linkedAtoms = interactiveSurface
									.getLinkedDestEntities(atomEntity);
							for (int i = 0; i < linkedAtoms.size(); i++) {
								if (linkedAtoms.get(i) instanceof ActAtomEntity)
									atomEntities.add((ActAtomEntity) linkedAtoms.get(i));
							}
						}
					}
					if (atomEntities.size() > 0) {
						ActAtomEntity atomEntity = atomEntities.first();
						int atomID = atomEntity.getAtomID();
						String idString = "";
						StringBuffer buffer = new StringBuffer();
						if (spectrum.getNucleus() == Nucleus.NUC_1H) {
							for (ActAtomEntity a : atomEntities) {
								if (molDisplay.getCompactMolecule().getAllAtoms() <= a.getAtomID()) {
									// hydrogen
									atomID = a.getAtomID();
									buffer.append(a.getAtomID());
									buffer.append(',');
								} else {
									int heavyAtomID = a.getAtomID();
									StereoMolecule enhancedMolecule = molDisplay.getEnhancedMolecule();
									for (int iConn = 0; iConn < enhancedMolecule.getAllConnAtoms(heavyAtomID); iConn++) {
										int iNeighbour = enhancedMolecule.getConnAtom(heavyAtomID, iConn);
										if (enhancedMolecule.getAtomicNo(iNeighbour) == 1) {
											atomID = iNeighbour;
											buffer.append(iNeighbour);
											buffer.append(',');
										}
									}
								}

							}
						} else {
							for (ActAtomEntity a : atomEntities) {
								buffer.append(a.getAtomID());
								buffer.append(',');
							}
						}

						if (buffer.length() > 0) {
							buffer.deleteCharAt(buffer.length() - 1);
						}
						assignmentString += buffer.toString()
								+ "\t"
								+ ((peakLabel.getNmrSignal1D().getEndX() + peakLabel.getNmrSignal1D().getStartX()) / 2)
								+ "\t"
								+ genInterface.getHoseCodes4Assignment(atomID)
								+ "\n";
					}
				}
			}
		}
		return assignmentString;
	}
	
	@Deprecated
	public static void evaluatePeakSelect(InteractiveSurface interactions, AsymmetricPeak[] peaks) {
		// get molecule
		ActMoleculeDisplay molDisplay = null;
		BasicEntity entity = interactions.getEntityByName("molDisplay");
		if (entity instanceof ActMoleculeDisplay)
			molDisplay = (ActMoleculeDisplay) entity;
		if (molDisplay == null)
			return;
		StereoMolecule molecule = molDisplay.getCompactMolecule();
		if (molecule == null)
			return;
		molecule.ensureHelperArrays(3);
		int nHydrogens = HydrogenHandler.getNumberOfHydrogens(molecule);
		// analyze peaks
		int nPeaks = peaks.length;
		double[] area = new double[nPeaks];
		double totalArea = 0.0;
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			area[iPeak] = peaks[iPeak].getAbsIntegral();
			totalArea += area[iPeak];
		}
		for (int iIntegral = 0; iIntegral < nPeaks; iIntegral++)
			area[iIntegral] = area[iIntegral] / totalArea * nHydrogens;
		//
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			if (area[iPeak] < 0.1) {
				peaks[iPeak] = null;
			}
		}
	}
	
	public static void evaluatePeakSelect(InteractiveSurface interactions, NMRSignal1D[] peaks) {
		// get molecule
		ActMoleculeDisplay molDisplay = null;
		BasicEntity entity = interactions.getEntityByName("molDisplay");
		if (entity instanceof ActMoleculeDisplay)
			molDisplay = (ActMoleculeDisplay) entity;
		if (molDisplay == null)
			return;
		StereoMolecule molecule = molDisplay.getCompactMolecule();
		if (molecule == null)
			return;
		molecule.ensureHelperArrays(3);
		int nHydrogens = HydrogenHandler.getNumberOfHydrogens(molecule);
		// analyze peaks
		int nPeaks = peaks.length;
		double[] area = new double[nPeaks];
		double totalArea = 0.0;
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			area[iPeak] = peaks[iPeak].getIntegralData().getValue();
			totalArea += area[iPeak];
		}
		for (int iIntegral = 0; iIntegral < nPeaks; iIntegral++)
			area[iIntegral] = area[iIntegral] / totalArea * nHydrogens;
		//
		for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
			if (area[iPeak] < 0.1) {
				peaks[iPeak] = null;
			}
		}
	}
	

		
		
	public static String canonizeMolfile(String molfile) {
		return CanonizeMolfile.canonizeMolfile(molfile);
	}
	
	public static ActAtomEntity getHeavyAtom(ActMoleculeDisplay molDisplay, int atomID) {
		StereoMolecule enhancedMolecule = molDisplay.getEnhancedMolecule();
		ActAtomEntity heteroAtom=null;
		if (enhancedMolecule.getAtomicNo(atomID) == 1) {
			int iNeighbour = enhancedMolecule.getConnAtom(atomID, 0);
			heteroAtom = molDisplay.getAtomEntityMap().get(iNeighbour);
		} else if (molDisplay.isAtomExpanded(atomID)) {
			// the atom is expanded and we have no clue
			// to what we should link
		}
		return heteroAtom;
	}
	
	
}
