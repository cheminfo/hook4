package org.cheminfo.hook.appli;

import java.util.Hashtable;

import javax.swing.JDialog;

import org.cheminfo.hook.framework.InteractiveSurface;
import org.cheminfo.hook.nemo.SpectraDisplay;

public class PrintSetupDialog extends JDialog
{
	private AllInformation allInformation;
	
	public PrintSetupDialog(AllInformation allInformation)
	{
		super();
		
		this.setModal(true);
		this.setSize(400, 300);
		this.setLocation(200,200);

		
		this.allInformation=allInformation;
		this.getContentPane().setLayout(null);
		
		InteractiveSurface interactions=new InteractiveSurface();
		interactions.setSize(this.getWidth()-40, this.getHeight()-40);
		interactions.setLocation(20,20);
		
		this.getContentPane().add(interactions);
		
		SpectraDisplay spectraDisplay=new SpectraDisplay();
		interactions.addEntity(spectraDisplay);

		
		Hashtable helpers=new Hashtable();
		spectraDisplay.init(allInformation.getNemo().getXMLEmbedded(), interactions.getWidth(), interactions.getHeight(), helpers);

		interactions.setCurrentAction(null);
		interactions.clearActiveEntities();

		this.validate();
		this.show();
	}
	

}