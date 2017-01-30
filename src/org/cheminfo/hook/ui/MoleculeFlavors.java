package org.cheminfo.hook.ui;

/**
 * <p>Title: DD_GUI</p>
 * Copyright 1997-2011 Actelion Ltd., Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.
 * @author Christian Rufener
 * @version 1.0
 */

import java.awt.datatransfer.DataFlavor;

import com.actelion.research.chem.Molecule;

public class MoleculeFlavors {
	// The DF_SERIALIZEDOBJECT is needed for the inter-jvm drag&drop!
	// If you use this don't obfuscate StereoMolecule
	static class MyFlavor extends DataFlavor {
		public MyFlavor(Class representationClass, String humanPresentableName) {
			super(representationClass, humanPresentableName);
			try {
				ClassLoader cl = this.getClass().getClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
			} catch (Throwable ex) {
			}
		}
	}

	public static final DataFlavor DF_SERIALIZEDOBJECT = new MyFlavor(Molecule.class, "Actelion Molecule Class");
	public static final DataFlavor DF_MDLMOLFILE = new DataFlavor("chemical/x-mdl-molfile;class=java.lang.String", "MDL Molfile");
	public static final DataFlavor DF_SMILES = new DataFlavor("chemical/x-daylight-smiles;class=java.lang.String", "Daylight Smiles");
	public static final DataFlavor[] FLAVORS = {DF_SERIALIZEDOBJECT, DF_MDLMOLFILE, DF_SMILES};
}
