package org.cheminfo.hook.nemo.nmr.prediction.tests;

import java.io.File;

import org.cheminfo.hook.nemo.nmr.prediction.PredictionRewriter;

public class PredictionRewriterTest {

	final static String DIRECTORY="bin/org/cheminfo/hook/nemo/nmr/prediction/tests";
	final static String TYPE="nmr_dft_TZVP_geom_dft_SVP";
	
	
	public static void main(String[] args) {
		PredictionRewriter rewriter = new PredictionRewriter(new File(DIRECTORY), TYPE);
		rewriter.rewrite();
	}
}
