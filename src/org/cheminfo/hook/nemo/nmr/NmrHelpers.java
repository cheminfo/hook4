package org.cheminfo.hook.nemo.nmr;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.Integral;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.nemo.Spectra;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NmrHelpers {	

	/*public static void appendSmartPeakLabels(Spectra spectra, JSONObject json) throws JSONException {
		SmartPeakLabel[] sPLArray = getSmartLabels(spectra, true);
		Nucleus nucleus=spectra.getNucleus();
		InteractiveSurface interactions = spectra.getInteractiveSurface();
		if (sPLArray.length == 0) return;

		// get access to the molecule display
		ActMoleculeDisplay molDisplay = ActMoleculeDisplay.getMolDisplay(interactions);

		JSONArray smartPeakLabels=new JSONArray();
		json.put("smartPeakLabels", smartPeakLabels);

		for (SmartPeakLabel smartPeakLabel : sPLArray) {
			JSONObject oneLine=new JSONObject();
			smartPeakLabels.put(oneLine);
			if (molDisplay != null) {
				oneLine.put("atomID", ProprietaryTools.resolveAtomIDsAsString(interactions, molDisplay, smartPeakLabel, nucleus, false));
				// we will add all the diastereotopicIDs corresponding to the assignment as a JSON array
				TreeSet<String> diaIDs=ProprietaryTools.resolveDiaIDs(interactions, molDisplay, smartPeakLabel, nucleus);
				JSONArray ids=new JSONArray();
				oneLine.put("diaIDs",ids);
				for (String diaID : diaIDs) {
					ids.put(diaID);
				}
			}
			oneLine.put("nucleus",nucleus.toString());
			String peakPattern = smartPeakLabel.getPeakPattern();
			if (peakPattern.equals("m") && (smartPeakLabel.isForceMassif() || smartPeakLabel.getNmrSignal1D().getNbPeaks() == 0)) {
				oneLine.put("delta1", smartPeakLabel.getNmrSignal1D().getStartX());
				oneLine.put("delta2", smartPeakLabel.getNmrSignal1D().getEndX());
			} else {
				oneLine.put("delta1", smartPeakLabel.getNmrSignal1D().getCenter());
			}
			oneLine.put("pubAssignment", smartPeakLabel.getNmrSignal1D().getPublicationAssignment());
			oneLine.put("pubMultiplicity", smartPeakLabel.getNmrSignal1D().getPublicationType());
			oneLine.put("multiplicity", peakPattern);
			Integral linkedIntegral = getLinkedIntegral(interactions, smartPeakLabel);
			// INTEGRAL
			if (linkedIntegral != null) {
				oneLine.put("integration", linkedIntegral.getRelArea());
				oneLine.put("pubIntegration", linkedIntegral.getPublicationValue());
			}

			// add peak pattern, eg. multiplicities, and coupling constants
			if ((! peakPattern.equals("m")) && (! peakPattern.equals("s"))) {
				JSONArray j=new JSONArray();
				oneLine.put("couplings", j);
				smartPeakLabel.putCouplingJSON(j);
			}

		}	
	}*/
	
	public static JSONArray labelsToJSON(Spectra spectra){
		JSONArray toReturn = new JSONArray();
		SmartPeakLabel[] sPLArray = getSmartLabels(spectra, true);
		for (SmartPeakLabel smartPeakLabel : sPLArray) {
			toReturn.put(smartPeakLabel.getNmrSignal1D().toJSON());
		}
		return toReturn;
	}
	
	public static SmartPeakLabel[] getSmartLabels(Spectra spectra, boolean ascending) { // Sort
		// the
		// SmartPeakLabels
		Vector<SmartPeakLabel> sPL = new Vector<SmartPeakLabel>(); // SmartPeakLabel vector
		for (int entity = 0; entity < spectra.getEntitiesCount(); entity++) {
			if (spectra.getEntity(entity) instanceof SmartPeakLabel)
				sPL.addElement((SmartPeakLabel)spectra.getEntity(entity));
		}

		SmartPeakLabel[] sPLArray = new SmartPeakLabel[sPL.size()];

		sPL.toArray(sPLArray);

		if (ascending) {
			Arrays.sort(sPLArray);
		} else {
			Arrays.sort(sPLArray, Collections.reverseOrder());
		}
		return sPLArray;
	}

	

	private static Integral getLinkedIntegral(InteractiveSurface interactions,
			SmartPeakLabel peakLabel) {
		Integral linkedIntegral = null;
		Vector linkedEntities = interactions.getLinkedEntities(peakLabel);
		for (int index = 0; index < linkedEntities.size(); index++) {
			if (linkedEntities.get(index) instanceof Integral) {
				linkedIntegral = (Integral) linkedEntities.get(index);
				break;
			}
		}
		return linkedIntegral;
	}


}