package org.cheminfo.hook.nemo;

import java.util.Vector;

import org.cheminfo.hook.framework.BasicEntity;
import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.moldraw.ActAtomEntity;

/**
 * This class is intended to fix a compatibility issue on old XML views. This class has to include the
 * IntegralData information within the NMRSignal1D
 * 
 * @author acastillo
 *
 */

public class CompatibilityLinks {
	
	public static void newLinkNmrSignal1D2IntegralData(BasicEntity linkable, InteractiveSurface interactions){
		
		//To get all the entities that we want to link
		SmartPeakLabel label = null;
		Integral integral = null;
		ActAtomEntity atom = null;
		for (int ent = 0; ent < linkable.getEntitiesCount(); ent++) {
			BasicEntity entity = ((BasicEntity) linkable.getEntity(ent));
			if(entity instanceof Spectra){
				CompatibilityLinks.newLinkNmrSignal1D2IntegralData(entity, interactions);
			}
			//System.out.println("Looping "+(entity.getClass().getName()));
			if(entity instanceof SmartPeakLabel){
				label=(SmartPeakLabel)entity;
				//System.out.println("SPL "+label.getNmrSignal1D().getCenter());
				Vector<BasicEntity> entities = interactions.getLinkedEntities(entity);
				//System.out.println("Linked "+entities.size());
				for(BasicEntity linked:entities){
					if(linked instanceof Integral){
						integral = (Integral)linked;
						//if(label.getNmrSignal1D().getIntegralData()==null){
							label.getNmrSignal1D().setIntegralData(integral.getIntegralData());
							//It is only necessary if the integralData is not already set.
							if(integral.getIntegralData().getBaseArea()==1){
								//Let set the base area if it is already available
								//It should be true always be, I'm not really sure
								if(linkable instanceof Spectra){
									integral.getIntegralData().setBaseArea(((Spectra)linkable).getIntegralsBaseArea());
								}
								
							}
						//}	
					}else{
						//Lets try to add the diaID for every single smartPeakLabel 
						if(linked instanceof ActAtomEntity){
							atom = (ActAtomEntity)linked;
							if(label.getNmrSignal1D().getDiaIDs().size()==0){
								label.getNmrSignal1D().addDiaID(atom.getDiastereotopicID());
							}	
						}
					}
				}
			}
		}
	}

}
