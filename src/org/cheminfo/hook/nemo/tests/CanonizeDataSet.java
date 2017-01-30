package org.cheminfo.hook.nemo.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.cheminfo.hook.nemo.nmr.ProprietaryTools;


public class CanonizeDataSet {

	public static void main(String[] args) {
		try {
			String path ="/Users/acastillo/git/autolearning/src/related/data/molfiles/"; 
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					BufferedReader in = new BufferedReader(new FileReader(path+listOfFiles[i].getName())); 
					
					String molfile="";
			        String line = null; //not declared within while loop
			
			        while (( line = in.readLine()) != null){
			        	molfile+=line;
			        	molfile+=System.getProperty("line.separator");
			        }
					
			        in.close();
				
			        String canonized = ProprietaryTools.canonizeMolfile(molfile);
			        
			        Writer writer = null;

			        try {
			            writer = new BufferedWriter(new OutputStreamWriter(
			                  new FileOutputStream(path+listOfFiles[i].getName().replace(".mol", "_can.mol")), "utf-8"));
			            writer.write(canonized);
			        } catch (IOException ex) {
			          // report
			        } finally {
			           try {writer.close();} catch (Exception ex) {}
			        }
			        	
				} 
				
			}
			
		} catch (IOException ex) {
			ex.printStackTrace(System.out);
		}
	}
}
