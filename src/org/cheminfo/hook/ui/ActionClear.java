package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;




public class ActionClear extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;

	public ActionClear(NemoInstance nemoInstance)
    {    	
        super("Clear");
		this.nemoInstance = nemoInstance;
    }

    public void actionPerformed(ActionEvent e)
    {
    	try {
			if(nemoInstance.isDirty()) {
				int answer=JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()),"Do you want to save " + nemoInstance.getRessource() + "?");		
				
				switch (answer) {
			    	case JOptionPane.NO_OPTION:
			    		break;
			    	case JOptionPane.YES_OPTION:
			    		nemoInstance.save();
			    		break;	
			    	default :
			    		return;
			    }
			}
	
	        nemoInstance.clear();
		} 
		catch(Exception ex)
		{
			JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(nemoInstance.getNemo()), ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
    }
}