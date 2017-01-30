package org.cheminfo.hook.fragments;


/**
 * 
 * This class represents the occurrence of a Fragment. 
 * It is essentially a mutable integer.
 * 
 * 
 * @author Marco Engeler
 *
 */
public class Occurrence {
	private int occurrence;

	public Occurrence() {
		super();
		this.occurrence = 0;
	}

	public Occurrence(int occurrence) {
		super();
		this.occurrence = occurrence;
	}

	public int getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(int occurrence) {
		this.occurrence = occurrence;
	}
	
	public int increaseOccurrence() {
		return ++occurrence;
	}
	
	public int decreaseOccurrence() {
		return --occurrence;
	}
}
