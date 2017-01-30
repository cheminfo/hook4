package org.cheminfo.hook.nemo;

import java.util.TreeSet;
import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.framework.LinkCreationHandler;
import org.cheminfo.hook.moldraw.ActAtomEntity;
import org.cheminfo.hook.moldraw.ActMoleculeDisplay;
import org.cheminfo.hook.nemo.nmr.ExperimentType;
import org.cheminfo.hook.nemo.nmr.Nucleus;
import org.cheminfo.hook.nemo.nmr.ProprietaryTools;

public class NemoLinkCreationHandler implements LinkCreationHandler {
	private static boolean isDebug = false;

	public void handleLinkCreation(InteractiveSurface interactiveSurface,
			BasicEntity entityA, BasicEntity entityB) {
		// enforce directionality when creating links
		//System.out.println(entityA.getClass().getName()+" "+entityB.getClass().getName());
		if(entityA instanceof ActAtomEntity && entityB instanceof SmartPeakLabel
				||entityA instanceof SmartPeakLabel && entityB instanceof ActAtomEntity){
			if(entityA instanceof ActAtomEntity){
				hadle1DNMRAssignment(interactiveSurface,(ActAtomEntity)entityA,(SmartPeakLabel)entityB);
			}
			else{
				hadle1DNMRAssignment(interactiveSurface,(ActAtomEntity)entityB,(SmartPeakLabel)entityA);
			}
		}
		else{
			if ((entityA instanceof Integral && entityB instanceof SmartPeakLabel)
					|| (entityA instanceof SmartPeakLabel && entityB instanceof PeakLabel)

			) {
				if (isDebug)
					System.out.println("inverted link " +entityA +"->"+entityB);
				interactiveSurface.createLink(entityB, entityA);
			} else if (entityA instanceof PeakLabel
					&& entityB instanceof ActAtomEntity) {
				this.handleHSQC(interactiveSurface, (PeakLabel) entityA,
						(ActAtomEntity) entityB);
			} else if (entityB instanceof PeakLabel
					&& entityA instanceof ActAtomEntity) {
				this.handleHSQC(interactiveSurface, (PeakLabel) entityB,
						(ActAtomEntity) entityA);
			} else {
				interactiveSurface.createLink(entityA, entityB);
			}
		}
	}
	
	public void hadle1DNMRAssignment(InteractiveSurface interactiveSurface, ActAtomEntity atomEntity, SmartPeakLabel peakLabel ){
		//atomEntity.
		if (peakLabel.getNmrSignal1D().getNucleus().compareTo(Nucleus.NUC_1H)==0) { // in this case we need to check the hydrogen to link on heavy atom
			TreeSet<String> diaH = atomEntity.getProtonDiastereotopicIDs();
			peakLabel.getNmrSignal1D().setReferenceAtomID(atomEntity.getAtomID());
			for(String diaID:diaH){
				peakLabel.getNmrSignal1D().addDiaID(diaID);
			}
		}
		else
			peakLabel.getNmrSignal1D().addDiaID(atomEntity.getDiastereotopicID());
		interactiveSurface.createLink(peakLabel, atomEntity);
	}

	@SuppressWarnings("unchecked")
	public void handleHSQC(InteractiveSurface interactions,
			PeakLabel peakLabel, ActAtomEntity atomEntity) {
		if (peakLabel.getParentEntity() instanceof Spectra
				&& ((Spectra) peakLabel.getParentEntity()).getSpectraData()
						.getExperimentType() == ExperimentType.HSQC) {
			Nucleus heteroNucleus =((Spectra) peakLabel.getParentEntity()).getNucleus(2); 
			if (isDebug)
				System.out.println("HSQC spectrum detected");
			Vector linkedEntities = interactions
					.getLinkedDestEntities(peakLabel);
			SmartPeakLabel heteroLabel = null;
			SmartPeakLabel hydrogenLabel = null;
			for (int ent = 0; ent < linkedEntities.size(); ent++) {
				if (linkedEntities.get(ent) instanceof SmartPeakLabel) {
					SmartPeakLabel spl = (SmartPeakLabel) linkedEntities
							.get(ent);
					if (spl.getParentEntity() instanceof Spectra) {
						Nucleus nucleus = ((Spectra) spl.getParentEntity())
								.getNucleus();
						if (nucleus == Nucleus.NUC_1H) {
							hydrogenLabel = spl;
						} else if (nucleus == heteroNucleus) {
							heteroLabel = spl;
						}
					}
				}
			}
			if (heteroLabel == null || hydrogenLabel == null) return;
			ActMoleculeDisplay molDisplay = (ActMoleculeDisplay) atomEntity.getParentEntity();
			ActAtomEntity heteroAtom = atomEntity;
			ActAtomEntity hydrogenAtom = atomEntity;
			int atomID = atomEntity.getAtomID();

			heteroAtom=ProprietaryTools.getHeavyAtom(molDisplay, atomID);
			
			if (heteroAtom==null) return;
			
			if (isDebug) {
				System.out.println("creating links");
			}
			interactions.createLink(hydrogenLabel, hydrogenAtom);
			interactions.createLink(heteroLabel, heteroAtom);
		}
	}

	public static void handleTwoDimToAtomLink(
			InteractiveSurface interactiveSurface, PeakLabel twoDimLabel,
			ActAtomEntity atomEntity) {
		// Spectra twoDimParentSpectra = (Spectra)twoDimLabel.getParentEntity();
		Vector linkedEntities = interactiveSurface
				.getLinkedEntities(twoDimLabel);
		for (int i = 0; i < linkedEntities.size(); i++) {
			if (linkedEntities.get(i) instanceof SmartPeakLabel) {
				interactiveSurface.createLink((BasicEntity) linkedEntities
						.get(i), (BasicEntity) atomEntity);
			}
		}
	}

	public static void resolvePredicitonSmartPeakLinks(
			InteractiveSurface interactiveSurface, PeakLabel peakLabel,
			PredictionLabel predictionLabel) {
		Vector predictionLabelLinks = interactiveSurface
				.getLinkedEntities(predictionLabel);
		Vector<ActAtomEntity> linkedAtomEntities = new Vector<ActAtomEntity>(
				predictionLabelLinks.size());
		for (int i = 0; i < predictionLabelLinks.size(); i++) {
			if (predictionLabelLinks.get(i) instanceof ActAtomEntity)
				linkedAtomEntities.add((ActAtomEntity) predictionLabelLinks
						.get(i));
		}
		if (linkedAtomEntities.size() == 0)
			return;
		Vector peakLabelLinks = interactiveSurface.getLinkedEntities(peakLabel);
		Vector<SmartPeakLabel> linkedSmartPeakLabels = new Vector<SmartPeakLabel>(
				peakLabelLinks.size());
		for (int i = 0; i < peakLabelLinks.size(); i++) {
			if (peakLabelLinks.get(i) instanceof SmartPeakLabel)
				linkedSmartPeakLabels.add((SmartPeakLabel) peakLabelLinks
						.get(i));
		}
		//
		for (int iAtomLabel = 0; iAtomLabel < linkedAtomEntities.size(); iAtomLabel++) {
			interactiveSurface.createLink(peakLabel, linkedAtomEntities
					.get(iAtomLabel));
			for (int iSmartPeakLabel = 0; iSmartPeakLabel < linkedSmartPeakLabels
					.size(); iSmartPeakLabel++) {
				interactiveSurface.createLink(linkedSmartPeakLabels
						.get(iSmartPeakLabel), linkedAtomEntities
						.get(iAtomLabel));
			}
		}
	}

	public void handleLinkDeletion(InteractiveSurface interactiveSurface,
			BasicEntity entityA, BasicEntity entityB) {
		if (entityA instanceof ActAtomEntity && entityB instanceof SmartPeakLabel) {
			NemoLinkCreationHandler.removeAssignments(interactiveSurface, (SmartPeakLabel)entityB, (ActAtomEntity)entityA);
		} else if (entityB instanceof ActAtomEntity && entityA instanceof SmartPeakLabel) {
			NemoLinkCreationHandler.removeAssignments(interactiveSurface, (SmartPeakLabel)entityA, (ActAtomEntity)entityB);
		} else {
			interactiveSurface.removeLink(entityA, entityB);
			interactiveSurface.removeLink(entityB, entityA);
		}
	}

	 private static void removeAssignments(InteractiveSurface interactiveSurface,
				SmartPeakLabel peakLabel, ActAtomEntity atomEntity) {
		 peakLabel.setMouseover(false);
		 interactiveSurface.removeLink(peakLabel, atomEntity);
		 peakLabel.getNmrSignal1D().removeDiaID(atomEntity.getDiastereotopicID());
		 Vector<?> links = interactiveSurface.getLinkedDestEntities(atomEntity);
		 for (int i = 0; i < links.size(); i++) {
			 if (links.get(i) instanceof ActAtomEntity) {
				 interactiveSurface.removeLink(peakLabel, (BasicEntity)links.get(i));
				 peakLabel.getNmrSignal1D().removeDiaID(((ActAtomEntity)links.get(i)).getDiastereotopicID());
			 }
		 }
	 }
	
}
