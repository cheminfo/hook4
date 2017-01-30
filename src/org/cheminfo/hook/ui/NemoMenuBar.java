package org.cheminfo.hook.ui;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.cheminfo.hook.util.NemoPreferences;

public class NemoMenuBar extends JMenuBar implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;

	protected NemoInstance nemoInstance;
	
	protected Action reloadAction;
	protected Action saveAction;
	protected Action saveRevertBareJdxAction;
	
	public NemoMenuBar(NemoInstance<?> nemoInstance) {
		this.nemoInstance = nemoInstance;
		nemoInstance.addPropertyChangeListener(this);
		
		JMenu menu;

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		
		if(nemoInstance.loadSaveHandler instanceof FileLoadSaveHandler)
			menu.add(new JMenuItem(new ActionOpenExtendedJcamp(nemoInstance)));

		if(NemoPreferences.getInstance().get(NemoPreferences.getInstance().PROVIDE_VIEW_XML_FILE_FUNCTIONS))
			menu.add(new ActionOpenXml(nemoInstance));
			
		reloadAction = new ActionReloadExtendedJcamp(nemoInstance);
		reloadAction.setEnabled(nemoInstance.hasOpenedJdx());
		menu.add(new JMenuItem(reloadAction));
		saveAction = new ActionSaveExtendedJcamp(nemoInstance);
		saveAction.setEnabled(nemoInstance.isDirty());
		menu.add(new JMenuItem(saveAction));		
		if(NemoPreferences.getInstance().get(NemoPreferences.getInstance().PROVIDE_VIEW_XML_FILE_FUNCTIONS))
			menu.add(new ActionSaveXml(nemoInstance));

		saveRevertBareJdxAction = new ActionSaveRevertBareJcamp(nemoInstance);
		saveRevertBareJdxAction.setEnabled(nemoInstance.getRessource()!=null);
		menu.add(new JMenuItem(saveRevertBareJdxAction));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActionExportPdf(nemoInstance)));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActionPrint(nemoInstance)));
		this.add(menu);
		
        menu = new JMenu("Edit");
        menu.add(new JMenuItem(new ActionClear(nemoInstance)));
        this.add(menu);
		
        menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_T);
		if(nemoInstance.loadSaveHandler instanceof FileLoadSaveHandler)
			menu.add(new JMenuItem(new ActionAddJcamp(nemoInstance)));

		menu.add(new ActionSetNmrString(nemoInstance));
		menu.addSeparator();
		menu.add(new JMenuItem(new ActionGetNmrString(nemoInstance)));

		menu.addSeparator();
		menu.add(new ActionAddMolfile(nemoInstance));
//		menu.add(new ActionAddText(nemoInstance));
		
//		menu.addSeparator();
//		menu.add(new ActionAutoFormat(nemoInstance));
//		menu.add(new ActionAddTextRelative(nemoInstance));
		
		menu.addSeparator();
		menu.add(new ActionPreferences(nemoInstance));

		this.add(menu);

        menu = new JMenu("Help");
        menu.add(new JMenuItem(new ActionAbout(nemoInstance)));
        menu.add(new JMenuItem(new ActionWiki(nemoInstance)));
        this.add(menu);
}

	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("dirty"))
		{
			boolean enable = !nemoInstance.hasMultipleJdxLoaded() && nemoInstance.hasOpenedJdx() && nemoInstance.isDirty(); 
			saveAction.setEnabled(enable);
		}
		else if(evt.getPropertyName().equals("ressource"))
		{
			reloadAction.setEnabled(nemoInstance.hasOpenedJdx());			
			saveRevertBareJdxAction.setEnabled(!nemoInstance.hasMultipleJdxLoaded() && nemoInstance.hasOpenedJdx());
		}
	}
		
}
