package org.cheminfo.hook.nemo.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.cheminfo.hook.nemo.nmr.ProprietaryTools;

public class ProprietaryToolsTest {

	
	
	public static void main(String[] args) {
		try {
			BufferedReader in = new BufferedReader(new FileReader("bin/org/cheminfo/hook/nemo/nmr/tests/bicyclo.mol")); 
			
			String molfile="";
	        String line = null; //not declared within while loop
	
	        while (( line = in.readLine()) != null){
	        	molfile+=line;
	        	molfile+=System.getProperty("line.separator");
	        }
			
	        in.close();
		
	        System.out.println(ProprietaryTools.canonizeMolfile(molfile));
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
		}
	}
}
