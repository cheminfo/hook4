package org.cheminfo.hook.nemo.nmr;

import java.util.TreeMap;
import java.util.Vector;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.SmartPeakLabel;
import org.cheminfo.hook.nemo.Spectra;

public class NmrSimulator {
	static boolean  DEBUG = false;

	public static boolean createFullPeakAtomLinks(ActMoleculeDisplay molDisplay, Spectra spectra) {		
		
		boolean[] previousState = molDisplay.expandAll();
		if(DEBUG){
			System.out.println("");
			for(int i=0;i<previousState.length;i++)
				System.out.print(previousState[i]+" ");
			System.out.println("");
		}
		
		boolean ok = createPeakAtomLinks(molDisplay,spectra);
		molDisplay.setExpansionState(previousState);
		
		return ok;
	}

	public static boolean createPeakAtomLinks(ActMoleculeDisplay molDisplay, Spectra spectra) {
		InteractiveSurface interactions = molDisplay.getInteractiveSurface();
		SmartPeakLabel currentLabel;
		TreeMap<Integer, ActAtomEntity> atomEntities = molDisplay.getAtomEntityMap();
		for (int ent = 0; ent < spectra.getEntitiesCount(); ent++) {
			if (spectra.getEntity(ent) instanceof SmartPeakLabel) {
			   currentLabel = (SmartPeakLabel) spectra.getEntity(ent);
			   if (currentLabel.getDiastereotopicIDs().size()>0) {
					ActAtomEntity atomEntity;
					for (String id : currentLabel.getDiastereotopicIDs()) {
						for (Integer atomID : atomEntities.keySet()) {
							atomEntity=atomEntities.get(atomID);
							if (id.equals(atomEntity.getDiastereotopicID())) {
								interactions.createLink(currentLabel, atomEntity);
							} else if (! atomEntity.isExpanded()) { // in this case we need to check the hydrogen to link on heavy atom
								if (atomEntity.getProtonDiastereotopicIDs().contains(id)) {
									interactions.createLink(currentLabel, atomEntity);
								}
							}
						}
					}
				}
				else{
					if(currentLabel.getNmrSignal1D().getAtomIDs().size()>0) {
						Vector<String> items = currentLabel.getNmrSignal1D().getAtomIDs();
						for (int i = items.size()-1; i >=0 ; i--) {
							try {
								int atomId = Integer.parseInt(items.get(i).trim());
								ActAtomEntity atomEntity = atomEntities.get(atomId);
								if (atomEntity != null){
									interactions.createLink(currentLabel, atomEntity);
									currentLabel.getNmrSignal1D().addDiaID(atomEntity.getDiastereotopicID());
								}
							} catch (NumberFormatException e) {
							}
						}
					}
					else{
						//Just for compatibility. I don't know if it is really necessary for something. A.C.
						if (currentLabel.getReferenceAtomID() != SmartPeakLabel.UNASSIGNED) {
							int referenceID = currentLabel.getReferenceAtomID();
							ActAtomEntity atomEntity = atomEntities.get(referenceID);
							if (atomEntity != null){
								interactions.createLink(currentLabel, atomEntity);
							}
						}
					}
				}

			}
		}
		
		return true;
	}

}
