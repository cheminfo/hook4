package org.cheminfo.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.cheminfo.scripting.spectradata.SD;

public class TestScripting {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	//	x = java.lang.reflect.Array.newInstance(java.lang.Integer, 10)<
		/*String table="1	1H	2			1	2	d	5	3	d	15\n";
		table+="2	1H	2.02			1	3	d	3\n";
		table+="3	1H	4			1";*/
		
		/*File ftable=new File("/home/acastillo/c13pred.txt");
		String table = getContents(ftable);
		System.out.println(table);
		File ssfile = new File("/home/acastillo/mol13C.txt");
		String mol=getContents(ssfile);
		System.out.println(mol);
		
		//JSONArray res = SD.SSMutator(table, mol, 2, "{'nucleus':'13C', 'shiftdev':1, 'jdev':0}");
		//System.out.println(res);
		ScriptingInstance interpreter = new ScriptingInstance("");
		JSONObject toReturn = interpreter.runScript("var list=SD.SSMutator('"+escape(table)+"','"+escape(mol)+"',2,{'nucleus':'13C', 'shiftdev':2, 'jdev':0});jexport('ss',list);");
		System.out.println(toReturn);*/
		
		ScriptingInstance interpreter = new ScriptingInstance("/Users/acastillo/WScripting/servletScript/WebContent/WEB-INF/plugins/");
		interpreter.setSafePath("./");
		String spinus = getContents(new File("/Users/acastillo/Dropbox/2andres/5molecule/00000/H1/spinus.predict"));
		String nmrShiftDB=getContents(new File("/Users/acastillo/Dropbox/2andres/5molecule/00000/c13/NMRSHIFTDB_0.txt"));
		
		//int[][] HSQC = SD.simulateHSQC(spinus, nmrShiftDB, 0.15, 1.5);
		SD sd = new SD();
		int[][] TOCSY = null;//sd.simulateTOCSY(spinus, 0.15);
		for(int i=0;i<1024;i++){
			for(int j=0;j<1024;j++)
				System.out.print(TOCSY[i][j]+"");
				//System.out.print(HSQC[i][j]+"");
			System.out.println("");
		}
		/*String script = "var INPUT_FILE='/Users/acastillo/scripttest/mol13C.txt';"
		+"var NSpectra=2;"
		+"var molfile = getUrlContent('file://'+INPUT_FILE);"
		//+"var url = 'http://nmrshiftdb.chemie.uni-mainz.de/portal/js_pane/P-Predict;jsessionid=1';"
		//+"var html = Default.getUrlContent(url,{'Datei':INPUT_FILE});"
		//+"var prediction = SD.NMRShiftDBResultParser(html);"
		
		//+"var prediction = SD.nmrShiftDBPred13C(molfile);"
		+"var prediction = SD.spinusPred1H(molfile);"
		
		+"out.println(prediction);"
		//+"out.println(molfile);"
		+"var parameters = SD.SSMutator(prediction,molfile,NSpectra,{'nucleus':'1H','shiftdev':0.2,'jdev':0});"
		+"out.println(unescape(parameters.get(0)));"
		+"out.println(unescape(parameters.get(1)));"
		+"var calMassPattern=ChemCalc.getJcamp('C8H9NO2', {});"
		+"out.println(calMassPattern);";
		//+"jexport('ss',parameters);";*/
		
		/*String script = "var maxSize=4;";
		script+="var m = new Array(maxSize);";
		script+="for (var i=0;i<maxSize;i++) {";
		script+="	m[i]=new Array(maxSize);";
		script+="}";
		script+="var t = new Array(maxSize);";
		script+="for (var i=0;i<maxSize;i++) {";
		script+="	t[i]=new Array(maxSize);";
		script+="}";
		script+="m[0][0]=1;m[0][1]=0.95;m[0][2]=0.35;m[0][3]=0.2;";
		script+="m[1][0]=0.95;m[1][1]=1;m[1][2]=0.25;m[1][3]=0.3;";
		script+="m[2][0]=0.35;m[2][1]=0.25;m[2][2]=1;m[2][3]=0.93;";
		script+="m[3][0]=0.2;m[3][1]=0.3;m[3][2]=0.93;m[3][3]=1;";
		
		script+="t[0][0]=1;t[0][1]=1;t[1][0]=1;t[1][1]=1;";
		script+="t[2][2]=1;t[2][3]=1;t[3][2]=1;t[3][3]=1;";
		
		script+="var stats = Statistic.distribution(m,t);";
		script+="jexport('stats',stats);";
		script+="var roc = Statistic.ROCAnalysis(m, t,100);";
		script+="jexport('ROC',roc);";
		JSONObject toReturn = interpreter.runScript(script);*/
		
		/*String script = "var spectraData=SD.loadJcamp('file:///Users/acastillo/Downloads/francois2.jdx');";
		script+="var peaks = SD.simplePeakDetection(spectraData, {threshold:0.1});";
		script+="jexport('peaks',peaks);";
		*/
		
		//String script = "var spectraData=SD.loadJCamp('file:///var/www/html/IR_data/redPaints/01_1.JDX');";
		//script+="var dataXY=spectraData.getXYData();";
		
		//String script = "var molfile = getUrlContent('file:///home/acastillo/Documents/dataset/output/00000/mol2d.mol');";
		//script+="jexport('molfile',molfile);";
		//script+="var prediction=SD.spinusPred1H(molfile);";
		
		
		//JSONObject toReturn = interpreter.runScript(script);
		//System.out.println(toReturn);

		/*SpectraData spectraData = SD.loadJcamp("file:///Users/acastillo/Downloads/francois2.jdx");
		JSONObject param = new JSONObject();
		try {
			param.accumulate("resolution", 0.1);
			param.accumulate("threshold", 0.1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		double[][] peaks = SD.simplePeakDetection(spectraData, param);
		for(int i=0;i<peaks.length;i++)
			System.out.println(peaks[i][0]+", "+peaks[i][1]);
		*/
		
		/*File ftable=new File("/home/acastillo/Downloads/1186373.mol");
		String table = getContents(ftable);
		String prediction=SD.spinusPred1H(table);
		System.out.println(prediction);*/
	}
	
	public static String escape(String value) {
		return value.replaceAll("\r","\\\\r").replaceAll("\n","\\\\n");
	}
	
	static public String getContents(File aFile) {
	    //...checks on aFile are elided
	    StringBuilder contents = new StringBuilder();
	    
	    try {
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; //not declared within while loop
	        /*
	        * readLine is a bit quirky :
	        * it returns the content of a line MINUS the newline.
	        * it returns null only for the END of the stream.
	        * it returns an empty String if two newlines appear in a row.
	        */
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    return contents.toString();
	}

}
