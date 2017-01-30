package org.cheminfo.hook.ui;

import java.math.BigDecimal;

/**
 * Do not modify this file (.java) it is generated automatically from the .java.tpl file
 *
 */
public class Version {

	public static final BigDecimal VERSION = new BigDecimal("@VERSION@");
	public static final int BUILD = @BUILD@;
	public static final String BUILD_DATE = "@BUILDDATE@";
	public static final String APPNAME = "@APPLICATION@";
	
	public static final String BANNER = APPNAME + " / Version: " + VERSION + " (build: " + Version.BUILD + " / " + BUILD_DATE + ")";
	 
	private Version() 
	{
	}
}
