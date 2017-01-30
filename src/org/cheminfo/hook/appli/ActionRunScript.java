package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.json.JSONException;

public class ActionRunScript extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2842372586476374583L;
	AllInformation allInformation;

	public ActionRunScript(AllInformation allInformation) {
		super("Run script");

		this.allInformation = allInformation;
		putValue(SHORT_DESCRIPTION, "Run script");

	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser(allInformation
				.getDefaultPath());
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION
				&& fileChooser.getSelectedFile() != null) {
			File file = fileChooser.getSelectedFile().getAbsoluteFile();
			runScriptFromFile(file);
		}
	}

	private void runScriptFromFile(File file)  {
		String scriptData = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String  line = null;
			while ((line = in.readLine()) != null) {
				scriptData += line + "\n";
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			allInformation.getNemo().runScript(scriptData);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
