package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class ActionAutoFormat extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;

	public ActionAutoFormat(NemoInstance nemoInstance)
    {    	
        super("Auto Format Layout");
		this.nemoInstance = nemoInstance;
    }

    public void actionPerformed(ActionEvent e)
    {
//		ImageIcon logo = new ImageIcon(getClass().getResource("/..."));					
//		ImageEntity logoEntity = new ImageEntity(logo.getImage());
//		logoEntity.setLocation(0, 0);
//		    	
//      nemoInstance.getNemo().addTextColRow(0, (int)logoEntity.getHeight() + 20, 65, 11, "MEASUREMENT_TYPE:\t{DATA TYPE}\nMEASUREMENT:\t{.OBSERVE NUCLEUS}\nFREQUENCY:\t\t{.OBSERVE FREQUENCY}\nNMR_TITLE:\t\t{TITLE}\nDevice:\t\t{ORIGIN}\nSAMPLE_NAME:\t{$NAME}\nMEASUREMENT_DATE:\t{$DATE}\nOPERATOR:\t\t{$USER}\nNO_SCANS:\t\t{$NS}\nSOLVENT:\t\t{.SOLVENT NAME}\nTEMPERATURE:\t{$TE}\nEXPERIMENT_TYPE:\t{$EXP}\n\nBRUKER_PROBEHEAD:\t{$PROBHD}\nBRUKER_EXPNO:\t{$EXPNO}");
//		nemoInstance.getNemo().getMainDisplay().addEntity(logoEntity);
    }
}