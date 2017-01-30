package org.cheminfo.hook.som;

import java.awt.Color;

public abstract class SomObject {

		public abstract double distanceTo(SomObject object);
		
		public abstract void initialize();
	
		public abstract Color getColor();
		
		
		/** 
		 * 
		 * The influence of distance and epoch is not easy to define ...
		 * 
		 * @param object
		 * @param distance
		 * @param epoch
		 */
		public abstract void learn(SomObject object, double distance, double epoch);

}
