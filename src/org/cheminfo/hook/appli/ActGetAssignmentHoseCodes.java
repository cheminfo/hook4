package org.cheminfo.hook.appli;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

public class ActGetAssignmentHoseCodes extends AbstractAction {
//	private String dbUrl = "jdbc:mysql://localhost:3306/NMRPrediction";
//	private String dbUser = "NMRPrediction";
//	private String dbPassword = "NMRPrediction";

//	private Connection conn = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 5113167187188906928L;
	AllInformation allInformation;

	public ActGetAssignmentHoseCodes(AllInformation allInformation) {
		super("Assignment hose codes");
		this.allInformation = allInformation;
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(SHORT_DESCRIPTION, "get assignment hose codes");
	}

	public void actionPerformed(ActionEvent e) {
		String assignmentString = allInformation.getNemo()
				.getHoseCodes4Assignment();
		if (assignmentString != null)
			System.out.println(assignmentString);

	}

}
