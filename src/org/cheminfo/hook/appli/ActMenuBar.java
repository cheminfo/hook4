package org.cheminfo.hook.appli;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

//import org.cheminfo.hook.scripting.ScriptingInstance;

/**
 * Contains the main menu and the events attached to it
 * 
 */

class ActMenuBar extends JMenuBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4597210498353029300L;
	AllInformation allInformation;

	public ActMenuBar(AllInformation allInformation) {
		super();

		this.allInformation = allInformation;

		JMenu menu;

		/* FILE menu */

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.add(new JMenuItem(new ActionOpenXml(allInformation)));
		menu.add(new JMenuItem(new ActionSaveXml(allInformation)));
		menu.add(new JMenuItem(new ActionSaveAsXml(allInformation)));

		menu.addSeparator();
		menu.add(new JMenuItem(new ActionPrint(allInformation)));
		menu.add(new JMenuItem(new ActionPrintReport(allInformation)));
		
		
		try {
			Class.forName ("com.lowagie.text.pdf.PdfWriter");
			menu.add(new JMenuItem(new ActionExportPDF(allInformation)));
		} catch (ClassNotFoundException e) {
			System.out.println("Can not initialize PDF export action: "+e.toString());
		}
		
		menu.addSeparator();

		menu.add(new JMenuItem(new ActionExit(allInformation)));

		this.add(menu);

		menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_T);
		menu.add(new JMenuItem(new ActionImportJcamp(allInformation)));
		menu.add(new JMenuItem(new ActionImportJcampV(allInformation)));
		menu.add(new JMenuItem(new ActionImportJcampFromURL(allInformation)));
		
		
		
		menu.add(new JMenuItem(new ActionImportPrediction(allInformation)));
		menu.add(new JMenuItem(new ActionSetPrediction(allInformation)));
		menu.add(new JMenuItem(new ActionSimulateSpectra(allInformation)));
		menu.add(new JMenuItem(new ActionMassFitting(allInformation)));
		
		menu.add(new JMenuItem(new ActionSpectraFromDescription(allInformation)));
		menu.add(new JMenuItem(new ActActionMolfile(allInformation)));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActionGetNmrTable(allInformation)));
		menu.add(new JMenuItem(new ActionGetPeaks(allInformation)));
		menu.add(new JMenuItem(new AddNMRTitle(allInformation)));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActionCopy(allInformation)));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActGetAssignmentHoseCodes(allInformation)));
		menu.add(new JMenuItem(new ActGetQueryHoseCodes(allInformation)));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActionRunScript(allInformation)));
		try {
			Class.forName("org.mozilla.javascript.Interpreter");
			menu.addSeparator();
			menu
					.add(new JMenuItem(new ActionOpenScriptConsole(
							allInformation)));
		} catch (ClassNotFoundException e) {
			System.out.println("Only old scripting available!");
		}
		this.add(menu);

		// menu = new JMenu("Help");
		// menu.setMnemonic(KeyEvent.VK_H);
		// menu.add(new JMenuItem(new ActionHelp(allInformation, "Contents",
		// "TOC")));
		// menu.add(new JMenuItem(new ActionHelp(allInformation, "Search",
		// "Search")));
		// this.add(menu);
	}
}
