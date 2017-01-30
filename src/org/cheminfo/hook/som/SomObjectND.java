package org.cheminfo.hook.som;

import java.awt.Color;
import java.text.DecimalFormat;

/**
 * Multidimentional SOM object
 * 
 * @author lpatiny
 *
 */

public class SomObjectND extends SomObject {

	double data[];
	
	final static double LEARNING_SPEED=0.1;
	
	SomObjectND (int dimension) {
		this.data=new double[dimension];
	}
	
	SomObjectND (double[] data) {
		this.data=data;
	}
	
	
	public double distanceTo(SomObject object) {
		double sum=0;
		for (int i=0; i<data.length; i++) {
			sum+=(((SomObjectND)object).data[i]-data[i]) * (((SomObjectND)object).data[i]-data[i]);
		}
		return Math.sqrt(sum);
	}

	public void initialize() {
		for (int i=0; i<data.length; i++) {
			data[i]=Math.random();
		}
	}
	
	public Color getColor() {
		return getColor("rgb");
	}
	
	// we may select what we want to color. For each dimension it may be r,g,b or 0
	
	public Color getColor(String colorScheme) {
		int r=0, g=0, b=0;
		for (int i=0; i<colorScheme.length(); i++) {
			if (colorScheme.charAt(i)=='r') r=(int)(data[i]*255);
			if (colorScheme.charAt(i)=='g') g=(int)(data[i]*255);
			if (colorScheme.charAt(i)=='b') b=(int)(data[i]*255);
		}
		return new Color(r,g,b);
	}

	public void learn(SomObject object, double distanceEffect, double epochEffect) {
		for (int i=0; i<data.length; i++) {
			data[i]=data[i]-(data[i]-((SomObjectND)object).data[i])*LEARNING_SPEED*distanceEffect*epochEffect;
		}		
	}
	@Override
	public String toString(){
		DecimalFormat format = new DecimalFormat("#.########");
		String toReturn="";
		for(int i=data.length-1;i>0;i--)
			toReturn=" "+format.format(data[i])+toReturn;
		toReturn=format.format(data[0])+toReturn;
		return toReturn;
		
	}
}
