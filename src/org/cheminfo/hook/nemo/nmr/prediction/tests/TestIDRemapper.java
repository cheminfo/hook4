package org.cheminfo.hook.nemo.nmr.prediction.tests;

import java.io.IOException;

import org.cheminfo.hook.nemo.nmr.prediction.IDRemapper;

public class TestIDRemapper {


	public static void main(String[] args) throws IOException {
		String[] parameters={"bin/org/cheminfo/hook/nemo/nmr/prediction/tests/741-00000.mol"};
		IDRemapper.main(parameters);
		
		String[] parameters2={"bin/org/cheminfo/hook/nemo/nmr/prediction/tests/741-00001.mol"};
		IDRemapper.main(parameters2);
		
		
		
	}
}
