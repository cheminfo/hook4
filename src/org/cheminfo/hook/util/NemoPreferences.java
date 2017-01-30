package org.cheminfo.hook.util;

import java.io.File;
import java.security.AccessControlException;

import org.cheminfo.hook.util.Preferences.Property;

public class NemoPreferences extends Preferences {
	private static final long serialVersionUID = 1L;

	public Property<File> MOST_RECENT_PATH;
	public Property<Boolean> USE_NEW_INTEGRAL_DISPLAY;
	public Property<Boolean> PROVIDE_VIEW_XML_FILE_FUNCTIONS;
	//public Property<Character> SHOW_ALL_SHORT_CUT;

	private static NemoPreferences instance = null;

	public static NemoPreferences getInstance() {
		if (instance == null)
			instance = new NemoPreferences();
		return instance;
	}

	public NemoPreferences() {
		try{
			MOST_RECENT_PATH = createProperty("Most Recent Path", "hidden", new File(System.getProperty("user.home")));
			USE_NEW_INTEGRAL_DISPLAY = createProperty("Use New Integral Display", Boolean.TRUE);
			PROVIDE_VIEW_XML_FILE_FUNCTIONS = createProperty("Provide View XML File Import/Export", Boolean.FALSE);
			//SHOW_ALL_SHORT_CUT = createProperty("It Defines the short cut key map for the \"Show all\" action", new Character('q'));
		}
		catch(AccessControlException e){
			//We are in an applet, so I'll set this property to null
			MOST_RECENT_PATH = null;
			USE_NEW_INTEGRAL_DISPLAY = null;
			PROVIDE_VIEW_XML_FILE_FUNCTIONS = null;
			//SHOW_ALL_SHORT_CUT = new Property<Character>("It Defines the short cut key map for the \"Show all\" action", new Character('q'));
		}
		
	}
	
	//public getInstance()
}
