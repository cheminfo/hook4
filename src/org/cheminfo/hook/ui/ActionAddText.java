package org.cheminfo.hook.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ActionAddText extends AbstractAction
{
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;

	public ActionAddText(NemoInstance nemoInstance)
    {    	
        super("Add Text Box");
		this.nemoInstance = nemoInstance;
    }

    public void actionPerformed(ActionEvent e)
    {
        nemoInstance.getNemo().addText(50, 50, 80, 40, "Text");
    }
}