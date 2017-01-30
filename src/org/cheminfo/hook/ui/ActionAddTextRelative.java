package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ActionAddTextRelative extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;

	public ActionAddTextRelative(NemoInstance nemoInstance)
    {    	
        super("Add Relative Text Box");
		this.nemoInstance = nemoInstance;
    }

    public void actionPerformed(ActionEvent e)
    {
    	//TODO add text
    	//nemoInstance.getNemo().addTextRelative(0, 0, 800, 400, "TextRelative");
    }
}