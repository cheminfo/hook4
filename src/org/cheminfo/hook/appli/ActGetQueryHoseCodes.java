package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ActGetQueryHoseCodes extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4058956715248191441L;
	AllInformation allInformation;

	public ActGetQueryHoseCodes(AllInformation allInformation) {
		super("Query hose codes");
		this.allInformation = allInformation;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(SHORT_DESCRIPTION, "get query hose codes");
	}

	public void actionPerformed(ActionEvent e) {
		String queryCodes = allInformation.getNemo().getQueryHoseCodes("1H");
		if (queryCodes != null)
			System.out.println("query\n"+queryCodes);
	}

}
