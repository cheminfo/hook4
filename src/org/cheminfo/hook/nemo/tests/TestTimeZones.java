package org.cheminfo.hook.nemo.tests;

import java.util.TimeZone;

/**
 * 
 * Tests the available timezones used for ant.
 * 
 * @author Marco Engeler
 *
 */

public class TestTimeZones {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] timeZoneIDS = TimeZone.getAvailableIDs();
		System.out.println("available time zones");
		for (int i = 0; i < timeZoneIDS.length; i++) {
			System.out.println(i +":\t"+timeZoneIDS[i]);
		}
	
	}

}
