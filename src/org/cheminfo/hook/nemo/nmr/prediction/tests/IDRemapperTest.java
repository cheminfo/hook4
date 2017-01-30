package org.cheminfo.hook.nemo.nmr.prediction.tests;

import java.io.File;

import org.cheminfo.hook.nemo.nmr.prediction.IDRemapper;

public class IDRemapperTest {

	
	
	public static void main(String[] args) {
		IDRemapper remapper=new IDRemapper(new File("bin/org/cheminfo/hook/nemo/nmr/prediction/tests/test2.mol"));	
		remapper.remap();
	}
}
