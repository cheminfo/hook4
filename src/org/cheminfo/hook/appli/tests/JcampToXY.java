package org.cheminfo.hook.appli.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class JcampToXY {

	
	public static String filter(String value) {
	//	HookServlet nemo=new HookServlet();
	//	nemo.addJcamp(null,value);
	//	System.out.println(nemo.getSpectraData().toXY());
		return "AB";
	//	return nemo.getSpectraData().toXY();
	}
	
	public static void main(String[] args) throws IOException {
		URL url=new URL("file:///Users/lpatiny/Documents/workspace6/hook3/src/org/cheminfo/hook/appli/test/jcamp.jdx");
		
		BufferedReader inputFile=new BufferedReader(new InputStreamReader(url.openStream()), 262144);
		String currentLine=null;
		try {
			currentLine=inputFile.readLine();
		} catch (IOException e) {}

		StringBuffer value=new StringBuffer();
		
		while (currentLine!=null) {
			value.append(currentLine+"\r\n");
			currentLine=inputFile.readLine();
		}
			
		inputFile.close();
		
		JcampToXY.filter(value.toString());
	}
}
