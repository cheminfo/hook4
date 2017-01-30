package tests.nemo.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.json.JSONArray;
import org.json.JSONObject;

public class TestMass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String filename="bin/tests/nemo/scripting/test.js";

		File scriptFile = new File(filename);
		String script = "";
		if  (scriptFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
				String inLine;
				while ((inLine = reader.readLine()) != null)
					script += inLine + "\n";
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else {
			System.out.println("File " + filename + "does not exist");
		}
		ScriptingInstance interpreter = new ScriptingInstance();
		JSONArray toReturn=new JSONArray();
		interpreter.addObjectToScope("toReturn", toReturn);
		JSONObject result = interpreter.runScript(script);
		
		System.out.println(toReturn.toString());
		System.out.println(toReturn.length());
		
		System.out.println(result.toString());
		System.out.println(result.length());
		
	}


}
