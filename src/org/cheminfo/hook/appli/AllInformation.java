package org.cheminfo.hook.appli;

import java.io.File;
import java.util.prefs.Preferences;

import org.cheminfo.hook.framework.BasicDisplay;
import org.cheminfo.hook.nemo.Nemo;

/**
* Contains all the information shared between all the classes.
*
* @author Luc Patiny
*/

class AllInformation {
 
	Preferences prefs=Preferences.userNodeForPackage(getClass());
 	ActionSaveXml actionSaveXml;
	ActionExit actionExit;
	boolean changed=false;
	File currentFile;
	BasicDisplay rootDisplay=null;
	Nemo nemo;
	
	public AllInformation () {
		
	}

	void setCurrentFile(File currentFile){
		this.currentFile=currentFile;	
	}
	
	File getCurrentFile() {
		return currentFile;
	}

	void setRootDisplay(BasicDisplay rootDisplay)
	{
		this.rootDisplay=rootDisplay;
	}

	void setNemo(Nemo nemo)
	{
		this.nemo=nemo;
	}
	
	Nemo getNemo()
	{
		return this.nemo;
	}
	
	/** Sets if the table has been changed
	 */
	void setChanged(boolean changed) {
		this.changed=changed;
		actionSaveXml.setEnabled(changed);
	}
	
	/** Returns true if the table has been changed
	 */
	boolean isChanged() {
		return changed;
	}

	String getDefaultPath() {
		return prefs.get("defaultPath",System.getProperty("user.dir"));
	}
	
	void setDefaultPath(File file) {
		prefs.put("defaultPath",file.getAbsolutePath());
	}
	
	void setActionExit(ActionExit actionExit) {
		this.actionExit=actionExit;
	}
	
	ActionExit getActionExit() {
		return actionExit;
	}
	
	void setActionSaveXml(ActionSaveXml actionSaveXml) {
		this.actionSaveXml=actionSaveXml;
	}
	
	ActionSaveXml getActionSaveXml() {
		return actionSaveXml;
	}
	
}





