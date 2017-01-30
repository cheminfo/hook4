package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


class ActionImportJcampFromURL extends AbstractAction {
	
	AllInformation allInformation;
	
	public ActionImportJcampFromURL(AllInformation allInformation) {
		super("Add jcamp file from URL");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
		putValue(SHORT_DESCRIPTION, "URL of JDX file");
		this.allInformation=allInformation;
	}
	
	public void actionPerformed (ActionEvent e) {
		
		
		String url = (String)JOptionPane.showInputDialog(null, "Paste Description", "Create Spectrum", JOptionPane.PLAIN_MESSAGE, null, null, "");		

		if (url != null) {
			processUrl(url);
		}
	}
	


	private void processUrl (String stringUrl)
	{

//		Converter tempConverter=Converter.getConverter("Jcamp");
//		SpectraData	spectraData = new SpectraData();
		URL correctUrl=null;
		// we need the URL
		// we first try to convert it directly
		try {
			correctUrl=new URL(stringUrl);
		} catch (MalformedURLException e) {System.out.println("Not found : "+stringUrl);return;}

		allInformation.getNemo().addJcamp(stringUrl);
	}
	
}
