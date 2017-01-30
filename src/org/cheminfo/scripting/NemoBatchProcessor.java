package org.cheminfo.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.cheminfo.function.scripting.ScriptingInstance;
import org.json.JSONObject;

/**
 * 
 * @author acastillo
 *
 */
public class NemoBatchProcessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: <prog> <scriptFile>");
			System.exit(0);
		}
		File scriptFile = new File(args[0]);
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
			System.out.println("File " + args[0] + "does not exist");
		}
		String pluginsFolder = "plugins";
        if(args.length>1)
        	pluginsFolder=args[1];
		
		ScriptingInstance interpreter = new ScriptingInstance(pluginsFolder);
		interpreter.setSafePath("./");
		JSONObject result = interpreter.runScript(script);
		System.out.println(result.toString());
	}
}
