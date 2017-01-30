package org.cheminfo.hook.nemo.nmr;

import com.actelion.research.chem.MolfileCreator;
import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.contrib.HydrogenHandler;
import com.actelion.research.epfl.EPFLUtils;

import java.io.*;

public class CanonizeMolfile {
	final static boolean DEBUG=false;
	
	static String canonizeMolfile(String molfile) {
		MolfileParser parser = new MolfileParser();
		StereoMolecule inputMolecule = new StereoMolecule();
		if (parser.parse(inputMolecule, molfile)) {
			StereoMolecule canonizedMolecule = EPFLUtils.getCanonicalForm(inputMolecule);
			HydrogenHandler.addImplicitHydrogens(canonizedMolecule);
			// we need to check if there is no superimposition in atoms
			double firstX, firstY, secondX, secondY, newX, newY;
			
			if (DEBUG) System.out.println("Number of atoms: "+canonizedMolecule.getAllAtoms());
			
			for (int i=0; i<(canonizedMolecule.getAllAtoms()-1); i++) {
				firstX=Math.round(canonizedMolecule.getAtomX(i)*1000);
				firstY=Math.round(canonizedMolecule.getAtomY(i)*1000);
				for (int j=i+1; j<canonizedMolecule.getAllAtoms(); j++) {
					secondX=Math.round(canonizedMolecule.getAtomX(j)*1000);
					secondY=Math.round(canonizedMolecule.getAtomY(j)*1000);
					if ((firstX==secondX) && (firstY==secondY)) {
						// two XY equals coordinates
						// we need to find new coordinates
						newX=firstX;
						newY=firstY;
						boolean unique;
						do {
							unique=true;
							newX=newX+1;
							for (int k=0; k<(canonizedMolecule.getAllAtoms()); k++) {
								secondX=Math.round(canonizedMolecule.getAtomX(k)*1000);
								secondY=Math.round(canonizedMolecule.getAtomY(k)*1000);
								if ((newX==secondX) && (newY==secondY)) {
									unique=false;
								}
							}
						} while (!unique);
						canonizedMolecule.setAtomX(i, newX/1000);
						canonizedMolecule.setAtomY(i, newY/1000);
						if (DEBUG) System.out.println("We found identical coordinates. Old: "+firstX+" - "+firstY+" New: "+newX+" - "+newY);
					}
				}				
			}
			
			
			
			MolfileCreator creator = new MolfileCreator(canonizedMolecule);
			return creator.getMolfile().replaceAll("Actelion Java MolfileCreator 1.0","Moldraw");
		}
		return null;
	}
	
	/**
	 * @param args
	 * java -cp hook3-proprietary.jar org.cheminfo.hook.nemo.nmr.CanonizeMolfile input.mol output.mol
	 */
	public static void main(String[] args) throws IOException {
		String inputMolfile=args[0];
		String outputMolfile=args[1];
		StringBuffer molfile=new StringBuffer();
		
		BufferedReader bufReader=new BufferedReader(new FileReader(inputMolfile));
		String line=null;
		while ((line=bufReader.readLine())!=null) {
			molfile.append(line+"\n");;
		}
		bufReader.close();
		
		if (DEBUG) System.out.println("Input molfile: "+molfile.toString());
		
		BufferedWriter bufWriter=new BufferedWriter(new FileWriter(outputMolfile));
		bufWriter.write(canonizeMolfile(molfile.toString()));
		bufWriter.close();
	}

	
	
}
