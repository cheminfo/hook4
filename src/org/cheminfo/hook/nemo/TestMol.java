package org.cheminfo.hook.nemo;

import org.cheminfo.hook.moldraw.ActMoleculeDisplay;

public class TestMol {
	public static void main(String[] args){
		ActMoleculeDisplay molDisplay = new ActMoleculeDisplay();
		molDisplay.setEntityName("molDisplay");
		//molDisplay.setErasable(true);
		//mainDisplay.addEntity(molDisplay,0);
		//molDisplay.init(10,10);
		molDisplay.addMoleculeByIDCode("fdq@P@@HCEImUUUVZjjjjX@@");
		System.out.println(molDisplay.getAtomEntityMap().firstEntry().getValue());

	}
}
